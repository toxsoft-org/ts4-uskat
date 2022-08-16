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

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkExtServOneWs::new;

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

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  public SkExtServOneWs( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
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
    DtoFullObject fobj = DtoFullObject.createDtoFullObject( OWS_SKID_PROFILE_ROOT, coreApi() );
    fobj.attrs().setStr( AID_NAME, STR_N_ROOT_PROFILE );
    fobj.attrs().setStr( AID_DESCRIPTION, STR_D_ROOT_PROFILE );
    fobj.attrs().setStr( ATRID_PROFILE_RULES,
        OneWsRule.KEEPER.coll2str( new SingleItemList<>( OneWsRule.RULE_ALLOW_ALL ) ) );
    fobj.links().ensureSkidList( LNKID_ROLES ).add( ISkUserServiceHardConstants.SKID_ROLE_ROOT );
    DtoFullObject.defineFullObject( coreApi(), fobj );
    // create guest profile
    fobj = DtoFullObject.createDtoFullObject( OWS_SKID_PROFILE_GUEST, coreApi() );
    fobj.attrs().setStr( AID_NAME, STR_N_GUEST_PROFILE );
    fobj.attrs().setStr( AID_DESCRIPTION, STR_D_GUEST_PROFILE );
    fobj.attrs().setStr( ATRID_PROFILE_RULES,
        OneWsRule.KEEPER.coll2str( new SingleItemList<>( OneWsRule.RULE_DENY_ALL ) ) );
    fobj.links().ensureSkidList( LNKID_ROLES ).add( ISkUserServiceHardConstants.SKID_ROLE_GUEST );
    DtoFullObject.defineFullObject( coreApi(), fobj );
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
    cinf.attrInfos().add( ATRINF_PROFILE_RULES );
    cinf.attrInfos().add( ATRINF_PROFILE_PARAMS );
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

  private ValidationResult canDefineProfile( String aProfileId, IOptionSet aAttrs, IList<OneWsRule> aRules ) {
    TsNullArgumentRtException.checkNulls( aProfileId, aAttrs, aRules );
    IOneWsProfile p = findProfileById( aProfileId );
    if( p != null && p.isBuiltinProfile() ) {
      return ValidationResult.error( FMT_ERR_CAN_TOUCH_BUILTIN_PROFILE, aProfileId );
    }
    if( aRules.isEmpty() ) {
      return ValidationResult.error( FMT_ERR_EMPTY_RULES, aProfileId );
    }
    return ValidationResult.SUCCESS;
  }

  private ValidationResult canRemoveProfile( String aProfileId ) {
    TsNullArgumentRtException.checkNull( aProfileId );
    IOneWsProfile p = findProfileById( aProfileId );
    if( p != null && p.isBuiltinProfile() ) {
      return ValidationResult.error( FMT_ERR_CAN_TOUCH_BUILTIN_PROFILE, aProfileId );
    }
    return ValidationResult.SUCCESS;
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
    ISkRole role = coreApi().userService().findRole( aRoleId );
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
    TsNullArgumentRtException.checkNulls( aRoleId, aProfileId );
    // check both role and profile exists
    ISkRole role = coreApi().userService().findRole( aRoleId );
    TsItemNotFoundRtException.checkNull( role, FMT_ERR_NO_ROLE_ID, aRoleId );
    IOneWsProfile p = getProfileByRoleId( aProfileId );
    // start
    pauseCoreValidation();
    try {
      // if role is already assosiated to profile cancel association
      IDtoLinkRev lr = coreApi().linkService().getLinkRev( IOneWsProfile.CLASS_ID, LNKID_ROLES, role.skid() );
      if( !lr.leftSkids().isEmpty() ) {
        // remove all associated profiles from role if they were because of some some error
        for( Skid roleProfSkid : lr.leftSkids() ) {
          IDtoLinkFwd lf1 = linkService().getLinkFwd( roleProfSkid, LNKID_ROLES );
          IDtoLinkFwd lf2 = DtoLinkFwd.createCopyMinusSkid( lf1, role.skid() );
          coreApi().linkService().setLink( lf2.leftSkid(), lf2.linkId(), lf2.rightSkids() );
        }
      }
      // add role to the profile
      IDtoLinkFwd lf1 = coreApi().linkService().getLinkFwd( p.skid(), LNKID_ROLES );
      IDtoLinkFwd lf2 = DtoLinkFwd.createCopyPlusSkid( lf1, role.skid() );
      coreApi().linkService().setLink( lf2.leftSkid(), lf2.linkId(), lf2.rightSkids() );
    }
    finally {
      resumeCoreValidation();
    }
  }

  @Override
  public IOneWsProfile defineProfile( String aProfileId, IOptionSet aAttrs, IList<OneWsRule> aRules ) {
    TsValidationFailedRtException.checkError( canDefineProfile( aProfileId, aAttrs, aRules ) );
    Skid skid = new Skid( CLSID_OWS_PROFILE, aProfileId );
    DtoFullObject fobj = DtoFullObject.createDtoFullObject( skid, coreApi() );
    fobj.attrs().refreshSet( aAttrs );
    fobj.attrs().setStr( ATRID_PROFILE_RULES, OneWsRule.KEEPER.coll2str( aRules ) );
    pauseCoreValidation();
    try {
      return objServ().defineObject( fobj );
    }
    finally {
      resumeCoreValidation();
    }
  }

  @Override
  public void removeProfile( String aProfileId ) {
    TsValidationFailedRtException.checkError( canRemoveProfile( aProfileId ) );
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
    knownAbilityKinds.add( aKind );
  }

  @Override
  public void defineAbility( IOneWsAbility aAbility ) {
    knownAbilities.add( aAbility );
  }

}
