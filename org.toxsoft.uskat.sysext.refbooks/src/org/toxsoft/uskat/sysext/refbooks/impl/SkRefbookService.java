package org.toxsoft.uskat.sysext.refbooks.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.sysext.refbooks.ISkRefbookServiceHardConstants.*;
import static org.toxsoft.uskat.sysext.refbooks.impl.ISkResources.*;
import static ru.uskat.common.ISkHardConstants.*;

import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.events.AbstractTsEventer;
import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.bricks.validator.impl.AbstractTsValidationSupport;
import org.toxsoft.core.tslib.bricks.validator.impl.TsValidationFailedRtException;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.helpers.ECrudOp;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.IGwHardConstants;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNotAllEnumsUsedRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;
import org.toxsoft.core.tslib.utils.txtmatch.ETextMatchMode;
import org.toxsoft.core.tslib.utils.txtmatch.TextMatcher;
import org.toxsoft.uskat.sysext.refbooks.*;

import ru.uskat.common.dpu.IDpuObject;
import ru.uskat.common.dpu.IDpuSdClassInfo;
import ru.uskat.common.dpu.impl.*;
import ru.uskat.common.dpu.rt.events.SkEvent;
import ru.uskat.core.api.ISkServiceCreator;
import ru.uskat.core.api.links.ISkLinkService;
import ru.uskat.core.api.links.ISkLinkServiceValidator;
import ru.uskat.core.api.objserv.ISkObjectService;
import ru.uskat.core.api.objserv.ISkObjectServiceValidator;
import ru.uskat.core.api.sysdescr.*;
import ru.uskat.core.common.skobject.ISkObject;
import ru.uskat.core.devapi.IDevCoreApi;
import ru.uskat.core.impl.AbstractSkService;

/**
 * {@link ISkRefbookService} implementation.
 * <p>
 * Refbooks are implemented the following way:
 * <ul>
 * <li>The refbook {@link ISkRefbook} simply is object of the class
 * {@link ISkRefbookServiceHardConstants#CLASSID_REFBOOK};</li>
 * <li>Refbook items are simply the objects of class {@link ISkRefbook#itemClassId()}, without any links to the refbook
 * object;</li>
 * <li>Item class is created by the method
 * {@link ISkRefbookServiceHardConstants#makeItemClassIdFromRefbookId(String)};</li>
 * <li>Listing refbook items is as simple as list all objects of the class {@link ISkRefbook#itemClassId()}.</li>
 * </ul>
 *
 * @author goga
 */
