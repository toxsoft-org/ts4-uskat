package org.toxsoft.uskat.backend.memtext;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.backend.memtext.ISkResources.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.uskat.core.backend.*;

/**
 * Backend configuration constants.
 *
 * @author hazard157
 */
public interface IBackendMemtextConstants {

  /**
   * Backend ID prefix for subclass implementations.
   */
  String SKB_ID_MEMTEXT = ISkBackendHardConstant.SKB_ID + ".memtext"; //$NON-NLS-1$

  /**
   * Backend arg: objects of the listed classs IDs will not be stored.
   * <p>
   * More precisely, objects will be removed on backend close. When backend is working objects are stored in memory.
   */
  IDataDef OPDEF_NOT_STORED_OBJ_CLASS_IDS = DataDef.create( SKB_ID_MEMTEXT + ".NotStoredObjClassIds", VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_N_NOT_STORED_OBJ_CLASS_IDS, //
      TSID_DESCRIPTION, STR_D_NOT_STORED_OBJ_CLASS_IDS, //
      TSID_KEEPER_ID, avStr( StringListKeeper.KEEPER_ID ), //
      TSID_DEFAULT_VALUE, avValobj( IStringList.EMPTY, StringListKeeper.KEEPER, StringListKeeper.KEEPER_ID ) //
  );

  /**
   * Backend arg: determines if events history is stored permamnently.
   */
  IDataDef OPDEF_IS_EVENTS_STORED = DataDef.create( SKB_ID_MEMTEXT + ".IsEventsStored", BOOLEAN, //$NON-NLS-1$
      TSID_NAME, STR_N_IS_EVENTS_STORED, //
      TSID_DESCRIPTION, STR_D_IS_EVENTS_STORED, //
      TSID_DEFAULT_VALUE, AV_TRUE //
  );

  /**
   * Min acceptable value of option {@link #OPDEF_MAX_EVENTS_COUNT}.
   */
  int MIN_MAX_EVENTS_COUNT = 86_400 / 100;

  /**
   * Max acceptable value of option {@link #OPDEF_MAX_EVENTS_COUNT}.
   */
  int MAX_MAX_EVENTS_COUNT = 365 * 86_400;

  /**
   * Backend arg: maximum number of events in history.
   */
  IDataDef OPDEF_MAX_EVENTS_COUNT = DataDef.create( SKB_ID_MEMTEXT + ".MaxEventsCount", INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_MAX_EVENTS_COUNT, //
      TSID_DESCRIPTION, STR_D_MAX_EVENTS_COUNT, //
      TSID_MIN_INCLUSIVE, avInt( MIN_MAX_EVENTS_COUNT ), //
      TSID_MAX_INCLUSIVE, avInt( MAX_MAX_EVENTS_COUNT ), //
      TSID_DEFAULT_VALUE, avInt( 86_400 ) //
  );

  /**
   * Backend arg: determines if events history is stored permamnently.
   */
  IDataDef OPDEF_IS_CMDS_STORED = DataDef.create( SKB_ID_MEMTEXT + ".IsEventsStored", BOOLEAN, //$NON-NLS-1$
      TSID_NAME, STR_N_IS_CMDS_STORED, //
      TSID_DESCRIPTION, STR_D_IS_CMDS_STORED, //
      TSID_DEFAULT_VALUE, AV_TRUE //
  );

  /**
   * Min acceptable value of option {@link #OPDEF_MAX_CMDS_COUNT}.
   */
  int MIN_MAX_CMDS_COUNT = 100;

  /**
   * Max acceptable value of option {@link #OPDEF_MAX_CMDS_COUNT}.
   */
  int MAX_MAX_CMDS_COUNT = 10_000;

  /**
   * Backend arg: maximum number of events in history.
   */
  IDataDef OPDEF_MAX_CMDS_COUNT = DataDef.create( SKB_ID_MEMTEXT + ".MaxEventsCount", INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_MAX_CMDS_COUNT, //
      TSID_DESCRIPTION, STR_D_MAX_CMDS_COUNT, //
      TSID_MIN_INCLUSIVE, avInt( MIN_MAX_CMDS_COUNT ), //
      TSID_MAX_INCLUSIVE, avInt( MAX_MAX_CMDS_COUNT ), //
      TSID_DEFAULT_VALUE, avInt( 1000 ) //
  );

}
