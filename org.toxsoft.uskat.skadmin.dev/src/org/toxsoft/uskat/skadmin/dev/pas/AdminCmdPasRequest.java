package org.toxsoft.uskat.skadmin.dev.pas;

import static org.toxsoft.core.pas.tj.impl.TjUtils.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.bricks.validator.ValidationResult.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.pas.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.pas.IAdminHardResources.*;

import java.io.FileWriter;
import java.io.PrintWriter;

import org.toxsoft.core.pas.client.PasClient;
import org.toxsoft.core.pas.client.PasClientChannel;
import org.toxsoft.core.pas.common.IPasIoLogger;
import org.toxsoft.core.pas.common.PasChannel;
import org.toxsoft.core.pas.json.*;
import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.TsNotAllEnumsUsedRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

/**
 * Команда s5admin: выполнение запросов к PAS-серверу (Public Access Server)
 *
 * @author mvk
 */
public class AdminCmdPasRequest
    extends AbstractAdminCmd {

  /**
   * Обратный вызов выполняемой команды
   */
  private static IAdminCmdCallback callback;

  /**
   * Конструктор
   */
  public AdminCmdPasRequest() {
    // Контекст: PAS-клиент
    addArg( CTX_PAS_CLIENT );
    // Имя вызываемого метода
    addArg( ARG_PAS_METHOD );
    // Параметры вызываемого метода
    addArg( ARG_PAS_PARAMS );
    // Имя файла в котором сохраняется результат
    addArg( ARG_PAS_REQUEST_FILENAME );
    // Таймаут (мсек) выполнения метода
    addArg( ARG_PAS_REQUEST_TIMEOUT );
    // Требование выводить на экран принятые данные
    addArg( ARG_PAS_REQUEST_IO_LOGGER );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_PAS_REQUEST_ID;
  }

  @Override
  public String alias() {
    return CMD_PAS_REQUEST_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_PAS_REQUEST_NAME;
  }

  @Override
  public String description() {
    return CMD_PAS_REQUEST_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return IPlexyType.NONE;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @SuppressWarnings( "resource" )
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
      IAtomicValue filename = argSingleValue( ARG_PAS_REQUEST_FILENAME );
      IAtomicValue timeout = argSingleValue( ARG_PAS_REQUEST_TIMEOUT );
      IAtomicValue needLogger = argSingleValue( ARG_PAS_REQUEST_IO_LOGGER );
      if( !timeout.isAssigned() ) {
        timeout = avInt( ARG_PAS_REQUEST_TIMEOUT_DEFAULT );
      }
      if( !needLogger.isAssigned() ) {
        needLogger = AV_TRUE;
      }
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
      // Регистрация слушателя ответа
      pasClient.registerResultHandler( method.asString(), new PasResultHandler() );
      // Регистрация слушателя ошибок обработки
      pasClient.registerErrorHandler( method.asString(), new PasErrorHandler() );
      try {
        // Журнал ввода/вывода
        PasIoLogger ioLogger = null;
        if( needLogger.asBool() || filename.isAssigned() ) {
          IAdminCmdCallback cb = (needLogger.asBool() ? callback : null);
          PrintWriter file = null;
          if( filename.isAssigned() ) {
            file = new PrintWriter( new FileWriter( filename.asString() ) );
          }
          ioLogger = new PasIoLogger( cb, file );
          // Установка журнала ввода/вывода
          pasClient.getChannelOrNull().setIoLogger( ioLogger );
        }
        // Отправка команды
        pasClient.getChannelOrNull().sendRequest( method.asString(), jsonParams );
        // Ожидание завершения
        synchronized (callback) {
          callback.wait( timeout.asLong() );
        }
        // Завершение работы журнала
        if( ioLogger != null ) {
          ioLogger.close();
        }
      }
      finally {
        // Дерегистрация слушателей
        pasClient.unregisterResultHandler( method.asString() );
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
   * Вывести сообщение в cb клиента
   *
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы сообщения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void print( String aMessage, Object... aArgs ) {
    callback.onNextStep( new ElemArrayList<>( info( aMessage, aArgs ) ), 0, 0, false );
  }

  /**
   * Вывести ошибку в cb клиента
   *
   * @param aMessage String - текст сообщения
   * @param aArgs Object[] - аргументы сообщения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void printError( String aMessage, Object... aArgs ) {
    callback.onNextStep( new ElemArrayList<>( error( aMessage, aArgs ) ), 0, 0, false );
  }

  // ------------------------------------------------------------------------------------
  // Обработчик результата выполнения
  //
  private static class PasResultHandler
      implements IJSONResultHandler<PasClientChannel> {

    @Override
    public void handle( PasClientChannel aChannel, IJSONRequest aRequest, IJSONResult aResult ) {
      // Вывод результата
      print( MSG_RECEVIED_RESULT, aChannel, aRequest.method(), aResult.toString() );
      synchronized (callback) {
        callback.notifyAll();
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // Журнал ввода/вывода
  //
  private static class PasIoLogger
      implements IPasIoLogger, ICloseable {

    private IAdminCmdCallback cb;
    private StringBuilder     sb;
    private PrintWriter       file;

    PasIoLogger( IAdminCmdCallback aCallback, PrintWriter aFile ) {
      cb = aCallback;
      if( cb != null ) {
        sb = new StringBuilder();
      }
      file = aFile;
    }

    @Override
    public void readChar( char aChar ) {
      if( sb != null ) {
        sb.append( aChar );
        if( sb.length() > 1048576 && aChar == '\n' ) {
          cb.onNextStep( new ElemArrayList<>( info( sb.toString() ) ), 0, 0, false );
          sb = new StringBuilder();
          sb.append( '\n' );
        }
      }
      if( file != null ) {
        file.write( aChar );
      }
    }

    @Override
    public void close() {
      if( sb != null ) {
        cb.onNextStep( new ElemArrayList<>( info( sb.toString() ) ), 0, 0, false );
      }
      if( file != null ) {
        file.close();
      }
    }
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
