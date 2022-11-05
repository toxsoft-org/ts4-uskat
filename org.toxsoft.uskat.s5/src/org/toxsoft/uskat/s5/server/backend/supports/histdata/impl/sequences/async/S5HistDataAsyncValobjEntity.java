package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.async;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.s5.utils.indexes.impl.S5BinaryIndexUtils.*;

import java.sql.ResultSet;

import javax.persistence.Entity;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.errors.AvUnassignedValueRtException;
import org.toxsoft.core.tslib.av.impl.AtomicValueKeeper;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.av.temporal.TemporalAtomicValue;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceBlockEdit;
import org.toxsoft.uskat.s5.utils.indexes.ILongKey;

/**
 * Блок хранения асинхронных атомарных значений типа {@link EAtomicType#VALOBJ}
 *
 * @author mvk
 */
@Entity
public class S5HistDataAsyncValobjEntity
    extends S5HistDataAsyncBlock<String[], S5HistDataAsyncValobjBlobEntity> {

  private static final long serialVersionUID = 157157L;

  /**
   * Создает блок значений данного
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aValues {@link ITimedList}&lt;{@link TemporalAtomicValue}&gt; список значений
   * @return {@link S5HistDataAsyncValobjEntity} блок значений
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException описание данного должно представлять асинхронное данное
   */
  static S5HistDataAsyncValobjEntity create( IParameterized aTypeInfo, Gwid aGwid,
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
      values[index] = (av.isAssigned() ? AtomicValueKeeper.KEEPER.ent2str( av.asValobj() ) : null);
      prev = next;
      index++;
    }
    S5HistDataAsyncValobjBlobEntity blob = new S5HistDataAsyncValobjBlobEntity( timestamps, values );
    return new S5HistDataAsyncValobjEntity( aTypeInfo, aGwid, blob );
  }

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataAsyncValobjEntity() {
  }

  /**
   * Конструктор
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aBlob {@link S5HistDataAsyncValobjBlobEntity} реализация blob-а в котором хранятся значения блока и их метки
   *          времени
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException количество меток времени не соответствует количеству значений
   * @throws TsIllegalArgumentRtException метки времени не отсортированы в порядке возрастания
   * @throws TsIllegalArgumentRtException описание данного должно представлять асинхронное данное
   */
  S5HistDataAsyncValobjEntity( IParameterized aTypeInfo, Gwid aGwid, S5HistDataAsyncValobjBlobEntity aBlob ) {
    super( aTypeInfo, aGwid, aBlob );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataAsyncValobjEntity( ResultSet aResultSet ) {
    super( aResultSet );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequenceBlock
  //
  @Override
  public TemporalAtomicValue getValue( int aIndex ) {
    Object value = values()[aIndex];
    IAtomicValue av = (value != null ? avValobj( value ) : IAtomicValue.NULL);
    return new TemporalAtomicValue( timestamp( aIndex ), av );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов
  //
  @Override
  protected IS5SequenceBlockEdit<ITemporalAtomicValue> doCreateBlock( IParameterized aTypeInfo, long[] aTimestamps,
      String[] aValues ) {
    S5HistDataAsyncValobjBlobEntity blob = new S5HistDataAsyncValobjBlobEntity( aTimestamps, aValues );
    return new S5HistDataAsyncValobjEntity( aTypeInfo, gwid(), blob );
  }

  @Override
  protected S5HistDataAsyncValobjBlobEntity doCreateBlob( ResultSet aResultSet ) {
    return new S5HistDataAsyncValobjBlobEntity( aResultSet );
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
  public EAtomicType atomicType() {
    return EAtomicType.VALOBJ;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public <T> T asValobj( int aIndex ) {
    String value = values()[aIndex];
    if( value == null ) {
      throw new AvUnassignedValueRtException();
    }
    return (T)AtomicValueKeeper.KEEPER.str2ent( value );
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  // }
}
