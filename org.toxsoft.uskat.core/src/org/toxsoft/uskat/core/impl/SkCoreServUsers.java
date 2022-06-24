package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.devapi.*;

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
    public ValidationResult canRemoveUser( String aLogin ) {
      // TODO Auto-generated method stub
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canRemoveRole( String aRoleId ) {
      // TODO Auto-generated method stub
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canEditUser( IDtoFullObject aUserDto, ISkUser aOldUser ) {
      // TODO Auto-generated method stub
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canEditRole( IDtoObject aRoleDto, ISkRole aOldRole ) {
      // TODO Auto-generated method stub
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canCreateUser( IDtoFullObject aUserDto ) {
      // TODO Auto-generated method stub
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canCreateRole( IDtoObject aRoleDto ) {
      // TODO Auto-generated method stub
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
    // nop
  }

  @Override
  protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // implementation
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
