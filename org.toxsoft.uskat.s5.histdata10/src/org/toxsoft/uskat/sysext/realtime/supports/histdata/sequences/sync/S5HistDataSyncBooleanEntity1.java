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
 * Блок хранения синхронных атомарных значений типа {@link EAtomicType#BOOLEAN}
 *
 * @author mvk
 */
@Entity
public class S5HistDataSyncBooleanEntity1
    extends S5HistDataSyncBlock<byte[], S5HistDataSyncBooleanBlobEntity1> {

  private static final long serialVersionUID = 157157L;

  /**
   * Создает блок значений данного
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aValues {@link ITimedList}&lt;{@link TemporalAtomicValue}&gt; список значений
   * @return {@link S5HistDataSyncBooleanEntity1} блок значений
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException описание данного должно представлять синхронное данное
   */
  static S5HistDataSyncBooleanEntity1 create( IParameterized aTypeInfo, Gwid aGwid,
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
    byte values[] = new byte[count];
    int prevWriteIndex = -1;
    // Пробег по списку через итератор, так как IList может быть связанным списком деградирующим на больших размерах
    for( TemporalAtomicValue next : aValues ) {
      checkValuesOrder( aGwid, first, last, prev, next );
      long timestamp = next.timestamp();
      int nextWriteIndex = getIndexByTime( startTime, syncDT, timestamp );
      IAtomicValue av = next.value();
      values[nextWriteIndex] = (av.isAssigned() ? (av.asBool() ? BOOLEAN_TRUE : BOOLEAN_FALSE) : BOOLEAN_NULL);
      for( int writeIndex = prevWriteIndex + 1; writeIndex < nextWriteIndex; writeIndex++ ) {
        values[writeIndex] = BOOLEAN_NULL;
      }
      prevWriteIndex = nextWriteIndex;
      prev = next;
    }
    S5HistDataSyncBooleanBlobEntity1 blob = new S5HistDataSyncBooleanBlobEntity1( values );
    return new S5HistDataSyncBooleanEntity1( aTypeInfo, aGwid, startTime, blob );
  }

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataSyncBooleanEntity1() {
  }

  /**
   * Конструктор
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aStartTime long метка (мсек с начала эпохи) начала данных в блоке
   * @param aBlob {@link S5HistDataSyncBooleanBlobEntity1} реализация blob-а в котором хранятся значения блока
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException описание данного должно представлять синхронное данное
   */
  S5HistDataSyncBooleanEntity1( IParameterized aTypeInfo, Gwid aGwid, long aStartTime,
      S5HistDataSyncBooleanBlobEntity1 aBlob ) {
    super( aTypeInfo, aGwid, aStartTime, aBlob );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataSyncBooleanEntity1( ResultSet aResultSet ) {
    super( aResultSet );
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISequenceBlock
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
  // Реализация абстрактных методов
  //
  @Override
  protected ISequenceBlockEdit<ITemporalAtomicValue> doCreateBlock( IParameterized aTypeInfo, long aStartTime,
      byte[] aValues ) {
    S5HistDataSyncBooleanBlobEntity1 blob = new S5HistDataSyncBooleanBlobEntity1( aValues );
    return new S5HistDataSyncBooleanEntity1( aTypeInfo, gwid(), aStartTime, blob );
  }

  @Override
  protected S5HistDataSyncBooleanBlobEntity1 doCreateBlob( ResultSet aResultSet ) {
    return new S5HistDataSyncBooleanBlobEntity1( aResultSet );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов (импорт значения)
  //
  @Override
  protected boolean doIsAssigned( int aIndex ) {
    return values()[aIndex] != BOOLEAN_NULL;
  }

  @Override
  protected boolean doAsBool( int aIndex ) {
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
  protected int doAsInt( int aIndex ) {
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
  protected long doAsLong( int aIndex ) {
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
  protected float doAsFloat( int aIndex ) {
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
  protected double doAsDouble( int aIndex ) {
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
  //
}
