package org.toxsoft.uskat.core.api.objserv;

import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;

/**
 * Extends information about object with CLOBs and links.
 * <p>
 * Note: fields may contain only part of data about object. For example there may be only part of the attributes or
 * CLOBs of the real object. This is just data container, not an existing object representation. But common practice is
 * to create {@link IDtoFullObject} with all attributes and rivets while CLOBs and links may be left empty.
 *
 * @author hazard157
 */
public interface IDtoFullObject
    extends IDtoObject {

  /**
   * Returns the CLOBs values.
   *
   * @return {@link IStringMap}&lt;String&gt; - the map "CLOB ID" - "CLOB - content"
   */
  IStringMap<String> clobs();

  /**
   * Returns the linked objects SKIDs.
   *
   * @return {@link IMappedSkids} - the map "link ID" - "linked right objects SKIDs list"
   */
  IMappedSkids links();

}
