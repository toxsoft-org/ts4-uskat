package org.toxsoft.uskat.s5.server.sequences.impl;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceSQL.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.S5DatabaseConfig.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.S5SequencePartitionConfig.*;

import java.util.*;

import javax.persistence.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.derivative.*;
import org.toxsoft.core.tslib.coll.derivative.Queue;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.maintenance.*;
import org.toxsoft.uskat.s5.utils.*;
import org.toxsoft.uskat.s5.utils.collections.*;

/**
 * Управление разделами таблиц.
 *
 * @author mvk
 */
abstract class S5AbstractPartitionManager {

  /**
   * Владелец менеджера.
   */
  private final String owner;

  /**
   * Фабрика менеджеров постоянства.
   */
  private final EntityManagerFactory entityManagerFactory;

  /**
   * Фабрика последовательностей блоков.
   */
  private final IS5SequenceFactory<?> sequenceFactory;

  /**
   * Текущая конфигурация.
   */
  private IOptionSet configuration = IOptionSet.NULL;

  /**
   * Карта разделов таблиц хранения данных.
   * <p>
   * Ключ: имя таблицы хранения блоков значения;<br>
   * Значение: список описаний разделов (партиций) таблицы.
   */
  private final IMapEdit<String, ITimedListEdit<S5Partition>> partitionsByTable =
      new WrapperMap<>( new HashMap<String, ITimedListEdit<S5Partition>>() );

  /**
   * Список имен таблиц (блок-blob) запланированных проверки необходимости операции удаления разделов.
   */
  private final IQueue<IS5SequenceTableNames> partitionCandidates = new Queue<>();

  /**
   * Таймер автоматических выполнений операций над разделами таблиц (один раз в сутки по умолчанию).
   */
  private S5IntervalTimer autoTimer = new S5IntervalTimer( 24 * 60 * 60 * 1000 );

  /**
   * Constructor.
   *
   * @param aOwner String собственник мендежера
   * @param aEntityManagerFactory {@link EntityManagerFactory} фабрика менеджеров постоянства
   * @param aSequenceFactory {@link IS5SequenceFactory} фабрика последовательностей блоков
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5AbstractPartitionManager( String aOwner, EntityManagerFactory aEntityManagerFactory,
      IS5SequenceFactory<?> aSequenceFactory ) {
    TsNullArgumentRtException.checkNulls( aOwner, aEntityManagerFactory, aSequenceFactory );
    owner = aOwner;
    entityManagerFactory = aEntityManagerFactory;
    sequenceFactory = aSequenceFactory;
  }

  /**
   * Журнал
   */
  private final ILogger logger = LoggerWrapper.getLogger( LOG_PARTITION_ID );

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  @SuppressWarnings( "boxing" )
  public void setConfiguration( IOptionSet aConfiguration ) {
    TsNullArgumentRtException.checkNull( aConfiguration );
    configuration = aConfiguration;
    // Интервал(таймаут) между выполнениями обработки разделов в автоматическом режиме (мсек).
    long timeout = PARTITION_AUTO_TIMEOUT.getValue( aConfiguration ).asLong();
    autoTimer = new S5IntervalTimer( timeout );
    // Попытка захвата блокировки писателя
    if( !tryLockWriter( STR_DO_PARTITION_AFTER_START, logger ) ) {
      return;
    }
    try {
      // Журнал
      logger.info( MSG_PARTITION_CHECK_AFTER_STARTUP, owner, timeout );
      // Состояние после перезапуска сервера. Принудительная проверка разделов всех таблиц в ускоренном порядке
      // EntityManager em = entityManagerFactory.createEntityManager();
      // try {
      // // Планирование обработки всех таблиц
      // planAllTablesPartitionsCheck( logger );
      // // Обработка текущего состояния разделов
      // IOptionSetEdit configEdit = new OptionSet( configuration );
      // // Максимальное количество потоков удаления (без ограничений)
      // PARTITION_AUTO_THREADS_COUNT.setValue( configEdit, AvUtils.AV_N1 );
      // // Мощность поиска удаляемых данных (без ограничений)
      // PARTITION_AUTO_LOOKUP_COUNT.setValue( configEdit, AvUtils.AV_N1 );
      // // Запуск операции проверки разделов
      // doPartition( MSG_PARTITION_AUTHOR_INIT, configEdit );
      // }
      // finally {
      // em.close();
      // }
    }
    finally {
      unlockWriter();
    }
  }

