package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;
import org.toxsoft.uskat.core.ISkHardConstants;
import org.toxsoft.uskat.core.api.ISkService;
import org.toxsoft.uskat.core.api.clobserv.ISkClobService;
import org.toxsoft.uskat.core.api.clobserv.ISkClobServiceValidator;
import org.toxsoft.uskat.core.api.cmdserv.ISkCommandService;
import org.toxsoft.uskat.core.api.evserv.ISkEventService;
import org.toxsoft.uskat.core.api.gwids.ISkGwidService;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.rtdserv.ISkRtdataService;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;
import org.toxsoft.uskat.core.api.users.ISkUserService;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;
import org.toxsoft.uskat.core.devapi.gwiddb.ISkGwidDbService;

import core.tslib.bricks.synchronize.ITsThreadSynchronizer;

/**
 * {@link ISkService} implementation base.
 * <p>
 * As a non-core {@link ISkService} implementation class must:
 * <ul>
 * <li>be <code>public</code>;</li>
 * <li>have <code>public</code> constructor with only one argument of type {@link IDevCoreApi}.</li>
 * </ul>
 * TODO tips on service implementation:
 * <ul>
 * <li>do absolute minimum in constructor beacause created service may be thrown away without initialization and
 * {@link #close()};</li>
 * <li>all initialization must be done in {@link #doInit(ITsContextRo)}. Note that {@link #close()} is called only for
 * services initialized in {@link #doInit(ITsContextRo)};</li>
 * <li>if any of classes are claimed by service, override {@link #doIsClassClaimedByService(String)};</li>
 * <li>xxx;</li>
 * <li>xxx;</li>
 * <li>zzz.</li>
 * </ul>
 *
 * @author hazard157
 */
public abstract class AbstractSkService
    implements ISkService {

  /**
   * Сore services validators implementation that prohibits services to change entities of non-claimed classes.
   * <p>
   * Instance may be created by subclass and added to {@link ISkSysdescr#svs()}, {@link ISkObjectService#svs()},
   * {@link ISkLinkService#svs()}, {@link ISkClobService#svs()}.
   *
   * @author hazard157
   */
  public class ClassClaimingCoreValidator
      implements ISkSysdescrValidator, ISkObjectServiceValidator, ISkLinkServiceValidator, ISkClobServiceValidator {

    @Override
    public ValidationResult canCreateClass( IDtoClassInfo aNewClassInfo ) {
      return validateEditByNonSysdescrService( aNewClassInfo.id() );
    }

    @Override
    public ValidationResult canEditClass( IDtoClassInfo aNewClassInfo, ISkClassInfo aOldClassInfo ) {
      ValidationResult vr = validateEditByNonSysdescrService( aNewClassInfo.id() );
      return ValidationResult.firstNonOk( vr, validateEditByNonSysdescrService( aOldClassInfo.id() ) );
    }

    @Override
    public ValidationResult canRemoveClass( String aClassId ) {
      return validateEditByNonSysdescrService( aClassId );
    }

    @Override
    public ValidationResult canCreateObject( IDtoObject aDtoObject ) {
      return validateEditByNonSysdescrService( aDtoObject.classId() );
    }

    @Override
    public ValidationResult canEditObject( IDtoObject aNewObject, ISkObject aOldObject ) {
      ValidationResult vr = validateEditByNonSysdescrService( aNewObject.classId() );
      return ValidationResult.firstNonOk( vr, validateEditByNonSysdescrService( aOldObject.classId() ) );
    }

    @Override
    public ValidationResult canRemoveObject( Skid aSkid ) {
      return validateEditByNonSysdescrService( aSkid.classId() );
    }

    @Override
    public ValidationResult canSetLink( IDtoLinkFwd aOldLink, IDtoLinkFwd aNewLink ) {
      ValidationResult vr = validateEditByNonSysdescrService( aNewLink.leftSkid().classId() );
      return ValidationResult.firstNonOk( vr, validateEditByNonSysdescrService( aNewLink.leftSkid().classId() ) );
    }

    @Override
    public ValidationResult canWriteClob( Gwid aGwid, String aClob ) {
      return validateEditByNonSysdescrService( aGwid.classId() );
    }

  }

  private final String     serviceId;
  private final SkCoreApi  coreApi;
  private final Thread     thread;
  private final CoreLogger logger;

  private boolean inited = false;

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
    ITsThreadSynchronizer synchronizer =
        ISkCoreConfigConstants.REFDEF_THREAD_SYNCHRONIZER.getRef( aCoreApi.openArgs(), null );
    thread = (synchronizer != null ? synchronizer.thread() : null);
    logger = new CoreLogger( LoggerUtils.defaultLogger(), aCoreApi.openArgs() );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Returns error if class with specified ID is claimed by this service.
   * <p>
   * This is helper method for various core services validaton implementations.
   *
   * @param aClassId String - ID of class to be checked
   * @return {@link ValidationResult} - error if entities of class is claimed be this service
   */
  ValidationResult validateEditByNonSysdescrService( String aClassId ) {
    TsNullArgumentRtException.checkNull( aClassId );
    if( !aClassId.equals( ISkSysdescr.SERVICE_ID ) ) {
      if( isClassClaimedByService( aClassId ) ) {
        return ValidationResult.error( FMT_ERR_CLAIM_VIOLATION, aClassId, serviceId );
      }
    }
    return ValidationResult.SUCCESS;
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
   * Throws and exception if the calling thread is not a API-thread.
   *
   * @throws TsIllegalStateRtException invalid thread access
   */
  final public void checkThread() {
    if( thread != null && thread != Thread.currentThread() ) {
      throw new TsIllegalStateRtException( FMT_ERR_INVALID_THREAD_ACCESS );
    }
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

  /**
   * Determines if class is claimed by this service.
   *
   * @param aClassId String - ID of class to be checked
   * @return boolean - <code>true</code> if this service is owning entities of asked class
   */
  final boolean isClassClaimedByService( String aClassId ) {
    return doIsClassClaimedByService( aClassId );
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

  @SuppressWarnings( "javadoc" )
  public ISkGwidService gwidService() {
    return coreApi.gwidService();
  }

  @SuppressWarnings( "javadoc" )
  public ISkUserService userService() {
    return coreApi.userService();
  }

  @SuppressWarnings( "javadoc" )
  public ISkGwidDbService gwidDbService() {
    return coreApi.gwidDbService();
  }

  // ------------------------------------------------------------------------------------
  // To override
  //

  /**
   * If owning (claiming on) any class then descendant must determine if asked class is claimed one.
   * <p>
   * In base class returns <code>false</code> there is no need to call superclass method when overriding.
   *
   * @param aClassId String - ID of class to be checked
   * @return boolean - <code>true</code> if this service is owning entities of asked class
   */
  protected boolean doIsClassClaimedByService( String aClassId ) {
    return false;
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
   * TODO:
   *
   * @param aIsActive - the backens activity state
   */
  protected void onBackendActiveStateChanged( boolean aIsActive ) {
    // nop
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
