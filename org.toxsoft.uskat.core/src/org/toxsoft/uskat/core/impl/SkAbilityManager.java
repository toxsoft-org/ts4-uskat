package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;
import static org.toxsoft.uskat.core.l10n.ISkCoreSharedResources.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.bricks.validator.std.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.api.users.ability.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * {@link ISkAbilityManager} implementation.
 * <p>
 * Note: USkat entities used by ability manager are initialized in {@link SkCoreServUsers#doInit(ITsContextRo)}
 *
 * @author hazard157
 */
class SkAbilityManager
    implements ISkAbilityManager {

  /**
   * Builds message to the user service siblings to generate event
   * {@link ISkAbilityManagerListener#onAbilitiesListChanged(ISkUserService)}
   *
   * @author hazard157
   */
  private static class BaMsgBuilderAbilitiesListChanged
      extends AbstractBackendMessageBuilder {

    static final String MSG_ID = "AbilitiesListChanged"; //$NON-NLS-1$

    static final BaMsgBuilderAbilitiesListChanged INSTANCE = new BaMsgBuilderAbilitiesListChanged();

    private BaMsgBuilderAbilitiesListChanged() {
      super( ISkUserService.SERVICE_ID, MSG_ID );
    }

    GtMessage makeMessage() {
      return makeMessageVarargs();
    }

  }

  /**
   * Builds message to the user service siblings to generate event
   * {@link ISkAbilityManagerListener#onRoleAbilitiesChanged(ISkUserService, IStringList)}
   *
   * @author hazard157
   */
  private static class BaMsgBuilderRoleAbilitiesChanged
      extends AbstractBackendMessageBuilder {

    static final String MSG_ID                = "RoleAbilitiesChanged"; //$NON-NLS-1$
    static final String OPID_ABILITY_IDS_LIST = "AbilityIdsList";       //$NON-NLS-1$

    static final BaMsgBuilderRoleAbilitiesChanged INSTANCE = new BaMsgBuilderRoleAbilitiesChanged();

    private BaMsgBuilderRoleAbilitiesChanged() {
      super( ISkUserService.SERVICE_ID, MSG_ID );
      defineArgValobj( OPID_ABILITY_IDS_LIST, StringListKeeper.KEEPER_ID, true );
    }

    GtMessage makeMessage( IStringList aAbilityIdsList ) {
      return makeMessageVarargs( OPID_ABILITY_IDS_LIST, avValobj( aAbilityIdsList ) );
    }

    IStringList getAbilityIds( GenericMessage aMsg ) {
      return getArg( aMsg, OPID_ABILITY_IDS_LIST ).asValobj();
    }

  }

  private final SkCoreServUsers userService;

  /**
   * {@link ISkAbilityManager#svs()} implementation.
   *
   * @author hazard157
   */
  class Svs
      extends AbstractTsValidationSupport<ISkAbilityManagerValidator>
      implements ISkAbilityManagerValidator {

    @Override
    public ISkAbilityManagerValidator validator() {
      return this;
    }

    @Override
    public ValidationResult canChangeRoleAbilities( String aRoleId, IStringList aAbilityIds, boolean aEnable ) {
      TsNullArgumentRtException.checkNulls( aAbilityIds, aRoleId );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkAbilityManagerValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canChangeRoleAbilities( aRoleId, aAbilityIds, aEnable ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canSetRoleAbilities( String aRoleId, IStringList aAbilityIds ) {
      TsNullArgumentRtException.checkNulls( aAbilityIds, aRoleId );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkAbilityManagerValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canSetRoleAbilities( aRoleId, aAbilityIds ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canDefineAbility( IDtoSkAbility aDto, ISkAbility aExistingAbility ) {
      TsNullArgumentRtException.checkNull( aDto );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkAbilityManagerValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canDefineAbility( aDto, aExistingAbility ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canRemoveAbility( String aAbilityId ) {
      TsNullArgumentRtException.checkNull( aAbilityId );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkAbilityManagerValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canRemoveAbility( aAbilityId ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canDefineKind( IDtoSkAbilityKind aDto, ISkAbilityKind aExistingKind ) {
      TsNullArgumentRtException.checkNull( aDto );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkAbilityManagerValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canDefineKind( aDto, aExistingKind ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canRemoveKind( String aKindId ) {
      TsNullArgumentRtException.checkNull( aKindId );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkAbilityManagerValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canRemoveKind( aKindId ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

  }

  /**
   * {@link ISkAbilityManager#eventer()} implementation.
   *
   * @author hazard157
   */
  class Eventer
      extends AbstractTsEventer<ISkAbilityManagerListener> {

    private final IStringListEdit changedRoleIds          = new StringLinkedBundleList();
    private boolean               wasAbilitiesListChanged = false;

    @Override
    protected boolean doIsPendingEvents() {
      return wasAbilitiesListChanged || !changedRoleIds.isEmpty();
    }

    @Override
    protected void doFirePendingEvents() {
      if( !changedRoleIds.isEmpty() ) {
        reallyFireRoleAbilitiesChanged( changedRoleIds );
      }
      if( wasAbilitiesListChanged ) {
        reallyFireAbilitiesListChanged();
      }
    }

    @Override
    protected void doClearPendingEvents() {
      changedRoleIds.clear();
      wasAbilitiesListChanged = false;
    }

    void reallyFireRoleAbilitiesChanged( IStringList aRoleIds ) {
      for( ISkAbilityManagerListener l : listeners() ) {
        l.onRoleAbilitiesChanged( userService, aRoleIds );
      }
    }

    void reallyFireAbilitiesListChanged() {
      for( ISkAbilityManagerListener l : listeners() ) {
        l.onAbilitiesListChanged( userService );
      }
    }

    void fireRoleAbilitiesChanged( IStringList aRoleIds ) {
      if( isFiringPaused() ) {
        for( String s : aRoleIds ) {
          if( !changedRoleIds.hasElem( s ) ) {
            changedRoleIds.add( s );
          }
        }
      }
      else {
        reallyFireRoleAbilitiesChanged( aRoleIds );
      }
    }

    void fireAbilitiesListChanged() {
      if( isFiringPaused() ) {
        wasAbilitiesListChanged = true;
      }
      else {
        reallyFireAbilitiesListChanged();
      }
    }

  }

  /**
   * Non-removable built-in validator.
   */
  private final ISkAbilityManagerValidator builtinValidator = new ISkAbilityManagerValidator() {

    @Override
    public ValidationResult canChangeRoleAbilities( String aRoleId, IStringList aAbilityIds, boolean aEnable ) {
      // error: role does not exists
      ISkRole role = userService.findRole( aRoleId );
      if( role == null ) {
        return ValidationResult.error( FMT_ERR_ROLE_NOT_EXISTS, aRoleId );
      }
      // error: role is immutable
      if( isImmutableRole( aRoleId ) ) {
        return ValidationResult.error( FMT_ERR_CANT_EDIT_BUILTIN_ROLE, aRoleId );
      }
      // warning: any ability does not exists
      IStridablesList<ISkAbility> absList = listAbilities();
      IStringListEdit absentAbilityIds = new StringArrayList();
      for( String abId : aAbilityIds ) {
        if( !absList.hasKey( abId ) && !absentAbilityIds.hasElem( abId ) ) {
          absentAbilityIds.add( abId );
        }
      }
      if( !absentAbilityIds.isEmpty() ) {
        return ValidationResult.error( FMT_WARN_ABSENT_ABILITIES, absentAbilityIds.toString() );
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canSetRoleAbilities( String aRoleId, IStringList aAbilityIds ) {
      // error: role does not exists
      ISkRole role = userService.findRole( aRoleId );
      if( role == null ) {
        return ValidationResult.error( FMT_ERR_ROLE_NOT_EXISTS, aRoleId );
      }
      // error: role is immutable
      if( isImmutableRole( aRoleId ) ) {
        return ValidationResult.error( FMT_ERR_CANT_EDIT_BUILTIN_ROLE, aRoleId );
      }
      // warning: any ability does not exists
      IStridablesList<ISkAbility> absList = listAbilities();
      IStringListEdit absentAbilityIds = new StringArrayList();
      for( String abId : aAbilityIds ) {
        if( !absList.hasKey( abId ) && !absentAbilityIds.hasElem( abId ) ) {
          absentAbilityIds.add( abId );
        }
      }
      if( !absentAbilityIds.isEmpty() ) {
        return ValidationResult.error( FMT_WARN_ABSENT_ABILITIES, absentAbilityIds.toString() );
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canDefineAbility( IDtoSkAbility aDto, ISkAbility aExistingAbility ) {
      // error: ability with ID exists
      if( aExistingAbility == null ) { // creating new ability
        if( findAbility( aDto.id() ) != null ) {
          return ValidationResult.error( FMT_ERR_ABILITY_EXISTS, aDto.id() );
        }
      }
      // warning: name is empty or has default value
      return NameStringValidator.VALIDATOR.validate( aDto.nmName() );
    }

    @Override
    public ValidationResult canRemoveAbility( String aAbilityId ) {
      // error: ability does not exists
      if( findAbility( aAbilityId ) == null ) {
        return ValidationResult.error( FMT_ERR_ABILITY_NOT_EXISTS, aAbilityId );
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canDefineKind( IDtoSkAbilityKind aDto, ISkAbilityKind aExistingKind ) {
      // error: kind with ID exists
      if( aExistingKind == null ) { // creating new kind
        if( findKind( aDto.id() ) != null ) {
          return ValidationResult.error( FMT_ERR_ABILITY_KIND_EXISTS, aDto.id() );
        }
      }
      // warning: name is empty or has default value
      return NameStringValidator.VALIDATOR.validate( aDto.nmName() );
    }

    @Override
    public ValidationResult canRemoveKind( String aKindId ) {
      // warning: kind does not exists
      if( findKind( aKindId ) == null ) {
        return ValidationResult.error( FMT_ERR_ABILITY_KIND_NOT_EXISTS, aKindId );
      }
      return ValidationResult.SUCCESS;
    }

  };

  private final Svs     svs     = new Svs();
  private final Eventer eventer = new Eventer();

  public SkAbilityManager( SkCoreServUsers aUserService ) {
    userService = aUserService;
    svs.addValidator( builtinValidator );
  }

  // ------------------------------------------------------------------------------------
  // package API
  //

  /**
   * Called from {@link SkCoreServUsers#doInit(ITsContextRo)}.
   *
   * @param aArgs {@link ITsContextRo} - connection opening arguments from {@link ISkConnection#open(ITsContextRo)}
   */
  void papiInit( ITsContextRo aArgs ) {
    // nop
  }

  boolean papiOnBackendMessage( GenericMessage aMessage ) {
    return switch( aMessage.messageId() ) {
      case BaMsgBuilderAbilitiesListChanged.MSG_ID -> {
        eventer.fireAbilitiesListChanged();
        yield true;
      }
      case BaMsgBuilderRoleAbilitiesChanged.MSG_ID -> {
        IStringList absList = BaMsgBuilderRoleAbilitiesChanged.INSTANCE.getAbilityIds( aMessage );
        eventer.fireRoleAbilitiesChanged( absList );
        yield true;
      }
      default -> false;
    };

  }

  // ------------------------------------------------------------------------------------
  // ISkAbilityManager
  //

  @Override
  public IStridablesList<ISkAbility> listRoleAbilities( String aRoleId ) {
    ISkRole role = userService.getRole( aRoleId );
    return role.listAllowedAbilities();
  }

  @Override
  public IStridablesList<ISkAbility> listCurrentRoleAbilities() {
    ISkRole role = userService.getRole( userService.getCurrentRoleSkid().strid() );
    return role.listAllowedAbilities();
  }

  @Override
  public boolean isAbilityAllowed( String aRoleId, String aAbilityId ) {
    TsNullArgumentRtException.checkNull( aAbilityId );
    IStridablesList<ISkAbility> absList = listRoleAbilities( aRoleId );
    return absList.hasKey( aAbilityId );
  }

  @Override
  public boolean isAbilityAllowed( String aAbilityId ) {
    IStridablesList<ISkAbility> absList = listCurrentRoleAbilities();
    return absList.hasKey( aAbilityId );
  }

  @Override
  public void changeRoleAbilities( String aRoleId, IStringList aAbilityIds, boolean aEnable ) {
    TsValidationFailedRtException.checkError( svs.canChangeRoleAbilities( aRoleId, aAbilityIds, aEnable ) );
    if( aAbilityIds.isEmpty() ) {
      return;
    }
    ISkRole role = userService.getRole( aRoleId );
    // create new list of ability IDs
    IStridablesList<ISkAbility> roleAbsList = listRoleAbilities( aRoleId );
    IStringListEdit newAbsIdsList;
    if( aEnable ) {
      newAbsIdsList = TsCollectionsUtils.union( roleAbsList.ids(), aAbilityIds );
    }
    else {
      newAbsIdsList = TsCollectionsUtils.subtract( new StringLinkedBundleList( roleAbsList.ids() ), aAbilityIds );
    }
    // create IDs and SKIDs list (by the way removing duplicates and non-existing abilities)
    IStringListEdit newAbIdsList = new StringLinkedBundleList();
    SkidList skl = new SkidList();
    for( String abId : newAbsIdsList ) {
      if( findAbility( abId ) != null ) {
        Skid skid = new Skid( CLSID_ABILITY, abId );
        if( !skl.hasElem( skid ) ) {
          skl.add( skid );
          newAbIdsList.add( abId );
        }
      }
    }
    // set role links of allowed abilities
    Gwid gwid = Gwid.createLink( CLSID_ROLE, LNKID_ROLE_ALLOWED_ABILITIES );
    DtoLinkFwd link = new DtoLinkFwd( gwid, role.skid(), skl );
    userService.papiPauseCoreValidation();
    try {
      userService.linkService().setLink( link );
    }
    finally {
      userService.papiResumeCoreValidation();
    }
    // inform siblings
    GtMessage msg = BaMsgBuilderRoleAbilitiesChanged.INSTANCE.makeMessage( newAbIdsList );
    userService.sendMessageToSiblings( msg );
  }

  @Override
  public void setRoleAbilities( String aRoleId, IStringList aEnabledAbilityIds ) {
    TsValidationFailedRtException.checkError( svs.canSetRoleAbilities( aRoleId, aEnabledAbilityIds ) );
    IStridablesList<ISkAbility> allAbsList = listAbilities();
    // create ability IDs list without non-existing and duplicate abilities
    IStringListEdit newAbIdsList = new StringArrayList();
    for( String abId : aEnabledAbilityIds ) {
      if( allAbsList.hasKey( aRoleId ) && !newAbIdsList.hasElem( abId ) ) {
        newAbIdsList.add( abId );
      }
    }
    // create SKIDs list from #newAbIds
    SkidList skl = new SkidList();
    for( String abId : newAbIdsList ) {
      Skid skid = new Skid( CLSID_ABILITY, abId );
      skl.add( skid );
    }
    // set role links of allowed abilities
    Gwid gwid = Gwid.createLink( CLSID_ROLE, LNKID_ROLE_ALLOWED_ABILITIES );
    ISkRole role = userService.getRole( aRoleId );
    DtoLinkFwd link = new DtoLinkFwd( gwid, role.skid(), skl );
    userService.papiPauseCoreValidation();
    try {
      userService.linkService().setLink( link );
    }
    finally {
      userService.papiResumeCoreValidation();
    }
    // inform siblings
    GtMessage msg = BaMsgBuilderRoleAbilitiesChanged.INSTANCE.makeMessage( newAbIdsList );
    userService.sendMessageToSiblings( msg );
  }

  @Override
  public ISkAbility findAbility( String aAbilityId ) {
    Skid skid = new Skid( CLSID_ABILITY, aAbilityId );
    return userService.objServ().find( skid );
  }

  @Override
  public ISkAbilityKind findKind( String aKindId ) {
    Skid skid = new Skid( CLSID_ABILITY_KIND, aKindId );
    return userService.objServ().find( skid );
  }

  @Override
  public IStridablesList<ISkAbility> listAbilities() {
    IList<ISkAbility> absList = userService.objServ().listObjs( CLSID_ABILITY, false );
    return new StridablesList<>( absList );
  }

  @Override
  public IStridablesList<ISkAbilityKind> listKinds() {
    IList<ISkAbilityKind> kindsList = userService.objServ().listObjs( CLSID_ABILITY_KIND, false );
    return new StridablesList<>( kindsList );
  }

  @Override
  public void defineAbility( IDtoSkAbility aDto ) {
    TsNullArgumentRtException.checkNull( aDto );
    ISkAbility existingAbility = findAbility( aDto.id() );
    TsValidationFailedRtException.checkError( svs.canDefineAbility( aDto, existingAbility ) );
    // prepare to create ability object
    DtoObject objDto;
    if( existingAbility != null ) {
      objDto = DtoObject.createFromSk( existingAbility, userService.coreApi() );
    }
    else {
      objDto = DtoObject.createDtoObject( new Skid( CLSID_ABILITY, aDto.id() ), userService.coreApi() );
    }
    objDto.attrs().setStr( AID_NAME, aDto.nmName() );
    objDto.attrs().setStr( AID_DESCRIPTION, aDto.description() );
    // prepare to add ability to the kind links
    ISkAbilityKind kind = findKind( aDto.kindId() );
    if( kind == null ) {
      kind = findKind( ABILITY_KIND_ID_UNDEFINED );
      TsInternalErrorRtException.checkNull( kind );
    }
    IDtoLinkFwd lfOld = userService.linkService().getLinkFwd( kind.skid(), LNKID_ABILITIES_OF_KIND );
    IDtoLinkFwd lfNew = DtoLinkFwd.createCopyPlusSkid( lfOld, objDto.skid() );
    // apply prepared
    userService.papiPauseCoreValidation();
    try {
      userService.objServ().defineObject( objDto );
      userService.linkService().setLink( lfNew );
    }
    finally {
      userService.papiResumeCoreValidation();
    }
    // generate message for siblings
    GtMessage msg = BaMsgBuilderAbilitiesListChanged.INSTANCE.makeMessage();
    userService.sendMessageToSiblings( msg );
  }

  @Override
  public void removeAbility( String aAbilityId ) {
    TsValidationFailedRtException.checkError( svs.canRemoveAbility( aAbilityId ) );
    Skid abilitySkid = new Skid( CLSID_ABILITY, aAbilityId );
    // remove ability from all the roles allowed abilities link
    for( ISkRole role : userService.listRoles() ) {
      if( isImmutableRole( role.id() ) ) {
        continue;
      }
      IStridablesListEdit<ISkAbility> absList = new StridablesList<>( listRoleAbilities( role.id() ) );
      if( absList.removeByKey( aAbilityId ) != null ) { // update links only if there was real change
        ISkidList skl = SkHelperUtils.objsToSkids( absList );
        userService.linkService().setLink( role.skid(), LNKID_ROLE_ALLOWED_ABILITIES, skl );
      }
    }
    // remove ability sk-object
    userService.papiPauseCoreValidation();
    try {
      userService.objServ().removeObject( abilitySkid );
    }
    finally {
      userService.papiResumeCoreValidation();
    }
    GtMessage msg = BaMsgBuilderAbilitiesListChanged.INSTANCE.makeMessage();
    userService.sendMessageToSiblings( msg );
  }

  @Override
  public void defineKind( IDtoSkAbilityKind aDto ) {
    TsNullArgumentRtException.checkNull( aDto );
    ISkAbilityKind existingKind = findKind( aDto.id() );
    TsValidationFailedRtException.checkError( svs.canDefineKind( aDto, existingKind ) );
    // create kind object
    DtoObject objDto;
    if( existingKind != null ) {
      objDto = DtoObject.createFromSk( existingKind, userService.coreApi() );
    }
    else {
      objDto = DtoObject.createDtoObject( new Skid( CLSID_ABILITY_KIND, aDto.id() ), userService.coreApi() );
    }
    objDto.attrs().setStr( AID_NAME, aDto.nmName() );
    objDto.attrs().setStr( AID_DESCRIPTION, aDto.description() );
    userService.papiPauseCoreValidation();
    try {
      userService.objServ().defineObject( objDto );
    }
    finally {
      userService.papiResumeCoreValidation();
    }
    // generate message for siblings
    GtMessage msg = BaMsgBuilderAbilitiesListChanged.INSTANCE.makeMessage();
    userService.sendMessageToSiblings( msg );
  }

  @Override
  public void removeKind( String aKindId ) {
    TsValidationFailedRtException.checkError( svs.canRemoveKind( aKindId ) );
    Skid skid = new Skid( CLSID_ABILITY_KIND, aKindId );
    userService.papiPauseCoreValidation();
    try {
      userService.objServ().removeObject( skid );
    }
    finally {
      userService.papiResumeCoreValidation();
    }
    GtMessage msg = BaMsgBuilderAbilitiesListChanged.INSTANCE.makeMessage();
    userService.sendMessageToSiblings( msg );
  }

  @Override
  public ITsValidationSupport<ISkAbilityManagerValidator> svs() {
    return svs;
  }

  @Override
  public ITsEventer<ISkAbilityManagerListener> eventer() {
    return eventer;
  }

}
