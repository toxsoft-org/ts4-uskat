package org.toxsoft.uskat.core.gui.valed.std;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.filter.*;
import org.toxsoft.core.tslib.bricks.filter.impl.*;

/**
 * Package-specific constants.
 *
 * @author hazard157
 */
public interface ISkStdValedControlConstants {

  /**
   * ID prefix of options listed below.
   */
  String OPID_PREFIX = SK_ID + ".valed.option.std"; //$NON-NLS-1$

  /**
   * Option: determines tree (<code>true</code>) or list (<code>false</code>) mode at startup.<br>
   * Default value: <code>true</code>
   */
  IDataDef OPDEF_IS_START_MODE_TREE = DataDef.create( OPID_PREFIX + ".IsStartModeTree", BOOLEAN, //$NON-NLS-1$
      TSID_DEFAULT_VALUE, AV_TRUE //
  );

  /**
   * Option: {@link ITsCombiFilterParams} to create class Id filter {@link ITsFilter}&lt;String&gt;.<br>
   * Default value: {@link ITsCombiFilterParams#ALL}
   */
  IDataDef OPDEF_CLASS_ID_FILTER_PARAMS = DataDef.create( OPID_PREFIX + ".ClassIdFilterParams", BOOLEAN, //$NON-NLS-1$
      TSID_KEEPER_ID, TsCombiFilterParamsKeeper.KEEPER_ID, //
      TSID_DEFAULT_VALUE, TsCombiFilterParamsKeeper.AV_ALL //
  );

}
