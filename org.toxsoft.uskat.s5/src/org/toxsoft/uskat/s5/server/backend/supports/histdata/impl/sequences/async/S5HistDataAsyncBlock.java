package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.async;

import java.sql.ResultSet;

import javax.persistence.MappedSuperclass;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.IS5HistDataBlock;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceCursor;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceAsyncBlob;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceAsyncBlock;

/**
 * Блок хранения асинхронных исторических данных s5-объекта
 *
 * @param <BLOB_ARRAY> тип массива blob-a в котором хранятся значения блока
 * @param <BLOB> реализация blob-а используемого для хранения значений блока
 * @author mvk
 */
@MappedSuperclass
// S5SequenceAsyncBlock<V extends ITemporal<V>, BLOB_ARRAY, BLOB extends S5SequenceAsyncBlob<?, BLOB_ARRAY, ?>>
public abstract class S5HistDataAsyncBlock<BLOB_ARRAY, BLOB extends S5SequenceAsyncBlob<?, BLOB_ARRAY, ?>>
    extends S5SequenceAsyncBlock<ITemporalAtomicValue, BLOB_ARRAY, BLOB>
    implements IS5HistDataBlock {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataAsyncBlock() {
  }

  /**
   * Конструктор
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aBlob BLOB реализация blob-а в котором хранятся значения блока и их метки времени
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException количество меток времени не соответствует количеству значений
   * @throws TsIllegalArgumentRtException метки времени не отсортированы в порядке возрастания
   * @throws TsIllegalArgumentRtException описание данного должно представлять асинхронное данное
   */
  protected S5HistDataAsyncBlock( IParameterized aTypeInfo, Gwid aGwid, BLOB aBlob ) {
    super( aTypeInfo, aGwid, aBlob );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  protected S5HistDataAsyncBlock( ResultSet aResultSet ) {
    super( aResultSet );
  }

  // ------------------------------------------------------------------------------------
  // IS5SequenceBlock
  //
  @Override
  public IS5SequenceCursor<ITemporalAtomicValue> createCursor() {
    // TODO:
    throw new TsUnderDevelopmentRtException();
  }

}
