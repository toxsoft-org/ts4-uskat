package org.toxsoft.uskat.core.api.sysdescr;

import org.toxsoft.core.tslib.coll.helpers.*;

/**
 * {@link ISkSysdescr} events listener.
 *
 * @author hazard157
 */
public interface ISkSysdescrListener {

  /**
   * This method is called when any changes in classes occur.
   *
   * @param aSource {@link ISkSysdescr} - the message event source
   * @param aOp {@link ECrudOp} - the kind of change
   * @param aClassId String - affected class ID or <code>null</code> for batch changes {@link ECrudOp#LIST}
   */
  void onClassInfosChanged( ISkSysdescr aSource, ECrudOp aOp, String aClassId );

}
