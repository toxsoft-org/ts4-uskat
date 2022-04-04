package org.toxsoft.uskat.core.api.objserv;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.gw.skid.*;

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
   *
   * @return {@link IMappedSkids} - the map "rivet ID" - "riveted right objects SKIDs list"
   */
  IMappedSkids rivets();

  // ------------------------------------------------------------------------------------
  // inline methods for convinience

  @SuppressWarnings( "javadoc" )
  default String classId() {
    return skid().classId();
  }

  @SuppressWarnings( "javadoc" )
  default String strid() {
    return skid().strid();
  }

}
