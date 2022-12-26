package org.toxsoft.uskat.onews.lib.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.gw.IGwHardConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.onews.lib.IOneWsConstants.*;
import static org.toxsoft.uskat.onews.lib.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.onews.lib.*;

/**
 * {@link ISkOneWsService} implementation.
 *
 * @author hazard157
 */
public class SkExtServOneWs
    extends AbstractSkService
    implements ISkOneWsService {

  // TODO listen to roles list change and remove removed roles from the links right objects

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkExtServOneWs::new;

  /**
   * {@link ISkOneWsService#svs()} implementation.
   *
   * @author hazard157
   */
  static class Svs
      extends AbstractTsValidationSupport<ISkOneWsServiceValidator>
      implements ISkOneWsServiceValidator {

    @Override
    public ISkOneWsServiceValidator validator() {
      return this;
    }

    @Override
    public ValidationResult canSetRoleProfile( String aRoleId, String aProfileId ) {
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkOneWsServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canSetRoleProfile( aRoleId, aProfileId ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canCreateProfile( String aProfileId, IOptionSet aAttrs, IList<OneWsRule> aRules ) {
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkOneWsServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canCreateProfile( aProfileId, aAttrs, aRules ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canEditProfile( String aProfileId, IOptionSet aAttrs, IList<OneWsRule> aRules,
        IOneWsProfile aExistingProfile ) {
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkOneWsServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canEditProfile( aProfileId, aAttrs, aRules, aExistingProfile ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canRemoveProfile( String aProfileId ) {
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkOneWsServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canRemoveProfile( aProfileId ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canDefineAbilityKind( IStridableParameterized aKind ) {
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkOneWsServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canDefineAbilityKind( aKind ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canDefineAbility( IOneWsAbility aAbility ) {
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkOneWsServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canDefineAbility( aAbility ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

  }

  private final ISkOneWsServiceValidator builtinValidator = new ISkOneWsServiceValidator() {

    @Override
    public ValidationResult canSetRoleProfile( String aRoleId, String aProfileId ) {
      TsNullArgumentRtException.checkNulls( aRoleId, aProfileId );
      if( !listProfiles().hasKey( aProfileId ) ) {
        return ValidationResult.warn( FMT_ERR_PROFILE_NOT_EXISTS, aProfileId );
      }
      if( !userService().listRoles().hasKey( aRoleId ) ) {
        return ValidationResult.warn( FMT_ERR_ROLE_NOT_EXISTS, aRoleId );
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canCreateProfile( String aProfileId, IOptionSet aAttrs, IList<OneWsRule> aRules ) {
      TsNullArgumentRtException.checkNulls( aProfileId, aAttrs, aRules );
      if( !listProfiles().hasKey( aProfileId ) ) {
        return ValidationResult.warn( FMT_ERR_PROFILE_EXISTS, aProfileId );
      }
      if( !StridUtils.isValidIdPath( aProfileId ) ) {
        return ValidationResult.warn( FMT_ERR_PROFILE_ID_NOT_IDPATH, aProfileId );
      }
      if( aRules.isEmpty() ) {
        return ValidationResult.error( FMT_ERR_EMPTY_RULES, aProfileId );
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canEditProfile( String aProfileId, IOptionSet aAttrs, IList<OneWsRule> aRules,
        IOneWsProfile aExistingProfile ) {
      TsNullArgumentRtException.checkNulls( aProfileId, aAttrs, aRules, aExistingProfile );
      if( OWS_BUILTIN_PROFILE_IDS.hasElem( aProfileId ) ) {
        return ValidationResult.warn( FMT_ERR_ATTEMPT_CHANGE_BUILTIN_PROFILE, aProfileId );
      }
      if( !listProfiles().hasKey( aProfileId ) ) {
        return ValidationResult.warn( FMT_ERR_PROFILE_NOT_EXISTS, aProfileId );
      }
      if( aRules.isEmpty() ) {
        return ValidationResult.error( FMT_ERR_EMPTY_RULES, aProfileId );
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canRemoveProfile( String aProfileId ) {
      TsNullArgumentRtException.checkNull( aProfileId );
      if( OWS_BUILTIN_PROFILE_IDS.hasElem( aProfileId ) ) {
        return ValidationResult.warn( FMT_ERR_ATTEMPT_REMOVE_BUILTIN_PROFILE, aProfileId );
      }
      if( !listProfiles().hasKey( aProfileId ) ) {
        return ValidationResult.warn( FMT_WARN_PROFILE_NOT_EXISTS, aProfileId );
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canDefineAbilityKind( IStridableParameterized aKind ) {
      TsNullArgumentRtException.checkNull( aKind );
      if( BUILTIN_ABILITY_KINDS.hasKey( aKind.id() ) ) {
        return ValidationResult.warn( FMT_ERR_ATTEMT_CHANGE_BUILTIN_KIND, aKind.id() );
      }
      if( listKnownAbilityKinds().hasKey( aKind.id() ) ) {
        return ValidationResult.warn( FMT_WARN_ABILITY_KIND_EXISTS, aKind.id() );
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canDefineAbility( IOneWsAbility aAbility ) {
      TsNullArgumentRtException.checkNull( aAbility );
      if( listKnownAbilities().hasKey( aAbility.id() ) ) {
        return ValidationResult.warn( FMT_WARN_ABILITY_EXISTS, aAbility.id() );
      }
      return ValidationResult.SUCCESS;
    }

  };

  private final ClassClaimingCoreValidator claimingValidator = new ClassClaimingCoreValidator();

  private final IStridablesListEdit<IStridableParameterized> knownAbilityKinds = new StridablesList<>();
  private final IStridablesListEdit<IOneWsAbility>           knownAbilities    = new StridablesList<>();

  /**
   * Builtin objects name/description is always in english, no need to localize in Java code.
   * <p>
   * As usual, localization may be done by means of {@link ICoreL10n}.
   */
  private static final String STR_N_ROOT_PROFILE  = "Root";                           //$NON-NLS-1$
  private static final String STR_D_ROOT_PROFILE  = "Root profile allows everything"; //$NON-NLS-1$
  private static final String STR_N_GUEST_PROFILE = "Guest";                          //$NON-NLS-1$
  private static final String STR_D_GUEST_PROFILE = "Guest prfile allows nothing";    //$NON-NLS-1$

  private final Svs svs = new Svs();

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  public SkExtServOneWs( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
    svs.addValidator( builtinValidator );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // create class for IOneWsProfile
    IDtoClassInfo roleCinf = internalCreateRoleClassDto();
    sysdescr().defineClass( roleCinf );
    objServ().registerObjectCreator( IOneWsProfile.CLASS_ID, SkOneWsProfile.CREATOR );
    // create root profile
    if( objServ().find( OWS_SKID_PROFILE_ROOT ) == null ) {
      DtoFullObject fobj = DtoFullObject.createDtoFullObject( OWS_SKID_PROFILE_ROOT, coreApi() );
      fobj.attrs().setStr( AID_NAME, STR_N_ROOT_PROFILE );
      fobj.attrs().setStr( AID_DESCRIPTION, STR_D_ROOT_PROFILE );
      fobj.clobs().put( CLBID_PROFILE_RULES,
          OneWsRule.KEEPER.coll2str( new SingleItemList<>( OneWsRule.RULE_ALLOW_ALL ) ) );
      fobj.links().ensureSkidList( LNKID_ROLES ).add( ISkUserServiceHardConstants.SKID_ROLE_ROOT );
      DtoFullObject.defineFullObject( coreApi(), fobj );
    }
    // create guest profile
    if( objServ().find( OWS_SKID_PROFILE_GUEST ) == null ) {
      DtoFullObject fobj = DtoFullObject.createDtoFullObject( OWS_SKID_PROFILE_GUEST, coreApi() );
      fobj.attrs().setStr( AID_NAME, STR_N_GUEST_PROFILE );
      fobj.attrs().setStr( AID_DESCRIPTION, STR_D_GUEST_PROFILE );
      fobj.clobs().put( CLBID_PROFILE_RULES,
          OneWsRule.KEEPER.coll2str( new SingleItemList<>( OneWsRule.RULE_DENY_ALL ) ) );
      fobj.links().ensureSkidList( LNKID_ROLES ).add( ISkUserServiceHardConstants.SKID_ROLE_GUEST );
      DtoFullObject.defineFullObject( coreApi(), fobj );
    }
    // add builtin kinds
    for( IStridableParameterized k : BUILTIN_ABILITY_KINDS ) {
      knownAbilityKinds.add( k );
    }
    //
    sysdescr().svs().addValidator( claimingValidator );
    objServ().svs().addValidator( claimingValidator );
    linkService().svs().addValidator( claimingValidator );
  }

  @Override
  protected void doClose() {
    // nop
  }

  @Override
  protected boolean doIsClassClaimedByService( String aClassId ) {
    return switch( aClassId ) {
      case IOneWsProfile.CLASS_ID -> true;
      default -> false;
    };
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Creates DTO of {@link IOneWsProfile#CLASS_ID} class.
   *
   * @return {@link IDtoClassInfo} - {@link IOneWsProfile#CLASS_ID} class info
   */
  private static IDtoClassInfo internalCreateRoleClassDto() {
    DtoClassInfo cinf = new DtoClassInfo( CLSID_OWS_PROFILE, GW_ROOT_CLASS_ID, IOptionSet.NULL );
    OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS.setValue( cinf.params(), AV_TRUE );
    OPDEF_SK_IS_SOURCE_USKAT_CORE_CLASS.setValue( cinf.params(), AV_TRUE );
    cinf.attrInfos().add( ATRINF_PROFILE_PARAMS );
    cinf.clobInfos().add( CLBINF_PROFILE_RULES );
    cinf.linkInfos().add( LNKINF_PROFILE_ROLES );
    return cinf;
  }

  private void pauseCoreValidation() {
    sysdescr().svs().pauseValidator( claimingValidator );
    objServ().svs().pauseValidator( claimingValidator );
    linkService().svs().pauseValidator( claimingValidator );
  }

  private void resumeCoreValidation() {
    sysdescr().svs().resumeValidator( claimingValidator );
    objServ().svs().resumeValidator( claimingValidator );
    linkService().svs().resumeValidator( claimingValidator );
  }

  // ------------------------------------------------------------------------------------
  // ISkOneWsService
  //

  @Override
  public IStridablesList<IOneWsProfile> listProfiles() {
    IList<IOneWsProfile> ll = objServ().listObjs( IOneWsProfile.CLASS_ID, false );
    return new StridablesList<>( ll );
  }

  @Override
  public IOneWsProfile findProfileById( String aProfileId ) {
    TsNullArgumentRtException.checkNull( aProfileId );
    return coreApi().objService().find( new Skid( IOneWsProfile.CLASS_ID, aProfileId ) );
  }

  @Override
  public IOneWsProfile getProfileByRoleId( String aRoleId ) {
    StridUtils.checkValidIdPath( aRoleId );
    ISkRole role = coreApi().userService().getRole( aRoleId );
    TsItemNotFoundRtException.checkNull( role, FMT_ERR_NO_ROLE_ID, aRoleId );
    IDtoLinkRev lr = linkService().getLinkRev( IOneWsProfile.CLASS_ID, LNKID_ROLES, role.skid() );
    if( !lr.leftSkids().isEmpty() ) {
      IOneWsProfile p = objServ().find( lr.leftSkids().first() );
      if( p != null ) {
        return p;
      }
    }
    // return guest profile as default
    return objServ().get( OWS_SKID_PROFILE_GUEST );
  }

  @Override
  public IStridablesList<IStridableParameterized> listKnownAbilityKinds() {
    return knownAbilityKinds;
  }

  @Override
  public IStridablesList<IOneWsAbility> listKnownAbilities() {
    return knownAbilities;
  }

  @Override
  public void setRoleProfile( String aRoleId, String aProfileId ) {
    TsValidationFailedRtException.checkError( svs.validator().canSetRoleProfile( aRoleId, aProfileId ) );
    ISkRole role = coreApi().userService().getRole( aRoleId );
    // start
    pauseCoreValidation();
    try {
      // if role is already assosiated to profile cancel association
      IDtoLinkRev lr = coreApi().linkService().getLinkRev( IOneWsProfile.CLASS_ID, LNKID_ROLES, role.skid() );
      if( !lr.leftSkids().isEmpty() ) {
        // remove all associated profiles from role if they were because of some error
        for( Skid roleProfSkid : lr.leftSkids() ) {
          IDtoLinkFwd lf1 = linkService().getLinkFwd( roleProfSkid, LNKID_ROLES );
          IDtoLinkFwd lf2 = DtoLinkFwd.createCopyMinusSkid( lf1, role.skid() );
          if( !lf1.equals( lf2 ) ) {
            coreApi().linkService().setLink( lf2.leftSkid(), lf2.linkId(), lf2.rightSkids() );
          }
        }
      }
      // add role to the profile
      IOneWsProfile profile = findProfileById( aProfileId );
      IDtoLinkFwd lf1 = coreApi().linkService().getLinkFwd( profile.skid(), LNKID_ROLES );
      // remove all unexisting roles if they were because of some error
      if( !lf1.rightSkids().isEmpty() ) {
        DtoLinkFwd lfCorrected = new DtoLinkFwd( lf1.gwid(), lf1.leftSkid(), ISkidList.EMPTY );
        for( Skid roleSkid : lf1.rightSkids() ) {
          if( objServ().find( roleSkid ) != null ) {
            lfCorrected.rightSkids().add( roleSkid );
          }
        }
        if( lfCorrected.rightSkids().size() != lf1.rightSkids().size() ) {
          coreApi().linkService().setLink( lfCorrected );
          lf1 = coreApi().linkService().getLinkFwd( profile.skid(), LNKID_ROLES );
        }
      }
      // add aRoleId role to the link's right objects
      IDtoLinkFwd lf2 = DtoLinkFwd.createCopyPlusSkid( lf1, role.skid() );
      coreApi().linkService().setLink( lf2 );
    }
    finally {
      resumeCoreValidation();
    }
  }

  @Override
  public IOneWsProfile defineProfile( String aProfileId, IOptionSet aAttrs, IList<OneWsRule> aRules ) {
    TsNullArgumentRtException.checkNulls( aProfileId, aAttrs, aRules );
    IOneWsProfile oldProf = findProfileById( aProfileId );
    if( oldProf != null ) {
      TsValidationFailedRtException.checkError( svs.validator().canEditProfile( aProfileId, aAttrs, aRules, oldProf ) );
    }
    else {
      TsValidationFailedRtException.checkError( svs.validator().canCreateProfile( aProfileId, aAttrs, aRules ) );
    }
    Skid skid = new Skid( CLSID_OWS_PROFILE, aProfileId );
    DtoFullObject fobj = DtoFullObject.createDtoFullObject( skid, coreApi() );
    fobj.attrs().refreshSet( aAttrs );
    fobj.clobs().put( CLBID_PROFILE_RULES, OneWsRule.KEEPER.coll2str( aRules ) );
    pauseCoreValidation();
    try {
      return DtoFullObject.defineFullObject( coreApi(), fobj );
    }
    finally {
      resumeCoreValidation();
    }
  }

  @Override
  public void removeProfile( String aProfileId ) {
    TsValidationFailedRtException.checkError( svs.validator().canRemoveProfile( aProfileId ) );
    Skid skid = new Skid( CLSID_OWS_PROFILE, aProfileId );
    try {
      objServ().removeObject( skid );
    }
    finally {
      resumeCoreValidation();
    }
  }

  @Override
  public void defineAbilityKind( IStridableParameterized aKind ) {
    TsValidationFailedRtException.checkError( svs.validator().canDefineAbilityKind( aKind ) );
    knownAbilityKinds.add( aKind );
  }

  @Override
  public void defineAbility( IOneWsAbility aAbility ) {
    TsValidationFailedRtException.checkError( svs.validator().canDefineAbility( aAbility ) );
    knownAbilities.add( aAbility );
  }

  @Override
  public ITsValidationSupport<ISkOneWsServiceValidator> svs() {
    return svs;
  }

}
