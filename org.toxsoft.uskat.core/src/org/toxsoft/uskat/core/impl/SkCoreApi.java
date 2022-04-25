package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkCoreConfigConstants.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.objserv.*;
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

  private final InternalCoreListenerEventer       coreEventer;
  private final IStringMapEdit<AbstractSkService> servMap = new StringMap<>();

  private final ITsContextRo    openArgs;
  private final SkConnection    conn;
  private final CoreL10n        coreL10n;
  private final ISkFrontendRear frontendForBackend;
  private final ISkBackend      backend;

  private final SkSysdescr      sysdescr;
  private final SkObjectService objService;

  private final IStringMapEdit<AbstractSkService> servicesMap = new StringMap<>();

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
    coreEventer = new InternalCoreListenerEventer( this );
    openArgs = aArgs;
    conn = aConn;
    coreL10n = new CoreL10n( aArgs );
    // create backend
    frontendForBackend = createAndInitFrontend();
    ISkBackendProvider bp = REFDEF_BACKEND_PROVIDER.getRef( aArgs );
    backend = bp.createBackend( frontendForBackend, aArgs );
    // TODO mandatory services
    sysdescr = new SkSysdescr( this );
    servMap.put( sysdescr().serviceId(), sysdescr );
    objService = new SkObjectService( this );
    servMap.put( objService.serviceId(), objService );
    // TODO realtime services - real or stubs

    // TODO backend-provided services

    // TODO user-provided services

    // TODO check if backend requires thread separator and it s present

    // init services
    internalInitServices();
    inited = true;
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Creates and returnes an {@link ISkFrontendRear} implementation.
   * <p>
   * Depending on connection opening argument FIXME ??? creates either single-threaded or thread-safe implementation.
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

  private void internalInitServices() {
    for( int i = 0; i < servMap.size(); i++ ) {
      AbstractSkService s = servMap.values().get( i );
      try {
        s.init( openArgs );
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
        if( s.isCoreService() ) {
          throw ex;
        }
      }
    }
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

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  @Override
  public IStringMap<ISkService> services() {
    return (IStringMap)servMap;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public <S extends ISkService> S getService( String aServiceId ) {
    return (S)servMap.getByKey( aServiceId );
  }

  @Override
  public <S extends AbstractSkService> S addService( ISkServiceCreator<S> aCreator ) {
    // TODO реализовать SkCoreApi.addService()
    throw new TsUnderDevelopmentRtException( "SkCoreApi.addService()" );
  }

  @Override
  public ITsEventer<ISkCoreListener> eventer() {
    return coreEventer;
  }

  @Override
  public void fireCoreEvent( SkCoreEvent aSkCoreEvent ) {
    coreEventer.fireCoreEvent( aSkCoreEvent );
  }

  @Override
  public ICoreL10n l10n() {
    return coreL10n;
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
      LoggerUtils.errorLogger().warning( LOG_WARN_UNHANDLED_BACKEND_MESSAGE, aMessage.topicId() );
    }
  }

  // ------------------------------------------------------------------------------------
  // IDevCoreApi
  //

  @Override
  public <T> T getBackendAddon( Class<T> aAddonInterface ) {
    // TODO реализовать SkCoreApi.getBackendAddon()
    throw new TsUnderDevelopmentRtException( "SkCoreApi.getBackendAddon()" );
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //

  @Override
  public void close() {
    if( !inited ) {
      return;
    }
    for( int i = servMap.size() - 1; i >= 0; i-- ) {
      try {
        AbstractSkService s = servMap.values().get( i );
        s.close();
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }
    // backend closes after services were closed
    backend.close();
    servMap.clear();
    inited = false;
  }

}
