package org.toxsoft.uskat.core.api.hqserv;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.api.hqserv.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;

/**
 * {@link ISkHistoryQuery} constants.
 * <p>
 * Listed options are used as arguments of query instance creation.
 *
 * @author mvk
 */
public interface ISkQueryServiceConstants {

  // ------------------------------------------------------------------------------------
  // Опции
  //

  /**
   * IDs prefix.
   */
  String HQ_ID = SK_ID + ".hdquery"; //$NON-NLS-1$

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
