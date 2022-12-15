package org.toxsoft.uskat.core.devapi.clobdb;

import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.basis.*;

public interface IClobDbSection
    extends ITsClearable {

  /**
   * Lists IDs of existing CLOBs under the specified {@link IdChain}.
   * <p>
   * If argument is {@link IdChain#NULL} than returns all keys in the section.
   *
   * @param aPrefix {@link IdChain} - the node ID
   * @return {@link IList}&lt; {@link IdChain} &gt; - the IDs list
   */
  IList<IdChain> listChildIds( IdChain aPrefix );

  boolean hasClob( IdChain aId );

  boolean writeClob( IdChain aId, String aValue );

  boolean copyClob( IdChain aSourceId, IdChain aDestId );

  String readClob( IdChain aId );

  boolean removeClob( IdChain aId );

}
