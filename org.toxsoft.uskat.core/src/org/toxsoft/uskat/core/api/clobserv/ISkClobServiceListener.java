package org.toxsoft.uskat.core.api.clobserv;

import org.toxsoft.core.tslib.gw.gwid.*;

/**
 * CLOBs listener.
 *
 * @author hazard157
 */
public interface ISkClobServiceListener {

  /**
   * Called when CLOB contents changed.
   *
   * @param aClobGwid {@link Gwid} - concrete {@link EGwidKind#GW_CLOB} identifier of changed CLOB
   */
  void onSkClobChanged( Gwid aClobGwid );

}
