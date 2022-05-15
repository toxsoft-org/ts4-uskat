package org.toxsoft.uskat.core.api.objserv;

import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.*;

/**
 * Listener to the changes in th objects.
 *
 * @author hazard157
 */
public interface ISkObjectServiceListener {

  /**
   * Called when any change in objects occur.
   *
   * @param aCoreApi {@link ISkCoreApi} - the event source
   * @param aOp {@link ECrudOp} - the kind of change
   * @param aSkid {@link Skid} - affected object SKID or <code>null</code> for batch changes {@link ECrudOp#LIST}
   */
  void onObjectsChanged( ISkCoreApi aCoreApi, ECrudOp aOp, Skid aSkid );

}
