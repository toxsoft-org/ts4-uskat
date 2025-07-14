package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.clobserv.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.gwids.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.devapi.gwiddb.*;

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

  private final String            serviceId;
  private final SkCoreApi         coreApi;
  private final ITsThreadExecutor executor;
  private final CoreLogger        logger;

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
    executor = aCoreApi.executor();
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
   * Returns thread executor.
   *
   * @return {@link ITsThreadExecutor} thread executor
   */
  final public ITsThreadExecutor threadExecutor() {
    return executor;
  }

  /**
   * @throws TsIllegalStateRtException invalid thread access
   */
  final public void checkThread() {
    Thread currentThread = Thread.currentThread();
    if( executor.thread() != currentThread ) {
      String owner = executor.thread().getName();
      String current = Thread.currentThread().getName();
      throw new TsIllegalStateRtException( FMT_ERR_INVALID_THREAD_ACCESS, owner, current );
    }
  }

  /**
   * Returns the individual logger for this service. ======= Returns the individual logger for this service.
   *
   * @return {@link CoreLogger} - service logger
   */
  final public CoreLogger logger() {
    return logger;
  }

  /**
   * Sends message to the sibling instances of this service.
   * <p>
   * The topic ID of the sent {@link GtMessage} must the the {@link #serviceId()} of this service. The message ID and
   * the arguments are determined and are specific to this service.
   * <p>
   * Siblings are instances of this service running in other CoreAPI of the same server. For local backends service does
   * not have any siblings.
   *
   * @param aMessage String - the message to send
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException message ID {@link GtMessage#messageId()} is not equal to {@link #serviceId()}
   */
  final public void sendMessageToSiblings( GtMessage aMessage ) {
    TsNullArgumentRtException.checkNull( aMessage );
    TsIllegalArgumentRtException.checkFalse( aMessage.topicId().equals( serviceId() ) );
    coreApi.backend().sendBackendMessage( aMessage );
  }

  /**
   * Creates instance of {@link GtMessage} for siblings, with topic ID equal to the {@link #serviceId()}.
   *
   * @param aMessageId String - the message ID
   * @param aArgs {@link IOptionSet} - arguments, will be copied to internal set
   * @return {@link GtMessage} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  final public GtMessage makeSiblingMessage( String aMessageId, IOptionSet aArgs ) {
    return new GtMessage( serviceId, aMessageId, aArgs );
  }

  /**
   * Creates instance of {@link GtMessage} for siblings, with topic ID equal to the {@link #serviceId()}.
   *
   * @param aMessageId String - the message ID
   * @param aIdsAndValues Object[] - identifier / value pairs as for {@link OptionSetUtils#createOpSet(Object...)}
   * @return {@link GtMessage} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   * @throws TsIllegalArgumentRtException number of elements in array is uneven
   * @throws ClassCastException argument types convention is violated
   */
  final public GtMessage makeSiblingMessage2( String aMessageId, Object... aIdsAndValues ) {
    return new GtMessage( serviceId, aMessageId, aIdsAndValues );
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
    logger().debug( FMT_INFO_SERVICE_INIT, serviceId() );
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
    logger().debug( FMT_INFO_SERVICE_CLOSE, serviceId() );
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
   * Handles message from backend.
   * <p>
   * Method simple checks if topic is of this service and passes message to the implementation as
   * {@link GenericMessage}, that is the message without topic.
   *
   * @param aMessage {@link GtMessage} - the message from backend
   */
  final public void papiOnBackendMessage( GtMessage aMessage ) {
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
   * Subclasses must finish jobs, save data, release resources and perform all necessary clean-ups.
   * <p>
   * Method is called when USkat core is finishing working. Avterf this method service will not be used.
   */
  protected abstract void doClose();

  /**
   * Subclass may handle message from the backend.
   * <p>
   * Base implementation simply returns <code>false</code>. There is no need to call superclass method when overriding.
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
   * Implementation may perform actions when backend activity state changes.
   * <p>
   * This method is meaningful only for backend which may became temporarily inactive while Sk-connection remains open.
   * For example, when connection to the server is temporarily lost. Most local backends (which uses file storage in
   * local file system) this method is never called because they are always active.
   * <p>
   * Base implementatation does nothing, there is no need to call superclass method when overriding. <<<<<<< HEAD
   *
   * @param aIsActive boolean - backend activity state >>>>>>> refs/heads/main
   */
  protected void onBackendActiveStateChanged( boolean aIsActive ) {
    // nop
  }

}
