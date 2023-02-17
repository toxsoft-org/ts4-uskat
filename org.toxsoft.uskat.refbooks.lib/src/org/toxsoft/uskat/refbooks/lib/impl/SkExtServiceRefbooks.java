package org.toxsoft.uskat.refbooks.lib.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.refbooks.lib.ISkRefbookServiceHardConstants.*;
import static org.toxsoft.uskat.refbooks.lib.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.refbooks.lib.*;

/**
 * {@link ISkRefbookService} implementation.
 *
 * @author hazard157
 */
public class SkExtServiceRefbooks
    extends AbstractSkService
    implements ISkRefbookService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkExtServiceRefbooks::new;

  /**
   * {@link ISkRefbookService#eventer()} implementation.
   *
   * @author hazard157
   */
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
      if( wasRefbooksListChanged ) {
        doFireRefbookChanged( ECrudOp.LIST, null );
      }
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

  /**
   * {@link ISkRefbookService#svs()} implementation.
   *
   * @author hazard157
   */
  class ValidationSupport
      extends AbstractTsValidationSupport<ISkRefbookServiceValidator>
      implements ISkRefbookServiceValidator {

    @Override
    public ISkRefbookServiceValidator validator() {
      return this;
    }

    @Override
    public ValidationResult canDefineRefbook( IDtoRefbookInfo aRefbookInfo, ISkRefbook aExistingRefbook ) {
      TsNullArgumentRtException.checkNull( aRefbookInfo );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkRefbookServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canDefineRefbook( aRefbookInfo, aExistingRefbook ) );
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
    public ValidationResult canDefineItem( ISkRefbook aRefbook, IDtoFullObject aDpuItemInfo,
        ISkRefbookItem aExistingItem ) {
      TsNullArgumentRtException.checkNulls( aRefbook, aDpuItemInfo );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkRefbookServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canDefineItem( aRefbook, aDpuItemInfo, aExistingItem ) );
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
    public ValidationResult canDefineRefbook( IDtoRefbookInfo aRefbookInfo, ISkRefbook aExistingRefbook ) {
      IStridablesList<ISkRefbook> rbs = listRefbooks();
      ValidationResult vr = ValidationResult.SUCCESS;
      // создание нового справочника
      if( aExistingRefbook == null ) {
        if( rbs.hasKey( aRefbookInfo.id() ) ) {
          return ValidationResult.error( FMT_ERR_REFBOOK_ALREADY_EXISTS, aRefbookInfo.id() );
        }
      }
      // предупреждение о дублировании имени
      for( ISkRefbook rb : rbs ) {
        if( rb.nmName().equals( aRefbookInfo.nmName() ) && // новое имя уже встречается?
        (aExistingRefbook == null || !aExistingRefbook.id().equals( rb.id() )) ) // исключим редактируемый справочник
        {
          vr = ValidationResult.warn( FMT_WARN_RB_NAME_ALREADY_EXISTS, aRefbookInfo.nmName() );
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
    public ValidationResult canDefineItem( ISkRefbook aRefbook, IDtoFullObject aItemInfo,
        ISkRefbookItem aExistingItem ) {
      IStridablesList<ISkRefbookItem> items = new StridablesList<>( aRefbook.listItems() );
      ValidationResult vr = ValidationResult.SUCCESS;
      // проверка дублирования при создании нового элемента
      if( aExistingItem == null ) {
        if( items.hasKey( aItemInfo.id() ) ) {
          return ValidationResult.error( FMT_ERR_ITEM_ALREADY_EXISTS, aItemInfo.strid() );
        }
      }
      // предупреждение о дублировании имени
      for( ISkRefbookItem item : items ) {
        if( item.nmName().equals( aItemInfo.nmName() ) && // новое имя уже встречается?
        (aExistingItem == null || !aExistingItem.id().equals( item.id() )) ) // исключим редактируемый элемент
        {
          vr = ValidationResult.warn( FMT_WARN_ITEM_NAME_ALREADY_EXISTS, aItemInfo.nmName() );
        }
      }
      // check links
      ISkClassInfo cinf = coreApi().sysdescr().getClassInfo( aRefbook.itemClassId() );
      for( String lid : aItemInfo.links().map().keys() ) {
        IDtoLinkInfo linf = cinf.links().list().findByKey( lid );
        if( linf == null ) {
          return ValidationResult.error( FMT_ERR_NO_SUCH_LINK, lid );
        }
        ValidationResult lvr = linf.linkConstraint().checkErrorSize( aItemInfo.links().map().getByKey( lid ) );
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
      // TODO other props: clobs, rivets, ...
      return vr;
    }

    @Override
    public ValidationResult canRemoveItem( ISkRefbook aRefbook, String aItemId ) {
      if( !aRefbook.listItems().hasKey( aItemId ) ) {
        return ValidationResult.error( FMT_ERR_ITEM_NOT_EXISTS, aItemId );
      }
      return ValidationResult.SUCCESS;
    }

  };

  /**
   * Eventer is the part of the package API.
   */
  final Eventer eventer = new Eventer();

  /**
   * Validator is the part of the package API.
   */
  final ValidationSupport validationSupport = new ValidationSupport();

  private final ClassClaimingCoreValidator claimingValidator = new ClassClaimingCoreValidator();

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkExtServiceRefbooks( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
    validationSupport.addValidator( builtinValidator );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // TODO ensure refbook class CLSID_REFBOOK existence

    // TODO register refbook and default items object creator
    // os.registerObjectCreator( CLASSID_REFBOOK, SkRefbook.CREATOR );
    // os.registerObjectCreator( rbItemClassIdMatcher, SkRefbookItem.CREATOR );

    // claim on classes
    sysdescr().svs().addValidator( claimingValidator );
    objServ().svs().addValidator( claimingValidator );
    linkService().svs().addValidator( claimingValidator );
    clobService().svs().addValidator( claimingValidator );
  }

  @Override
  protected void doClose() {
    // nop
  }

  @Override
  protected boolean doIsClassClaimedByService( String aClassId ) {
    return aClassId.startsWith( CLSID_PREFIX );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  // ------------------------------------------------------------------------------------
  // package API
  //

  void pauseCoreValidation() {
    sysdescr().svs().pauseValidator( claimingValidator );
    objServ().svs().pauseValidator( claimingValidator );
    linkService().svs().pauseValidator( claimingValidator );
    clobService().svs().pauseValidator( claimingValidator );
  }

  void resumeCoreValidation() {
    sysdescr().svs().resumeValidator( claimingValidator );
    objServ().svs().resumeValidator( claimingValidator );
    linkService().svs().resumeValidator( claimingValidator );
    clobService().svs().resumeValidator( claimingValidator );
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
    return new StridablesList<>( coreApi().objService().listObjs( CLSID_REFBOOK, false ) );
  }

  @Override
  public ISkRefbook defineRefbook( IDtoRefbookInfo aDpuRefbookInfo ) {
    // check pre-requisites
    TsNullArgumentRtException.checkNull( aDpuRefbookInfo );
    ISkRefbook oldRb = findRefbook( aDpuRefbookInfo.id() );
    TsValidationFailedRtException.checkError( svs().validator().canDefineRefbook( aDpuRefbookInfo, oldRb ) );
    pauseCoreValidation();
    ISkRefbook rb;
    try {
      ISkSysdescr sd = coreApi().sysdescr();
      ISkObjectService os = coreApi().objService();
      // create item class
      String itemClassId = makeItemClassIdFromRefbookId( aDpuRefbookInfo.id() );
      DtoClassInfo itemClassInf = new DtoClassInfo( itemClassId, IGwHardConstants.GW_ROOT_CLASS_ID, IOptionSet.NULL );
      OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS.setValue( itemClassInf.params(), AV_TRUE );
      itemClassInf.attrInfos().addAll( aDpuRefbookInfo.attrInfos() );
      itemClassInf.linkInfos().addAll( aDpuRefbookInfo.linkInfos() );
      itemClassInf.rivetInfos().addAll( aDpuRefbookInfo.rivetInfos() );
      itemClassInf.clobInfos().addAll( aDpuRefbookInfo.clobInfos() );
      sd.defineClass( itemClassInf );
      // create refbook object
      IOptionSetEdit attrs = new OptionSet();
      DDEF_NAME.setValue( attrs, avStr( aDpuRefbookInfo.nmName() ) );
      DDEF_DESCRIPTION.setValue( attrs, avStr( aDpuRefbookInfo.description() ) );
      attrs.setStr( ATRINF_ITEM_CLASS_ID.id(), itemClassId );
      Skid rbObjSkid = ISkRefbookServiceHardConstants.makeRefbookObjSkid( aDpuRefbookInfo.id() );
      DtoObject rbDto = new DtoObject( rbObjSkid, attrs, IStringMap.EMPTY );
      rb = os.defineObject( rbDto );
    }
    finally {
      resumeCoreValidation();
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
    pauseCoreValidation();
    try {
      ISkSysdescr sd = coreApi().sysdescr();
      ISkObjectService os = coreApi().objService();
      // remove all items objects
      ISkidList toRemove = os.listSkids( rb.itemClassId(), false );
      for( Skid skid : toRemove ) {
        os.removeObject( skid );
      }
      // remove item class
      sd.removeClass( rb.itemClassId() );
      // remove refbook object
      os.removeObject( rb.skid() );
    }
    finally {
      resumeCoreValidation();
    }
    // fire event
    eventer.fireRefbookChanged( ECrudOp.REMOVE, aRefbookId );
  }

  @Override
  public ITimedList<SkEvent> queryRefbookEditHistory( IQueryInterval aInterval, String aRefbookId ) {
    TsNullArgumentRtException.checkNull( aInterval );
    Skid refbookSkid = makeRefbookObjSkid( aRefbookId );
    Gwid gwid = Gwid.createEvent( refbookSkid.classId(), refbookSkid.strid(), EVID_REFBOOK_ITEM_CHANGE );
    return eventService().queryObjEvents( aInterval, gwid );
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
