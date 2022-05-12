package org.toxsoft.uskat.core.api.sysdescr;

import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.uskat.core.*;

/**
 * {@link ISkSysdescr} events listener.
 *
 * @author hazard157
 */
public interface ISkSysdescrListener {

  /**
   * This method is called when any changes in classes occur.
   *
   * @param aCoreApi {@link ISkCoreApi} - the event source
   * @param aOp {@link ECrudOp} - the kind of change
   * @param aClassId String - affected class ID or <code>null</code> for batch changes {@link ECrudOp#LIST}
   */
  void onClassInfosChanged( ISkCoreApi aCoreApi, ECrudOp aOp, String aClassId );

}
