package org.toxsoft.uskat.core.connection;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.connection.ISkResources.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.uskat.core.api.users.*;

/**
 * ISkConnection related constants.
 *
 * @author hazard157
 */
public interface ISkConnectionConstants {

  // ------------------------------------------------------------------------------------
  // Opening arguments

  /**
   * ID of connection opening argument {@link #ARGDEF_LOGIN}.
   */
  String ARGID_LOGIN = SK_ID + "login"; //$NON-NLS-1$

  /**
   * ID of connection opening argument {@link #ARGDEF_PASSWORD}.
   */
  String ARGID_PASSWORD = SK_ID + "password"; //$NON-NLS-1$

  /**
   * ID of connection opening argument {@link #ARGDEF_ROLES}.
   */
  String ARGID_ROLES = SK_ID + "roles"; //$NON-NLS-1$

  /**
   * The user login name.<br>
   * Usage: login name is an IDpath. If backend requests login name than it is mandatory.<br>
   * Default value: no default value
   */
  IDataDef ARGDEF_LOGIN = DataDef.create( ARGID_LOGIN, STRING, //
      TSID_NAME, STR_N_LOGIN, //
      TSID_DESCRIPTION, STR_D_LOGIN, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * User password information.<br>
   * Usage: user password or information about password. The format and requirements to this argument is backend
   * specific. Event more some backends does not request for login/passord/roles. However, if backend requests for
   * password it is a mandatory argument.<br>
   * Default value: no default value
   */
  IDataDef ARGDEF_PASSWORD = DataDef.create( ARGID_PASSWORD, STRING, //
      TSID_NAME, STR_N_PASSWORD, //
      TSID_DESCRIPTION, STR_D_PASSWORD, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * Optional {@link IStringList} with user roles STRIDs.<br>
   * Usage: each user has assocciated roles {@link ISkUser#listRoles()}, however at login time user roles may be
   * restricted by specifying this argument. Only the roles listed in this argument will be assigned to the user to
   * particular login. Note that STRIDs of the roles not listed in {@link ISkUser#listRoles()} are ignored. An mmpty
   * list or no arguemtn value in this option is interpreted as if all roles are accepted.<br>
   * Default value: {@link IStringList#EMPTY}<br>
   */
  IDataDef ARGDEF_ROLES = DataDef.create( ARGID_ROLES, VALOBJ, //
      TSID_NAME, STR_N_ROLES, //
      TSID_DESCRIPTION, STR_D_ROLES, //
      TSID_KEEPER_ID, StringListKeeper.KEEPER_ID, //
      TSID_DEFAULT_VALUE, StringListKeeper.AV_EMPTY_STRING_LIST, //
      TSID_IS_MANDATORY, AV_FALSE //
  );

}
