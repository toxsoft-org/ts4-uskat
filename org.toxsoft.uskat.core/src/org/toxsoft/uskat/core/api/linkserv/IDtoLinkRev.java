package org.toxsoft.uskat.core.api.linkserv;

import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;

/**
 * Many-to-one reverse link - from many left objects to the one right object.
 *
 * @author hazard157
 */
public interface IDtoLinkRev {

  /**
   * Returns abstract GWID of this link.
   *
   * @return {@link Gwid} - GWID of this link
   */
  Gwid gwid();

  /**
   * Returns the SKID of the right object.
   *
   * @return {@link Skid} - SKID of the right object
   */
  Skid rightSkid();

  /**
   * Returns the SKIDs of the left objects.
   *
   * @return {@link ISkidList} - the SKIDs list of the left objects
   */
  ISkidList leftSkids();

  // ------------------------------------------------------------------------------------
  // Convinience inline methods

  /**
   * Returns link declaring class ID.
   *
   * @return String - declaring class ID
   */
  default String classId() {
    return gwid().classId();
  }

  /**
   * Returns the link ID.
   *
   * @return String - the link ID
   */
  default String linkId() {
    return gwid().propId();
  }

}
