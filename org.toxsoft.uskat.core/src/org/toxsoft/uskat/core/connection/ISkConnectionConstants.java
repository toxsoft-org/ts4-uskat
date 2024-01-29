package org.toxsoft.uskat.core.connection;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.connection.ISkResources.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;

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
   * ID of connection opening argument {@link #ARGDEF_ROLE}.
   */
  String ARGID_ROLE = SK_ID + "role"; //$NON-NLS-1$

  /**
   * The user login name.<br>
   * Usage: login name is an IDpath. If backend requests login name than it is mandatory.<br>
   * Default value: no default value
   */
  IDataDef ARGDEF_LOGIN = DataDef.create( ARGID_LOGIN, STRING, //
      TSID_NAME, STR_LOGIN, //
      TSID_DESCRIPTION, STR_LOGIN_D, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * User password information.<br>
   * Usage: user password or information about password. The format and requirements to this argument is backend
   * specific. Even more some backends does not request for login/password/roles. However, if backend requests for
   * password it is a mandatory argument.<br>
   * Default value: no default value
   */
  IDataDef ARGDEF_PASSWORD = DataDef.create( ARGID_PASSWORD, STRING, //
      TSID_NAME, STR_PASSWORD, //
      TSID_DESCRIPTION, STR_PASSWORD_D, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * The user login name.<br>
   * Usage: login name is an IDpath. If backend requests login name than it is mandatory.<br>
   * Default value: no default value
   */
  IDataDef ARGDEF_ROLE = DataDef.create( ARGID_ROLE, STRING, //
      TSID_NAME, STR_ROLE, //
      TSID_DESCRIPTION, STR_ROLE_D, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * All argument options needed for {@link ESkAuthentificationType#SIMPLE}.
   */
  IStridablesList<IDataDef> ALL_SIMPLE_AUTHENTIFICATION_ARGS = new StridablesList<>( //
      ARGDEF_LOGIN, //
      ARGDEF_PASSWORD //
  );

}
