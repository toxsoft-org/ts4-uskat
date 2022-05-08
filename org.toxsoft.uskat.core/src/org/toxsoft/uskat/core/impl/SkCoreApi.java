package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkCoreConfigConstants.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
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
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.devapi.*;

/**
 * An {@link ISkCoreApi} and {@link IDevCoreApi} implementation.
 *
 * @author hazard157
 */
public class SkCoreApi
    implements IDevCoreApi, ISkFrontendRear, ICloseable {

  private final IStringMapEdit<AbstractSkService> servicesMap = new StringMap<>();

  private final ITsContextRo    openArgs;
  private final SkConnection    conn;
  private final CoreL10n        coreL10n;
  private final CoreLogger      logger;
  private final ISkFrontendRear frontendForBackend;
  private final ISkBackend      backend;

  private final SkCoreServSysdescr sysdescr;
  private final SkCoreServObject   objService;
  private final SkCoreServClobs    clobService;
  private final SkCoreServCommands cmdService;
  private final SkCoreServEvents   eventService;
  private final SkCoreServLinks    linkService;
  private final SkCoreServRtdata   rtdService;

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
    coreL10n = new CoreL10n( aArgs );
    // create backend
    frontendForBackend = createAndInitFrontend();
    ISkBackendProvider bp = REFDEF_BACKEND_PROVIDER.getRef( aArgs );
    backend = bp.createBackend( frontendForBackend, aArgs );
    // prepare services to be created
    IListEdit<ISkServiceCreator<? extends AbstractSkService>> llCreators = new ElemArrayList<>();
    // mandatory built-in services
    llCreators.add( SkCoreServSysdescr.CREATOR );
    llCreators.add( SkCoreServObject.CREATOR );
    llCreators.add( SkCoreServClobs.CREATOR );
    llCreators.add( SkCoreServCommands.CREATOR );
    llCreators.add( SkCoreServEvents.CREATOR );
    llCreators.add( SkCoreServLinks.CREATOR );
    llCreators.add( SkCoreServRtdata.CREATOR );
    // backend and user-specified services
    llCreators.addAll( backend.listBackendServicesCreators() );
    IList<ISkServiceCreator<? extends AbstractSkService>> llUser = REFDEF_USER_SERVICES.getRef( aArgs, IList.EMPTY );
    llCreators.addAll( llUser );
    // fill map of the services
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
        try {
          s.close();
        }
        catch( Exception ex1 ) {
          logger().error( ex1 );
        }
        throw ex;
      }
      servicesMap.put( s.serviceId(), s );
    }
    // init mandatory service refs
    sysdescr = getService( ISkSysdescr.SERVICE_ID );
    objService = getService( ISkObjectService.SERVICE_ID );
    clobService = getService( ISkClobService.SERVICE_ID );
    cmdService = getService( ISkCommandService.SERVICE_ID );
    eventService = getService( ISkEventService.SERVICE_ID );
    linkService = getService( ISkLinkService.SERVICE_ID );
    rtdService = getService( ISkRtdataService.SERVICE_ID );
    // initialize services
    for( int i = 0; i < servicesMap.size(); i++ ) {
      AbstractSkService s = servicesMap.values().get( i );
      try {
        s.init( openArgs );
      }
      catch( Exception ex ) {
        logger().error( ex );
        if( s.isCoreService() ) {
          throw ex;
        }
      }
    }
    inited = true;
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Creates and returnes an {@link ISkFrontendRear} implementation.
   * <p>
   * Depending on connection opening argument {@link ISkCoreConfigConstants#REFDEF_BACKEND_THREAD_SEPARATOR} creates
   * either single-threaded or thread-safe implementation.
   *
   * @return {@link ISkFrontendRear} - created instance of fromtend read
   */
  private ISkFrontendRear createAndInitFrontend() {
    ISkFrontendRear singleThreadFrontend = this; // this class itself is the single-threaded frontend
    SkBackendThreadSeparator separator =
        ISkCoreConfigConstants.REFDEF_BACKEND_THREAD_SEPARATOR.getRef( openArgs, null );
    if( separator == null ) {
      return singleThreadFrontend;
    }
    // create and return thread-separated adapter
    SkFrontendRearThreadSeparatedWrapper separatedFrontend =
        new SkFrontendRearThreadSeparatedWrapper( singleThreadFrontend );
    separator.addDoJobTask( separatedFrontend );
    return separatedFrontend;
  }

  private CoreLogger logger() {
    return logger;
  }

  // ------------------------------------------------------------------------------------
  // Package API
  //

  /**
   * Check if connection is open and throws an exception if not.
   *
   * @throws TsIllegalStateRtException connection is not open but may be inactive
   */
  void papiCheckIsOpen() {
    if( conn.state() == ESkConnState.CLOSED ) {
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
    try {
      s.init( openArgs );
      servicesMap.put( s.serviceId(), s );
    }
    catch( Exception ex ) {
      logger().error( ex );
      throw ex;
    }
    return s;
  }

  // ------------------------------------------------------------------------------------
  // ISkFrontendRear
  //

  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    AbstractSkService s = servicesMap.findByKey( aMessage.topicId() );
    if( s != null ) {
      s.papiOnBackendMessage( aMessage );
    }
    else {
      logger().warning( LOG_WARN_UNHANDLED_BACKEND_MESSAGE, aMessage.topicId() );
    }
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
    servicesMap.clear();
    inited = false;
  }

}
