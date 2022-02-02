package org.toxsoft.uskat.core.api.objserv;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoRivetInfo;

/**
 * Encapsulated information about object.
 *
 * @author hazard157
 */
public interface IDtoObject {

  /**
   * Returns the objct SKID.
   *
   * @return {@link Skid} - the objct SKID
   */
  Skid skid();

  /**
   * Returns the attributes values.
   *
   * @return {@link IOptionSet} - the attributes
   */
  IOptionSet attrs();

  /**
   * Returns the riveted objects SKIDs.
   * <p>
   * The map always contains all rivets from {@link ISkClassInfo#rivets()}. All lists in map always contains
   * {@link IDtoRivetInfo#count()} items.
   *
   * @return {@link IStringMap}&lt;{@link ISkidList}&gt; - the map "rivet ID" - "riveted right objects SKIDs list"
   */
  IStringMap<ISkidList> rivets();

}
