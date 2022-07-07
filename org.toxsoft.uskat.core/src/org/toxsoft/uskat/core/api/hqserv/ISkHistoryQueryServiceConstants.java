package org.toxsoft.uskat.core.api.hqserv;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.api.hqserv.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.gw.gwid.*;

/**
 * {@link ISkQueryRawHistory} constants.
 * <p>
 * Listed options are used as arguments of query instance creation.
 *
 * @author mvk
 */
public interface ISkHistoryQueryServiceConstants {

  /**
   * IDs prefix.
   */
  String HQ_ID = SK_ID + ".hdquery"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Builtin data processing functions
  //

  /**
   * Mandatory argument of all aggregating functions: duration of the aggregation time interval.<br>
   * Type: {@link EAtomicType#INTEGER}<br>
   * Usage: duration is specified in milliseconds. Aggregating functions creates single value for each aggregation time
   * interval. Value 0 means sinlge aggregation interval of whole queried time interval. Please note, that aggregation
   * intervals by default starts at time 0 millisecond at epoch start (01.01.1970 00:00:00.000) unless argiment
   * {@link #HQFUNC_ARG_AGGREGAION_START} is specified.<br>
   * Default value: no default for mandatory argument
   */
  String HQFUNC_ARG_AGGREGAION_INTERVAL = "AggregationIntervalMsecs"; //$NON-NLS-1$

  /**
   * Optional argument of all aggregating functions: starting point of aggregation time intervals.<br>
   * Type: {@link EAtomicType#INTEGER}<br>
   * Usage: specifies the reference point (starting timestamp in milliseconds) for the origin of uniform aggregation
   * time intervals.<br>
   * Default value: 0 (epoch start at 01.01.1970 00:00:00.000)
   */
  String HQFUNC_ARG_AGGREGAION_START = "AggregationIntervalsStart"; //$NON-NLS-1$

  /**
   * Aggragating function returns single minimal value for each aggregation interval.<br>
   * Applicable to the {@link EGwidKind#GW_RTDATA} of type {@link EAtomicType#INTEGER INTEGER} or
   * {@link EAtomicType#FLOATING}.
   */
  String HQFUNC_ID_MIN = HQ_ID + ".Min"; //$NON-NLS-1$

  /**
   * Aggragating function returns single minimal value for each aggregation interval. <br>
   * Applicable to the {@link EGwidKind#GW_RTDATA} of type {@link EAtomicType#INTEGER INTEGER} or
   * {@link EAtomicType#FLOATING}.
   */
  String HQFUNC_ID_MAX = HQ_ID + ".Max"; //$NON-NLS-1$

  /**
   * Aggragating function returns single arithmetical average value for each aggregation interval. <br>
   * Applicable to the {@link EGwidKind#GW_RTDATA} of type {@link EAtomicType#INTEGER INTEGER} or
   * {@link EAtomicType#FLOATING}.
   */
  String HQFUNC_ID_AVERAGE = HQ_ID + ".Average"; //$NON-NLS-1$

  /**
   * Aggragating function returns single sum of all values for each aggregation interval. <br>
   * Applicable to the {@link EGwidKind#GW_RTDATA} of type {@link EAtomicType#INTEGER INTEGER} or
   * {@link EAtomicType#FLOATING}.
   */
  String HQFUNC_ID_SUM = HQ_ID + ".Sum"; //$NON-NLS-1$

  /**
   * Aggragating function returns single arithmetical average value for each aggregation interval. <br>
   * Applicable to the {@link EGwidKind#GW_RTDATA} of type {@link EAtomicType#INTEGER INTEGER} or
   * {@link EAtomicType#FLOATING}.
   */
  String HQFUNC_ID_COUNT = HQ_ID + ".Count"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Query creation options
  //

  /**
   * Maximal alloed time of query executions in milliseconds.
   * <p>
   * After specified time query will be cancelled.
   * <p>
   * Type: {@link EAtomicType#INTEGER}
   * <p>
   * Default value: <code>10000</code> (10 seconds)
   */
  IDataDef OP_SK_MAX_EXECUTION_TIME = create( HQ_ID + ".maxExecutionTime", INTEGER, //$NON-NLS-1$
      TSID_NAME, STR_N_MAX_EXECUTION_TIME, //
      TSID_DESCRIPTION, STR_D_MAX_EXECUTION_TIME, //
      TSID_DEFAULT_VALUE, avInt( 10 * 1000 ) //
  );

}
