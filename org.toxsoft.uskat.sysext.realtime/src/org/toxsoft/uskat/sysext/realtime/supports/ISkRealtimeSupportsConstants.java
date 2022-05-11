package org.toxsoft.uskat.sysext.realtime.supports;

import org.toxsoft.uskat.sysext.realtime.supports.commands.impl.S5BackendCommandSingleton;
import org.toxsoft.uskat.sysext.realtime.supports.currdata.S5BackendCurrDataSingleton;

/**
 * Константы реализации поддержки аддона реального времени
 *
 * @author mvk
 */
public interface ISkRealtimeSupportsConstants {

  /**
   * 12. Синглетон поддержки доступа к командам
   */
  String BACKEND_COMMANDS_SINGLETON = S5BackendCommandSingleton.BACKEND_COMMANDS_ID;

  /**
   * 13. Синглетон поддержки доступа к данным реального времени
   */
  String BACKEND_RTDATA_SINGLETON = S5BackendCurrDataSingleton.BACKEND_CURRDATA_ID;

}
