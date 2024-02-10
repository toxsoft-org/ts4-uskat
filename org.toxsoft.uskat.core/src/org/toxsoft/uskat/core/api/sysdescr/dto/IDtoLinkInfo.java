package org.toxsoft.uskat.core.api.sysdescr.dto;

import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.primtypes.*;

/**
 * Information about link property of class.
 *
 * @author hazard157
 */
public interface IDtoLinkInfo
    extends IDtoClassPropInfoBase {

  /**
   * Returns the classes allowed as the objects at the right of the link.
   *
   * @return {@link IStringList} - identifiers list of the classes at right
   */
  IStringList rightClassIds();

  /**
   * Returns the constraint on linked objects.
   *
   * @return {@link CollConstraint} - the constraint
   */
  CollConstraint linkConstraint();

}
