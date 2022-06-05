package org.toxsoft.uskat.skadmin.dev.commands;

import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.commands.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.commands.IAdminHardResources.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

import ru.uskat.common.dpu.rt.cmds.*;
import ru.uskat.core.ISkCoreApi;
import ru.uskat.core.api.cmds.ISkCommandExecutor;
import ru.uskat.core.api.cmds.ISkCommandService;

/**
 * Команда s5admin: отправка команды исполнителю
 *
 * @author mvk
 */
public class AdminCmdExecutor
    extends AbstractAdminCmd
    implements ISkCommandExecutor {

  /**
   * Обратный вызов выполняемой команды
   */
  private IAdminCmdCallback callback;

  /**
   * Метка получения последней команды
   */
  private long lastCommandTimestamp = System.currentTimeMillis();

  /**
   * Состояние отправленяемое в ответ при получении команды
   */
  private ESkCommandState responseState;

  /**
   * Таймаут отправки состояния в ответ
   */
  private int responseTimeout;

  /**
   * Конструктор
   */
  public AdminCmdExecutor() {
    // Контекст: API ISkConnection
    addArg( CTX_SK_CORE_API );
    // Идентификаторы выполняемых команд
    addArg( ARG_EXEC_GWIDS );
    // Таймаут(мсек) ожидания команд
    addArg( ARG_EXEC_TIMEOUT );
    // Состояние возвращаемое при получении команды
    addArg( ARG_EXEC_RESPONSE );
    // Таймаут отправки ответного состояния команды (мсек)
    addArg( ARG_EXEC_RESPONSE_TIMEOUT );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_EXEC_ID;
  }

  @Override
  public String alias() {
    return CMD_EXEC_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_EXEC_NAME;
  }

  @Override
  public String description() {
    return CMD_EXEC_DESCR;
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
    ISkCommandService commandService = coreApi.cmdService();
    try {
      // Идентификаторы выполняемых команд
      GwidList gwids = new GwidList();
      for( String gwid : argStrList( ARG_EXEC_GWIDS ) ) {
        gwids.add( Gwid.KEEPER.str2ent( gwid ) );
      }
      int timeout = argSingleValue( ARG_EXEC_TIMEOUT ).asInt();
      responseState = ESkCommandState.getById( argSingleValue( ARG_EXEC_RESPONSE ).asString() );
      responseTimeout = argSingleValue( ARG_EXEC_RESPONSE_TIMEOUT ).asInt();

      // Установка слушателя команд
      commandService.registerExecutor( this, gwids );
      try {
        long startTime = lastCommandTimestamp = System.currentTimeMillis();
        while( System.currentTimeMillis() - lastCommandTimestamp < timeout ) {
          Thread.sleep( timeout - (System.currentTimeMillis() - lastCommandTimestamp) );
        }
        long delta = (System.currentTimeMillis() - startTime) / 1000;
        addResultInfo( MSG_CMD_TIME, Long.valueOf( delta ) );
        resultOk();
      }
      catch( Throwable e ) {
        addResultError( e );
        resultFail();
      }
      finally {
        // Удаление слушателя команд
        commandService.unregisterExecutor( this );
      }
    }
    finally {
      // nop
    }
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    if( aArgId.equals( ARG_EXEC_RESPONSE.id() ) ) {
      // Подготовка списка возможных значений
      IListEdit<IPlexyValue> values = new ElemArrayList<>();
      for( ESkCommandState state : ESkCommandState.asList() ) {
        IAtomicValue dataValue = AvUtils.avStr( state.id() );
        IPlexyValue plexyValue = pvSingleValue( dataValue );
        values.add( plexyValue );
      }
      return values;
    }
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkCommandExecutor
  //
  @Override
  public void executeCommand( IDpuCommand aCmd ) {
    try {
      // Печать состояния команды
      println( MSG_COMMAND_EXECUTE, aCmd );
      // Время получения команды
      lastCommandTimestamp = System.currentTimeMillis();
      // Таймаут
      println( MSG_COMMAND_EXECUTE_TIMEOUT, Long.valueOf( responseTimeout ) );
      Thread.sleep( responseTimeout );
      // Формирование ответа
      println( MSG_COMMAND_EXECUTE_RESPONSNE, responseState );
      IPlexyValue pxCoreApi = contextParamValueOrNull( CTX_SK_CORE_API );
      ISkCoreApi coreApi = (ISkCoreApi)pxCoreApi.singleRef();
      ISkCommandService commandService = coreApi.cmdService();
      SkCommandState state = new SkCommandState( System.currentTimeMillis(), responseState );
      commandService.changeCommandState( new DpuCommandStateChangeInfo( aCmd.id(), state ) );
    }
    catch( Throwable e ) {
      logger().error( e );
    }
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
