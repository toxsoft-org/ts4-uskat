package org.toxsoft.uskat.core.backend;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.backend.ISkResources.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;

/**
 * Constants common for all backends.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface ISkBackendHardConstant {

  String SKB_ID = SK_ID + ".backend"; //$NON-NLS-1$

  String SKBI_ID = SKB_ID + ".info"; //$NON-NLS-1$

  String OPID_SKBI_MAX_CLOB_LENGTH = SKBI_ID + ".MaxClobLength"; //$NON-NLS-1$

  IDataDef OPDEF_SKBI_MAX_CLOB_LENGTH = DataDef.create( OPID_SKBI_MAX_CLOB_LENGTH, INTEGER, //
      TSID_NAME, STR_N_SKBI_MAX_CLOB_LENGTH, //
      TSID_DESCRIPTION, STR_D_SKBI_MAX_CLOB_LENGTH //
  );

  // ------------------------------------------------------------------------------------
  // IBaEvents info

  String OPID_SKBI_BA_EVENTS_IS_REMOTE  = SKBI_ID + ".BaEvents.IsRemove";  //$NON-NLS-1$
  String OPID_SKBI_BA_EVENTS_IS_HISTORY = SKBI_ID + ".BaEvents.IsHistory"; //$NON-NLS-1$

  IDataDef OPDEF_SKBI_BA_EVENTS_IS_REMOTE = DataDef.create( OPID_SKBI_BA_EVENTS_IS_REMOTE, INTEGER, //
      TSID_NAME, STR_N_SKBI_BA_EVENTS_IS_REMOTE, //
      TSID_DESCRIPTION, STR_D_SKBI_BA_EVENTS_IS_REMOTE, //
      TSID_DEFAULT_VALUE, AV_FALSE //
  );

  IDataDef OPDEF_SKBI_BA_EVENTS_IS_HISTORY = DataDef.create( OPID_SKBI_BA_EVENTS_IS_HISTORY, INTEGER, //
      TSID_NAME, STR_N_SKBI_BA_EVENTS_IS_HISTORY, //
      TSID_DESCRIPTION, STR_D_SKBI_BA_EVENTS_IS_HISTORY, //
      TSID_DEFAULT_VALUE, AV_FALSE //
  );

}
