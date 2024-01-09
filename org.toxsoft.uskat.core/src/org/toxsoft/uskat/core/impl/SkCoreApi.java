package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkCoreConfigConstants.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.validator.impl.TsValidationFailedRtException;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.derivative.*;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.api.ISkService;
import org.toxsoft.uskat.core.api.clobserv.ISkClobService;
import org.toxsoft.uskat.core.api.cmdserv.ISkCommandService;
import org.toxsoft.uskat.core.api.evserv.ISkEventService;
import org.toxsoft.uskat.core.api.gwids.ISkGwidService;
import org.toxsoft.uskat.core.api.hqserv.ISkHistoryQueryService;
import org.toxsoft.uskat.core.api.linkserv.ISkLinkService;
import org.toxsoft.uskat.core.api.objserv.ISkObjectService;
import org.toxsoft.uskat.core.api.rtdserv.ISkRtdataService;
import org.toxsoft.uskat.core.api.sysdescr.ISkSysdescr;
import org.toxsoft.uskat.core.api.users.ISkLoggedUserInfo;
import org.toxsoft.uskat.core.api.users.ISkUserService;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.BackendMsgActiveChanged;
import org.toxsoft.uskat.core.connection.ESkConnState;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.core.devapi.ICoreL10n;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;
import org.toxsoft.uskat.core.devapi.gwiddb.ISkGwidDbService;

import core.tslib.bricks.synchronize.ITsThreadExecutor;

/**
 * An {@link ISkCoreApi} and {@link IDevCoreApi} implementation.
 *
 * @author hazard157
 */
