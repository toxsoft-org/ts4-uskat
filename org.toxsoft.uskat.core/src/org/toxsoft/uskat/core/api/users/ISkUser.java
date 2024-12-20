package org.toxsoft.uskat.core.api.users;

import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.uskat.core.api.objserv.*;

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
  String CLASS_ID = ISkUserServiceHardConstants.CLSID_USER;

  /**
   * Returns user login, the same as {@link #id()} or {@link #strid()}.
   *
   * @return String - user login
   */
  default String login() {
    return strid();
  }

  /**
   * Returns the roles the user are allowed to log in.
   *
   * @return {@link IStridablesList}&lt;{@link ISkRole}&gt; - the user roles
   */
  IStridablesList<ISkRole> listRoles();

  /**
   * Determines if user is allowed to connect to server.
   *
   * @return boolean - user is enabled to login<br>
   *         <code>true</code> if user is allowed to connect to the server<br>
   *         <code>false</code> if the user is temporary disabled
   */
  default boolean isEnabled() {
    return attrs().getBool( ATRID_USER_IS_ENABLED );
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
    return attrs().getBool( ATRID_USER_IS_HIDDEN );
  }

}
