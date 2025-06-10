package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.backend.api.IBaObjectsMessages.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.core.tslib.utils.txtmatch.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * {@link ISkObjectService} implementation.
 *
 * @author hazard157
 */
public class SkCoreServObject
    extends AbstractSkCoreService
    implements ISkObjectService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServObject::new;

  /**
   * Internal cache of the objects.
   *
   * @author hazard157
   */
  static class ObjsCache {

    private static final int MAX_SIZE = 256 * 1024;

    private final IMapEdit<Skid, SkObject> cache = new ElemMap<>( TsCollectionsUtils.getMapBucketsCount( //
        TsCollectionsUtils.estimateOrder( MAX_SIZE ) ), TsCollectionsUtils.DEFAULT_BUNDLE_CAPACITY );

    ObjsCache() {
      // nop
    }

    boolean has( Skid aSkid ) {
      return cache.hasKey( aSkid );
    }

    SkObject find( Skid aSkid ) {
      return cache.findByKey( aSkid );
    }

    SkObject put( SkObject aObject ) {
      if( cache.size() >= MAX_SIZE ) {
        cache.removeByKey( cache.keys().first() );
      }
      cache.put( aObject.skid(), aObject );
      return aObject;
    }

    void remove( Skid aSkid ) {
      cache.removeByKey( aSkid );
    }

    void removeByClass( String aClassId ) {
      SkidList toRemove = new SkidList();
      for( Skid s : cache.keys() ) {
        if( s.classId().equals( aClassId ) ) {
          toRemove.add( s );
        }
      }
      for( Skid s : toRemove ) {
        cache.removeByKey( s );
      }
    }

    void clear() {
      cache.clear();
    }

  }

  /**
   * Objects creators storage.
   *
   * @author hazard157
   */
  static class ObjsCreators {

    private final IStringMapEdit<ISkObjectCreator<?>>        objCreatorsByIds   = new StringMap<>( 157 );
    private final IMapEdit<TextMatcher, ISkObjectCreator<?>> objCreatorsByRules = new ElemMap<>( 157, 32 );

    // ------------------------------------------------------------------------------------
    // API
    //

    ISkObjectCreator<?> getCreator( String aClassId ) {
      ISkObjectCreator<? extends SkObject> creator = objCreatorsByIds.findByKey( aClassId );
      if( creator == null ) {
        // iterate in the reverse order, for rules registered later to override earlier ones
        for( int i = objCreatorsByRules.size() - 1; i >= 0; i-- ) {
          TextMatcher rule = objCreatorsByRules.keys().get( i );
          if( rule.match( aClassId ) ) {
            creator = objCreatorsByRules.getByKey( rule );
            break;
          }
        }
      }
      if( creator == null ) {
        creator = SkObject.DEFAULT_CREATOR;
      }
      return creator;
    }

    public void registerObjectCreator( String aClassId, ISkObjectCreator<?> aCreator ) {
      TsNullArgumentRtException.checkNull( aCreator );
      StridUtils.checkValidIdPath( aClassId );
      TsItemAlreadyExistsRtException.checkTrue( objCreatorsByIds.hasKey( aClassId ) );
      objCreatorsByIds.put( aClassId, aCreator );
    }

    public void registerObjectCreator( TextMatcher aRule, ISkObjectCreator<?> aCreator ) {
      TsNullArgumentRtException.checkNull( aCreator );
      TsItemAlreadyExistsRtException.checkTrue( objCreatorsByRules.hasKey( aRule ) );
      objCreatorsByRules.put( aRule, aCreator );
    }

    public void unregisterObjectCreator( String aClassId, ISkObjectCreator<?> aCreator ) {
      TsNullArgumentRtException.checkNull( aCreator );
      ISkObjectCreator<?> creator = objCreatorsByIds.findByKey( aClassId );
      if( creator != null && creator != aCreator ) {
        throw new TsIllegalStateRtException();
      }
      objCreatorsByIds.removeByKey( aClassId );
    }

    public void unregisterObjectCreator( TextMatcher aRule, ISkObjectCreator<?> aCreator ) {
      TsNullArgumentRtException.checkNull( aCreator );
      ISkObjectCreator<?> creator = objCreatorsByRules.findByKey( aRule );
      if( creator != null && creator != aCreator ) {
        throw new TsIllegalStateRtException();
      }
      objCreatorsByRules.removeByKey( aRule );
    }

  }

  /**
   * {@link ISkObjectService#eventer()} implementation.
   *
   * @author hazard157
   */
  class Eventer
      extends AbstractTsEventer<ISkObjectServiceListener> {

    private boolean isPending = false;

    @Override
    protected void doClearPendingEvents() {
      isPending = false;
    }

    @Override
    protected void doFirePendingEvents() {
      isPending = false;
      fireObjectsChanged( ECrudOp.LIST, null );
    }

    @Override
    protected boolean doIsPendingEvents() {
      return isPending;
    }

    void fireObjectsChanged( ECrudOp aOp, Skid aSkid ) {
      if( isFiringPaused() ) {
        isPending = true;
        return;
      }
      for( ISkObjectServiceListener l : listeners() ) {
        try {
          l.onObjectsChanged( coreApi(), aOp, aSkid );
        }
        catch( Exception ex ) {
          LoggerUtils.errorLogger().error( ex );
        }
      }
    }

  }

  /**
   * The service validator {@link ISkObjectService#svs()} implementation.
   *
   * @author hazard157
   */
  class ValidationSupport
      extends AbstractTsValidationSupport<ISkObjectServiceValidator>
      implements ISkObjectServiceValidator {

    @Override
    public ISkObjectServiceValidator validator() {
      return this;
    }

    @Override
    public ValidationResult canCreateObject( IDtoObject aObject ) {
      TsNullArgumentRtException.checkNull( aObject );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkObjectServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canCreateObject( aObject ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canCreateObjects( IList<IDtoObject> aObjects ) {
      TsNullArgumentRtException.checkNull( aObjects );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkObjectServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canCreateObjects( aObjects ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canEditObject( IDtoObject aObject, ISkObject aOldInfo ) {
      TsNullArgumentRtException.checkNull( aObject );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkObjectServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canEditObject( aObject, aOldInfo ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canRemoveObject( Skid aSkid ) {
      TsNullArgumentRtException.checkNull( aSkid );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkObjectServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canRemoveObject( aSkid ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canRemoveObjects( ISkidList aSkids ) {
      TsNullArgumentRtException.checkNull( aSkids );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkObjectServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canRemoveObjects( aSkids ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

  }

  private final ISkObjectServiceValidator builtinValidator = new ISkObjectServiceValidator() {

    private ValidationResult checkAttrsAndRivets( IDtoObject aDtoObj, boolean aCreation ) {
      ISkClassInfo classInfo = coreApi().sysdescr().getClassInfo( aDtoObj.skid().classId() );
      // check attributes
      for( IDtoAttrInfo attrInfo : classInfo.attrs().list() ) {
        IAtomicValue defVal = attrInfo.dataType().params().getValue( TSID_DEFAULT_VALUE, null );
        // at creation type all non-system attributes must have value specified
        if( !aDtoObj.attrs().hasKey( attrInfo.id() ) ) {
          if( aCreation && defVal == null && !isSkSysAttr( attrInfo ) ) {
            return ValidationResult.error( FMT_ERR_NO_ATTR_VAL, aDtoObj.skid().toString(), attrInfo.id() );
          }
        }
        IAtomicValue val = aDtoObj.attrs().getValue( attrInfo.id(), IAtomicValue.NULL );
        // check if NULL value is allowed
        if( val == IAtomicValue.NULL && !attrInfo.params().getBool( TSID_IS_NULL_ALLOWED, true ) ) {
          return ValidationResult.error( FMT_ERR_NULL_ATTR_VAL, aDtoObj.skid().toString(), attrInfo.id() );
        }
        // check if atomic type of attribute is compatible with the specified value
        EAtomicType lType = attrInfo.dataType().atomicType();
        EAtomicType rType = val.atomicType();
        AvTypeCastRtException.checkCanAssign( lType, rType, FMT_ERR_INV_ATTR_TYPE, aDtoObj.skid().toString(),
            attrInfo.id(), rType.id(), lType.id() );
      }
      // check rivets
      for( IDtoRivetInfo rivetInfo : classInfo.rivets().list() ) {
        // check right class exists
        ISkClassInfo rightClass = coreApi().sysdescr().findClassInfo( rivetInfo.rightClassId() );
        if( rightClass == null ) {
          return ValidationResult.error( FMT_ERR_NO_RIVET_CLASS, aDtoObj.skid().toString(), rivetInfo.id(),
              rivetInfo.rightClassId() );
        }
        // check riveted objects are specified
        ISkidList rivet = aDtoObj.rivets().map().findByKey( rivetInfo.id() );
        if( rivet == null ) {
          return ValidationResult.error( FMT_ERR_NO_RIVET, aDtoObj.skid().toString(), rivetInfo.id() );
        }
        // check riveted objects number is equal to specified one
        if( rivet.size() != rivetInfo.count() ) {
          return ValidationResult.error( FMT_ERR_INV_RIVET_COUNT, aDtoObj.skid().toString(), rivetInfo.id(),
              Integer.valueOf( rivet.size() ), Integer.valueOf( rivetInfo.count() ) );
        }
        // check riveted objects is of specified right class ID
        for( Skid skid : rivet ) {
          if( !skid.isNone() ) {
            if( !rightClass.isAssignableFrom( skid.classId() ) ) {
              return ValidationResult.error( FMT_ERR_INV_RIVET_OBJ_CLS, aDtoObj.skid().toString(), rivetInfo.id(),
                  skid.toString(), rightClass.id() );
            }
          }
        }
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canCreateObject( IDtoObject aDtoObject ) {
      if( internalDoesObjectExists( aDtoObject.skid() ) ) {
        return ValidationResult.error( FMT_ERR_OBJ_ALREADY_EXISTS, aDtoObject.skid().toString() );
      }
      return checkAttrsAndRivets( aDtoObject, true );
    }

    @Override
    public ValidationResult canCreateObjects( IList<IDtoObject> aDtoObjects ) {
      ValidationResult vr = ValidationResult.SUCCESS;
      for( IDtoObject dtoObj : aDtoObjects ) {
        vr = ValidationResult.firstNonOk( vr, canCreateObject( dtoObj ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canEditObject( IDtoObject aDtoObject, ISkObject aOldObject ) {
      if( !aDtoObject.skid().equals( aOldObject.skid() ) ) {
        return ValidationResult.error( FMT_ERR_CANT_CHANGE_SKID, aOldObject.skid().toString() );
      }
      return checkAttrsAndRivets( aDtoObject, false );
    }

    @Override
    public ValidationResult canRemoveObject( Skid aSkid ) {
      if( internalDoesObjectExists( aSkid ) ) {
        return ValidationResult.SUCCESS;
      }
      return ValidationResult.error( FMT_ERR_CANT_REMOVE_NO_OBJ, aSkid.toString() );
    }

    @Override
    public ValidationResult canRemoveObjects( ISkidList aSkids ) {
      ValidationResult vr = ValidationResult.SUCCESS;
      for( Skid skid : aSkids ) {
        vr = ValidationResult.firstNonOk( vr, canRemoveObject( skid ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

  };

  /**
   * Updates objects class when Sk-class changes.
   */
  private final ISkSysdescrListener sysdescrListener = ( aCoreApi, aOp, aClassId ) -> {
    switch( aOp ) {
      case CREATE: {
        // nop
        break;
      }
      case EDIT:
      case REMOVE: {
        this.objsCache.removeByClass( aClassId );
        break;
      }
      case LIST: {
        this.objsCache.clear();
        break;
      }
      default:
        throw new TsNotAllEnumsUsedRtException( aOp.id() );
    }
  };

  final ObjsCache         objsCache         = new ObjsCache();
  final ObjsCreators      objsCreators      = new ObjsCreators();
  final Eventer           eventer           = new Eventer();
  final ValidationSupport validationSupport = new ValidationSupport();

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkCoreServObject( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
    validationSupport.addValidator( builtinValidator );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkCoreService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    sysdescr().eventer().addListener( sysdescrListener );
  }

  @Override
  protected void doClose() {
    eventer.clearListenersList();
    eventer.resetPendingEvents();
  }

  @Override
  protected boolean onBackendMessage( GenericMessage aMessage ) {
    return switch( aMessage.messageId() ) {
      case MSGID_OBJECTS_CHANGE -> {
        ECrudOp op = extractCrudOp( aMessage );
        Skid skid = extractSkid( aMessage );
        if( skid != null ) {
          // if created by this service, object is already in cache, if sibling service - remove() has no sense
          if( op != ECrudOp.CREATE ) {
            objsCache.remove( skid );
          }
        }
        else {
          objsCache.clear();
        }
        eventer.fireObjectsChanged( op, skid );
        yield true;
      }
      default -> false;
    };
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Creates {@link SkObject} from the data {@link IDtoObject}.
   * <p>
   * {@link DtoObject} may contain not all properties (eg. not all attributes) however contained data must be valid.
   * This methods adds missing values but does not checks for provided values validity.
   * <p>
   * Created instance will have all and only the attributes and rivets as described in {@link ISkClassInfo}.
   *
   * @param aObjectDto {@link IDtoObject} - data for object creation
   * @param aClassInfo {@link ISkClassInfo} - class description of the object to be created
   * @return {@link SkObject} - created instance
   */
  private SkObject fromDto( IDtoObject aObjectDto, ISkClassInfo aClassInfo ) {
    String classId = aObjectDto.skid().classId();
    ISkObjectCreator<? extends SkObject> creator = objsCreators.getCreator( classId );
    SkObject sko = creator.createObject( aObjectDto.skid() );
    sko.papiSetCoreApi( coreApi() );
    // initialize attributes from aObjectDto or by default values
    for( IDtoAttrInfo ainf : aClassInfo.attrs().list() ) {
      if( !isSkSysAttr( ainf ) ) {
        IAtomicValue av = aObjectDto.attrs().getValue( ainf.id(), ainf.dataType().defaultValue() );
        sko.attrs().setValue( ainf.id(), av );
      }
    }
    // initialize rivets from aObjectDto or by Skid.NONE
    for( IDtoRivetInfo rinf : aClassInfo.rivets().list() ) {
      ISkidList rivet = aObjectDto.rivets().map().findByKey( rinf.id() );
      if( rivet == null ) {
        rivet = SkidList.createNones( rinf.count() );
      }
      else {
        rivet = new SkidList( rivet );
      }
      sko.rivets().map().put( rinf.id(), rivet );
    }
    return sko;
  }

  /**
   * Creates {@link DtoObject} to be saved to the backend.
   * <p>
   * For size optimization, created instance:
   * <ul>
   * <li>does not have system attribute values in the set {@link IDtoObject#attrs()};</li>
   * <li>does not includes attributes with default values - they will be restored from attribute info at load time.</li>
   * </ul>
   *
   * @param aSkObj {@link ISkObject} - the source
   * @return {@link DtoObject} - created instance
   */
  private DtoObject createForBackendSave( ISkObject aSkObj ) {
    ISkClassInfo classInfo = coreApi().sysdescr().getClassInfo( aSkObj.classId() );
    DtoObject dtoObj = new DtoObject( aSkObj.skid(), IOptionSet.NULL, aSkObj.rivets().map(), aSkObj.rivetRevs() );
    // copy all but system attributes and attributes with default values
    for( IDtoAttrInfo ainf : classInfo.attrs().list() ) {
      // don't save system attributes
      if( !ISkHardConstants.isSkSysAttr( ainf ) ) {
        IAtomicValue attrVal = aSkObj.attrs().getValue( ainf.id() );
        IAtomicValue defVal = ainf.dataType().defaultValue();
        // don't save attribute with the default value
        if( !attrVal.equals( defVal ) ) {
          dtoObj.attrs().setValue( ainf.id(), attrVal );
        }
      }
    }
    return dtoObj;
  }

  private void internalWriteSkObjectToBackend( ISkObject aObj ) {
    DtoObject dtoObj = createForBackendSave( aObj );
    ba().baObjects().writeObjects( ISkidList.EMPTY, new SingleItemList<>( dtoObj ) );
  }

  boolean internalDoesObjectExists( Skid aSkid ) {
    if( objsCache.has( aSkid ) ) {
      return true;
    }
    if( ba().baObjects().findObject( aSkid ) != null ) {
      return true;
    }
    return false;
  }

  private void internalRemoveObjects( ISkidList aSkids ) {
    // remove all links before removing object
    for( Skid s : aSkids ) {
      linkService().removeLinks( s );
    }
    ba().baObjects().writeObjects( aSkids, IList.EMPTY );
  }

  // ------------------------------------------------------------------------------------
  // ISkObjectService
  //

  @SuppressWarnings( "unchecked" )
  @Override
  public <T extends ISkObject> T find( Skid aSkid ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aSkid );
    coreApi().papiCheckIsOpen();
    SkObject sko = objsCache.find( aSkid );
    if( sko != null ) {
      return (T)sko;
    }
    IDtoObject dpu = coreApi().l10n().l10nObject( ba().baObjects().findObject( aSkid ) );
    if( dpu != null ) {
      ISkClassInfo cInfo = coreApi().sysdescr().getClassInfo( aSkid.classId() );
      sko = fromDto( dpu, cInfo );
      return (T)objsCache.put( sko );
    }
    return null;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public <T extends ISkObject> T get( Skid aSkid ) {
    checkThread();
    coreApi().papiCheckIsOpen();
    ISkObject sko = find( aSkid );
    TsItemNotFoundRtException.checkNull( sko, FMT_ERR_NO_SUCH_OBJ, aSkid.toString() );
    return (T)sko;
  }

  @Override
  public ISkidList listSkids( String aClassId, boolean aIncludeSubclasses ) {
    checkThread();
    coreApi().papiCheckIsOpen();
    ISkClassInfo cinf = coreApi().sysdescr().getClassInfo( aClassId );
    IStridablesList<ISkClassInfo> classesList;
    if( aIncludeSubclasses ) {
      classesList = cinf.listSubclasses( !aIncludeSubclasses, true );
    }
    else {
      classesList = new StridablesList<>( cinf );
    }
    IList<IDtoObject> dpuObjs = coreApi().l10n().l10nObjectsList( ba().baObjects().readObjects( classesList.ids() ) );
    SkidList ll = new SkidList();
    for( IDtoObject dpu : dpuObjs ) {
      ll.add( dpu.skid() );
    }
    return ll;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public <T extends ISkObject> IList<T> listObjs( String aClassId, boolean aIncludeSubclasses ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aClassId );
    coreApi().papiCheckIsOpen();
    ISkClassInfo cinf = sysdescr().getClassInfo( aClassId );
    IStridablesList<ISkClassInfo> classesList;
    if( aIncludeSubclasses ) {
      classesList = cinf.listSubclasses( !aIncludeSubclasses, true );
    }
    else {
      classesList = new StridablesList<>( cinf );
    }
    // OPTIMIZE search objects in cache ???
    IList<IDtoObject> dpuObjs = coreApi().l10n().l10nObjectsList( ba().baObjects().readObjects( classesList.ids() ) );
    // aAllowDuplicates = false
    IListEdit<ISkObject> ll = new ElemLinkedBundleList<>( 256, false );
    for( IDtoObject dpu : dpuObjs ) {
      SkObject sko = objsCache.find( dpu.skid() );
      if( sko == null ) {
        ISkClassInfo cInfo = sysdescr().getClassInfo( dpu.skid().classId() );
        sko = fromDto( dpu, cInfo );
        objsCache.put( sko );
      }
      ll.add( sko );
    }
    return (IList<T>)ll;
  }

  @Override
  public IList<ISkObject> getObjs( ISkidList aSkids ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aSkids );
    coreApi().papiCheckIsOpen();
    IList<IDtoObject> dpuObjs = coreApi().l10n().l10nObjectsList( ba().baObjects().readObjectsByIds( aSkids ) );
    IListEdit<ISkObject> result = new ElemLinkedBundleList<>(
        TsCollectionsUtils.getListInitialCapacity( TsCollectionsUtils.estimateOrder( 1_000 ) ), false );
    for( IDtoObject dpu : dpuObjs ) {
      SkObject sko = objsCache.find( dpu.skid() );
      if( sko == null ) {
        ISkClassInfo cInfo = sysdescr().getClassInfo( dpu.skid().classId() );
        sko = fromDto( dpu, cInfo );
        objsCache.put( sko );
      }
      result.add( sko );
    }
    return result;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public <T extends ISkObject> T defineObject( IDtoObject aDtoObject ) {
    checkThread();
    // check preconditions
    TsNullArgumentRtException.checkNull( aDtoObject );
    coreApi().papiCheckIsOpen();
    ISkClassInfo cInfo = coreApi().sysdescr().getClassInfo( aDtoObject.skid().classId() );
    // validate operation
    SkObject sko = find( aDtoObject.skid() );
    if( sko != null ) { // validate exiting object editing
      TsValidationFailedRtException.checkError( validationSupport.canEditObject( aDtoObject, sko ) );
    }
    else { // validate new object creation
      TsValidationFailedRtException.checkError( validationSupport.canCreateObject( aDtoObject ) );
      sko = fromDto( aDtoObject, cInfo );
    }
    // refresh attribute values (validator already checked that attributes set is valid)
    for( IDtoAttrInfo ainf : cInfo.attrs().list() ) {
      if( !isSkSysAttr( ainf ) ) {
        IAtomicValue val = aDtoObject.attrs().getValue( ainf.id(), ainf.dataType().defaultValue() );
        sko.attrs().setValue( ainf.id(), val );
      }
    }
    // refresh rivets values
    for( IDtoRivetInfo rinf : cInfo.rivets().list() ) {
      ISkidList rivets = aDtoObject.rivets().map().getByKey( rinf.id() );
      sko.rivets().ensureSkidList( rinf.id() ).setAll( rivets );
    }
    // save object

    /**
     * FIXME here we need to localize object after it was written to the backend.<br>
     * When creating system objects they are created in English. but in cache they have to be put localized!
     */

    objsCache.put( sko );
    internalWriteSkObjectToBackend( sko );
    return (T)sko;
  }

  @Override
  public void removeObject( Skid aSkid ) {
    checkThread();
    coreApi().papiCheckIsOpen();
    TsValidationFailedRtException.checkError( validationSupport.canRemoveObject( aSkid ) );
    objsCache.remove( aSkid );
    internalRemoveObjects( new SkidList( aSkid ) );
  }

  @Override
  public ISkidList getRivetRev( String aClassId, String aRivetId, Skid aRightSkid ) {
    checkThread();
    // TODO implement SkCoreServObject.getRivetRev()
    throw new TsUnderDevelopmentRtException( "SkCoreServObject.getRivetRev()" );
  }

  @Override
  public IMap<Gwid, ISkidList> getAllRivetsRev( Skid aRightSkid ) {
    checkThread();
    // TODO implement SkCoreServObject.getRivetRev()
    throw new TsUnderDevelopmentRtException( "SkCoreServObject.getRivetRev()" );
  }

  @Override
  public void removeObjects( ISkidList aSkids ) {
    checkThread();
    coreApi().papiCheckIsOpen();
    TsValidationFailedRtException.checkError( validationSupport.canRemoveObjects( aSkids ) );
    for( Skid s : aSkids ) {
      objsCache.remove( s );
    }
    internalRemoveObjects( aSkids );
  }

  @Override
  public void registerObjectCreator( TextMatcher aRule, ISkObjectCreator<?> aCreator ) {
    checkThread();
    objsCreators.registerObjectCreator( aRule, aCreator );
  }

  @Override
  public ITsValidationSupport<ISkObjectServiceValidator> svs() {
    return validationSupport;
  }

  @Override
  public ITsEventer<ISkObjectServiceListener> eventer() {
    return eventer;
  }

}
