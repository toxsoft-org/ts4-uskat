package org.toxsoft.uskat.core.api.users;

import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * User - an entity allowed to connect to the Skat server.
 * <p>
 * User identifier {@link #id()} (same as {@link #strid()}) is user login.
 *
 * @author hazard157
 */
public interface ISkUser
    extends ISkObject {

  /**
   * The {@link ISkUser} class identifier.
   */
  String CLASS_ID = ISkHardConstants.SK_ID + ".User"; //$NON-NLS-1$

  /**
   * Returns user login, the same as {@link #id()} or {@link #strid()}.
   *
   * @return String - user login
   */
  default String login() {
    return strid();
  }

  /**
   * Returns the password hashcode string.
   *
   * @return String - the password used to open the session with the server
   */
  default String password() {
    return attrs().getStr( AID_PASSWORD );
  }

  /**
   * Returns the roles the user are allowed to log in.
   * <p>
   * At each login the user roles may be restricted by specifying the value of the argument
   * {@link ISkConnectionConstants#ARGDEF_ROLES}.
   *
   * @return {@link IStridablesList}&lt;{@link ISkRole}&gt; - the user roles
   */
  IStridablesList<ISkRole> listRoles();

  /**
   * Determines if user is allowed to connect to server.
   *
   * @return boolean - user is enabled to login<br>
   *         <code>true</code> if user is allowed to connect to the server<br>
   *         <code>false</code> if thei user is temporary disabled
   */
  default boolean isEnabled() {
    return attrs().getBool( AID_USER_IS_ENABLED );
  }

  /**
   * Determines if user is hidden from system administrator.
   * <p>
   * Usually hidden users are system components such as local servers connected to main one, or control boxes directly
   * connected to servers.
   * <p>
   * This is just a hint - user service soes not uses this flag. It's up to application to hide users with
   * {@link #isHidden()} flags.
   *
   * @return boolean - <code>true</code> if user is visible not for system administrator, only for developer
   */
  default boolean isHidden() {
    return attrs().getBool( AID_USER_IS_HIDDEN );
  }

}
