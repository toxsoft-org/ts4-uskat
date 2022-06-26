package org.toxsoft.uskat.core.incub;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.incub.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.gw.gwid.*;

/**
 * Константы службы {@link ISkHistoryQuery}
 *
 * @author mvk
 */
public interface ISkHistoryQueryConstants {

  // ------------------------------------------------------------------------------------
  // Опции
  //

  /**
   * IDs prefix.
   */
  String HQ_ID = SK_ID + ".hdquery"; //$NON-NLS-1$

  /**
   * Параметр {@link ISkRtDataService#createQuery(IOptionSet)}: требование выбрасывать исключения для неправильных
   * {@link Gwid}.
   * <p>
   * Тип: {@link EAtomicType#BOOLEAN}
   * <p>
   * {@link Gwid} считается неправильным если он не является конкретным {@link Gwid} вида {@link EGwidKind#GW_RTDATA}
   * или {@link ISkGwidManager#isExisting(Gwid)} возвращает <code>false</code>. *
   * <p>
   * По умолчанию: <code>false</code>
   */
  IDataDef OP_SK_CHECK_VALID_GWIDS = create( HQ_ID + ".checkValidGwids", //$NON-NLS-1$
      BOOLEAN, //
      TSID_NAME, STR_N_CHECK_VALID_GWIDS, //
      TSID_DESCRIPTION, STR_D_CHECK_VALID_GWIDS, //
      TSID_DEFAULT_VALUE, AV_FALSE //
  );

  /**
   * Параметр {@link ISkRtDataService#createQuery(IOptionSet)}: максимальное время выполнения запроса (мсек).
   * <p>
   * Тип: {@link EAtomicType#INTEGER}
   * <p>
   * По умолчанию: <code>10000</code> (10 секунд)
   */
  IDataDef OP_SK_MAX_EXECUTION_TIME = create( HQ_ID + ".maxExecutionTime", //$NON-NLS-1$
      INTEGER, //
      TSID_NAME, STR_N_MAX_EXECUTION_TIME, //
      TSID_DESCRIPTION, STR_D_MAX_EXECUTION_TIME, //
      TSID_DEFAULT_VALUE, avInt( 10 * 1000 ) //
  );

}
