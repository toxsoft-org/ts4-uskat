package org.toxsoft.uskat.s5.server.statistics;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;

/**
 * Статистическая информация о работе компонентов системы
 * <p>
 * TODO: добавить статистику по работе отдельных службы
 *
 * @author mvk
 */
public interface IS5Statistic {

  /**
   * Несуществующая статистика
   */
  IS5Statistic NULL = new S5NullStatistic();

  // ------------------------------------------------------------------------------------
  // Общая информация об обмене данными между сервером и клиентом
  //
  /**
   * Возвращает список интервалов значения которых есть в статитистике
   * <p>
   * Метод возвращает все интервалы которые представлены в статистике, но отдельные параметры могут не иметь значений за
   * какой-либой интервал. Поэтому при получении набора значений ({@link #params(IS5StatisticInterval)}) необходимо
   * проверять существует ли в наборе значение требуемого параметра ({@link IOptionSet#hasValue(String)}).
   *
   * @return {@link IStridablesList}&lt;{@link IS5StatisticInterval}&gt;
   */
  IStridablesList<IS5StatisticInterval> intervals();

  /**
   * Возвращает параметры статистики по указанному интервалу
   * <p>
   * Пример параметров статистики:
   * <ul>
   * <li>{@link IS5ServerHardConstants#STAT_SESSION_SENDED};</li>
   * <li>{@link IS5ServerHardConstants#STAT_SESSION_RECEVIED};</li>
   * <li>{@link IS5ServerHardConstants#STAT_SESSION_ERRORS};</li>
   * <li>поиск по {@link #params(IS5StatisticInterval)}.</li>
   * </ul>
   *
   * @param aInterval {@link IS5StatisticInterval } интервал запрашиваемых значений параметров
   * @return {@link IOptionSet} параметры
   */
  IOptionSet params( IS5StatisticInterval aInterval );

}

class S5NullStatistic
    implements IS5Statistic, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Метод корректно восстанавливает сериализированный {@link IS5Statistic#NULL}.
   *
   * @return Object объект {@link IS5Statistic#NULL}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return IS5Statistic.NULL;
  }

  @Override
  public IStridablesList<IS5StatisticInterval> intervals() {
    return IStridablesList.EMPTY;
  }

  @Override
  public IOptionSet params( IS5StatisticInterval aInterval ) {
    return IOptionSet.NULL;
  }
}
