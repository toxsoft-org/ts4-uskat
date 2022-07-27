package org.toxsoft.uskat.skadmin.dev.commands;

import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.commands.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.commands.IAdminHardResources.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.change.IGenericChangeListener;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.objserv.ISkObjectService;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.ISkSysdescr;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoCmdInfo;
import org.toxsoft.uskat.core.api.users.ISkUser;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.core.impl.SkCommand;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

/**
 * Команда s5admin: отправка команды исполнителю
 *
 * @author mvk
 */
public class AdminCmdSend
    extends AbstractAdminCmd
    implements IGenericChangeListener {

  /**
   * Обратный вызов выполняемой команды
   */
  private IAdminCmdCallback callback;

  /**
   * Признак того, что выполнение команды завершено
   */
  private boolean commandFinished;

  /**
   * Конструктор
   */
  public AdminCmdSend() {
    // Контекст: ISkConnection
    addArg( CTX_SK_CONNECTION );
    // Контекст: API ISkConnection
    addArg( CTX_SK_CORE_API );
    // Идентификатор класса объекта
    addArg( ARG_SEND_CLASSID );
    // Строковый идентификатор объекта
    addArg( ARG_SEND_STRID );
    // cmdId
    addArg( ARG_SEND_CMDID );
    // Аргументы команды
    addArg( ARG_SEND_ARGS );
    // Идентификатор класса автора команды
    addArg( ARG_SEND_AUTHOR_CLASSID );
    // Строковый идентификатор объекта автора команды (strid)
    addArg( ARG_SEND_AUTHOR_STRID );
    // Таймаут(мсек) между отправкой следующей команды
    addArg( ARG_SEND_TIMEOUT );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_SEND_ID;
  }

  @Override
  public String alias() {
    return CMD_SEND_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_SEND_NAME;
  }

  @Override
  public String description() {
    return CMD_SEND_DESCR;
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
    ISkConnection connection = argSingleRef( CTX_SK_CONNECTION );
    ISkCoreApi coreApi = argSingleRef( CTX_SK_CORE_API );
    ISkCommandService commandService = coreApi.cmdService();
    try {
      // Аргументы команды
      String classId = argSingleValue( ARG_SEND_CLASSID ).asString();
      String objStrid = argSingleValue( ARG_SEND_STRID ).asString();
      String cmdId = argSingleValue( ARG_SEND_CMDID ).asString();
      IOptionSet args = argOptionSet( ARG_SEND_ARGS );
      IAtomicValue authorClassId = argSingleValue( ARG_SEND_AUTHOR_CLASSID );
      IAtomicValue authorStrid = argSingleValue( ARG_SEND_AUTHOR_STRID );
      if( !authorClassId.isAssigned() ) {
        authorClassId = AvUtils.avStr( ISkUser.CLASS_ID );
      }
      if( !authorStrid.isAssigned() ) {
        // TODO: connection.getConnectionInfo()....
        // authorStrid = AvUtils.avStr( connection.sessionInfo().getUser().login() );
        authorStrid = AvUtils.avStr( "root" ); //$NON-NLS-1$
      }
      int timeout = argSingleValue( ARG_SEND_TIMEOUT ).asInt();
      try {
        long startTime = System.currentTimeMillis();
        Gwid cmdGwid = Gwid.createCmd( classId, objStrid, cmdId );
        Skid authorSkid = new Skid( authorClassId.asString(), authorStrid.asString() );
        synchronized (this) {
          commandFinished = false;
          // Выполнение
          ISkCommand cmd = commandService.sendCommand( cmdGwid, authorSkid, args );
          // Установка слушателя команды
          cmd.stateEventer().addListener( this );
          // Команда отправлена на выполнение
          println( MSG_COMMAND_SEND, cmd.instanceId() );
          // Ожидание выполнения
          this.wait( timeout );
          // Проверка завершения
          if( !commandFinished ) {
            // Команда не выполнена в установленное время и будет отменена
            addResultError( ERR_CANT_EXECUTE_BY_TIMEOUT + '\n', cmdGwid, Long.valueOf( timeout ) );
            SkCommandState cancelState = new SkCommandState( System.currentTimeMillis(), ESkCommandState.TIMEOUTED );
            commandService.changeCommandState( new DtoCommandStateChangeInfo( cmd.instanceId(), cancelState ) );
            resultFail();
            return;
          }
        }
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
    ISkCoreApi coreApi = (ISkCoreApi)pxCoreApi.singleRef();
    ISkSysdescr sysdescr = coreApi.sysdescr();
    ISkObjectService objService = coreApi.objService();
    if( aArgId.equals( ARG_SEND_CLASSID.id() ) ) {
      // Список всех классов
      IStridablesList<ISkClassInfo> classInfos = sysdescr.listClasses();
      // Подготовка списка возможных значений
      IListEdit<IPlexyValue> values = new ElemArrayList<>( classInfos.size() );
      // Тип значений
      for( int index = 0, n = classInfos.size(); index < n; index++ ) {
        IAtomicValue dataValue = AvUtils.avStr( classInfos.get( index ).id() );
        IPlexyValue plexyValue = pvSingleValue( dataValue );
        values.add( plexyValue );
      }
      return values;
    }
    if( (aArgId.equals( ARG_SEND_STRID.id() ) && aArgValues.keys().hasElem( ARG_SEND_CLASSID.id() )) ) {
      // Идентификатор класса
      String classId = aArgValues.getByKey( ARG_SEND_CLASSID.id() ).singleValue().asString();
      // Список всех объектов с учетом наследников
      ISkidList objList = objService.listSkids( classId, true );
      // Подготовка списка возможных значений
      IListEdit<IPlexyValue> values = new ElemArrayList<>( objList.size() );
      // Пустое значение
      IAtomicValue dataValue = AvUtils.avStr( TsLibUtils.EMPTY_STRING );
      IPlexyValue plexyValue = pvSingleValue( dataValue );
      values.add( plexyValue );
      for( int index = 0, n = objList.size(); index < n; index++ ) {
        dataValue = AvUtils.avStr( objList.get( index ).strid() );
        plexyValue = pvSingleValue( dataValue );
        values.add( plexyValue );
      }
      return values;
    }
    if( aArgId.equals( ARG_SEND_CMDID.id() ) && aArgValues.keys().hasElem( ARG_SEND_CLASSID.id() ) ) {
      // Идентификатор класса
      String classId = aArgValues.getByKey( ARG_SEND_CLASSID.id() ).singleValue().asString();
      // Список всех связей с учетом наследников
      ISkClassInfo classInfo = sysdescr.findClassInfo( classId );
      if( classInfo == null ) {
        return IList.EMPTY;
      }
      IStridablesList<IDtoCmdInfo> cmdInfoes = classInfo.cmds().list();
      IListEdit<IPlexyValue> values = new ElemArrayList<>( cmdInfoes.size() );
      // Пустое значение
      IPlexyValue plexyValue = pvSingleValue( AvUtils.AV_STR_EMPTY );
      values.add( plexyValue );
      for( IDtoCmdInfo cmdInfo : cmdInfoes ) {
        IAtomicValue dataValue = AvUtils.avStr( cmdInfo.id() );
        plexyValue = pvSingleValue( dataValue );
        values.add( plexyValue );
      }
      return values;
    }
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IGenericChangeListener
  //
  @Override
  public void onGenericChangeEvent( Object aSource ) {
    SkCommand aCommand = (SkCommand)aSource;
    // Печать состояния команды
    StringBuilder sb = new StringBuilder();
    sb.append( aCommand.state() );
    sb.append( '(' );
    IOptionSet params = aCommand.state().params();
    for( int index = 0, n = params.size(); index < n; index++ ) {
      String paramName = params.keys().get( index );
      sb.append( paramName + '=' + params.getValue( paramName ) );
      if( index + 1 < n ) {
        sb.append( ',' );
      }
    }
    sb.append( ')' );
    println( MSG_COMMAND_STATE_CHANGED, sb.toString() );
    synchronized (this) {
      if( aCommand.state().state().isComplete() ) {
        // Команда завершена
        commandFinished = true;
        // Сигнал о завершении команды
        this.notifyAll();
      }
    }
  }

  // @Override
  // public void onExecutableCommandGwidsChanged( IGwidList aExecutableCommandGwids ) {
  // // Печать об изменении списка поддерживаемых команд
  // println( MSG_EXCUTABLE_COMMAND_GWIDS_CHANGED, Integer.valueOf( aExecutableCommandGwids.size() ) );
  // }

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
