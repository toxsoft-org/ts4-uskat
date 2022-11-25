package org.toxsoft.uskat.alarms.s5.addons;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.uskat.alarms.lib.IBaAlarms;
import org.toxsoft.uskat.s5.server.frontend.IS5BackendAddonData;

/**
 * Данные конфигурации фронтенда для {@link IBaAlarms}.
 *
 * @author mvk
 */
public class S5BaAlarmData
    implements IS5BackendAddonData, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Список описаний алармов в которых заинтересован клиент
   */
  public final IListEdit<ITsCombiFilterParams> alarmFilters = new ElemArrayList<>( false ); // aAllowDuplicates = false

}