  /**
   * Выполнение операций над разделами таблиц
   * <p>
   * Ничего не делает, если писателю не требуется выполнять операции над разделами (aForce = false и
   *
   * @param aAuthor String автор задачи (для журнала)
   * @param aArgs {@link IOptionSet} аргументы для операций над разделами (смотри {@link S5SequencePartitionConfig}).
   * @return {@link IS5SequencePartitionStat} статистика процесса выполнения операций
   * @throws TsNullArgumentRtException аргумент = null
   */
  public synchronized final IS5SequencePartitionStat partition( String aAuthor, IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aAuthor, aArgs );
    if( !tryLockWriter( STR_DO_PARTITION, logger ) ) {
      return IS5SequencePartitionStat.NONE;
    }
    // Признак принудительного выполнения операции
    boolean force = (S5SequencePartitionConfig.PARTITION_DOJOB_TIMEOUT.getValue( aArgs ).asLong() <= 0);
    // Возвращаемый результат
    IS5SequencePartitionStat retValue = IS5SequencePartitionStat.NONE;
    try {
      if( !force && !autoTimer.isOver() ) {
        return retValue;
      }
      // Планирование обработки разделов таблиц
      planAllTablesPartitionsCheck( logger );
      retValue = doPartition( aAuthor, aArgs );
      return retValue;

    }
    finally {
      if( autoTimer.isOver() && retValue.errorCount() == 0 ) {
        autoTimer.reset();
      }
      unlockWriter();
    }
  }

  // ------------------------------------------------------------------------------------
  // Методы для определения
  //
  /**
   * Получить блокировку писателя.
   *
   * @param aOwner String имя нового владельца блокировки писателя
   * @param aLogger {@link ILogger} журнал
   * @return boolean <b>true</b> блокировка получена; <b>false</b> ошибка получения блокировки.
   */
  protected abstract boolean tryLockWriter( String aOwner, ILogger aLogger );

  /**
   * Освобождает блокировку писателя
   */
  protected abstract void unlockWriter();

  /**
   * Сформировать событие: выполнены операции над разделами таблиц.
   *
   * @param aConfig {@link IOptionSet} конфигурация подсистемы для операции(смотри {@link S5SequencePartitionConfig}).
   * @param aOps {@link IList}&lt;{@link S5PartitionOperation}&gt; список выполненных операций над разделами.
   * @param aLogger {@link ILogger} журнал работы
   */
  protected abstract void firePartitionEvent( IOptionSet aConfig, IList<S5PartitionOperation> aOps, ILogger aLogger );

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Планирование обработки разделов таблиц
   */
  private void planAllTablesPartitionsCheck( ILogger aLogger ) {
    aLogger.info( "%s. partitionCandidates.putTail", owner ); //$NON-NLS-1$
    for( IS5SequenceTableNames tableNames : sequenceFactory.tableNames() ) {
      if( !partitionCandidates.hasElem( tableNames ) ) {
        partitionCandidates.putTail( tableNames );
      }
    }
  }

  /**
   * Загрузка текущих разделов таблицы в карту разделов
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aSchema String схема базы данных с которой работает сервер
   * @param aTable String имя таблицы
   * @param aDepth int глубина (в сутках) хранения значений
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private void loadTablePartitions( EntityManager aEntityManager, String aSchema, String aTable, int aDepth,
      ILogger aLogger ) {
    ES5DatabaseEngine engine = DATABASE_ENGINE.getValue( configuration ).asValobj();
    ITimedListEdit<S5Partition> partitionInfos =
        new TimedList<>( readPartitions( aEntityManager, engine, aSchema, aTable ) );
    partitionsByTable.put( aTable, partitionInfos );
    StringBuilder sb = new StringBuilder();
    for( S5Partition info : partitionInfos ) {
      sb.append( '\n' );
      sb.append( info );
    }
    aLogger.debug( "%s.%s: depth = %d, partitions: %s", aSchema, aTable, Integer.valueOf( aDepth ), sb.toString() ); //$NON-NLS-1$
  }

  private S5SequencePartitionStat doPartition( String aAuthor, IOptionSet aConfiguration ) {
    TsNullArgumentRtException.checkNulls( aConfiguration );
    // Время запуска операции
    long traceStartTime = System.currentTimeMillis();
    // Состояние задачи операций над разделами
    S5SequencePartitionStat statistics = new S5SequencePartitionStat();
    // Схема базы данных
    String schema = DATABASE_SCHEMA.getValue( aConfiguration ).asString();
    try {
      if( partitionsByTable.size() == 0 ) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
          for( IS5SequenceTableNames tableNames : sequenceFactory.tableNames() ) {
            int blockDepth = sequenceFactory.getTableDepth( tableNames.blockTableName() );
            int blobDepth = sequenceFactory.getTableDepth( tableNames.blobTableName() );
            loadTablePartitions( em, schema, tableNames.blockTableName(), blockDepth, logger );
            loadTablePartitions( em, schema, tableNames.blobTableName(), blobDepth, logger );
          }
        }
        finally {
          em.close();
        }
      }
      // Интервал удаления разделов. Если интервал ITimeInterval.NULLL, то определяется автоматически.
      ITimeInterval interval = PARTITION_REMOVE_INTERVAL.getValue( aConfiguration ).asValobj();
      // Признак ручного или автоматического запроса операций над разделами
      boolean isAuto = (interval == ITimeInterval.NULL);
      // Список операций над разделами
      IList<S5PartitionOperation> ops = IList.EMPTY;
      // Создание менеджера постоянства
      EntityManager em = entityManagerFactory.createEntityManager();
      try {
        // Формирование списка операций над разделами
        ops = (!isAuto ? preparePartitionManual( em, aConfiguration, logger ) : //
            preparePartitionAuto( em, aConfiguration, statistics, logger ));
      }
      finally {
        em.close();
      }
      // Текущий размер очереди заданий на проверку необходимости операций над разделами в авт.режиме
      int partitionCount = partitionCandidates.size();
      // Установки общих данных статистики
      statistics.setInfoes( ops != null ? ops : IList.EMPTY );
      statistics.setQueueSize( partitionCount );
      // Проверка возможности провести дефрагментацию
      if( ops == null || ops.size() == 0 ) {
        // Нет задач на обработку разделов
        logger.info( MSG_PARTITION_TASK_NOT_FOUND, owner );
        return statistics;
      }
      // Вывод в журнал
      Integer c = Integer.valueOf( ops.size() );
      Integer q = Integer.valueOf( partitionCount );
      logger.info( MSG_PARTITION_START_THREAD, owner, c, q, interval );

      for( S5PartitionOperation op : ops ) {
        // Создание менеджера постоянства
        em = entityManagerFactory.createEntityManager();
        try {
          // Обработка статистики
          S5SequencePartitionStat result = partitionJob( em, schema, op, logger );
          statistics.addAdded( result.addedCount() );
          statistics.addRemovedPartitions( result.removedPartitionCount() );
          statistics.addRemovedBlocks( result.removedBlockCount() );
          statistics.addErrors( result.errorCount() );
        }
        catch( Throwable e ) {
          // Неожиданная ошибка операции обработки разделов таблиц
          statistics.addErrors( 1 );
          logger.error( e, ERR_PARTITION_OP, owner, op, cause( e ) );
        }
        finally {
          em.close();
        }
      }
      // Оповещение наследников о выполнении операций над разделами таблиц
      firePartitionEvent( aConfiguration, ops, logger );
      return statistics;
    }
    finally {
      // Обработка ошибок
      if( statistics.errorCount() > 0 ) {
        // Запрос повторить операции обработки всех таблиц
        partitionsByTable.clear();
        logger.warning( ERR_REPLAIN_PARTITION_OPS_BY_ERROR, owner );
      }
      // Журнал
      if( statistics.operations().size() > 0 || statistics.queueSize() > 0 ) {
        // Список выполненных операций
        IList<S5PartitionOperation> operations = statistics.operations();
        // Вывод статистики
        Long d = Long.valueOf( (System.currentTimeMillis() - traceStartTime) / 1000 );
        Integer tc = Integer.valueOf( operations.size() );
        String threaded = TsLibUtils.EMPTY_STRING;
        if( operations.size() > 0 ) {
          threaded = "[" + operations.get( 0 ).toString(); //$NON-NLS-1$
          if( operations.size() > 1 ) {
            threaded += ", ..."; //$NON-NLS-1$
          }
          threaded += "]"; //$NON-NLS-1$
        }
        Integer lc = Integer.valueOf( statistics.lookupCount() );
        Integer ac = Integer.valueOf( statistics.addedCount() );
        Integer rc = Integer.valueOf( statistics.removedPartitionCount() );
        Integer rbc = Integer.valueOf( statistics.removedBlockCount() );
        Integer ec = Integer.valueOf( statistics.errorCount() );
        Integer qs = Integer.valueOf( statistics.queueSize() );
        logger.info( MSG_PARTITION_TASK_FINISH, owner, aAuthor, lc, tc, threaded, ac, rc, rbc, ec, qs, d );
      }
    }
  }

  /**
   * Проводит поиск разделов которые необходимо добавить в dbms
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aSchema String схема базы данных с которой работает сервер
   * @param aTable String имя таблицы
   * @param aInterval {@link ITimeInterval} интервал значений сохраняемых в базу данны
   * @param aDepth int глубина (сутки) хранения значений
   * @return {@link IList}&lt; {@link S5Partition}&gt; список добавляемых разделов
   */
  private IList<S5Partition> findAddPartitions( EntityManager aEntityManager, String aSchema, String aTable,
      ITimeInterval aInterval, int aDepth, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aSchema, aTable, aInterval );
    ITimedListEdit<S5Partition> partitions = partitionsByTable.findByKey( aTable );
    if( partitions == null ) {
      ES5DatabaseEngine engine = DATABASE_ENGINE.getValue( configuration ).asValobj();
      partitions = new TimedList<>( readPartitions( aEntityManager, engine, aSchema, aTable ) );
      partitionsByTable.put( aTable, partitions );
      StringBuilder sb = new StringBuilder();
      for( S5Partition info : partitions ) {
        sb.append( '\n' );
        sb.append( info );
      }
      aLogger.info( "%s.%s: partitions: %s", aSchema, aTable, sb.toString() ); //$NON-NLS-1$
    }
    // Список описаний новых разделов которые необходимо сохранить в базе данных
    return S5Partition.getNewPartitionsForInterval( partitions, aInterval, aDepth );
  }

  /**
   * Формирует список описания для удаления разделов из таблицы
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aSchema String схема базы данных сервера
   * @param aTableName String таблица базы данных
   * @param aInterval {@link ITimeInterval} интервал в который должны полностью попадать удаляемые разделы
   * @return {@link IList};&lt;{@link S5Partition}&gt; разделы для удаления
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unused" )
  private IList<S5Partition> findRemovePartitions( EntityManager aEntityManager, String aSchema, String aTableName,
      ITimeInterval aInterval, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aSchema, aTableName, aInterval );
    IList<S5Partition> partitionInfos = partitionsByTable.getByKey( aTableName );
    IListEdit<S5Partition> retValue = IList.EMPTY;
    // Формирование списка удаляемых разделов
    for( S5Partition partitionInfo : partitionInfos ) {
      if( TimeUtils.contains( aInterval, partitionInfo.interval() ) ) {
        if( retValue == IList.EMPTY ) {
          retValue = new ElemLinkedList<>();
        }
        retValue.add( partitionInfo );
      }
    }
    return retValue;
  }

  /**
   * Возращает операции над разделами таблиц которые необходимо выполнить по запросу пользователя
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aConfiguration {@link IOptionSet} конфигурация подсистемы для выполнения операций над разделами.
   * @return {@link IList}&lt;S5PartitionOperation&gt; список операций над разделами
   * @throws TsNullArgumentRtException аргумент = null
   */
  private IList<S5PartitionOperation> preparePartitionManual( EntityManager aEntityManager, IOptionSet aConfiguration,
      ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aConfiguration );
    // Схема базы данных
    String schema = DATABASE_SCHEMA.getValue( aConfiguration ).asString();
    // Список имен таблиц разделы которых будут обработаны. Если список не указан, то все таблицы
    IStringList userTables = PARTITION_TABLES.getValue( aConfiguration ).asValobj();
    // Интервал удаления разделов. Если интервал не указан, то процесс автоматически определяет требуемый интервал
    ITimeInterval interval = PARTITION_REMOVE_INTERVAL.getValue( aConfiguration ).asValobj();
    // Результат
    IListEdit<S5PartitionOperation> retValue = new ElemArrayList<>( false );
    _nextTable:
    for( String table : partitionsByTable.keys() ) {
      if( userTables.size() > 0 && !userTables.hasElem( table ) ) {
        // Пользователь указал таблицы, но текущей нет в этом списке
        continue _nextTable;
      }
      IList<S5Partition> removePartitions = findRemovePartitions( aEntityManager, schema, table, interval, aLogger );
      if( removePartitions.size() > 0 ) {
        S5PartitionOperation op = new S5PartitionOperation( table );
        op.removePartitions().addAll( removePartitions );
        retValue.add( op );
      }
    }
    return retValue;
  }

  /**
   * Возращает операции над разделами таблиц которые необходимо выполнить в автоматическом режиме
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aConfiguration {@link IOptionSet} конфигурация подсистемы для выполнения операций над разделами.
   * @param aStatistics {@link S5SequencePartitionStat} статистика с возможностью редактирования
   * @param aLogger {@link ILogger} журнал
   * @return {@link IList}&lt;S5PartitionOperation&gt; список операций над разделами
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "boxing" )
  private IList<S5PartitionOperation> preparePartitionAuto( EntityManager aEntityManager, IOptionSet aConfiguration,
      S5SequencePartitionStat aStatistics, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aConfiguration, aStatistics, aLogger );
    // Схема базы данных
    String schema = DATABASE_SCHEMA.getValue( aConfiguration ).asString();
    // Максимальное количество потоков удаления
    int threadCount = PARTITION_AUTO_THREADS_COUNT.getValue( aConfiguration ).asInt();
    // Мощность поиска удаляемых данных
    int lookupCountMax = PARTITION_AUTO_LOOKUP_COUNT.getValue( aConfiguration ).asInt();
    // Текущее время
    long currTime = System.currentTimeMillis();
    // мсек в сутках
    long msecInDay = 24 * 60 * 60 * 1000;
    // Интервал записи создаваемых разделов в таблицах (двойной, упреждение)
    ITimeInterval writeInterval = new TimeInterval( currTime, currTime + 2 * msecInDay );
    // Возвращаемый результат
    IListEdit<S5PartitionOperation> retValue = new ElemArrayList<>( false );
    // Текущее количество выполненных операций поиска удаляемых данных
    int lookupCount = 0;
    while( true ) {
      if( lookupCountMax > 0 && lookupCount >= lookupCountMax ) {
        // Установлено ограничение по количество операций поиска за один проход
        break;
      }
      // Текущий кандидат (таблицы блоков-blob) для проведения удаления разделов
      IS5SequenceTableNames candidate;
      // Описание процесса удаления текущего данного
      candidate = partitionCandidates.getHeadOrNull();
      if( candidate == null ) {
        // Больше нет кандидатов для операций удаления разделов
        break;
      }
      // Счетчик операций поиска. +=2: blockTable + blobTable
      lookupCount += 2;
      // Обновление статистики
      aStatistics.addLookupCount( 2 );
      // Имя таблицы блоков
      String blockTable = candidate.blockTableName();
      // Имя таблицы blob
      String blobTable = candidate.blobTableName();
      // Глубина хранения значений
      int depth = sequenceFactory.getTableDepth( blockTable );
      // Интервал удаления
      ITimeInterval removeInterval = new TimeInterval( TimeUtils.MIN_TIMESTAMP, currTime - depth * msecInDay );
      // Список добавляемых разделов
      IList<S5Partition> addPartitions =
          findAddPartitions( aEntityManager, schema, blockTable, writeInterval, depth, aLogger );
      // Список удаляемых разделов
      IList<S5Partition> removePartitions =
          findRemovePartitions( aEntityManager, schema, blockTable, removeInterval, aLogger );
      // Собственник писателя
      if( addPartitions.size() > 0 || removePartitions.size() > 0 ) {
        S5PartitionOperation op = new S5PartitionOperation( blockTable );
        op.addPartitions().addAll( addPartitions );
        op.removePartitions().addAll( removePartitions );
        retValue.addAll( op );
        if( addPartitions.size() > 0 ) {
          aLogger.debug( MSG_PARTITION_PLAN_ADD, owner, schema, blockTable, depth, op.addPartitions() );
        }
        if( removePartitions.size() > 0 ) {
          aLogger.debug( MSG_PARTITION_PLAN_REMOVE, owner, schema, blockTable, depth, op.removePartitions() );
        }
      }
      // Список добавляемых разделов
      addPartitions = findAddPartitions( aEntityManager, schema, blobTable, writeInterval, depth, aLogger );
      // Список удаляемых разделов
      removePartitions = findRemovePartitions( aEntityManager, schema, blobTable, removeInterval, aLogger );
      if( addPartitions.size() > 0 || removePartitions.size() > 0 ) {
        S5PartitionOperation op = new S5PartitionOperation( blobTable );
        op.addPartitions().addAll( addPartitions );
        op.removePartitions().addAll( removePartitions );
        retValue.addAll( op );
        if( addPartitions.size() > 0 ) {
          aLogger.debug( MSG_PARTITION_PLAN_ADD, owner, schema, blobTable, depth, op.addPartitions() );
        }
        if( removePartitions.size() > 0 ) {
          aLogger.debug( MSG_PARTITION_PLAN_REMOVE, owner, schema, blobTable, depth, op.removePartitions() );
        }
      }
      // Проверка на завершение
      if( threadCount > 0 && retValue.size() >= threadCount ) {
        // Сформировано необходимое количество операций удаления разделов
        break;
      }
    }
    return retValue;
  }

  /**
   * Выполняет операцию над разделами таблицы
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aSchema схема базы данных сервера
   * @param aPartitionOp {@link S5PartitionOperation} операция над разделами
   * @param aLogger {@link ILogger} журнал работы
   * @return {@link S5SequencePartitionStat} статистика удаления
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsInternalErrorRtException внутренняя ошибка удаления блоков
   */
  private S5SequencePartitionStat partitionJob( EntityManager aEntityManager, String aSchema,
      S5PartitionOperation aPartitionOp, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aSchema, aPartitionOp, aLogger );
    // Состояние задачи удаления данного
    S5SequencePartitionStat retValue = new S5SequencePartitionStat();
    // Таблица
    String table = aPartitionOp.tableName();
    // Текущее время сервера
    long currTime = System.currentTimeMillis();

    for( S5Partition partition : aPartitionOp.addPartitions() ) {
      try {
        aLogger.info( MSG_ADD_PARTITION, owner, aSchema, table, partition );
        ES5DatabaseEngine engine = DATABASE_ENGINE.getValue( configuration ).asValobj();
        S5SequenceSQL.addPartition( aEntityManager, engine, aSchema, table, partition );
        retValue.addAdded( 1 );
      }
      catch( Throwable e ) {
        retValue.addErrors( 1 );
        aLogger.error( e, ERR_ADD_PARTITION, owner, aSchema, table, partition, cause( e ) );
      }
    }
    // Удаляемые разделы
    IListEdit<S5Partition> removePartitions = aPartitionOp.removePartitions();
    // Установка идентификаторов данных удаленных разделов
    if( removePartitions.size() > 0 ) {
      aPartitionOp.removeGwids().addAll( getAllPartitionGwids( aEntityManager, aSchema, table, removePartitions ) );
    }
    // Удаление разделов из базы данных
    for( S5Partition partition : removePartitions ) {
      String partitionName = partition.name();
      try {
        aLogger.info( MSG_REMOVE_PARTITION, owner, aSchema, table, partition );
        retValue.addRemovedPartitions( 1 );
        int removedBlocks = dropPartition( aEntityManager, aSchema, table, partitionName );
        if( removedBlocks > 0 ) {
          aLogger.info( "%s. dropPartition(...). removedBlocks = %d", owner, Integer.valueOf( removedBlocks ) ); //$NON-NLS-1$
        }
        retValue.addRemovedBlocks( removedBlocks );
      }
      catch( Throwable e ) {
        // Ошибка удаления раздела
        retValue.addErrors( 1 );
        aLogger.error( e, String.format( ERR_DROP_PARTITION, owner, aSchema, table, partitionName, cause( e ) ) );
      }
    }
    // Добавление, удаление разделов из кэша описаний разделов
    ITimedListEdit<S5Partition> tablePartitions = partitionsByTable.findByKey( table );
    if( tablePartitions == null ) {
      tablePartitions = new TimedList<>();
      partitionsByTable.put( table, tablePartitions );
    }
    tablePartitions.addAll( aPartitionOp.addPartitions() );
    for( S5Partition partitionInfo : aPartitionOp.removePartitions() ) {
      tablePartitions.remove( partitionInfo );
    }
    // Полный интервал удаленных разделов
    ITimeInterval interval = ITimeInterval.NULL;
    // Количество полных дней в дефрагментации
    Integer dayCount = Integer.valueOf( -1 );
    if( aPartitionOp.removePartitions().size() > 0 ) {
      long startTime = aPartitionOp.removePartitions().first().interval().startTime();
      long endTime = aPartitionOp.removePartitions().last().interval().endTime();
      interval = new TimeInterval( startTime, endTime );
      // Количество полных дней в дефрагментации
      dayCount = Integer.valueOf( (int)((endTime - startTime) / 1000 / 60 / 60 / 24) );
    }
    // Вывод информации о завершенном объединении
    Long duration = Long.valueOf( (System.currentTimeMillis() - currTime) / 1000 );
    Long rparts = Long.valueOf( retValue.removedPartitionCount() );
    Long rblocks = Long.valueOf( retValue.removedBlockCount() );
    Long errors = Long.valueOf( retValue.errorCount() );
    aLogger.debug( MSG_PARTITION_FINISH, owner, aSchema, table, dayCount, interval, rparts, rblocks, errors, duration );
    return retValue;
  }
}
