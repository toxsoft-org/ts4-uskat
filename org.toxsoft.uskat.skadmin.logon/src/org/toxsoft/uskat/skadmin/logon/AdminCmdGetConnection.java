package org.toxsoft.uskat.skadmin.logon;

import static org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;
import static org.toxsoft.uskat.skadmin.logon.AdminLogonUtils.*;
import static org.toxsoft.uskat.skadmin.logon.IAdminHardConstants.*;
import static org.toxsoft.uskat.skadmin.logon.IAdminResources.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.synch.SynchronizedListEdit;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.skadmin.core.*;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

/**
 * Команда администрирования: возвращает соединение по индексу
 *
 * @author mvk
 */
public class AdminCmdGetConnection
    extends AbstractAdminCmd {

  /**
   * Слушатель текущего соединения (установка параметров контекста)
   */
  static final LoginConnectionAdapter CURRENT_CONNECTION_LISTENER = new LoginConnectionAdapter();

  /**
   * Список открытых соединений
   */
  static final IListEdit<ISkConnection> SK_CONNECTIONS = new SynchronizedListEdit<>( new ElemArrayList<>() );

  /**
   * Конструктор
   */
  public AdminCmdGetConnection() {
    // Индекс соединения. Не указан: вывод списка открытых соединений
    addArg( ARG_GET_CONNECTION_INDEX );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_GET_CONNECTION_ID;
  }

  @Override
  public String alias() {
    return CMD_GET_CONNECTION_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_GET_CONNECTION_NAME;
  }

  @Override
  public String description() {
    return CMD_GET_CONNECTION_DESCR;
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
    params.addAll( CTX_SK_CONNECTION, CTX_SK_CORE_API, CTX_SK_CLASS_SERVICE, CTX_SK_OBJECT_SERVICE,
        CTX_SK_LINK_SERVICE );
    IPlexyValue pxServerApi = contextParamValueOrNull( CTX_SK_CORE_API );
    ISkCoreApi coreApi = (pxServerApi != null ? (ISkCoreApi)pxServerApi.singleRef() : null);
    if( coreApi != null ) {
      for( String serviceId : coreApi.services().keys() ) {
        if( !params.hasKey( serviceId ) ) {
          params.add( new IAdminCmdContextParam() {

            @Override
            public String id() {
              return serviceId;
            }

            @Override
            public String nmName() {
              return id();
            }

            @Override
            public String description() {
              return MSG_GET_CONNECTION_EXT_SERVICE;
            }

            @Override
            public IPlexyType type() {
              return ptSingleRef( coreApi.services().getByKey( serviceId ).getClass() );
            }

          } );
        }
      }
    }
    return params;
    // IStridablesListEdit<IAdminCmdContextParam> params = new StridablesList<>();
    // params.addAll( CTX_CONNECTION, CTX_SERVER_API );
    // return params;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @Override
  public void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    IAtomicValue connectionIndex = argSingleValue( ARG_GET_CONNECTION_INDEX );
    if( !connectionIndex.isAssigned() ) {
      // Не указан индекс соединения
      printConnections();
      resultFail();
      return;
    }
    int index = connectionIndex.asInt();
    int count = SK_CONNECTIONS.size();
    if( index < 0 || index >= count ) {
      printConnections();
      // Недопустимый индекс соединения
      if( count == 0 ) {
        addResultError( ERR_GET_CONNECTION_NO_OPENS );
        resultFail();
        return;
      }
      addResultError( ERR_GET_CONNECTION_WRONG_INDEX, Integer.valueOf( index ), Integer.valueOf( count ) );
      resultFail();
      return;
    }
    ISkConnection connection = null;
    if( contextParamReferenceCount( CTX_SK_CONNECTION ) != 0 ) {
      // В контексте только одна ссылка на текущее соединение. Завершаем его
      connection = (ISkConnection)contextParamValue( CTX_SK_CONNECTION ).singleRef();
      // Удаляем своего слушателя соединения
      connection.removeConnectionListener( CURRENT_CONNECTION_LISTENER );
    }
    connection = SK_CONNECTIONS.get( index );
    connection.addConnectionListener( CURRENT_CONNECTION_LISTENER );
    ISkCoreApi coreApi = connection.coreApi();
    // Размещаем соединение в контексте, чтобы на него могли зарегистрироваться слушатели
    IPlexyValue pxConnection = pvSingleRef( connection );
    setContextParamValue( CTX_SK_CONNECTION, pxConnection );
    setContextParamValue( CTX_SK_CORE_API, pvSingleRef( coreApi ) );
    for( String serviceId : coreApi.services().keys() ) {
      setContextParamValue( serviceId, pvSingleRef( coreApi.services().getByKey( serviceId ) ) );
    }
    printConnections();
    resultOk( pxConnection );
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    return IList.EMPTY;
  }

  @Override
  public void setContext( IAdminCmdContext aContext ) {
    super.setContext( aContext );
    CURRENT_CONNECTION_LISTENER.setContext( aContext );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Печать списка открытых соединений
   */
  @SuppressWarnings( "nls" )
  private void printConnections() {
    ISkConnection currentConnection = null;
    if( contextParamReferenceCount( CTX_SK_CONNECTION ) != 0 ) {
      // В контексте только одна ссылка на текущее соединение. Завершаем его
      currentConnection = (ISkConnection)contextParamValue( CTX_SK_CONNECTION ).singleRef();
    }
    IList<ISkConnection> connections = new ElemArrayList<>( SK_CONNECTIONS );
    addResultInfo( MSG_GET_CONNECTION_CONNECTIONS, Integer.valueOf( connections.size() ) );
    for( int index = 0, n = connections.size(); index < n; index++ ) {
      ISkConnection connection = connections.get( index );
      String isCurrent = (connection.equals( currentConnection ) ? "*" : " ");
      addResultInfo( MSG_GET_CONNECTION_CONNECTION, isCurrent, Integer.valueOf( index ),
          connectionToString( connections.get( index ).backendInfo() ) );
    }
  }

  /**
   * Адаптер команды для s5-соединения
   */
  static final class LoginConnectionAdapter
      implements ISkConnectionListener {

    IAdminCmdContext context;

    /**
     * Констурктор
     */
    LoginConnectionAdapter() {
    }

    // ------------------------------------------------------------------------------------
    // Методы пакета
    //
    /**
     * Установка текущего контекста
     *
     * @param aContext {@link IAdminCmdContext} текущий контекст
     * @throws TsNullArgumentRtException аргумент =null
     */
    void setContext( IAdminCmdContext aContext ) {
      TsNullArgumentRtException.checkNull( aContext );
      context = aContext;
    }

    // ------------------------------------------------------------------------------------
    // Переопределение S5ConnectionListenerAdapter
    //
    @Override
    public void onSkConnectionStateChanged( ISkConnection aSource, ESkConnState aOldState ) {
      // public void onAfterActivate( IS5Connection aSource ) {
      if( aSource.state() != ESkConnState.ACTIVE ) {
        return;
      }
      // Клиентская реализация API сервера
      ISkCoreApi coreApi = aSource.coreApi();
      // Размещаем в контексте "известные" параметры (в режиме только чтение)
      setReadOnlyContextParamValue( CTX_SK_CORE_API, pvSingleRef( coreApi ) );
      for( String serviceId : coreApi.services().keys() ) {
        setReadOnlyContextParamValue( serviceId, pvSingleRef( coreApi.services().getByKey( serviceId ) ) );
      }
      // setReadOnlyContextParamValue( CTX_MESSAGE_SERVICE, pvSingleRef( serverApi.serverMessageService() ) );
      // setReadOnlyContextParamValue( CTX_CLASS_SERVICE, pvSingleRef( serverApi.classService() ) );
      // setReadOnlyContextParamValue( CTX_OBJECT_SERVICE, pvSingleRef( serverApi.objectService() ) );
      // setReadOnlyContextParamValue( CTX_LINK_SERVICE, pvSingleRef( serverApi.linkService() ) );
      // setReadOnlyContextParamValue( CTX_REFBOOK_SERVICE, pvSingleRef( serverApi.refbookService() ) );
      // setReadOnlyContextParamValue( CTX_USER_SERVICE, pvSingleRef( serverApi.userService() ) );
      // setReadOnlyContextParamValue( CTX_USER_PREFS_SERVICE, pvSingleRef( serverApi.prefsService() ) );
      // setReadOnlyContextParamValue( CTX_CURRDATA_SERVICE, pvSingleRef( serverApi.currDataService() ) );
      // setReadOnlyContextParamValue( CTX_HISTDATA_SERVICE, pvSingleRef( serverApi.histDataService() ) );
      // setReadOnlyContextParamValue( CTX_COMMAND_SERVICE, pvSingleRef( serverApi.commandService() ) );
      // setReadOnlyContextParamValue( CTX_EVENT_SERVICE, pvSingleRef( serverApi.eventService() ) );
    }

    // ------------------------------------------------------------------------------------
    // Внутренние методы
    //
    /**
     * Устанавливает значение параметра контекста только на чтение.
     * <p>
     * Если параметр контекст до это существовал, то производится его удаление
     *
     * @param aParamId {@link IStridable} идентификатор параметра
     * @param aValue {@link IPlexyValue} значение параметра
     */
    private void setReadOnlyContextParamValue( IStridable aParamId, IPlexyValue aValue ) {
      TsNullArgumentRtException.checkNulls( aParamId, aValue );
      setReadOnlyContextParamValue( aParamId.id(), aValue );
    }

    /**
     * Устанавливает значение параметра контекста только на чтение.
     * <p>
     * Если параметр контекст до это существовал, то производится его удаление
     *
     * @param aParamId String идентификатор параметра
     * @param aValue {@link IPlexyValue} значение параметра
     */
    private void setReadOnlyContextParamValue( String aParamId, IPlexyValue aValue ) {
      TsNullArgumentRtException.checkNulls( aParamId, aValue );
      if( context.hasParam( aParamId ) ) {
        context.removeParam( aParamId );
      }
      context.setParamValue( aParamId, aValue, true );
    }
  }
}
