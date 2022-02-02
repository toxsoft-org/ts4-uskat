package org.toxsoft.uskat.core.api.linkserv;

import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;

/**
 * Direct one-to-many relationship from one left object to many right objects.
 *
 * @author hazard157
 */
public interface IDtoLinkFwd {

  /**
   * Returns abstract GWID of this link.
   *
   * @return {@link Gwid} - GWID of this link
   */
  Gwid gwid();

  /**
   * Returns the SKID of the left object.
   *
   * @return {@link Skid} - SKID of the left object
   */
  Skid leftSkid();

  /**
   * Returns the SKIDs of the right (linked) objects.
   *
   * @return {@link ISkidList} - the SKIDs list of the right objects
   */
  ISkidList rightSkids();

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
