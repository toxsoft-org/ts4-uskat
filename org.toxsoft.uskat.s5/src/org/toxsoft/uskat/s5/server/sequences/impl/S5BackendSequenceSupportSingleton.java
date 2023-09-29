package org.toxsoft.uskat.s5.server.sequences.impl;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.bricks.time.EQueryIntervalType.*;
import static org.toxsoft.core.tslib.bricks.time.impl.TimeUtils.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceSQL.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceUtils.*;

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
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.idgen.IStridGenerator;
import org.toxsoft.core.tslib.bricks.strid.idgen.UuidStridGenerator;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
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
import org.toxsoft.uskat.s5.legacy.S5LongArrayList;
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
  private IS5SequenceFactory<V> factory;

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
   * Таймеры расписания задачи дефрагментации блоков
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
   * Таймеры расписания задачи удаления блоков
   */
  private IListEdit<Timer> removeTimers = new ElemArrayList<>();

  /**
   * Исполнитель потоков удаления
   */
  private ManagedExecutorService removeExecutor;

  /**
   * Последний запущенный поток удаления блоков последовательностей. null: неопределен
   */
  private S5SequenceRemoveThread removeThread;

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
   * Журнал удаления значений последовательностей
   */
  private ILogger removeLogger = getLogger( LOG_REMOVER_ID );

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
    IOptionSetEdit retValue = new OptionSet();
    // Неизменяемые параметры конфигурации службы
    retValue.setValue( OP_BACKEND_DB_SCHEME_NAME,
        OP_BACKEND_DB_SCHEME_NAME.getValue( backend().initialConfig().impl().params() ) );
    return retValue;
  }

  @Override
  protected void onConfigChanged( IOptionSet aPrevConfig, IOptionSet aNewConfig ) {
    S5ScheduleExpressionList prevUnionCalendars =
        IS5SequenceAddonConfig.UNION_CALENDARS.getValue( aPrevConfig ).asValobj();
    S5ScheduleExpressionList newUnionCalendars =
        IS5SequenceAddonConfig.UNION_CALENDARS.getValue( aNewConfig ).asValobj();
    if( !newUnionCalendars.equals( prevUnionCalendars ) ) {
      // Изменение календарей дефрагментации
      updateUnionTimers();
    }
    S5ScheduleExpressionList prevRemoveCalendars =
        IS5SequenceAddonConfig.REMOVE_CALENDARS.getValue( aPrevConfig ).asValobj();
    S5ScheduleExpressionList newRemoveCalendars =
        IS5SequenceAddonConfig.REMOVE_CALENDARS.getValue( aNewConfig ).asValobj();
    if( !newRemoveCalendars.equals( prevRemoveCalendars ) ) {
      // Изменение календарей дефрагментации
      updateRemoveTimers();
    }
  }

  @Override
  public void saveConfiguration( IOptionSet aConfiguration ) {
    TsNullArgumentRtException.checkNull( aConfiguration );
    IOptionSetEdit factConfiguration = new OptionSet( aConfiguration );
    // Неизменяемые параметры конфигурации службы
    factConfiguration.setValue( OP_BACKEND_DB_SCHEME_NAME,
        OP_BACKEND_DB_SCHEME_NAME.getValue( backend().initialConfig().impl().params() ) );
    super.saveConfiguration( factConfiguration );
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

    // Поиск исполнителя потоков удаления блоков
    removeExecutor = S5ServiceSingletonUtils.lookupExecutor( REMOVE_EXECUTOR_JNDI );
    // Запуск потока удаления блоков
    removeThread = new S5SequenceRemoveThread( getBusinessObject(), MSG_REMOVE_AUTHOR_SCHEDULE, removeLogger );
    removeExecutor.execute( removeThread );

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
    // Инициализация таймера запуска задачи дефрагментации значений
    updateUnionTimers();
    // Инициализация таймера запуска задачи удаления данных
    updateRemoveTimers();
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
    // Завершение потока удаления значений
    removeThread.close();
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
      // Фоновая задача писателя
      sequenceWriter.doJob();
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

      // Текущая загрузка системы
      double loadAverage = S5ServerPlatformUtils.loadAverage();

      // Получаем бизнес интерфейс синглетона, чтобы вызов doUnionTask выполнялся вне транзакции
      for( int index = 0, n = unionTimers.size(); index < n; index++ ) {
        Timer timer = unionTimers.get( index );
        if( timer.equals( aTimer ) ) {
          // Опции службы
          IOptionSet config = configuration();
          // Максимальная загрузка системы при которой возможна дефрагментация
          final double loadAverageMax = IS5SequenceAddonConfig.UNION_LOAD_AVERAGE_MAX.getValue( config ).asDouble();
          // Проверка возможности выполнения дефрагментации при текущей загрузке системы
          if( loadAverage > loadAverageMax ) {
            // Загруженность системы не позволяет провести дефрагментацию значений
            Double la = Double.valueOf( loadAverage );
            uniterLogger.warning( ERR_UNION_DISABLE_BY_LOAD_AVERAGE, id(), la );
            break;
          }
          // Запуск потока дефрагментации
          if( !uniterThread.tryStart() ) {
            // Запрет дефрагментации значений по календарю (незавершен предыдущий процесс)
            uniterLogger.warning( ERR_UNION_DISABLE_BY_PREV_UNITER, id() );
            break;
          }
          break;
        }
      }
      // Получаем бизнес интерфейс синглетона, чтобы вызов doRemoveTask выполнялся вне транзакции
      for( int index = 0, n = removeTimers.size(); index < n; index++ ) {
        Timer timer = removeTimers.get( index );
        if( timer.equals( aTimer ) ) {
          // Опции службы
          IOptionSet config = configuration();
          // Максимальная загрузка системы при которой возможно удаление данных
          final double loadAverageMax = IS5SequenceAddonConfig.REMOVE_LOAD_AVERAGE_MAX.getValue( config ).asDouble();
          if( loadAverage > loadAverageMax ) {
            // Загруженность системы не позволяет провести удаление значений
            Double la = Double.valueOf( loadAverage );
            removeLogger.warning( ERR_REMOVE_DISABLE_BY_LOAD_AVERAGE, id(), la );
            break;
          }
          // Запуск потока дефрагментации
          if( !removeThread.tryStart() ) {
            // Запрет дефрагментации значений по календарю (незавершен предыдущий процесс)
            removeLogger.warning( ERR_REMOVE_DISABLE_BY_PREV_UNITER, id() );
            break;
          }
          break;
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
    ILongListEdit retValue = new S5LongArrayList( aGwids.size() );
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
    ILongListEdit retValue = new S5LongArrayList( aGwid.size() );
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
  public IMap<Gwid, S> readSequences( IGwidList aGwids, IQueryInterval aInterval, long aMaxExecutionTimeout ) {
    return readSequences( IS5FrontendRear.NULL, uuidGenerator.nextId(), aGwids, aInterval, aMaxExecutionTimeout );
  }

  @Lock( LockType.READ )
  @Override
  public IMap<Gwid, S> readSequences( IS5FrontendRear aFrontend, String aQueryId, IGwidList aGwids,
      IQueryInterval aInterval, long aTimeout ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aQueryId, aGwids, aInterval );
    if( aGwids.size() == 0 ) {
      // Частный случай, ничего не запрашивается
      return IMap.EMPTY;
    }
    if( readQueries.hasKey( aQueryId ) ) {
      // Запрос уже выполняется
      throw new TsIllegalArgumentRtException( ERR_QUERY_IS_ALREADY_EXECUTE, aQueryId );
    }
    long traceStartTime = System.currentTimeMillis();
    int count = aGwids.size();
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
    }
    // Соединение с dmbs
    try( Connection dbConnection = dataSource.getConnection() ) {
      // Выполняемый запрос
      IS5SequenceReadQuery query =
          new S5SequenceReadQuery( aFrontend, aQueryId, aInterval, factory, dbConnection, aTimeout );
      readQueries.put( aQueryId, query );
      logger().info( "readSequences(...): put query. aQueryId = %s", aQueryId ); //$NON-NLS-1$
      try {
        // Запрос последовтельностей значений
        IMap<Gwid, S> readSequences = readSequences( query, new GwidList( readingGwids.toArray( new Gwid[0] ) ) );
        // Формирование окончательного результата
        IMapEdit<Gwid, S> retValue = new ElemMap<>();
        for( Gwid gwid : aGwids ) {
          S sequence = readSequences.findByKey( gwid );
          if( sequence == null ) {
            // Нет данных
            IQueryInterval interval = new QueryInterval( CSCE, aInterval.startTime(), aInterval.endTime() );
            sequence = (S)factory.createSequence( gwid, interval, IList.EMPTY );
          }
          retValue.put( gwid, sequence );
        }
        // Журналирование
        Long traceTimeout = Long.valueOf( System.currentTimeMillis() - traceStartTime );
        logger().debug( MSG_READ_SEQUENCE_TIME, Integer.valueOf( count ), traceTimeout );
        return retValue;
      }
      finally {
        readQueries.removeByKey( aQueryId );
        logger().info( "readSequences(...): query is completed. aQueryId = %s", aQueryId ); //$NON-NLS-1$
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
      // Max load average (загрузка системы) при котором возможно проведение записи значений последовательностей
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
        stat.onEvent( STAT_HISTORABLE_BACKEND_DEFRAGMENT_LOOKUP_COUNT, avInt( unionStat.lookupCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_DEFRAGMENT_THREAD_COUNT, avInt( unionStat.infoes().size() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_DEFRAGMENT_VALUE_COUNT, avInt( unionStat.valueCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_DEFRAGMENT_MERGED_COUNT, avInt( unionStat.dbmsMergedCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_DEFRAGMENT_REMOVED_COUNT, avInt( unionStat.dbmsRemovedCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_DEFRAGMENT_ERROR_COUNT, avInt( unionStat.errorCount() ) );
      }
      // Журнал
      if( unionStat.infoes().size() > 0 || unionStat.queueSize() > 0 ) {
        // Список описаний данных в запросе на дефрагментацию
        IList<IS5SequenceFragmentInfo> fragmentInfoes = unionStat.infoes();
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
      IList<IS5SequenceFragmentInfo> fragmentInfos = unionStat.infoes();
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

  @TransactionTimeout( value = SEQUENCE_UNION_TRANSACTION_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
  @Lock( LockType.READ )
  @Override
  public IS5SequenceRemoveStat remove( String aAuthor, IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aAuthor, aArgs );
    IAtomicValue dbScheme = OP_BACKEND_DB_SCHEME_NAME.getValue( aArgs );
    if( !dbScheme.isAssigned() ) {
      // Не определено имя схемы базы данных в реализации сервера
      throw new TsIllegalArgumentRtException( ERR_DB_SCHEME_NOT_DEFINED );
    }
    if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      // Запуск задачи удаления
      logger().debug( MSG_SINGLETON_REMOVE_TASK_START );
    }
    try {
      // Время запуска операции
      long traceStartTime = System.currentTimeMillis();
      // Запуск процесса регламента
      IS5SequenceRemoveStat removeStat = sequenceWriter.remove( aArgs );
      // Формирование статистики
      S5StatisticWriter stat = statisticWriter;
      if( stat != null ) {
        stat.onEvent( STAT_HISTORABLE_BACKEND_REMOVE_COUNT, AV_1 );
        stat.onEvent( STAT_HISTORABLE_BACKEND_REMOVE_LOOKUP_COUNT, avInt( removeStat.lookupCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_REMOVE_THREAD_COUNT, avInt( removeStat.infoes().size() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_REMOVE_REMOVED_COUNT, avInt( removeStat.removedCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_REMOVE_ERROR_COUNT, avInt( removeStat.errorCount() ) );
      }
      // Журнал
      if( removeStat.infoes().size() > 0 || removeStat.queueSize() > 0 ) {
        // Список описаний данных в запросе на удаление
        IList<IS5SequenceRemoveInfo> removeInfoes = removeStat.infoes();
        // Вывод статистики
        Long d = Long.valueOf( (System.currentTimeMillis() - traceStartTime) / 1000 );
        Integer tc = Integer.valueOf( removeInfoes.size() );
        String threaded = TsLibUtils.EMPTY_STRING;
        if( removeInfoes.size() > 0 ) {
          threaded = "[" + removeInfoes.get( 0 ).toString(); //$NON-NLS-1$
          if( removeInfoes.size() > 1 ) {
            threaded += ", ..."; //$NON-NLS-1$
          }
          threaded += "]"; //$NON-NLS-1$
        }
        Integer lc = Integer.valueOf( removeStat.lookupCount() );
        Integer rc = Integer.valueOf( removeStat.removedCount() );
        Integer ec = Integer.valueOf( removeStat.errorCount() );
        Integer qs = Integer.valueOf( removeStat.queueSize() );
        removeLogger.info( MSG_REMOVE_TASK_FINISH, id(), aAuthor, lc, tc, threaded, rc, ec, qs, d );
      }
      // Информация об проведенной операции удаления значений данных
      IList<IS5SequenceRemoveInfo> removeInfos = removeStat.infoes();
      // Обработка ошибок целостности
      if( removeStat.errorCount() == 0 || removeInfos.size() == 0 ) {
        return removeStat;
      }
      return removeStat;
    }
    finally {
      // Завершение задачи дефрагментации
      logger().debug( MSG_SINGLETON_REMOVE_TASK_FINISH );
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
    // Создание/обновление бекенда как объекта системы
    objectService.defineObject( new DtoObject( backendId, IOptionSet.NULL, IStringMap.EMPTY ) );
    linkService.defineLink( backendId, IS5ClassBackend.LNKID_NODE, ISkidList.EMPTY, new SkidList( nodeId ) );

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
   * @return {@link IS5SequenceFactory} фабрика формирования последовательностей
   */
  @SuppressWarnings( "unchecked" )
  protected final <T extends IS5SequenceFactory<V>> T factory() {
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
   * @return {@link IS5SequenceFactory} фабрика формирования последовательностей
   */
  protected abstract IS5SequenceFactory<V> doCreateFactory();

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
   * Читает последовательности значений
   *
   * @param aQuery {@link IS5SequenceReadQuery} запрос чтения хранимых данных
   * @param aGwids {@link IGwidList} список идентификаторов данных.
   * @return {@link IMap}&lt;{@link Gwid},S&lt;V&gt;&gt; карта прочитанных последовательностей.<br>
   *         Ключ: идентификатор данного<br>
   *         Значение: последовательность значений данного.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  private IMap<Gwid, S> readSequences( IS5SequenceReadQuery aQuery, IGwidList aGwids ) {
    TsNullArgumentRtException.checkNulls( aQuery, aGwids );
    String table = (aGwids.size() > 0 ? gwidsToString( aGwids, 3 ) : TsLibUtils.EMPTY_STRING);
    String infoStr = String.format( MSG_INFO, table, Integer.valueOf( aGwids.size() ) );
    // Интервал запроса
    IQueryInterval interval = aQuery.interval();
    long traceTimestamp = System.currentTimeMillis();
    long traceReadTimeout = 0;
    long traceCreateTimeout = 0;
    try {
      // Установка потока выполняющего запрос
      aQuery.setThread( Thread.currentThread() );
      // Чтение блоков
      IMap<Gwid, IList<IS5SequenceBlock<V>>> readBlocks = readBlocks( aQuery, aGwids );
      traceReadTimeout = System.currentTimeMillis() - traceTimestamp;
      traceTimestamp = System.currentTimeMillis();
      // Результат
      IMapEdit<Gwid, S> retValue = new ElemMap<>();
      // Создание последовательностей
      for( int index = 0, n = readBlocks.keys().size(); index < n; index++ ) {
        Gwid gwid = readBlocks.keys().get( index );
        IList<IS5SequenceBlockEdit<V>> blocks =
            (IList<IS5SequenceBlockEdit<V>>)(Object)readBlocks.values().get( index );
        // Фактический интервал значений (может быть больше запрашиваемого так как могут быть значения ДО и ПОСЛЕ)
        long factStartTime = interval.startTime();
        long factEndTime = interval.endTime();
        if( blocks.size() > 0 ) {
          long startTime = blocks.get( 0 ).startTime();
          long endTime = blocks.get( blocks.size() - 1 ).endTime();
          if( startTime < factStartTime ) {
            factStartTime = startTime;
          }
          if( factEndTime < endTime ) {
            factEndTime = endTime;
          }
        }
        IQueryInterval factInterval = new QueryInterval( EQueryIntervalType.CSCE, factStartTime, factEndTime );
        IS5SequenceEdit<V> sequence = factory.createSequence( gwid, factInterval, blocks );
        // 2020-12-07 mvk ???
        // if( interval.equals( factInterval ) == false ) {
        // sequence.setInterval( interval );
        // }
        retValue.put( gwid, (S)sequence );
      }
      traceCreateTimeout = System.currentTimeMillis() - traceTimestamp;
      // Журнал
      if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
        Long ta = Long.valueOf( System.currentTimeMillis() - traceTimestamp );
        Long tr = Long.valueOf( traceReadTimeout );
        Long tc = Long.valueOf( traceCreateTimeout );
        String s = sequencesToString( retValue.values() );
        logger().debug( MSG_READ_SEQUENCES_FINISH, infoStr, interval, ta, tr, tc, s );
      }
      return retValue;
    }
    catch( RuntimeException e ) {
      throw new TsInternalErrorRtException( e, ERR_SEQUENCE_READ_UNEXPECTED, infoStr, interval, cause( e ) );
    }
  }

  /**
   * Проводит исправление хранения последовательностей значений указанных данных
   *
   * @param aFragmentInfos {@link IList}&lt;{@link IS5SequenceFragmentInfo}&gt; список описаний дефрагментации данных
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void repairSequences( IList<IS5SequenceFragmentInfo> aFragmentInfos ) {
    TsNullArgumentRtException.checkNull( aFragmentInfos );
    // Список восстанавливаемых данных (избыточный, нет точной информации где была ошибка)
    GwidList ids = new GwidList();
    IS5SequenceFragmentInfo fragmentInfo = aFragmentInfos.get( 0 );
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
   * Обновление таймеров дефрагментации по календарю
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

  /**
   * Обновление таймеров удаления значений по календарю
   */
  private void updateRemoveTimers() {
    if( OP_BACKEND_DATA_WRITE_DISABLE.getValue( backend().initialConfig().impl().params() ).asBool() ) {
      // Запрет записи хранимых данных
      return;
    }
    IAtomicValue dbScheme = OP_BACKEND_DB_SCHEME_NAME.getValue( backend().initialConfig().impl().params() );
    if( !dbScheme.isAssigned() ) {
      // Не определено имя схемы базы данных в реализации сервера
      logger().error( ERR_DB_SCHEME_NOT_DEFINED );
    }
    IListEdit<Timer> oldTimers = new ElemArrayList<>( removeTimers );
    // Опции службы
    IOptionSet config = configuration();
    // Текущие календари конфигурации
    S5ScheduleExpressionList calendars = IS5SequenceAddonConfig.REMOVE_CALENDARS.getValue( config ).asValobj();
    // Удаление календарей которые больше не используется
    for( Timer timer : oldTimers ) {
      ScheduleExpression schedule = timer.getSchedule();
      try {
        if( !calendars.hasElem( new S5ScheduleExpression( schedule ) ) ) {
          timer.cancel();
          removeTimers.remove( timer );
          logger().info( MSG_CANCEL_REMOVE_TIMER, schedule );
          continue;
        }
      }
      catch( Exception e ) {
        logger().error( ERR_CANCEL_REMOVE_TIMER, schedule, cause( e ) );
      }
    }
    // Проверка текущих таймеров и создание если они неопределены
    for( int index = 0, n = calendars.size(); index < n; index++ ) {
      IScheduleExpression schedule = calendars.get( index );
      Timer newTimer = null;
      for( Timer timer : removeTimers ) {
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
            String.format( "RemoveTimer[%s]", S5ScheduleExpressionKeeper.KEEPER.ent2str( schedule ) ), false ); //$NON-NLS-1$
        newTimer = timerService.createCalendarTimer( (ScheduleExpression)schedule, tc );
        removeTimers.add( newTimer );
        logger().info( MSG_CREATE_REMOVE_TIMER, schedule );
      }
      catch( Exception e ) {
        // Ошибка создания таймера задачи объединения
        logger().error( ERR_CREATE_REMOVE_TIMER, schedule, cause( e ) );
      }
    }
  }
}
