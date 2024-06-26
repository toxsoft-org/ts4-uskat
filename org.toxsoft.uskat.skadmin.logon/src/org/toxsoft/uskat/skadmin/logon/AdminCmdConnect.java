package org.toxsoft.uskat.skadmin.logon;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.logon.AdminLogonUtils.*;
import static org.toxsoft.uskat.skadmin.logon.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.logon.IAdminResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.legacy.plexy.*;
import org.toxsoft.uskat.s5.client.*;
import org.toxsoft.uskat.s5.client.remote.*;
import org.toxsoft.uskat.s5.common.*;
import org.toxsoft.uskat.s5.server.*;
import org.toxsoft.uskat.skadmin.core.*;
import org.toxsoft.uskat.skadmin.core.impl.*;

/**
 * Команда администрирования: вход пользователя на skat-s5-сервер
 *
 * @author mvk
 */
public class AdminCmdConnect
    extends AbstractAdminCmd {

  /**
   * Конструктор
   */
  public AdminCmdConnect() {
    // Контекст: исполнитель запросов в одном потоке
    addArg( CTX_THREAD_EXECUTOR );
    // Имя пользователя
    addArg( ARG_CONNECT_USER );
    // Пароль пользователя
    addArg( ARG_CONNECT_PASSWORD );
    // Список хостов узлов кластера
    addArg( ARG_CONNECT_HOST );
    // Список портов узлов кластера
    addArg( ARG_CONNECT_PORT );
    // Таймаут создания соединения
    addArg( ARG_CONNECT_CONNECT_TIMEOUT );
    // Таймаут разрыва соединения
    addArg( ARG_CONNECT_FAILURE_TIMEOUT );
    // Таймаут текущих данных
    addArg( ARG_CONNECT_CURRDATA_TIMEOUT );
    // Таймаут хранимых данных
    addArg( ARG_CONNECT_HISTDATA_TIMEOUT );
    // Имя класса-инициализатора клиентского API
    // addArg( ARG_CONNECT_INITIALIZER );
    // Подключение расширения API: Реальное время
    addArg( ARG_CONNECT_REALTIME );
    // Подключение расширения API: Пакетные операции
    addArg( ARG_CONNECT_BATCH );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_CONNECT_ID;
  }

  @Override
  public String alias() {
    return CMD_CONNECT_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_CONNECT_NAME;
  }

  @Override
  public String description() {
    return CMD_CONNECT_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return CTX_SK_CONNECTION.type();
  }

  @Override
  public String resultDescription() {
    return CTX_SK_CONNECTION.description();
  }

  @Override
  public IStridablesList<IAdminCmdContextParam> resultContextParams() {
    IStridablesListEdit<IAdminCmdContextParam> params = new StridablesList<>();
    params.addAll( CTX_SK_CONNECTION, CTX_SK_HOSTS, CTX_SK_CORE_API, CTX_SK_CLASS_SERVICE, CTX_SK_OBJECT_SERVICE,
        CTX_SK_LINK_SERVICE );
    return params;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @Override
  public void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    ITsThreadExecutor threadExecutor = argSingleRef( CTX_THREAD_EXECUTOR );
    // Параметры команды
    String login = argSingleValue( ARG_CONNECT_USER ).asString();
    String password = argSingleValue( ARG_CONNECT_PASSWORD ).asString();

    IStringList hostnames = argStrList( ARG_CONNECT_HOST );
    IIntList ports = argIntList( ARG_CONNECT_PORT );
    if( hostnames.size() == 0 ) {
      // Неопределен список сетевых имен сервера (host)
      hostnames = new StringArrayList( "localhost" ); //$NON-NLS-1$
    }
    if( ports.size() == 0 ) {
      // Неопределен список ip-адресов сервера
      ports = new IntArrayList( 8080 );
    }
    if( hostnames.size() > 1 && ports.size() == 0 && hostnames.size() != ports.size() ) {
      // Количество портов не соответствует количеству узлов кластера
      addResultError( ERR_CONNECT_WRONG_NODE_PORTS );
      resultFail();
      return;
    }
    S5HostList hosts = new S5HostList();
    for( int index = 0, n = hostnames.size(); index < n; index++ ) {
      hosts.add( new S5Host( hostnames.get( index ), ports.getValue( index ) ) );
    }
    IAtomicValue connectTimeout = argSingleValue( ARG_CONNECT_CONNECT_TIMEOUT );
    IAtomicValue failureTimeout = argSingleValue( ARG_CONNECT_FAILURE_TIMEOUT );
    IAtomicValue currdataTimeout = argSingleValue( ARG_CONNECT_CURRDATA_TIMEOUT );
    IAtomicValue histdataTimeout = argSingleValue( ARG_CONNECT_HISTDATA_TIMEOUT );
    // IAtomicValue initializator = argSingleValue( ARG_CONNECT_INITIALIZER );
    try {
      ISkConnection connection = SkCoreUtils.createConnection();
      // S5SynchronizedConnection.createSynchronizedConnection( SkCoreUtils.createConnection() );
      connection.addConnectionListener( ( aSource, aOldState ) -> {
        if( aSource.state() == ESkConnState.ACTIVE ) {
          AdminCmdGetConnection.SK_CONNECTIONS.add( aSource );
        }
        if( aOldState == ESkConnState.ACTIVE ) {
          AdminCmdGetConnection.SK_CONNECTIONS.remove( aSource );
        }
      } );
      IPlexyValue pxConnection = pvSingleRef( connection );
      setContextParamValue( CTX_SK_CONNECTION, pxConnection );
      // Слушаем соединение
      connection.addConnectionListener( AdminCmdGetConnection.CURRENT_CONNECTION_LISTENER );

      ITsContext ctx = new TsContext();
      // Настройка параметров будущего соединения
      ISkCoreConfigConstants.REFDEF_BACKEND_PROVIDER.setRef( ctx, new S5RemoteBackendProvider() );
      ISkCoreConfigConstants.REFDEF_THREAD_EXECUTOR.setRef( ctx, threadExecutor );

      IS5ConnectionParams.OP_USERNAME.setValue( ctx.params(), avStr( login ) );
      IS5ConnectionParams.OP_PASSWORD.setValue( ctx.params(), avStr( password ) );

      IS5ConnectionParams.OP_HOSTS.setValue( ctx.params(), avValobj( hosts ) );
      IS5ConnectionParams.OP_CLIENT_PROGRAM.setValue( ctx.params(), avStr( "skadmin" ) ); //$NON-NLS-1$
      IS5ConnectionParams.OP_CLIENT_VERSION.setValue( ctx.params(), avValobj( IS5ServerHardConstants.version ) );
      IS5ConnectionParams.OP_CONNECT_TIMEOUT.setValue( ctx.params(), connectTimeout );
      IS5ConnectionParams.OP_FAILURE_TIMEOUT.setValue( ctx.params(), failureTimeout );
      IS5ConnectionParams.OP_CURRDATA_TIMEOUT.setValue( ctx.params(), currdataTimeout );
      IS5ConnectionParams.OP_HISTDATA_TIMEOUT.setValue( ctx.params(), histdataTimeout );

      // Создание соединения
      connection.open( ctx );
      setContextParamValue( CTX_SK_CORE_API, pvSingleRef( connection.coreApi() ) );
      setContextParamValue( CTX_SK_HOSTS, pvSingleRef( new S5Host( hostnames.first(), ports.first().intValue() ) ) );

      if( contextParamReferenceCount( CTX_SK_CONNECTION ) != 0 ) {
        // В контексте только одна ссылка на текущее соединение. Завершаем его
        connection = (ISkConnection)contextParamValue( CTX_SK_CONNECTION ).singleRef();
        // Удаляем своего слушателя соединения
        connection.removeConnectionListener( AdminCmdGetConnection.CURRENT_CONNECTION_LISTENER );
      }
      ISkBackendInfo info = connection.backendInfo();
      addResultInfo( MSG_CONNECT, connectionToString( info ) );
      addResultInfo( MSG_CONNECT_SERVER_ID, info.id() );
      addResultInfo( MSG_CONNECT_SERVER_NAME, info.nmName() );
      addResultInfo( MSG_CONNECT_SERVER_DESCR, info.description() );
      addResultInfo( MSG_CONNECT_BACKEND );
      S5Module module = IS5ServerHardConstants.OP_BACKEND_MODULE.getValue( info.params() ).asValobj();
      addResultInfo( "\n" ); //$NON-NLS-1$
      addResultInfo( MSG_CONNECT_VERSION, module.version() );
      addResultInfo( MSG_CONNECT_DEPENDS );
      for( S5Module depend : module.depends() ) {
        addResultInfo( MSG_CONNECT_MODULE, depend.id(), depend.description(), depend.version() );
      }

      // addResultInfo( MSG_SERVER_VERSION, OP_BACKEND_MODULE.getFimbed( info.params() ) );

      /**
       * TODO: упраздняется в пользу {@link ISkBackendInfo}
       */
      // addResultInfo( MSG_CONNECT_SERVER_ID, backendInfo.id() );
      // addResultInfo( MSG_CONNECT_SERVER_NAME, backendInfo.nmName() );
      // addResultInfo( MSG_CONNECT_SERVER_DESCR, backendInfo.description() );
      // addResultInfo( MSG_SERVER_VERSION, backendInfo.module().version() );
      // Команда выполнена успешно
      resultOk( pxConnection );
    }
    catch( RuntimeException e ) {
      // Ошибка подключения к серверу
      String errMsg = e.getLocalizedMessage();
      addResultError( ERR_CONNECT_NO_CONNECTION, (errMsg != null ? errMsg : e.getClass().getName()) );
      resultFail();
      return;
    }
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    return IList.EMPTY;
  }

  @Override
  public void setContext( IAdminCmdContext aContext ) {
    super.setContext( aContext );
    AdminCmdGetConnection.CURRENT_CONNECTION_LISTENER.setContext( aContext );
  }

}
