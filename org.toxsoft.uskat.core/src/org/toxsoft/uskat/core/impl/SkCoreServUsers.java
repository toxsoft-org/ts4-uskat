package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
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
      // warn about attempt to remove unexisting user
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
      // TODO check attempt to remove current user
      // ???
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canRemoveRole( String aRoleId ) {
      // warn about attempt to remove unexisting role
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
      // TODO check attempt to remove current role
      // ???
      return ValidationResult.SUCCESS;
    }

  };

  private final ISkAbilityManager abilityManager;

  private final ValidationSupport            validationSupport = new ValidationSupport();
  private final ITsCompoundValidator<String> passwordValidator = TsCompoundValidator.create( true, true );
  private final Eventer                      eventer           = new Eventer();
  private final ClassClaimingCoreValidator   claimingValidator = new ClassClaimingCoreValidator();

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
    internalCreateClasses();
    internalCreateBuiltinObjects();
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

  private void pauseCoreValidation() {
    sysdescr().svs().pauseValidator( claimingValidator );
    objServ().svs().pauseValidator( claimingValidator );
    linkService().svs().pauseValidator( claimingValidator );
    clobService().svs().pauseValidator( claimingValidator );
  }

  private void resumeCoreValidation() {
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
    // DtoObject objRoleRoot = DtoObject.createDtoObject( SKID_ROLE_ROOT, coreApi() );
    // TODO SkCoreServUsers.internalCreateBuiltinObjects()
    // USkat core abilities kind
    // TODO SkCoreServUsers.internalCreateBuiltinObjects()
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

  /**
   * TODO listen to the server/backend messages to generate eventer messages
   */

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
    pauseCoreValidation();
    try {
      // forcefully set password hash attribute
      DtoFullObject dtoUser = new DtoFullObject( aDtoUser );
      dtoUser.attrs().setStr( ATRID_PASSWORD_HASH, SkHelperUtils.getPasswordHashCode( aPassword ) );
      return DtoFullObject.defineFullObject( coreApi(), aDtoUser );
    }
    finally {
      resumeCoreValidation();
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
    pauseCoreValidation();
    try {
      // edit object retaining previous password hash
      DtoFullObject dtoUser = new DtoFullObject( aDtoUser );
      dtoUser.attrs().setStr( ATRID_PASSWORD_HASH, oldUser.attrs().getStr( ATRID_PASSWORD_HASH ) );
      return DtoFullObject.defineFullObject( coreApi(), aDtoUser );
    }
    finally {
      resumeCoreValidation();
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
    pauseCoreValidation();
    try {
      return objServ().defineObject( aDtoRole );
    }
    finally {
      resumeCoreValidation();
    }
  }

  @Override
  public void removeUser( String aUserId ) {
    checkThread();
    TsValidationFailedRtException.checkError( svs().validator().canRemoveUser( aUserId ) );
    pauseCoreValidation();
    try {
      coreApi().objService().removeObject( new Skid( ISkUser.CLASS_ID, aUserId ) );
    }
    finally {
      resumeCoreValidation();
    }
  }

  @Override
  public void removeRole( String aRoleId ) {
    checkThread();
    TsValidationFailedRtException.checkError( svs().validator().canRemoveRole( aRoleId ) );
    pauseCoreValidation();
    try {
      coreApi().objService().removeObject( new Skid( ISkRole.CLASS_ID, aRoleId ) );
    }
    finally {
      resumeCoreValidation();
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
