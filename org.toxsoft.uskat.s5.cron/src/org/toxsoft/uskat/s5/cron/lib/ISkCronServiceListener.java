package org.toxsoft.uskat.s5.cron.lib;

import org.toxsoft.core.tslib.gw.skid.ISkidList;

/**
 * Слушатель службы {@link ISkCronService}.
 *
 * @author mvk
 */
public interface ISkCronServiceListener {

  /**
   * Вызывается при возникновении события "наступило время по расписанию".
   *
   * @param aScheduleIds {@link ISkidList} список идентификаторов расписаний (объекты класса
   *          {@link ISkCronHardConstants#CLSID_SCHEDULE}) у которых возникло событие.
   */
  void onScheduled( ISkidList aScheduleIds );

}
