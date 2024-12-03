package org.toxsoft.uskat.core.api.sysdescr.dto;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;

/**
 * Information about event property of class.
 *
 * @author hazard157
 */
public interface IDtoEventInfo
    extends IDtoClassPropInfoBase {

  /**
   * Determines if the history of this event will be stored.
   *
   * @return boolean - historical event flag
   */
  boolean isHist();

  // FIXME use format string like ISkMessageInfo for event human-readable string creation
  // String fmtStr();

  /**
   * Returns the event parameter definitions.
   *
   * @return {@link IStridablesList}&lt;{@link IDataDef}&gt; - the parameters data definitions list
   */
  IStridablesList<IDataDef> paramDefs();

}
