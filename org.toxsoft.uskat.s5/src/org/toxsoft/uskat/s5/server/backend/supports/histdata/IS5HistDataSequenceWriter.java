package org.toxsoft.uskat.s5.server.backend.supports.histdata;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Писатель последовательностей значений исторических данных
 *
 * @author mvk
 */
public interface IS5HistDataSequenceWriter {

  /**
   * Записать значения хранимых данных
   *
   * @param aHistData {@link ITimedList} значения хранимых данных для записи
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void write( IMap<Gwid, ITimedList<ITemporalAtomicValue>> aHistData );

}
