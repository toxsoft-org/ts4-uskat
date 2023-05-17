package org.toxsoft.uskat.core.api.users;

import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.uskat.core.connection.ESkAuthentificationType;

/**
 * The user information.
 *
 * @author hazard157
 */
public interface ISkLoggedUserInfo {

  /**
   * User id.
   *
   * @return {@link Skid} user id
   */
  Skid userSkid();

  /**
   * User role.
   *
   * @return {@link Skid} role id
   */
  Skid roleSkid();

  /**
   * Authentification type used to login.
   *
   * @return {@link ESkAuthentificationType} authentification type.
   */
  ESkAuthentificationType authentificationType();
}
