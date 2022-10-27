package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.async;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.S5HistDataSequenceFactory.*;
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
 * Блок хранения асинхронных атомарных значений типа {@link EAtomicType#BOOLEAN}
 *
 * @author mvk
 */
@Entity
public class S5HistDataAsyncBooleanEnity
    extends S5HistDataAsyncBlock<byte[], S5HistDataAsyncBooleanBlobEntity> {

  private static final long serialVersionUID = 157157L;

  /**
   * Создает блок значений данного
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aValues {@link ITimedList}&lt;{@link TemporalAtomicValue}&gt; список значений
   * @return {@link S5HistDataAsyncBooleanEnity} блок значений
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException описание данного должно представлять асинхронное данное
   */
  static S5HistDataAsyncBooleanEnity create( IParameterized aTypeInfo, Gwid aGwid,
      ITimedList<TemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aTypeInfo, aGwid, aValues );
    TsIllegalArgumentRtException.checkFalse( aValues.size() > 0 );
    TsIllegalArgumentRtException.checkTrue( OP_IS_SYNC.getValue( aTypeInfo.params() ).asBool() );
    // TODO: 2020-07-10 mvkd
    // if( OP_IS_SYNC.getValue( aTypeInfo.params() ).asBool() ) {
    // StringBuilder sb = new StringBuilder();
    // for( int index = 0, n = aTypeInfo.params().listNames().size(); index < n; index++ ) {
    // String paramName = aTypeInfo.params().listNames().get( index );
    // sb.append( String.format( " %s = %s", paramName, aTypeInfo.params().getValue( paramName ) ) );
    // if( index + 1 < n ) {
    // sb.append( "\n" );
    // }
    // }
    // logger.error( "create: aGwid = %s, typeInfo.params():\n%s", aGwid, sb.toString() );
    // throw new TsIllegalArgumentRtException();
    // }
    int count = aValues.size();
    long timestamps[] = new long[count];
    byte values[] = new byte[count];
    TemporalAtomicValue first = aValues.first();
    TemporalAtomicValue last = aValues.last();
    TemporalAtomicValue prev = first;
    // Пробег по списку через итератор, так как IList может быть связанным списком деградирующим на больших размерах
    int index = 0;
    for( TemporalAtomicValue next : aValues ) {
      checkValuesOrder( aGwid, first, last, prev, next );
      timestamps[index] = next.timestamp();
      IAtomicValue av = next.value();
      values[index] = (av.isAssigned() ? (av.asBool() ? BOOLEAN_TRUE : BOOLEAN_FALSE) : BOOLEAN_NULL);
      prev = next;
      index++;
    }
    S5HistDataAsyncBooleanBlobEntity blob = new S5HistDataAsyncBooleanBlobEntity( timestamps, values );
    return new S5HistDataAsyncBooleanEnity( aTypeInfo, aGwid, blob );
  }

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataAsyncBooleanEnity() {
  }

  /**
   * Конструктор
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aBlob {@link S5HistDataAsyncBooleanBlobEntity} реализация blob-а в котором хранятся значения блока и их
   *          метки времени
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException количество меток времени не соответствует количеству значений
   * @throws TsIllegalArgumentRtException метки времени не отсортированы в порядке возрастания
   * @throws TsIllegalArgumentRtException описание данного должно представлять асинхронное данное
   */
  S5HistDataAsyncBooleanEnity( IParameterized aTypeInfo, Gwid aGwid, S5HistDataAsyncBooleanBlobEntity aBlob ) {
    super( aTypeInfo, aGwid, aBlob );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataAsyncBooleanEnity( ResultSet aResultSet ) {
    super( aResultSet );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequenceBlock
  //
  @Override
  public TemporalAtomicValue getValue( int aIndex ) {
    byte value = values()[aIndex];
    switch( value ) {
      case BOOLEAN_FALSE:
        return new TemporalAtomicValue( timestamp( aIndex ), AV_FALSE );
      case BOOLEAN_TRUE:
        return new TemporalAtomicValue( timestamp( aIndex ), AV_TRUE );
      case BOOLEAN_NULL:
        return new TemporalAtomicValue( timestamp( aIndex ), IAtomicValue.NULL );
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов
  //
  @Override
  protected IS5SequenceBlockEdit<ITemporalAtomicValue> doCreateBlock( IParameterized aTypeInfo, long[] aTimestamps,
      byte[] aValues ) {
    S5HistDataAsyncBooleanBlobEntity blob = new S5HistDataAsyncBooleanBlobEntity( aTimestamps, aValues );
    return new S5HistDataAsyncBooleanEnity( aTypeInfo, gwid(), blob );
  }

  @Override
  protected S5HistDataAsyncBooleanBlobEntity doCreateBlob( ResultSet aResultSet ) {
    return new S5HistDataAsyncBooleanBlobEntity( aResultSet );
  }

  @Override
  protected ILongKey doTimestampIndex( boolean aRestore ) {
    if( aRestore ) {
      // Восстановление индекса без проверки
      return restoreLongByteIndex( timestamps(), values(), false );
    }
    return createLongByteIndex( timestamps(), values() );
  }

  // ------------------------------------------------------------------------------------
  // IS5HistDataBlockReader
  //
  @Override
  public boolean isAssigned( int aIndex ) {
    return values()[aIndex] != BOOLEAN_NULL;
  }

  @Override
  public boolean asBool( int aIndex ) {
    switch( values()[aIndex] ) {
      case BOOLEAN_TRUE:
        return true;
      case BOOLEAN_FALSE:
        return false;
      case BOOLEAN_NULL:
        throw new AvUnassignedValueRtException();
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  @Override
  public int asInt( int aIndex ) {
    switch( values()[aIndex] ) {
      case BOOLEAN_TRUE:
        return AV_1.asInt();
      case BOOLEAN_FALSE:
        return AV_0.asInt();
      case BOOLEAN_NULL:
        throw new AvUnassignedValueRtException();
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  @Override
  public long asLong( int aIndex ) {
    switch( values()[aIndex] ) {
      case BOOLEAN_TRUE:
        return AV_1.asLong();
      case BOOLEAN_FALSE:
        return AV_0.asLong();
      case BOOLEAN_NULL:
        throw new AvUnassignedValueRtException();
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  @Override
  public float asFloat( int aIndex ) {
    switch( values()[aIndex] ) {
      case BOOLEAN_TRUE:
        return AV_1.asFloat();
      case BOOLEAN_FALSE:
        return AV_0.asFloat();
      case BOOLEAN_NULL:
        throw new AvUnassignedValueRtException();
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  @Override
  public double asDouble( int aIndex ) {
    switch( values()[aIndex] ) {
      case BOOLEAN_TRUE:
        return AV_1.asDouble();
      case BOOLEAN_FALSE:
        return AV_0.asDouble();
      case BOOLEAN_NULL:
        throw new AvUnassignedValueRtException();
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  // }
}
