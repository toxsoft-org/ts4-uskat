package org.toxsoft.uskat.s5.server.sequences.impl;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.bricks.time.EQueryIntervalType.*;
import static org.toxsoft.core.tslib.bricks.time.impl.TimeUtils.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.jboss.ejb3.annotation.TransactionTimeout;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.idgen.IStridGenerator;
import org.toxsoft.core.tslib.bricks.strid.idgen.UuidStridGenerator;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.TimeInterval;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.coll.synch.SynchronizedStringMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.classes.IS5ClassBackend;
import org.toxsoft.uskat.classes.IS5ClassHistorableBackend;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.api.linkserv.ISkLinkService;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.core.impl.dto.DtoObject;
import org.toxsoft.uskat.s5.common.sysdescr.ISkSysdescrReader;
import org.toxsoft.uskat.s5.legacy.QueryInterval;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.maintenance.*;
import org.toxsoft.uskat.s5.server.sequences.reader.IS5SequenceReadQuery;
import org.toxsoft.uskat.s5.server.sequences.writer.*;
import org.toxsoft.uskat.s5.server.singletons.S5ServiceSingletonUtils;
import org.toxsoft.uskat.s5.server.statistics.EStatisticInterval;
import org.toxsoft.uskat.s5.server.statistics.S5StatisticWriter;
import org.toxsoft.uskat.s5.utils.platform.S5ServerPlatformUtils;
import org.toxsoft.uskat.s5.utils.schedules.*;
import org.toxsoft.uskat.s5.utils.threads.impl.S5ReadThreadExecutor;

/**
 * Базовая (абстрактная) реализация синглетона поддержки бекенда обрабатывающего последовательности данных
 * {@link IS5Sequence}.
 *
 * @author mvk
 * @param <S> тип последовательности значений данного
 * @param <V> тип значения последовательности
 */