public class SkRefbookService
    extends AbstractSkService
    implements ISkRefbookService {

  /**
   * Синглтон создателя сервиса.
   */
  public static final ISkServiceCreator<SkRefbookService> CREATOR = SkRefbookService::new;

  class Eventer
      extends AbstractTsEventer<ISkRefbookServiceListener> {

    private final IStringMapEdit<IListEdit<SkEvent>> refbooksChangedItems = new StringMap<>();

    private boolean wasRefbooksListChanged = false;

    @Override
    protected void doClearPendingEvents() {
      refbooksChangedItems.clear();
      wasRefbooksListChanged = false;
    }

    @Override
    protected void doFirePendingEvents() {
      doFireRefbookChanged( ECrudOp.LIST, null );
      IStridablesList<ISkRefbook> rbList = listRefbooks();
      for( String rbId : refbooksChangedItems.keys() ) {
        if( rbList.hasKey( rbId ) ) {
          doFireItemsChanged( rbId, refbooksChangedItems.getByKey( rbId ) );
        }
      }
    }

    @Override
    protected boolean doIsPendingEvents() {
      return wasRefbooksListChanged || !refbooksChangedItems.isEmpty();
    }

    private void doFireRefbookChanged( ECrudOp aOp, String aRefbookId ) {
      for( ISkRefbookServiceListener l : listeners() ) {
        try {
          l.onRefbookChanged( aOp, aRefbookId );
        }
        catch( Exception ex ) {
          LoggerUtils.errorLogger().error( ex );
        }
      }
    }

    private void doFireItemsChanged( String aRefbookId, IList<SkEvent> aEvents ) {
      for( ISkRefbookServiceListener l : listeners() ) {
        try {
          l.onRefbookItemsChanged( aRefbookId, aEvents );
        }
        catch( Exception ex ) {
          LoggerUtils.errorLogger().error( ex );
        }
      }
    }

    public void fireRefbookChanged( ECrudOp aOp, String aRefbookId ) {
      if( isFiringPaused() ) {
        wasRefbooksListChanged = true;
        return;
      }
      doFireRefbookChanged( aOp, aRefbookId );
    }

    public void fireItemsChanged( String aRefbookId, IList<SkEvent> aEvent ) {
      if( isFiringPaused() ) {
        IListEdit<SkEvent> events = refbooksChangedItems.findByKey( aRefbookId );
        if( events == null ) {
          events = new ElemArrayList<>();
          refbooksChangedItems.put( aRefbookId, events );
        }
        events.addAll( aEvent );
        return;
      }
      doFireItemsChanged( aRefbookId, aEvent );
    }

  }

  class ValidationSupport
      extends AbstractTsValidationSupport<ISkRefbookServiceValidator>
      implements ISkRefbookServiceValidator {

    @Override
    public ISkRefbookServiceValidator validator() {
      return this;
    }

    @Override
    public ValidationResult canDefineRefbook( ISkRefbookDpuInfo aDpuRefbookInfo, ISkRefbook aExistingRefbook ) {
      TsNullArgumentRtException.checkNull( aDpuRefbookInfo );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkRefbookServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canDefineRefbook( aDpuRefbookInfo, aExistingRefbook ) );
      }
      return vr;
    }

    @Override
    public ValidationResult canRemoveRefbook( String aRefbookId ) {
      TsNullArgumentRtException.checkNull( aRefbookId );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkRefbookServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canRemoveRefbook( aRefbookId ) );
      }
      return vr;
    }

    @Override
    public ValidationResult canDefineItem( ISkRefbook aRefbook, ISkRefbookDpuItemInfo aDpuItemInfo,
        IStringMap<ISkidList> aLinks, ISkRefbookItem aExistingItem ) {
      TsNullArgumentRtException.checkNulls( aRefbook, aDpuItemInfo );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkRefbookServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canDefineItem( aRefbook, aDpuItemInfo, aLinks, aExistingItem ) );
      }
      return vr;
    }

    @Override
    public ValidationResult canRemoveItem( ISkRefbook aRefbook, String aItemId ) {
      TsNullArgumentRtException.checkNulls( aRefbook, aItemId );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkRefbookServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canRemoveItem( aRefbook, aItemId ) );
      }
      return vr;
    }

  }

  /**
   * Builtin validation rules.
   */
  private final ISkRefbookServiceValidator builtinValidator = new ISkRefbookServiceValidator() {

    @Override
    public ValidationResult canDefineRefbook( ISkRefbookDpuInfo aDpuRefbookInfo, ISkRefbook aExistingRefbook ) {
      IStridablesList<ISkRefbook> rbs = listRefbooks();
      ValidationResult vr = ValidationResult.SUCCESS;
      // создание нового справочника
      if( aExistingRefbook == null ) {
        if( rbs.hasKey( aDpuRefbookInfo.id() ) ) {
          return ValidationResult.error( FMT_ERR_REFBOOK_ALREADY_EXISTS, aDpuRefbookInfo.id() );
        }
      }
      // предупреждение о дублировании имени
      for( ISkRefbook rb : rbs ) {
        if( rb.nmName().equals( aDpuRefbookInfo.nmName() ) && // новое имя уже встречается?
        (aExistingRefbook == null || !aExistingRefbook.id().equals( rb.id() )) ) // исключим редактируемый справочник
        {
          vr = ValidationResult.warn( FMT_WARN_RB_NAME_ALREADY_EXISTS, aDpuRefbookInfo.nmName() );
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canRemoveRefbook( String aRefbookId ) {
      if( !listRefbooks().hasKey( aRefbookId ) ) {
        return ValidationResult.error( FMT_ERR_REFBOOK_NOT_EXISTS, aRefbookId );
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canDefineItem( ISkRefbook aRefbook, ISkRefbookDpuItemInfo aDpuItemInfo,
        IStringMap<ISkidList> aLinks, ISkRefbookItem aExistingItem ) {
      IStridablesList<ISkRefbookItem> items = new StridablesList<>( aRefbook.listItems() );
      ValidationResult vr = ValidationResult.SUCCESS;
      // проверка дублирования при создании нового элемента
      if( aExistingItem == null ) {
        if( items.hasKey( aDpuItemInfo.id() ) ) {
          return ValidationResult.error( FMT_ERR_ITEM_ALREADY_EXISTS, aDpuItemInfo.strid() );
        }
      }
      // предупреждение о дублировании имени
      for( ISkRefbookItem item : items ) {
        if( item.nmName().equals( aDpuItemInfo.nmName() ) && // новое имя уже встречается?
        (aExistingItem == null || !aExistingItem.id().equals( item.id() )) ) // исключим редактируемый элемент
        {
          vr = ValidationResult.warn( FMT_WARN_ITEM_NAME_ALREADY_EXISTS, aDpuItemInfo.nmName() );
        }
      }
      // проверка связей
      ISkClassInfo cinf = coreApi().sysdescr().classInfoManager().getClassInfo( aRefbook.itemClassId() );
      for( String lid : aLinks.keys() ) {
        ISkLinkInfo linf = cinf.linkInfos().findByKey( lid );
        if( linf == null ) {
          return ValidationResult.error( FMT_ERR_NO_SUCH_LINK, lid );
        }
        ValidationResult lvr = linf.linkConstraint().checkErrorSize( aLinks.getByKey( lid ) );
        switch( lvr.type() ) {
          case OK: {
            break;
          }
          case WARNING: {
            vr = ValidationResult.firstNonOk( vr, ValidationResult.warn( FMT_ERR_INV_LINK, lid, lvr.message() ) );
            break;
          }
          case ERROR: {
            return ValidationResult.error( FMT_ERR_INV_LINK, lid, lvr.message() );
          }
          default:
            throw new TsNotAllEnumsUsedRtException( lvr.type().toString() );
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canRemoveItem( ISkRefbook aRefbook, String aItemId ) {
      if( !aRefbook.items().hasKey( aItemId ) ) {
        return ValidationResult.error( FMT_ERR_ITEM_NOT_EXISTS, aItemId );
      }
      return ValidationResult.SUCCESS;
    }

  };

  /**
   * Prevents {@link ISkClassInfoManager} to work with refbook service owned classes.
   */
  private final ISkClassInfoManagerValidator classInfoManagerValidator = new ISkClassInfoManagerValidator() {

    @Override
    public ValidationResult canCreateClass( IDpuSdClassInfo aDpuClassInfo ) {
      return validateClassIsManagedByThisService( aDpuClassInfo.id() );
    }

    @Override
    public ValidationResult canEditClass( IDpuSdClassInfo aDpuClassInfo, ISkClassInfo aOldClassInfo ) {
      return validateClassIsManagedByThisService( aDpuClassInfo.id() );
    }

    @Override
    public ValidationResult canRemoveClass( String aClassId ) {
      return validateClassIsManagedByThisService( aClassId );
    }
  };

  /**
   * Prevents {@link ISkObjectService} to work with refbook service owned objects.
   */
  private final ISkObjectServiceValidator objectServiceValidator = new ISkObjectServiceValidator() {

    @Override
    public ValidationResult canCreateObject( IDpuObject aDpuObject ) {
      return validateObjectIsManagedByThisService( aDpuObject.skid().classId() );
    }

    @Override
    public ValidationResult canEditObject( IDpuObject aDpuObject, ISkObject aOldObject ) {
      return validateObjectIsManagedByThisService( aOldObject.classId() );
    }

    @Override
    public ValidationResult canRemoveObject( Skid aSkid ) {
      return validateObjectIsManagedByThisService( aSkid.classId() );
    }
  };

  /**
   * Prevents {@link ISkLinkService} to work with this service owned objects.
   */
  private final ISkLinkServiceValidator linkServiceValidator =
      aLink -> validateObjectIsManagedByThisService( aLink.leftSkid().classId() );

  /**
   * The rule to check class ID is claimed by this service.
   */
  private static final TextMatcher THIS_SERVICE_CLASS_ID_MATCHER =
      new TextMatcher( ETextMatchMode.STARTS, ISkRefbookServiceHardConstants.CLASSID_PREFIX_OWNED, true );

  /**
   * The rule to check class ID is one of the refbook item's class ID.
   */
  private static final TextMatcher rbItemClassIdMatcher =
      new TextMatcher( ETextMatchMode.STARTS, ISkRefbookServiceHardConstants.CLASSID_START_REFBOOK_ITEM, true );

  final Eventer           eventer           = new Eventer();
  final ValidationSupport validationSupport = new ValidationSupport();
  final SkRefbookHistory  history;

  /**
   * The constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - core API for service developers
   */
  public SkRefbookService( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
    history = new SkRefbookHistory( this );
    validationSupport.addValidator( builtinValidator );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  ValidationResult validateClassIsManagedByThisService( String aClassId ) {
    if( StridUtils.startsWithIdPath( aClassId, CLASSID_PREFIX_OWNED ) ) {
      return ValidationResult.error( FMT_ERR_CLASS_IS_REFBOOK_OWNED, aClassId );
    }
    return ValidationResult.SUCCESS;
  }

  ValidationResult validateObjectIsManagedByThisService( String aClassId ) {
    if( StridUtils.startsWithIdPath( aClassId, CLASSID_PREFIX_OWNED ) ) {
      return ValidationResult.error( FMT_ERR_OBJ_CLASS_IS_REFBOOK_OWNED, aClassId );
    }
    return ValidationResult.SUCCESS;
  }

  // ------------------------------------------------------------------------------------
  // package API
  //

  void pauseExternalValidation() {
    coreApi().sysdescr().classInfoManager().svs().pauseValidator( classInfoManagerValidator );
    coreApi().objService().svs().pauseValidator( objectServiceValidator );
    coreApi().linkService().svs().pauseValidator( linkServiceValidator );
    coreApi().sysdescr().classInfoManager().eventer().pauseFiring();
    coreApi().objService().eventer().pauseFiring();
    coreApi().linkService().eventer().pauseFiring();
  }

  void resumeExternalValidation() {
    coreApi().sysdescr().classInfoManager().svs().resumeValidator( classInfoManagerValidator );
    coreApi().objService().svs().resumeValidator( objectServiceValidator );
    coreApi().linkService().svs().resumeValidator( linkServiceValidator );
    coreApi().sysdescr().classInfoManager().eventer().resumeFiring( true );
    coreApi().objService().eventer().resumeFiring( true );
    coreApi().linkService().eventer().resumeFiring( true );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    ISkClassInfoManager cim = coreApi().sysdescr().classInfoManager();
    cim.claimOnClasses( SERVICE_ID, THIS_SERVICE_CLASS_ID_MATCHER );
    // ensure refbook class CLASSID_REFBOOK existence
    //
    DpuSdClassInfo rbClass = new DpuSdClassInfo( CLASSID_REFBOOK, IGwHardConstants.GW_ROOT_CLASS_ID );
    DpuSdAttrInfo ainf = DpuSdAttrInfo.create1( AID_ITEM_CLASS_ID, DDID_IDPATH, //
        DDEF_NAME, STR_N_ATTR_ITEM_CLASS_ID, //
        DDEF_DESCRIPTION, STR_D_ATTR_ITEM_CLASS_ID //
    );
    rbClass.attrInfos().add( ainf );
    rbClass.eventInfos().add( EVDPU_REFBOOK_ITEM_CHANGE );
    rbClass.params().setBool( OP_SK_CLASS_IS_CODE_DEFINED, true );
    cim.defineClass( rbClass );
    // service validators
    cim.svs().addValidator( classInfoManagerValidator );
    ISkObjectService os = coreApi().objService();
    os.registerObjectCreator( CLASSID_REFBOOK, SkRefbook.CREATOR );
    os.registerObjectCreator( rbItemClassIdMatcher, SkRefbookItem.CREATOR );
    os.svs().addValidator( objectServiceValidator );
    coreApi().linkService().svs().addValidator( linkServiceValidator );
  }

  @Override
  protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ISkRefbookService
  //

  @Override
  public ISkRefbook findRefbook( String aRefbookId ) {
    Skid skid = ISkRefbookServiceHardConstants.makeRefbookObjSkid( aRefbookId );
    return coreApi().objService().find( skid );
  }

  @Override
  public ISkRefbook findRefbookByItemClassId( String aRefbookItemClassId ) {
    String refbookId = makeRefbookIdFromItemClassId( aRefbookItemClassId );
    return findRefbook( refbookId );
  }

  @Override
  public IStridablesList<ISkRefbook> listRefbooks() {
    return new StridablesList<>( coreApi().objService().listObjs( CLASSID_REFBOOK, false ) );
  }

  @Override
  public ISkRefbook defineRefbook( ISkRefbookDpuInfo aDpuRefbookInfo ) {
    // check pre-requisites
    TsNullArgumentRtException.checkNull( aDpuRefbookInfo );
    ISkRefbook oldRb = findRefbook( aDpuRefbookInfo.id() );
    TsValidationFailedRtException.checkError( svs().validator().canDefineRefbook( aDpuRefbookInfo, oldRb ) );
    pauseExternalValidation();
    ISkRefbook rb;
    try {
      ISkClassInfoManager cim = coreApi().sysdescr().classInfoManager();
      ISkObjectService os = coreApi().objService();
      // create item class
      String itemClassId = makeItemClassIdFromRefbookId( aDpuRefbookInfo.id() );
      DpuSdClassInfo itemClassInfo = new DpuSdClassInfo( itemClassId, IGwHardConstants.GW_ROOT_CLASS_ID );
      itemClassInfo.attrInfos().addAll( aDpuRefbookInfo.itemAttrInfos() );
      itemClassInfo.linkInfos().addAll( aDpuRefbookInfo.itemLinkInfos() );
      cim.defineClass( itemClassInfo );
      // create refbook object
      IOptionSetEdit attrs = new OptionSet();
      DDEF_NAME.setValue( attrs, avStr( aDpuRefbookInfo.nmName() ) );
      DDEF_DESCRIPTION.setValue( attrs, avStr( aDpuRefbookInfo.description() ) );
      OP_ATTR_ITEM_CLASS_ID.setValue( attrs, avStr( itemClassId ) );
      DpuObject dpuRb =
          new DpuObject( ISkRefbookServiceHardConstants.makeRefbookObjSkid( aDpuRefbookInfo.id() ), attrs );
      rb = os.defineObject( dpuRb );
    }
    finally {
      resumeExternalValidation();
    }
    // fire event
    ECrudOp op = oldRb != null ? ECrudOp.EDIT : ECrudOp.CREATE;
    eventer.fireRefbookChanged( op, rb.strid() );
    return rb;
  }

  @Override
  public void removeRefbook( String aRefbookId ) {
    TsValidationFailedRtException.checkError( svs().validator().canRemoveRefbook( aRefbookId ) );
    ISkRefbook rb = findRefbook( aRefbookId );
    pauseExternalValidation();
    try {
      ISkClassInfoManager cim = coreApi().sysdescr().classInfoManager();
      ISkObjectService os = coreApi().objService();
      // remove all items objects
      ISkidList toRemove = os.listSkids( rb.itemClassId(), false );
      for( Skid skid : toRemove ) {
        os.removeObject( skid );
      }
      // remove item class
      cim.removeClass( rb.itemClassId() );
      // remove refbook object
      os.removeObject( rb.skid() );
    }
    finally {
      resumeExternalValidation();
    }
    // fire event
    eventer.fireRefbookChanged( ECrudOp.REMOVE, aRefbookId );
  }

  @Override
  public ISkRefbookHistory history() {
    return history;
  }

  @Override
  public ITsValidationSupport<ISkRefbookServiceValidator> svs() {
    return validationSupport;
  }

  @Override
  public ITsEventer<ISkRefbookServiceListener> eventer() {
    return eventer;
  }

}
