package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
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

  private final String     serviceId;
  private final SkCoreApi  coreApi;
  private final CoreLogger logger;

  private boolean inited        = false;
  private boolean backendActive = false;

  /**
   * Constructor for subclasses.
   * <p>
   * Implementation notes: subclasses must not initilize it's content in constructor. All initialization staff must be
   * performed in {@link #doInit(ITsContextRo)} method.
   *
   * @param aId String - the service ID
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  protected AbstractSkService( String aId, IDevCoreApi aCoreApi ) {
    serviceId = StridUtils.checkValidIdPath( aId );
    coreApi = SkCoreApi.class.cast( aCoreApi );
    logger = new CoreLogger( LoggerUtils.defaultLogger(), aCoreApi.openArgs() );
  }

  // ------------------------------------------------------------------------------------
  // API for subclasses
  //

  /**
   * Determines if this is the core service.
   * <p>
   * Core service identifier starts with {@link ISkHardConstants#SK_CORE_SERVICE_ID_PREFIX}.
   *
   * @return boolean <code>true</code> if this is core service
   */
  final public boolean isCoreService() {
    return serviceId.startsWith( ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX );
  }

  /**
   * Returns the backend state
   *
   * @return boolean <b>true</b> backend is active; <b>false</b> backend is not active
   */
  final public boolean backendState() {
    return backendActive;
  }

  /**
   * Returns the inidividual logger for this service.
   *
   * @return {@link CoreLogger} - service logger
   */
  final public CoreLogger logger() {
    return logger;
  }

  // ------------------------------------------------------------------------------------
  // package API
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
    logger().info( FMT_INFO_SERVICE_INIT, serviceId() );
    TsIllegalStateRtException.checkTrue( inited );
    try {
      doInit( aArgs );
      inited = true;
    }
    catch( Exception ex ) {
      logger().error( ex );
      throw ex;
    }
  }

  /**
   * Closes the service, finishes work and frees the resources.
   * <p>
   * After closing the service it is not usable.
   */
  final void close() {
    logger().info( FMT_INFO_SERVICE_CLOSE, serviceId() );
    if( !inited ) {
      return;
    }
    try {
      doClose();
    }
    catch( Exception ex ) {
      logger().error( ex );
    }
  }

  /**
   * Handles mesaage from backend.
   * <p>
   * Method simple checks if topic is of this service and passes message to the implementation as
   * {@link GenericMessage}, that is the message without topic.
   *
   * @param aMessage {@link GtMessage} - the message from backend
   */
  public final void papiOnBackendMessage( GtMessage aMessage ) {
    if( !aMessage.topicId().equals( serviceId ) ) {
      logger().warning( FMT_WARN_INV_SERVICE_GT_MSG, serviceId, aMessage.topicId() );
      return;
    }
    if( !onBackendMessage( aMessage ) ) {
      logger().warning( FMT_WARN_UNKNOWN_MSG, serviceId, aMessage.messageId() );
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkService
  //

  @Override
  public final String serviceId() {
    return serviceId;
  }

  @Override
  final public SkCoreApi coreApi() {
    return coreApi;
  }

  // ------------------------------------------------------------------------------------
  // API for descendants
  //

  @SuppressWarnings( "javadoc" )
  public ISkSysdescr sysdescr() {
    return coreApi.sysdescr();
  }

  @SuppressWarnings( "javadoc" )
  public ISkObjectService objServ() {
    return coreApi.objService();
  }

  @SuppressWarnings( "javadoc" )
  public ISkClobService clobService() {
    return coreApi.clobService();
  }

  @SuppressWarnings( "javadoc" )
  public ISkCommandService cmdService() {
    return coreApi.cmdService();
  }

  @SuppressWarnings( "javadoc" )
  public ISkEventService eventService() {
    return coreApi.eventService();
  }

  @SuppressWarnings( "javadoc" )
  public ISkLinkService linkService() {
    return coreApi.linkService();
  }

  @SuppressWarnings( "javadoc" )
  public ISkRtdataService rtdService() {
    return coreApi.rtdService();
  }

  // ------------------------------------------------------------------------------------
  // To implement
  //

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
   * Subclass may handle message from the backend.
   * <p>
   * Base inmplementation simply returns <code>false</code>. When overridoing there is no need to call superclass
   * method.
   * <p>
   * If method returns <code>false</code> caller base class will log an "unhandled message" warning.
   *
   * @param aMessage {@link GenericMessage} - message from the backend
   * @return boolean - <code>true</code> = marks message as handled
   */
  protected boolean onBackendMessage( GenericMessage aMessage ) {
    return false;
  }

  /**
   * Subclass may process backend state change.
   *
   * @param aActive boolean - the backens activity state
   */
  protected void doWhenBackendStateChanged( boolean aActive ) {
    // nop
  }

}
