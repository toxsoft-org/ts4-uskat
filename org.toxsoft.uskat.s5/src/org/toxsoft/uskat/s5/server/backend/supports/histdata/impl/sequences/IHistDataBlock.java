package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlock;

/**
 * Блок последовательности хранимых данных
 *
 * @author mvk
 */
public interface IHistDataBlock
    extends ISequenceBlock<ITemporalAtomicValue> {

  /**
   * Установить начальную метку времени для импорта значений
   *
   * @param aTimestamp long метка времени (мсек с начала эпохи) с которой будет производиться импорт значений
   */
  void setImportTime( long aTimestamp );

  /**
   * Возвращает признак того, что импорт значений может быть продолжен вызовом {@link #nextImport()}
   *
   * @return <b>true</b> есть данные для импорта. <b>false</b> нет данных для импорта
   */
  boolean hasImport();

  /**
   * Импортировать следующее значение
   *
   * @return {@link ITemporalValueImporter} способ получения значений
   * @throws TsIllegalArgumentRtException нет больше данных для импорта
   */
  ITemporalValueImporter nextImport();

}
