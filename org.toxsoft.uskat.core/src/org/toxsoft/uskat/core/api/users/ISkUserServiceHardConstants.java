package org.toxsoft.uskat.core.api.users;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.api.users.ISkResources.*;

import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * Unchangeable constants of the user service.
 *
 * @author hazard157
 */
public interface ISkUserServiceHardConstants {

  // ------------------------------------------------------------------------------------
  // Role

  /**
   * Role class ID.
   */
  String CLSID_ROLE = ISkHardConstants.SK_ID + ".Role"; //$NON-NLS-1$

  /**
   * Unremovable administrative role.
   * <p>
   * This role always have all the right.
   */
  String ROLE_ID_ROOT = "rootRole"; //$NON-NLS-1$

  /**
   * Unremovable administrative role.
   * <p>
   * This role rights as defined for guest by the administrator.
   * <p>
   * Any user always has an assigned guest role.
   */
  String ROLE_ID_GUEST = "guestRole"; //$NON-NLS-1$

  /**
   * SKID of root role.
   */
  Skid SKID_ROLE_ROOT = new Skid( CLSID_ROLE, ROLE_ID_GUEST );

  /**
   * SKID of guest role.
   */
  Skid SKID_ROLE_GUEST = new Skid( CLSID_ROLE, ROLE_ID_GUEST );

  /**
   * ID of attribute {@link ISkRole#isEnabled()}.
   */
  String ATRID_ROLE_IS_ENABLED = "enabled"; //$NON-NLS-1$

  /**
   * ID of attribute {@link ISkRole#isHidden()}.
   */
  String ATRID_ROLE_IS_HIDDEN = "hidden"; //$NON-NLS-1$

  /**
   * Attribute {@link ISkRole#isEnabled()}.
   */
  IDtoAttrInfo ATRINF_ROLE_IS_ENABLED = DtoAttrInfo.create2( ATRID_ROLE_IS_ENABLED, DDEF_TS_BOOL, //
      TSID_NAME, STR_N_ROLE_IS_ENABLED, //
      TSID_DESCRIPTION, STR_D_ROLE_IS_ENABLED, //
      TSID_DEFAULT_VALUE, AV_TRUE //
  );

  /**
   * Attribute {@link ISkRole#isHidden()}.
   */
  IDtoAttrInfo ATRINF_ROLE_IS_HIDDEN = DtoAttrInfo.create2( ATRID_ROLE_IS_HIDDEN, DDEF_TS_BOOL, //
      TSID_NAME, STR_N_ROLE_IS_HIDDEN, //
      TSID_DESCRIPTION, STR_D_ROLE_IS_HIDDEN, //
      TSID_DEFAULT_VALUE, AV_FALSE //
  );

  // ------------------------------------------------------------------------------------
  // User

  /**
   * User class ID.
   */
  String CLSID_USER = ISkHardConstants.SK_ID + ".User"; //$NON-NLS-1$

  /**
   * Unremovable administrative account login.
   * <p>
   * This account always has only one role {@link #ROLE_ID_ROOT}. This account can not be disabled.
   */
  String USER_ID_ROOT = "root"; //$NON-NLS-1$

  /**
   * Unremovable guest account login.
   * <p>
   * This account may be disabled.
   */
  String USER_ID_GUEST = "guest"; //$NON-NLS-1$

  /**
   * SKID of user {@link #USER_ID_ROOT}.
   */
  Skid SKID_USER_ROOT = new Skid( CLSID_USER, USER_ID_ROOT );

  /**
   * SKID of user {@link #USER_ID_GUEST}.
   */
  Skid SKID_USER_GUEST = new Skid( CLSID_USER, USER_ID_GUEST );

  /**
   * ID of attribute {@link #ATRINF_PASSWORD}.
   */
  String ATRID_PASSWORD = "password"; //$NON-NLS-1$

  /**
   * ID of attribute {@link #ATRINF_USER_IS_ENABLED}.
   */
  String ATRID_USER_IS_ENABLED = "enabled"; //$NON-NLS-1$

  /**
   * ID of attribute {@link #ATRINF_USER_IS_HIDDEN}.
   */
  String ATRID_USER_IS_HIDDEN = "hidden"; //$NON-NLS-1$

  /**
   * ID of link {@link #LNKID_USER_ROLES}.
   */
  String LNKID_USER_ROLES = "roles"; //$NON-NLS-1$

  /**
   * Attribute {@link ISkUser#password()}.
   */
  IDtoAttrInfo ATRINF_PASSWORD = DtoAttrInfo.create2( ATRID_PASSWORD, DDEF_STRING, //
      TSID_NAME, STR_N_PASSWORD, //
      TSID_DESCRIPTION, STR_D_PASSWORD, //
      TSID_DEFAULT_VALUE, AV_STR_EMPTY //
  );

  /**
   * Attribute {@link ISkUser#isEnabled()}.
   */
  IDtoAttrInfo ATRINF_USER_IS_ENABLED = DtoAttrInfo.create2( ATRID_USER_IS_ENABLED, DDEF_TS_BOOL, //
      TSID_NAME, STR_N_USER_IS_ENABLED, //
      TSID_DESCRIPTION, STR_D_USER_IS_ENABLED, //
      TSID_DEFAULT_VALUE, AV_TRUE //
  );

  /**
   * Attribute {@link ISkUser#isHidden()}.
   */
  IDtoAttrInfo ATRINF_USER_IS_HIDDEN = DtoAttrInfo.create2( ATRID_USER_IS_HIDDEN, DDEF_TS_BOOL, //
      TSID_NAME, STR_N_USER_IS_HIDDEN, //
      TSID_DESCRIPTION, STR_D_USER_IS_HIDDEN, //
      TSID_DEFAULT_VALUE, AV_FALSE //
  );

  /**
   * Link {@link ISkUser#listRoles()}.
   */
  IDtoLinkInfo LNKINF_USER_ROLES = DtoLinkInfo.create2( LNKID_USER_ROLES, //
      new SingleStringList( CLSID_ROLE ), new CollConstraint( 0, false, true, true ), //
      TSID_NAME, STR_N_ROLES, //
      TSID_DESCRIPTION, STR_D_ROLES //
  );

}
