package org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.sync;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.S5HistDataSequenceFactory.*;

import java.sql.ResultSet;

import javax.persistence.Entity;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.errors.AvUnassignedValueRtException;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.av.temporal.TemporalAtomicValue;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlockEdit;
import org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.S5HistDataSyncBlock;

/**
 * Блок хранения асинхронных атомарных значений типа {@link EAtomicType#TIMESTAMP}
 *
 * @author mvk
 */
@Entity
public class S5HistDataSyncTimestampEntity2
    extends S5HistDataSyncBlock<long[], S5HistDataSyncTimestampBlobEntity2> {

  private static final long serialVersionUID = 157157L;

  /**
   * Создает блок значений данного
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aValues {@link ITimedList}&lt;{@link TemporalAtomicValue}&gt; список значений
   * @return {@link S5HistDataSyncTimestampEntity2} блок значений
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException описание данного должно представлять синхронное данное
   */
  static S5HistDataSyncTimestampEntity2 create( IParameterized aTypeInfo, Gwid aGwid,
      ITimedList<TemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aTypeInfo, aGwid, aValues );
    TsIllegalArgumentRtException.checkFalse( aValues.size() > 0 );
    TsIllegalArgumentRtException.checkFalse( OP_IS_SYNC.getValue( aTypeInfo.params() ).asBool() );
    TemporalAtomicValue first = aValues.first();
    TemporalAtomicValue last = aValues.last();
    TemporalAtomicValue prev = first;
    long syncDT = OP_SYNC_DT.getValue( aTypeInfo.params() ).asLong();
    long startTime = alignByDDT( aTypeInfo, first.timestamp() );
    long endTime = alignByDDT( aTypeInfo, last.timestamp() );
    int count = (int)((endTime - startTime) / syncDT + 1);
    long values[] = new long[count];
    int prevWriteIndex = -1;
    // Пробег по списку через итератор, так как IList может быть связанным списком деградирующим на больших размерах
    for( TemporalAtomicValue next : aValues ) {
      checkValuesOrder( aGwid, first, last, prev, next );
      long timestamp = next.timestamp();
      int nextWriteIndex = getIndexByTime( startTime, syncDT, timestamp );
      IAtomicValue av = next.value();
      values[nextWriteIndex] = (av.isAssigned() ? av.asLong() : LONG_NULL);
      for( int writeIndex = prevWriteIndex + 1; writeIndex < nextWriteIndex; writeIndex++ ) {
        values[writeIndex] = LONG_NULL;
      }
      prevWriteIndex = nextWriteIndex;
      prev = next;
    }
    S5HistDataSyncTimestampBlobEntity2 blob = new S5HistDataSyncTimestampBlobEntity2( values );
    return new S5HistDataSyncTimestampEntity2( aTypeInfo, aGwid, startTime, blob );
  }

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataSyncTimestampEntity2() {
  }

  /**
   * Конструктор
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aStartTime long метка (мсек с начала эпохи) начала данных в блоке
   * @param aBlob {@link S5HistDataSyncTimestampBlobEntity2} реализация blob-а в котором хранятся значения блока
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException описание данного должно представлять синхронное данное
   */
  S5HistDataSyncTimestampEntity2( IParameterized aTypeInfo, Gwid aGwid, long aStartTime,
      S5HistDataSyncTimestampBlobEntity2 aBlob ) {
    super( aTypeInfo, aGwid, aStartTime, aBlob );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataSyncTimestampEntity2( ResultSet aResultSet ) {
    super( aResultSet );
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISequenceBlock
  //
  @Override
  public TemporalAtomicValue getValue( int aIndex ) {
    long value = values()[aIndex];
    IAtomicValue av = (value != LONG_NULL ? avInt( value ) : IAtomicValue.NULL);
    return new TemporalAtomicValue( timestamp( aIndex ), av );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов
  //
  @Override
  protected ISequenceBlockEdit<ITemporalAtomicValue> doCreateBlock( IParameterized aTypeInfo, long aStartTime,
      long[] aValues ) {
    S5HistDataSyncTimestampBlobEntity2 blob = new S5HistDataSyncTimestampBlobEntity2( aValues );
    return new S5HistDataSyncTimestampEntity2( aTypeInfo, gwid(), aStartTime, blob );
  }

  @Override
  protected S5HistDataSyncTimestampBlobEntity2 doCreateBlob( ResultSet aResultSet ) {
    return new S5HistDataSyncTimestampBlobEntity2( aResultSet );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов (импорт значения)
  //
  @Override
  protected boolean doIsAssigned( int aIndex ) {
    return values()[aIndex] != LONG_NULL;
  }

  @Override
  protected long doAsLong( int aIndex ) {
    long value = values()[aIndex];
    if( value == LONG_NULL ) {
      throw new AvUnassignedValueRtException();
    }
    return value;
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
}