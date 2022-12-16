package org.toxsoft.uskat.legacy;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.impl.SingleStringList;
import org.toxsoft.core.tslib.gw.IGwHardConstants;

/**
 * TODO: from {@link org.toxsoft.uskat.core.ISkHardConstants} of ts3
 *
 * @author mvk
 */
public interface ISkHardConstants {

  // ------------------------------------------------------------------------------------
  // helpers
  //
  /**
   * Constant: immutable list of class IDs containing the one element {@link IGwHardConstants#GW_ROOT_CLASS_ID}.
   * <p>
   * Simplifies link definitions.
   */
  IStringList SL_SKOBJ = new SingleStringList( IGwHardConstants.GW_ROOT_CLASS_ID );

  /**
   * Atomic value correspnding to the {@link #SL_SKOBJ} to be used as keepable option values.
   */
  IAtomicValue AV_SL_SKOBJ = AvUtils.avValobj( SL_SKOBJ );

}
