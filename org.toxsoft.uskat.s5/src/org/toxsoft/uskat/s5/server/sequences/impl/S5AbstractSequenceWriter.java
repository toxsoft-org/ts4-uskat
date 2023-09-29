package org.toxsoft.uskat.s5.server.sequences.impl;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.bricks.time.impl.TimeUtils.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5SequencePartitionInfo.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceSQL.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceUtils.*;
import static org.toxsoft.uskat.s5.server.transactions.ES5TransactionResources.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.util.*;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

import org.toxsoft.core.log4j.LoggerWrapper;
import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.pas.tj.impl.TjUtils;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.strio.IStrioHardConstants;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.derivative.IQueue;
import org.toxsoft.core.tslib.coll.derivative.Queue;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.coll.synch.SynchronizedListEdit;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommand;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterManager;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.cluster.S5ClusterCommandSequeneceUnlockGwids;
import org.toxsoft.uskat.s5.server.sequences.maintenance.*;
import org.toxsoft.uskat.s5.server.sequences.writer.IS5SequenceWriteStat;
import org.toxsoft.uskat.s5.server.sequences.writer.IS5SequenceWriter;
import org.toxsoft.uskat.s5.server.singletons.S5ServiceSingletonUtils;
import org.toxsoft.uskat.s5.server.startup.IS5InitialImplementSingleton;
import org.toxsoft.uskat.s5.server.transactions.*;
import org.toxsoft.uskat.s5.utils.collections.WrapperMap;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

/**
 * Абстрактная реализация писателя значений последовательностей данных {@link IS5SequenceWriter}
 *
 * @author mvk
 * @param <S> тип последовательности значений данного
 * @param <V> тип значения последовательности
 */
