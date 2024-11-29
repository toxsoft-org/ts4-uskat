package org.toxsoft.uskat.s5.cron.lib;

import org.toxsoft.uskat.core.backend.api.IBackendAddon;

/**
 * backend службы {@link ISkCronService}.
 *
 * @author mvk
 */
public interface IBaCrone
    extends IBackendAddon {

  /**
   * ID of this backend addon.
   */
  String ADDON_ID = ISkCronHardConstants.BAID_CRON;

}
