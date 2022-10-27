package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.sync;

import java.sql.ResultSet;

import javax.persistence.MappedSuperclass;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.IS5HistDataBlock;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceCursor;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceSyncBlob;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceSyncBlock;

/**
 * Блок хранения синхронных исторических данных s5-объекта .
 *
 * @param <BLOB_ARRAY> тип массива blob-a в котором хранятся значения блока
 * @param <BLOB> реализация blob-а используемого для хранения значений блока
 * @author mvk
 */
@MappedSuperclass
public abstract class S5HistDataSyncBlock<BLOB_ARRAY, BLOB extends S5SequenceSyncBlob<?, BLOB_ARRAY, ?>>
    extends S5SequenceSyncBlock<ITemporalAtomicValue, BLOB_ARRAY, BLOB>
    implements IS5HistDataBlock {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataSyncBlock() {
  }

  /**
   * Конструктор
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aStartTime long метка (мсек с начала эпохи) начала данных в блоке
   * @param aBlob BLOB реализация blob-а в котором хранятся значения блока
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException неверная метка времени начала значений
   * @throws TsIllegalArgumentRtException описание данного должно представлять синхронное данное
   */
  protected S5HistDataSyncBlock( IParameterized aTypeInfo, Gwid aGwid, long aStartTime, BLOB aBlob ) {
    super( aTypeInfo, aGwid, aStartTime, aBlob );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  protected S5HistDataSyncBlock( ResultSet aResultSet ) {
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
