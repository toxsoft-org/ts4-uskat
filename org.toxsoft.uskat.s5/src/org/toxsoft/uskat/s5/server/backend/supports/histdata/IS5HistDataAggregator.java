package org.toxsoft.uskat.s5.server.backend.supports.histdata;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.sequences.ITemporalValueImporter;

/**
 * Агрегатор значений хранимых данных
 *
 * @author mvk
 */
public interface IS5HistDataAggregator {

  /**
   * Ворзвращает интервал агрегации значений.
   *
   * @return интервал (мсек) агрегации значений. 0: интервал установлен от метки начала до метки завершения запроса
   *         включительно.
   */
  long aggregationStep();

  /**
   * Возвращает следующее агрегированное значение
   *
   * @param aImporter {@link ITemporalValueImporter} импортер следущего значения для агрегации.
   *          {@link ITemporalValueImporter#NULL} - больше нет значений
   * @return {@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt; список агрегированных значений. Пустой список -
   *         агрегированные значения еще не сформированы
   * @throws TsNullArgumentRtException аргумент = null
   */
  IList<ITemporalAtomicValue> aggregate( ITemporalValueImporter aImporter );

}
