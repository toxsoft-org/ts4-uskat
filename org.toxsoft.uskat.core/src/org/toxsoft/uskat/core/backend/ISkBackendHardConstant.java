package org.toxsoft.uskat.core.backend;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.backend.ISkResources.*;

import org.toxsoft.core.tslib.av.impl.DataDef;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.impl.Stridable;

/**
 * Constants common for all backends.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkBackendHardConstant {

  String SKB_ID  = SK_ID + ".backend"; //$NON-NLS-1$
  String SKBI_ID = SKB_ID + ".info";   //$NON-NLS-1$

  int DEFAULT_MAX_CLOB_LENGTH = 100_000;

  // ------------------------------------------------------------------------------------
  // ISkBackendInfo

  String OPID_SKBI_LOGGED_USER               = SKBI_ID + ".LoggedUser";             //$NON-NLS-1$
  String OPID_SKBI_MAX_CLOB_LENGTH           = SKBI_ID + ".MaxClobLength";          //$NON-NLS-1$
  String OPID_SKBI_NEED_THREAD_SAFE_FRONTEND = SKBI_ID + ".NeedThreadSafeFrontend"; //$NON-NLS-1$

  IDataDef OPDEF_SKBI_LOGGED_USER = DataDef.create( OPID_SKBI_LOGGED_USER, VALOBJ, //
      TSID_NAME, STR_N_SKBI_LOGGED_USER, //
      TSID_DESCRIPTION, STR_D_SKBI_LOGGED_USER //
  );

  IDataDef OPDEF_SKBI_MAX_CLOB_LENGTH = DataDef.create( OPID_SKBI_MAX_CLOB_LENGTH, INTEGER, //
      TSID_NAME, STR_N_SKBI_MAX_CLOB_LENGTH, //
      TSID_DEFAULT_VALUE, avInt( DEFAULT_MAX_CLOB_LENGTH ), //
      TSID_DESCRIPTION, STR_D_SKBI_MAX_CLOB_LENGTH //
  );

  IDataDef OPDEF_SKBI_NEED_THREAD_SAFE_FRONTEND = DataDef.create( OPID_SKBI_NEED_THREAD_SAFE_FRONTEND, BOOLEAN, //
      TSID_NAME, STR_N_SKBI_NEED_THREAD_SAFE_FRONTEND, //
      TSID_DESCRIPTION, STR_D_SKBI_NEED_THREAD_SAFE_FRONTEND, //
      TSID_DEFAULT_VALUE, AV_FALSE //
  );

  // ------------------------------------------------------------------------------------
  // IBaClasses

  String BAID_CLASSES = SKB_ID + ".Classes"; //$NON-NLS-1$

  IStridable BAINF_CLASSES = new Stridable( BAID_CLASSES, STR_N_BA_CLASSES, STR_D_BA_CLASSES );

  // ------------------------------------------------------------------------------------
  // IBaClobs

  String BAID_CLOBS = SKB_ID + ".Clobs"; //$NON-NLS-1$

  IStridable BAINF_CLOBS = new Stridable( BAID_CLOBS, STR_N_BA_CLOBS, STR_D_BA_CLOBS );

  // ------------------------------------------------------------------------------------
  // IBaEvents

  String BAID_EVENTS                    = SKB_ID + ".Events";              //$NON-NLS-1$
  String OPID_SKBI_BA_EVENTS_IS_HISTORY = SKBI_ID + ".BaEvents.IsHistory"; //$NON-NLS-1$

  IStridable BAINF_EVENTS = new Stridable( BAID_EVENTS, STR_N_BA_EVENTS, STR_D_BA_EVENTS );

  IDataDef OPDEF_SKBI_BA_EVENTS_IS_HISTORY = DataDef.create( OPID_SKBI_BA_EVENTS_IS_HISTORY, INTEGER, //
      TSID_NAME, STR_N_SKBI_BA_EVENTS_IS_HISTORY, //
      TSID_DESCRIPTION, STR_D_SKBI_BA_EVENTS_IS_HISTORY, //
      TSID_DEFAULT_VALUE, AV_FALSE //
  );

  // ------------------------------------------------------------------------------------
  // IBaCommands

  String BAID_COMMANDS = SKB_ID + ".Commands"; //$NON-NLS-1$

  IStridable BAINF_COMMANDS = new Stridable( BAID_COMMANDS, STR_N_BA_COMMANDS, STR_D_BA_COMMANDS );

  // ------------------------------------------------------------------------------------
  // IBaLinks

  String BAID_RTDATA = SKB_ID + ".RTdata"; //$NON-NLS-1$

  IStridable BAINF_RTDATA = new Stridable( BAID_RTDATA, STR_N_BA_RTDATA, STR_D_BA_RTDATA );

  // ------------------------------------------------------------------------------------
  // IBaLinks

  String BAID_LINKS = SKB_ID + ".Links"; //$NON-NLS-1$

  IStridable BAINF_LINKS = new Stridable( BAID_LINKS, STR_N_BA_LINKS, STR_D_BA_LINKS );

  // ------------------------------------------------------------------------------------
  // IBaObjects

  String BAID_OBJECTS = SKB_ID + ".Objects"; //$NON-NLS-1$

  IStridable BAINF_OBJECTS = new Stridable( BAID_OBJECTS, STR_N_BA_OBJECTS, STR_D_BA_OBJECTS );

  // ------------------------------------------------------------------------------------
  // IBaQueries

  String BAID_QUERIES = SKB_ID + ".Queries"; //$NON-NLS-1$

  IStridable BAINF_QUERIES = new Stridable( BAID_QUERIES, STR_N_BA_QUERIES, STR_D_BA_QUERIES );

  // ------------------------------------------------------------------------------------
  // IBaGwidDb

  String BAID_GWID_DB = SKB_ID + ".GwidDb"; //$NON-NLS-1$

  IStridable BAINF_GWID_DB = new Stridable( BAID_GWID_DB, STR_N_BA_GWID_DB, STR_D_BA_GWID_DB );

}
