package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

/**
 * Backend addon to wirk with commands.
 * <p>
 * This is the mandatory addon.
 *
 * @author hazard157
 */
public interface IBaCommands
    extends IBackendAddon {

  /**
   * ID of this backend addon.
   */
  String ADDON_ID = SK_ID + "ba.Commands"; //$NON-NLS-1$

}
