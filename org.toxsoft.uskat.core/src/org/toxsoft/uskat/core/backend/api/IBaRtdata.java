package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

/**
 * Backend addon for current and historic RTdata.
 * <p>
 * This is the mandatory addon.
 *
 * @author hazard157
 */
public interface IBaRtdata
    extends IBackendAddon {

  /**
   * ID of this backend addon.
   */
  String ADDON_ID = SK_ID + "ba.Rtdata"; //$NON-NLS-1$

}
