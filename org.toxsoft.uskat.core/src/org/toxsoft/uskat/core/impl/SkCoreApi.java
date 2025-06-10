package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkCoreConfigConstants.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.clobserv.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.gwids.*;
import org.toxsoft.uskat.core.api.hqserv.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.ugwis.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.devapi.gwiddb.*;
import org.toxsoft.uskat.core.impl.dto.*;

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

  private final SkCoreServSysdescr  sysdescr;
  private final SkCoreServObject    objService;
  private final SkCoreServClobs     clobService;
  private final SkCoreServCommands  cmdService;
  private final SkCoreServEvents    eventService;
  private final SkCoreServLinks     linkService;
  private final SkCoreServRtdata    rtdService;
  private final SkCoreServHistQuery hqService;
  private final SkCoreServUsers     userService;
  private final SkCoreServGwids     gwidService;
  private final SkCoreServUgwis     ugwiService;
  private final SkCoreServGwidDb    gwidDbService;

  private final IList<ISkCoreExternalHandler> coreApiHandlersList;

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
    coreApiHandlersList = new ElemArrayList<>( SkCoreUtils.listRegisteredCoreApiHandlers() );
    // create backend
    ISkBackendProvider bp = REFDEF_BACKEND_PROVIDER.getRef( aArgs );
    TsValidationFailedRtException.checkError( bp.getMetaInfo().checkArguments( aArgs ) );
    backend = bp.createBackend( this, aArgs );
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
    llCreators.add( SkCoreServHistQuery.CREATOR );
    llCreators.add( SkCoreServUsers.CREATOR );
    llCreators.add( SkCoreServGwids.CREATOR );
    llCreators.add( SkCoreServUgwis.CREATOR );
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
    ugwiService = getService( ISkUgwiService.SERVICE_ID );
    gwidDbService = getService( ISkGwidDbService.SERVICE_ID );
    // initialize services
    executor.syncExec( () -> {
      for( int i = 0; i < servicesMap.size(); i++ ) {
        AbstractSkService s = servicesMap.values().get( i );
        internalInitService( s );
      }
    } );

    inited = true;

    // process external handlers in direct order
    executor.syncExec( () -> {
      for( int i = 0, n = coreApiHandlersList.size(); i < n; i++ ) {
        ISkCoreExternalHandler h = coreApiHandlersList.get( i );
        try {
          h.processSkCoreInitialization( this );
        }
        catch( Exception ex ) {
          LoggerUtils.errorLogger().error( ex );
        }
      }
    } );
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

  private void callExternalBackendActivityChangeHandlers( boolean aBackendActive ) {
    for( int i = 0, n = coreApiHandlersList.size(); i < n; i++ ) {
      ISkCoreExternalHandler h = coreApiHandlersList.get( i );
      try {
        h.processSkBackendActiveStateChange( this, aBackendActive );
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }
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
  public ISkUgwiService ugwiService() {
    return ugwiService;
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
  final public <S extends ISkService> S findService( String aServiceId ) {
    return (S)servicesMap.findByKey( aServiceId );
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
    // 2024-04-07 mvk gateway local connection open error
    // executor.syncExec( () -> {
    executor.asyncExec( () -> {
      if( BackendMsgActiveChanged.INSTANCE.isOwnMessage( aMessage ) ) {
        if( !inited ) {
          return;
        }
        boolean isActive = BackendMsgActiveChanged.INSTANCE.getActive( aMessage );
        ESkConnState oldState = conn.state();
        if( !conn.changeState( isActive ? ESkConnState.ACTIVE : ESkConnState.INACTIVE ) ) {
          // state was not changed
          return;
        }
        for( int i = servicesMap.size() - 1; i >= 0; i-- ) {
          try {
            AbstractSkService s1 = servicesMap.values().get( i );
            s1.onBackendActiveStateChanged( isActive );
          }
          catch( Exception ex ) {
            logger().error( ex );
          }
        }
        // process external handlers
        callExternalBackendActivityChangeHandlers( isActive );
        // client notifications
        conn.stateChanged( oldState );
        return;
      }
      AbstractSkService s2 = servicesMap.findByKey( aMessage.topicId() );
      if( s2 != null ) {
        s2.papiOnBackendMessage( aMessage );
      }
      else {
        logger().warning( LOG_WARN_UNHANDLED_BACKEND_MESSAGE, aMessage.topicId(), aMessage.messageId(),
            aMessage.args() == IOptionSet.NULL ? "IOptionSet.NULL" : aMessage.args() ); //$NON-NLS-1$
      }
    } );
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
  public ISkConnection skConn() {
    return conn;
  }

  @Override
  public ITsThreadExecutor executor() {
    return executor;
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //

  @Override
  public void close() {
    if( !inited ) {
      return;
    }
    // process external handlers in reverse order
    for( int n = coreApiHandlersList.size(), i = n - 1; i >= 0; i-- ) {
      ISkCoreExternalHandler h = coreApiHandlersList.get( i );
      try {
        h.processSkCoreShutdown( this );
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }
    // close services
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
    servicesMap.clear();
    inited = false;
  }

}
