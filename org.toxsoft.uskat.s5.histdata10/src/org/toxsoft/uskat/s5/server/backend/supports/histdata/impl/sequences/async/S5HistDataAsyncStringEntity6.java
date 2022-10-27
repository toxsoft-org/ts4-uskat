package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.async;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.s5.utils.indexes.impl.S5BinaryIndexUtils.*;

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
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceBlockEdit;
import org.toxsoft.uskat.s5.utils.indexes.ILongKey;

/**
 * Блок хранения асинхронных атомарных значений типа {@link EAtomicType#STRING}
 *
 * @author mvk
 */
@Entity
public class S5HistDataAsyncStringEntity6
    extends S5HistDataAsyncBlock<String[], S5HistDataAsyncStringBlobEntity6> {

  private static final long serialVersionUID = 157157L;

  /**
   * Создает блок значений данного
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aValues {@link ITimedList}&lt;{@link TemporalAtomicValue}&gt; список значений
   * @return {@link S5HistDataAsyncStringEntity6} блок значений
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException описание данного должно представлять асинхронное данное
   */
  static S5HistDataAsyncStringEntity6 create( IParameterized aTypeInfo, Gwid aGwid,
      ITimedList<TemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aTypeInfo, aGwid, aValues );
    TsIllegalArgumentRtException.checkFalse( aValues.size() > 0 );
    TsIllegalArgumentRtException.checkTrue( OP_IS_SYNC.getValue( aTypeInfo.params() ).asBool() );
    int count = aValues.size();
    long timestamps[] = new long[count];
    String[] values = new String[count];
    TemporalAtomicValue first = aValues.first();
    TemporalAtomicValue last = aValues.last();
    TemporalAtomicValue prev = first;
    // Пробег по списку через итератор, так как IList может быть связанным списком деградирующим на больших размерах
    int index = 0;
    for( TemporalAtomicValue next : aValues ) {
      checkValuesOrder( aGwid, first, last, prev, next );
      timestamps[index] = next.timestamp();
      IAtomicValue av = next.value();
      values[index] = (av.isAssigned() ? av.asString() : null);
      prev = next;
      index++;
    }
    S5HistDataAsyncStringBlobEntity6 blob = new S5HistDataAsyncStringBlobEntity6( timestamps, values );
    return new S5HistDataAsyncStringEntity6( aTypeInfo, aGwid, blob );
  }

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataAsyncStringEntity6() {
  }

  /**
   * Конструктор
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aBlob {@link S5HistDataAsyncStringBlobEntity6} реализация blob-а в котором хранятся значения блока и их
   *          метки времени
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException количество меток времени не соответствует количеству значений
   * @throws TsIllegalArgumentRtException метки времени не отсортированы в порядке возрастания
   * @throws TsIllegalArgumentRtException описание данного должно представлять асинхронное данное
   */
  S5HistDataAsyncStringEntity6( IParameterized aTypeInfo, Gwid aGwid, S5HistDataAsyncStringBlobEntity6 aBlob ) {
    super( aTypeInfo, aGwid, aBlob );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataAsyncStringEntity6( ResultSet aResultSet ) {
    super( aResultSet );
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISequenceBlock
  //
  @Override
  public TemporalAtomicValue getValue( int aIndex ) {
    String value = values()[aIndex];
    IAtomicValue av = (value != null ? avStr( value ) : IAtomicValue.NULL);
    return new TemporalAtomicValue( timestamp( aIndex ), av );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов
  //
  @Override
  protected IS5SequenceBlockEdit<ITemporalAtomicValue> doCreateBlock( IParameterized aTypeInfo, long[] aTimestamps,
      String[] aValues ) {
    S5HistDataAsyncStringBlobEntity6 blob = new S5HistDataAsyncStringBlobEntity6( aTimestamps, aValues );
    return new S5HistDataAsyncStringEntity6( aTypeInfo, gwid(), blob );
  }

  @Override
  protected S5HistDataAsyncStringBlobEntity6 doCreateBlob( ResultSet aResultSet ) {
    return new S5HistDataAsyncStringBlobEntity6( aResultSet );
  }

  @Override
  protected ILongKey doTimestampIndex( boolean aRestore ) {
    if( aRestore ) {
      // Восстановление индекса без проверки
      return restoreLongElemIndex( timestamps(), values(), false );
    }
    return createLongElemIndex( timestamps(), values() );
  }

  // ------------------------------------------------------------------------------------
  // IS5HistDataBlockReader
  //
  @Override
  public boolean isAssigned( int aIndex ) {
    return values()[aIndex] != null;
  }

  @Override
  public String asString( int aIndex ) {
    String value = values()[aIndex];
    if( value == null ) {
      throw new AvUnassignedValueRtException();
    }
    return value;
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  // }
}
