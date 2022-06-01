package org.toxsoft.uskat.skadmin.dev.pas;

import static org.toxsoft.core.pas.server.IPasServerParams.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.dev.pas.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.dev.pas.IAdminHardResources.*;

import org.toxsoft.core.pas.client.PasClient;
import org.toxsoft.core.pas.client.PasClientChannel;
import org.toxsoft.core.pas.common.IPasParams;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants;
import org.toxsoft.core.tslib.bricks.ctx.ITsContext;
import org.toxsoft.core.tslib.bricks.ctx.impl.TsContext;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.IAdminCmdContextParam;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

/**
 * Команда s5admin: подключение к PAS-серверу (Public Access Server)
 *
 * @author mvk
 */
public class AdminCmdPasConnect
    extends AbstractAdminCmd {

  /**
   * Конструктор
   */
  public AdminCmdPasConnect() {
    // Имя хоста или IP адрес PAS-сервера
    addArg( ARG_PAS_HOST );
    // Порт PAS-сервера
    addArg( ARG_PAS_PORT );
    // Таймаут (мсек) подключения к PAS
    addArg( ARG_PAS_CREATE_TIMEOUT );
    // Таймаут (мсек) обрыва связи с PAS
    addArg( ARG_PAS_FAILURE_TIMEOUT );
    // Таймаут (мсек) записи в PAS
    addArg( ARG_PAS_WRITE_TIMEOUT );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_PAS_CONNECT_ID;
  }

  @Override
  public String alias() {
    return CMD_PAS_CONNECT_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_PAS_CONNECT_NAME;
  }

  @Override
  public String description() {
    return CMD_PAS_CONNECT_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return CTX_PAS_CLIENT.type();
  }

  @Override
  public String resultDescription() {
    return CTX_PAS_CLIENT.description();
  }

  @Override
  public IStridablesList<IAdminCmdContextParam> resultContextParams() {
    IStridablesListEdit<IAdminCmdContextParam> params = new StridablesList<>();
    params.addAll( CTX_PAS_CLIENT );
    return params;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @Override
  public void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    try {
      // Чтение аргументов введенных пользователем
      IAtomicValue host = argSingleValue( ARG_PAS_HOST );
      IAtomicValue port = argSingleValue( ARG_PAS_PORT );
      IAtomicValue createTimeout = argSingleValue( ARG_PAS_CREATE_TIMEOUT );
      IAtomicValue writeTimeout = argSingleValue( ARG_PAS_WRITE_TIMEOUT );
      IAtomicValue failureTimeout = argSingleValue( ARG_PAS_FAILURE_TIMEOUT );
      // Установка значений по умолчанию
      if( !host.isAssigned() ) {
        host = avStr( ARG_PAS_HOST_DEFAULT );
      }
      if( !port.isAssigned() ) {
        port = avInt( ARG_PAS_PORT_DEFAULT );
      }
      if( !createTimeout.isAssigned() ) {
        createTimeout = avInt( ARG_PAS_CREATE_TIMEOUT_DEFAULT );
      }
      if( !failureTimeout.isAssigned() ) {
        failureTimeout = avInt( ARG_PAS_FAILURE_TIMEOUT_DEFAULT );
      }
      if( !writeTimeout.isAssigned() ) {
        writeTimeout = avInt( ARG_PAS_WRITE_TIMEOUT_DEFAULT );
      }
      // Время начала выполнения команды запроса событий
      long startTime = System.currentTimeMillis();
      // Подключение к PAS
      ITsContext ctx = new TsContext();
      // ctx.put( S5CallbackClient.class, aReader );
      OP_PAS_SERVER_ADDRESS.setValue( ctx.params(), avStr( host.asString() ) );
      OP_PAS_SERVER_PORT.setValue( ctx.params(), avInt( port.asInt() ) );
      // Имя канала
      String channelName = host.toString() + ':' + port.toString();
      // Идентификация канала (узел-сессия)
      ctx.params().setStr( IAvMetaConstants.TSID_NAME, channelName );
      ctx.params().setStr( IAvMetaConstants.TSID_DESCRIPTION, channelName );
      // Установка таймаутов
      IPasParams.OP_PAS_WRITE_TIMEOUT.setValue( ctx.params(), writeTimeout );
      IPasParams.OP_PAS_FAILURE_TIMEOUT.setValue( ctx.params(), failureTimeout );
      // aExternalDoJobCall = false: создавать внутренний поток для doJob
      PasClient<PasClientChannel> pasClient = new PasClient<>( ctx, PasClientChannel.CREATOR, false, logger() );
      // Запуск потока
      Thread thread = new Thread( pasClient );
      thread.start();
      // Ожидание подключения
      while( pasClient.getChannelOrNull() == null ) {
        if( System.currentTimeMillis() - startTime > createTimeout.asInt() ) {
          // Ошибка подключения по таймату
          addResultError( '\n' + ERR_NOT_CONNECTION_BY_TIMEOUT, channelName, Long.valueOf( createTimeout.asInt() ) );
          pasClient.close();
          resultFail();
          return;
        }
        Thread.sleep( 10 );
      }
      // Сообщение о подключении
      addResultInfo( '\n' + MSG_CMD_PAS_CONNECT, channelName );
      long delta = (System.currentTimeMillis() - startTime) / 1000;
      addResultInfo( '\n' + MSG_CMD_TIME, Long.valueOf( delta ) );
      // Подготовка результата
      IPlexyValue pxPasClient = pvSingleRef( pasClient );
      setContextParamValue( CTX_PAS_CLIENT, pxPasClient );

      resultOk( pxPasClient );
    }
    catch( Throwable e ) {
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
}
