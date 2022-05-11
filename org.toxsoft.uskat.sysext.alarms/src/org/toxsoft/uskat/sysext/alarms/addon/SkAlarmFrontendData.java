package org.toxsoft.uskat.sysext.alarms.addon;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendAddonData;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;

/**
 * Данные конфигурации frontend расширения backend {@link SkAlarmAddon}.
 *
 * @author mvk
 */
public class SkAlarmFrontendData
    implements IS5FrontendAddonData, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Список описаний фильтров тревог которые передаются фронтенду
   */
  public final IList<ITsCombiFilterParams> alarmFilterParams = new ElemArrayList<>();

  /**
   * Возвращает данные фронтенда "тревоги"
   *
   * @param aFrontend {@link IS5FrontendRear} фронтенд
   * @return {@link SkAlarmFrontendData} данные фронтенда. null: данные не существуют
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static SkAlarmFrontendData getFromFrontend( IS5FrontendRear aFrontend ) {
    return aFrontend.frontendData().getAddonData( ISkBackendAddonAlarm.S5_BACKEND_ALARMS_ID,
        SkAlarmFrontendData.class );
  }

}
