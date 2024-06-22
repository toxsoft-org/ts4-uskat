package org.toxsoft.uskat.s5.schedules.lib;

import org.toxsoft.core.tslib.gw.skid.ISkidList;

/**
 * Слушатель службы {@link ISkScheduleService}.
 *
 * @author mvk
 */
public interface ISkScheduleServiceListener {

  /**
   * Вызывается при возникновении события "наступило время по расписанию".
   *
   * @param aScheduleIds {@link ISkidList} список идентификаторов расписаний (объекты класса
   *          {@link ISkSchedulesHardConstants#CLSID_SCHEDULE}) у которых возникло событие.
   */
  void onScheduled( ISkidList aScheduleIds );

}
