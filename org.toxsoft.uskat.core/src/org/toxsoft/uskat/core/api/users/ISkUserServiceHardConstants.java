package org.toxsoft.uskat.core.api.users;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.gw.IGwHardConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.api.users.ISkResources.*;

import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.core.utils.*;

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
   * Non-removable administrative role.
   * <p>
   * This role always have all the right.
   */
  String ROLE_ID_ROOT = "rootRole"; //$NON-NLS-1$

  /**
   * Non-removable administrative role.
   * <p>
   * This role rights as defined for guest by the administrator.
   * <p>
   * Any user always has an assigned guest role.
   */
  String ROLE_ID_GUEST = "guestRole"; //$NON-NLS-1$

  /**
   * SKID of root role.
   */
  Skid SKID_ROLE_ROOT = new Skid( CLSID_ROLE, ROLE_ID_ROOT );

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
      TSID_NAME, STR_ROLE_IS_ENABLED, //
      TSID_DESCRIPTION, STR_ROLE_IS_ENABLED_D, //
      TSID_DEFAULT_VALUE, AV_TRUE //
  );

  /**
   * Attribute {@link ISkRole#isHidden()}.
   */
  IDtoAttrInfo ATRINF_ROLE_IS_HIDDEN = DtoAttrInfo.create2( ATRID_ROLE_IS_HIDDEN, DDEF_TS_BOOL, //
      TSID_NAME, STR_ROLE_IS_HIDDEN, //
      TSID_DESCRIPTION, STR_ROLE_IS_HIDDEN_D, //
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
   * Initial password for root user.
   * <p>
   * On freshly installed systems root user will have this password untill first password change.
   */
  String INITIAL_ROOT_PASSWORD = "root"; //$NON-NLS-1$

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
   * ID of attribute {@link #ATRINF_PASSWORD_HASH}.
   */
  String ATRID_PASSWORD_HASH = "passwordHash"; //$NON-NLS-1$

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
   * Internal attribute storing password hash code calculated by {@link SkHelperUtils#getPasswordHashCode(String)}.
   */
  IDtoAttrInfo ATRINF_PASSWORD_HASH = DtoAttrInfo.create2( ATRID_PASSWORD_HASH, DDEF_STRING, //
      TSID_NAME, STR_PASSWORD, //
      TSID_DESCRIPTION, STR_PASSWORD_D, //
      TSID_DEFAULT_VALUE, AV_STR_EMPTY //
  );

  /**
   * Attribute {@link ISkUser#isEnabled()}.
   */
  IDtoAttrInfo ATRINF_USER_IS_ENABLED = DtoAttrInfo.create2( ATRID_USER_IS_ENABLED, DDEF_TS_BOOL, //
      TSID_NAME, STR_USER_IS_ENABLED, //
      TSID_DESCRIPTION, STR_USER_IS_ENABLED_D, //
      TSID_DEFAULT_VALUE, AV_TRUE //
  );

  /**
   * Attribute {@link ISkUser#isHidden()}.
   */
  IDtoAttrInfo ATRINF_USER_IS_HIDDEN = DtoAttrInfo.create2( ATRID_USER_IS_HIDDEN, DDEF_TS_BOOL, //
      TSID_NAME, STR_USER_IS_HIDDEN, //
      TSID_DESCRIPTION, STR_USER_IS_HIDDEN_D, //
      TSID_DEFAULT_VALUE, AV_FALSE //
  );

  /**
   * Link {@link ISkUser#listRoles()}.
   */
  IDtoLinkInfo LNKINF_USER_ROLES = DtoLinkInfo.create2( LNKID_USER_ROLES, //
      new SingleStringList( CLSID_ROLE ), new CollConstraint( 0, false, true, true ), //
      TSID_NAME, STR_ALLOWED_ROLES, //
      TSID_DESCRIPTION, STR_ALLOWED_ROLES_D //
  );

  /**
   * Creates DTO of {@link ISkRole#CLASS_ID} class.
   *
   * @return {@link IDtoClassInfo} - {@link ISkRole#CLASS_ID} class info
   */
  static IDtoClassInfo internalCreateRoleClassDto() {
    DtoClassInfo cinf = new DtoClassInfo( CLSID_ROLE, GW_ROOT_CLASS_ID, OptionSetUtils.createOpSet( //
        TSID_NAME, STR_ROLE, //
        TSID_DESCRIPTION, STR_ROLE_D //
    ) );
    OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS.setValue( cinf.params(), AV_TRUE );
    OPDEF_SK_IS_SOURCE_USKAT_CORE_CLASS.setValue( cinf.params(), AV_TRUE );
    cinf.attrInfos().add( ATRINF_ROLE_IS_ENABLED );
    cinf.attrInfos().add( ATRINF_ROLE_IS_HIDDEN );
    return cinf;
  }

  /**
   * Creates DTO of {@link ISkUser#CLASS_ID} class.
   *
   * @return {@link IDtoClassInfo} - {@link ISkUser#CLASS_ID} class info
   */
  static IDtoClassInfo internalCreateUserClassDto() {
    DtoClassInfo cinf = new DtoClassInfo( CLSID_USER, GW_ROOT_CLASS_ID, OptionSetUtils.createOpSet( //
        TSID_NAME, STR_USER, //
        TSID_DESCRIPTION, STR_USER_D //
    ) );
    OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS.setValue( cinf.params(), AV_TRUE );
    OPDEF_SK_IS_SOURCE_USKAT_CORE_CLASS.setValue( cinf.params(), AV_TRUE );
    cinf.attrInfos().add( ATRINF_PASSWORD_HASH );
    cinf.attrInfos().add( ATRINF_USER_IS_ENABLED );
    cinf.attrInfos().add( ATRINF_USER_IS_HIDDEN );
    cinf.linkInfos().add( LNKINF_USER_ROLES );
    return cinf;
  }

}
