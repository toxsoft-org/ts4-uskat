package org.toxsoft.uskat.s5.server.statistics;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;

/**
 * Константы подсистемы
 *
 * @author mvk
 */
public interface IS5StatisticHardConstants {

  /**
   * Идентификатор опции: атомарный тип значений параметра
   * <p>
   * Тип: {@link EAtomicType#VALOBJ} содержащий {@link EAtomicType}.
   */
  String S5ID_STATISTIC_TYPE = IS5ServerHardConstants.S5_ID_START + "StatisticType"; //$NON-NLS-1$

  /**
   * Идентификатор опции: тип статистической функции
   * <p>
   * Тип: {@link EAtomicType#VALOBJ} содержащий {@link EStatisticFunc}.
   */
  String S5ID_STATISTIC_FUNC = IS5ServerHardConstants.S5_ID_START + "StatisticFunc"; //$NON-NLS-1$

  /**
   * Идентификатор опции: список интервалов статистической обработки
   * <p>
   * Тип: {@link EAtomicType#VALOBJ} содержащий {@link IStridablesList}&lt;{@link IS5StatisticInterval}&gt;.
   */
  String S5ID_STATISTIC_INTERVALS = IS5ServerHardConstants.S5_ID_START + "StatisticIntevals"; //$NON-NLS-1$

}
