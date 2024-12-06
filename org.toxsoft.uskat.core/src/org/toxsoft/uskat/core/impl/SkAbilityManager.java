package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.users.ability.*;

/**
 * {@link ISkAbilityManager} implementation.
 * <p>
 * Note: USkat entities used by ability manager are initialized in {@link SkCoreServUsers#doInit(ITsContextRo)}
 *
 * @author hazard157
 */
class SkAbilityManager
    implements ISkAbilityManager {

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
    public ValidationResult canResetRoleAbilities( String aRoleId, boolean aEnableAll ) {
      TsNullArgumentRtException.checkNull( aRoleId );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkAbilityManagerValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canResetRoleAbilities( aRoleId, aEnableAll ) );
        if( vr.isError() ) {
          break;
        }
      }
      return vr;
    }

    @Override
    public ValidationResult canSetRoleAbility( String aRoleId, String aAbilityId, boolean aEnable ) {
      TsNullArgumentRtException.checkNulls( aAbilityId, aRoleId );
      ValidationResult vr = ValidationResult.SUCCESS;
      for( ISkAbilityManagerValidator v : validatorsList() ) {
        vr = ValidationResult.firstNonOk( vr, v.canSetRoleAbility( aRoleId, aAbilityId, aEnable ) );
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

    @Override
    protected boolean doIsPendingEvents() {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    protected void doFirePendingEvents() {
      // TODO Auto-generated method stub

    }

    @Override
    protected void doClearPendingEvents() {
      // TODO Auto-generated method stub

    }

  }

  /**
   * Non-removable built-in validator.
   */
  private final ISkAbilityManagerValidator builtinValidator = new ISkAbilityManagerValidator() {

    @Override
    public ValidationResult canSetRoleAbility( String aRoleId, String aAbilityId, boolean aEnable ) {
      // TODO Auto-generated method stub
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canResetRoleAbilities( String aRoleId, boolean aEnableAll ) {
      // TODO Auto-generated method stub
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canRemoveKind( String aKindId ) {
      // TODO Auto-generated method stub
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canRemoveAbility( String aAbilityId ) {
      // TODO Auto-generated method stub
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canDefineKind( IDtoSkAbilityKind aDto, ISkAbilityKind aExistingKind ) {
      // TODO Auto-generated method stub
      return ValidationResult.SUCCESS;
    }

    @Override
    public ValidationResult canDefineAbility( IDtoSkAbility aDto, ISkAbility aExistingAbility ) {
      // TODO Auto-generated method stub
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
  // ISkAbilityManager
  //

  @Override
  public IStridablesList<ISkAbility> listRoleAbilities( String aRoleId ) {
    // TODO Auto-generated method stub
    return IStridablesList.EMPTY;
  }

  @Override
  public boolean isAbilityAllowed( String aRoleId, String aAbilityId ) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void resetRoleAbilities( String aRoleId, boolean aEnableAll ) {
    // TODO Auto-generated method stub
  }

  @Override
  public void setRoleAbility( String aRoleId, String aAbilityId, boolean aEnable ) {
    // TODO Auto-generated method stub
  }

  @Override
  public IStridablesList<ISkAbility> listAbilities() {
    // TODO Auto-generated method stub
    return IStridablesList.EMPTY;
  }

  @Override
  public IStridablesList<ISkAbilityKind> listKinds() {
    // TODO Auto-generated method stub
    return IStridablesList.EMPTY;
  }

  @Override
  public void defineAbility( IDtoSkAbility aDto ) {
    // TODO Auto-generated method stub
  }

  @Override
  public void removeAbility( String aAbilityId ) {
    // TODO Auto-generated method stub
  }

  @Override
  public void defineKind( IDtoSkAbilityKind aDto ) {
    // TODO Auto-generated method stub
  }

  @Override
  public void removeKind( String aKindId ) {
    // TODO Auto-generated method stub
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
