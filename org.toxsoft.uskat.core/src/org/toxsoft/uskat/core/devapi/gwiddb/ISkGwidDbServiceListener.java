package org.toxsoft.uskat.core.devapi.gwiddb;

import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.gw.gwid.*;

/**
 * Listener to the changes in {@link ISkGwidDbService}.
 *
 * @author hazard157
 */
public interface ISkGwidDbServiceListener {

  /**
   * Called when any change occurs in database.
   * <p>
   * Note: section creation by {@link ISkGwidDbService#defineSection(IdChain)} does not affects database hence does not
   * fires an event.
   * <p>
   * Note: by definition, argument <code>aKey</code> has value <code>null</code> only when batch changes happens, that
   * is when the change kind is {@link ECrudOp#LIST}. Batch changes happens in following cases:
   * <ul>
   * <li>section was removes by {@link ISkGwidDbService#removeSection(IdChain)};</li>
   * <li>several GWIDs was removed from the system at once.</li>
   * </ul>
   *
   * @param aSource {@link ISkGwidDbService} - the event source
   * @param aSectionId {@link IdChain} - the ID of the section where the change happened
   * @param aOp {@link ECrudOp} - the change kind
   * @param aKey {@link Gwid} - the changed key or <code>null</code>
   */
  void onGwidDbChange( ISkGwidDbService aSource, IdChain aSectionId, ECrudOp aOp, Gwid aKey );

}
