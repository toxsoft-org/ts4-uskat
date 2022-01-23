package org.toxsoft.uskat.core.api.sysdescr;

import org.toxsoft.tslib.coll.helpers.ECrudOp;
import org.toxsoft.uskat.core.ISkCoreApi;

/**
 * {@link ISkSysdescr} service listener.
 *
 * @author hazard157
 */
public interface ISkSysdescrListener {

  /**
   * Called then there is any change to the classes.
   *
   * @param aCoreApi {@link ISkCoreApi} - source CoreAPI
   * @param aOp {@link ECrudOp} - the kind of change
   * @param aClassId String - ID of modified class or <code>null</code> for batch changes
   */
  void onClassInfosChanged( ISkCoreApi aCoreApi, ECrudOp aOp, String aClassId );

}
