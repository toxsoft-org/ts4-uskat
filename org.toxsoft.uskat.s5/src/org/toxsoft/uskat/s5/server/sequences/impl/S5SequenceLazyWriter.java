package org.toxsoft.uskat.s5.server.sequences.impl;

import static java.lang.String.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceSQL.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.util.HashMap;
import java.util.List;

import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import org.toxsoft.core.log4j.Logger;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.TimeInterval;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.derivative.IQueue;
import org.toxsoft.core.tslib.coll.derivative.Queue;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.maintenance.IS5SequenceUnionOptions;
import org.toxsoft.uskat.s5.server.sequences.maintenance.IS5SequenceValidationOptions;
import org.toxsoft.uskat.s5.server.sequences.writer.IS5SequenceWriter;
import org.toxsoft.uskat.s5.utils.collections.WrapperMap;
import org.toxsoft.uskat.s5.utils.threads.impl.*;

/**
 * Реализация писателя значений последовательностей данных {@link IS5SequenceWriter}
 * <p>
 * Писатель реализует "ленивое" сохранение блоков и предполагает, что для окончательной их обработки будет вызываться
 * процесс дефрагментации {@link IS5SequenceWriter#union(IOptionSet)}.
 *
 * @author mvk
 * @param <S> тип последовательности значений данного
 * @param <V> тип значения последовательности
 */
