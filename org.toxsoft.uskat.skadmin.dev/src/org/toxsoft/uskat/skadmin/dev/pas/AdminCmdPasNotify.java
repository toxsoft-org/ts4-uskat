package org.toxsoft.uskat.skadmin.dev.pas;

import static org.toxsoft.core.pas.tj.impl.TjUtils.*;
import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.pas.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.pas.IAdminHardResources.*;

import org.toxsoft.core.pas.client.PasClient;
import org.toxsoft.core.pas.client.PasClientChannel;
import org.toxsoft.core.pas.common.PasChannel;
import org.toxsoft.core.pas.json.*;
import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.TsNotAllEnumsUsedRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

/**
 * Команда s5admin: передача уведомления PAS-серверу (Public Access Server)
 *
 * @author mvk
 */
public class AdminCmdPasNotify
    extends AbstractAdminCmd {

  /**
   * Обратный вызов выполняемой команды
   */
  private static IAdminCmdCallback callback;

  /**
   * Конструктор
   */
  public AdminCmdPasNotify() {
    // Контекст: PAS-клиент
    addArg( CTX_PAS_CLIENT );
    // Имя вызываемого метода
    addArg( ARG_PAS_METHOD );
    // Параметры вызываемого метода
    addArg( ARG_PAS_PARAMS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_PAS_NOTIFY_ID;
  }

  @Override
  public String alias() {
    return CMD_PAS_NOTIFY_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_PAS_NOTIFY_NAME;
  }

  @Override
  public String description() {
    return CMD_PAS_NOTIFY_DESCR;
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
    try {
      PasClient<PasClientChannel> pasClient = argSingleRef( CTX_PAS_CLIENT );
      PasChannel channel = pasClient.getChannelOrNull();
      if( channel == null ) {
        // Нет связи с PAS
        addResultInfo( '\n' + ERR_NOT_CONNECTION, pasClient );
        resultFail();
        return;
      }
      // Чтение аргументов введенных пользователем
      IAtomicValue method = argSingleValue( ARG_PAS_METHOD );
      IOptionSet params = argOptionSet( ARG_PAS_PARAMS );
      // Параметры запроса
      IStringMapEdit<ITjValue> jsonParams = new StringMap<>();
      for( String name : params.keys() ) {
        IAtomicValue value = params.getValue( name );
        switch( value.atomicType() ) {
          case INTEGER:
            jsonParams.put( name, createNumber( value.asInt() ) );
            break;
          case FLOATING:
            jsonParams.put( name, createNumber( value.asDouble() ) );
            break;
          case BOOLEAN:
          case TIMESTAMP:
          case STRING:
          case VALOBJ:
          case NONE:
            jsonParams.put( name, createString( value.asString() ) );
            break;
          default:
            throw new TsNotAllEnumsUsedRtException();
        }
      }
      long startTime = System.currentTimeMillis();
      // Регистрация слушателя ошибок обработки
      pasClient.registerErrorHandler( method.asString(), new PasErrorHandler() );
      try {
        // Отправка команды
        pasClient.getChannelOrNull().sendNotification( method.asString(), jsonParams );
      }
      finally {
        // Дерегистрация слушателей
        pasClient.unregisterErrorHandler( method.asString() );
      }
      long delta = (System.currentTimeMillis() - startTime) / 1000;
      addResultInfo( '\n' + MSG_CMD_TIME, Long.valueOf( delta ) );
      resultOk();
    }
    catch(

    Throwable e ) {
      addResultError( e );
      resultFail();
    }
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    return IList.EMPTY;
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
  static void print( String aMessage, Object... aArgs ) {
    callback.onNextStep( new ElemArrayList<>( info( aMessage, aArgs ) ), 0, 0, false );
  }

  /**
   * Вывести ошибку в callback клиента
   *
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы сообщения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void printError( String aMessage, Object... aArgs ) {
    callback.onNextStep( new ElemArrayList<>( error( aMessage, aArgs ) ), 0, 0, false );
  }

  // ------------------------------------------------------------------------------------
  // Обработчик ошибок выполнения
  //
  private static class PasErrorHandler
      implements IJSONErrorHandler<PasClientChannel> {

    @Override
    public void handle( PasClientChannel aChannel, IJSONRequest aRequest, IJSONError aError ) {
      // Вывод результата
      printError( ERR_RECEVIED_ERROR, aChannel, aRequest.method(), aError.toString() );
      synchronized (callback) {
        callback.notifyAll();
      }
    }
  }
}