public class SkCoreApi
    implements IDevCoreApi, ISkFrontendRear, ICloseable {

  private final IStringMapEdit<AbstractSkService> servicesMap = new StringMap<>();

  private final ITsContextRo      openArgs;
  private final SkConnection      conn;
  private final ITsThreadExecutor executor;
  private final CoreL10n          coreL10n;
  private final CoreLogger        logger;
  private final ISkBackend        backend;

  private final SkCoreServSysdescr         sysdescr;
  private final SkCoreServObject           objService;
  private final SkCoreServClobs            clobService;
  private final SkCoreServCommands         cmdService;
  private final SkCoreServEvents           eventService;
  private final SkCoreServLinks            linkService;
  private final SkCoreServRtdata           rtdService;
  private final SkCoreServHistQueryService hqService;
  private final SkCoreServUsers            userService;
  private final SkCoreServGwids            gwidService;
  private final SkCoreServGwidDb           gwidDbService;

  /**
   * Queue of messages from the backend.
   * <p>
   * When backend calls {@link ISkFrontendRear#onBackendMessage(GtMessage)}, message is put in thes queue and execution
   * immediately returns to the backend. Messages are taken from the queue in {@link SkCoreApi#doJobInCoreMainThread()}
   * method and dispatched to the services. <br>
   * Queue-oriented message handling has foloowing motivation:
   * <ul>
   * <li>Many services need to fire event for Core API users about changes it does. Change events are generated by the
   * backend in API methods calls. However generally backend user needs to do some houskeeping job <i>after</i> database
   * update but <i>before</i> the change message is delivered to the listeners. Using queue allows Sk-service
   * implementations to call backend API methods several times and remain sure that message will be send to listeners at
   * the end of core service method, when user explicitly calls {@link SkCoreApi#doJobInCoreMainThread()};</li>
   * <li>Some backends have their own execution threads. While calling such backend's API is thread safe, reverse call
   * to the {@link ISkFrontendRear#onBackendMessage(GtMessage)} must be also thread-safe. For such backends (marked by
   * {@link ISkBackendHardConstant#OPDEF_SKBI_NEED_THREAD_SAFE_FRONTEND} option) the queue implementation will be
   * wrapped in the thread-safe {@link SynchronizedQueueWrapper}.</li>
   * </ul>
   */
  private final IQueue<GtMessage> backendMessageQueue;

  /**
   * Initialization flag.
   */
  private boolean inited = false;

  /**
   * Constructor.
   *
   * @param aArgs {@link ITsContextRo} - connection opening args from {@link ISkConnection#open(ITsContextRo)}
   * @param aConn {@link SkConnection} - connection which creates core API
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  SkCoreApi( ITsContextRo aArgs, SkConnection aConn ) {
    TsNullArgumentRtException.checkNulls( aArgs, aConn );
    logger = new CoreLogger( LoggerUtils.defaultLogger(), aArgs );
    openArgs = aArgs;
    conn = aConn;
    executor = REFDEF_THREAD_EXECUTOR.getRef( aArgs );
    coreL10n = new CoreL10n( aArgs );
    // create backend
    ISkBackendProvider bp = REFDEF_BACKEND_PROVIDER.getRef( aArgs );
    TsValidationFailedRtException.checkError( bp.getMetaInfo().checkArguments( aArgs ) );
    backend = bp.createBackend( this, aArgs );
    // if needed create thread-safe queue
    if( ISkBackendHardConstant.OPDEF_SKBI_NEED_THREAD_SAFE_FRONTEND.getValue( backend.getBackendInfo().params() )
        .asBool() ) {
      backendMessageQueue = new SynchronizedQueueWrapper<>( new Queue<>() );
    }
    else {
      backendMessageQueue = new Queue<>();
    }
    backend.initialize();
    // prepare services to be created
    IListEdit<ISkServiceCreator<? extends AbstractSkService>> llCreators = new ElemArrayList<>( 100, false );
    // mandatory built-in services
    llCreators.add( SkCoreServSysdescr.CREATOR );
    llCreators.add( SkCoreServObject.CREATOR );
    llCreators.add( SkCoreServClobs.CREATOR );
    llCreators.add( SkCoreServCommands.CREATOR );
    llCreators.add( SkCoreServEvents.CREATOR );
    llCreators.add( SkCoreServLinks.CREATOR );
    llCreators.add( SkCoreServRtdata.CREATOR );
    llCreators.add( SkCoreServHistQueryService.CREATOR );
    llCreators.add( SkCoreServUsers.CREATOR );
    llCreators.add( SkCoreServGwids.CREATOR );
    llCreators.add( SkCoreServGwidDb.CREATOR );
    // backend and user-specified services
    llCreators.addAll( backend.listBackendServicesCreators() );
    llCreators.addAll( SkCoreUtils.listRegisteredSkServiceCreators() );
    // thread separator service
    llCreators.add( SkThreadExecutorService.CREATOR );

    // fill map of the services
    executor.syncExec( () -> {
      for( ISkServiceCreator<? extends AbstractSkService> c : llCreators ) {
        AbstractSkService s;
        try {
          s = c.createService( this );
        }
        catch( Exception ex ) {
          logger().error( ex );
          throw new TsInternalErrorRtException( ex, FMT_ERR_CANT_CREATE_SERVICE, c.getClass().getName() );
        }
        if( servicesMap.hasKey( s.serviceId() ) ) {
          RuntimeException ex =
              new TsItemAlreadyExistsRtException( FMT_ERR_DUP_SERVICE_ID, c.getClass().getName(), s.serviceId() );
          logger().error( ex );
          throw ex;
        }
        servicesMap.put( s.serviceId(), s );
      }
    } );
    // init mandatory service refs
    sysdescr = getService( ISkSysdescr.SERVICE_ID );
    objService = getService( ISkObjectService.SERVICE_ID );
    clobService = getService( ISkClobService.SERVICE_ID );
    cmdService = getService( ISkCommandService.SERVICE_ID );
    eventService = getService( ISkEventService.SERVICE_ID );
    linkService = getService( ISkLinkService.SERVICE_ID );
    rtdService = getService( ISkRtdataService.SERVICE_ID );
    hqService = getService( ISkHistoryQueryService.SERVICE_ID );
    userService = getService( ISkUserService.SERVICE_ID );
    gwidService = getService( ISkGwidService.SERVICE_ID );
    gwidDbService = getService( ISkGwidDbService.SERVICE_ID );
    // initialize services
    executor.syncExec( () -> {
      for( int i = 0; i < servicesMap.size(); i++ ) {
        AbstractSkService s = servicesMap.values().get( i );
        internalInitService( s );
      }
    } );
    inited = true;
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private CoreLogger logger() {
    return logger;
  }

  private <S extends AbstractSkService> S internalInitService( S aService ) {
    try {
      aService.init( openArgs );
    }
    catch( Exception ex ) {
      logger().error( ex );
      if( aService.isCoreService() ) {
        throw ex;
      }
    }
    return aService;
  }

  // ------------------------------------------------------------------------------------
  // API for service implementations
  //

  /**
   * Check if connection is open and throws an exception if not.
   *
   * @throws TsIllegalStateRtException connection is not open but may be inactive
   */
  public void papiCheckIsOpen() {
    // check also openArgs!=null, means that CoreAPI is in initalization state (inside constructor)
    if( conn.state() == ESkConnState.CLOSED && openArgs == null ) {
      throw new TsIllegalStateRtException( MSG_ERR_CONN_NOT_OPEN );
    }
  }

  /**
   * Returns the backend.
   *
   * @return {@link ISkBackend} - the backend
   */
  public ISkBackend backend() {
    return backend;
  }

  // ------------------------------------------------------------------------------------
  // ISkCoreApi
  //

  @Override
  public ISkSysdescr sysdescr() {
    return sysdescr;
  }

  @Override
  public ISkObjectService objService() {
    return objService;
  }

  @Override
  public ISkClobService clobService() {
    return clobService;
  }

  @Override
  public ISkCommandService cmdService() {
    return cmdService;
  }

  @Override
  public ISkEventService eventService() {
    return eventService;
  }

  @Override
  public ISkLinkService linkService() {
    return linkService;
  }

  @Override
  public ISkRtdataService rtdService() {
    return rtdService;
  }

  @Override
  public ISkHistoryQueryService hqService() {
    return hqService;
  }

  @Override
  public ISkUserService userService() {
    return userService;
  }

  @Override
  public ISkGwidService gwidService() {
    return gwidService;
  }

  @Override
  public ISkGwidDbService gwidDbService() {
    return gwidDbService;
  }

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  @Override
  public IStringMap<ISkService> services() {
    return (IStringMap)servicesMap;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  final public <S extends ISkService> S getService( String aServiceId ) {
    return (S)servicesMap.getByKey( aServiceId );
  }

  @Override
  public <S extends AbstractSkService> S addService( ISkServiceCreator<S> aCreator ) {
    TsNullArgumentRtException.checkNull( aCreator );
    TsIllegalStateRtException.checkFalse( inited );
    S s = aCreator.createService( this );
    TsItemAlreadyExistsRtException.checkTrue( servicesMap.hasKey( s.serviceId() ) );
    internalInitService( s );
    servicesMap.put( s.serviceId(), s );
    return s;
  }

  @Override
  public ISkLoggedUserInfo getCurrentUserInfo() {
    return ISkBackendHardConstant.OPDEF_SKBI_LOGGED_USER.getValue( backend.getBackendInfo().params() ).asValobj();
  }

  // ------------------------------------------------------------------------------------
  // ISkFrontendRear
  //

  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    backendMessageQueue.putTail( aMessage );
  }

  // ------------------------------------------------------------------------------------
  // IDevCoreApi
  //

  @Override
  public <T> T findBackendAddon( String aAddonId, Class<T> aExpectedType ) {
    return backend().findBackendAddon( aAddonId, aExpectedType );
  }

  @Override
  public ICoreL10n l10n() {
    return coreL10n;
  }

  @Override
  public ITsContextRo openArgs() {
    return openArgs;
  }

  @Override
  public String determineClassClaimingServiceId( String aClassId ) {
    TsNullArgumentRtException.checkNull( aClassId );
    for( AbstractSkService s : servicesMap ) {
      if( s.isClassClaimedByService( aClassId ) ) {
        return s.serviceId();
      }
    }
    return ISkSysdescr.SERVICE_ID;
  }

  @Override
  public ITsThreadExecutor executor() {
    return executor;
  }

  @Override
  public void doJobInCoreMainThread() {
    if( !inited ) {
      // API еще не готово к работе
      return;
    }
    GtMessage msg;
    while( (msg = backendMessageQueue.getHeadOrNull()) != null ) {
      if( BackendMsgActiveChanged.INSTANCE.isOwnMessage( msg ) ) {
        boolean isActive = BackendMsgActiveChanged.INSTANCE.getActive( msg );
        conn.changeState( isActive ? ESkConnState.ACTIVE : ESkConnState.INACTIVE );
        for( int i = servicesMap.size() - 1; i >= 0; i-- ) {
          try {
            AbstractSkService s = servicesMap.values().get( i );
            s.onBackendActiveStateChanged( isActive );
          }
          catch( Exception ex ) {
            logger().error( ex );
          }
        }
        continue;
      }
      AbstractSkService s = servicesMap.findByKey( msg.topicId() );
      if( s != null ) {
        s.papiOnBackendMessage( msg );
      }
      else {
        logger().warning( LOG_WARN_UNHANDLED_BACKEND_MESSAGE, msg.topicId(), msg.messageId(),
            msg.args() == IOptionSet.NULL ? "IOptionSet.NULL" : msg.args() ); //$NON-NLS-1$
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //

  @Override
  public void close() {
    if( !inited ) {
      return;
    }
    for( int i = servicesMap.size() - 1; i >= 0; i-- ) {
      try {
        AbstractSkService s = servicesMap.values().get( i );
        s.close();
      }
      catch( Exception ex ) {
        logger().error( ex );
      }
    }
    // backend closes after services were closed
    backend.close();
    backendMessageQueue.clear();
    servicesMap.clear();
    inited = false;
  }

}
