package org.toxsoft.uskat.core.api.users;

import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * The users and roles managment service.
 * <p>
 * The user {@link ISkUser} identifies both a person and a software component allowed to establish connection to the
 * USkat. The role is set of the rules and rights to access data and USkat functionality. Several roles may be
 * associated to the user.
 * <p>
 * This service is based on the following consepts:
 * <ul>
 * <li>there alwayes exists the superuser (with ID {@link ISkUserServiceHardConstants#SUPER_USER_ID}) with associated
 * super-role (with ID {@link ISkUserServiceHardConstants#SUPER_ROLE_ID}) both having all rights and access to the whole
 * funcionality of USkat. Nither super-user nor supr-role may be disabled;</li>
 * <li>the user ID {@link ISkUser#id()} is the same as the user login {@link ISkUser#login()} and STRID of the used
 * object {@link ISkUser#strid()};</li>
 * <li>to open the connection mandatory arguments login and password ({@link ISkConnectionConstants#ARGDEF_LOGIN} and
 * {@link ISkConnectionConstants#ARGID_PASSWORD}) must be specified;</li>
 * <li>there always exists the guest user {@link ISkUserServiceHardConstants#USER_ID_GUEST} and the guest role
 * {@link ISkUserServiceHardConstants#ROLE_ID_GUEST} which simply does not allows any data or functionality it is asked
 * for. Other data and fucnctionality is allowed for guest role. Guest user roles can not be changed and iot has the
 * only role - the guest role. However, either guest user or guest role may be disabled;</li>
 * <li>the user may be temporarily disabled - {@link ISkUser#isEnabled()} = <code>false</code>. Disabled users are not
 * alloed to open the connection.</li>
 * </ul>
 *
 * @author hazard157
 */
public interface ISkUserService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".Users"; //$NON-NLS-1$

  /**
   * Returns the list of the existing users.
   * <p>
   * Returns all usrs including the hidden ones. It's up to application to hide users with {@link ISkUser#isHidden()}
   * flags set.
   *
   * @return {@link IStridablesList}&lt;{@link ISkUser}&gt; - the list or users objects
   */
  IStridablesList<ISkUser> listUsers();

  /**
   * Returns the list of the existing roles.
   *
   * @return {@link IStridablesList}&lt;{@link ISkRole}&gt; - the list or roles objects
   */
  IStridablesList<ISkRole> listRoles();

  /**
   * Creates new or updates existing user.
   * <p>
   * The argument may contain links to the roles.
   *
   * @param aDtoUser {@link IDtoFullObject} - the user data
   * @return {@link ISkUser} - created/update user object
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException object DTO does not refers to {@link ISkUser#CLASS_ID}
   * @throws TsValidationFailedRtException failed check of {@link ISkUserServiceValidator}
   */
  ISkUser defineUser( IDtoFullObject aDtoUser );

  /**
   * Creates new or updates existing role.
   *
   * @param aDtoRole {@link IDtoObject} - the role data
   * @return {@link ISkRole} - created/update role object
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException object DTO does not refers to {@link ISkRole#CLASS_ID}
   * @throws TsValidationFailedRtException failed check of {@link ISkUserServiceValidator}
   */
  ISkRole defineRole( IDtoObject aDtoRole );

  /**
   * Removes the user.
   *
   * @param aLogin String - user login that is user ID and {@link ISkObject#strid()}
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed check of {@link ISkUserServiceValidator}
   */
  void removeUser( String aLogin );

  /**
   * Removes the role.
   *
   * @param aRoleId String - the role ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed check of {@link ISkUserServiceValidator}
   */
  void removeRole( String aRoleId );

  /**
   * Changes user's enabled state {@link ISkUser#isEnabled()}.
   * <p>
   * Any user may be disabled at any time except of builtin root user.
   *
   * @param aLogin String - user login that is user ID and {@link ISkObject#strid()}
   * @param aEnabled boolean - enabledstate
   * @return {@link ISkUser} - updated user object
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such user
   * @throws TsIllegalStateRtException trying to disable root user
   */
  ISkUser setUserEnabled( String aLogin, boolean aEnabled );

  /**
   * Changes user's hidden state {@link ISkUser#isHidden()}.
   * <p>
   * Any user may be hidden at any time including builtin root user.
   *
   * @param aLogin String - user login that is user ID and {@link ISkObject#strid()}
   * @param aHidden boolean - hidden state
   * @return {@link ISkUser} - updated user object
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such user
   */
  ISkUser setUserHidden( String aLogin, boolean aHidden );

  /**
   * Sets (changes) passord for the user specified by the login (ID).
   *
   * @param aLogin String - user login that is user ID and {@link ISkObject#strid()}
   * @param aPassword String - the new password
   * @return {@link ISkUser} - updated user object
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such user
   * @throws TsValidationFailedRtException failed check of {@link #passwordValidator()}
   * @throws TsValidationFailedRtException failed check of {@link ISkUserServiceValidator}
   */
  ISkUser setUserPassword( String aLogin, String aPassword );

  /**
   * Sets (changes) roles user may log in.
   * <p>
   * Event if list of roles is empty the user always has an assigned guest role.
   *
   * @param aRoles {@link IStridablesList}&lt;{@link ISkRole}&gt; - list of user roles
   * @return {@link ISkUser} - updated user object
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such user
   * @throws TsValidationFailedRtException failed check of {@link ISkUserServiceValidator}
   */
  ISkUser setUserRoles( IStridablesList<ISkRole> aRoles );

  /**
   * Returns the password validator.
   * <p>
   * Validator checks if password is acceptable. Besides builtin validation, application specific validators may be
   * added by {@link #addPasswordValidator(ITsValidator)}.
   *
   * @return {@link ITsValidator}&lt;String&gt; - the password string validator
   */
  ITsValidator<String> passwordValidator();

  /**
   * Add application specific password validation strategy to {@link #passwordValidator()}.
   *
   * @param aPasswordValidator {@link ITsValidationSupport}&lt;String&gt; - the app-specific password string validator
   */
  void addPasswordValidator( ITsValidator<String> aPasswordValidator );

  // ------------------------------------------------------------------------------------
  // Service support

  /**
   * Returns the service validator.
   *
   * @return {@link ITsValidationSupport}&lt;{@link ISkUserServiceValidator}&gt; - the service validator
   */
  ITsValidationSupport<ISkUserServiceValidator> svs();

  /**
   * Returns the service eventer.
   *
   * @return {@link ITsEventer}&lt;{@link ISkUserServiceListener}&gt; - the service eventer
   */
  ITsEventer<ISkUserServiceListener> eventer();

}
