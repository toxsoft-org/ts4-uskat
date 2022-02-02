package org.toxsoft.uskat.core.api.sysdescr.dto;

import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;

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

  /**
   * Returns ths identified parameter definitions.
   *
   * @return {@link IStridablesList}&lt;{@link IDataDef}&gt; - the parameters data defs list
   */
  IStridablesList<IDataDef> paramDefs();

}
