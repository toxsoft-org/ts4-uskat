package org.toxsoft.uskat.core.api.rtdserv;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;

/**
 * Listener to current data value(s) change.
 *
 * @author hazard157
 */
public interface ISkCurrDataChangeListener {

  /**
   * Called when current value(s) of RTdata change.
   *
   * @param aNewValues {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; - map "RTdata GWID" - "current value"
   */
  void onCurrData( IMap<Gwid, IAtomicValue> aNewValues );

}
