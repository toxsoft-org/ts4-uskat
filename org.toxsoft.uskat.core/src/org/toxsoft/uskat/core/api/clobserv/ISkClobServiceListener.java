package org.toxsoft.uskat.core.api.clobserv;

import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.core.*;

/**
 * Listen to the CLOB changes.
 * <p>
 * Please note that listener informs only about CLOB content change. By definition, CLOBs are created and removed as
 * part of the object when object is created or removed. Also note that CLOB content change does not leads to object
 * change event.
 *
 * @author hazard157
 */
public interface ISkClobServiceListener {

  /**
   * Called when the CLOB content changes.
   *
   * @param aCoreApi {@link ISkCoreApi} - the event source
   * @param aClobGwid {@link Gwid} - concrete GWID of kind {@link EGwidKind#GW_CLOB}
   */
  void onClobChanged( ISkCoreApi aCoreApi, Gwid aClobGwid );

}