@SuppressWarnings( "unused" )
class S5SequenceLazyWriter<S extends IS5Sequence<V>, V extends ITemporal<?>>
    extends S5AbstractSequenceWriter<S, V> {

  /**
   * Список идентификаторов данных требующих проверки на дефрагментацию в автоматическом режиме
   */
  private final IQueue<Gwid> unionCandidates = new Queue<>();

  /**
   * Карта описаний дефрагментации сформированной текущей записью.
   * <p>
   * Ключ: идентификатор данного;<br>
   * Значение: описание дефрагментации {@link S5SequenceFragmentInfo}
   */
  private final IMapEdit<Gwid, S5SequenceFragmentInfo> unionCandidateFragments =
      new WrapperMap<>( new HashMap<Gwid, S5SequenceFragmentInfo>() );

  /**
   * Карта описаний дефрагментации с момента проведения последнего процесса дефрагментации в автоматическом режиме.
   * <p>
   * Ключ: идентификатор данного;<br>
   * Значение: описание дефрагментации {@link S5SequenceFragmentInfo}
   */
  private final IMapEdit<Gwid, S5SequenceFragmentInfo> unionAllFragments =
      new WrapperMap<>( new HashMap<Gwid, S5SequenceFragmentInfo>() );

  /**
   * Блокировка доступа к данным дефрагментации: {@link #unionCandidates}, {@link #unionCandidateFragments}
   */
  private final S5Lockable unionLock = new S5Lockable();

  /**
   * Создает писатель последовательностей
   *
   * @param aBackendCore {@link IS5BackendCoreSingleton} ядро бекенда сервера
   * @param aSequenceFactory {@link ISequenceFactory} фабрика последовательностей блоков
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5SequenceLazyWriter( IS5BackendCoreSingleton aBackendCore, ISequenceFactory<V> aSequenceFactory ) {
    super( aBackendCore, aSequenceFactory );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов
  //
  @Override
  public void doWrite( EntityManager aEntityManager, IList<S> aSequences, S5SequenceWriteStat aStatistics ) {
    if( aSequences.size() == 0 ) {
      // Частный случай. Нечего делать
      return;
    }
    // TODO: сделать опцию позволяющую менять режим записи (с потоками или без)
    // @formatter:off
    /*
    // Журнал для потоков
    ILogger logger = new Logger( Logger.getLogger( WriteThread.class ) );
    // Фабрика менеджеров постоянства
    EntityManagerFactory entityManagerFactory = entityManagerFactory();
    // Список созданных менджеров постоянства
    IListEdit<EntityManager> ems = new ElemArrayList<>();
    try {
      // Исполнитель s5-потоков проверки данных
      S5WriteThreadExecutor executor = new S5WriteThreadExecutor( writeExecutor(), logger );
      for( int index = 0, n = aInfoes.size(); index < n; index++ ) {
        // Менеджер постоянства
        EntityManager em = entityManagerFactory.createEntityManager();
        // Присоединение менджера к текущей транзакции
        em.joinTransaction();
        // Размещение в списке для последующего завершения
        ems.add( em );
        // Описания данного
        I fragmentInfo = aInfoes.get( index );
        S sequence = aSequences.get( index );
        // Поток
        IS5WriteThread thread = new WriteThread( em, fragmentInfo, sequence, aStatistics, logger );
        // Регистрация потока
        executor.add( thread );
      }
      // Запуск потоков (с ожиданием, с поднятием исключений на ошибках потоков)
      executor.run( true, true );
      addUnionCandidates( aSequences );
    }
    finally {
      for( EntityManager em : ems ) {
        em.close();
      }
    }
    */
    //@formatter:on
    for( int index = 0, n = aSequences.size(); index < n; index++ ) {
      writeSequence( aEntityManager, aSequences.get( index ), aStatistics, index );
    }
    // Добавить записанные данные на дефрагментацию
    addUnionCandidates( aSequences );
  }

  @Override
  public S5SequenceUnionStat<V> doUnion( IOptionSet aArgs ) {
    // Журнал для потоков
    ILogger uniterLogger = Logger.getLogger( LOG_UNITER_ID );
    // Состояние задачи объединения данного
    S5SequenceUnionStat<V> statistics = new S5SequenceUnionStat<>();
    IOptionSetEdit args = new OptionSet( aArgs );
    // Признак ручного или автоматического объединения данных
    boolean isAuto = !args.hasValue( IS5SequenceUnionOptions.INTERVAL );
    // Список данных для объединения
    IList<ISequenceFragmentInfo> infoes =
        (!isAuto ? prepareManual( args ) : prepareAuto( args, statistics, uniterLogger ));
    // Текущий размер очереди на дефрагментацию
    int queueSize = getUnionQueueSize();
    // Установки общих данных статистики
    statistics.setInfoes( infoes != null ? infoes : IList.EMPTY );
    statistics.setQueueSize( queueSize );
    // Проверка возможности провести дефрагментацию
    if( infoes == null || infoes.size() == 0 ) {
      // Запрет выполнять объединение
      return statistics;
    }
    // Вывод в журнал
    Integer c = Integer.valueOf( infoes.size() );
    Integer q = Integer.valueOf( queueSize );
    ITimeInterval interval = IS5SequenceUnionOptions.INTERVAL.getValue( args ).asValobj();
    uniterLogger.info( MSG_UNION_START_THREAD, c, q, interval );
    // Исполнитель s5-потоков записи данных
    S5WriteThreadExecutor executor = new S5WriteThreadExecutor( unionExecutor(), uniterLogger );
    for( ISequenceFragmentInfo info : infoes ) {
      // Регистрация потока
      executor.add( new UnionThread( info, args, statistics, uniterLogger ) );
    }
    // Запуск потоков (с ожиданием, без поднятия исключений на ошибках потоков)
    executor.run( true, false );
    // Оповещение наследников о проведение дефрагментации блоков
    onUnionEvent( args, infoes, uniterLogger );
    return statistics;
  }

  @Override
  public S5SequenceValidationStat doValidation( IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNull( aArgs );
    // Состояние задачи проверки блоков
    S5SequenceValidationStat statistics = new S5SequenceValidationStat();
    // Менеджер постоянства
    EntityManager em = entityManagerFactory().createEntityManager();
    try {
      // Журнал для потоков
      ILogger logger = Logger.getLogger( LOG_VALIDATOR_ID );
      // Исполнитель s5-потоков проверки данных
      S5WriteThreadExecutor executor = new S5WriteThreadExecutor( validationExecutor(), logger );

      // Идентификаторы данных. null: неопределены
      IGwidList gwids = null;
      if( aArgs.hasValue( IS5SequenceValidationOptions.GWIDS ) ) {
        gwids = IS5SequenceValidationOptions.GWIDS.getValue( aArgs ).asValobj();
      }
      if( gwids == null ) {
        // Запрос всех идентификаторов данных которые есть в базе данных
        gwids = getAllGwids( em, sequenceFactory().tableNames() );
      }
      for( Gwid gwid : gwids ) {
        executor.add( new ValidationThread( gwid, aArgs, statistics, logger ) );
      }
      // Запуск потоков (с ожиданием, без поднятия исключений на ошибках потоков)
      executor.run( true, false );
    }
    finally {
      em.close();
    }
    return statistics;
  }

  // ------------------------------------------------------------------------------------
  // Методы для наследников
  //
  /***
   * Добавить кандидатов для процесса дефрагментации
   *
   * @param aSequences {@link IList}&lt;S&gt; список последовательностей для проверки дефрагментации
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected final void addUnionCandidates( IList<S> aSequences ) {
    TsNullArgumentRtException.checkNull( aSequences );
    long currTime = System.currentTimeMillis();
    lockWrite( unionLock );
    try {
      for( S sequence : aSequences ) {
        Gwid gwid = sequence.gwid();
        S5SequenceFragmentInfo fragmentInfo = unionCandidateFragments.findByKey( gwid );
        if( fragmentInfo == null ) {
          String tableName = tableName( sequenceFactory(), gwid );
          fragmentInfo = new S5SequenceFragmentInfo( tableName, gwid, currTime, currTime, 0 );
          unionCandidateFragments.put( gwid, fragmentInfo );
          unionCandidates.putTail( gwid );
        }
        fragmentInfo.setFragmentCount( fragmentInfo.fragmentCount() + 1 );
        fragmentInfo.setInterval( fragmentInfo.interval().startTime(), currTime );
      }
    }
    finally {
      unlockWrite( unionLock );
    }
  }

  // ------------------------------------------------------------------------------------
  // Методы для переопределения наследниками
  //
  /**
   * Проводит запись последовательности данных
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства используемый для записи
   * @param aSequence S последовательность
   * @param aStatistics {@link S5SequenceWriteStat} редактируемая статистика
   * @param aThreadIndex int индекс записи (индекс потока или просто порядковый номер, для журнала)
   * @return {@link ISequenceBlock}&lt;V&gt; блок последних значений записаных в dbms. null: неопределен
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException попытка конкуретного изменения данных последовательности
   */
  @SuppressWarnings( "unchecked" )
  protected ISequenceBlock<V> writeSequence( EntityManager aEntityManager, S aSequence, S5SequenceWriteStat aStatistics,
      int aThreadIndex ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aSequence, aSequence );
    // Индекс потока
    Integer threadIndex = Integer.valueOf( aThreadIndex );
    // Описание типа значений
    IParameterized typeInfo = aSequence.typeInfo();
    // Идентификатор данного
    Gwid gwid = aSequence.gwid();
    // Фабрика последовательностей
    ISequenceFactory<V> factory = sequenceFactory();
    // Статистика dbms
    S5DbmsStatistics dbmsStat = aStatistics.dbmsStatistics();
    // Локальная статистика обработки
    ITimeInterval writeInterval = aSequence.interval();
    ITimeInterval loadedInterval = aSequence.interval();
    // Последовательность блоков.
    List<ISequenceBlock<V>> blocks = null;
    // Интервал запрашиваемых блоков
    long st = loadedInterval.startTime();
    long et = loadedInterval.endTime();
    IQueryInterval interval = new QueryInterval( EQueryIntervalType.OSOE, st, et );
    long startTime = System.currentTimeMillis();
    try {
      startTime = System.currentTimeMillis();
      int firstPosition = 0;
      int maxResultCount = 0; // Запрос всех блоков
      blocks = loadBlocks( aEntityManager, factory, gwid, interval, firstPosition, maxResultCount );
      // Формирование статистики по загруженным блокам
      int loadedCount = blocks.size();
      int loadedTime = (int)(System.currentTimeMillis() - startTime);
      dbmsStat.addLoaded( loadedCount, loadedTime );
      // Вывод загруженных блоков
      if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
        printBlocks( logger(), format( MSG_DBMS_LOADED, threadIndex, gwid, interval ), blocks );
      }
    }
    catch( OutOfMemoryError e ) {
      // TODO: уточнить отработать ошибку чтения блоков по java heap, а не отбрасывать запрос клиента
      throw new TsIllegalArgumentRtException( ERR_SEQUENCE_OUT_OF_MEMORY_ERROR, gwid, interval, cause( e ) );
    }
    startTime = System.currentTimeMillis();
    if( blocks.size() > 0 ) {
      // Коррекция времени последовательности. Интервал определяется так, чтобы целевая последовательность содержала
      // все считанные блоки и при этом интервал исходной последовательности (aSequence) должен быть подмножеством
      // целевой последовательности
      st = Math.min( st, blocks.get( 0 ).startTime() );
      et = Math.max( et, blocks.get( blocks.size() - 1 ).endTime() );
      loadedInterval = new TimeInterval( st, et );
    }
    // Использование реализации ElemArrayList из-за prepareDbmsBlocks которому это необходимо для производительности
    ElemArrayList<ISequenceBlock<V>> addedBlocks = new ElemArrayList<>( aSequence.blocks().size() );
    ElemArrayList<ISequenceBlock<V>> mergedBlocks = new ElemArrayList<>( blocks.size() );
    ElemArrayList<ISequenceBlock<V>> removedBlocks = new ElemArrayList<>( blocks.size() );
    ElemArrayList<ISequenceBlock<V>> unmanagedBlocks = new ElemArrayList<>( blocks.size() );

    int writedCount = aSequence.blocks().size();
    long addedTime = 0;
    long mergedTime = 0;
    long removedTime = 0;

    // Формирование последовательности
    IQueryInterval targetInterval = new QueryInterval( EQueryIntervalType.CSCE, st, et );
    Iterable<ISequenceBlockEdit<V>> targetBlocks = (Iterable<ISequenceBlockEdit<V>>)(Object)blocks;
    IS5SequenceEdit<V> target = factory.createSequence( gwid, targetInterval, targetBlocks );
    // Редактирование значений
    if( !target.edit( aSequence, removedBlocks ) ) {
      // Редактирования не произошло
      aStatistics.addWriteBlocks( writeInterval, writedCount );
      return null;
    }
    // Синхронизация с dbms. Удаление блоков
    removeBlocksFromDbms( aEntityManager, sequenceFactory(), gwid, removedBlocks, dbmsStat );
    // Синхронизация с dbms. Добавление/обновление блоков блоков
    writeBlocksToDbms( aEntityManager, target.blocks(), logger(), dbmsStat );
    // Вывод журнал
    if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      Integer ac = Integer.valueOf( addedBlocks.size() );
      Integer mc = Integer.valueOf( mergedBlocks.size() );
      Integer rc = Integer.valueOf( removedBlocks.size() );
      printBlocks( logger(), format( MSG_DBMS_ADDED, threadIndex, gwid, ac ), addedBlocks );
      printBlocks( logger(), format( MSG_DBMS_MERGED, threadIndex, gwid, mc ), mergedBlocks );
      printBlocks( logger(), format( MSG_DBMS_REMOVED, threadIndex, gwid, rc ), removedBlocks );
    }
    // Формирование статистики
    aStatistics.addWriteBlocks( writeInterval, writedCount );
    // Возвращение последнего блока
    return target.blocks().last();
  }

  /**
   * Событие: проведена дефрагментация хранения значений данных.
   *
   * @param aArgs {@link IOptionSet} аргументы для объединения блоков (смотри {@link IS5SequenceUnionOptions}).
   * @param aInfoes {@link IList}&lt;I&gt; описания данных для которых была проведена дефрагментация
   * @param aLogger {@link ILogger} журнал работы
   */
  protected void onUnionEvent( IOptionSet aArgs, IList<ISequenceFragmentInfo> aInfoes, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aArgs, aInfoes, aLogger );
  }

  // ------------------------------------------------------------------------------------
  // Внутренняя реализация
  //
  // Имя таблицы
  /**
   * Возвращает имя таблицы базы данных хранения значений данного
   *
   * @param aFactory {@link ISequenceFactory} фабрика формирования последовательностей
   * @param aDataGwid {@link Gwid} идентификатор данного
   * @return String имя таблицы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static String tableName( ISequenceFactory<?> aFactory, Gwid aDataGwid ) {
    TsNullArgumentRtException.checkNulls( aFactory, aDataGwid );
    // Параметризованное описание типа данного
    IParameterized typeInfo = aFactory.typeInfo( aDataGwid );
    return StridUtils.getLast( OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString() );
  }

  /**
   * Возращает описания и добавляет необходимые параметры для выполнения объединения в ручном режиме
   *
   * @param aArgs {@link IOptionSetEdit} аргументы для дефрагментации блоков
   * @return {@link IList}&lt;I&gt; список описаний данных для объединения
   * @throws TsNullArgumentRtException аргумент = null
   */
  private IList<ISequenceFragmentInfo> prepareManual( IOptionSetEdit aArgs ) {
    TsNullArgumentRtException.checkNull( aArgs );
    // Менеджер постоянства
    EntityManager em = entityManagerFactory().createEntityManager();
    try {
      // Запрос всех идентификаторов данных которые есть в базе данных
      IGwidList allGwids = getAllGwids( em, sequenceFactory().tableNames() );
      // Информация о фрагментации
      IListEdit<ISequenceFragmentInfo> infoes = new ElemArrayList<>( allGwids.size() );
      // Идентификаторы данных. null: неопределены
      IGwidList gwids = null;
      if( aArgs.hasValue( IS5SequenceUnionOptions.GWIDS ) ) {
        gwids = IS5SequenceUnionOptions.GWIDS.getValue( aArgs ).asValobj();
      }
      // Интервал дефрагментации
      ITimeInterval interval = IS5SequenceUnionOptions.INTERVAL.getValue( aArgs ).asValobj();
      // Указан список объединяемых данных
      for( Gwid gwid : allGwids ) {
        if( gwids == null || gwids.size() == 0 || gwids.hasElem( gwid ) ) {
          String tableName = tableName( sequenceFactory(), gwid );
          infoes.add( new S5SequenceFragmentInfo( tableName, gwid, interval, -1 ) );
        }
      }
      return infoes;
    }
    finally {
      em.close();
    }
  }

  /**
   * Возращает описания и добавляет необходимые параметры для выполнения объединения в автоматическом режиме
   *
   * @param aArgs {@link IOptionSetEdit} аргументы для дефрагментации блоков .
   * @param aStatistics {@link S5SequenceUnionStat} статистика с возможностью редактирования
   * @param aLogger {@link ILogger} журнал
   * @return {@link IList}&lt;ISequenceFragmentInfo&gt; список описаний фрагментированности данных
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private IList<ISequenceFragmentInfo> prepareAuto( IOptionSetEdit aArgs, S5SequenceUnionStat<V> aStatistics,
      ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aArgs, aLogger );
    // Фабрика последовательностей
    ISequenceFactory<V> factory = sequenceFactory();
    // Смещение дефрагментации от текущего времении
    long offset = IS5SequenceUnionOptions.AUTO_OFFSET.getValue( aArgs ).asLong();
    // Максимальное время фрагментации
    long fragmentTimeout = IS5SequenceUnionOptions.AUTO_FRAGMENT_TIMEOUT.getValue( aArgs ).asLong();
    // // Минимальное количество блоков для принудительного объединения
    // int fragmentCountMin = IS5SequenceUnionOptions.AUTO_FRAGMENT_COUNT_MIN.getValue( aArgs ).asInt();
    // // Максимальное количество блоков для принудительного объединения
    // int fragmentCountMax = IS5SequenceUnionOptions.AUTO_FRAGMENT_COUNT_MAX.getValue( aArgs ).asInt();
    // Максимальное количество потоков дефрагментации
    int threadCount = IS5SequenceUnionOptions.AUTO_THREADS_COUNT.getValue( aArgs ).asInt();
    // Мощность поиска дефрагментации
    int lookupCountMax = IS5SequenceUnionOptions.AUTO_LOOKUP_COUNT.getValue( aArgs ).asInt();
    // Текущее время
    long currTime = System.currentTimeMillis();
    // Время завершения интервала дефрагментации
    long fragmentEndTime = currTime - offset;
    EntityManager em = entityManagerFactory().createEntityManager();
    // Возвращаемый результат
    IListEdit<ISequenceFragmentInfo> retValue = new ElemArrayList<>();
    // Текущее количество выполненных операций поиска дефрагментации
    int lookupCount = 0;
    try {
      while( true ) {
        if( lookupCountMax > 0 && lookupCount >= lookupCountMax ) {
          // Установлено ограничение по количество операций поиска за один проход
          break;
        }
        // Счетчик операций поиска
        lookupCount++;
        // Идентификатор данного
        Gwid gwid = null;
        // Дефрагментация полученая записью при ожидании процесса дефрагментации
        S5SequenceFragmentInfo candiateFragments = null;
        // Общая дефрагментация данного с момента прошлого процесса дефрагментации. null: дефрагментация неопределена
        S5SequenceFragmentInfo allFragments = null;
        lockWrite( unionLock );
        try {
          gwid = unionCandidates.peekHeadOrNull();
          if( gwid != null ) {
            // Создаем потоко-независимую копию дефрагментации данного
            candiateFragments = new S5SequenceFragmentInfo( unionCandidateFragments.getByKey( gwid ) );
            ISequenceFragmentInfo tmp = unionAllFragments.findByKey( gwid );
            if( tmp != null ) {
              allFragments = new S5SequenceFragmentInfo( tmp );
            }
          }
        }
        finally {
          unlockWrite( unionLock );
        }
        if( gwid == null ) {
          // Нет данных для дефрагментации
          break;
        }
        // Параметризованное описание типа данного
        IParameterized typeInfo = factory.typeInfo( gwid );
        // Минимальное количество блоков для принудительного объединения
        int fragmentCountMin = OP_BLOCK_SIZE_MAX.getValue( typeInfo.params() ).asInt();
        // Максимальное количество блоков для принудительного объединения
        int fragmentCountMax = fragmentCountMin;
        // Количество текущих фрагментов
        int fragmentCount = (allFragments != null ? allFragments.fragmentCount() : 0)
            + (candiateFragments != null ? candiateFragments.fragmentCount() : 0);
        // Реальное количество фрагментов полученных чтением из базы данных
        ISequenceFragmentInfo realAllFragments = ISequenceFragmentInfo.NULL;
        if( allFragments == null || (fragmentCountMin < 0 || fragmentCount >= fragmentCountMin) ) {
          // Фрагментация с момента прошлого процесса дефрагментации данного неопределена или накопилось много
          // фрагментов (по записи) которые могут быть дефрагментированы. Запрос к базе для получения реальной
          // дефрагментации. Максимальное количество значений в блоке:
          int maxSize = OP_BLOCK_SIZE_MAX.getValue( typeInfo.params() ).asInt();
          // Фактическая дефрагментация данного полученная чтением из базы данных
          // Внимание! Несмотря на легковесность SQL-запроса, при интенсивной работе с dbms может вызвать задержку
          realAllFragments = findFragmentationTime( em, factory, gwid, fragmentEndTime, maxSize, fragmentCountMin,
              fragmentCountMax, fragmentTimeout );
          // Обновление статистики
          aStatistics.addLookupCount();
          // // Обновление количества фрагментов
          // if( realAllFragments != ISequenceFragmentInfo.NULL ) {
          // fragmentCount = realAllFragments.fragmentCount();
          // }
        }
        // Признак необходимости провести дефрагментацию
        boolean needDefragmentation =
            (realAllFragments != ISequenceFragmentInfo.NULL && realAllFragments.fragmentCount() > 0);
        // Признак того, что дефрагментированная последовательность выбрана полностью
        boolean fragmentCompleted =
            (!needDefragmentation || fragmentCountMax < 0 || realAllFragments.fragmentCount() < fragmentCountMax);
        lockWrite( unionLock );
        try {
          if( !needDefragmentation || fragmentCompleted ) {
            // Данное поставлено на обработку (дефрагментацию)
            unionCandidates.getHeadOrNull();
            unionCandidateFragments.removeByKey( gwid );
          }
          if( !fragmentCompleted ) {
            // Будет произведена повторная попытка дефрагментации. Блоки дефрагментации сбрасываются
            String tableName = tableName( sequenceFactory(), gwid );
            long startTime = System.currentTimeMillis();
            long endTime = startTime;
            unionCandidateFragments.put( gwid, new S5SequenceFragmentInfo( tableName, gwid, startTime, endTime, 0 ) );
          }
          if( !needDefragmentation ) {
            // Накопленных фрагментов недостаточно для процесса дефрагментации
            if( allFragments == null ) {
              // Таблица хранения значений данного
              String tableName = tableName( sequenceFactory(), gwid );
              // Количество фрагментов найденных в базе, но недостаточно для дефрагментации
              int realCount =
                  (realAllFragments == ISequenceFragmentInfo.NULL ? 0 : realAllFragments.fragmentAfterCount());
              // Время с которого начинаются фрагменты найденные в базе
              long realStartTime =
                  (realAllFragments == ISequenceFragmentInfo.NULL ? currTime : realAllFragments.interval().endTime());
              // Реальная фрагментация (полученная из базы) до текущего времени
              allFragments = new S5SequenceFragmentInfo( tableName, gwid, realStartTime, currTime, realCount );
              unionAllFragments.put( gwid, allFragments );
              continue;
            }
            // Перемещение накопленных с момента прошлой проверки фрагментов
            long endTime = System.currentTimeMillis();
            long startTime = allFragments.interval().startTime();
            allFragments.setInterval( (startTime <= endTime ? startTime : endTime), endTime );
            allFragments.setFragmentCount( fragmentCount );
            unionAllFragments.put( gwid, allFragments );
            continue;
          }
          // Следующий проход (после дефрагментации) требуем повторить запрос фрагментации из базы
          unionAllFragments.removeByKey( gwid );
        }
        finally {
          unlockWrite( unionLock );
        }
        // Добавление в результат
        retValue.add( realAllFragments );
        aLogger.debug( MSG_UNION_AUTO_ADD_INFO, gwid );
        if( !fragmentCompleted ) {
          // Требуется повторная дефрагментация
          aLogger.debug( MSG_UNION_AUTO_REPEAT, gwid );
          break;
        }
        if( retValue.size() >= threadCount ) {
          // Сформировано необходимое количество данных для дефрагментации
          break;
        }
      }
      return retValue;
    }
    finally {
      em.close();
    }
  }

  /**
   * Асинхронная задача обновления данных последовательности данного
   *
   * @author mvk
   */
  protected class WriteThread
      extends S5AbstractWriteThread {

    private final EntityManager       em;
    private final S                   sequence;
    private final S5SequenceWriteStat stat;

    /**
     * Создание асинхронной задачи проверки блоков последовательности данных
     *
     * @param aEntityManager {@link EntityManager} менеджер постоянства
     * @param aSequence S последовательность
     * @param aStatistics {@link S5SequenceWriteStat} статистика выполнения задачи
     * @param aLogger {@link ILogger} журнал
     * @throws TsNullArgumentRtException любой аргумент = null
     */
    protected WriteThread( EntityManager aEntityManager, S aSequence, S5SequenceWriteStat aStatistics,
        ILogger aLogger ) {
      super( aLogger );
      TsNullArgumentRtException.checkNulls( aEntityManager, aSequence, aStatistics );
      em = aEntityManager;
      sequence = aSequence;
      stat = aStatistics;
    }

    // ------------------------------------------------------------------------------------
    // API для наследников
    //
    /**
     * Возвращает менджер постоянства используемый потоком
     *
     * @return {@link EntityManager} менджер постоянства
     */
    protected final EntityManager entityManager() {
      return em;
    }

    /**
     * Возвращает последоватльность для записи в dbms
     *
     * @return S последовательность
     */
    protected final S sequence() {
      return sequence;
    }

    /**
     * Возвращает статистика выполнения задачи
     *
     * @return {@link S5SequenceWriteStat} статистика выполнения задачи
     */
    protected final S5SequenceWriteStat stat() {
      return stat;
    }

    // ------------------------------------------------------------------------------------
    // Реализация абстрактных методов S5AbstractThread
    //
    @Override
    protected void doRun() {
      // Запись последовательности
      writeSequence( em, sequence, stat, threadIndex() );
    }

    @Override
    protected void doCancel() {
      // nop
    }

    // ------------------------------------------------------------------------------------
    // Внутренняя реализация
    //
  }

  /**
   * Асинхронная задача объединения блоков последовательности данного
   *
   * @author mvk
   */
  private class UnionThread
      extends S5AbstractWriteThread {

    private final ISequenceFragmentInfo  fragmentInfo;
    private final S5SequenceUnionStat<V> stat;

    /**
     * Создание асинхронной задачи объединения блоков последовательности данных
     *
     * @param aArgs {@link IOptionSet} аргументы для объединения блоков (смотри {@link IS5SequenceUnionOptions}).
     * @param aFragmentInfo I описание фрагментации данных
     * @param aStatistics {@link S5SequenceUnionStat} статистика выполнения задачи
     * @param aLogger {@link ILogger} журнал
     * @throws TsNullArgumentRtException любой аргумент = null
     */
    UnionThread( ISequenceFragmentInfo aFragmentInfo, IOptionSet aArgs, S5SequenceUnionStat<V> aStatistics,
        ILogger aLogger ) {
      super( aLogger );
      TsNullArgumentRtException.checkNulls( aArgs, aFragmentInfo, aStatistics );
      fragmentInfo = aFragmentInfo;
      stat = aStatistics;
    }

    // ------------------------------------------------------------------------------------
    // Реализация абстрактных методов S5AbstractThread
    //
    @Override
    protected void doRun() {
      EntityManager em = entityManagerFactory().createEntityManager();
      try {
        // Идентификатор данного
        Gwid gwid = fragmentInfo.gwid();
        // Список блокируемых данных
        IGwidList lockedGwids = new GwidList( gwid );
        // Блокировка доступа к данным (false: без проверки текущий транзакции)
        tryLockGwids( lockedGwids, false );
        try {
          try {
            UserTransaction tx = InitialContext.doLookup( USER_TRANSACTION_JNDI );
            // Открываем транзакцию
            tx.begin();
            try {
              // Присоединение менеджера постоянства к транзкации
              em.joinTransaction();
              // Объединение блоков на интервале
              S5SequenceUnionStat<V> result = unionInterval( em, gwid, fragmentInfo.interval(), logger() );
              stat.addDbmsMerged( result.dbmsMergedCount() );
              stat.addDbmsRemoved( result.dbmsRemovedCount() );
              stat.addValues( result.valueCount() );
              stat.addErrors( result.errorCount() );
              // Завершаем транзакцию
              tx.commit();
            }
            catch( Throwable e ) {
              // Откат транзакции на любой ошибке
              tx.rollback();
              throw e;
            }
          }
          catch( Throwable e ) {
            stat.addErrors( 1 );
            logger().error( e, ERR_ASYNC_UNION_TASK, fragmentInfo, cause( e ) );
          }
        }
        finally {
          unlockGwids( lockedGwids );
        }
      }
      finally {
        em.close();
      }
    }

    @Override
    protected void doCancel() {
      // nop
    }

    // ------------------------------------------------------------------------------------
    // Внутренняя реализация
    //
  }

  /**
   * Асинхронная задача проверки блоков последовательности данного
   *
   * @author mvk
   */
  private class ValidationThread
      extends S5AbstractWriteThread {

    private final Gwid                     gwid;
    private final IQueryInterval           interval;
    private final boolean                  canUpdate;
    private final boolean                  canRemove;
    private final S5SequenceValidationStat stat;

    /**
     * Создание асинхронной задачи проверки блоков последовательности данных
     *
     * @param aGwid {@link Gwid} идентификатор данного
     * @param aArgs {@link IOptionSet} аргументы для проверки блоков ).
     * @param aStatistics буфер для размещения результата выполнения задачи
     * @param aLogger {@link ILogger} журнал
     * @throws TsNullArgumentRtException любой аргумент = null
     */
    ValidationThread( Gwid aGwid, IOptionSet aArgs, S5SequenceValidationStat aStatistics, ILogger aLogger ) {
      super( aLogger );
      TsNullArgumentRtException.checkNulls( aGwid, aArgs, aStatistics, aLogger );
      gwid = aGwid;
      ITimeInterval ti = IS5SequenceValidationOptions.INTERVAL.getValue( aArgs ).asValobj();
      if( ti == null ) {
        ti = ITimeInterval.WHOLE;
      }
      interval = new QueryInterval( EQueryIntervalType.CSCE, ti.startTime(), ti.endTime() );
      canRemove = IS5SequenceValidationOptions.FORCE_REPAIR.getValue( aArgs ).asBool();
      canUpdate = IS5SequenceValidationOptions.REPAIR.getValue( aArgs ).asBool() || canRemove;
      stat = aStatistics;
    }

    // ------------------------------------------------------------------------------------
    // Реализация абстрактных методов S5AbstractThread
    //
    @Override
    protected void doRun() {
      EntityManager em = entityManagerFactory().createEntityManager();
      try {
        IGwidList lockedGwids = new GwidList( gwid );
        // Блокировка доступа к данным (false: без проверки текущий транзакции)
        tryLockGwids( lockedGwids, false );
        try {
          try {
            UserTransaction tx = InitialContext.doLookup( USER_TRANSACTION_JNDI );
            // Открываем транзакцию
            tx.begin();
            try {
              // Присоединение менеджера постоянства к транзкации
              em.joinTransaction();
              // Объединение блоков
              validationInterval( em, gwid, interval, stat, canUpdate, canRemove, logger() );
              // Завешаем транзакцию
              tx.commit();
            }
            catch( Throwable e ) {
              // Откат транзакции на любой ошибке
              tx.rollback();
              throw e;
            }
          }
          catch( Throwable e ) {
            logger().error( e, ERR_ASYNC_UNION_TASK, gwid, cause( e ) );
          }
        }
        finally {
          unlockGwids( lockedGwids );
        }
      }
      finally {
        em.close();
        stat.addInfo();
      }
    }

    @Override
    protected void doCancel() {
      // nop
    }

    // ------------------------------------------------------------------------------------
    // Внутренняя реализация
    //
  }

  // ------------------------------------------------------------------------------------
  // Внутренняя реализация
  //
  /**
   * Возвращает текущий размер очереди на дефрагментацию
   *
   * @return int размер очереди
   */
  private int getUnionQueueSize() {
    lockRead( unionLock );
    try {
      return unionCandidates.size();
    }
    finally {
      unlockRead( unionLock );
    }
  }

}
