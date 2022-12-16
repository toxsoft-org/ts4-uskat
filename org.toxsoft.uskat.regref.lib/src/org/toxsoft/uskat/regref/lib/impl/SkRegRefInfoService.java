package org.toxsoft.uskat.regref.lib.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.regref.lib.impl.ISkRegRefServiceHardConstants.*;
import static org.toxsoft.uskat.regref.lib.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.events.AbstractTsEventer;
import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.bricks.validator.impl.AbstractTsValidationSupport;
import org.toxsoft.core.tslib.bricks.validator.impl.TsValidationFailedRtException;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.helpers.ECrudOp;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.IGwHardConstants;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;
import org.toxsoft.core.tslib.utils.txtmatch.ETextMatchMode;
import org.toxsoft.core.tslib.utils.txtmatch.TextMatcher;
import org.toxsoft.uskat.core.ISkHardConstants;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.api.linkserv.ISkLinkServiceValidator;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;
import org.toxsoft.uskat.core.connection.ISkConnectionConstants;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;
import org.toxsoft.uskat.core.impl.AbstractSkService;
import org.toxsoft.uskat.core.impl.dto.DtoClassInfo;
import org.toxsoft.uskat.core.impl.dto.DtoObject;
import org.toxsoft.uskat.regref.lib.*;

/**
 * Реализация {@link ISkRegRefInfoService}.
 *
 * @author goga
 */
