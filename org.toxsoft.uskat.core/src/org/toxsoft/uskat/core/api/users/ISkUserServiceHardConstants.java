package org.toxsoft.uskat.core.api.users;

/**
 * Unchangeable constants of the user service.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkUserServiceHardConstants {

  /**
   * Unremovable administrative account login.
   * <p>
   * This account always has only one role {@link #SUPER_ROLE_ID}. This account can not be disabled.
   */
  String SUPER_USER_ID = "root"; //$NON-NLS-1$

  /**
   * Unremovable administrative role.
   * <p>
   * This role always have all the right.
   */
  String SUPER_ROLE_ID = "rootRole"; //$NON-NLS-1$

  /**
   * Unremovable guest account login.
   * <p>
   * This account may be disabled.
   */
  String GUEST_USER_ID = "guest"; //$NON-NLS-1$

  /**
   * Unremovable administrative role.
   * <p>
   * This role rights as defined for guest by the administrator.
   * <p>
   * Any user always has an assigned guest role.
   */
  String GUEST_ROLE_ID = "guestRole"; //$NON-NLS-1$

  String AID_PASSWORD        = "password"; //$NON-NLS-1$
  String AID_USER_IS_ENABLED = "enabled";  //$NON-NLS-1$
  String AID_USER_IS_HIDDEN  = "hidden";   //$NON-NLS-1$
  String LID_USER_ROLES      = "roles";    //$NON-NLS-1$

  String AID_ROLE_IS_ENABLED = "enabled"; //$NON-NLS-1$
  String AID_ROLE_IS_HIDDEN  = "hidden";  //$NON-NLS-1$

}
