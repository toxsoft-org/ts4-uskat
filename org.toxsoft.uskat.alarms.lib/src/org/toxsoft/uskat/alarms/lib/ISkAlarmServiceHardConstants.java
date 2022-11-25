package org.toxsoft.uskat.alarms.lib;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.alarms.lib.ISkResources.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.impl.Stridable;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;

/**
 * Unchangeable constants of the alarm service.
 *
 * @author mvk
 */
@SuppressWarnings( { "nls", "javadoc" } )
public interface ISkAlarmServiceHardConstants {

  // ------------------------------------------------------------------------------------
  // IBaAlarms
  //
  String BAID_ALARMS = ISkBackendHardConstant.SKB_ID + ".Alarms";

  IStridable BAINF_ALARMS = new Stridable( BAID_ALARMS, STR_N_BA_ALARMS, STR_D_BA_ALARMS );

  // ------------------------------------------------------------------------------------
  // Alarms history params definitions
  //
  /**
   * TODO:
   * <p>
   * Тип: {@link EAtomicType#BOOLEAN}
   */
  IDataDef BOOL_PARAM = create( "BoolHistoryParam", EAtomicType.BOOLEAN, //$NON-NLS-1$
      TSID_NAME, STR_N_BOOL_PARAM, //
      TSID_DESCRIPTION, STR_D_BOOL_PARAM, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_FALSE );

  /**
   * TODO:
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   */
  IDataDef INTEGER_PARAM = create( "IntHistoryParam", EAtomicType.INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_INT_PARAM, //
      TSID_DESCRIPTION, STR_D_INT_PARAM, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_0 );

  /**
   * TODO:
   * <p>
   * Тип: {@link EAtomicType#STRING}
   */
  IDataDef STR_PARAM = create( "AlarmHistMsg", EAtomicType.STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_MESSAGE, //
      TSID_DESCRIPTION, STR_D_MESSAGE, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, avStr( "SkAlarm history message" ) );
}
