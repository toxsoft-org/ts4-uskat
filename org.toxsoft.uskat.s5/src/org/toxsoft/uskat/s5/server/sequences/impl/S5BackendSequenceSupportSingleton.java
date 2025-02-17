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
import static org.toxsoft.uskat.s5.server.sequences.maintenance.S5DatabaseConfig.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.S5SequenceConfig.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.S5SequencePartitionConfig.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.S5SequenceUnionConfig.*;
import static org.toxsoft.uskat.s5.server.sequences.maintenance.S5SequenceValidationConfig.*;
import static org.toxsoft.uskat.s5.utils.platform.S5ServerPlatformUtils.*;

import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import javax.annotation.*;
import javax.ejb.*;
import javax.ejb.Timer;
import javax.enterprise.concurrent.*;
import javax.persistence.*;
import javax.sql.*;

import org.jboss.ejb3.annotation.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.idgen.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.coll.synch.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.s5.common.sysdescr.*;
import org.toxsoft.uskat.s5.legacy.*;
import org.toxsoft.uskat.s5.server.backend.*;
import org.toxsoft.uskat.s5.server.backend.impl.*;
import org.toxsoft.uskat.s5.server.backend.supports.objects.*;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.*;
import org.toxsoft.uskat.s5.server.frontend.*;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.maintenance.*;
import org.toxsoft.uskat.s5.server.sequences.reader.*;
import org.toxsoft.uskat.s5.server.sequences.writer.*;
import org.toxsoft.uskat.s5.server.singletons.*;
import org.toxsoft.uskat.s5.server.statistics.*;
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
   * Таймеры расписания задачи обработки разделов таблиц значений хранимых данных
   */
  private IListEdit<Timer> partitionTimers = new ElemArrayList<>();

  /**
   * Исполнитель потоков обработки разделов таблиц
   */
  private ManagedExecutorService partitionExecutor;

  /**
   * Последний запущенный поток обработки разделов таблиц. null: неопределен
   */
  private S5SequencePartitionThread partitionThread;

  /**
   * Таймер обновления статистики
   */
  private Timer dbmsStatisticsTimer;

  /**
   * Писатель статитистики объекта {@link ISkServerHistorable}. null: нет соединения
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
   * Журнал обработки разделов таблиц
   */
  private ILogger partitionLogger = getLogger( LOG_PARTITION_ID );

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
  protected IStringList doConfigurationPaths() {
    IStringListEdit retValue = new StringArrayList();
    retValue.addAll( ALL_DATABASE_OPDEFS.keys() );
    retValue.addAll( ALL_SEQUENCES_OPDEFS.keys() );
    retValue.addAll( ALL_UNION_OPDEFS.keys() );
    retValue.addAll( ALL_VALIDATION_OPDEFS.keys() );
    retValue.addAll( ALL_PARTITION_OPDEFS.keys() );
    return retValue;
  }

  @Override
  protected IOptionSet doCreateConfiguration() {
    return super.doCreateConfiguration();
  }

  @Override
  protected void onConfigChanged( IOptionSet aPrevConfig, IOptionSet aNewConfig ) {
    S5ScheduleExpressionList prevUnionCalendars = UNION_CALENDARS.getValue( aPrevConfig ).asValobj();
    S5ScheduleExpressionList newUnionCalendars = UNION_CALENDARS.getValue( aNewConfig ).asValobj();
    if( !newUnionCalendars.equals( prevUnionCalendars ) ) {
      // Изменение календарей дефрагментации
      updateUnionTimers();
    }
    S5ScheduleExpressionList prevRemoveCalendars = PARTITION_CALENDARS.getValue( aPrevConfig ).asValobj();
    S5ScheduleExpressionList newRemoveCalendars = PARTITION_CALENDARS.getValue( aNewConfig ).asValobj();
    if( !newRemoveCalendars.equals( prevRemoveCalendars ) ) {
      // Изменение календарей дефрагментации
      updatePartitionTimers();
    }
  }

  @Override
  public void saveConfiguration( IOptionSet aConfiguration ) {
    TsNullArgumentRtException.checkNull( aConfiguration );
    super.saveConfiguration( aConfiguration );
    sequenceWriter.setConfiguration( aConfiguration );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendSupportSingleton
  //
  @Override
  protected void doInitSupport() {
    try( Connection dbConnection = dataSource.getConnection() ) {
      DatabaseMetaData metaData = dbConnection.getMetaData();
      String databaseProductName = metaData.getDatabaseProductName();
      ES5DatabaseEngine databaseType = ES5DatabaseEngine.findById( databaseProductName );
      setConfigurationConstant( S5DatabaseConfig.DATABASE_ENGINE, avValobj( databaseType ) );
    }
    catch( SQLException e ) {
      throw new TsInternalErrorRtException( e );
    }

    // Поиск исполнителя потоков чтения блоков
    readExecutor = S5ServiceSingletonUtils.lookupExecutor( READ_EXECUTOR_JNDI );

    // Поиск исполнителя потоков объединения блоков
    unionExecutor = S5ServiceSingletonUtils.lookupExecutor( UNION_EXECUTOR_JNDI );
    // Запуск потока дефрагментации
    uniterThread = new S5SequenceUniterThread( getBusinessObject(), MSG_UNION_AUTHOR_SCHEDULE, uniterLogger );
    unionExecutor.execute( uniterThread );

    // Поиск исполнителя потоков удаления блоков
    partitionExecutor = S5ServiceSingletonUtils.lookupExecutor( PARTITION_EXECUTOR_JNDI );
    // Запуск потока удаления блоков
    partitionThread =
        new S5SequencePartitionThread( getBusinessObject(), MSG_PARTITION_AUTHOR_SCHEDULE, partitionLogger );
    partitionExecutor.execute( partitionThread );

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
    sequenceWriter = new S5SequenceLastBlockWriter<>( id(), backend(), factory(), configuration() );
    // Инициализация таймера запуска задачи дефрагментации значений
    updateUnionTimers();
    // Инициализация таймера запуска задачи обработки разделов таблиц
    updatePartitionTimers();
    // Формирование таймера обновления статистики
    long statisticsUpdateInterval = EStatisticInterval.SECOND.milli();
    TimerConfig tc = new TimerConfig( "StatisticsTimer", false ); //$NON-NLS-1$
    dbmsStatisticsTimer = timerService.createIntervalTimer( statisticsUpdateInterval, statisticsUpdateInterval, tc );
  }

  @Override
  public void doJob() {
    // Фоновая задача писателя
    sequenceWriter.doJob();
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
    partitionThread.close();
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
        // S5PlatformInfo pi = getPlatformInfo();
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

      for( int index = 0, n = unionTimers.size(); index < n; index++ ) {
        Timer timer = unionTimers.get( index );
        if( timer.equals( aTimer ) ) {
          ES5ServerMode serverMode = serverMode();
          // Проверка возможности выполнения дефрагментации при текущей загрузке системы
          if( serverMode != ES5ServerMode.WORKING ) {
            // Загруженность системы не позволяет провести дефрагментацию значений
            if( serverMode != ES5ServerMode.STARTING ) {
              Double la = Double.valueOf( loadAverage() );
              uniterLogger.warning( ERR_UNION_DISABLE_BY_LOAD_AVERAGE, id(), serverMode(), la );
            }
            break;
          }
          // Запуск потока дефрагментации
          if( !uniterThread.tryStart() ) {
            // Запрет дефрагментации значений по календарю (незавершен предыдущий процесс)
            uniterLogger.warning( ERR_UNION_DISABLE_BY_PREV_UNITER, id() );
          }
          break;
        }
      }
      for( int index = 0, n = partitionTimers.size(); index < n; index++ ) {
        Timer timer = partitionTimers.get( index );
        if( timer.equals( aTimer ) ) {
          if( serverMode() != ES5ServerMode.WORKING ) {
            // Текущий режим запрещает проводить операцию
            partitionLogger.warning( ERR_PARTITION_DISABLE_BY_LOAD_AVERAGE, id(), serverMode(),
                Double.valueOf( loadAverage() ) );
            break;
          }
          // System.err.println( "doTimerEventHandle" );
          // Запуск потока обработки разделов таблиц
          if( !partitionThread.tryStart() ) {
            // Запрет обработки таблиц по календарю (незавершен предыдущий процесс)
            partitionLogger.warning( ERR_PARTITION_DISABLE_BY_PREV, id() );
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
    return ISkServerHistorable.CLASS_ID;
  }

  @Override
  protected void doAfterSetSharedConnection( ISkConnection aConnection ) {
    ISkCoreApi coreApi = aConnection.coreApi();
    ISkObjectService objectService = coreApi.objService();
    ISkLinkService linkService = coreApi.linkService();
    // Идентификатор узла
    Skid nodeId = nodeId();
    // Полный (с именем узла) идентификатор backend
    Skid backendId = backendId();
    // Создание/обновление бекенда как объекта системы
    objectService.defineObject( new DtoObject( backendId, IOptionSet.NULL, IStringMap.EMPTY ) );
    linkService.defineLink( backendId, ISkServerBackend.LNKID_NODE, ISkidList.EMPTY, new SkidList( nodeId ) );
    // Создание писателя статистики узла сервера
    statisticWriter = new S5StatisticWriter( aConnection, backendId, STAT_HISTORABLE_BACKEND_PARAMS );
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
  public boolean writeSequences( IList<S> aSequences ) {
    TsNullArgumentRtException.checkNull( aSequences );
    if( OP_BACKEND_DATA_WRITE_DISABLE.getValue( backend().initialConfig().impl().params() ).asBool() ) {
      // Запрет записи хранимых данных
      return false;
    }
    // Установка фабрики формирования последовательности (подготовка к возможному редактированию)
    for( S sequence : aSequences ) {
      ((S5Sequence<V>)sequence).setFactory( factory );
    }
    // Формирование статистики
    S5StatisticWriter stat = statisticWriter;
    try {
      // Проверка возможности выполнения записи при текущей загрузке системы
      ES5ServerMode serverMode = serverMode();
      switch( serverMode ) {
        case STARTING:
        case WORKING:
        case BOOSTED:
          break;
        case OVERLOADED:
        case SHUTDOWNING:
        case OFF:
          // Текущий режим запрещает запись
          writeLogger.error( ERR_WRITE_DISABLE_BY_LOAD_AVERAGE, id(), serverMode(), Double.valueOf( loadAverage() ) );
          return false;
        default:
          break;
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
      return true;
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
      // Запуск процесса регламента
      IS5SequenceUnionStat unionStat = sequenceWriter.union( aAuthor, aArgs );
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
      // Информация о данных в проведенной дефрагментации
      IList<IS5SequenceFragmentInfo> fragmentInfos = unionStat.infoes();
      // Обработка ошибок целостности
      if( unionStat.errorCount() == 0 || fragmentInfos.size() == 0 ) {
        return unionStat;
      }
      // Признак требование проводить автоматическое восстановление целостности
      final boolean autoRepair = VALIDATION_AUTO_REPAIR.getValue( configuration() ).asBool();
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
  public IS5SequencePartitionStat partition( String aAuthor, IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aAuthor, aArgs );
    if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      // Запуск задачи обработки разделов
      logger().debug( MSG_SINGLETON_PARTITION_TASK_START );
    }
    try {
      // Запуск процесса регламента
      IS5SequencePartitionStat partitionStat = sequenceWriter.partition( aAuthor, aArgs );
      // Формирование статистики
      S5StatisticWriter stat = statisticWriter;
      if( stat != null ) {
        stat.onEvent( STAT_HISTORABLE_BACKEND_PARTITIONS_TASKS_COUNT, AV_1 );
        stat.onEvent( STAT_HISTORABLE_BACKEND_PARTITIONS_LOOKUP_COUNT, avInt( partitionStat.lookupCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_PARTITIONS_THREAD_COUNT, avInt( partitionStat.operations().size() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_PARTITIONS_ADDED_COUNT, avInt( partitionStat.addedCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_PARTITIONS_REMOVED_COUNT,
            avInt( partitionStat.removedPartitionCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_PARTITIONS_BLOCKS_REMOVED_COUNT,
            avInt( partitionStat.removedBlockCount() ) );
        stat.onEvent( STAT_HISTORABLE_BACKEND_PARTITIONS_ERROR_COUNT, avInt( partitionStat.errorCount() ) );
      }
      return partitionStat;
    }
    finally {
      // Завершение задачи дефрагментации
      logger().debug( MSG_SINGLETON_PARTITION_TASK_FINISH );
    }
  }

  @TransactionTimeout( value = SEQUENCE_VALIDATION_TRANSACTION_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
  @Lock( LockType.READ )
  @Override
  public IS5SequenceValidationStat validation( String aAuthor, IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aAuthor, aArgs );
    logger().info( MSG_VALIDATION_TASK_START, aAuthor );
    // Запуск процесса проверки
    return sequenceWriter.validation( aAuthor, aArgs );
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
    IOptionSetEdit configuration = new OptionSet( configuration() );
    VALIDATION_INTERVAL.setValue( configuration, avValobj( new TimeInterval( startTime, currTime ) ) );
    VALIDATION_REPAIR.setValue( configuration, avValobj( VALIDATION_REPAIR.getValue( configuration ) ) );
    VALIDATION_GWIDS.setValue( configuration, avValobj( ids ) );
    // Запуск операции восстановления
    validation( MSG_AUTO_VALIDATION_AUTHOR, configuration );
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
    // Текущие календари конфигурации
    S5ScheduleExpressionList calendars = UNION_CALENDARS.getValue( configuration() ).asValobj();
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
   * Обновление таймеров обработки разделов таблиц по календарю
   */
  private void updatePartitionTimers() {
    if( OP_BACKEND_DATA_WRITE_DISABLE.getValue( backend().initialConfig().impl().params() ).asBool() ) {
      // Запрет записи хранимых данных
      return;
    }
    IListEdit<Timer> oldTimers = new ElemArrayList<>( partitionTimers );
    // Текущие календари конфигурации
    S5ScheduleExpressionList calendars = PARTITION_CALENDARS.getValue( configuration() ).asValobj();
    // Удаление календарей которые больше не используется
    for( Timer timer : oldTimers ) {
      ScheduleExpression schedule = timer.getSchedule();
      try {
        if( !calendars.hasElem( new S5ScheduleExpression( schedule ) ) ) {
          timer.cancel();
          partitionTimers.remove( timer );
          logger().info( MSG_CANCEL_PARTITION_TIMER, schedule );
          continue;
        }
      }
      catch( Exception e ) {
        logger().error( ERR_CANCEL_PARTITION_TIMER, schedule, cause( e ) );
      }
    }
    // Проверка текущих таймеров и создание если они неопределены
    for( int index = 0, n = calendars.size(); index < n; index++ ) {
      IScheduleExpression schedule = calendars.get( index );
      Timer newTimer = null;
      for( Timer timer : partitionTimers ) {
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
        partitionTimers.add( newTimer );
        logger().info( MSG_CREATE_PARTITION_TIMER, schedule );
      }
      catch( Exception e ) {
        // Ошибка создания таймера задачи объединения
        logger().error( ERR_CREATE_PARTITION_TIMER, schedule, cause( e ) );
      }
    }
  }
}
