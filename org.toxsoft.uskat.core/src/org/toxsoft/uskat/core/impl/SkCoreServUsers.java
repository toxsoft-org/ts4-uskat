package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;
import static org.toxsoft.uskat.core.l10n.ISkCoreSharedResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.api.users.ability.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * {@link ISkUserService} implementation.
 *
 * @author hazard157
 */
public class SkCoreServUsers
    extends AbstractSkCoreService
    implements ISkUserService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServUsers::new;

  /**
   * Builds message to the user service siblings to generate event
   * {@link ISkUserServiceListener#onUsersChanged(ISkCoreApi, ECrudOp, String)}
   *
   * @author hazard157
   */
  static class BaMsgBuilderUsersChanged
      extends AbstractBackendMessageBuilder {

    static final String MSG_ID     = "UsersChanged"; //$NON-NLS-1$
    static final String OPID_OP    = "op";           //$NON-NLS-1$
    static final String OPID_LOGIN = "login";        //$NON-NLS-1$

    static final BaMsgBuilderUsersChanged INSTANCE = new BaMsgBuilderUsersChanged();

    private BaMsgBuilderUsersChanged() {
      super( ISkUserService.SERVICE_ID, MSG_ID );
      defineArgValobj( OPID_OP, ECrudOp.KEEPER_ID, true );
      defineArgNonValobj( OPID_LOGIN, EAtomicType.STRING, false );
    }

    GtMessage makeMessage( ECrudOp aOp, String aLogin ) {
      if( aLogin == null ) {
        return makeMessageVarargs( OPID_OP, avValobj( aOp ) );
      }
      return makeMessageVarargs( OPID_OP, avValobj( aOp ), OPID_LOGIN, avStr( aLogin ) );
    }

    ECrudOp getCrudOp( GenericMessage aMsg ) {
      return getArg( aMsg, OPID_OP ).asValobj();
    }

    String getLogin( GenericMessage aMsg ) {
      if( !aMsg.args().hasKey( OPID_LOGIN ) ) {
        return null;
      }
      return getArg( aMsg, OPID_LOGIN ).asString();
    }

  }

  /**
   * Builds message to the role service siblings to generate event
   * {@link ISkUserServiceListener#onRolesChanged(ISkCoreApi, ECrudOp, String)}
   *
   * @author hazard157
   */
  static class BaMsgBuilderRolesChanged
      extends AbstractBackendMessageBuilder {

    static final String MSG_ID       = "RolesChanged"; //$NON-NLS-1$
    static final String OPID_OP      = "op";           //$NON-NLS-1$
    static final String OPID_ROLE_ID = "roleId";       //$NON-NLS-1$

    static final BaMsgBuilderRolesChanged INSTANCE = new BaMsgBuilderRolesChanged();

    private BaMsgBuilderRolesChanged() {
      super( ISkUserService.SERVICE_ID, MSG_ID );
      defineArgValobj( OPID_OP, ECrudOp.KEEPER_ID, true );
      defineArgNonValobj( OPID_ROLE_ID, EAtomicType.STRING, false );
    }

    GtMessage makeMessage( ECrudOp aOp, String aRoleId ) {
      if( aRoleId == null ) {
        return makeMessageVarargs( OPID_OP, avValobj( aOp ) );
      }
      return makeMessageVarargs( OPID_OP, avValobj( aOp ), OPID_ROLE_ID, avStr( aRoleId ) );
    }

    ECrudOp getCrudOp( GenericMessage aMsg ) {
      return getArg( aMsg, OPID_OP ).asValobj();
    }

    String getRoleId( GenericMessage aMsg ) {
      if( !aMsg.args().hasKey( OPID_ROLE_ID ) ) {
        return null;
      }
      return getArg( aMsg, OPID_ROLE_ID ).asString();
    }

  }

  /**
   * {@link ISkUserService#svs()} implementation.
   *
   * @author hazard157
   */
  static class ValidationSupport
      extends AbstractTsValidationSupport<ISkUserServiceValidator>
      implements ISkUserServiceValidator {

    @Override
    public ISkUserServiceValidator validator() {
      return this;
    }

    @Override
    public ValidationResult canCreateUser( IDtoFullObject aUserDto ) {
      TsNullArgumentRtException.checkNull( aUserDto );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkUserServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canCreateUser( aUserDto ) );
      }
      return vr;
    }

    @Override
    public ValidationResult canEditUser( IDtoFullObject aUserDto, ISkUser aOldUser ) {
      TsNullArgumentRtException.checkNulls( aUserDto, aOldUser );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkUserServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canEditUser( aUserDto, aOldUser ) );
      }
      return vr;
    }

    @Override
    public ValidationResult canRemoveUser( String aUserId ) {
      TsNullArgumentRtException.checkNull( aUserId );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkUserServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canRemoveUser( aUserId ) );
      }
      return vr;
    }

    @Override
    public ValidationResult canCreateRole( IDtoObject aRoleDto ) {
      TsNullArgumentRtException.checkNull( aRoleDto );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkUserServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canCreateRole( aRoleDto ) );
      }
      return vr;
    }

    @Override
    public ValidationResult canEditRole( IDtoObject aRoleDto, ISkRole aOldRole ) {
      TsNullArgumentRtException.checkNulls( aRoleDto, aOldRole );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkUserServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canEditRole( aRoleDto, aOldRole ) );
      }
      return vr;
    }

    @Override
    public ValidationResult canRemoveRole( String aRoleId ) {
      TsNullArgumentRtException.checkNull( aRoleId );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkUserServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canRemoveRole( aRoleId ) );
      }
      return vr;
    }

  }

  /**
   * {@link ISkUserService#eventer()} implementation.
   *
   * @author hazard157
   */
  class Eventer
      extends AbstractTsEventer<ISkUserServiceListener> {

    private boolean isPendingUsers = false;
    private boolean isPendingRoles = false;

    @Override
    protected void doClearPendingEvents() {
      isPendingUsers = false;
      isPendingRoles = false;
    }

    @Override
    protected void doFirePendingEvents() {
      if( isPendingUsers ) {
        reallyFireUser( ECrudOp.LIST, null );
      }
      if( isPendingRoles ) {
        reallyFireRole( ECrudOp.LIST, null );
      }
    }

    @Override
    protected boolean doIsPendingEvents() {
      return isPendingUsers || isPendingRoles;
    }

    private void reallyFireUser( ECrudOp aOp, String aUserId ) {
      for( ISkUserServiceListener l : listeners() ) {
        try {
          l.onRolesChanged( coreApi(), aOp, aUserId );
        }
        catch( Exception ex ) {
          LoggerUtils.errorLogger().error( ex );
        }
      }
    }

    private void reallyFireRole( ECrudOp aOp, String aRoleId ) {
      for( ISkUserServiceListener l : listeners() ) {
        try {
          l.onRolesChanged( coreApi(), aOp, aRoleId );
        }
        catch( Exception ex ) {
          LoggerUtils.errorLogger().error( ex );
        }
      }
    }

    void fireUserChanged( ECrudOp aOp, String aUserId ) {
      if( isFiringPaused() ) {
        isPendingUsers = true;
        return;
      }
      reallyFireUser( aOp, aUserId );
    }

    void fireRoleChanged( ECrudOp aOp, String aRoleId ) {
      if( isFiringPaused() ) {
        isPendingRoles = true;
        return;
      }
      reallyFireRole( aOp, aRoleId );
    }

  }

  /**
   * Builtin service validator.
   */
  private final ISkUserServiceValidator builtinValidator = new ISkUserServiceValidator() {

    @Override
    public ValidationResult canCreateUser( IDtoFullObject aUserDto ) {
      // check precondition
      if( !aUserDto.skid().classId().equals( ISkUser.CLASS_ID ) ) {
        return ValidationResult.error( FMT_ERR_NOT_USER_DPU, aUserDto.classId(), ISkUser.CLASS_ID );
      }
      // user exists?
      if( listUsers().hasKey( aUserDto.strid() ) ) {
        return ValidationResult.error( FMT_ERR_DUP_USER, aUserDto.strid() );
      }
      // check if any role is assigned
      ISkidList rolesSkids = aUserDto.links().map().findByKey( LNKID_USER_ROLES );
      if( rolesSkids == null || rolesSkids.isEmpty() ) {
        return ValidationResult.error( MSG_ERR_NO_ROLES );
      }
      // check roles exists
      IStridablesList<ISkRole> existingRoles = listRoles();
      for( Skid s : rolesSkids ) {
        if( !existingRoles.hasKey( s.strid() ) ) {
          return ValidationResult.error( FMT_ERR_INV_ROLES, s.strid() );
        }
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canCreateRole( IDtoObject aRoleDto ) {
      // check precondition
      if( !aRoleDto.skid().classId().equals( ISkRole.CLASS_ID ) ) {
        return ValidationResult.error( FMT_ERR_NOT_ROLE_DPU, aRoleDto.classId(), ISkRole.CLASS_ID );
      }
      // role exists?
      if( listRoles().hasKey( aRoleDto.strid() ) ) {
        return ValidationResult.error( FMT_ERR_DUP_ROLE, aRoleDto.strid() );
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canEditUser( IDtoFullObject aUserDto, ISkUser aOldUser ) {
      ValidationResult vr = ValidationResult.SUCCESS;
      // check precondition
      if( !aUserDto.skid().classId().equals( ISkUser.CLASS_ID ) ) {
        return ValidationResult.error( FMT_ERR_NOT_USER_DPU, aUserDto.classId(), ISkUser.CLASS_ID );
      }
      // error: can't disable root user
      if( USER_ID_ROOT.equals( aUserDto.strid() ) ) {
        boolean enable = aUserDto.attrs().getBool( ATRID_USER_IS_ENABLED, true );
        if( !enable ) {
          return ValidationResult.error( MSG_ERR_CANT_DISABLE_ROOT_USER );
        }
      }
      // warning: disabling enabled guest user
      if( USER_ID_GUEST.equals( aUserDto.strid() ) ) {
        boolean enable = aUserDto.attrs().getBool( ATRID_USER_IS_ENABLED, true );
        boolean enabledNow = aOldUser.isEnabled();
        if( !enable && enabledNow ) {
          return ValidationResult.warn( MSG_WARN_DISABLING_GUEST_USER );
        }
      }
      // check if any role is assigned
      ISkidList rolesSkids = aUserDto.links().map().findByKey( LNKID_USER_ROLES );
      if( rolesSkids == null || rolesSkids.isEmpty() ) {
        return ValidationResult.error( MSG_ERR_NO_ROLES );
      }
      // check roles exists
      IStridablesList<ISkRole> existingRoles = listRoles();
      for( Skid s : rolesSkids ) {
        if( !existingRoles.hasKey( s.strid() ) ) {
          return ValidationResult.error( FMT_ERR_INV_ROLES, s.strid() );
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canEditRole( IDtoObject aRoleDto, ISkRole aOldRole ) {
      // check precondition
      if( !aRoleDto.skid().classId().equals( ISkRole.CLASS_ID ) ) {
        return ValidationResult.error( FMT_ERR_NOT_ROLE_DPU, aRoleDto.classId(), ISkRole.CLASS_ID );
      }
      // error: can't disable root role
      if( ROLE_ID_ROOT.equals( aRoleDto.strid() ) ) {
        boolean enable = aRoleDto.attrs().getBool( ATRID_ROLE_IS_ENABLED, true );
        if( !enable ) {
          return ValidationResult.error( MSG_ERR_CANT_DISABLE_ROOT_ROLE );
        }
      }
      // warning: disabling enabled guest role
      if( ROLE_ID_GUEST.equals( aRoleDto.strid() ) ) {
        boolean enable = aRoleDto.attrs().getBool( ATRID_ROLE_IS_ENABLED, true );
        boolean enabledNow = aOldRole.isEnabled();
        if( !enable && enabledNow ) {
          return ValidationResult.warn( MSG_WARN_DISABLING_GUEST_ROLE );
        }
      }
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canRemoveUser( String aUserId ) {
      // warn about attempt to remove non-existing user
      if( !listUsers().hasKey( aUserId ) ) {
        return ValidationResult.warn( FMT_WARN_CANT_DEL_NO_USER, aUserId );
      }
      // can't remove root user
      if( USER_ID_ROOT.equals( aUserId ) ) {
        return ValidationResult.error( MSG_ERR_CANT_DEL_ROOT_USER );
      }
      // can't remove guest user
      if( USER_ID_GUEST.equals( aUserId ) ) {
        return ValidationResult.error( MSG_ERR_CANT_DEL_GUEST_USER );
      }
      if( aUserId.equals( currUserSkid.strid() ) ) {
        return ValidationResult.error( MSG_ERR_CANT_DEL_CURRENT_USER );
      }
      // ???
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canRemoveRole( String aRoleId ) {
      // warn about attempt to remove non-existing role
      if( !listRoles().hasKey( aRoleId ) ) {
        return ValidationResult.warn( FMT_WARN_CANT_DEL_NO_ROLE, aRoleId );
      }
      // can't remove root role
      if( ROLE_ID_ROOT.equals( aRoleId ) ) {
        return ValidationResult.error( MSG_ERR_CANT_DEL_ROOT_ROLE );
      }
      // can't remove guest role
      if( ROLE_ID_GUEST.equals( aRoleId ) ) {
        return ValidationResult.error( MSG_ERR_CANT_DEL_GUEST_ROLE );
      }
      if( aRoleId.equals( currRoleSkid.strid() ) ) {
        return ValidationResult.error( MSG_ERR_CANT_DEL_CURRENT_ROLE );
      }
      // ???
      return ValidationResult.SUCCESS;
    }

  };

  private final SkAbilityManager abilityManager;

  private final ValidationSupport            validationSupport = new ValidationSupport();
  private final ITsCompoundValidator<String> passwordValidator = TsCompoundValidator.create( true, true );
  private final Eventer                      eventer           = new Eventer();
  private final ClassClaimingCoreValidator   claimingValidator = new ClassClaimingCoreValidator();

  private Skid currUserSkid = null; // currently logged user
  private Skid currRoleSkid = null; // role of the currently logged user

  private final ITsValidator<String> builtinPasswordValidator = aValue -> {
    if( aValue.isBlank() ) {
      return ValidationResult.error( MSG_ERR_PSWD_IS_BLANK );
    }
    return ValidationResult.SUCCESS;
  };

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkCoreServUsers( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
    abilityManager = new SkAbilityManager( this );
    passwordValidator.addValidator( builtinPasswordValidator );
    validationSupport.addValidator( builtinValidator );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkCoreService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    ISkLoggedUserInfo userInfo = coreApi().getCurrentUserInfo();
    currUserSkid = userInfo.userSkid();
    currRoleSkid = userInfo.roleSkid();
    // TODO setup L10n for built-in entities
    internalCreateClasses();
    internalCreateBuiltinObjects();
    sysdescr().svs().addValidator( claimingValidator );
    objServ().svs().addValidator( claimingValidator );
    linkService().svs().addValidator( claimingValidator );
    clobService().svs().addValidator( claimingValidator );
    abilityManager.papiInit( aArgs );
  }

  @Override
  protected void doClose() {
    // nop
  }

  @Override
  protected boolean onBackendMessage( GenericMessage aMessage ) {
    // process ability manager messages
    if( abilityManager.papiOnBackendMessage( aMessage ) ) {
      return true;
    }
    // process user service own messages
    return switch( aMessage.messageId() ) {
      case BaMsgBuilderUsersChanged.MSG_ID -> {
        ECrudOp op = BaMsgBuilderUsersChanged.INSTANCE.getCrudOp( aMessage );
        String login = BaMsgBuilderUsersChanged.INSTANCE.getLogin( aMessage );
        eventer.fireUserChanged( op, login );
        yield true;
      }
      case BaMsgBuilderRolesChanged.MSG_ID -> {
        ECrudOp op = BaMsgBuilderRolesChanged.INSTANCE.getCrudOp( aMessage );
        String roleId = BaMsgBuilderRolesChanged.INSTANCE.getRoleId( aMessage );
        eventer.fireRoleChanged( op, roleId );
        yield true;
      }
      default -> false;
    };
  }

  @Override
  protected boolean doIsClassClaimedByService( String aClassId ) {
    return switch( aClassId ) {
      case ISkUser.CLASS_ID -> true;
      case ISkRole.CLASS_ID -> true;
      case ISkAbility.CLASS_ID -> true;
      case ISkAbilityKind.CLASS_ID -> true;
      default -> false;
    };
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  IDataType DT_PASSWORD = DataType.create( EAtomicType.STRING, //
      TSID_DEFAULT_VALUE, AV_STR_EMPTY //
  );

  void papiPauseCoreValidation() {
    sysdescr().svs().pauseValidator( claimingValidator );
    objServ().svs().pauseValidator( claimingValidator );
    linkService().svs().pauseValidator( claimingValidator );
    clobService().svs().pauseValidator( claimingValidator );
  }

  void papiResumeCoreValidation() {
    sysdescr().svs().resumeValidator( claimingValidator );
    objServ().svs().resumeValidator( claimingValidator );
    linkService().svs().resumeValidator( claimingValidator );
    clobService().svs().resumeValidator( claimingValidator );
  }

  /**
   * Creates classes: {@link ISkRole}, {@link ISkUser}, {@link ISkAbility}, {@link ISkAbilityKind}.
   */
  private void internalCreateClasses() {
    // ISkAbilityKind
    IDtoClassInfo abKindCinf = internalCreateAbilityKindClassDto();
    sysdescr().defineClass( abKindCinf );
    objServ().registerObjectCreator( ISkAbilityKind.CLASS_ID, SkAbilityKind.CREATOR );
    // ISkAbility
    IDtoClassInfo abilityCinf = internalCreateAbilityClassDto();
    sysdescr().defineClass( abilityCinf );
    objServ().registerObjectCreator( ISkAbility.CLASS_ID, SkAbility.CREATOR );
    // ISkRole
    IDtoClassInfo roleCinf = internalCreateRoleClassDto();
    sysdescr().defineClass( roleCinf );
    objServ().registerObjectCreator( ISkRole.CLASS_ID, SkRole.CREATOR );
    // ISkUser
    IDtoClassInfo userCinf = internalCreateUserClassDto();
    sysdescr().defineClass( userCinf );
    objServ().registerObjectCreator( ISkUser.CLASS_ID, SkUser.CREATOR );
  }

  private void internalCreateBuiltinObjects() {
    // undefined abilities kind
    DtoObject objKindUndefined = DtoObject.createDtoObject( SKID_ABILITY_KIND_UNDEFINED, coreApi() );
    objKindUndefined.attrs().setStr( AID_NAME, ENG_ABILITY_KIND_UNDEFINED );
    objKindUndefined.attrs().setStr( AID_DESCRIPTION, ENG_ABILITY_KIND_UNDEFINED_D );
    objServ().defineObject( objKindUndefined );
    // root role
    DtoObject objRoleRoot = DtoObject.createDtoObject( SKID_ROLE_ROOT, coreApi() );
    objRoleRoot.attrs().setStr( AID_NAME, ENG_ROOT_ROLE );
    objRoleRoot.attrs().setStr( AID_DESCRIPTION, ENG_ROOT_ROLE_D );
    objServ().defineObject( objRoleRoot );
    // guest role
    DtoObject objRoleGuest = DtoObject.createDtoObject( SKID_ROLE_GUEST, coreApi() );
    objRoleGuest.attrs().setStr( AID_NAME, ENG_GUEST_ROLE );
    objRoleGuest.attrs().setStr( AID_DESCRIPTION, ENG_GUEST_ROLE_D );
    objServ().defineObject( objRoleGuest );
    // root user
    DtoObject objUserRoot = DtoObject.createDtoObject( SKID_USER_ROOT, coreApi() );
    objUserRoot.attrs().setStr( AID_NAME, ENG_ROOT_USER );
    objUserRoot.attrs().setStr( AID_DESCRIPTION, ENG_ROOT_USER_D );
    // if password is not set (or was reset) specify the builtin default password
    if( objUserRoot.attrs().getStr( ATRID_PASSWORD_HASH ).isEmpty() ) {
      objUserRoot.attrs().setStr( ATRID_PASSWORD_HASH, SkHelperUtils.getPasswordHashCode( INITIAL_ROOT_PASSWORD ) );
    }
    ISkObject skoRootUser = objServ().defineObject( objUserRoot );
    linkService().setLink( skoRootUser.skid(), LNKID_USER_ROLES, new SkidList( objRoleRoot.skid() ) );
    // guest user
    DtoObject objUserGuest = DtoObject.createDtoObject( SKID_USER_GUEST, coreApi() );
    objUserGuest.attrs().setStr( AID_NAME, ENG_GUEST_USER );
    objUserGuest.attrs().setStr( AID_DESCRIPTION, ENG_GUEST_USER_D );
    ISkObject skoGuestUser = objServ().defineObject( objUserGuest );
    linkService().setLink( skoGuestUser.skid(), LNKID_USER_ROLES, new SkidList( objRoleGuest.skid() ) );
  }

  // ------------------------------------------------------------------------------------
  // package API
  //

  Skid getCurrentUserSkid() {
    return currUserSkid;
  }

  Skid getCurrentRoleSkid() {
    return currRoleSkid;
  }

  // ------------------------------------------------------------------------------------
  // ISkUserService
  //

  @Override
  public ISkUser findUser( String aUserId ) {
    TsNullArgumentRtException.checkNull( aUserId );
    return coreApi().objService().find( new Skid( ISkUser.CLASS_ID, aUserId ) );
  }

  @Override
  public ISkRole findRole( String aRoleId ) {
    TsNullArgumentRtException.checkNull( aRoleId );
    return coreApi().objService().find( new Skid( ISkRole.CLASS_ID, aRoleId ) );
  }

  @Override
  public IStridablesList<ISkUser> listUsers() {
    IList<ISkUser> ll = objServ().listObjs( ISkUser.CLASS_ID, false );
    return new StridablesList<>( ll );
  }

  @Override
  public IStridablesList<ISkRole> listRoles() {
    IList<ISkRole> ll = objServ().listObjs( ISkRole.CLASS_ID, false );
    return new StridablesList<>( ll );
  }

  @Override
  public ISkUser createUser( IDtoFullObject aDtoUser, String aPassword ) {
    checkThread();
    TsValidationFailedRtException.checkError( passwordValidator.validate( aPassword ) );
    TsValidationFailedRtException.checkError( validationSupport.canCreateUser( aDtoUser ) );
    papiPauseCoreValidation();
    try {
      // forcefully set password hash attribute
      DtoFullObject dtoUser =
          new DtoFullObject( aDtoUser, aDtoUser.clobs(), aDtoUser.links().map(), aDtoUser.rivetRevs() );
      dtoUser.attrs().setStr( ATRID_PASSWORD_HASH, SkHelperUtils.getPasswordHashCode( aPassword ) );
      ISkUser user = DtoFullObject.defineFullObject( coreApi(), dtoUser );
      // inform siblings
      GtMessage msg = BaMsgBuilderUsersChanged.INSTANCE.makeMessage( ECrudOp.CREATE, dtoUser.id() );
      sendMessageToSiblings( msg );
      return user;
    }
    finally {
      papiResumeCoreValidation();
    }
  }

  @Override
  public ISkUser editUser( IDtoFullObject aDtoUser ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aDtoUser );
    TsIllegalArgumentRtException.checkFalse( aDtoUser.classId().equals( ISkUser.CLASS_ID ) );
    ISkUser oldUser = objServ().find( aDtoUser.skid() );
    TsItemNotFoundRtException.checkNull( oldUser );
    TsValidationFailedRtException.checkError( validationSupport.canEditUser( aDtoUser, oldUser ) );
    papiPauseCoreValidation();
    try {
      ISkUser user = DtoFullObject.defineFullObject( coreApi(), aDtoUser );
      // inform siblings
      GtMessage msg = BaMsgBuilderUsersChanged.INSTANCE.makeMessage( ECrudOp.EDIT, aDtoUser.id() );
      sendMessageToSiblings( msg );
      return user;
    }
    finally {
      papiResumeCoreValidation();
    }
  }

  @Override
  public ISkRole defineRole( IDtoObject aDtoRole ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aDtoRole );
    TsIllegalArgumentRtException.checkFalse( aDtoRole.classId().equals( ISkRole.CLASS_ID ) );
    ISkRole oldRole = objServ().find( aDtoRole.skid() );
    if( oldRole != null ) {
      TsValidationFailedRtException.checkError( validationSupport.canEditRole( aDtoRole, oldRole ) );
    }
    else {
      TsValidationFailedRtException.checkError( validationSupport.canCreateRole( aDtoRole ) );
    }
    papiPauseCoreValidation();
    try {
      ISkRole role = objServ().defineObject( aDtoRole );
      // inform siblings
      ECrudOp op = oldRole != null ? ECrudOp.EDIT : ECrudOp.CREATE;
      GtMessage msg = BaMsgBuilderRolesChanged.INSTANCE.makeMessage( op, aDtoRole.id() );
      sendMessageToSiblings( msg );
      return role;
    }
    finally {
      papiResumeCoreValidation();
    }
  }

  @Override
  public void removeUser( String aUserId ) {
    checkThread();
    TsValidationFailedRtException.checkError( svs().validator().canRemoveUser( aUserId ) );
    papiPauseCoreValidation();
    try {
      coreApi().objService().removeObject( new Skid( ISkUser.CLASS_ID, aUserId ) );
      // inform siblings
      GtMessage msg = BaMsgBuilderUsersChanged.INSTANCE.makeMessage( ECrudOp.REMOVE, aUserId );
      sendMessageToSiblings( msg );
    }
    finally {
      papiResumeCoreValidation();
    }
  }

  @Override
  public void removeRole( String aRoleId ) {
    checkThread();
    TsValidationFailedRtException.checkError( svs().validator().canRemoveRole( aRoleId ) );
    papiPauseCoreValidation();
    try {
      coreApi().objService().removeObject( new Skid( ISkRole.CLASS_ID, aRoleId ) );
      // inform siblings
      GtMessage msg = BaMsgBuilderRolesChanged.INSTANCE.makeMessage( ECrudOp.REMOVE, aRoleId );
      sendMessageToSiblings( msg );
    }
    finally {
      papiResumeCoreValidation();
    }
  }

  @Override
  public ISkUser setUserEnabled( String aUserId, boolean aEnabled ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aUserId );
    TsItemNotFoundRtException.checkNull( findUser( aUserId ) );
    Skid skid = new Skid( ISkUser.CLASS_ID, aUserId );
    DtoFullObject dto = DtoFullObject.createDtoFullObject( skid, coreApi() );
    dto.attrs().setBool( ATRID_USER_IS_ENABLED, aEnabled );
    return editUser( dto );
  }

  @Override
  public ISkUser setUserHidden( String aUserId, boolean aHidden ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aUserId );
    TsItemNotFoundRtException.checkNull( findUser( aUserId ) );
    Skid skid = new Skid( ISkUser.CLASS_ID, aUserId );
    DtoFullObject dto = DtoFullObject.createDtoFullObject( skid, coreApi() );
    dto.attrs().setBool( ATRID_USER_IS_HIDDEN, aHidden );
    return editUser( dto );
  }

  @Override
  public ISkUser setUserPassword( String aUserId, String aPassword ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aUserId );
    TsItemNotFoundRtException.checkNull( findUser( aUserId ) );
    TsValidationFailedRtException.checkError( passwordValidator.validate( aPassword ) );
    Skid skid = new Skid( ISkUser.CLASS_ID, aUserId );
    DtoFullObject dto = DtoFullObject.createDtoFullObject( skid, coreApi() );
    dto.attrs().setStr( ATRID_PASSWORD_HASH, SkHelperUtils.getPasswordHashCode( aPassword ) );
    return editUser( dto );
  }

  @Override
  public ISkUser setUserRoles( String aUserId, IStridablesList<ISkRole> aRoles ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aUserId );
    TsItemNotFoundRtException.checkNull( findUser( aUserId ) );
    Skid skid = new Skid( ISkUser.CLASS_ID, aUserId );
    DtoFullObject dto = DtoFullObject.createDtoFullObject( skid, coreApi() );
    SkidList rolesList = new SkidList();
    for( ISkRole r : aRoles ) {
      rolesList.add( r.skid() );
    }
    dto.links().map().put( LNKID_USER_ROLES, rolesList );
    return editUser( dto );
  }

  @Override
  public ISkAbilityManager abilityManager() {
    return abilityManager;
  }

  @Override
  public ITsValidator<String> passwordValidator() {
    return passwordValidator;
  }

  @Override
  public void addPasswordValidator( ITsValidator<String> aPasswordValidator ) {
    passwordValidator.addValidator( aPasswordValidator );
  }

  @Override
  public ITsValidationSupport<ISkUserServiceValidator> svs() {
    return validationSupport;
  }

  @Override
  public ITsEventer<ISkUserServiceListener> eventer() {
    return eventer;
  }

}
