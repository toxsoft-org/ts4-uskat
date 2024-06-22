package org.toxsoft.uskat.s5.schedules.lib;

import org.toxsoft.uskat.core.backend.api.IBackendAddon;

/**
 * backend службы {@link ISkScheduleService}.
 *
 * @author mvk
 */
public interface IBaSchedules
    extends IBackendAddon {

  /**
   * ID of this backend addon.
   */
  String ADDON_ID = ISkSchedulesHardConstants.BAID_SCHEDULES;

}
