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

  // ------------------------------------------------------------------------------------
  // Common for all addons

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

  // ------------------------------------------------------------------------------------
  // IBaEvents addon

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

  // ------------------------------------------------------------------------------------
  // IBaCommands addon

  /**
   * Backend arg: determines if commands history is stored permamnently.
   */
  IDataDef OPDEF_IS_CMDS_STORED = DataDef.create( SKB_ID_MEMTEXT + ".IsCommandsStored", BOOLEAN, //$NON-NLS-1$
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
   * Backend arg: maximum number of commands in history.
   */
  IDataDef OPDEF_MAX_CMDS_COUNT = DataDef.create( SKB_ID_MEMTEXT + ".MaxCommandsCount", INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_MAX_CMDS_COUNT, //
      TSID_DESCRIPTION, STR_D_MAX_CMDS_COUNT, //
      TSID_MIN_INCLUSIVE, avInt( MIN_MAX_CMDS_COUNT ), //
      TSID_MAX_INCLUSIVE, avInt( MAX_MAX_CMDS_COUNT ), //
      TSID_DEFAULT_VALUE, avInt( 1000 ) //
  );

  // ------------------------------------------------------------------------------------
  // IBaRtdata addon

  /**
   * Min acceptable value of option {@link #OPDEF_HISTORY_DEPTH_HOURS}.
   */
  int MIN_HISTORY_DEPTH_HOURS = 1;

  /**
   * Max acceptable value of option {@link #OPDEF_HISTORY_DEPTH_HOURS}.
   */
  int MAX_HISTORY_DEPTH_HOURS = 24 * 30; // 1 month

  /**
   * Backend arg: RTdata history will be kept for specified number of hours.
   */
  IDataDef OPDEF_HISTORY_DEPTH_HOURS = DataDef.create( SKB_ID_MEMTEXT + ".HistoryDepthHours", INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_HISTORY_DEPTH_HOURS, //
      TSID_DESCRIPTION, STR_D_HISTORY_DEPTH_HOURS, //
      TSID_MIN_INCLUSIVE, avInt( MIN_HISTORY_DEPTH_HOURS ), //
      TSID_MAX_INCLUSIVE, avInt( MAX_HISTORY_DEPTH_HOURS ), //
      TSID_DEFAULT_VALUE, avInt( 24 ) // 1 day
  );

  /**
   * Min acceptable value of option {@link #OPDEF_CURR_DATA_10MS_TICKS}.
   */
  int MIN_CURR_DATA_10MS_TICKS = 5; // 50 msec

  /**
   * Max acceptable value of option {@link #OPDEF_CURR_DATA_10MS_TICKS}.
   */
  int MAX_CURR_DATA_10MS_TICKS = 100; // 1 sec

  /**
   * Backend arg: Current data will be checked and updated every specified ticks of 10 msec.
   */
  IDataDef OPDEF_CURR_DATA_10MS_TICKS = DataDef.create( SKB_ID_MEMTEXT + "CurrDataCheck10MsTicks", INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_CURR_DATA_10MS_TICKS, //
      TSID_DESCRIPTION, STR_D_CURR_DATA_10MS_TICKS, //
      TSID_MIN_INCLUSIVE, avInt( MIN_CURR_DATA_10MS_TICKS ), //
      TSID_MAX_INCLUSIVE, avInt( MAX_CURR_DATA_10MS_TICKS ), //
      TSID_DEFAULT_VALUE, avInt( 25 ) // 250 msec means 4 times per second
  );

}
