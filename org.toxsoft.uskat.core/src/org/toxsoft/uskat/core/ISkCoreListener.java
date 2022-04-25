package org.toxsoft.uskat.core;

import org.toxsoft.core.tslib.coll.*;

/**
 * Listener to changes in USkat core.
 *
 * @author hazard157
 */
public interface ISkCoreListener {

  /**
   * Called when something changes in green world.
   * <p>
   * TODO notes on how list is formed
   *
   * @param aEvents {@link IList}&lt;{@link SkCoreEvent}&gt; - list of events reverse-ordered by time (last is first)
   */
  void onGreenWorldChanged( IList<SkCoreEvent> aEvents );

}
