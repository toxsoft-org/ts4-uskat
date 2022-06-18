package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
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
  }

  // ------------------------------------------------------------------------------------
  // ApiWrapAbstractSkService
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
  public ISkUser defineUser( IDtoObject aDtoUser ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ISkRole defineRole( IDtoObject aDtoRole ) {
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
  public ITsValidator<String> passwordValidator() {
    return passwordValidator;
  }

  @Override
  public void addPasswordValidator( ITsValidator<String> aPasswordValidator ) {
    passwordValidator.addValidator( aPasswordValidator );
  }

  @Override
  public ITsValidationSupport<ISkUserServiceValidator> svs() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ITsEventer<ISkUserServiceListener> eventer() {
    // TODO Auto-generated method stub
    return null;
  }

}
