package org.toxsoft.uskat.s5.server.backend.supports.histdata;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.common.dpu.rt.events.DpuWriteHistData;

/**
 * Писатель последовательностей значений исторических данных
 *
 * @author mvk
 */
public interface IS5HistDataSequenceWriter {

  /**
   * Записать значения хранимых данных
   *
   * @param aHistData {@link DpuWriteHistData} значения хранимых данных для записи
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void write( DpuWriteHistData aHistData );

}
