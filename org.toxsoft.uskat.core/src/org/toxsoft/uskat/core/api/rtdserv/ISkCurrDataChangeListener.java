package org.toxsoft.uskat.core.api.rtdserv;

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
   * @param aRtdMap {@link IMap}&lt;{@link Gwid}, {@link ISkReadCurrDataChannel} &gt; - channels with changed values
   */
  void onCurrData( IMap<Gwid, ISkReadCurrDataChannel> aRtdMap );

}
