package org.toxsoft.uskat.core.api.sysdescr.dto;

import org.toxsoft.tslib.av.metainfo.IDataDef;
import org.toxsoft.tslib.bricks.strid.coll.IStridablesList;

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

}
