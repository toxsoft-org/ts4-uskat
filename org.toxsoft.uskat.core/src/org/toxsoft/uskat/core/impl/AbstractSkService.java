package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.devapi.*;

/**
 * {@link ISkService} implementation base.
 * <p>
 * As a non-core {@link ISkService} implementation class must:
 * <ul>
 * <li>be <code>public</code>;</li>
 * <li>have <code>public</code> constructor with only one argument of type {@link IDevCoreApi}.</li>
 * </ul>
 *
 * @author goga
 */
public abstract class AbstractSkService
    implements ISkService {

  private final String    serviceId;
  private final SkCoreApi coreApi;

  private boolean inited        = false;
  private boolean backendActive = false;

  /**
   * Constructor.
   *
   * @param aId String - the service ID
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  protected AbstractSkService( String aId, IDevCoreApi aCoreApi ) {
    serviceId = StridUtils.checkValidIdPath( aId );
    coreApi = SkCoreApi.class.cast( aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // public API
  //

  /**
   * Returns the core API.
   *
   * @return {@link SkCoreApi} - core API
   */
  public SkCoreApi coreApi() {
    return coreApi;
  }

  /**
   * Determines if this is the core service.
   * <p>
   * Core service identifier starts with {@link ISkHardConstants#SK_CORE_SERVICE_ID_PREFIX}.
   *
   * @return boolean <code>true</code> if this is core service
   */
  public boolean isCoreService() {
    return serviceId.startsWith( ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX );
  }

  /**
   * Returns the backend state
   *
   * @return boolean <b>true</b> backend is active; <b>false</b> backend is not active
   */
  public boolean backendState() {
    return backendActive;
  }

  // ------------------------------------------------------------------------------------
  // package API for lifecycle management
  //

  /**
   * Determines if service is is inited state - after {@link #init(ITsContextRo)} and before {@link #close()}.
   *
   * @return boolean - the initialization flag
   */
  final boolean isInited() {
    return inited;
  }

  /**
   * Initializes the service.
   * <p>
   * Service is "usable" only after initialization.
   *
   * @param aArgs {@link ITsContextRo} - initialization arguments (the same as backend init args)
   */
  final void init( ITsContextRo aArgs ) {
    LoggerUtils.defaultLogger().info( FMT_INFO_SERVICE_INIT, serviceId() );
    TsIllegalStateRtException.checkTrue( inited );
    try {
      doInit( aArgs );
      inited = true;
    }
    catch( Exception ex ) {
      LoggerUtils.errorLogger().error( ex );
      throw ex;
    }
  }

  /**
   * Closes the service, finishes work and frees the resources.
   * <p>
   * After closing the service it is not usable.
   */
  final void close() {
    LoggerUtils.defaultLogger().info( FMT_INFO_SERVICE_CLOSE, serviceId() );
    if( !inited ) {
      return;
    }
    try {
      doClose();
    }
    catch( Exception ex ) {
      LoggerUtils.errorLogger().error( ex );
    }
  }

  // ------------------------------------------------------------------------------------
  // package API for communication with other parts
  //

  public void papiOnBackendMessage( GtMessage aMessage ) {

    // TODO AbstractSkService.papiOnBackendMessage()

  }

  // ------------------------------------------------------------------------------------
  // ISkService
  //

  @Override
  public final String serviceId() {
    return serviceId;
  }

  // ------------------------------------------------------------------------------------
  // To implement
  //

  // TODO TRANSLATE

  /**
   * Subclasses must initialize internal data, and became ready to process API method calls.
   *
   * @param aArgs {@link ITsContextRo} - connection opening arguments from {@link ISkConnection#open(ITsContextRo)}
   */
  protected abstract void doInit( ITsContextRo aArgs );

  /**
   * Subclasses must finish jobs, save data, release resources and perform all neccessary clean-ups.
   * <p>
   * Method is called when USkat core is finishing working. Avterf this method service will not be used.
   */
  protected abstract void doClose();

  /**
   * Subclass may process backend state change.
   *
   * @param aActive boolean - the backens activity state
   */
  protected void doWhenBackendStateChanged( boolean aActive ) {
    // nop
  }

  // FIXME нужно ли это в прекрасном БСкат будущего?
  //
  // // ------------------------------------------------------------------------------------
  // // API for subclasses
  // //
  //
  // /**
  // * Check if connecion is active and throw an exception if not.
  // *
  // * @throws TsIllegalStateRtException connection is not active
  // */
  // protected void checkIsOpen() {
  // coreApi.papiCheckIsOpen();
  // }

}
