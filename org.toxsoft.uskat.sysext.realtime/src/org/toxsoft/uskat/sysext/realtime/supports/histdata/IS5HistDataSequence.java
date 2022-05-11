package org.toxsoft.uskat.sysext.realtime.supports.histdata;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.IS5Sequence;
import org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.ITemporalValueImporter;

/**
 * Последовательность значений одного исторического данного.
 *
 * @author mvk
 */
public interface IS5HistDataSequence
    extends IS5Sequence<ITemporalAtomicValue> {

  /**
   * Установить начальную метку времени для импорта значений
   * <p>
   * Если в последовательности нет значения точно по указанной метке, то метка устанавливается на первое значение за
   * указанной меткой
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
