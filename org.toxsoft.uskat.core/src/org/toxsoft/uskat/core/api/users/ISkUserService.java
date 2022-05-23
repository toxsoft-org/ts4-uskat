package org.toxsoft.uskat.core.api.users;

import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.*;
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
 * <li>there always exists the guest user {@link ISkUserServiceHardConstants#GUEST_USER_ID} and the guest role
 * {@link ISkUserServiceHardConstants#GUEST_ROLE_ID} which simply does not allows any data or functionality it is asked
 * for. Other data and fucnctionality is allowed for guest role. Guest user roles can not be changed and iot has the
 * only role - the guest role. However, either guest user or guest role may be disabled;</li>
 * <li>the user may be temporarily disabled - {@link ISkUser#isEnabled()} = <code>false</code>. Disabled users are not
 * alloed to open the connection.</li>
 * </ul>
 *
 * @author hazard157
 */
public interface ISkUserService {

  IStridablesList<ISkUser> listUsers();

  IStridablesList<ISkRole> listRoles();

  ISkUser defineUser( IDtoObject aDtoUser );

  ISkRole defineRole( IDtoObject aDtoRole );

  void removeUser( String aUserId );

  void removeRole( String aRoleId );

  ISkUser setUserEnabled( String aLogin, boolean aEnabled );

  ISkUser setUserHidden( String aLogin, boolean aHidden );

  ISkUser setUserPassword( String aLogin, String aPassword );

  ValidationResult validatePassowrd( String aPassword );

  ITsValidationSupport<ISkUserServiceValidator> svs();

  ITsEventer<ISkUserServiceListener> eventer();

}
