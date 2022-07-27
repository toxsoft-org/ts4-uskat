package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.gw.IGwHardConstants.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
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
   * Builtin objects name/description is always in english, no need to localize in Java code.
   * <p>
   * As usual, localization may be done by means of {@link CoreL10n}.
   */
  private static final String STR_N_ROOT_ROLE  = "Superuser";        //$NON-NLS-1$
  private static final String STR_D_ROOT_ROLE  = "Superuser role";   //$NON-NLS-1$
  private static final String STR_N_GUEST_ROLE = "Guest";            //$NON-NLS-1$
  private static final String STR_D_GUEST_ROLE = "Guest role";       //$NON-NLS-1$
  private static final String STR_N_ROOT_USER  = "Root";             //$NON-NLS-1$
  private static final String STR_D_ROOT_USER  = "Root - superuser"; //$NON-NLS-1$
  private static final String STR_N_GUEST_USER = "Guest";            //$NON-NLS-1$
  private static final String STR_D_GUEST_USER = "Guest user";       //$NON-NLS-1$

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
    public ValidationResult canRemoveUser( String aLogin ) {
      TsNullArgumentRtException.checkNull( aLogin );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkUserServiceValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canRemoveUser( aLogin ) );
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

    private void reallyFireUser( ECrudOp aOp, String aLogin ) {
      for( ISkUserServiceListener l : listeners() ) {
        try {
          l.onRolesChanged( coreApi(), aOp, aLogin );
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

    void fireUserChanged( ECrudOp aOp, String aLogin ) {
      if( isFiringPaused() ) {
        isPendingUsers = true;
        return;
      }
      reallyFireUser( aOp, aLogin );
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
      // check password validity
      String passowrd = aUserDto.attrs().getStr( ATRID_PASSWORD, EMPTY_STRING );
      ValidationResult vr = passwordValidator().validate( passowrd );
      if( vr.isError() ) {
        return vr;
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
      // check password validity
      String passowrd = aUserDto.attrs().getStr( ATRID_PASSWORD, EMPTY_STRING );
      vr = ValidationResult.firstNonOk( vr, passwordValidator().validate( passowrd ) );
      if( vr.isError() ) {
        return vr;
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
    public ValidationResult canRemoveUser( String aLogin ) {
      // warn about attempt to remove unexisting user
      if( !listUsers().hasKey( aLogin ) ) {
        return ValidationResult.warn( FMT_WARN_CANT_DEL_NO_USER, aLogin );
      }
      // can't remove root user
      if( USER_ID_ROOT.equals( aLogin ) ) {
        return ValidationResult.error( MSG_ERR_CANT_DEL_ROOT_USER );
      }
      // can't remove guest user
      if( USER_ID_GUEST.equals( aLogin ) ) {
        return ValidationResult.error( MSG_ERR_CANT_DEL_GUEST_USER );
      }
      // TODO onlone user can not be removed
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
      // TODO if any user is online then role can not be removed
      return ValidationResult.SUCCESS;
    }

  };

  private final ValidationSupport validationSupport = new ValidationSupport();

  private final Eventer eventer = new Eventer();

  private final ITsCompoundValidator<String> passwordValidator = TsCompoundValidator.create( true, true );

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
    passwordValidator.addValidator( builtinPasswordValidator );
    validationSupport.addValidator( builtinValidator );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkCoreService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // create class for ISkRole
    IDtoClassInfo roleCinf = internalCreateRoleClassDto();
    sysdescr().defineClass( roleCinf );
    // create class for ISkUser
    IDtoClassInfo userCinf = internalCreateUserClassDto();
    sysdescr().defineClass( userCinf );
    // create role rootRole
    DtoObject objRoleRoot = SkHelperUtils.createDtoObject( SKID_ROLE_ROOT, coreApi() );
    objRoleRoot.attrs().setStr( AID_NAME, STR_N_ROOT_ROLE );
    objRoleRoot.attrs().setStr( AID_DESCRIPTION, STR_D_ROOT_ROLE );
    objServ().defineObject( objRoleRoot );
    // create role guestRole
    DtoObject objRoleGuest = SkHelperUtils.createDtoObject( SKID_ROLE_GUEST, coreApi() );
    objRoleGuest.attrs().setStr( AID_NAME, STR_N_GUEST_ROLE );
    objRoleGuest.attrs().setStr( AID_DESCRIPTION, STR_D_GUEST_ROLE );
    objServ().defineObject( objRoleGuest );
    // create user root
    DtoObject objUserRoot = SkHelperUtils.createDtoObject( SKID_USER_ROOT, coreApi() );
    objRoleRoot.attrs().setStr( AID_NAME, STR_N_ROOT_USER );
    objRoleRoot.attrs().setStr( AID_DESCRIPTION, STR_D_ROOT_USER );
    objServ().defineObject( objUserRoot );
    // create user guest
    DtoObject objUserGuest = SkHelperUtils.createDtoObject( SKID_USER_GUEST, coreApi() );
    objRoleGuest.attrs().setStr( AID_NAME, STR_N_GUEST_USER );
    objRoleGuest.attrs().setStr( AID_DESCRIPTION, STR_D_GUEST_USER );
    objServ().defineObject( objUserGuest );
    // FIXME sysdescr().svs().addValidator();
    // FIXME objServ().svs().addValidator();
    // FIXME linkService().svs().addValidator();
  }

  @Override
  protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  IDataType DT_PASSWORD = DataType.create( EAtomicType.STRING, //
      TSID_DEFAULT_VALUE, AV_STR_EMPTY //
  );

  /**
   * Creates DTO of {@link ISkRole#CLASS_ID} class.
   *
   * @return {@link IDtoClassInfo} - {@link ISkRole#CLASS_ID} class info
   */
  private static IDtoClassInfo internalCreateRoleClassDto() {
    DtoClassInfo cinf = new DtoClassInfo( CLSID_ROLE, GW_ROOT_CLASS_ID, IOptionSet.NULL );
    OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS.setValue( cinf.params(), AV_TRUE );
    cinf.attrInfos().add( ATRINF_ROLE_IS_ENABLED );
    cinf.attrInfos().add( ATRINF_ROLE_IS_HIDDEN );
    return cinf;
  }

  /**
   * Creates DTO of {@link ISkUser#CLASS_ID} class.
   *
   * @return {@link IDtoClassInfo} - {@link ISkUser#CLASS_ID} class info
   */
  private static IDtoClassInfo internalCreateUserClassDto() {
    DtoClassInfo cinf = new DtoClassInfo( CLSID_USER, GW_ROOT_CLASS_ID, IOptionSet.NULL );
    OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS.setValue( cinf.params(), AV_TRUE );
    cinf.attrInfos().add( ATRINF_PASSWORD );
    cinf.attrInfos().add( ATRINF_USER_IS_ENABLED );
    cinf.attrInfos().add( ATRINF_USER_IS_HIDDEN );
    cinf.linkInfos().add( LNKINF_USER_ROLES );
    return cinf;
  }

  // ------------------------------------------------------------------------------------
  // ISkUserService
  //

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
  public ISkUser defineUser( IDtoFullObject aDtoUser ) {
    TsNullArgumentRtException.checkNull( aDtoUser );
    TsIllegalArgumentRtException.checkFalse( aDtoUser.classId().equals( ISkUser.CLASS_ID ) );
    ISkUser oldUser = objServ().find( aDtoUser.skid() );
    if( oldUser != null ) {
      TsValidationFailedRtException.checkError( validationSupport.canEditUser( aDtoUser, oldUser ) );
    }
    else {
      TsValidationFailedRtException.checkError( validationSupport.canCreateRole( aDtoUser ) );
    }

    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ISkRole defineRole( IDtoObject aDtoRole ) {
    TsNullArgumentRtException.checkNull( aDtoRole );
    TsIllegalArgumentRtException.checkFalse( aDtoRole.classId().equals( ISkRole.CLASS_ID ) );
    ISkRole oldRole = objServ().find( aDtoRole.skid() );
    if( oldRole != null ) {
      TsValidationFailedRtException.checkError( validationSupport.canEditRole( aDtoRole, oldRole ) );
    }
    else {
      TsValidationFailedRtException.checkError( validationSupport.canCreateRole( aDtoRole ) );
    }

    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void removeUser( String aUserId ) {
    TsValidationFailedRtException.checkError( svs().validator().canRemoveUser( aUserId ) );

    // TODO remove user object
  }

  @Override
  public void removeRole( String aRoleId ) {
    TsValidationFailedRtException.checkError( svs().validator().canRemoveRole( aRoleId ) );

    // TODO remove role object
  }

  @Override
  public ISkUser setUserEnabled( String aLogin, boolean aEnabled ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ISkUser setUserHidden( String aLogin, boolean aHidden ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ISkUser setUserPassword( String aLogin, String aPassword ) {
    TsNullArgumentRtException.checkNulls( aLogin, aPassword );
    TsValidationFailedRtException.checkWarn( passwordValidator.validate( aPassword ) );

    // TODO Auto-generated method stub

    return null;
  }

  @Override
  public ISkUser setUserRoles( IStridablesList<ISkRole> aRoles ) {
    // TODO Auto-generated method stub
    return null;
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