public class SkRegRefInfoService
    extends AbstractSkService
    implements ISkRegRefInfoService {

  /**
   * Синглтон создателя сервиса.
   */
  public static final ISkServiceCreator<SkRegRefInfoService> CREATOR = SkRegRefInfoService::new;

  /**
   * Class for {@link ISkRegRefInfoService#eventer()} instance implementation.
   *
   * @author goga
   */
  class Eventer
      extends AbstractTsEventer<ISkRegRefInfoServiceListener> {

    private boolean wasChanges = false;

    @Override
    protected void doClearPendingEvents() {
      wasChanges = false;
    }

    @Override
    protected void doFirePendingEvents() {
      doFireChanged( ECrudOp.LIST, null );
    }

    @Override
    protected boolean doIsPendingEvents() {
      return wasChanges;
    }

    private void doFireChanged( ECrudOp aOp, String aSectionId ) {
      for( ISkRegRefInfoServiceListener l : listeners() ) {
        try {
          l.onSectionsChanged( aOp, aSectionId );
        }
        catch( Exception ex ) {
          LoggerUtils.errorLogger().error( ex );
        }
      }
    }

    void fireChanged( ECrudOp aOp, String aSectionId ) {
      if( isFiringPaused() ) {
        wasChanges = true;
        return;
      }
      doFireChanged( aOp, aSectionId );
    }

  }

  /**
   * Class for {@link ISkRegRefInfoService#svs()} instance implementation.
   *
   * @author goga
   */
  class ValidationSupport
      extends AbstractTsValidationSupport<ISkRegRefInfoServiceValidator>
      implements ISkRegRefInfoServiceValidator {

    @Override
    public ISkRegRefInfoServiceValidator validator() {
      return this;
    }

    @Override
    public ValidationResult canCreateSection( String aSectionId, String aName, String aDescription,
        IOptionSet aParams ) {
      TsNullArgumentRtException.checkNulls( aSectionId, aName, aDescription, aParams );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkRegRefInfoServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canCreateSection( aSectionId, aName, aDescription, aParams ) );
      }
      return vr;
    }

    @Override
    public ValidationResult canRemoveSection( String aSectionId ) {
      TsNullArgumentRtException.checkNull( aSectionId );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkRegRefInfoServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canRemoveSection( aSectionId ) );
      }
      return vr;
    }

  }

  /**
   * Builtin validation rules.
   */
  private final ISkRegRefInfoServiceValidator builtinValidator = new ISkRegRefInfoServiceValidator() {

    @Override
    public ValidationResult canCreateSection( String aSectionId, String aName, String aDescription,
        IOptionSet aParams ) {
      if( listSections().hasKey( aSectionId ) ) {
        return ValidationResult.error( FMT_ERR_SECTION_ID_ALREADY_ESISTS, aSectionId );
      }
      if( aName.isEmpty() ) {
        return ValidationResult.error( FMT_ERR_EMPTY_NAME );
      }
      if( !StridUtils.isValidIdPath( aSectionId ) ) {
        return ValidationResult.error( FMT_ERR_INV_SECTION_ID, aSectionId );
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canRemoveSection( String aSectionId ) {
      TsNullArgumentRtException.checkNull( aSectionId );
      if( !listSections().hasKey( aSectionId ) ) {
        return ValidationResult.error( FMT_ERR_SECTION_ID_NOT_ESISTS, aSectionId );
      }
      return ValidationResult.SUCCESS;
    }
  };

  /**
   * Prevents {@link ISkSysdescr} to work with refbook service owned classes.
   */
  private final ISkSysdescrValidator classInfoManagerValidator = new ISkSysdescrValidator() {

    @Override
    public ValidationResult canCreateClass( IDtoClassInfo aDpuClassInfo ) {
      return validateClassIsManagedByThisService( aDpuClassInfo.id() );
    }

    @Override
    public ValidationResult canEditClass( IDtoClassInfo aDpuClassInfo, ISkClassInfo aOldClassInfo ) {
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
    public ValidationResult canCreateObject( IDtoObject aDpuObject ) {
      return validateClassIsManagedByThisService( aDpuObject.skid().classId() );
    }

    @Override
    public ValidationResult canEditObject( IDtoObject aDpuObject, ISkObject aOldObject ) {
      return validateClassIsManagedByThisService( aOldObject.classId() );
    }

    @Override
    public ValidationResult canRemoveObject( Skid aSkid ) {
      return validateClassIsManagedByThisService( aSkid.classId() );
    }
  };

  /**
   * Prevents {@link ISkObjectService} to work with refbook service owned objects.
   */
  private final ISkLinkServiceValidator linkServiceValidator = //
      ( aOldLink, aNewLink ) -> validateClassIsManagedByThisService( aNewLink.leftSkid().classId() );

  /**
   * The rule to check class ID is claimed by this service.
   */
  private static final TextMatcher THIS_SERVICE_CLASS_ID_MATCHER =
      new TextMatcher( ETextMatchMode.STARTS, ISkRegRefServiceHardConstants.CLASSID_PREFIX_OWNED, true );

  final Eventer           eventer           = new Eventer();
  final ValidationSupport validationSupport = new ValidationSupport();
  final SkRriHistory      history;

  private final IStridablesListEdit<SkRriSection> sectsList = new StridablesList<>();
  private String                                  authorLogin = TsLibUtils.EMPTY_STRING;

  /**
   * The constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - core API for service developers
   */
  public SkRegRefInfoService( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
    history = new SkRriHistory( this );
    validationSupport.addValidator( builtinValidator );
  }

  // ------------------------------------------------------------------------------------
  // Implementation
  //

  ValidationResult validateClassIsManagedByThisService( String aClassId ) {
    if( aClassId.startsWith( CLASSID_PREFIX_OWNED ) ) {
      return ValidationResult.error( FMT_ERR_CLASS_IS_REGREF_OWNED, aClassId );
    }
    return ValidationResult.SUCCESS;
  }

  // ------------------------------------------------------------------------------------
  // package API
  //

  void pauseExternalValidation() {
    coreApi().sysdescr().svs().pauseValidator( classInfoManagerValidator );
    coreApi().objService().svs().pauseValidator( objectServiceValidator );
    coreApi().linkService().svs().pauseValidator( linkServiceValidator );
  }

  void resumeExternalValidation() {
    coreApi().sysdescr().svs().resumeValidator( classInfoManagerValidator );
    coreApi().objService().svs().resumeValidator( objectServiceValidator );
    coreApi().linkService().svs().resumeValidator( linkServiceValidator );
  }

  String authorLogin() {
    return authorLogin;
  }
  // ------------------------------------------------------------------------------------
  // AbstractSkService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    ISkSysdescr cim = coreApi().sysdescr();
    // ensure RRI section class exists
    DtoClassInfo rriSectionClass =
        new DtoClassInfo( CLASSID_RRI_SECTION, IGwHardConstants.GW_ROOT_CLASS_ID, IOptionSet.NULL );
    rriSectionClass.eventInfos().add( EVDPU_RRI_PARAM_CHANGE );
    rriSectionClass.attrInfos().add( AINF_RRI_SECTION_PARAMS );
    rriSectionClass.params().setBool( ISkHardConstants.OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS, true );
    cim.defineClass( rriSectionClass );
    // service validators
    cim.svs().addValidator( classInfoManagerValidator );
    coreApi().objService().svs().addValidator( objectServiceValidator );
    coreApi().linkService().svs().addValidator( linkServiceValidator );
    // load sections
    IList<ISkObject> rsObjs = coreApi().objService().listObjs( CLASSID_RRI_SECTION, true );
    for( ISkObject sko : rsObjs ) {
      sectsList.add( new SkRriSection( sko, this ) );
    }
    // Логин пользователя
    IAtomicValue argLogin = ISkConnectionConstants.ARGDEF_LOGIN.getValue( aArgs.params() );
    authorLogin = (argLogin != null && argLogin.isAssigned() ? argLogin.asString() : TsLibUtils.EMPTY_STRING);
  }

  @Override
  protected boolean doIsClassClaimedByService( String aClassId ) {
    return THIS_SERVICE_CLASS_ID_MATCHER.accept( aClassId );
  }

  @Override
  protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ISkRegRefInfoService
  //

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  @Override
  public IStridablesList<ISkRriSection> listSections() {
    return (IStridablesList)sectsList;
  }

  @Override
  public ISkRriSection findSection( String aSectionId ) {
    return sectsList.findByKey( aSectionId );
  }

  @Override
  public ISkRriSection getSection( String aSectionId ) {
    return sectsList.getByKey( aSectionId );
  }

  @Override
  public ISkRriSection createSection( String aId, String aName, String aDescription, IOptionSet aParams ) {
    TsValidationFailedRtException.checkError( svs().validator().canCreateSection( aId, aName, aDescription, aParams ) );
    IOptionSetEdit attrs = new OptionSet();
    IAvMetaConstants.DDEF_IDNAME.setValue( attrs, avStr( aName ) );
    IAvMetaConstants.DDEF_DESCRIPTION.setValue( attrs, avStr( aDescription ) );
    attrs.setValobj( AID_RRI_SECTION_PARAMS, aParams );
    DtoObject dtoObj = new DtoObject( new Skid( CLASSID_RRI_SECTION, aId ), attrs, IStringMap.EMPTY );
    SkRriSection sect = null;
    try {
      pauseExternalValidation();
      ISkObject sko = coreApi().objService().defineObject( dtoObj );
      sect = new SkRriSection( sko, this );
      sectsList.add( sect );
    }
    finally {
      resumeExternalValidation();
    }
    // fire event
    eventer.fireChanged( ECrudOp.CREATE, aId );
    return sect;
  }

  @Override
  public void removeSection( String aSectionId ) {
    TsValidationFailedRtException.checkError( svs().validator().canRemoveSection( aSectionId ) );
    try {
      pauseExternalValidation();
      SkRriSection sect = sectsList.getByKey( aSectionId );
      sect.removeAll( aSectionId );
      coreApi().objService().removeObject( sect.getRriSectionObjectSkid() );
      sectsList.removeById( aSectionId );
    }
    finally {
      resumeExternalValidation();
    }
    // fire event
    eventer.fireChanged( ECrudOp.REMOVE, aSectionId );
  }

  @Override
  public ISkRriHistory history() {
    return history;
  }

  @Override
  public ITsValidationSupport<ISkRegRefInfoServiceValidator> svs() {
    return validationSupport;
  }

  @Override
  public ITsEventer<ISkRegRefInfoServiceListener> eventer() {
    return eventer;
  }

}
