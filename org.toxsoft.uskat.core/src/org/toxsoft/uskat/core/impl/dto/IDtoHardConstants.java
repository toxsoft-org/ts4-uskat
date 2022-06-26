package org.toxsoft.uskat.core.impl.dto;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * Package constants.
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public interface IDtoHardConstants {

  /**
   * {@link DtoClassInfo} keeper keywords.
   */
  String KW_ATTRS  = "attrs";  //$NON-NLS-1$
  String KW_RIVETS = "rivets"; //$NON-NLS-1$
  String KW_LINKS  = "links";  //$NON-NLS-1$
  String KW_CLOBS  = "clobs";  //$NON-NLS-1$
  String KW_RTDATA = "rtdata"; //$NON-NLS-1$
  String KW_CMDS   = "cmds";   //$NON-NLS-1$
  String KW_EVENTS = "events"; //$NON-NLS-1$

  /**
   * Identifier of option {@link #OPDEF_DATA_TYPE}.
   */
  String OPID_DATA_TYPE = SK_ID + ".dto.DataType"; //$NON-NLS-1$

  /**
   * Option: {@link IDtoAttrInfo#dataType()}, {@link IDtoRtdataInfo#dataType()}.
   */
  IDataDef OPDEF_DATA_TYPE = DataDef.create( OPID_DATA_TYPE, VALOBJ, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * Identifier of option {@link #OPDEF_RIGHT_CLASS_IDS}.
   */
  String OPID_RIGHT_CLASS_IDS = SK_ID + ".dto.RightClassIds"; //$NON-NLS-1$

  /**
   * Option: {@link IDtoLinkInfo#rightClassIds()}.
   */
  IDataDef OPDEF_RIGHT_CLASS_IDS = DataDef.create( OPID_RIGHT_CLASS_IDS, VALOBJ, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * Identifier of option {@link #OPDEF_LINK_CONSTRAINT}.
   */
  String OPID_LINK_CONSTRAINT = SK_ID + ".dto.LinkConstraint"; //$NON-NLS-1$

  /**
   * Option: {@link IDtoLinkInfo#linkConstraint()}.
   */
  IDataDef OPDEF_LINK_CONSTRAINT = DataDef.create( OPID_LINK_CONSTRAINT, VALOBJ, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * Identifier of option {@link #OPDEF_MAX_CHARS_COUNT}.
   */
  String OPID_MAX_CHARS_COUNT = SK_ID + ".dto.MaxCharsCount"; //$NON-NLS-1$

  /**
   * Option: {@link IDtoAttrInfo#dataType()}.
   */
  IDataDef OPDEF_MAX_CHARS_COUNT = DataDef.create( OPID_MAX_CHARS_COUNT, INTEGER, //
      TSID_MIN_INCLUSIVE, AV_0, //
      TSID_DEFAULT_VALUE, AV_0, //
      TSID_IS_MANDATORY, AV_FALSE //
  );

  /**
   * Identifier of option {@link #OPDEF_IS_CURR}.
   */
  String OPID_IS_CURR = SK_ID + ".dto.isCurr"; //$NON-NLS-1$

  /**
   * Option: {@link IDtoRtdataInfo#isCurr()}.
   */
  IDataDef OPDEF_IS_CURR = DataDef.create( OPID_IS_CURR, BOOLEAN, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * Identifier of option {@link #OPDEF_IS_HIST}.
   */
  String OPID_IS_HIST = SK_ID + ".dto.isHist"; //$NON-NLS-1$

  /**
   * Option: {@link IDtoRtdataInfo#isHist()}, {@link IDtoEventInfo#isHist()}.
   */
  IDataDef OPDEF_IS_HIST = DataDef.create( OPID_IS_HIST, BOOLEAN, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * Identifier of option {@link #OPDEF_IS_SYNC}.
   */
  String OPID_IS_SYNC = SK_ID + ".dto.isSync"; //$NON-NLS-1$

  /**
   * Option: {@link IDtoRtdataInfo#isSync()}.
   */
  IDataDef OPDEF_IS_SYNC = DataDef.create( OPID_IS_SYNC, BOOLEAN, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * Identifier of option {@link #OPDEF_SYNC_DATA_DELTA_T}.
   */
  String OPID_SYNC_DATA_DELTA_T = SK_ID + ".dto.syncDataDeltaT"; //$NON-NLS-1$

  /**
   * Option: {@link IDtoRtdataInfo#syncDataDeltaT()}.
   */
  IDataDef OPDEF_SYNC_DATA_DELTA_T = DataDef.create( OPID_SYNC_DATA_DELTA_T, INTEGER, //
      TSID_MIN_INCLUSIVE, AV_1, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * Identifier of option {@link #OPDEF_ARG_DEFS}.
   */
  String OPID_ARG_DEFS = SK_ID + ".dto.argDefs"; //$NON-NLS-1$

  /**
   * Option: {@link IDtoCmdInfo#argDefs()}.<br>
   * Note: stores {@link IStridablesList}&lt;{@link IDataDef}&gt; via
   * {@link StrioUtils#readStridablesList(IStrioReader, IEntityKeeper)}.
   */
  IDataDef OPDEF_ARG_DEFS = DataDef.create( OPID_ARG_DEFS, STRING, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * Identifier of option {@link #OPDEF_PARAM_DEFS}.
   */
  String OPID_PARAM_DEFS = SK_ID + ".dto.paramDefs"; //$NON-NLS-1$

  /**
   * Option: {@link IDtoEventInfo#paramDefs()}.<br>
   * Note: stores {@link IStridablesList}&lt;{@link IDataDef}&gt; via
   * {@link StrioUtils#readStridablesList(IStrioReader, IEntityKeeper)}.
   */
  IDataDef OPDEF_PARAM_DEFS = DataDef.create( OPID_PARAM_DEFS, STRING, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * Identifier of option {@link #OPDEF_RIGHT_CLASS_ID}.
   */
  String OPID_RIGHT_CLASS_ID = SK_ID + ".dto.rightClassId"; //$NON-NLS-1$

  /**
   * Option: {@link IDtoRivetInfo#rightClassId()}.
   */
  IDataDef OPDEF_RIGHT_CLASS_ID = DataDef.create( OPID_RIGHT_CLASS_ID, STRING, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

  /**
   * Identifier of option {@link #OPDEF_COUNT}.
   */
  String OPID_COUNT = SK_ID + ".dto.count"; //$NON-NLS-1$

  /**
   * Option: {@link IDtoRivetInfo#count()}.
   */
  IDataDef OPDEF_COUNT = DataDef.create( OPID_COUNT, INTEGER, //
      TSID_MIN_INCLUSIVE, AV_1, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL, //
      TSID_IS_MANDATORY, AV_TRUE //
  );

}
