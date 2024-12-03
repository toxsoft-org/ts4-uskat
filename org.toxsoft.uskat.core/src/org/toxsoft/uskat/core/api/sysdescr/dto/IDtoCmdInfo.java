package org.toxsoft.uskat.core.api.sysdescr.dto;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;

/**
 * Information about command property of class.
 *
 * @author hazard157
 */
public interface IDtoCmdInfo
    extends IDtoClassPropInfoBase {

  /**
   * Returns the identified argument definitions.
   *
   * @return {@link IStridablesList}&lt;{@link IDataDef}&gt; - the arguments data defs list
   */
  IStridablesList<IDataDef> argDefs();

  // FIXME use format string like ISkMessageInfo for command human-readable string creation
  // String fmtStr();

}
