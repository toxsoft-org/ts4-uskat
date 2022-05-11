package org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.async;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.s5.utils.indexes.impl.S5BinaryIndexUtils.*;
import static org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.S5HistDataSequenceFactory.*;
import static org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.async.IS5Resources.*;

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
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlockEdit;
import org.toxsoft.uskat.s5.utils.indexes.ILongKey;

/**
 * Блок хранения асинхронных атомарных значений типа {@link EAtomicType#INTEGER}
 *
 * @author mvk
 */
@Entity
public class S5HistDataAsyncIntegerEntity1
    extends S5HistDataAsyncBlock<long[], S5HistDataAsyncIntegerBlobEntity1> {

  private static final long serialVersionUID = 157157L;

  /**
   * Создает блок значений данного
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aValues {@link ITimedList}&lt;{@link TemporalAtomicValue}&gt; список значений
   * @return {@link S5HistDataAsyncIntegerEntity1} блок значений
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException описание данного должно представлять асинхронное данное
   */
  static S5HistDataAsyncIntegerEntity1 create( IParameterized aTypeInfo, Gwid aGwid,
      ITimedList<TemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aTypeInfo, aGwid, aValues );
    TsIllegalArgumentRtException.checkFalse( aValues.size() > 0 );
    TsIllegalArgumentRtException.checkTrue( OP_IS_SYNC.getValue( aTypeInfo.params() ).asBool() );
    int count = aValues.size();
    long timestamps[] = new long[count];
    long values[] = new long[count];
    TemporalAtomicValue first = aValues.first();
    TemporalAtomicValue last = aValues.last();
    TemporalAtomicValue prev = first;
    // Пробег по списку через итератор, так как IList может быть связанным списком деградирующим на больших размерах
    int index = 0;
    for( TemporalAtomicValue next : aValues ) {
      checkValuesOrder( aGwid, first, last, prev, next );
      timestamps[index] = next.timestamp();
      IAtomicValue av = next.value();
      values[index] = (av.isAssigned() ? av.asLong() : LONG_NULL);
      prev = next;
      index++;
    }
    S5HistDataAsyncIntegerBlobEntity1 blob = new S5HistDataAsyncIntegerBlobEntity1( timestamps, values );
    return new S5HistDataAsyncIntegerEntity1( aTypeInfo, aGwid, blob );
  }

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataAsyncIntegerEntity1() {
  }

  /**
   * Конструктор
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aBlob {@link S5HistDataAsyncIntegerBlobEntity1} реализация blob-а в котором хранятся значения блока и их
   *          метки времени
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException количество меток времени не соответствует количеству значений
   * @throws TsIllegalArgumentRtException метки времени не отсортированы в порядке возрастания
   * @throws TsIllegalArgumentRtException описание данного должно представлять асинхронное данное
   */
  S5HistDataAsyncIntegerEntity1( IParameterized aTypeInfo, Gwid aGwid, S5HistDataAsyncIntegerBlobEntity1 aBlob ) {
    super( aTypeInfo, aGwid, aBlob );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataAsyncIntegerEntity1( ResultSet aResultSet ) {
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
  // Реализация шаблонных методов
  //
  @Override
  protected ISequenceBlockEdit<ITemporalAtomicValue> doCreateBlock( IParameterized aTypeInfo, long[] aTimestamps,
      long[] aValues ) {
    S5HistDataAsyncIntegerBlobEntity1 blob = new S5HistDataAsyncIntegerBlobEntity1( aTimestamps, aValues );
    return new S5HistDataAsyncIntegerEntity1( aTypeInfo, gwid(), blob );
  }

  @Override
  protected S5HistDataAsyncIntegerBlobEntity1 doCreateBlob( ResultSet aResultSet ) {
    return new S5HistDataAsyncIntegerBlobEntity1( aResultSet );
  }

  @Override
  protected ILongKey doTimestampIndex( boolean aRestore ) {
    if( aRestore ) {
      // Восстановление индекса без проверки
      return restoreLongLongIndex( timestamps(), values(), false );
    }
    return createLongLongIndex( timestamps(), values() );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов (импорт значения)
  //
  @Override
  protected boolean doIsAssigned( int aIndex ) {
    return values()[aIndex] != LONG_NULL;
  }

  @Override
  protected int doAsInt( int aIndex ) {
    long value = values()[aIndex];
    if( value == LONG_NULL ) {
      throw new AvUnassignedValueRtException();
    }
    if( value > Integer.MAX_VALUE || value < Integer.MIN_VALUE ) {
      throw new AvDataLossRtException( ERR_CAST_VALUE_ACCURACY, this, EAtomicType.INTEGER );
    }
    return (int)value;
  }

  @Override
  protected long doAsLong( int aIndex ) {
    long value = values()[aIndex];
    if( value == LONG_NULL ) {
      throw new AvUnassignedValueRtException();
    }
    return value;
  }

  @Override
  protected float doAsFloat( int aIndex ) {
    long value = values()[aIndex];
    if( value == LONG_NULL ) {
      throw new AvUnassignedValueRtException();
    }
    return value;
  }

  @Override
  protected double doAsDouble( int aIndex ) {
    long value = values()[aIndex];
    if( value == LONG_NULL ) {
      throw new AvUnassignedValueRtException();
    }
    return value;
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  // }
}
