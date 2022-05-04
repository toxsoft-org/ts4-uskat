package org.toxsoft.uskat.s5.legacy;

import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.skid.ISkidList;

/**
 * The map of the links.
 * <p>
 * Keys in map are link IDs, values - linked objects SKIDs.
 * <p>
 * This is read-only interface, implementing class is an editable one.
 *
 * @author hazard157
 */
public interface ISkLinksMap {

  /**
   * Returns the linked objects SKIDs.
   *
   * @return {@link IStringMap}&lt;{@link ISkidList}&gt; - map "link ID" - "SKIDs list"
   */
  IStringMap<ISkidList> map();

}
