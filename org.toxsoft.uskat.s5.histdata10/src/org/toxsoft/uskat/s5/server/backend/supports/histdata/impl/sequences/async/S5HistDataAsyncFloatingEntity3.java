package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.async;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.S5HistDataSequenceFactory.*;
import static org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.async.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.s5.utils.indexes.impl.S5BinaryIndexUtils.*;

import java.sql.ResultSet;

import javax.persistence.Entity;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.errors.AvDataLossRtException;
import org.toxsoft.core.tslib.av.errors.AvUnassignedValueRtException;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.av.temporal.TemporalAtomicValue;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceBlockEdit;
import org.toxsoft.uskat.s5.utils.indexes.ILongKey;

/**
 * Блок хранения асинхронных атомарных значений типа {@link EAtomicType#FLOATING}
 *
 * @author mvk
 */
@Entity
public class S5HistDataAsyncFloatingEntity3
    extends S5HistDataAsyncBlock<double[], S5HistDataAsyncFloatingBlobEntity3> {

  private static final long serialVersionUID = 157157L;

  /**
   * Создает блок значений данного
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aValues {@link ITimedList}&lt;{@link TemporalAtomicValue}&gt; список значений
   * @return {@link S5HistDataAsyncFloatingEntity3} блок значений
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException описание данного должно представлять асинхронное данное
   */
  static S5HistDataAsyncFloatingEntity3 create( IParameterized aTypeInfo, Gwid aGwid,
      ITimedList<TemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aTypeInfo, aGwid, aValues );
    TsIllegalArgumentRtException.checkFalse( aValues.size() > 0 );
    TsIllegalArgumentRtException.checkTrue( OP_IS_SYNC.getValue( aTypeInfo.params() ).asBool() );
    int count = aValues.size();
    long timestamps[] = new long[count];
    double values[] = new double[count];
    TemporalAtomicValue first = aValues.first();
    TemporalAtomicValue last = aValues.last();
    TemporalAtomicValue prev = first;
    // Пробег по списку через итератор, так как IList может быть связанным списком деградирующим на больших размерах
    int index = 0;
    for( TemporalAtomicValue next : aValues ) {
      checkValuesOrder( aGwid, first, last, prev, next );
      timestamps[index] = next.timestamp();
      IAtomicValue av = next.value();
      values[index] = (av.isAssigned() ? av.asFloat() : DOUBLE_NULL);
      prev = next;
      index++;
    }
    S5HistDataAsyncFloatingBlobEntity3 blob = new S5HistDataAsyncFloatingBlobEntity3( timestamps, values );
    return new S5HistDataAsyncFloatingEntity3( aTypeInfo, aGwid, blob );
  }

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataAsyncFloatingEntity3() {
  }

  /**
   * Конструктор
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aBlob {@link S5HistDataAsyncFloatingBlobEntity3} реализация blob-а в котором хранятся значения блока и их
   *          метки времени
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException количество меток времени не соответствует количеству значений
   * @throws TsIllegalArgumentRtException метки времени не отсортированы в порядке возрастания
   * @throws TsIllegalArgumentRtException описание данного должно представлять асинхронное данное
   */
  S5HistDataAsyncFloatingEntity3( IParameterized aTypeInfo, Gwid aGwid, S5HistDataAsyncFloatingBlobEntity3 aBlob ) {
    super( aTypeInfo, aGwid, aBlob );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataAsyncFloatingEntity3( ResultSet aResultSet ) {
    super( aResultSet );
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISequenceBlock
  //
  @Override
  public TemporalAtomicValue getValue( int aIndex ) {
    double value = values()[aIndex];
    IAtomicValue av = (value != DOUBLE_NULL ? avFloat( value ) : IAtomicValue.NULL);
    return new TemporalAtomicValue( timestamp( aIndex ), av );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов
  //
  @Override
  protected IS5SequenceBlockEdit<ITemporalAtomicValue> doCreateBlock( IParameterized aTypeInfo, long[] aTimestamps,
      double[] aValues ) {
    S5HistDataAsyncFloatingBlobEntity3 blob = new S5HistDataAsyncFloatingBlobEntity3( aTimestamps, aValues );
    return new S5HistDataAsyncFloatingEntity3( aTypeInfo, gwid(), blob );
  }

  @Override
  protected S5HistDataAsyncFloatingBlobEntity3 doCreateBlob( ResultSet aResultSet ) {
    return new S5HistDataAsyncFloatingBlobEntity3( aResultSet );
  }

  @Override
  protected ILongKey doTimestampIndex( boolean aRestore ) {
    if( aRestore ) {
      // Восстановление индекса без проверки
      return restoreLongDoubleIndex( timestamps(), values(), false );
    }
    return createLongDoubleIndex( timestamps(), values() );
  }

  // ------------------------------------------------------------------------------------
  // IS5HistDataBlockReader
  //
  @Override
  public boolean isAssigned( int aIndex ) {
    return values()[aIndex] != DOUBLE_NULL;
  }

  @Override
  public EAtomicType atomicType() {
    return EAtomicType.FLOATING;
  }

  @Override
  public double asDouble( int aIndex ) {
    double value = values()[aIndex];
    if( value == DOUBLE_NULL ) {
      throw new AvUnassignedValueRtException();
    }
    return value;
  }

  @Override
  public float asFloat( int aIndex ) {
    double value = values()[aIndex];
    if( value == DOUBLE_NULL ) {
      throw new AvUnassignedValueRtException();
    }
    if( value > Float.MAX_VALUE || value < -(double)Float.MAX_VALUE ) {
      throw new AvDataLossRtException( ERR_CAST_VALUE_ACCURACY, this, EAtomicType.FLOATING );
    }
    return (float)value;
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  // }
}