public abstract class S5BackendSequenceSupportSingleton<S extends IS5Sequence<V>, V extends ITemporal<?>>
    extends S5BackendSupportSingleton
    implements IS5BackendSequenceSupportSingleton<S, V> {

  private static final long serialVersionUID = 157157L;

  /**
   * Менеджер постоянства entities
   */
  @PersistenceContext
  private EntityManager em;

  /**
   * База данных
   */
  @Resource
  private DataSource dataSource;

  /**
   * Служба таймера
   */
  @Resource
  private TimerService timerService;

  /**
   * backend управления классами системы
   */
  @EJB
  private IS5BackendSysDescrSingleton sysdescrBackend;

  /**
   * backend управления объектами системы
   */
  @EJB
  private IS5BackendObjectsSingleton objectsBackend;

  /**
   * Читатель системного описания
   */
  private ISkSysdescrReader sysdescrReader;

  // TODO: сделать статистику работы когда появятся currdata и histdata

  /**
   * Фабрика формирования последовательностей
   */
  private ISequenceFactory<V> factory;

  /**
   * Писатель(статегия) сохранения последовательностей в dbms
   */
  private IS5SequenceWriter<S, V> sequenceWriter;

  /**
   * Исполнитель потоков чтения блоков
   */
  private ManagedExecutorService readExecutor;

  /**
   * Карта выполнямых запросов чтения хранимых данных
   * <p>
   * Ключ карты: идентификатор запроса; <br>
   * Значение карты: выполняемый запрос
   */
  private final IStringMapEdit<IS5SequenceReadQuery> readQueries = new SynchronizedStringMap<>( new StringMap<>() );

  /**
   * Генератор идентификаторов запроса чтения хранимых данных
   */
  private final IStridGenerator uuidGenerator;

  /**
   * Таймеры расписания задачи объединения блоков
   */
  private IListEdit<Timer> unionTimers = new ElemArrayList<>();

  /**
   * Исполнитель потоков дефрагментации
   */
  private ManagedExecutorService unionExecutor;

  /**
   * Последний запущенный поток дефрагментации блоков последовательностей. null: неопределен
   */
  private S5SequenceUniterThread uniterThread;

  /**
   * Рабочая(формируемая) статистика ввода/вывода блоков {@link ISequenceBlock} в dbms по интервалам времени. <br>
   * Индекс в списке = индекс {@link EStatisticInterval}.
   */
  // private IListEdit<S5DbmsStatistics> dbmsWorksStatistics = new ElemArrayList<>( EStatisticInterval.values().length
  // );

  /**
   * Метки времени (мсек с начала эпохи) формирования статистических данных dbmsWorksStatistics по интервалам времени.
   * <br>
   * Индекс в списке = индекс {@link EStatisticInterval}.
   */
  // private long dbmsWorksStatisticsTimestamps[];

  /**
   * Сформированная статистика ввода/вывода блоков {@link ISequenceBlock} в dbms по интервалам времени. <br>
   * Индекс в списке = индекс {@link EStatisticInterval}. {@link IS5DbmsStatistics#NULL}: статистика еще не определена
   * за период времени
   */
  // private IListEdit<IS5DbmsStatistics> dbmsStatistics = new ElemArrayList<>( EStatisticInterval.values().length );

  /**
   * Таймер обновления статистики
   */
  private Timer dbmsStatisticsTimer;

  /**
   * Писатель статитистики объекта {@link IS5ClassHistorableBackend}. null: нет соединения
   */
  private S5StatisticWriter statisticWriter;

  /**
   * Индексы записи данных статистики
   */
  // TODO: сделать статистику работы когда появятся currdata и histdata
  // private static final int STAT_INDEX_WRITE_COUNT = 0;
  // private static final int STAT_INDEX_LOADED_COUNT = 1;
  // private static final int STAT_INDEX_LOADED_TIME = 2;
  // private static final int STAT_INDEX_INSERT_COUNT = 3;
  // private static final int STAT_INDEX_INSERT_TIME = 4;
  // private static final int STAT_INDEX_MERGE_COUNT = 5;
  // private static final int STAT_INDEX_MERGE_TIME = 6;
  // private static final int STAT_INDEX_REMOVED_COUNT = 7;
  // private static final int STAT_INDEX_REMOVED_TIME = 8;
  // private static final int STAT_INDEX_REWRITED_COUNT = 9;
  // private static final int STAT_INDEX_ERROR_COUNT = 10;
  // private static final int STAT_INDEX_FRAGMENT_COUNT = 11;
  // private static final int STAT_INDEX_FRAGMENT_LOOKUPS = 12;
  // private static final int STAT_INDEX_FRAGMENT_THREADS = 13;
  // private static final int STAT_INDEX_FRAGMENT_REMOVES = 14;
  // private static final int STAT_INDEX_FRAGMENT_ERRORS = 15;

  // TODO: сделать статистику работы когда появятся currdata и histdata
  // /**
  // * Набор записи статистики текущих данных
  // */
  // private IWriteCurrDataSet dbmsStatisticsCurrDataSet;
  //
  // /**
  // * Набор записи статистики хранимых данных
  // */
  // private IWriteHistDataSet dbmsStatisticsHistDataSet;

  /**
   * Журнал записи последовательностей
   */
  private ILogger writeLogger = getLogger( LOG_WRITER_ID );

  /**
   * Журнал дефрагментации последовательностей
   */
  private ILogger uniterLogger = getLogger( LOG_UNITER_ID );

  /**
   * Журнал проверки последовательностей
   */
  private ILogger validatorLogger = getLogger( LOG_VALIDATOR_ID );

  /**
   * Конструктор для наследников.
   *
   * @param aId String идентификатор синглетона
   * @param aDescription String описание синглетона
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5BackendSequenceSupportSingleton( String aId, String aDescription ) {
    super( aId, aDescription );
    uuidGenerator = new UuidStridGenerator( UuidStridGenerator.createState( aId ) );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5ServiceSingletonBase
  //
  @Override
  protected IOptionSet doCreateConfiguration() {
    return new OptionSet();
  }

  @Override
  protected void onConfigChanged( IOptionSet aPrevConfig, IOptionSet aNewConfig ) {
    S5ScheduleExpressionList prevCalendars = IS5SequenceAddonConfig.UNION_CALENDARS.getValue( aPrevConfig ).asValobj();
    S5ScheduleExpressionList newCalendars = IS5SequenceAddonConfig.UNION_CALENDARS.getValue( aNewConfig ).asValobj();
    if( !newCalendars.equals( prevCalendars ) ) {
      // Изменение календарей дефрагментации
      updateUnionTimers();
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendSupportSingleton
  //
  @Override
  protected void doInitSupport() {
    // TODO: 2020-05-07 mvkd ???
    // // Запрет повторных вызовов инициализации
    // TsIllegalStateRtException.checkNoNull( connection );
    // // Использование ISkConnection
    // connection = localConnectionProvider.openConnection( id() );
    // ReentrantReadWriteLock lock = connection.mainLock();
    // ISkCoreApi coreApi = connection.coreApi();
    // ISkSysdescr sysdescr = coreApi.sysdescr();
    // typesManager = new S5SynchronizedDataTypesManager( sysdescr.dataTypesManager(), lock );
    // classInfoManager = new S5SynchronizedClassInfoManager( sysdescr.classInfoManager(), lock );
    // objectService = new S5SynchronizedObjectService( coreApi.objService(), lock );
    // Поиск исполнителя потоков чтения блоков
    readExecutor = S5ServiceSingletonUtils.lookupExecutor( READ_EXECUTOR_JNDI );
    // Поиск исполнителя потоков объединения блоков
    unionExecutor = S5ServiceSingletonUtils.lookupExecutor( UNION_EXECUTOR_JNDI );
    // Запуск потока дефрагментации
    uniterThread = new S5SequenceUniterThread( getBusinessObject(), MSG_UNION_AUTHOR_SCHEDULE, uniterLogger );
    unionExecutor.execute( uniterThread );
    // Бизнес интерфейс синглетона
    IS5BackendSequenceSupportSingleton<S, V> singletonView = getBusinessObject();
    // Регистрация слушателей ядра. 100: низкий приоритет
    backend().addBackendCoreInterceptor( singletonView, 100 );
    // Перехват операций службы IClassService
    sysdescrBackend.addClassInterceptor( singletonView, doGetInterceptorsPriority() );
    // Перехват операций службы IObjectService
    objectsBackend.addObjectsInterceptor( singletonView, doGetInterceptorsPriority() );
    // Читатель системного описания
    sysdescrReader = sysdescrBackend.getReader();
    // Инициализация статегии записи последовательностей
    sequenceWriter = new S5SequenceLastBlockWriter<>( backend(), factory() );
    // Инициализация таймера запуска задачи объединения блоков исторических данных
    updateUnionTimers();
    // Формирование таймера обновления статистики
    long statisticsUpdateInterval = EStatisticInterval.SECOND.milli();
    TimerConfig tc = new TimerConfig( "StatisticsTimer", false ); //$NON-NLS-1$
    dbmsStatisticsTimer = timerService.createIntervalTimer( statisticsUpdateInterval, statisticsUpdateInterval, tc );
  }

  @Override
  protected void doCloseSupport() {
    // Выполнение завершения наследниками
    super.doClose();
    // TODO: сделать статистику работы когда появятся currdata и histdata
    // // Завершение работы набора записи статистики
    // if( dbmsStatisticsCurrDataSet != null ) {
    // dbmsStatisticsCurrDataSet.close();
    // }
    // if( dbmsStatisticsHistDataSet != null ) {
    // dbmsStatisticsHistDataSet.close();
    // }
    // Завершение потока дефрагментации
    uniterThread.close();
    // Завершение работы таймеров
    dbmsStatisticsTimer.cancel();
    for( Timer timer : unionTimers ) {
      timer.cancel();
    }
    // Завершение работы писателя последовательностей
    sequenceWriter.close();
    // Бизнес интерфейс синглетона
    IS5BackendSequenceSupportSingleton<S, V> singletonView = getBusinessObject();
    // Дерегистрация перехвата в службе IObjectService
    objectsBackend.removeObjectsInterceptor( singletonView );
    // Дерегистрация перехвата в службе IClassService
    sysdescrBackend.removeClassInterceptor( singletonView );
    // TODO: 2020-05-07 mvkd ???
    // // Завершение соединения
    // if( connection != null ) {
    // connection.close();
    // connection = null;
    // }
  }

  @Timeout
  @TransactionTimeout( value = SEQUENCE_TIMER_TRANSACTION_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
  // Блокировка на запись серьезно изменяет ранее хорошо отлаженную реализацию и проводит к тому, что
  // проваливаются регламенты по календарю и возникают ложные блокировки службы. Да и в целом блокировать в общем случае
  // на запись не есть хорошо по вопросам производительности. Клиенты, если им необходимо блокировка на запись должны
  // самостоятельно решать этот вопрос, например, через асинхронные вызовы
  // @Lock( LockType.WRITE )
  // 2021-04-04
  @Lock( LockType.READ )
  private void doTimerEventHandle( Timer aTimer ) {
    if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      // Запускается обработка события таймера
      logger().debug( MSG_TIMER_EVENT_START, aTimer.getInfo() );
    }
    try {

      // Обновление писателя статистики если он определен
      S5StatisticWriter stat = statisticWriter;
      if( stat != null ) {
        // S5PlatformInfo pi = S5ServerPlatformUtils.getPlatformInfo();
        // IAtomicValue loadAverage = dvFloat( pi.loadAverage() );
        // stat.onEvent( STAT_BACKEND_NODE_LOAD_AVERAGE, loadAverage );
        // stat.onEvent( STAT_BACKEND_NODE_LOAD_MAX, loadAverage );
        // stat.onEvent( STAT_BACKEND_NODE_FREE_PHYSICAL_MEMORY, dvFloat( pi.freePhysicalMemory() ) );
        // stat.onEvent( STAT_BACKEND_NODE_MAX_HEAP_MEMORY, dvFloat( pi.maxHeapMemory() ) );
        // stat.onEvent( STAT_BACKEND_NODE_USED_HEAP_MEMORY, dvFloat( pi.usedHeapMemory() ) );
        // stat.onEvent( STAT_BACKEND_NODE_MAX_NON_HEAP_MEMORY, dvFloat( pi.maxNonHeapMemory() ) );
        // stat.onEvent( STAT_BACKEND_NODE_USED_NON_HEAP_MEMORY, dvFloat( pi.usedNonHeapMemory() ) );
        // stat.onEvent( STAT_BACKEND_NODE_OPEN_TX_MAX, dvInt( txManager.openCount() ) );
        // stat.onEvent( STAT_BACKEND_NODE_OPEN_SESSION_MAX, dvInt( sessionManager.openSessionCount() ) );
        stat.update();
      }

      // Получаем бизнес интерфейс синглетона, чтобы вызов doUnionTask выполнялся вне транзакции
      for( int index = 0, n = unionTimers.size(); index < n; index++ ) {
        Timer timer = unionTimers.get( index );
        if( timer.equals( aTimer ) ) {
          // Опции службы
          IOptionSet config = configuration();
          // Максимальная загрузка системы при которой возможна дефрагментация
          final double loadAverageMax = IS5SequenceAddonConfig.UNION_LOAD_AVERAGE_MAX.getValue( config ).asDouble();
          // Проверка возможности выполнения дефрагментации при текущей загрузке системы
          double loadAverage = S5ServerPlatformUtils.loadAverage();
          if( loadAverage > loadAverageMax ) {
            // Загруженность системы не позволяет провести дефрагментацию значений
            Double la = Double.valueOf( loadAverage );
            uniterLogger.warning( ERR_UNION_DISABLE_BY_LOAD_AVERAGE, id(), la );
            return;
          }
          // Запуск потока дефрагментации
          if( !uniterThread.tryStart() ) {
            // Запрет дефрагментации значений по календарю (незавершен предыдущий процесс)
            uniterLogger.warning( ERR_UNION_DISABLE_BY_PREV_UNITER, id() );
            return;
          }
          return;
        }
      }
    }
    finally {
      if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
        // Завершается обработка события таймера
        logger().debug( MSG_TIMER_EVENT_FINISH, aTimer.getInfo() );
      }

    }
  }

  @Override
  protected String doBackendClassId() {
    return IS5ClassHistorableBackend.CLASS_ID;
  }

  // ------------------------------------------------------------------------------------
  // IS5SequenceReader
  //
  @Lock( LockType.READ )
  @Override
  public long findStartTime( IGwidList aGwids, long aAfterTime ) {
    TsNullArgumentRtException.checkNull( aGwids );
    long traceStartTime = System.currentTimeMillis();
    // WORKAROUND: нельзя давать в SQL запросы константы TimeUtils.MIN_TIMESTAMP, TimeUtils.MIN_TIMESTAMP ???
    Long afterTime = Long.valueOf( aAfterTime == MIN_TIMESTAMP ? aAfterTime + 1 : aAfterTime );
    // Результат
    long retValue = MAX_TIMESTAMP;
    for( Gwid gwid : aGwids ) {
      // Поиск времени последовательности. Находим первый блок в котором может быть значение на/за указанным временем
      long startTime = S5SequenceSQL.findTimeAfter( em, factory(), gwid, afterTime );
      if( startTime != MAX_TIMESTAMP && startTime < retValue ) {
        // Найден новый минимум
        retValue = startTime;
      }
    }
    // Журналирование
    Integer infoSizeI = Integer.valueOf( aGwids.size() );
    Long traceTime = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    String result = String.valueOf( TimeUtils.timestampToString( retValue ) );
    Long resultL = Long.valueOf( retValue );
    logger().debug( MSG_FIND_START_TIME, infoSizeI, traceTime, result, resultL );
    return retValue;
  }

  @Lock( LockType.READ )
  @Override
  public ILongList findStartTimes( IGwidList aGwids, long aAfterTime ) {
    TsNullArgumentRtException.checkNull( aGwids );
    long traceStartTime = System.currentTimeMillis();
    // WORKAROUND: нельзя давать в SQL запросы константы TimeUtils.MIN_TIMESTAMP, TimeUtils.MIN_TIMESTAMP ???
    Long afterTime = Long.valueOf( aAfterTime == MIN_TIMESTAMP ? aAfterTime + 1 : aAfterTime );
    // Ближайшее время к метке
    long minTime = MAX_TIMESTAMP;
    // Результат
    ILongListEdit retValue = new LongArrayList( aGwids.size() );
    for( Gwid gwid : aGwids ) {
      // Поиск времени последовательности. Находим первый блок в котором может быть значение на/за указанным временем
      long startTime = S5SequenceSQL.findTimeAfter( em, factory(), gwid, afterTime );
      retValue.add( startTime );
      if( startTime != MAX_TIMESTAMP && startTime < minTime ) {
        // Найден новый минимум
        minTime = startTime;
      }
    }
    // Журналирование
    Integer infoSizeI = Integer.valueOf( aGwids.size() );
    Long traceTime = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    String result = String.valueOf( TimeUtils.timestampToString( minTime ) );
    Long resultL = Long.valueOf( minTime );
    logger().debug( MSG_FIND_START_TIME, infoSizeI, traceTime, result, resultL );
    return retValue;
  }

  @Lock( LockType.READ )
  @Override
  public long findEndTime( IGwidList aGwids, long aBeforeTime ) {
    TsNullArgumentRtException.checkNull( aGwids );
    long traceStartTime = System.currentTimeMillis();
    // WORKAROUND: нельзя давать в SQL запросы константы TimeUtils.MIN_TIMESTAMP, TimeUtils.MIN_TIMESTAMP ???
    Long beforeTime = Long.valueOf( aBeforeTime == MAX_TIMESTAMP ? aBeforeTime - 1 : aBeforeTime );
    // Результат
    long retValue = MIN_TIMESTAMP;
    for( Gwid gwid : aGwids ) {
      // Поиск времени последовательности. Находим первый блок в котором может быть значение на/перед указанным временем
      long endTime = S5SequenceSQL.findTimeBefore( em, factory(), gwid, beforeTime );
      if( endTime != MIN_TIMESTAMP && endTime > retValue ) {
        // Найден новый максимум
        retValue = endTime;
      }
    }
    // Журналирование
    Integer infoSizeI = Integer.valueOf( aGwids.size() );
    Long traceTime = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    String result = String.valueOf( TimeUtils.timestampToString( retValue ) );
    Long resultL = Long.valueOf( retValue );
    logger().debug( MSG_FIND_END_TIME, infoSizeI, traceTime, result, resultL );
    return retValue;
  }

  @Lock( LockType.READ )
  @Override
  public ILongList findEndTimes( IGwidList aGwid, long aBeforeTime ) {
    TsNullArgumentRtException.checkNull( aGwid );
    long traceStartTime = System.currentTimeMillis();
    // WORKAROUND: нельзя давать в SQL запросы константы TimeUtils.MIN_TIMESTAMP, TimeUtils.MIN_TIMESTAMP ???
    Long beforeTime = Long.valueOf( aBeforeTime == MAX_TIMESTAMP ? aBeforeTime - 1 : aBeforeTime );
    // Ближайшее время к метке
    long maxTime = MIN_TIMESTAMP;
    // Результат
    ILongListEdit retValue = new LongArrayList( aGwid.size() );
    for( Gwid gwid : aGwid ) {
      // Поиск времени последовательности. Находим первый блок в котором может быть значение на/перед указанным временем
      long endTime = S5SequenceSQL.findTimeBefore( em, factory(), gwid, beforeTime );
      retValue.add( endTime );
      if( endTime != MIN_TIMESTAMP && endTime > maxTime ) {
        // Найден новый максимум
        maxTime = endTime;
      }
    }
    // Журналирование
    Integer infoSizeI = Integer.valueOf( aGwid.size() );
    Long traceTime = Long.valueOf( System.currentTimeMillis() - traceStartTime );
    String result = String.valueOf( TimeUtils.timestampToString( maxTime ) );
    Long resultL = Long.valueOf( maxTime );
    logger().debug( MSG_FIND_END_TIME, infoSizeI, traceTime, result, resultL );
    return retValue;
  }

  @Lock( LockType.READ )
  @Override
  public IStridGenerator uuidGenerator() {
    return uuidGenerator;
  }

  @Lock( LockType.READ )
  @Override
  public IStringList readQueryIds() {
    return new StringArrayList( readQueries.keys() );
  }

  @Lock( LockType.READ )
  @Override
  public IList<S> readSequences( IGwidList aGwids, IQueryInterval aInterval, long aMaxExecutionTimeout ) {
    return readSequences( IS5FrontendRear.NULL, uuidGenerator.nextId(), aGwids, aInterval, aMaxExecutionTimeout );
  }

  @Lock( LockType.READ )
  @Override
  public IList<S> readSequences( IS5FrontendRear aFrontend, String aQueryId, IGwidList aGwids, IQueryInterval aInterval,
      long aTimeout ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aQueryId, aGwids, aInterval );
    if( aGwids.size() == 0 ) {
      // Частный случай, ничего не запрашивается
      return IList.EMPTY;
    }
    if( readQueries.hasKey( aQueryId ) ) {
      // Запрос уже выполняется
      throw new TsIllegalArgumentRtException( ERR_QUERY_IS_ALREADY_EXECUTE, aQueryId );
    }
    long traceStartTime = System.currentTimeMillis();
    int count = aGwids.size();
    // Идентификаторов данных по реализациям. Ключ: полное имя реализации. Значение: список идентификаторовданных
    IStringMapEdit<GwidList> gwidsByImpls = new StringMap<>();
    // Список данных принятых в обработку
    Set<Gwid> readingGwids = new HashSet<>();
    for( Gwid gwid : aGwids ) {
      if( readingGwids.contains( gwid ) ) {
        // gwid указан в списке запроса несколько раз
        logger().warning( ERR_GWID_DOUBLE_ON_READ, gwid );
        continue;
      }
      // Данное принимается в обработку
      readingGwids.add( gwid );
      // Описание данного
      IParameterized typeInfo = factory().typeInfo( gwid );
      String blockImplClassName = OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString();
      GwidList gwids = gwidsByImpls.findByKey( blockImplClassName );
      if( gwids == null ) {
        gwids = new GwidList();
        gwidsByImpls.put( blockImplClassName, gwids );
      }
      if( gwids.hasElem( gwid ) ) {
        // gwid указан в списке запроса несколько раз
        logger().warning( ERR_GWID_DOUBLE_ON_READ, gwid );
        continue;
      }
      gwids.add( gwid );
    }
    // Соединение с dmbs
    try( Connection dbConnection = dataSource.getConnection() ) {
      // Выполняемый запрос
      IS5SequenceReadQuery query =
          new S5SequenceReadQuery( aFrontend, aQueryId, aInterval, factory, dbConnection, aTimeout );
      readQueries.put( aQueryId, query );
      try {
        // Исполнитель s5-потоков чтения данных
        S5ReadThreadExecutor<IMap<Gwid, S>> executor = new S5ReadThreadExecutor<>( readExecutor, logger() );
        // Результат выполнения запроса
        IMapEdit<Gwid, S> results = new ElemMap<>();
        for( int index = 0, n = gwidsByImpls.size(); index < n; index++ ) {
          IGwidList gwids = gwidsByImpls.values().get( index );
          // Регистрация потока
          S5SequenceThreadRead<S, V> thread = new S5SequenceThreadRead<>( query, gwids );
          executor.add( thread );
        }
        // Запуск потоков (с ожиданием завершения, c поднятием исключений на ошибках потоков)
        executor.run( true, true );
        // Результат запроса
        IList<IMap<Gwid, S>> executorResults = executor.results();
        for( IMap<Gwid, S> result : executorResults ) {
          results.putAll( result );
        }
        // Формирование окончательного результата
        IListEdit<S> retValue = new ElemArrayList<>();
        for( Gwid gwid : aGwids ) {
          S sequence = results.findByKey( gwid );
          if( sequence == null ) {
            // Нет данных
            IQueryInterval interval = new QueryInterval( CSCE, aInterval.startTime(), aInterval.endTime() );
            sequence = (S)factory.createSequence( gwid, interval, IList.EMPTY );
          }
          retValue.add( sequence );
        }
        // Журналирование
        Long traceTimeout = Long.valueOf( System.currentTimeMillis() - traceStartTime );
        logger().debug( MSG_READ_SEQUENCE_TIME, Integer.valueOf( count ), traceTimeout );
        return retValue;
      }
      finally {
        readQueries.removeByKey( aQueryId );
      }
    }
    catch( SQLException e ) {
      throw new TsInternalErrorRtException( e );
    }
  }

  @Lock( LockType.READ )
  @Override
  public IS5SequenceReadQuery cancelReadQuery( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    IS5SequenceReadQuery retValue = readQueries.removeByKey( aQueryId );
    if( retValue != null ) {
      retValue.close();
    }
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendSequenceSupportSingleton
  //
  @SuppressWarnings( "unchecked" )
  // 2020-09-15 mvk попытка вернуть блокировку READ после разделения backend на currdata и histdata
  // @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Lock( LockType.READ )
  @Override
  public void writeSequences( IList<S> aSequences ) {
    TsNullArgumentRtException.checkNull( aSequences );
    if( OP_BACKEND_DATA_WRITE_DISABLE.getValue( backend().initialConfig().impl().params() ).asBool() ) {
      // Запрет записи хранимых данных
      return;
    }
    // Установка фабрики формирования последовательности (подготовка к возможному редактированию)
    for( S sequence : aSequences ) {
      ((S5Sequence<V>)sequence).setFactory( factory );
    }
    // Формирование статистики
    S5StatisticWriter stat = statisticWriter;
    try {
      // Опции службы
      IOptionSet config = configuration();
      // Максимальная загрузка системы при которой возможна запись хранимых данных
      final double loadAverageMax = IS5SequenceAddonConfig.WRITE_LOAD_AVERAGE_MAX.getValue( config ).asDouble();
      // Проверка возможности выполнения записи при текущей загрузке системы
      double loadAverage = S5ServerPlatformUtils.loadAverage();
      if( loadAverage > loadAverageMax ) {
        // Загруженность системы не позволяет провести запись значений
        Double la = Double.valueOf( loadAverage );
        writeLogger.error( ERR_WRITE_DISABLE_BY_LOAD_AVERAGE, id(), la );
        return;
      }
      // Запись хранимых данных
      IS5SequenceWriteStat writeStat = sequenceWriter.write( entityManager(), aSequences );
      // Формирование статистики
      if( stat != null ) {
        IS5DbmsStatistics dbmsStat = writeStat.dbmsStatistics();
        stat.onEvent( STAT_HISTORABLE_BACKEND_WRITED_COUNT, AV_1 );
        stat.onEvent( STAT_HISTORABLE_BACKEND_LOADED_COUNT, avInt( dbmsStat.loadedCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_LOADED_TIME, avInt( dbmsStat.loadedTime() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_INSERTED_COUNT, avInt( dbmsStat.insertedCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_INSERTED_TIME, avInt( dbmsStat.insertedTime() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_MERGED_COUNT, avInt( dbmsStat.mergedCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_MERGED_TIME, avInt( dbmsStat.mergedTime() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_REMOVED_COUNT, avInt( dbmsStat.removedCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_REMOVED_TIME, avInt( dbmsStat.removedTime() ) );
      }
    }
    catch( RuntimeException e ) {
      if( stat != null ) {
        stat.onEvent( STAT_HISTORABLE_BACKEND_ERROR_COUNT, AV_1 );
      }
      // Журнал
      writeLogger.error( e );
      // Формирование статистики
      // Делегирование ошибки
      throw e;
    }
    catch( Throwable e ) {
      if( stat != null ) {
        stat.onEvent( STAT_HISTORABLE_BACKEND_ERROR_COUNT, AV_1 );
      }
      // Журнал
      writeLogger.error( e );
      // Формирование статистики
      // Делегирование ошибки
      throw new TsInternalErrorRtException( e );
    }
  }

  @Asynchronous
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Lock( LockType.READ )
  @Override
  public void writeAsyncSequences( IList<S> aSequences ) {
    TsNullArgumentRtException.checkNull( aSequences );
    writeSequences( aSequences );
  }

  @TransactionTimeout( value = SEQUENCE_UNION_TRANSACTION_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
  @Lock( LockType.READ )
  @Override
  public IS5SequenceUnionStat union( String aAuthor, IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aAuthor, aArgs );
    if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      // Запуск задачи дефрагментации
      logger().debug( MSG_SINGLETON_UNION_TASK_START );
    }
    try {
      // Время запуска операции
      long traceStartTime = System.currentTimeMillis();
      // Запуск процесса регламента
      IS5SequenceUnionStat unionStat = sequenceWriter.union( aArgs );
      // Формирование статистики
      S5StatisticWriter stat = statisticWriter;
      if( stat != null ) {
        stat.onEvent( STAT_HISTORABLE_BACKEND_DEFRAGMENT_COUNT, AV_1 );
        stat.onEvent( STAT_HISTORABLE_BACKEND_FRAGMENT_LOOKUP_COUNT, avInt( unionStat.lookupCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_FRAGMENT_THREAD_COUNT, avInt( unionStat.infoes().size() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_FRAGMENT_VALUE_COUNT, avInt( unionStat.valueCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_FRAGMENT_MERGED_COUNT, avInt( unionStat.dbmsMergedCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_FRAGMENT_REMOVED_COUNT, avInt( unionStat.dbmsRemovedCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_FRAGMENT_ERROR_COUNT, avInt( unionStat.errorCount() ) );
      }
      // Журнал
      if( unionStat.infoes().size() > 0 || unionStat.queueSize() > 0 ) {
        // Список описаний данных в запросе на дефрагментацию
        IList<ISequenceFragmentInfo> fragmentInfoes = unionStat.infoes();
        // Вывод статистики
        Long d = Long.valueOf( (System.currentTimeMillis() - traceStartTime) / 1000 );
        Integer tc = Integer.valueOf( fragmentInfoes.size() );
        String threaded = TsLibUtils.EMPTY_STRING;
        if( fragmentInfoes.size() > 0 ) {
          threaded = "[" + fragmentInfoes.get( 0 ).toString(); //$NON-NLS-1$
          if( fragmentInfoes.size() > 1 ) {
            threaded += ", ..."; //$NON-NLS-1$
          }
          threaded += "]"; //$NON-NLS-1$
        }
        Integer lc = Integer.valueOf( unionStat.lookupCount() );
        Integer mc = Integer.valueOf( unionStat.dbmsMergedCount() );
        Integer rc = Integer.valueOf( unionStat.dbmsRemovedCount() );
        Integer vc = Integer.valueOf( unionStat.valueCount() );
        Integer ec = Integer.valueOf( unionStat.errorCount() );
        Integer qs = Integer.valueOf( unionStat.queueSize() );
        uniterLogger.info( MSG_UNION_TASK_FINISH, id(), aAuthor, lc, tc, threaded, mc, rc, vc, ec, qs, d );
      }
      // Информация о данных в проведенной дефрагментации
      IList<ISequenceFragmentInfo> fragmentInfos = unionStat.infoes();
      // Обработка ошибок целостности
      if( unionStat.errorCount() == 0 || fragmentInfos.size() == 0 ) {
        return unionStat;
      }
      // Опции конфигурации
      IOptionSet config = configuration();
      // Признак требование проводить автоматическое восстановление целостности
      final boolean autoRepair = IS5SequenceValidationOptions.AUTO_REPAIR.getValue( config ).asBool();
      if( !autoRepair ) {
        // При дефрагментации произошли ошибки. Разрешено автоматическое восстановление последовательностей
        repairSequences( fragmentInfos );
      }
      return unionStat;
    }
    finally {
      // Завершение задачи дефрагментации
      logger().debug( MSG_SINGLETON_UNION_TASK_FINISH );
    }
  }

  @TransactionTimeout( value = SEQUENCE_VALIDATION_TRANSACTION_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
  @Lock( LockType.READ )
  @Override
  public IS5SequenceValidationStat validation( String aAuthor, IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aAuthor, aArgs );
    logger().info( MSG_VALIDATION_TASK_START, aAuthor );
    // Время запуска операции
    long startTime = System.currentTimeMillis();
    // Запуск процесса проверки
    IS5SequenceValidationStat statistics = sequenceWriter.validation( aArgs );
    // Вывод статистики
    Long i = Long.valueOf( statistics.infoCount() );
    Long a = Long.valueOf( statistics.processedCount() );
    Long w = Long.valueOf( statistics.warnCount() );
    Long e = Long.valueOf( statistics.errCount() );
    Long u = Long.valueOf( statistics.dbmsMergedCount() );
    Long r = Long.valueOf( statistics.dbmsRemovedCount() );
    Long n = Long.valueOf( statistics.nonOptimalCount() );
    Long v = Long.valueOf( statistics.valuesCount() );
    Long d = Long.valueOf( (System.currentTimeMillis() - startTime) / 1000 );
    validatorLogger.info( MSG_VALIDATION_TASK_FINISH, aAuthor, i, a, w, e, u, r, n, v, d );
    return statistics;
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendCoreInterceptor
  //
  @TransactionAttribute( TransactionAttributeType.MANDATORY )
  @Lock( LockType.READ )
  @Override
  public boolean beforeSetConnection( ISkConnection aOldConnection, ISkConnection aNewConnection ) {
    // nop
    return true;
  }

  @TransactionAttribute( TransactionAttributeType.MANDATORY )
  @Lock( LockType.READ )
  @Override
  public void afterSetConnection( ISkConnection aOldConnection, ISkConnection aNewConnection ) {
    ISkCoreApi coreApi = aNewConnection.coreApi();
    ISkObjectService objectService = coreApi.objService();
    ISkLinkService linkService = coreApi.linkService();
    // Идентификатор узла
    Skid nodeId = nodeId();
    // Полный (с именем узла) идентификатор backend
    Skid backendId = backendId();
    // Проверка существования backend как объекта системы
    IS5ClassHistorableBackend backend = objectService.find( backendId );
    if( backend == null ) {
      // Бекенд не найден. Создание бекенда как объекта системы
      backend = objectService.defineObject( new DtoObject( backendId, IOptionSet.NULL, IStringMap.EMPTY ) );
      linkService.defineLink( backendId, IS5ClassBackend.LNKID_NODE, ISkidList.EMPTY, new SkidList( nodeId ) );
    }
    if( statisticWriter != null ) {
      // Завершение работы предыдущего писателя статистики
      statisticWriter.close();
    }
    // Создание писателя статистики узла сервера
    statisticWriter = new S5StatisticWriter( aNewConnection, backendId, STAT_HISTORABLE_BACKEND_PARAMS );
  }

  // ------------------------------------------------------------------------------------
  // IS5ClassesInterceptor (методы запрещено делать final так как они являються частью view)
  //
  @TransactionAttribute( TransactionAttributeType.MANDATORY )
  @Lock( LockType.READ )
  @Override
  public void beforeCreateClass( IDtoClassInfo aClassInfo ) {
    TsNullArgumentRtException.checkNull( aClassInfo );
    doBeforeCreateClass( aClassInfo );
  }

  @TransactionAttribute( TransactionAttributeType.MANDATORY )
  @Lock( LockType.READ )
  @Override
  public void afterCreateClass( IDtoClassInfo aClassInfo ) {
    TsNullArgumentRtException.checkNull( aClassInfo );
    doAfterCreateClass( aClassInfo );
  }

  @TransactionAttribute( TransactionAttributeType.MANDATORY )
  @Lock( LockType.READ )
  @Override
  public void beforeUpdateClass( IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo,
      IStridablesList<IDtoClassInfo> aDescendants ) {
    TsNullArgumentRtException.checkNulls( aPrevClassInfo, aNewClassInfo, aDescendants );
    doBeforeUpdateClass( aPrevClassInfo, aNewClassInfo, aDescendants );
  }

  @TransactionAttribute( TransactionAttributeType.MANDATORY )
  @Lock( LockType.READ )
  @Override
  public void afterUpdateClass( IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo,
      IStridablesList<IDtoClassInfo> aDescendants ) {
    TsNullArgumentRtException.checkNulls( aPrevClassInfo, aNewClassInfo, aDescendants );
    doAfterUpdateClass( aPrevClassInfo, aNewClassInfo, aDescendants );
  }

  @TransactionAttribute( TransactionAttributeType.MANDATORY )
  @Lock( LockType.READ )
  @Override
  public void beforeDeleteClass( IDtoClassInfo aClassInfo ) {
    TsNullArgumentRtException.checkNull( aClassInfo );
    doBeforeDeleteClass( aClassInfo );
  }

  @TransactionAttribute( TransactionAttributeType.MANDATORY )
  @Lock( LockType.READ )
  @Override
  public void afterDeleteClass( IDtoClassInfo aClassInfo ) {
    TsNullArgumentRtException.checkNull( aClassInfo );
    doAfterDeleteClass( aClassInfo );
  }

  // ------------------------------------------------------------------------------------
  // IS5ObjectsInterceptor (методы запрещено делать final так как они являются частью view)
  //
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Lock( LockType.READ )
  @Override
  public IDtoObject beforeFindObject( Skid aSkid, IDtoObject aObj ) {
    TsNullArgumentRtException.checkNull( aSkid );
    return doBeforeFindObject( aSkid, aObj );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Lock( LockType.READ )
  @Override
  public IDtoObject afterFindObject( Skid aSkid, IDtoObject aObj ) {
    TsNullArgumentRtException.checkNull( aSkid );
    return doAfterFindObject( aSkid, aObj );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Lock( LockType.READ )
  @Override
  public void beforeReadObjects( IStringList aClassIds, IListEdit<IDtoObject> aObjs ) {
    TsNullArgumentRtException.checkNulls( aClassIds, aObjs );
    doBeforeReadObjects( aClassIds, aObjs );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Lock( LockType.READ )
  @Override
  public void afterReadObjects( IStringList aClassIds, IListEdit<IDtoObject> aObjs ) {
    TsNullArgumentRtException.checkNulls( aClassIds, aObjs );
    doAfterReadObjects( aClassIds, aObjs );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Lock( LockType.READ )
  @Override
  public void beforeReadObjectsByIds( ISkidList aSkids, IListEdit<IDtoObject> aObjs ) {
    TsNullArgumentRtException.checkNulls( aSkids, aObjs );
    doBeforeReadObjectsByIds( aSkids, aObjs );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Lock( LockType.READ )
  @Override
  public void afterReadObjectsByIds( ISkidList aSkids, IListEdit<IDtoObject> aObjs ) {
    TsNullArgumentRtException.checkNulls( aSkids, aObjs );
    doAfterReadObjectsByIds( aSkids, aObjs );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Lock( LockType.READ )
  @Override
  public void beforeWriteObjects( //
      IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs ) {
    TsNullArgumentRtException.checkNulls( aRemovedObjs, aUpdatedObjs, aCreatedObjs );
    doBeforeWriteObjects( aRemovedObjs, aUpdatedObjs, aCreatedObjs );
  }

  @TransactionAttribute( TransactionAttributeType.MANDATORY )
  @Lock( LockType.READ )
  @Override
  public void afterWriteObjects( //
      IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs ) {
    TsNullArgumentRtException.checkNulls( aRemovedObjs, aUpdatedObjs, aCreatedObjs );
    doAfterWriteObjects( aRemovedObjs, aUpdatedObjs, aCreatedObjs );
  }

  // ------------------------------------------------------------------------------------
  // API для наследников
  //
  /**
   * Возвращает менеджер постоянства JPA используемый синглетоном
   *
   * @return {@link EntityManager} менеджер постоянства JPA
   */
  protected final EntityManager entityManager() {
    return em;
  }

  /**
   * Возвращает исполнителя потоков чтения блоков
   *
   * @return {@link ExecutorService} исполнитель потоков
   */
  protected final ExecutorService readExecutor() {
    return readExecutor;
  }

  /**
   * Возвращает backend управления классами
   *
   * @return {@link IS5BackendSysDescrSingleton} backend управления классами
   */
  protected final IS5BackendSysDescrSingleton sysdescrBackend() {
    return sysdescrBackend;
  }

  /**
   * Возвращает backend управления объектами
   *
   * @return {@link IS5BackendObjectsSingleton} backend управления объектами
   */
  protected final IS5BackendObjectsSingleton objectsBackend() {
    return objectsBackend;
  }

  /**
   * Возвращает читателя системного описания
   *
   * @return {@link ISkSysdescrReader} читатель системного описания
   */
  protected final ISkSysdescrReader sysdescrReader() {
    return sysdescrReader;
  }

  /**
   * Возвращает фабрику формирования последовательностей
   *
   * @param <T> тип фабрики
   * @return {@link ISequenceFactory} фабрика формирования последовательностей
   */
  @SuppressWarnings( "unchecked" )
  protected final <T extends ISequenceFactory<V>> T factory() {
    if( factory == null ) {
      factory = doCreateFactory();
    }
    return (T)factory;
  }

  // ------------------------------------------------------------------------------------
  // Методы для переопределения наследниками
  //
  /**
   * Возвращает бизнес интерфейс конечной реализации синглетона
   *
   * @return {@link IS5BackendSequenceSupportSingleton} бизнес интерфейс
   */
  protected abstract IS5BackendSequenceSupportSingleton<S, V> getBusinessObject();

  /**
   * Создать фабрику формирования последовательностей
   *
   * @return {@link ISequenceFactory} фабрика формирования последовательностей
   */
  protected abstract ISequenceFactory<V> doCreateFactory();

  /**
   * Возвращает приоритет перехватчиков (interceptors) backend системного описания и объектов
   *
   * @return int приоритет регистрации перехватчиков
   */
  protected int doGetInterceptorsPriority() {
    return 3;
  }

  /**
   * Вызывается до создания нового класса в системе
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassInfo {@link IDtoClassInfo} описание создаваемого класса
   * @throws TsIllegalStateRtException запрещено создавать класс
   */
  protected void doBeforeCreateClass( IDtoClassInfo aClassInfo ) {
    // nop
  }

  /**
   * Вызывается после создания класса в системе, но до завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassInfo {@link IDtoClassInfo} описание созданного класса
   * @throws TsIllegalStateRtException отменить создание класса (откат транзакции)
   */
  protected void doAfterCreateClass( IDtoClassInfo aClassInfo ) {
    // nop
  }

  /**
   * Вызывается до редактирования существующего класса в системе
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aPrevClassInfo {@link IDtoClassInfo} описание редактируемого класса (старая редакция)
   * @param aNewClassInfo {@link IDtoClassInfo} описание редактируемого класса (новая редакция)
   * @param aDescendants {@link IStridablesList}&lt;IDtoClassInfo&gt; описания классов-потомков изменяемого класса
   * @throws TsIllegalStateRtException запрещено редактировать класс
   */
  protected void doBeforeUpdateClass( IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo,
      IStridablesList<IDtoClassInfo> aDescendants ) {
    // nop
  }

  /**
   * Вызывается после редактирования существующего класса в системе, но до завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aPrevClassInfo {@link IDtoClassInfo} описание редактируемого класса (старая редакция)
   * @param aNewClassInfo {@link IDtoClassInfo} описание редактируемого класса (новая редакция)
   * @param aDescendants {@link IStridablesList}&lt;IDtoClassInfo&gt; описания классов-потомков измененного класса
   * @throws TsIllegalStateRtException отменить редактирование класса (откат транзакции)
   */
  protected void doAfterUpdateClass( IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo,
      IStridablesList<IDtoClassInfo> aDescendants ) {
    // nop
  }

  /**
   * Вызывается до удаления класса из системы
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassInfo {@link IDtoClassInfo} описание удаляемого класса
   * @throws TsIllegalStateRtException запрещено удалять класс
   */
  protected void doBeforeDeleteClass( IDtoClassInfo aClassInfo ) {
    // nop
  }

  /**
   * Вызывается после удаления класса в системе, но до завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassInfo {@link IDtoClassInfo} описание удаленного класса
   * @throws TsIllegalStateRtException отменить удаление класса (откат транзакции)
   */
  protected void doAfterDeleteClass( IDtoClassInfo aClassInfo ) {
    // nop
  }

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendObjectsSingleton#findObject(Skid)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aSkid {@link Skid} - идентификатор объекта
   * @param aObj {@link IDtoObject} объект найденный ранее интерсепторами
   * @return {@link ISkObject} - найденный объект или <code>null</code> если нет такого
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendObjectsSingleton#findObject(Skid)}
   */
  protected IDtoObject doBeforeFindObject( Skid aSkid, IDtoObject aObj ) {
    return aObj;
  }

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendObjectsSingleton#findObject(Skid)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aSkid {@link Skid} - идентификатор объекта
   * @param aObj {@link IDtoObject} объект найденный ранее службой или интерсепторами
   * @return {@link ISkObject} - найденный объект или <code>null</code> если нет такого
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendObjectsSingleton#findObject(Skid)}
   */
  protected IDtoObject doAfterFindObject( Skid aSkid, IDtoObject aObj ) {
    return aObj;
  }

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassIds {@link IStringList} список идентификаторов классов
   * @param aObjs {@link IListEdit}&lt;{link IDtoObject}&gt; список объектов найденных ранее интерсепторами
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   */
  protected void doBeforeReadObjects( IStringList aClassIds, IListEdit<IDtoObject> aObjs ) {
    // nop
  }

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aClassIds {@link IStringList} список идентификаторов классов
   * @param aObjs {@link IListEdit}&lt;{link IDtoObject}&gt; список объектов найденных ранее службой и/или
   *          интерсепторами
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   */
  protected void doAfterReadObjects( IStringList aClassIds, IListEdit<IDtoObject> aObjs ) {
    // nop
  }

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aSkids {@link ISkidList} список идентификаторов объектов
   * @param aObjs {@link IListEdit}&lt;{link IDtoObject}&gt; список объектов найденных ранее интерсепторами
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   */
  protected void doBeforeReadObjectsByIds( ISkidList aSkids, IListEdit<IDtoObject> aObjs ) {
    // nop
  }

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aSkids {@link ISkidList} список идентификаторов объектов
   * @param aObjs {@link IListEdit}&lt;{link IDtoObject}&gt; список объектов найденных ранее службой и/или
   *          интерсепторами
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendObjectsSingleton#readObjects(IStringList)}
   */
  protected void doAfterReadObjectsByIds( ISkidList aSkids, IListEdit<IDtoObject> aObjs ) {
    // nop
  }

  /**
   * Вызывается ДО выполнения метода
   * {@link IS5BackendObjectsSingleton#writeObjects(IS5FrontendRear, ISkidList, IList, boolean)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aRemovedObjs {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link IDtoObject}&gt;&gt; карта
   *          удаляемых объектов из базы данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список удаляемых объектов класса.
   * @param aUpdatedObjs
   *          {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link Pair}&lt;{@link IDtoObject},{@link IDtoObject}&gt;&gt;&gt;
   *          карта объектов обновляемых в базе данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список пар: {@link Pair#left()} - старое состояние , {@link Pair#right()} - новое состояние.
   * @param aCreatedObjs {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link IDtoObject}&gt;&gt; карта объектов
   *          создаваемых в базе данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список создаваемых объектов класса.
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendObjectsSingleton#writeObjects(IS5FrontendRear, ISkidList, IList, boolean)}
   */
  protected void doBeforeWriteObjects( //
      IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs ) {
    // nop
  }

  /**
   * Вызывается ПОСЛЕ {@link IS5BackendObjectsSingleton#writeObjects(IS5FrontendRear, ISkidList, IList, boolean)}, но до
   * завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aRemovedObjs {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link IDtoObject}&gt;&gt; карта
   *          удаляемых объектов из базы данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список удаляемых объектов класса.
   * @param aUpdatedObjs
   *          {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link Pair}&lt;{@link IDtoObject},{@link IDtoObject}&gt;&gt;&gt;
   *          карта объектов обновляемых в базе данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список пар: {@link Pair#left()} - старое состояние , {@link Pair#right()} - новое состояние.
   * @param aCreatedObjs {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link IDtoObject}&gt;&gt; карта объектов
   *          создаваемых в базе данных.<br>
   *          Ключ: Описание класса;<br>
   *          Значение: Список создаваемых объектов класса.
   * @throws TsIllegalStateRtException отменить изменения сделанные методом
   *           {@link IS5BackendObjectsSingleton#writeObjects(IS5FrontendRear, ISkidList, IList, boolean)} (откат
   *           транзакции)
   */
  protected void doAfterWriteObjects( //
      IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Проводит исправление хранения последовательностей значений указанных данных
   *
   * @param aFragmentInfos {@link IList}&lt;{@link ISequenceFragmentInfo}&gt; список описаний дефрагментации данных
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void repairSequences( IList<ISequenceFragmentInfo> aFragmentInfos ) {
    TsNullArgumentRtException.checkNull( aFragmentInfos );
    // Список восстанавливаемых данных (избыточный, нет точной информации где была ошибка)
    GwidList ids = new GwidList();
    ISequenceFragmentInfo fragmentInfo = aFragmentInfos.get( 0 );
    ids.add( fragmentInfo.gwid() );
    // Определение интервала дефрагментации
    ITimeInterval interval = fragmentInfo.interval();
    long startTime = interval.startTime();
    long endTime = interval.endTime();
    for( int index = 1, n = aFragmentInfos.size(); index < n; index++ ) {
      fragmentInfo = aFragmentInfos.get( index );
      interval = fragmentInfo.interval();
      ids.add( fragmentInfo.gwid() );
      if( startTime > interval.startTime() ) {
        startTime = interval.startTime();
      }
      if( endTime < interval.endTime() ) {
        endTime = interval.endTime();
      }
    }
    long currTime = System.currentTimeMillis();
    // Опции конфигурации
    IOptionSetEdit config = new OptionSet( configuration() );
    // IS5SequenceValidationOptions.INTERVAL.set( vo, new TimeInterval( startTime, endTime ) );
    IS5SequenceValidationOptions.INTERVAL.setValue( config, avValobj( new TimeInterval( startTime, currTime ) ) );
    IS5SequenceValidationOptions.REPAIR.setValue( config,
        avValobj( IS5SequenceValidationOptions.REPAIR.getValue( config ) ) );
    IS5SequenceValidationOptions.GWIDS.setValue( config, avValobj( ids ) );
    // Запуск операции восстановления
    validation( MSG_AUTO_VALIDATION_AUTHOR, config );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы. Таймеры
  //
  /**
   * Обновление таймеров по календарю
   */
  private void updateUnionTimers() {
    if( OP_BACKEND_DATA_WRITE_DISABLE.getValue( backend().initialConfig().impl().params() ).asBool() ) {
      // Запрет записи хранимых данных
      return;
    }
    IListEdit<Timer> oldTimers = new ElemArrayList<>( unionTimers );
    // Опции службы
    IOptionSet config = configuration();
    // Текущие календари конфигурации
    S5ScheduleExpressionList calendars = IS5SequenceAddonConfig.UNION_CALENDARS.getValue( config ).asValobj();
    // Удаление календарей которые больше не используется
    for( Timer timer : oldTimers ) {
      ScheduleExpression schedule = timer.getSchedule();
      try {
        if( !calendars.hasElem( new S5ScheduleExpression( schedule ) ) ) {
          timer.cancel();
          unionTimers.remove( timer );
          logger().info( MSG_CANCEL_UNION_TIMER, schedule );
          continue;
        }
      }
      catch( Exception e ) {
        logger().error( ERR_CANCEL_UNION_TIMER, schedule, cause( e ) );
      }
    }
    // Проверка текущих таймеров и создание если они неопределены
    for( int index = 0, n = calendars.size(); index < n; index++ ) {
      IScheduleExpression schedule = calendars.get( index );
      Timer newTimer = null;
      for( Timer timer : unionTimers ) {
        if( schedule.equals( new S5ScheduleExpression( timer.getSchedule() ) ) ) {
          // Таймер найден
          newTimer = timer;
          break;
        }
      }
      if( newTimer != null ) {
        // Таймер уже существует
        continue;
      }
      try {
        TimerConfig tc = new TimerConfig(
            String.format( "UnionTimer[%s]", S5ScheduleExpressionKeeper.KEEPER.ent2str( schedule ) ), false ); //$NON-NLS-1$
        newTimer = timerService.createCalendarTimer( (ScheduleExpression)schedule, tc );
        unionTimers.add( newTimer );
        logger().info( MSG_CREATE_UNION_TIMER, schedule );
      }
      catch( Exception e ) {
        // Ошибка создания таймера задачи объединения
        logger().error( ERR_CREATE_UNION_TIMER, schedule, cause( e ) );
      }
    }
  }
}
