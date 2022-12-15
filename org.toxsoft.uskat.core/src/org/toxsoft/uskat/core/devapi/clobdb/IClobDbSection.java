package org.toxsoft.uskat.core.devapi.clobdb;

import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.coll.*;

public interface IClobDbSection {

  /**
   * Lists IDs of existing CLOBs under the specified
   *
   * @param aPrefix
   * @return
   */
  IList<IdChain> listChildIds( IdChain aPrefix );

  boolean hasClob( IdChain aId );

  boolean writeClob( IdChain aId, String aValue );

  boolean copyClob( IdChain aSourceId, IdChain aDestId );

  String readClob( IdChain aId );

  boolean removeClob( IdChain aId );

}
