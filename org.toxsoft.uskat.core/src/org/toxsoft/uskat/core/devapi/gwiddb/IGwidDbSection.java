package org.toxsoft.uskat.core.devapi.gwiddb;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.basis.*;
import org.toxsoft.core.tslib.gw.gwid.*;

/**
 * This is simple key-value database.
 * <p>
 * Keys are the GWIDs or existing entities. Values are the String CLOBs. If some entitiy is removed from the system
 * corresponding value will be removed from the database.
 *
 * @author hazard157
 */
public interface IGwidDbSection
    extends ITsClearable {

  /**
   * Lists keys of existing values in database.
   *
   * @return {@link IList}&lt; {@link Gwid} &gt; - the keys list
   */
  IList<Gwid> listKeys();

  boolean hasClob( Gwid aKey );

  boolean writeClob( Gwid aKey, String aValue );

  boolean copyClob( Gwid aSourceId, Gwid aDestId );

  String readClob( Gwid aKey );

  boolean removeClob( Gwid aKey );

}
