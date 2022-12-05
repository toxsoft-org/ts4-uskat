package org.toxsoft.uskat.alarms.s5;

import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;
import org.toxsoft.uskat.alarms.lib.impl.*;

/**
 * Регистрация хранителей данных подсистемы
 *
 * @author mvk
 */
public class S5AlarmValobjUtils {

  /**
   * Регистрация известных хранителей
   */
  public static void registerS5Keepers() {
    TsValobjUtils.registerKeeperIfNone( SkAlarm.KEEPER_ID, SkAlarm.KEEPER );
    TsValobjUtils.registerKeeperIfNone( SkAlarmFlacon.KEEPER_ID, SkAlarmFlacon.KEEPER );
    TsValobjUtils.registerKeeperIfNone( SkAlarmThreadHistoryItem.KEEPER_ID, SkAlarmThreadHistoryItem.KEEPER );
  }
}
