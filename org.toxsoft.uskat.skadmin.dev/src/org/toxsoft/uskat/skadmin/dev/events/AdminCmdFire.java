package org.toxsoft.uskat.skadmin.dev.events;

import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.events.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.events.IAdminHardResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.skadmin.core.*;
import org.toxsoft.uskat.skadmin.core.impl.*;

/**
 * Команда s5admin: генерация события
 *
 * @author mvk
 */
public class AdminCmdFire
    extends AbstractAdminCmd {

  /**
   * Обратный вызов выполняемой команды
   */
  private IAdminCmdCallback callback;

  /**
   * Конструктор
   */
  public AdminCmdFire() {
    // Контекст: API ISkConnection
    addArg( CTX_SK_CORE_API );
    // Идентификатор класса объекта
    addArg( ARG_CLASSID );
    // Строковый идентификатор объекта
    addArg( ARG_STRID );
    // evId
    addArg( ARG_EVID );
    // Аргументы события
    addArg( ARG_FIRE_PARAMS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_FIRE_ID;
  }

  @Override
  public String alias() {
    return CMD_FIRE_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_FIRE_NAME;
  }

  @Override
  public String description() {
    return CMD_FIRE_DESCR;
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
    // API сервера
    ISkCoreApi coreApi = argSingleRef( CTX_SK_CORE_API );
    ISkEventService eventService = coreApi.eventService();
    try {
      // Аргументы команды
      String classId = argSingleValue( ARG_CLASSID ).asString();
      String objStrid = argSingleValue( ARG_STRID ).asString();
      String eventId = argSingleValue( ARG_EVID ).asString();
      IOptionSet args = argOptionSet( ARG_FIRE_PARAMS );
      try {
        long startTime = System.currentTimeMillis();
        Gwid eventGwid = Gwid.createEvent( classId, objStrid, eventId );
        // Исполнитель uskat-потоков
        ITsThreadExecutor threadExecutor = SkThreadExecutorService.getExecutor( coreApi );
        // Передача события
        threadExecutor.syncExec( () -> eventService.fireEvent( new SkEvent( startTime, eventGwid, args ) ) );
        // Команда отправлена на выполнение
        println( MSG_EVENT_FIRED, eventGwid );
        long delta = (System.currentTimeMillis() - startTime) / 1000;
        addResultInfo( MSG_CMD_TIME, Long.valueOf( delta ) );
        resultOk();
      }
      catch( Throwable e ) {
        addResultError( e );
        resultFail();
      }
    }
    finally {
      // nop
    }
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    IPlexyValue pxCoreApi = contextParamValueOrNull( CTX_SK_CORE_API );
    if( pxCoreApi == null ) {
      return IList.EMPTY;
    }
    IListEdit<IPlexyValue> retValues = new ElemArrayList<>();
    // Исполнитель uskat-потоков
    ITsThreadExecutor threadExecutor = SkThreadExecutorService.getExecutor( pxCoreApi.singleRef() );
    // Определение допускаемых значений
    threadExecutor.syncExec( () -> {
      ISkCoreApi coreApi = (ISkCoreApi)pxCoreApi.singleRef();
      ISkSysdescr sysdescr = coreApi.sysdescr();
      ISkObjectService objService = coreApi.objService();
      if( aArgId.equals( ARG_CLASSID.id() ) ) {
        // Список всех классов
        IStridablesList<ISkClassInfo> classInfos = sysdescr.listClasses();
        // Подготовка списка возможных значений
        for( int index = 0, n = classInfos.size(); index < n; index++ ) {
          IAtomicValue dataValue = AvUtils.avStr( classInfos.get( index ).id() );
          IPlexyValue plexyValue = pvSingleValue( dataValue );
          retValues.add( plexyValue );
        }
      }
      if( aArgId.equals( ARG_STRID.id() ) && aArgValues.keys().hasElem( ARG_CLASSID.id() ) ) {
        // Идентификатор класса
        String classId = aArgValues.getByKey( ARG_CLASSID.id() ).singleValue().asString();
        // Список всех объектов с учетом наследников
        ISkidList objList = objService.listSkids( classId, true );
        // Пустое значение
        IAtomicValue dataValue = AvUtils.avStr( EMPTY_STRING );
        IPlexyValue plexyValue = pvSingleValue( dataValue );
        retValues.add( plexyValue );
        for( int index = 0, n = objList.size(); index < n; index++ ) {
          dataValue = AvUtils.avStr( objList.get( index ).strid() );
          plexyValue = pvSingleValue( dataValue );
          retValues.add( plexyValue );
        }
      }
      if( aArgId.equals( ARG_EVID.id() ) && aArgValues.keys().hasElem( ARG_CLASSID.id() ) ) {
        // Идентификатор класса
        String classId = aArgValues.getByKey( ARG_CLASSID.id() ).singleValue().asString();
        // Список всех связей с учетом наследников
        ISkClassInfo classInfo = sysdescr.findClassInfo( classId );
        if( classInfo == null ) {
          return;
        }
        IStridablesList<IDtoEventInfo> eventInfoes = classInfo.events().list();
        // Пустое значение
        IAtomicValue dataValue = AvUtils.avStr( EMPTY_STRING );
        IPlexyValue plexyValue = pvSingleValue( dataValue );
        retValues.add( plexyValue );
        for( IDtoEventInfo eventInfo : eventInfoes ) {
          dataValue = AvUtils.avStr( eventInfo.id() );
          plexyValue = pvSingleValue( dataValue );
          retValues.add( plexyValue );
        }
      }
    } );
    return retValues;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Вывести сообщение в callback клиента
   *
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы сообщения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private void println( String aMessage, Object... aArgs ) {
    print( aMessage + '\n', aArgs );
  }

  /**
   * Вывести сообщение в callback клиента
   *
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы сообщения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private void print( String aMessage, Object... aArgs ) {
    callback.onNextStep( new ElemArrayList<>( info( aMessage, aArgs ) ), 0, 0, false );
  }
}