public abstract class S5AbstractSequenceWriter<S extends IS5Sequence<V>, V extends ITemporal<?>>
    implements IS5SequenceWriter<S, V>, IS5TransactionListener {

  /**
   * Имя параметра отключения записи последовательностей
   */
  public static final String WRITE_DISABLE_PARAM = "s5.sequence.write.disabled"; //$NON-NLS-1$

  /**
   * Включена отладка системы. Все(!) писатели последовательностей отключены
   */
  public static boolean WRITE_DISABLED = false;

  /**
   * Количество разрешенных повторов объединения блоков последовательности при недостаточном java heap
   */
  private static final int SEQUENCE_UNION_TRY_HEAP_COUNT = 3;

  /**
   * Максимальное количество проходов объединения блоков после которого процесс объединения принудительно завершается
   */
  private static final int SEQUENCE_UNION_PASS_MAX = 64;

  /**
   * Таймат (мсек) проверки активной транзакции при ожидании захвата блокировки данных
   */
  private static final int TX_LOCK_CHECK_ACTIVE_TX_TIMEOUT = 1000;

  /**
   * Максимальный таймат (мсек) при ожидании захвата блокировки данных
   */
  private static final int TX_LOCKED_GWIDS_TIMEOUT = 10000;

  /**
   * Исполнитель потоков записи
   */
  private final ManagedExecutorService writeExecutor;

  /**
   * Исполнитель потоков дефрагментации
   */
  private final ManagedExecutorService unionExecutor;

  /**
   * Исполнитель потоков удаления
   */
  // private final ManagedExecutorService removeExecutor;

  /**
   * Исполнитель потоков объединения
   */
  private final ManagedExecutorService validationExecutor;

  /**
   * Фабрика менеджеров постоянства
   */
  private final EntityManagerFactory entityManagerFactory;

  /**
   * Менеджер кластера
   */
  private final IS5ClusterManager clusterManager;

  /**
   * Команда кластера: всем узлам разблокировать доступ к указанным данным;
   */
  private final S5ClusterCommandSequeneceUnlockGwids clusterUnlockGwidsCmd;

  /**
   * Фабрика последовательностей блоков
   */
  private final IS5SequenceFactory<V> sequenceFactory;

  /**
   * Неизменяемая конфигурация сервера
   */
  private final IS5InitialImplementSingleton initialConfig;

  /**
   * Синглетон управления s5-транзакциями
   */
  private final IS5TransactionManagerSingleton txManager;

  /**
   * Набор идентификаторов данных заблокированных для локального доступа
   * <p>
   * Данные блокируются на время выполнения операции добавления/обновления/удаления значений
   */
  private final Set<Gwid> localLockedGwids = new HashSet<>();

  /**
   * Набор идентификаторов данных заблокированных для удаленного доступа
   * <p>
   * Данные блокируются на время выполнения операции добавления/обновления/удаления значений и остаются заблокированными
   * до попытки их блокировки удаленной стороной
   */
  private final Set<Gwid> remoteLockedGwids = new HashSet<>();

  /**
   * Карта разделов таблиц хранения данных.
   * <p>
   * Ключ: имя таблицы хранения блоков значения;<br>
   * Значение: список описаний разделов (партиций) таблицы
   */
  private final IMapEdit<String, ITimedListEdit<S5SequencePartitionInfo>> partitionInfosByTable =
      new WrapperMap<>( new HashMap<String, ITimedListEdit<S5SequencePartitionInfo>>() );

  /**
   * Блокировка доступа к данным на удаление: {@link #partitionInfosByTable}
   */
  private final S5Lockable partitionInfoLock = new S5Lockable();

  /**
   * Список запланированных операций удаления данных
   */
  private final IQueue<IS5SequenceRemoveInfo> removeCandidates = new Queue<>();

  /**
   * Блокировка доступа к данным на удаление: {@link #removeCandidates}
   */
  private final S5Lockable removeCandidatesLock = new S5Lockable();

  /**
   * Время (мсек с начала эпохи) последней проверки разделов таблиц
   */
  private long lastCheckPartitionTime = System.currentTimeMillis();

  /**
   * Журнал
   */
  private final ILogger logger;

  static {
    // Запрет записей последовательностей в базу
    String writeDisabled = System.getProperty( WRITE_DISABLE_PARAM );
    WRITE_DISABLED = (writeDisabled != null ? Boolean.parseBoolean( writeDisabled ) : false);
  }

  /**
   * Создает писатель последовательностей
   *
   * @param aBackendCore {@link IS5BackendCoreSingleton} ядро бекенда сервера
   * @param aSequenceFactory {@link IS5SequenceFactory} фабрика последовательностей блоков
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException аргумент нет соединения с базой данных
   */
  protected S5AbstractSequenceWriter( IS5BackendCoreSingleton aBackendCore, IS5SequenceFactory<V> aSequenceFactory ) {
    TsNullArgumentRtException.checkNulls( aBackendCore, aSequenceFactory );
    // Поиск исполнителя потоков объединения блоков
    writeExecutor = S5ServiceSingletonUtils.lookupExecutor( WRITE_EXECUTOR_JNDI );
    // Поиск исполнителя потоков дефрагментации блоков
    unionExecutor = S5ServiceSingletonUtils.lookupExecutor( UNION_EXECUTOR_JNDI );
    // Поиск исполнителя потоков удаления блоков
    // removeExecutor = S5ServiceSingletonUtils.lookupExecutor( REMOVE_EXECUTOR_JNDI );
    // Поиск исполнителя потоков проверки блоков
    validationExecutor = S5ServiceSingletonUtils.lookupExecutor( VALIDATION_EXECUTOR_JNDI );
    // Неизменяемая конфигурация сервера
    initialConfig = aBackendCore.initialConfig();
    // Управление транзакциями
    txManager = aBackendCore.txManager();
    // Менджер кластера
    clusterManager = aBackendCore.clusterManager();
    // Фабрика постоянства
    entityManagerFactory = aBackendCore.entityManagerFactory();
    // Фабрика последовательности
    sequenceFactory = aSequenceFactory;
    // Команда кластера: всем узлам разблокировать доступ к указанным данным
    clusterUnlockGwidsCmd = new S5ClusterCommandSequeneceUnlockGwids( aSequenceFactory.id(), this );
    // Регистрация исполнителя команды
    clusterManager.addCommandHandler( clusterUnlockGwidsCmd.method(), clusterUnlockGwidsCmd );
    // Журнал
    logger = getLogger( LOG_WRITER_ID );
    // Схема базы данных сервера
    IAtomicValue dbScheme = OP_BACKEND_DB_SCHEME_NAME.getValue( initialConfig().impl().params() );
    if( !dbScheme.isAssigned() ) {
      // Не определено имя схемы базы данных в реализации сервера
      throw new TsIllegalArgumentRtException( ERR_DB_SCHEME_NOT_DEFINED );
    }
    // Менеджер постоянства
    EntityManager em = entityManagerFactory().createEntityManager();
    try {
      // После запуска требуется проверить все таблицы на необходимость удаления. Загружаем все разделы
      loadAllTablePartitions( em, dbScheme.asString() );
    }
    finally {
      em.close();
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация ICloseable
  //
  @Override
  public void close() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Делает попытку разблокирования удаленного доступа к указанным данным
   *
   * @param aGwids {@link IList}&lt;{@link Gwid}&gt; список идентификаторов освобождаемых данных
   * @return boolean <b>true</b> данные освобождены;<b>false</b> данные не могут быть освобождены (еще используются)
   * @throws TsNullArgumentRtException аргумент = null
   */
  public final boolean remoteUnlockGwids( IList<Gwid> aGwids ) {
    TsNullArgumentRtException.checkNull( aGwids );
    synchronized (localLockedGwids) {
      // Проверка доступа к данным. false: без удаленного доступа
      Gwid lockGwid = getGwidsAccess( this, aGwids, false );
      if( lockGwid != null ) {
        // Рерурс занят локальной транзакцией
        return false;
      }
      // Разблокировка удаленного доступа к данным
      for( Gwid gwid : aGwids ) {
        remoteLockedGwids.remove( gwid );
      }
    }
    // Ресурсы не заняты
    return true;
  }

  // ------------------------------------------------------------------------------------
  // API для наследников
  //
  /**
   * Возвращает начальную, неизменяемую, проектно-зависимую конфигурация реализации бекенда сервера
   *
   * @return {@link IS5InitialImplementSingleton} конфигурация
   */
  protected final IS5InitialImplementSingleton initialConfig() {
    return initialConfig;
  }

  /**
   * Возвращает исполнителя потоков для записи блоков
   *
   * @return {@link ManagedExecutorService} исполнитель потоков
   */
  protected final ManagedExecutorService writeExecutor() {
    return writeExecutor;
  }

  /**
   * Возвращает исполнителя потоков для дефрагментации блоков
   *
   * @return {@link ManagedExecutorService} исполнитель потоков
   */
  protected final ManagedExecutorService unionExecutor() {
    return unionExecutor;
  }

  /**
   * Возвращает исполнителя потоков для удаления блоков
   *
   * @return {@link ManagedExecutorService} исполнитель потоков
   */
  // protected final ManagedExecutorService removeExecutor() {
  // return removeExecutor;
  // }

  /**
   * Возвращает исполнителя потоков для проверки блоков
   *
   * @return {@link ManagedExecutorService} исполнитель потоков
   */
  protected final ManagedExecutorService validationExecutor() {
    return validationExecutor;
  }

  /**
   * Возвращает фабрику менеджеров постоянства
   *
   * @return {@link EntityManagerFactory} фабрика
   */
  protected final EntityManagerFactory entityManagerFactory() {
    return entityManagerFactory;
  }

  /**
   * Возвращает фабрику последовательностей блоков
   *
   * @return {@link IS5SequenceFactory} фабрика
   */
  protected final IS5SequenceFactory<V> sequenceFactory() {
    return sequenceFactory;
  }

  /**
   * Возвращает карту разделов таблиц хранения данных.
   * <p>
   *
   * @return {@link IMapEdit} карта с возможностью удаления. Ключ: имя таблицы хранения блоков значения;<br>
   *         Значение: список описаний разделов (партиций) таблицы
   */
  protected final IMapEdit<String, ITimedListEdit<S5SequencePartitionInfo>> partitionInfosByTable() {
    return partitionInfosByTable;
  }

  /**
   * Возвращает блокировку доступа к данным на удаление: {@link #partitionInfosByTable()}
   *
   * @return {@link S5Lockable} блокировка
   */
  protected final S5Lockable partitionInfoLock() {
    return partitionInfoLock;
  }

  /**
   * Возвращает журнал писателя
   *
   * @return {@link ILogger} журнал писателя
   */
  protected final ILogger logger() {
    return logger;
  }

  /**
   * Производит попытку заблокировать доступ к указанным данным
   * <p>
   * Предполагается, что клиент при начале транзакции должнен блокировать {@link #tryLockGwids(IList, boolean)} доступ к
   * данным, а по завершению разблокировать {@link #unlockGwids(IList)}
   *
   * @param aGwids {@link IList}&lt;{@link Gwid}&gt; список идентификаторов блокируемых данных
   * @param aPutInTransaction boolean <b>true</b> разместить заблокированные данные в транзакции. <b>false</b> не
   *          размещать заблокированные данные в транзакции
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException нет текущей транзакции
   */
  protected final void tryLockGwids( IList<Gwid> aGwids, boolean aPutInTransaction ) {
    TsNullArgumentRtException.checkNull( aGwids );
    Long count = Long.valueOf( aGwids.size() );
    // Транзакция в которой происходит блокировка данных. null: неопределенна
    IS5Transaction transaction = (aPutInTransaction ? txManager.findTransaction() : null);
    if( aPutInTransaction && transaction == null ) {
      // Нет транзакции
      throw new TsIllegalStateRtException( ERR_ROLLBACK_INFOES, count );
    }
    // Метка времени (мсек с начала эпохи) начала запроса блокирования данных
    long tryLockStartTime = System.currentTimeMillis();
    // Метка времени (мсек с начала эпохи) последней проверки активной транзакции
    long lastCheckTime = tryLockStartTime;
    synchronized (localLockedGwids) {
      // Проверка доступа к данным. true: с удаленным доступом
      for( Gwid lockGwid = getGwidsAccess( this, aGwids, true ); lockGwid != null; //
          lockGwid = getGwidsAccess( this, aGwids, true ) ) {
        try {
          long currTime = System.currentTimeMillis();
          long timeFromCheck = currTime - lastCheckTime;
          if( timeFromCheck > TX_LOCK_CHECK_ACTIVE_TX_TIMEOUT ) {
            lastCheckTime = currTime;
            Long t = Long.valueOf( currTime - tryLockStartTime );
            logger.warning( MSG_WAIT_LOCK_INFOES, count, lockGwid, t );
            // Проверка что транзакция не была отмена пока ожидали блокировку
            if( transaction != null && transaction.getStatus() == ETransactionStatus.ROLLEDBACK ) {
              // Пока ждали блокировку, транзакция была отменена
              throw new TsIllegalStateRtException( ERR_ROLLBACK_INFOES, count );
            }
            if( currTime - tryLockStartTime > TX_LOCKED_GWIDS_TIMEOUT ) {
              // Превышение времени максимального ожидания блокировки данного
              throw new TsIllegalStateRtException( ERR_LOCK_INFOES_TIMEOUT, count );
            }
          }
          localLockedGwids.wait( TX_LOCK_CHECK_ACTIVE_TX_TIMEOUT );
        }
        catch( InterruptedException e ) {
          logger.error( e );
          throw new TsIllegalStateRtException( e );
        }
      }
      // Проверка что транзакция не была отмена пока ожидали блокировку
      if( transaction != null && transaction.getStatus() == ETransactionStatus.ROLLEDBACK ) {
        // Пока ждали блокировку, транзакция была отменена
        throw new TsIllegalStateRtException( ERR_ROLLBACK_INFOES, count );
      }
      // Блокировка доступа к данным
      for( Gwid gwid : aGwids ) {
        localLockedGwids.add( gwid );
        remoteLockedGwids.add( gwid );
      }
      // Сохранение в транзакции идентификаторов записываемых данных
      if( transaction != null ) {
        try {
          IListEdit<Gwid> gwids = transaction.findResource( TX_SEQUENCE_LOCKED_GWIDS );
          if( gwids == null ) {
            // Для транзакции еще не была создана карта описаний. false: дубли запрещены
            gwids = new SynchronizedListEdit<>( new ElemArrayList<>( false ) );
          }
          gwids.addAll( aGwids );
          transaction.putResource( TX_SEQUENCE_LOCKED_GWIDS, gwids );
          // Регистрируемся на получение событий об изменении состояния транзакции
          transaction.addListener( this );
        }
        finally {
          // Проверка, возможно пока производилась блокировка транзакция завершилась
          if( transaction.getStatus() == ETransactionStatus.ROLLEDBACK ) {
            for( Gwid gwid : aGwids ) {
              localLockedGwids.remove( gwid );
            }
            throw new TsIllegalStateRtException( ERR_ROLLBACK_INFOES, count );
          }
        }
      }
      // TODO: запросить монопольный доступ на кэш lockedCache, если его нет, то освободить lockedGwids и все повторить

    }
  }

  /**
   * Раблокирование доступа к указанным данным
   * <p>
   * Предполагается, что клиент перед началом транзакции должнен блокировать {@link #tryLockGwids(IList, boolean)}
   * доступ к данным, а по завершению разблокировать {@link #unlockGwids(IList)}
   *
   * @param aGwids {@link IList}&lt;{@link Gwid}&gt; список идентификаторов данных
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected final void unlockGwids( IList<Gwid> aGwids ) {
    TsNullArgumentRtException.checkNull( aGwids );
    synchronized (localLockedGwids) {
      // Разблокирование доступа к данным
      for( Gwid gwid : aGwids ) {
        localLockedGwids.remove( gwid );
      }
      // Сигнал оповещения ожидающих потоков об освобождении ресурса
      localLockedGwids.notifyAll();
    }
  }

  /**
   * Формирует список описаний для удаления разделов из таблиц
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aUserTables {@link IStringList} список имен таблиц в которых удаляются разделы. Пустой список: все таблицы
   * @param aInterval {@link ITimeInterval} интервал в который должны полностью попадать удаляемые разделы
   * @return {@link IList} список описаний удаляемых разделов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected final IList<IS5SequenceRemoveInfo> getRemoveInfoList( EntityManager aEntityManager, IStringList aUserTables,
      ITimeInterval aInterval ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aUserTables, aInterval );
    // Схема базы данных сервера
    String scheme = OP_BACKEND_DB_SCHEME_NAME.getValue( initialConfig().impl().params() ).asString();
    IListEdit<IS5SequenceRemoveInfo> retValue = new ElemArrayList<>( false );
    lockWrite( partitionInfoLock );
    try {
      _nextTable:
      for( String tableName : partitionInfosByTable.keys() ) {
        if( aUserTables.size() > 0 && !aUserTables.hasElem( tableName ) ) {
          // Пользователь указал таблицы, но текущей нет в этом списке
          continue _nextTable;
        }
        IList<S5SequencePartitionInfo> partitionInfos = partitionInfosByTable.getByKey( tableName );
        IListEdit<S5SequencePartitionInfo> removePartitionInfos = new ElemArrayList<>();
        // Формирование списка удаляемых разделов
        for( S5SequencePartitionInfo partitionInfo : partitionInfos ) {
          if( TimeUtils.contains( aInterval, partitionInfo.interval() ) ) {
            removePartitionInfos.add( partitionInfo );
          }
        }
        if( removePartitionInfos.size() == 0 ) {
          // Нет удаляемых разделов
          continue _nextTable;
        }
        // Идентификаторы данных таблицы
        IGwidList partitionGwids = getAllPartitionGwids( aEntityManager, scheme, tableName, partitionInfos );
        // Формирование списка удаляемых данных
        retValue.add( new S5SequenceRemoveInfo( tableName, partitionGwids, removePartitionInfos ) );
      }
    }
    finally {
      unlockWrite( partitionInfoLock );
    }
    return retValue;
  }

  /**
   * Сохраняет блоки в dbms
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aBlocks {@link Iterable}&lt;{@link IS5SequenceBlock}&lt;V;&gt;&gt; сохраняемые блоки значений
   * @param aStat {@link S5DbmsStatistics} статистика работы
   * @param aLogger {@link ILogger} журнал
   * @param <V> тип значений в блоке
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException попытка конкуретного изменения данных последовательности
   */
  @SuppressWarnings( "unchecked" )
  protected final static <V extends ITemporal<?>> void writeBlocksToDbms( EntityManager aEntityManager,
      Iterable<IS5SequenceBlock<V>> aBlocks, ILogger aLogger, S5DbmsStatistics aStat ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aBlocks, aLogger );
    // Время начала операции
    long startTime = System.currentTimeMillis();
    // Количество добавленных блоков
    int insertedCount = 0;
    // Количество обновленных блоков
    int mergedCount = 0;
    for( IS5SequenceBlock<V> block : aBlocks ) {
      S5SequenceBlock<V, ?, ?> dbmsBlock = (S5SequenceBlock<V, ?, ?>)block;
      try {
        if( dbmsBlock.created() ) {
          aEntityManager.persist( dbmsBlock );
          dbmsBlock.afterWrite();
          insertedCount++;
          continue;
        }
        if( dbmsBlock.merged() ) {
          ((S5SequenceBlock<V, ?, ?>)aEntityManager.merge( block )).update( dbmsBlock );
          dbmsBlock.afterWrite();
          mergedCount++;
          continue;
        }
      }
      catch( Throwable e ) {
        aStat.addWriteErrorCount();
        if( dbmsBlock.created() ) {
          throw new TsInternalErrorRtException( e, ERR_PERSIST_UNEXPECTED, block, cause( e ) );
        }
        if( dbmsBlock.merged() ) {
          throw new TsInternalErrorRtException( e, ERR_MERGE_UNEXPECTED, block, cause( e ) );
        }
        throw new TsInternalErrorRtException( e, ERR_WRITE_UNEXPECTED, block, cause( e ) );
      }
    }
    try {
      aEntityManager.flush();
    }
    catch( Throwable e ) {
      aStat.addWriteErrorCount();
      throw new TsInternalErrorRtException( e, ERR_WRITE_FLASH_UNEXPECTED, cause( e ) );
    }
    // Общее время выполнения операции
    int writeTime = (int)(System.currentTimeMillis() - startTime);
    // Статистика
    if( insertedCount > 0 ) {
      aStat.addInserted( insertedCount, writeTime * (insertedCount / (insertedCount + mergedCount)) );
    }
    if( mergedCount > 0 ) {
      aStat.addMergedCount( mergedCount, writeTime * (mergedCount / (insertedCount + mergedCount)) );
    }
  }

  /**
   * Удаляет из базы данных все блоки покрывающие интервал указанным списком
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aRemovedBlocks {@link IList} список блоков
   * @param aStat {@link S5DbmsStatistics} статистика работы
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected final static <V extends ITemporal<?>> void removeBlocksFromDbms( EntityManager aEntityManager,
      IList<IS5SequenceBlock<V>> aRemovedBlocks, S5DbmsStatistics aStat ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aRemovedBlocks, aStat );
    if( aRemovedBlocks.size() == 0 ) {
      return;
    }
    // Время начала операции
    long startTime = System.currentTimeMillis();
    // TODO: mvk 2019-11-17 native пока не работает + не доделано удаление blob
    // S5SequenceSQL.removeBlocks( aEntityManager, aFactory, aGwid, interval( EQueryIntervalType.CSCE, aRemovedBlocks )
    // );
    // https://stackoverflow.com/questions/17027398/java-lang-illegalargumentexception-removing-a-detached-instance-com-test-user5
    // aEntityManager.remove( aEntityManager.contains( block ) ? block : aEntityManager.merge( block ) );
    //
    // old s5 code: aEntityManager.remove( aEntityManager.merge( block ) );

    for( IS5SequenceBlock<V> block : aRemovedBlocks ) {
      try {
        // 2020-05-25 mvk IlleaglArgumentException Removing a detach instance S5EventBlock#tm.Measuer...
        // aEntityManager.remove( block );
        aEntityManager.remove( aEntityManager.merge( block ) );
      }
      catch( Throwable e ) {
        aStat.addWriteErrorCount();
        throw new TsInternalErrorRtException( e, ERR_REMOVE_UNEXPECTED, block, cause( e ) );
      }
    }
    try {
      aEntityManager.flush();
    }
    catch( Throwable e ) {
      aStat.addWriteErrorCount();
      throw new TsInternalErrorRtException( e, ERR_WRITE_FLASH_UNEXPECTED, cause( e ) );
    }
    // Общее время выполнения операции
    int removedTime = (int)(System.currentTimeMillis() - startTime);
    // Статистика
    aStat.addRemovedCount( aRemovedBlocks.size(), removedTime );
  }

  /**
   * Проводит дефрагментацию блоков в указанном интервале
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link ITimeInterval} интервал дефрагментации
   * @param aLogger {@link ILogger} журнал работы
   * @return {@link S5SequenceUnionStat} статистика дефрагментации
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsInternalErrorRtException внутренняя ошибка дефрагментации блоков
   */
  protected final S5SequenceUnionStat<V> unionInterval( EntityManager aEntityManager, Gwid aGwid,
      ITimeInterval aInterval, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aGwid, aInterval, aLogger );
    // Состояние задачи дефрагментации данного
    S5SequenceUnionStat<V> unionState = new S5SequenceUnionStat<>();
    try {
      long startTime = aInterval.startTime();
      long endTime = aInterval.endTime();
      // Количество полных дней в дефрагментации
      Integer dayCount = Integer.valueOf( (int)((endTime - startTime) / 1000 / 60 / 60 / 24) );
      // Текущее время сервера
      long currTime = System.currentTimeMillis();
      // Номер прохода
      int unionPass = 0;
      // Текущее количество ошибок попыток дефрагментации
      int unionErrorCount = 0;
      while( true ) {
        try {
          boolean completed = tryUnion( aEntityManager, unionPass, aGwid, aInterval, unionState );
          if( completed ) {
            // Обработка завершена
            break;
          }
          unionPass++;
          if( unionPass > SEQUENCE_UNION_PASS_MAX ) {
            // Слишком много попыток дефрагментации блоков.
            aLogger.warning( ERR_UNION_PASS_MAX, aGwid, dayCount, Long.valueOf( SEQUENCE_UNION_PASS_MAX ) );
            break;
          }
          unionErrorCount = 0;
        }
        catch( RuntimeException e ) {
          // Ошибка дефрагментации блоков
          Integer tc = Integer.valueOf( unionErrorCount );
          Integer up = Integer.valueOf( unionPass );
          if( unionErrorCount < SEQUENCE_UNION_TRY_HEAP_COUNT ) {
            // Требуем повторить дефрагментацию
            aLogger.warning( ERR_AUTO_UNION_PASS_RETRY, aGwid, tc, up, dayCount, aInterval, cause( e ) );
            unionErrorCount++;
            unionState.addErrors( 1 );
            continue;
          }
          String errMsg = String.format( ERR_AUTO_CANT_UNION_PASS, aGwid, up, dayCount, aInterval, cause( e ) );
          // Использованы все попытки дефрагментации блоков. Дефрагментация невозможна.
          aLogger.error( errMsg );
          throw new TsInternalErrorRtException( e, errMsg );
        }
      }
      // Вывод информации о завершенной дефрагментации
      Long duration = Long.valueOf( (System.currentTimeMillis() - currTime) / 1000 );
      Long unitedL = Long.valueOf( unionState.dbmsMergedCount() );
      Long removedL = Long.valueOf( unionState.dbmsRemovedCount() );
      Integer size = Integer.valueOf( unionState.valueCount() );
      Long errors = Long.valueOf( unionState.errorCount() );
      if( size.intValue() > 0 ) {
        aLogger.debug( MSG_UNION_FINISH, aGwid, dayCount, aInterval, unitedL, removedL, size, errors, duration );
      }
      return unionState;
    }
    finally {
      if( unionState.lastUnitedBlockOrNull() != null ) {
        // Отвязываем блок от EJB чтобы не перегружать java heap
        aEntityManager.detach( unionState.lastUnitedBlockOrNull() );
      }
    }
  }

  /**
   * Проводит удаление блоков в указанном интервале
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aRemoveInfo {@link IS5SequenceRemoveInfo} описание для удаления
   * @param aLogger {@link ILogger} журнал работы
   * @return {@link S5SequenceRemoveStat} статистика удаления
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsInternalErrorRtException внутренняя ошибка удаления блоков
   */
  protected final S5SequenceRemoveStat<V> dropPartitions( EntityManager aEntityManager,
      IS5SequenceRemoveInfo aRemoveInfo, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aRemoveInfo, aLogger );
    // Состояние задачи удаления данного
    S5SequenceRemoveStat<V> removeState = new S5SequenceRemoveStat<>();
    if( aRemoveInfo.partitionInfos().size() == 0 ) {
      return removeState;
    }
    // Схема базы данных сервера
    String scheme = OP_BACKEND_DB_SCHEME_NAME.getValue( initialConfig().impl().params() ).asString();
    // Таблица
    String tableName = aRemoveInfo.tableName();
    long startTime = aRemoveInfo.partitionInfos().first().interval().startTime();
    long endTime = aRemoveInfo.partitionInfos().last().interval().endTime();
    // Текущее время сервера
    long currTime = System.currentTimeMillis();
    // Удаление разделов из базы данных
    for( S5SequencePartitionInfo partitionInfo : aRemoveInfo.partitionInfos() ) {
      String partitionName = partitionInfo.partitionName();
      try {
        removeState.addRemoved( dropPartition( aEntityManager, scheme, tableName, partitionName ) );
      }
      catch( RuntimeException e ) {
        // Ошибка удаления раздела
        removeState.addErrors( 1 );
        aLogger.error( e, String.format( ERR_DROP_PARTITION, scheme, tableName, partitionName, cause( e ) ) );
      }
    }
    // Удаление разделов из кэша описаний разделов
    lockWrite( partitionInfoLock );
    try {
      ITimedListEdit<S5SequencePartitionInfo> partitionInfos = partitionInfosByTable.findByKey( tableName );
      for( S5SequencePartitionInfo partitionInfo : aRemoveInfo.partitionInfos() ) {
        partitionInfos.remove( partitionInfo );
      }
    }
    finally {
      unlockWrite( partitionInfoLock );
    }
    // Полный интервал удаленных разделов
    ITimeInterval interval = new TimeInterval( startTime, endTime );
    // Количество полных дней в дефрагментации
    Integer dayCount = Integer.valueOf( (int)((endTime - startTime) / 1000 / 60 / 60 / 24) );
    // Вывод информации о завершенном объединении
    Long duration = Long.valueOf( (System.currentTimeMillis() - currTime) / 1000 );
    Long removedBlocks = Long.valueOf( removeState.removedCount() );
    Long errors = Long.valueOf( removeState.errorCount() );
    aLogger.debug( MSG_REMOVE_FINISH, scheme, tableName, dayCount, interval, removedBlocks, errors, duration );
    return removeState;
  }

  /**
   * Проводит проверку блоков указанного данного в указанном интервале и восстановления их состояния
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link IQueryInterval} интервал блоков проверяемых блоков
   * @param aState {@link S5SequenceValidationStat} состояние задачи проверки блоков
   * @param aCanUpdate boolean <b>true</b> разрешение обновления блоков; <b>false</b> запрет обновления блоков
   * @param aCanRemove boolean <b>true</b> разрешение удаления блоков; <b>false</b> запрет удаления блоков
   * @param aLogger {@link ILogger} журнал работы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  protected final void validationInterval( EntityManager aEntityManager, Gwid aGwid, IQueryInterval aInterval,
      S5SequenceValidationStat aState, boolean aCanUpdate, boolean aCanRemove, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aGwid, aInterval, aState, aLogger );
    EntityManager em = aEntityManager;
    IS5SequenceFactory<V> factory = sequenceFactory();
    // Параметризованное описание типа данного
    IParameterized typeInfo = factory.typeInfo( aGwid );
    // Текущее время сервера
    long currTime = System.currentTimeMillis();
    // Последний блок-результат. null: неопределен
    IS5SequenceBlock<V> lastBlock = null;
    // Признак того, что последовательность содержит синхронные значения
    boolean isSync = OP_IS_SYNC.getValue( typeInfo.params() ).asBool();
    // Интервал (мсек) синхронных значений. Для асинхронных: 1
    long syncDT = (isSync ? OP_SYNC_DT.getValue( typeInfo.params() ).asLong() : 1);
    // Интервал времени запроса блоков
    IQueryInterval interval = aInterval;
    // Статистика dbms
    S5DbmsStatistics dbmsStat = new S5DbmsStatistics();
    // Счетчики
    int processedQtty = 0;
    int warnQtty = 0;
    int errQtty = 0;
    int valuesQtty = 0;
    int nonOptimalQtty = 0;
    // Строка состояния java heap
    StringBuilder memoryLog = new StringBuilder();
    while( true ) {
      if( lastBlock != null ) {
        // Запрос начинается с последнего блока-результата
        long nextStartTime = lastBlock.endTime() + syncDT;
        interval = new QueryInterval( aInterval.type(), nextStartTime, Math.max( nextStartTime, aInterval.endTime() ) );
      }
      // Получаем размеры блоков в указанном диапазоне, но не более SEQUENCE_READ_COUNT_MAX
      List<Integer> blkSizes = loadBlockSizes( em, factory, aGwid, interval, 0, SEQUENCE_READ_COUNT_MAX );
      if( blkSizes.size() == 0 ) {
        // Нет блоков
        break;
      }
      // Строка состояния java heap
      memoryLog = new StringBuilder();
      // Максимальный % свободной памяти используемой для запроса
      double freeMaxBusy = 0.5;
      // Расчет максимального количества блоков которое может быть запрошено при текущем состоянии java-heap
      int blockCount = calcBlockCountByMemory( typeInfo, blkSizes, 0, freeMaxBusy, memoryLog );
      if( blockCount <= 0 ) {
        // Недостаточно памяти для проверки блоков
        aLogger.warning( ERR_VALIDATION_NOT_ENOUGH_MEMORY, aGwid, interval, memoryLog );
        return;
      }
      try {
        // Загрузка блоков полностью попадающих в интервал объединения
        List<IS5SequenceBlock<V>> blocks = loadBlocks( em, factory, aGwid, interval, 0, blockCount );
        // Количество загруженных блоков
        int count = blocks.size();
        if( count == 0 ) {
          // Больше нет блоков для проверки
          break;
        }
        ElemArrayList<IS5SequenceBlock<V>> writeBlocks = new ElemArrayList<>( count );
        ElemArrayList<IS5SequenceBlock<V>> removedBlocks = new ElemArrayList<>( count );

        // Общее количество обработанных блоков
        processedQtty += count;
        // Количество блоков которое осталось для обработки
        int leftQtty = count;
        // ПРОВЕРКА БЛОКОВ
        for( IS5SequenceBlock<V> block : blocks ) {
          leftQtty--;
          long lastEndTime = (lastBlock != null ? lastBlock.endTime() : MIN_TIMESTAMP);
          // Проверка порядка следования блоков и их интервалов (ложные пересечения)
          if( lastEndTime >= block.startTime() ) {
            removedBlocks.add( block );
            errQtty++;
            aLogger.error( MSG_VALIDATION_REMOVE_UNORDER, aGwid, block, lastBlock );
            continue;
          }
          IS5SequenceBlockEdit<V> blockEdit = (IS5SequenceBlockEdit<V>)block;
          // Валидация блока
          IList<ValidationResult> validationResults = blockEdit.validation( typeInfo ).results();
          // Признак необходимости обновления блока
          boolean needUpdate = false;
          // Признак необходимости удаления блока
          boolean needRemove = false;
          for( ValidationResult validationResult : validationResults ) {
            if( validationResult.isOk() ) {
              aLogger.debug( validationResult.message() );
            }
            if( validationResult.isWarning() ) {
              needUpdate = true;
              warnQtty++;
              aLogger.warning( validationResult.message() );
            }
            if( validationResult.isError() ) {
              needRemove = true;
              errQtty++;
              aLogger.error( validationResult.message() );
            }
          }
          if( needUpdate ) {
            writeBlocks.add( block );
            aLogger.warning( MSG_VALIDATION_UPDATE, aGwid, block );
          }
          if( needRemove ) {
            removedBlocks.add( block );
            aLogger.error( MSG_VALIDATION_REMOVE, aGwid, block );
          }
          if( !needRemove && lastBlock != block ) {
            lastBlock = block;
            valuesQtty += block.size();
          }
          if( leftQtty == 0 && needRemove && !aCanRemove ) {
            // Последний блок с ошибкой. Меняем последний блок, чтобы не попасть в бесконечный цикл (удаление запрещено)
            lastBlock = block;
          }
        }
        // Для блоков синхронных значений проводим расчет неэффективного хранения
        if( isSync ) {
          IListEdit<S5SequenceSyncBlock<V, ?, ?>> syncBlocks = new ElemArrayList<>( blocks.size() );
          for( IS5SequenceBlock<V> block : blocks ) {
            if( removedBlocks.hasElem( block ) ) {
              // В проверку попадают только неудаленные блоки
              continue;
            }
            syncBlocks.add( (S5SequenceSyncBlock<V, ?, ?>)block );
          }
          nonOptimalQtty += S5SequenceSyncBlock.checkBlockSequence( typeInfo, syncBlocks, aLogger );
        }
        if( aCanRemove || aCanUpdate ) {
          // Удаление блоков которые невозможно восстановить
          if( aCanRemove ) {
            removeBlocksFromDbms( em, removedBlocks, dbmsStat );
          }
          // Обновление восстановленных блоков
          if( aCanUpdate ) {
            writeBlocksToDbms( aEntityManager, writeBlocks, aLogger, dbmsStat );
          }
        }
        if( blocks.size() < blockCount ) {
          // Проверены все блоки
          break;
        }
      }
      catch( Throwable e ) {
        aLogger.error( e );
        break;
      }
    }
    // Обработка состояния
    aState.addProcessed( processedQtty );
    aState.addWarnings( warnQtty );
    aState.addErrors( errQtty );
    aState.addDbmsMerged( dbmsStat.insertedCount() + dbmsStat.mergedCount() );
    aState.addDbmsRemoved( dbmsStat.removedCount() );
    aState.addValues( valuesQtty );
    aState.addNonOptimal( nonOptimalQtty );

    // Вывод журнал
    Long a = Long.valueOf( processedQtty );
    Long u = Long.valueOf( dbmsStat.insertedCount() + dbmsStat.mergedCount() );
    Long r = Long.valueOf( dbmsStat.removedCount() );
    Integer v = Integer.valueOf( valuesQtty );
    Integer n = Integer.valueOf( nonOptimalQtty );
    Long d = Long.valueOf( (System.currentTimeMillis() - currTime) / 1000 );

    aLogger.debug( MSG_VALIDATION_FINISH, aGwid, interval, a, u, r, v, n, d, memoryLog );
  }

  /**
   * Вывод содержимого блоков в журнал
   *
   * @param aLogger {@link ILogger} журнал
   * @param aMessage String пояснение к выводу
   * @param aBlocks {@link List} список блоков
   * @param <V> тип значений блока
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected final static <V extends ITemporal<?>> void printBlocks( ILogger aLogger, String aMessage,
      List<IS5SequenceBlock<V>> aBlocks ) {
    TsNullArgumentRtException.checkNulls( aLogger, aMessage, aBlocks );
    StringBuilder sb = new StringBuilder();
    sb.append( aMessage );
    sb.append( IStrioHardConstants.CHAR_EOL );
    for( IS5SequenceBlock<?> block : aBlocks ) {
      Long st = Long.valueOf( block.startTime() );
      Long et = Long.valueOf( block.endTime() );
      sb.append( String.format( "  %s, startTime = %d, endTime = %d", block, st, et ) ); //$NON-NLS-1$
      sb.append( IStrioHardConstants.CHAR_EOL );
    }
    aLogger.debug( sb.toString() );
  }

  /**
   * Вывод содержимого блоков в журнал
   *
   * @param aLogger {@link ILogger} журнал
   * @param aMessage String пояснение к выводу
   * @param aBlocks {@link IList} список блоков
   * @param <V> тип значений блока
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected final static <V extends ITemporal<?>> void printBlocks( ILogger aLogger, String aMessage,
      IList<IS5SequenceBlock<V>> aBlocks ) {
    TsNullArgumentRtException.checkNulls( aLogger, aMessage, aBlocks );
    StringBuilder sb = new StringBuilder();
    sb.append( aMessage );
    sb.append( IStrioHardConstants.CHAR_EOL );
    for( IS5SequenceBlock<?> block : aBlocks ) {
      Long st = Long.valueOf( block.startTime() );
      Long et = Long.valueOf( block.endTime() );
      sb.append( String.format( "  %s, startTime = %d, endTime = %d", block, st, et ) ); //$NON-NLS-1$
      sb.append( IStrioHardConstants.CHAR_EOL );
    }
    aLogger.debug( sb.toString() );
  }

  // ------------------------------------------------------------------------------------
  // Шаблонные методы для реализации наследниками
  //
  /**
   * Шаблонный метод записи данных последовательностей
   *
   * @param aEntityManager {@link EntityManager} мененджер постоянства который МОЖЕТ использовать писатель для записи
   * @param aSequences {@link IList}&lt;{@link S5Sequence}&gt; последовательность значений
   * @param aStatistics {@link IS5SequenceWriteStat} редактируемая статистика выполнения записи
   * @throws TsIllegalStateRtException попытка конкуретного изменения данных последовательности
   */
  protected abstract void doWrite( EntityManager aEntityManager, IList<S> aSequences, S5SequenceWriteStat aStatistics );

  /**
   * Шаблонный метод дефрагментации данных последовательностей
   *
   * @param aArgs {@link IOptionSet}&gt; аргументы для дефрагментации блоков (смотри {@link IS5SequenceUnionOptions}).
   * @return {@link IS5SequenceUnionStat} статистика процесса объединения
   */
  protected abstract S5SequenceUnionStat<V> doUnion( IOptionSet aArgs );

  /**
   * Шаблонный метод проверки целостности данных последовательности
   *
   * @param aArgs {@link IOptionSet} аргументы для проверки блоков (смотри {@link IS5SequenceValidationOptions}).
   * @return {@link IS5SequenceValidationStat} статистика процесса проверки
   */
  protected abstract S5SequenceValidationStat doValidation( IOptionSet aArgs );

  /**
   * Событие: проведено удаление значений данных.
   *
   * @param aArgs {@link IOptionSet} аргументы для объединения блоков (смотри {@link IS5SequenceRemoveOptions}).
   * @param aInfos {@link IList}&lt; {@link IS5SequenceRemoveInfo}&gt; список описаний данных для удаления.
   * @param aLogger {@link ILogger} журнал работы
   */
  protected void onRemoveEvent( IOptionSet aArgs, IList<IS5SequenceRemoveInfo> aInfos, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aArgs, aInfos );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequenceWriter
  //
  @Override
  public final S5SequenceWriteStat write( EntityManager aEntityManager, IList<S> aSequences ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aSequences );
    // Формирование статистики записи
    S5SequenceWriteStat statistics = new S5SequenceWriteStat();
    statistics.setDataCount( aSequences.size() );

    // 2020-09-15 mvkd
    if( WRITE_DISABLED ) {
      // Запись запрещена
      logger.warning( "%s = true", WRITE_DISABLE_PARAM ); //$NON-NLS-1$
      return statistics;
    }

    // Размещение статистики в транзакции
    // txManager.putTransactionResource( TX_SEQUENCE_WRITE_STAT, statistics );
    // Запрос блокировка доступа к данным набора (true: пытаемся заблокировать пока текущая транзакция не отменена)
    // tryLockInfoes( aInfoes, true );
    try {
      statistics.setStartTime( System.currentTimeMillis() );
      try {
        // Запись
        doWrite( aEntityManager, aSequences, statistics );
      }
      catch( TsIllegalStateRtException e ) {
        // Попытка конкуретного изменения данных последовательности
        logger().error( e, ERR_SEQUENCE_WRITE_CONCURRENT, cause( e ) );
        throw new TsInternalErrorRtException( e, ERR_SEQUENCE_WRITE_CONCURRENT, cause( e ) );
      }
      catch( RuntimeException e ) {
        logger().error( e, ERR_SEQUENCE_WRITE_UNEXPECTED, cause( e ) );
        throw new TsInternalErrorRtException( e, ERR_SEQUENCE_WRITE_UNEXPECTED, cause( e ) );
      }
      // Установка времени завершения записи (НЕ транзакции!)
      // statistics.setEndTime( System.currentTimeMillis() );
      return statistics;
    }
    finally {
      // Описания будут разблокированы после завершения транзакции (или ее откате)
      // TODO: ??? пока есть где-то "дыра" и данное может быть заблокировано навечно
      // надо проанализировать, может делать unlockInfoes нет нужды? (мы работаем в транзакции, доступ нужно
      // ограничивать только к "lastBlock")
      // unlockInfoes( aInfoes );
    }
  }

  @Override
  public final S5SequenceUnionStat<V> union( IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aArgs );
    return doUnion( aArgs );
  }

  @Override
  public final S5SequenceRemoveStat<V> remove( IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aArgs );
    // Журнал для потоков
    ILogger removeLogger = LoggerWrapper.getLogger( LOG_REMOVER_ID );
    // Состояние задачи удаления данного
    S5SequenceRemoveStat<V> statistics = new S5SequenceRemoveStat<>();
    // Аргументы запроса
    IOptionSetEdit args = new OptionSet( aArgs );
    // Признак ручного или автоматического объединения данных
    boolean isAuto = !args.hasValue( IS5SequenceRemoveOptions.REMOVE_INTERVAL );
    // Описания удаляемых данных
    IList<IS5SequenceRemoveInfo> infoes = IList.EMPTY;
    // Создание менеджера постоянства
    EntityManager em = entityManagerFactory().createEntityManager();
    try {
      // Формирование описания данных
      infoes = (!isAuto ? prepareRemoveManual( em, args ) : prepareRemoveAuto( em, args, statistics, removeLogger ));
      // Текущий размер очереди на дефрагментацию
      int partitionCount = removeCandidatesCount();
      // Установки общих данных статистики
      statistics.setInfoes( infoes != null ? infoes : IList.EMPTY );
      statistics.setQueueSize( partitionCount );
      // Проверка возможности провести дефрагментацию
      if( infoes == null || infoes.size() == 0 ) {
        // Запрет выполнять объединение
        return statistics;
      }
      // Вывод в журнал
      Integer c = Integer.valueOf( infoes.size() );
      Integer q = Integer.valueOf( partitionCount );
      ITimeInterval interval = IS5SequenceRemoveOptions.REMOVE_INTERVAL.getValue( args ).asValobj();
      removeLogger.info( MSG_REMOVE_START_THREAD, c, q, interval );

      for( IS5SequenceRemoveInfo info : infoes ) {
        // Удаление разделов
        S5SequenceRemoveStat<V> result = dropPartitions( em, info, logger() );
        statistics.addRemoved( result.removedCount() );
        statistics.addErrors( result.errorCount() );
      }
    }
    finally {
      em.close();
    }
    // Оповещение наследников о проведении удаления блоков
    onRemoveEvent( args, infoes, removeLogger );
    return statistics;
  }

  @Override
  public final S5SequenceValidationStat validation( IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aArgs );
    return doValidation( aArgs );
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //
  @Override
  public void doJob() {
    // Текущее время
    long currTime = System.currentTimeMillis();
    // мсек в сутках
    long msecInDay = 24 * 60 * 60 * 1000;
    if( currTime - lastCheckPartitionTime < msecInDay ) {
      // Проверка разделов проводится один раз в сутки
      return;
    }
    EntityManager em = entityManagerFactory().createEntityManager();
    try {
      try {
        UserTransaction tx = InitialContext.doLookup( USER_TRANSACTION_JNDI );
        // Открываем транзакцию
        tx.begin();
        try {
          // Присоединение менеджера постоянства к транзкации
          em.joinTransaction();

          // Создание разделов в таблицах для нового интервала
          ITimeInterval writeInterval = new TimeInterval( currTime - msecInDay, currTime + msecInDay );
          preparePartitionsForInterval( em, writeInterval );

          // Глубина хранения значений
          int depth = OP_VALUE_STORAGE_DEPTH.getValue( initialConfig().impl().params() ).asInt();
          ITimeInterval removeInterval = new TimeInterval( TimeUtils.MIN_TIMESTAMP, currTime - depth * msecInDay );
          // Формирование очереди на удаление разделов. aUserTables = IStringList.EMPTY (все таблицы)
          IList<IS5SequenceRemoveInfo> removeInfos = getRemoveInfoList( em, IStringList.EMPTY, removeInterval );
          lockWrite( removeCandidatesLock );
          try {
            for( IS5SequenceRemoveInfo info : removeInfos ) {
              removeCandidates.putTail( info );
            }
          }
          finally {
            unlockWrite( removeCandidatesLock );
          }

          // Завершаем транзакцию
          tx.commit();
          // Фиксация времени последней обработки
          lastCheckPartitionTime = currTime;
        }
        catch( Throwable e ) {
          // Откат транзакции на любой ошибке
          tx.rollback();
          throw e;
        }
      }
      catch( Throwable e ) {
        throw new TsInternalErrorRtException( e, ERR_UNEXPECT_DOJOB_ERROR, cause( e ) );
      }
    }
    finally {
      em.close();
    }
  }

  // ------------------------------------------------------------------------------------
  // IS5TransactionListener
  //
  @Override
  public void checkCommitResources( IS5Transaction aTransaction ) {
    // nop
  }

  @Override
  public void changeTransactionStatus( IS5Transaction aTransaction ) {
    IList<Gwid> txLockedGwids = null;
    try {
      try {
        switch( aTransaction.getStatus() ) {
          case NO_TRANSACTION:
            break;
          case ACTIVE:
            break;
          case PREPARED:
            break;
          case PREPARING:
            break;
          case COMMITED:
            txLockedGwids = aTransaction.findResource( TX_SEQUENCE_LOCKED_GWIDS );
            S5SequenceWriteStat statistics = aTransaction.findResource( TX_SEQUENCE_WRITE_STAT );
            // Вывод статистики
            String gwidsString = gwidsToString( txLockedGwids, 3 );
            Long dt = Long.valueOf( System.currentTimeMillis() - statistics.createTime() );
            Long dw = Long.valueOf( statistics.endTime() - statistics.createTime() );
            Long c = Long.valueOf( statistics.dataCount() );
            ITimeInterval wi = statistics.writedBlockInterval();
            Long wc = Long.valueOf( statistics.writedBlockCount() );
            logger().info( MSG_WRITE_SEQUENCE_TIME, gwidsString, dt, dw, c, wi, wc, statistics.dbmsStatistics() );
            break;
          case COMMITTING:
            break;
          case MARKED_ROLLBACK:
            break;
          case ROLLING_BACK:
            break;
          case ROLLEDBACK:
            txLockedGwids = aTransaction.findResource( TX_SEQUENCE_LOCKED_GWIDS );
            break;
          case UNKNOWN:
            break;
          default:
            throw new TsNotAllEnumsUsedRtException();
        }
      }
      catch( Throwable e ) {
        // Неожиданная ошибка
        logger.error( e );
      }
    }
    finally {
      if( txLockedGwids != null ) {
        unlockGwids( txLockedGwids );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренняя реализация
  //
  /**
   * Выполняет попытку объединения блоков в указанном интервале
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aPassNo int номер попытки объединения для информации (отсчет от 0)
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link ITimeInterval} интервал дефрагментации
   * @param aState {@link S5SequenceUnionStat} статистика объединения
   * @return boolean <b>true</b> объединение завершено(больше нет данных). <b>false</b> требуется продолжить процесс
   *         объединения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  private boolean tryUnion( EntityManager aEntityManager, int aPassNo, Gwid aGwid, ITimeInterval aInterval,
      S5SequenceUnionStat<V> aState ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aGwid, aInterval, aState );
    EntityManager em = aEntityManager;
    IS5SequenceFactory<V> factory = sequenceFactory();
    // Параметризованное описание типа данного
    IParameterized typeInfo = factory.typeInfo( aGwid );
    // Текущее время сервера
    long currTime = System.currentTimeMillis();
    // Количество полных дней в объединении
    Integer dayCount = Integer.valueOf( (int)((aInterval.endTime() - aInterval.startTime()) / 1000 / 60 / 60 / 24) );

    // Признак того, что последовательность содержит синхронные значения
    boolean isSync = OP_IS_SYNC.getValue( typeInfo.params() ).asBool();
    // Интервал (мсек) синхронных значений. Для асинхронных: 1
    long syncDT = (isSync ? OP_SYNC_DT.getValue( typeInfo.params() ).asLong() : 1);
    // Максимальный размер блока
    int blockSizeMax = OP_BLOCK_SIZE_MAX.getValue( typeInfo.params() ).asInt();

    // Последний блок-результат. null: неопределен
    IS5SequenceBlock<V> lastUnitedBlock = aState.lastUnitedBlockOrNull();
    // Интервал времени запроса блоков
    IQueryInterval interval = new QueryInterval( EQueryIntervalType.CSOE, aInterval.startTime(), aInterval.endTime() );
    if( lastUnitedBlock != null ) {
      // Объединение проводится по блоку-результату
      long startTime = lastUnitedBlock.endTime() + syncDT;
      long endTime = aInterval.endTime();
      if( startTime > endTime ) {
        // Интервал заканчивается раньше чем последний блок. Дальше объединять нечего
        return true;
      }
      interval = new QueryInterval( EQueryIntervalType.CSCE, startTime, endTime );
    }
    // Получаем размеры блоков в указанном диапазоне, но не более SEQUENCE_READ_COUNT_MAX
    List<Integer> blkSizes = loadBlockSizes( em, factory, aGwid, interval, 0, SEQUENCE_READ_COUNT_MAX );
    // Признак необходимости провести фрагментацию одного найденного блока
    boolean needFragmentation = (blkSizes.size() == 1 && blkSizes.get( 0 ).intValue() > 2 * blockSizeMax);
    if( blkSizes.size() <= 1 && !needFragmentation ) {
      // Больше нет блоков для объединения
      if( blkSizes.size() == 1 ) {
        aState.addDbmsMerged( 1 );
        aState.addValues( blkSizes.get( 0 ).intValue() );
      }
      // На интервале найдено меньше 2 блоков - нет возможности объединения. Чтение по интервалу завершено
      return true;
    }
    // Строка состояния java heap
    StringBuilder memoryLog = new StringBuilder();
    // Максимальный % свободной памяти используемой для запроса
    double freeMaxBusy = 0.5;
    // Расчет максимального количества блоков которое может быть запрошено при текущем состоянии java-heap
    int blockCount = calcBlockCountByMemory( typeInfo, blkSizes, 0, freeMaxBusy, memoryLog );
    if( blockCount <= 0 ) {
      // Недостаточно памяти для объединения блоков
      logger().warning( ERR_UNION_NOT_ENOUGH_MEMORY, aGwid, dayCount, interval, memoryLog.toString() );
      // Требуем повторить чтение
      return false;
    }
    try {
      // Загрузка блоков полностью попадающих в интервал объединения
      List<IS5SequenceBlock<V>> blocks = loadBlocks( em, factory, aGwid, interval, 0, blockCount );
      if( blocks.size() <= 1 && !needFragmentation ) {
        // Больше нет блоков для объединения
        if( blocks.size() == 1 ) {
          aState.addDbmsMerged( 1 );
          aState.addValues( blocks.get( 0 ).size() );
        }
        // На интервале найдено меньше 2 блоков - нет возможности объединения. Чтение по интервалу завершено
        return true;
      }
      // Количество полученных значений
      int valueCount = 0;
      for( IS5SequenceBlock<V> bloсk : blocks ) {
        valueCount += bloсk.size();
      }
      // Если есть блок-результат, то он добавляется в последовательность
      if( lastUnitedBlock != null ) {
        blocks.add( 0, lastUnitedBlock );
      }
      // Интервал времени последовательности
      long startTime = blocks.get( 0 ).startTime();
      long endTime = blocks.get( blocks.size() - 1 ).endTime();

      // Последовательность блоков
      IQueryInterval targetInterval = new QueryInterval( EQueryIntervalType.CSCE, startTime, endTime );
      Iterable<IS5SequenceBlockEdit<V>> targetBlocks = (Iterable<IS5SequenceBlockEdit<V>>)(Object)blocks;
      IS5SequenceEdit<V> target = factory.createSequence( aGwid, targetInterval, targetBlocks );
      // Использование реализации ElemArrayList из-за prepareDbmsBlocks которому это необходимо для производительности
      ElemArrayList<IS5SequenceBlock<V>> removedBlocks = new ElemArrayList<>( target.blocks().size() );

      // ОБЪЕДИНЕНИЕ БЛОКОВ
      removedBlocks.addAll( (IList<IS5SequenceBlock<V>>)(Object)target.uniteBlocks() );

      // Статистика
      S5DbmsStatistics dbmsStat = new S5DbmsStatistics();
      // Удаление блоков значения которые были добавлены в другие блоки
      removeBlocksFromDbms( em, removedBlocks, dbmsStat );
      // Создание/обновление блоков в базе данных
      writeBlocksToDbms( em, target.blocks(), logger(), dbmsStat );

      // Количество блоков в последовательности после объединения
      blockCount = target.blocks().size();
      // Определение текущего блока-результата
      lastUnitedBlock = target.blocks().get( blockCount - 1 );
      // Отвязываем блок от EJB чтобы не перегружать java heap
      for( int index = 0; index < blockCount; index++ ) {
        IS5SequenceBlock<V> block = target.blocks().get( index );
        if( block != aState.lastUnitedBlockOrNull() ) {
          em.detach( block );
        }
      }
      // Обновление состояния задачи
      aState.setUnitedBlock( lastUnitedBlock );
      aState.addDbmsMerged( dbmsStat.insertedCount() + dbmsStat.mergedCount() );
      aState.addDbmsRemoved( dbmsStat.removedCount() );
      aState.addValues( valueCount );
      // Запуск следующего прохода объединения блоков
      Integer pass = Integer.valueOf( aPassNo );
      Long d = Long.valueOf( (System.currentTimeMillis() - currTime) / 1000 );
      Long u = Long.valueOf( dbmsStat.insertedCount() + dbmsStat.mergedCount() );
      Long r = Long.valueOf( dbmsStat.removedCount() );
      Integer all = Integer.valueOf( aState.valueCount() );
      Integer last = Integer.valueOf( lastUnitedBlock.size() );
      logger().debug( MSG_UNION_PASS_FINISH, aGwid, pass, dayCount, interval, u, r, all, last, d, memoryLog );

      // false: объединение завершено, true: требуется продолжить процесс объединения
      return (blocks.size() < blockCount);
    }
    catch( Throwable e ) {
      aState.addErrors( 1 );
      Integer pass = Integer.valueOf( aPassNo );
      logger().error( e, ERR_UNION_UNEXPECTED, pass, aGwid, dayCount, aInterval, memoryLog, cause( e ) );
      throw new TsInternalErrorRtException( cause( e ) );
    }
  }

  /**
   * Загрузка текущих разделов всех таблиц в карту разделов
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aScheme String схема базы данных с которой работает сервер
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private void loadAllTablePartitions( EntityManager aEntityManager, String aScheme ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aScheme );
    // Обработка разделов таблиц
    lockWrite( partitionInfoLock );
    try {
      for( Pair<String, String> tableNames : sequenceFactory().tableNames() ) {
        loadTablePartitions( aEntityManager, aScheme, tableNames.left() );
        loadTablePartitions( aEntityManager, aScheme, tableNames.right() );
      }
    }
    finally {
      unlockWrite( partitionInfoLock );
    }
  }

  /**
   * Загрузка текущих разделов таблицы в карту разделов
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aScheme String схема базы данных с которой работает сервер
   * @param aTable String имя таблицы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private void loadTablePartitions( EntityManager aEntityManager, String aScheme, String aTable ) {
    ITimedListEdit<S5SequencePartitionInfo> partitionInfos =
        new TimedList<>( readPartitions( aEntityManager, aScheme, aTable ) );
    partitionInfosByTable.put( aTable, partitionInfos );
    StringBuilder sb = new StringBuilder();
    for( S5SequencePartitionInfo info : partitionInfos ) {
      sb.append( '\n' );
      sb.append( info );
    }
    logger().info( "%s.%s: partitions: %s", aScheme, aTable, sb.toString() ); //$NON-NLS-1$
  }

  /**
   * Подготовить (если необходимо) разделы таблиц для записи значений последовательностей
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aInterval {@link ITimeInterval} интервал для которого должны быть подготовлены разделы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private void preparePartitionsForInterval( EntityManager aEntityManager, ITimeInterval aInterval ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aInterval );
    // Схема базы данных сервера
    IAtomicValue dbScheme = OP_BACKEND_DB_SCHEME_NAME.getValue( initialConfig().impl().params() );
    if( !dbScheme.isAssigned() ) {
      // Не определено имя схемы базы данных в реализации сервера
      throw new TsIllegalArgumentRtException( ERR_DB_SCHEME_NOT_DEFINED );
    }
    // Схема базы данных
    String scheme = dbScheme.asString();
    // Глубина хранения значений
    int depth = OP_VALUE_STORAGE_DEPTH.getValue( initialConfig().impl().params() ).asInt();
    // Карта описаний разделов добавляемых таблицы без разделов. Ключ: имя таблицы.
    IStringMapEdit<IList<S5SequencePartitionInfo>> createPartitions = new StringMap<>();
    // Карта описаний разделов добавляемых в таблицы с существующими разделами. Ключ: имя таблицы.
    IStringMapEdit<IList<S5SequencePartitionInfo>> addPartitions = new StringMap<>();
    // Обработка разделов таблиц
    lockWrite( partitionInfoLock );
    try {
      for( Pair<String, String> tables : sequenceFactory.tableNames() ) {
        String blockTable = tables.left();
        String blobTable = tables.right();
        prepareTablePartition( aEntityManager, scheme, blockTable, aInterval, depth, createPartitions, addPartitions );
        prepareTablePartition( aEntityManager, scheme, blobTable, aInterval, depth, createPartitions, addPartitions );
      }
    }
    finally {
      unlockWrite( partitionInfoLock );
    }
    if( createPartitions.size() == 0 && addPartitions.size() == 0 ) {
      // Нет необходимости создавать новые разделы
      return;
    }
    // Текущая таблица базы данных. null: нет таблицы
    String tablename = null;
    // Текущий добавляемый раздел. null: нет раздела
    S5SequencePartitionInfo partitionInfo = null;
    try {
      for( String table : createPartitions.keys() ) {
        for( S5SequencePartitionInfo info : createPartitions.getByKey( table ) ) {
          tablename = table;
          partitionInfo = info;
          logger().info( MSG_ADD_PARTITION, scheme, table, info );
          S5SequenceSQL.addPartition( aEntityManager, scheme, table, info, true );
        }
      }
      for( String table : addPartitions.keys() ) {
        for( S5SequencePartitionInfo info : addPartitions.getByKey( table ) ) {
          tablename = table;
          partitionInfo = info;
          logger().info( MSG_ADD_PARTITION, scheme, table, info );
          S5SequenceSQL.addPartition( aEntityManager, scheme, table, info, false );
        }
      }
      tablename = null;
      partitionInfo = null;
    }
    catch( Throwable e ) {
      String t = tablename != null ? tablename : "???"; //$NON-NLS-1$
      String p = partitionInfo != null ? partitionInfo.toString() : "???"; //$NON-NLS-1$
      logger().error( e, ERR_ADD_PARTITION, scheme, t, p, cause( e ) );
    }
    // Обработка разделов таблиц
    lockWrite( partitionInfoLock );
    try {
      for( String table : createPartitions.keys() ) {
        ITimedListEdit<S5SequencePartitionInfo> partitionInfos = partitionInfosByTable.findByKey( table );
        partitionInfos.addAll( createPartitions.getByKey( table ) );
      }
      for( String table : addPartitions.keys() ) {
        ITimedListEdit<S5SequencePartitionInfo> partitionInfos = partitionInfosByTable.findByKey( table );
        partitionInfos.addAll( addPartitions.getByKey( table ) );
      }
    }
    finally {
      unlockWrite( partitionInfoLock );
    }
  }

  /**
   * Подготовка разделов таблицы к работе
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aScheme String схема базы данных с которой работает сервер
   * @param aTable String имя таблицы
   * @param aInterval {@link ITimeInterval} интервал значений сохраняемых в базу данны
   * @param aDepth int глубина (сутки) хранения значений
   * @param aCreatingPartitions {@link IStringMapEdit} карта разделов для таблиц без разделов. Ключ: имя таблицы
   * @param aAddPartitions {@link IStringMapEdit} карта разделов для таблиц с разделами. Ключ: имя таблицы
   */
  private void prepareTablePartition( EntityManager aEntityManager, String aScheme, String aTable,
      ITimeInterval aInterval, int aDepth, IStringMapEdit<IList<S5SequencePartitionInfo>> aCreatingPartitions,
      IStringMapEdit<IList<S5SequencePartitionInfo>> aAddPartitions ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aScheme, aTable, aInterval, aCreatingPartitions,
        aAddPartitions );
    ITimedListEdit<S5SequencePartitionInfo> partitionInfos = partitionInfosByTable.findByKey( aTable );
    if( partitionInfos == null ) {
      partitionInfos = new TimedList<>( readPartitions( aEntityManager, aScheme, aTable ) );
      partitionInfosByTable.put( aTable, partitionInfos );
      StringBuilder sb = new StringBuilder();
      for( S5SequencePartitionInfo info : partitionInfos ) {
        sb.append( '\n' );
        sb.append( info );
      }
      logger().info( "%s.%s: partitions: %s", aScheme, aTable, sb.toString() ); //$NON-NLS-1$
    }
    // Список описаний новых разделов котоорые необходимо сохранить в базе данных
    IList<S5SequencePartitionInfo> newInfos = getNewPartitionsForInterval( partitionInfos, aInterval, aDepth );
    if( newInfos.size() == 0 ) {
      // Новые разделы не требуются
      return;
    }
    // Признак необходимости добавления раздела в таблице без разделов
    boolean creating = (partitionInfos.size() == 0 && !aCreatingPartitions.hasKey( aTable ));
    if( creating ) {
      aCreatingPartitions.put( aTable, newInfos );
      return;
    }
    // Добавление разделов в таблицы с уже существующими разделами (с обработкой дублей)
    IListEdit<S5SequencePartitionInfo> infos = (IListEdit<S5SequencePartitionInfo>)aAddPartitions.findByKey( aTable );
    if( infos == null ) {
      // aAllowDuplicates == false
      infos = new ElemArrayList<>( false );
      aAddPartitions.put( aTable, infos );
    }
    infos.addAll( newInfos );
  }

  /**
   * Возвращает общее заданий в очереди на удаление разделов
   *
   * @return int текущее количество разделов
   */
  protected final int removeCandidatesCount() {
    // Обработка разделов таблиц
    lockWrite( removeCandidatesLock );
    try {
      return removeCandidates.size();
    }
    finally {
      unlockWrite( removeCandidatesLock );
    }
  }

  /**
   * Возращает описания и добавляет необходимые параметры для выполнения удаления данных по запросу
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aArgs {@link IOptionSetEdit} аргументы для удаления блоков
   * @return {@link IList}&lt;IS5SequenceFragmentInfo&gt; список описаний фрагментированности данных
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected final IList<IS5SequenceRemoveInfo> prepareRemoveManual( EntityManager aEntityManager,
      IOptionSetEdit aArgs ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aArgs );
    // Идентификаторы данных. null: неопределены
    IStringList userTables = IStringList.EMPTY;
    if( aArgs.hasValue( IS5SequenceRemoveOptions.REMOVE_FROM_TABLES ) ) {
      userTables = IS5SequenceRemoveOptions.REMOVE_FROM_TABLES.getValue( aArgs ).asValobj();
    }
    // Интервал удаления
    ITimeInterval interval = IS5SequenceRemoveOptions.REMOVE_INTERVAL.getValue( aArgs ).asValobj();

    return getRemoveInfoList( aEntityManager, userTables, interval );
  }

  /**
   * Возращает описания и добавляет необходимые параметры для выполнения удаления в автоматическом режиме
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @param aArgs {@link IOptionSetEdit} аргументы для удаления блоков .
   * @param aStatistics {@link S5SequenceRemoveStat} статистика с возможностью редактирования
   * @param aLogger {@link ILogger} журнал
   * @return {@link IList}&lt;IS5SequenceRemoveInfo&gt; список описаний фрагментированности данных
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected final IList<IS5SequenceRemoveInfo> prepareRemoveAuto( EntityManager aEntityManager, IOptionSetEdit aArgs,
      S5SequenceRemoveStat<V> aStatistics, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aArgs, aStatistics, aLogger );
    // Фабрика последовательностей
    IS5SequenceFactory<V> factory = sequenceFactory();
    // Схема базы данных сервера
    String scheme = OP_BACKEND_DB_SCHEME_NAME.getValue( initialConfig().impl().params() ).asString();
    // Максимальное количество потоков удаления
    int threadCount = IS5SequenceRemoveOptions.REMOVE_AUTO_THREADS_COUNT.getValue( aArgs ).asInt();
    // Мощность поиска удаляемых данных
    int lookupCountMax = IS5SequenceRemoveOptions.REMOVE_AUTO_LOOKUP_COUNT.getValue( aArgs ).asInt();
    // Текущее время
    long currTime = System.currentTimeMillis();
    // Список данных добавляемых в на проверку
    GwidList newCandidates = new GwidList();
    // Возвращаемый результат
    IListEdit<IS5SequenceRemoveInfo> retValue = new ElemArrayList<>( false );
    // Текущее количество выполненных операций поиска удаляемых данных
    int lookupCount = 0;
    while( true ) {
      if( lookupCountMax > 0 && lookupCount >= lookupCountMax ) {
        // Установлено ограничение по количество операций поиска за один проход
        break;
      }
      // Счетчик операций поиска
      lookupCount++;
      // Обновление статистики
      aStatistics.addLookupCount();

      lockWrite( removeCandidatesLock );
      try {
        // Описание процесса удаления текущего данного
        IS5SequenceRemoveInfo candiateInfo = removeCandidates.getHeadOrNull();
        if( candiateInfo == null ) {
          // Больше нет данных для удаления
          break;
        }
        // Добавление в результат
        retValue.add( candiateInfo );
        // Журнал
        aLogger.debug( MSG_REMOVE_AUTO_ADD_INFO, candiateInfo );
        // Проверка на завершение
        if( retValue.size() >= threadCount ) {
          // Сформировано необходимое количество данных для дефрагментации
          break;
        }
      }
      finally {
        unlockWrite( removeCandidatesLock );
      }
    }
    return retValue;
  }

  /**
   * Проверяет существование конфликтных записей по одним и тем же ресурсам и пытается их разрешить
   *
   * @param aWriter {@link S5AbstractSequenceWriter} писатель данных
   * @param aNeedGwids {@link IList}&lt;{@link Gwid}&gt; набор идентификаторов к которым необходим доступ на запись
   * @param aNeedRemote <b>true</b> получить доступ к данным на уровне узла кластера;<b>false</b> проверка только
   *          локального доступа (на уровне транзакции)
   * @return {@link Gwid} идентификатор данных из третьего набора который есть в одном(или обоих) первых наборах. null:
   *         общих данных не найдено
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static Gwid getGwidsAccess( S5AbstractSequenceWriter<?, ?> aWriter, IList<Gwid> aNeedGwids,
      boolean aNeedRemote ) {
    TsNullArgumentRtException.checkNulls( aWriter, aNeedGwids );
    // Проверка локального доступа
    for( Gwid gwid : aNeedGwids ) {
      if( aWriter.localLockedGwids.contains( gwid ) ) {
        return gwid;
      }
    }
    if( !aNeedRemote ) {
      // Удаленный доступ не требуется
      return null;
    }
    // Набор данных требующих блокировку на уровне узла кластера
    GwidList needRemoteGwids = new GwidList();
    // Проверка удаленного доступа
    for( Gwid gwid : aNeedGwids ) {
      if( !aWriter.remoteLockedGwids.contains( gwid ) ) {
        needRemoteGwids.add( gwid );
      }
    }
    if( needRemoteGwids.size() > 0 ) {
      try {
        // Формирование команды на получение удаленного доступа
        IS5ClusterCommand cmd = aWriter.clusterUnlockGwidsCmd.createCommand( needRemoteGwids );
        // Отправляем запрос на получение доступа: boolean remoteOnly = true; boolean primaryOnly = false
        IStringMap<ITjValue> result = aWriter.clusterManager.sendSyncCommand( cmd, true, false );
        // Обработка результата
        for( String nodeId : result.keys() ) {
          ITjValue value = result.getByKey( nodeId );
          if( !value.equals( TjUtils.TRUE ) ) {
            // Ошибка получения удаленного доступа к данным на узле
            aWriter.logger().warning( ERR_REMOTE_ACCESS, nodeId, gwidsToString( needRemoteGwids, 1 ) );
            // Ошибка получения доступа к данным
            return needRemoteGwids.first();
          }
        }
      }
      catch( Throwable e ) {
        aWriter.logger().error( e );
        // Ошибка получения доступа к данным
        return needRemoteGwids.first();
      }
    }
    return null;
  }
}
