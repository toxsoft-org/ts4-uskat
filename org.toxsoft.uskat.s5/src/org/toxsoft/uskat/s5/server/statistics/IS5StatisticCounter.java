package org.toxsoft.uskat.s5.server.statistics;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Счетчик статистической информации
 */
public interface IS5StatisticCounter {

  /**
   * Отработать появления события параметра
   *
   * @param aParam {@link IStridable} идентификатор параметра параметра
   * @param aValue {@link IAtomicValue} значение параметра
   * @return boolean <b>true</b> данные параметра изменились;<b>false</b> данные параметра не изменились
   * @throws TsNullArgumentRtException аргумент = 0
   * @throws TsIllegalArgumentRtException значения быть только числового типа {@link EAtomicType#INTEGER},
   *           {@link EAtomicType#FLOATING}
   */
  boolean onEvent( IStridable aParam, IAtomicValue aValue );

  /**
   * Отработать появления события параметра
   *
   * @param aParam String имя параметра
   * @param aValue {@link IAtomicValue} значение параметра
   * @return boolean <b>true</b> данные параметра изменились;<b>false</b> данные параметра не изменились
   * @throws TsNullArgumentRtException аргумент = 0
   * @throws TsIllegalArgumentRtException значения быть только числового типа {@link EAtomicType#INTEGER},
   *           {@link EAtomicType#FLOATING}
   */
  boolean onEvent( String aParam, IAtomicValue aValue );

  /**
   * Отработать появления события параметра
   *
   * @param aInterval {@link IS5StatisticInterval} интервал статистики для которого сформировано событие
   * @param aParam String имя параметра
   * @param aValue {@link IAtomicValue} значение параметра
   * @return boolean <b>true</b> данные параметра изменились;<b>false</b> данные параметра не изменились
   * @throws TsNullArgumentRtException аргумент = 0
   * @throws TsIllegalArgumentRtException значения быть только числового типа {@link EAtomicType#INTEGER},
   *           {@link EAtomicType#FLOATING}
   */
  boolean onEvent( IS5StatisticInterval aInterval, String aParam, IAtomicValue aValue );

  /**
   * Обновление состояние счетчика
   * <p>
   * Предполагается, что метод {@link #update()} будет вызываться периодически с интервалом
   * {@link EStatisticInterval#SECOND}
   *
   * @return boolean <b>true</b> данные были обработаны; <b>false</b> данные не изменились
   */
  boolean update();

  /**
   * Возвращает время последнего обновления данных
   *
   * @return long время (мсек с начала эпохи) обновления данных.
   * @throws TsNullArgumentRtException аргумент = null
   */
  long updateTime();

  /**
   * Привести статистику в начальное состояние
   */
  void reset();
}
