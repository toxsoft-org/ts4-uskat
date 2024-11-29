package org.toxsoft.uskat.s5.cron.supports;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.cron.lib.ISkCronHardConstants.*;
import static org.toxsoft.uskat.s5.cron.supports.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.ejb.Timer;

import org.jboss.ejb3.annotation.TransactionTimeout;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.IntArrayList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.api.objserv.IDtoObject;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoAttrInfo;
import org.toxsoft.uskat.s5.common.sysdescr.ISkSysdescrReader;
import org.toxsoft.uskat.s5.cron.lib.*;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.events.IS5BackendEventSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5ObjectsInterceptor;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.utils.jobs.IS5ServerJob;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

/**
 * Синглетон backend {@link IBaCrone} предоставляемый s5-сервером.
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    // BACKEND_RTDATA_SINGLETON, // уже включено неявным образом
    // BACKEND_COMMANDS_SINGLETON, // уже включено неявным образом
    // BACKEND_EVENTS_SINGLETON, // уже включено неявным образом
    PROJECT_INITIAL_IMPLEMENT_SINGLETON// уже включено неявным образом
// PROJECT_INITIAL_SYSDESCR_SINGLETON
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@ConcurrencyManagement( ConcurrencyManagementType.BEAN )
@Lock( LockType.READ )
public class S5BackendCronSingleton
    extends S5BackendSupportSingleton
    implements IS5BackendCronSingleton, IS5ObjectsInterceptor, IS5ServerJob {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_SCHEDULES_ID = "S5BackendCronSingleton"; //$NON-NLS-1$

  /**
   * Интервал выполнения doJob (мсек)
   */
  private static final long DOJOB_INTERVAL = 1000;

  /**
   * Служба таймера
   */
  @Resource
  private TimerService timerService;

  /**
   * backend управления классами системы (интерсепция объектов системы)
   */
  @EJB
  private IS5BackendSysDescrSingleton sysdescrBackend;

  /**
   * backend управления классами системы (интерсепция объектов системы)
   */
  @EJB
  private IS5BackendObjectsSingleton objectsBackend;

  /**
   * backend событий
   */
  @EJB
  private IS5BackendEventSingleton eventsBackend;

  /**
   * Календарь
   */
  private Calendar calendar = new GregorianCalendar();

  /**
   * Карта таймеров расписаний. <br>
   * Ключ карты: идентификатор объекта класса расписания {@link ISkSchedule#CLASS_ID}; <br>
   * Значение карты: таймер
   */
  private IMapEdit<Skid, Timer> schedulesTimers = new ElemMap<>();

  /**
   * Блокировка доступа к {@link #doJob()}
   */
  private S5Lockable schedulesTimersLock = new S5Lockable();

  /**
   * Тайматут (мсек) ожидания блокировки {@link #schedulesTimersLock}
   */
  // private static final long LOCK_TIMEOUT = 1000;

  static {
    // Регистрация хранителей данных
    // SkSchedulesValobjUtils.registerS5Keepers();
  }

  /**
   * Пустой конструктор.
   */
  public S5BackendCronSingleton() {
    super( BACKEND_SCHEDULES_ID, ISkCronHardConstants.BAINF_CRON.nmName() );
  }

  // ------------------------------------------------------------------------------------
  // S5SingletonBase
  //
  @Override
  protected IOptionSet doCreateConfiguration() {
    return super.doCreateConfiguration();
  }

  @Override
  protected void onConfigChanged( IOptionSet aPrevConfig, IOptionSet aNewConfig ) {
    // nop
  }

  @Override
  protected void doInitSupport() {
    // Инициализация базового класса
    super.doInitSupport();
    // Инициализация таймеров расписаний. Примечание: чтобы избежать рекурсивного обращения к синглетону на этапе его
    // загрузки, инициализация таймеров должна быть проведена до установки перехвата операций над объектами
    initScheduleTimers();
    // Перехват операций над объектами
    IS5ObjectsInterceptor objectsInterceptor = sessionContext().getBusinessObject( IS5ObjectsInterceptor.class );
    objectsBackend.addObjectsInterceptor( objectsInterceptor, 2 );
    // Запуск doJob
    addOwnDoJob( DOJOB_INTERVAL );
  }

  @Override
  protected void doCloseSupport() {
    super.doCloseSupport();
  }

  @Timeout
  @TransactionTimeout( value = TRANSACTION_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
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
      IDtoObject foundSchedule = null;
      lockWrite( schedulesTimersLock );
      try {
        for( Skid scheduleId : schedulesTimers.keys() ) {
          if( schedulesTimers.getByKey( scheduleId ).equals( aTimer ) ) {
            foundSchedule = objectsBackend.findObject( scheduleId );
            break;
          }
        }
      }
      finally {
        unlockWrite( schedulesTimersLock );
      }
      if( foundSchedule == null ) {
        // Событие неизвестного таймера
        logger().error( ERR_UNKNOWN_TIMER_EVENT, aTimer );
        aTimer.cancel();
        return;
      }
      // Проверка календаря на особый случай: одновременное определение дней месяца и недели.
      String daysOfWeek = getAttrStr( foundSchedule, ATRINF_DAYS_OF_WEEK ).replaceAll( SPACE, EMPTY_STRING );
      String daysOfMonth = getAttrStr( foundSchedule, ATRINF_DAYS_OF_MONTH ).replaceAll( SPACE, EMPTY_STRING );
      if( daysOfWeek.indexOf( WILDCARD ) < 0 && daysOfWeek.indexOf( QUSTION ) < 0 ) {
        calendar.setTimeInMillis( System.currentTimeMillis() );
        // Проверка для недели
        String calendarWeekDay = CALENDAR_WEEK_DAYS[calendar.get( Calendar.DAY_OF_WEEK ) - 1];
        IStringList scheduleWeekDays = new StringArrayList( daysOfWeek.split( COMMA ) );
        if( !scheduleWeekDays.hasElem( calendarWeekDay ) ) {
          // В расписании нет текущего дня недели. Правило "И" не пропускает событие
          return;
        }
        // Проверка для месяца
        int calendarMonthDay = calendar.get( Calendar.DAY_OF_MONTH );
        IIntList scheduleMonthDays = strToIntList( daysOfMonth.split( COMMA ) );
        if( !scheduleMonthDays.hasValue( calendarMonthDay ) ) {
          // В расписании нет текущего дня месяца. Правило "И" не пропускает событие
          return;
        }
      }
      // Формирование события
      long createTime = System.currentTimeMillis();
      Gwid eventGwid = Gwid.createEvent( foundSchedule.classId(), foundSchedule.strid(), EVID_ON_SCHEDULED );
      IOptionSetEdit params = new OptionSet();
      SkEvent event = new SkEvent( createTime, eventGwid, params );
      eventsBackend.fireEvents( IS5FrontendRear.NULL, new TimedList<>( event ) );
      // Журнал
      logger().info( MSG_SCHEDULE_TIMER_EVENT_START, foundSchedule );
    }
    finally {
      if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
        // Завершается обработка события таймера
        logger().debug( MSG_TIMER_EVENT_FINISH, aTimer.getInfo() );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendCronSingleton
  //

  // ------------------------------------------------------------------------------------
  // IS5ServerJob
  //
  @Override
  public void doJob() {
    // 2024-05-26 mvk ВАЖНО: нельзя ставить блокировку schedulesTimersLock, в initScheduleTimers будет вызывана
    // в потоке ITsThreadExecutor что приведет к DEADLOCK
    // if( !tryLockWrite( schedulesTimersLock, LOCK_TIMEOUT ) ) {
    // // Ошибка получения блокировки
    // logger().warning( ERR_TRY_LOCK, schedulesTimersLock );
    // return;
    // }
    // Вывод журнала
    logger().debug( MSG_DOJOB );
  }

  // ------------------------------------------------------------------------------------
  // IS5ObjectsInterceptor
  //
  @Override
  public IDtoObject beforeFindObject( Skid aSkid, IDtoObject aObj ) {
    return aObj;
  }

  @Override
  public IDtoObject afterFindObject( Skid aSkid, IDtoObject aObj ) {
    return aObj;
  }

  @Override
  public void beforeReadObjects( IStringList aClassIds, IListEdit<IDtoObject> aObjs ) {
    // nop
  }

  @Override
  public void afterReadObjects( IStringList aClassIds, IListEdit<IDtoObject> aObjs ) {
    // nop
  }

  @Override
  public void beforeReadObjectsByIds( ISkidList aSkids, IListEdit<IDtoObject> aObjs ) {
    // nop
  }

  @Override
  public void afterReadObjectsByIds( ISkidList aSkids, IListEdit<IDtoObject> aObjs ) {
    // nop
  }

  @Override
  public void beforeWriteObjects( IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs ) {
    // nop
  }

  @Override
  public void afterWriteObjects( IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs ) {
    // Проверка удаленных расписаний
    for( ISkClassInfo classInfo1 : aRemovedObjs.keys() ) {
      if( !sysdescrBackend.isAssignableFrom( CLSID_SCHEDULE, classInfo1.id() ) ) {
        // Класс объектов не представляют расписание
        continue;
      }
      for( IDtoObject obj1 : aRemovedObjs.getByKey( classInfo1 ) ) {
        removeScheduleTimer( obj1.skid() );
      }
    }
    // Проверка обновленных расписаний
    for( ISkClassInfo classInfo2 : aUpdatedObjs.keys() ) {
      if( !sysdescrBackend.isAssignableFrom( CLSID_SCHEDULE, classInfo2.id() ) ) {
        // Класс объектов не представляют расписание
        continue;
      }
      for( Pair<IDtoObject, IDtoObject> objPair : aUpdatedObjs.getByKey( classInfo2 ) ) {
        initScheduleTimer( objPair.right() );
      }
    }
    // Проверка созданных расписаний
    for( ISkClassInfo classInfo3 : aCreatedObjs.keys() ) {
      if( !sysdescrBackend.isAssignableFrom( CLSID_SCHEDULE, classInfo3.id() ) ) {
        // Класс объектов не представляют расписание
        continue;
      }
      for( IDtoObject obj2 : aCreatedObjs.getByKey( classInfo3 ) ) {
        initScheduleTimer( obj2 );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Инициаилизация таймеров расписаний
   */
  private void initScheduleTimers() {
    ISkSysdescrReader sysdescrReader = sysdescrBackend.getReader();
    ISkClassInfo scheduleClassInfo = sysdescrReader.getClassInfo( ISkSchedule.CLASS_ID );
    IStridablesList<ISkClassInfo> classIds = scheduleClassInfo.listSubclasses( false, true );

    // Список всех расписаний.
    IList<IDtoObject> schedules = objectsBackend.readObjects( classIds.ids() );
    // Инициализация таймеров
    for( IDtoObject schedule : schedules ) {
      try {
        initScheduleTimer( schedule );
      }
      catch( Throwable e ) {
        logger().error( ERR_START_TIMER, schedule.id(), cause( e ) );
      }
    }
  }

  /**
   * Инициаилизация таймера расписания
   *
   * @param aSchedule {@link IDtoObject} расписание, объект класса {@link ISkSchedule#CLASS_ID}
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void initScheduleTimer( IDtoObject aSchedule ) {
    TsNullArgumentRtException.checkNull( aSchedule );
    lockWrite( schedulesTimersLock );
    try {
      Skid scheduleId = aSchedule.skid();
      Timer timer = schedulesTimers.findByKey( scheduleId );
      if( timer != null ) {
        // Остановка текущего таймера
        timer.cancel();
        schedulesTimers.removeByKey( scheduleId );
        logger().info( MSG_FINISH_TIMER, scheduleId );
      }
      // Параметры календаря
      String second = getAttrStr( aSchedule, ATRINF_SECONDS );
      String minute = getAttrStr( aSchedule, ATRINF_MINUTES );
      String hour = getAttrStr( aSchedule, ATRINF_HOURS );
      String dayOfMonth = getAttrStr( aSchedule, ATRINF_DAYS_OF_MONTH );
      String month = getAttrStr( aSchedule, ATRINF_MONTHS );
      String dayOfWeek = getAttrStr( aSchedule, ATRINF_DAYS_OF_WEEK );
      String year = getAttrStr( aSchedule, ATRINF_YEARS );
      String timezone = getAttrStr( aSchedule, ATRINF_TIMEZONE );
      Date startTime = new Date( getAttrTime( aSchedule, ATRINF_START ) );
      Date endTime = new Date( getAttrTime( aSchedule, ATRINF_END ) );
      // Создание нового таймера
      TimerConfig tc = new TimerConfig( String.format( STR_SCHEDULE_TIMER, scheduleId ), false );
      tc.setPersistent( false );
      ScheduleExpression se = new ScheduleExpression();
      se.second( second );
      se.minute( minute );
      se.hour( hour );
      if( dayOfMonth.indexOf( QUSTION ) < 0 ) {
        se.dayOfMonth( dayOfMonth );
      }
      if( dayOfWeek.indexOf( QUSTION ) < 0 ) {
        se.dayOfWeek( dayOfWeek );
      }
      se.month( month );
      se.year( year );
      se.timezone( timezone );
      se.start( startTime );
      se.end( endTime );
      timer = timerService.createCalendarTimer( se, tc );
      schedulesTimers.put( scheduleId, timer );
      logger().info( MSG_START_TIMER, scheduleId, second, minute, hour, dayOfWeek, dayOfMonth, month, year );
    }
    finally {
      unlockWrite( schedulesTimersLock );
    }
  }

  /**
   * Удаляет таймер расписания
   * <p>
   * Если таймер расписания не найден, то ничего не делает
   *
   * @param aScheduleId {@link Skid} идентификатор расписания, объект класса {@link ISkSchedule#CLASS_ID}
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void removeScheduleTimer( Skid aScheduleId ) {
    TsNullArgumentRtException.checkNull( aScheduleId );
    lockWrite( schedulesTimersLock );
    try {
      Timer timer = schedulesTimers.findByKey( aScheduleId );
      if( timer != null ) {
        timer.cancel();
        schedulesTimers.removeByKey( aScheduleId );
        logger().info( MSG_FINISH_TIMER, aScheduleId );
        return;
      }
      // Таймен не найден
      logger().warning( ERR_REMOVING_TIMER_NOT_FOUND, aScheduleId );
    }
    finally {
      unlockWrite( schedulesTimersLock );
    }
  }

  /**
   * Читает строковое значение атрибута
   *
   * @param aDtoObj {@link IDtoObject} объект
   * @param aAttr {@link IDtoAttrInfo} описание атрибута
   * @return String текстовое значение
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static String getAttrStr( IDtoObject aDtoObj, IDtoAttrInfo aAttr ) {
    TsNullArgumentRtException.checkNulls( aDtoObj, aAttr );
    return aDtoObj.attrs().getStr( aAttr.id(), aAttr.dataType().defaultValue().asString() );
  }

  /**
   * Читает метку времени значения атрибута
   *
   * @param aDtoObj {@link IDtoObject} объект
   * @param aAttr {@link IDtoAttrInfo} описание атрибута
   * @return long метка времени
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static long getAttrTime( IDtoObject aDtoObj, IDtoAttrInfo aAttr ) {
    TsNullArgumentRtException.checkNulls( aDtoObj, aAttr );
    return aDtoObj.attrs().getTime( aAttr.id(), aAttr.dataType().defaultValue().asLong() );
  }

  /**
   * Пребразует массив чисел в текстовом виде в список чисел
   *
   * @param aList String[] массив чисел в текстовом виде
   * @return {@link IIntList} список чисел
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static IIntList strToIntList( String[] aList ) {
    TsNullArgumentRtException.checkNull( aList );
    IIntListEdit retValue = new IntArrayList();
    for( String item : aList ) {
      retValue.add( Integer.parseInt( item ) );
    }
    return retValue;
  }
}
