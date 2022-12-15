package org.toxsoft.uskat.regref.lib;

import org.toxsoft.core.tslib.coll.helpers.ECrudOp;

/**
 * {@link ISkRegRefInfoService} changes listener.
 *
 * @author goga
 */
public interface ISkRegRefInfoServiceListener {

  /**
   * Informs about changes in sections list (not changed in infos).
   *
   * @param aOp {@link ECrudOp} - the kind of changes
   * @param aSectionId String - identifier of the changed section or <code>null</code> on batch changes
   */
  void onSectionsChanged( ECrudOp aOp, String aSectionId );

}
