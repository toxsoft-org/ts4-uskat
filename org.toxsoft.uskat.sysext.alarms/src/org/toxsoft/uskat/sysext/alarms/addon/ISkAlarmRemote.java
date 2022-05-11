package org.toxsoft.uskat.sysext.alarms.addon;

import javax.ejb.Remote;

import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonRemote;

/**
 * Удаленный доступ к расширению backend {@link ISkBackendAddonAlarm}
 *
 * @author mvk
 */
@Remote
public interface ISkAlarmRemote
    extends ISkBackendAddonAlarm, IS5BackendAddonRemote {
  // nop
}
