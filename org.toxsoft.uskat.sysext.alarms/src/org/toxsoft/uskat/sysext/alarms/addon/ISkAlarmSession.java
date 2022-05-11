package org.toxsoft.uskat.sysext.alarms.addon;

import javax.ejb.Local;

import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonSession;

/**
 * Локальный доступ к расширению backend {@link ISkBackendAddonAlarm}
 *
 * @author mvk
 */
@Local
public interface ISkAlarmSession
    extends ISkBackendAddonAlarm, IS5BackendAddonSession {
  // nop
}
