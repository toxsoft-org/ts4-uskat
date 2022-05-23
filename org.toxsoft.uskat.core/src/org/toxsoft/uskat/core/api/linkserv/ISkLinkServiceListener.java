package org.toxsoft.uskat.core.api.linkserv;

import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.*;

/**
 * {@link ISkLinkService} events listener.
 *
 * @author hazard157
 */
public interface ISkLinkServiceListener {

  /**
   * Called when any change in link occur.
   *
   * @param aCoreApi {@link ISkCoreApi} - the event source
   * @param aChangedLinks {@link IStringMap}&lt;{@link Skid}&gt; - the map "link ID" - "left object SKID"
   */
  void onLinkChanged( ISkCoreApi aCoreApi, IStringMap<Skid> aChangedLinks );

}
