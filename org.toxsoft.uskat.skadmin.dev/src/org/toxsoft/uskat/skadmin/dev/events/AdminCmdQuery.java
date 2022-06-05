package org.toxsoft.uskat.skadmin.dev.events;

import static org.toxsoft.core.tslib.bricks.time.EQueryIntervalType.*;
import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.events.AdminEventsUtils.*;
import static org.toxsoft.uskat.skadmin.dev.events.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.events.IAdminHardResources.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.TimeZone;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.s5.legacy.QueryInterval;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

import ru.uskat.backend.ISkBackendInfo;
import ru.uskat.common.dpu.rt.events.SkEvent;
import ru.uskat.core.ISkCoreApi;
import ru.uskat.core.api.objserv.ISkObjectService;
import ru.uskat.core.api.sysdescr.*;
import ru.uskat.core.common.helpers.ITemporalsHistory;
import ru.uskat.core.connection.ISkConnection;

/**
 * Команда s5admin: Чтение значения текущего данного
 *
 * @author mvk
 */
public class AdminCmdQuery
    extends AbstractAdminCmd {

  /**
   * Формат вывода времени (с мсек)
   */
  private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss.SSS" ); //$NON-NLS-1$

  private static IAdminCmdCallback callback;

  /**
   * Конструктор
   */
  public AdminCmdQuery() {
    // Контекст: API ISkConnection
    addArg( CTX_SK_CONNECTION );
    // Идентификатор класса объекта
    addArg( ARG_CLASSID );
    // Строковый идентификатор объекта
    addArg( ARG_STRID );
    // Идентификатор данного
    addArg( ARG_EVID );
    // Метка времени начала чтения событий
    addArg( ARG_QUERY_START_TIME );
    // Метка времени завершения чтения событий
    addArg( ARG_QUERY_END_TIME );
    // Требование вывода значений параметров событий
    addArg( ARG_QUERY_PARAMS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_QUERY_ID;
  }

  @Override
  public String alias() {
    return CMD_QUERY_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_QUERY_NAME;
  }

  @Override
  public String description() {
    return CMD_QUERY_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return IPlexyType.NONE;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @Override
  public void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    callback = aCallback;
    ISkConnection connection = argSingleRef( CTX_SK_CONNECTION );
    try {
      ISkCoreApi coreApi = connection.coreApi();
      String classId = argSingleValue( ARG_CLASSID ).asString();
      String objStrid = argSingleValue( ARG_STRID ).asString();
      String eventId = argSingleValue( ARG_EVID ).asString();
      if( classId.equals( EMPTY_STRING ) ) {
        classId = MULTI;
      }
      if( objStrid.equals( EMPTY_STRING ) ) {
        objStrid = MULTI;
      }
      if( eventId.equals( EMPTY_STRING ) ) {
        eventId = MULTI;
      }
      IAtomicValue readStartTime = argSingleValue( ARG_QUERY_START_TIME );
      IAtomicValue readEndTime = argSingleValue( ARG_QUERY_END_TIME );
      IAtomicValue params = argSingleValue( ARG_QUERY_PARAMS );
      try {
        // Время начала выполнения команды запроса событий
        long startTime = System.currentTimeMillis();
        // Информация о сервере
        ISkBackendInfo info = connection.serverInfo();
        // Часовой пояс в котором работает сервер
        ZoneId zone = OP_BACKEND_ZONE_ID.getValue( info.params() ).asValobj();
        // История событий
        ITemporalsHistory<SkEvent> eventHistory = coreApi.eventService().history();
        // Определение интервала запроса
        if( !readStartTime.isAssigned() ) {
          // По умолчанию с начала суток
          Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( zone ) );
          cal.set( Calendar.HOUR_OF_DAY, 0 );
          cal.set( Calendar.MINUTE, 0 );
          cal.set( Calendar.SECOND, 0 );
          cal.set( Calendar.MILLISECOND, 0 );
          readStartTime = AvUtils.avTimestamp( cal.getTimeInMillis() );
        }
        if( !readEndTime.isAssigned() ) {
          // По умолчанию текущее вермя + час
          Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( zone ) );
          cal.setTimeInMillis( System.currentTimeMillis() + 60 * 60 * 1000 );
          readEndTime = AvUtils.avTimestamp( cal.getTimeInMillis() );
        }
        if( !params.isAssigned() ) {
          params = AvUtils.AV_FALSE;
        }
        IQueryInterval interval = new QueryInterval( CSCE, readStartTime.asLong(), readEndTime.asLong() );
        // Идентификаторы классов
        IStringListEdit classIds = new StringArrayList( classId );
        if( classId.equals( MULTI ) ) {
          classIds.clear();
          classIds.addAll( coreApi.sysdescr().classInfoManager().listClasses().keys() );
        }
        // Получение идентификаторов событий
        GwidList gwids = new GwidList();
        for( String clsId : classIds ) {
          gwids.addAll( getEventGwids( coreApi, clsId, objStrid, eventId ) );
        }
        // Журнал
        print( '\n' + MSG_QUERY, Integer.valueOf( gwids.size() ),
            getZonedDateTime( zone, interval.startTime() ).format( dtf ),
            getZonedDateTime( zone, interval.endTime() ).format( dtf ) );
        // Запрос
        ITimedList<SkEvent> events = eventHistory.query( interval, gwids );
        // Вывод результатов
        for( SkEvent event : events ) {
          Gwid gwid = event.eventGwid();
          String time = getZonedDateTime( zone, event.timestamp() ).format( dtf );
          StringBuilder sbParams = new StringBuilder();
          if( params.asBool() ) {
            sbParams.append( '\n' );
            for( int index = 0, n = event.paramValues().keys().size(); index < n; index++ ) {
              String name = event.paramValues().keys().get( index );
              sbParams.append( "   " + name ); //$NON-NLS-1$
              sbParams.append( '=' );
              sbParams.append( event.paramValues().getValue( name ) );
              sbParams.append( '\n' );
            }
          }
          else {
            sbParams.append( "(...)" ); //$NON-NLS-1$
          }
          print( '\n' + MSG_CMD_QUERY_EVENT, time, gwid, sbParams.toString() );
        }
        // Вывод количества событий
        addResultInfo( '\n' + MSG_QUERY_EVENTS_SIZE, Integer.valueOf( events.size() ) );

        long delta = (System.currentTimeMillis() - startTime) / 1000;
        addResultInfo( '\n' + MSG_CMD_TIME, Long.valueOf( delta ) );
        resultOk();
      }
      catch( Throwable e ) {
        addResultError( e );
        resultFail();
      }
    }
    finally {
      connection = null;
    }
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    IPlexyValue pxCoreApi = contextParamValueOrNull( CTX_SK_CORE_API );
    if( pxCoreApi == null ) {
      return IList.EMPTY;
    }
    ISkCoreApi coreApi = (ISkCoreApi)pxCoreApi.singleRef();
    ISkSysdescr sysdescr = coreApi.sysdescr();
    ISkClassInfoManager classManager = sysdescr.classInfoManager();
    ISkObjectService objService = coreApi.objService();
    if( aArgId.equals( ARG_CLASSID.id() ) ) {
      // Список всех классов
      IStridablesList<ISkClassInfo> classInfos = classManager.listClasses();
      // Подготовка списка возможных значений
      IListEdit<IPlexyValue> values = new ElemArrayList<>( classInfos.size() );
      for( int index = 0, n = classInfos.size(); index < n; index++ ) {
        IAtomicValue dataValue = AvUtils.avStr( classInfos.get( index ).id() );
        IPlexyValue plexyValue = pvSingleValue( dataValue );
        values.add( plexyValue );
      }
      return values;
    }
    if( (aArgId.equals( ARG_STRID.id() ) && aArgValues.keys().hasElem( ARG_CLASSID.id() )) ) {
      // Идентификатор класса
      String classId = aArgValues.getByKey( ARG_CLASSID.id() ).singleValue().asString();
      // Список всех объектов с учетом наследников
      ISkidList objList = objService.listSkids( classId, true );
      // Подготовка списка возможных значений
      IListEdit<IPlexyValue> values = new ElemArrayList<>( objList.size() );
      // Значение '*'
      IAtomicValue dataValue = AvUtils.avStr( MULTI );
      IPlexyValue plexyValue = pvSingleValue( dataValue );
      values.add( plexyValue );
      for( int index = 0, n = objList.size(); index < n; index++ ) {
        dataValue = AvUtils.avStr( objList.get( index ).strid() );
        plexyValue = pvSingleValue( dataValue );
        values.add( plexyValue );
      }
      return values;
    }
    if( aArgId.equals( ARG_EVID.id() ) && aArgValues.keys().hasElem( ARG_CLASSID.id() ) ) {
      String classId = aArgValues.getByKey( ARG_CLASSID.id() ).singleValue().asString();
      ISkClassInfo classInfo = classManager.findClassInfo( classId );
      if( classInfo == null ) {
        return IList.EMPTY;
      }
      IStridablesList<ISkEventInfo> eventInfos = classInfo.eventInfos();
      IListEdit<IPlexyValue> values = new ElemLinkedList<>();
      // Значение '*'
      IAtomicValue eventValue = AvUtils.avStr( MULTI );
      IPlexyValue plexyValue = pvSingleValue( eventValue );
      values.add( plexyValue );
      for( ISkEventInfo eventInfo : eventInfos ) {
        eventValue = AvUtils.avStr( eventInfo.id() );
        plexyValue = pvSingleValue( eventValue );
        values.add( plexyValue );
      }
      return values;
    }
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает время с учетом часового пояса
   *
   * @param aZone {@link ZoneId} часовой пояс
   * @param aTime long текущее время (мсек с начала эпохи)
   * @return ZonedDateTime время с учетом часового пояса
   */
  private static ZonedDateTime getZonedDateTime( ZoneId aZone, long aTime ) {
    return ZonedDateTime.of( Instant.ofEpochMilli( aTime ).atZone( aZone ).toLocalDateTime(), aZone );
  }

  /**
   * Вывести сообщение в callback клиента
   *
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы сообщения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void print( String aMessage, Object... aArgs ) {
    callback.onNextStep( new ElemArrayList<>( info( aMessage, aArgs ) ), 0, 0, false );
  }
}
