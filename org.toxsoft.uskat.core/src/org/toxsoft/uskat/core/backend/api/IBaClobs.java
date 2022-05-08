package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Backend addon for CLOBs storage.
 * <p>
 * This is the mandatory addon.
 *
 * @author hazard157
 */
public interface IBaClobs
    extends IBackendAddon {

  /**
   * ID of this backend addon.
   */
  String ADDON_ID = SK_ID + "ba.Clobs"; //$NON-NLS-1$

  /**
   * Reads the CLOB content.
   *
   * @param aGwid {@link Gwid} - concrete GWID of the clob
   * @return String - the CLOB content
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such CLOB
   * @throws TsIllegalStateRtException CLOB is too big for reading
   */
  String readClob( Gwid aGwid );

  /**
   * Writes the CLOB content.
   *
   * @param aGwid {@link Gwid} - concrete GWID of the clob
   * @param aClob String - the CLOB content
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such CLOB
   * @throws TsIllegalArgumentRtException CLOB is too big for reading
   */
  void writeClob( Gwid aGwid, String aClob );

}
