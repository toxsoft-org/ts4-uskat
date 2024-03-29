package org.toxsoft.uskat.core.api.users;

import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;

import org.toxsoft.uskat.core.api.objserv.*;

/**
 * The role is set of rules determining rights of the {@link ISkUser}.
 *
 * @author hazard157
 */
public interface ISkRole
    extends ISkObject {

  /**
   * The {@link ISkRole} class identifier.
   */
  String CLASS_ID = ISkUserServiceHardConstants.CLSID_ROLE;

  /**
   * Determines if the role is allowed to connect to server.
   *
   * @return boolean - role is enabled to login<br>
   *         <code>true</code> if role is allowed to connect to the server<br>
   *         <code>false</code> if the role is temporary disabled
   */
  default boolean isEnabled() {
    return attrs().getBool( ATRID_ROLE_IS_ENABLED );
  }

  /**
   * Determines if the role is hidden from system administrator.
   * <p>
   * Usually hidden roles holds system rights such as slave servers connected to master one, or control boxes directly
   * connected to servers.
   *
   * @return boolean - <code>true</code> if the roleis visible not for system administrator, only for developer
   */
  default boolean isHidden() {
    return attrs().getBool( ATRID_ROLE_IS_HIDDEN );
  }

}
