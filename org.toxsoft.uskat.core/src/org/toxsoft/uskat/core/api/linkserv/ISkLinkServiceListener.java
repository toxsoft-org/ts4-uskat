package org.toxsoft.uskat.core.api.linkserv;

import org.toxsoft.core.tslib.gw.gwid.*;
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
   * @param aChangedLinks {@link IGwidList}&gt; - list of the concrete GWIDs of the changed links
   */
  void onLinkChanged( ISkCoreApi aCoreApi, IGwidList aChangedLinks );

}
