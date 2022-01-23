package org.toxsoft.uskat.core.api.objserv;

import org.toxsoft.tslib.coll.helpers.ECrudOp;
import org.toxsoft.tslib.gw.skid.Skid;
import org.toxsoft.uskat.core.ISkCoreApi;

/**
 * {@link ISkObjectService} service listener.
 *
 * @author goga
 */
public interface ISkObjectServiceListener {

  /**
   * Called then there is any change to the objects.
   *
   * @param aCoreApi {@link ISkCoreApi} - source CoreAPI
   * @param aOp {@link ECrudOp} - the kind of change
   * @param aSkid {@link Skid} - ID of modified object or <code>null</code> for batch changes
   */
  void onObjectsChanged( ISkCoreApi aCoreApi, ECrudOp aOp, Skid aSkid );

}
