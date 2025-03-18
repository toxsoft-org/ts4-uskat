package org.toxsoft.uskat.core.api.users;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.gw.IGwHardConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.l10n.ISkCoreSharedResources.*;

import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.api.users.ability.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Unchangeable constants of the user service.
 *
 * @author hazard157
 */
public interface ISkUserServiceHardConstants {

  // ------------------------------------------------------------------------------------
  // class IDs
  //

  /**
   * {@link ISkRole} class ID.
   */
  String CLSID_ROLE = ISkHardConstants.SK_ID + ".Role"; //$NON-NLS-1$

  /**
   * {@link ISkUser} class ID.
   */
  String CLSID_USER = ISkHardConstants.SK_ID + ".User"; //$NON-NLS-1$

  /**
   * {@link ISkAbility} class ID.
   */
  String CLSID_ABILITY = ISkHardConstants.SK_ID + ".Ability"; //$NON-NLS-1$

  /**
   * {@link ISkAbilityKind} class ID.
   */
  String CLSID_ABILITY_KIND = ISkHardConstants.SK_ID + ".AbilityKind"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Role

  /**
   * Non-removable administrative role.
   * <p>
   * This role always have all the rights.
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
   * The role identifies at connection opening that user must be logged in with it's default role.
   * <p>
   * FIXME the concept of the default role for user must be implemented in UserService.
   */
  String ROLE_ID_USKAT_DEFAULT = "__uskat_default__"; //$NON-NLS-1$

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
   * ID of link {@link ISkRole#listAllowedAbilities()}.
   */
  String LNKID_ROLE_ALLOWED_ABILITIES = "allowedAbilities"; //$NON-NLS-1$

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

  /**
   * Link {@link ISkRole#listAllowedAbilities()}.
   */
  IDtoLinkInfo LNKINF_ROLE_ALLOWED_ABILITIES = DtoLinkInfo.create2( LNKID_ROLE_ALLOWED_ABILITIES, //
      new SingleStringList( CLSID_ABILITY ), new CollConstraint( 0, false, true, true ), //
      TSID_NAME, STR_ROLE_ALLOWED_ABILITIES, //
      TSID_DESCRIPTION, STR_ROLE_ALLOWED_ABILITIES_D //
  );

  /**
   * Creates DTO of {@link #CLSID_ROLE} class.
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
    cinf.linkInfos().add( LNKINF_ROLE_ALLOWED_ABILITIES );
    return cinf;
  }

  /**
   * Determines if role is an immutable built-in role.
   *
   * @param aRoleId String - the role ID
   * @return boolean - <code>true</code> built-in immutable role, <code>false</code> - administrator defined role
   */
  static boolean isImmutableRole( String aRoleId ) {
    return switch( aRoleId ) {
      case ROLE_ID_ROOT, ROLE_ID_GUEST -> true;
      default -> false;
    };
  }

  // ------------------------------------------------------------------------------------
  // User

  /**
   * Non-removable administrative account login.
   * <p>
   * This account always has only one role {@link #ROLE_ID_ROOT}. This account can not be disabled.
   */
  String USER_ID_ROOT = "root"; //$NON-NLS-1$

  /**
   * Initial password for root user.
   * <p>
   * On freshly installed systems root user will have this password until first password change.
   */
  String INITIAL_ROOT_PASSWORD = "root"; //$NON-NLS-1$

  /**
   * Non-removable guest account login.
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
   * Creates DTO of {@link #CLSID_USER} class.
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

  /**
   * Determines if user is an immutable built-in user.
   *
   * @param aUserId String - the user ID
   * @return boolean - <code>true</code> built-in immutable user, <code>false</code> - administrator defined user
   */
  static boolean isImmutableUser( String aUserId ) {
    return switch( aUserId ) {
      case USER_ID_ROOT, USER_ID_GUEST -> true;
      default -> false;
    };
  }

  // ------------------------------------------------------------------------------------
  // Ability and ability kind
  //

  /**
   * ID of the link {@link #LNKINF_ABILITIES_OF_KIND}.
   */
  String LNKID_ABILITIES_OF_KIND = "abilities"; //$NON-NLS-1$

  /**
   * Link {@link ISkAbilityKind#listAbilities()}.
   */
  IDtoLinkInfo LNKINF_ABILITIES_OF_KIND = DtoLinkInfo.create2( LNKID_ABILITIES_OF_KIND, //
      new SingleStringList( CLSID_ABILITY ), new CollConstraint( 0, false, true, true ), //
      TSID_NAME, STR_ABILITIES_OF_KIND, //
      TSID_DESCRIPTION, STR_ABILITIES_OF_KIND_D //
  );

  /**
   * Built-in kind ID: for abilities with unspecified or non-existing kind IDs.
   */
  String ABILITY_KIND_ID_UNDEFINED = "kind.undefined"; //$NON-NLS-1$

  /**
   * SKID corresponding to {@link #ABILITY_KIND_ID_UNDEFINED}.
   */
  Skid SKID_ABILITY_KIND_UNDEFINED = new Skid( CLSID_ABILITY_KIND, ABILITY_KIND_ID_UNDEFINED );

  /**
   * Creates DTO of {@link #CLSID_ABILITY} class.
   *
   * @return {@link IDtoClassInfo} - {@link ISkAbility#CLASS_ID} class info
   */
  static IDtoClassInfo internalCreateAbilityClassDto() {
    DtoClassInfo cinf = new DtoClassInfo( CLSID_ABILITY, GW_ROOT_CLASS_ID, OptionSetUtils.createOpSet( //
        TSID_NAME, STR_ABILITY, //
        TSID_DESCRIPTION, STR_ABILITY_D //
    ) );
    OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS.setValue( cinf.params(), AV_TRUE );
    OPDEF_SK_IS_SOURCE_USKAT_CORE_CLASS.setValue( cinf.params(), AV_TRUE );
    return cinf;
  }

  /**
   * Creates DTO of {@link #CLSID_ABILITY_KIND} class.
   *
   * @return {@link IDtoClassInfo} - {@link ISkAbilityKind#CLASS_ID} class info
   */
  static IDtoClassInfo internalCreateAbilityKindClassDto() {
    DtoClassInfo cinf = new DtoClassInfo( CLSID_ABILITY_KIND, GW_ROOT_CLASS_ID, OptionSetUtils.createOpSet( //
        TSID_NAME, STR_ABILITY_KIND, //
        TSID_DESCRIPTION, STR_ABILITY_KIND_D //
    ) );
    OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS.setValue( cinf.params(), AV_TRUE );
    OPDEF_SK_IS_SOURCE_USKAT_CORE_CLASS.setValue( cinf.params(), AV_TRUE );
    cinf.linkInfos().add( LNKINF_ABILITIES_OF_KIND );
    return cinf;
  }

}
