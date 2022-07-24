package org.toxsoft.uskat.sysext.realtime.supports.histdata.sequences.sync;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;

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
import org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.S5HistDataSyncBlock;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlockEdit;

/**
 * Блок хранения асинхронных атомарных значений типа {@link EAtomicType#STRING}
 *
 * @author mvk
 */
@Entity
public class S5HistDataSyncStringEntity2
    extends S5HistDataSyncBlock<String[], S5HistDataSyncStringBlobEntity2> {

  private static final long serialVersionUID = 157157L;

  /**
   * Создает блок значений данного
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aValues {@link ITimedList}&lt;{@link TemporalAtomicValue}&gt; список значений
   * @return {@link S5HistDataSyncStringEntity2} блок значений
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException описание данного должно представлять синхронное данное
   */
  static S5HistDataSyncStringEntity2 create( IParameterized aTypeInfo, Gwid aGwid,
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
    String values[] = new String[count];
    int prevWriteIndex = -1;
    // Пробег по списку через итератор, так как IList может быть связанным списком деградирующим на больших размерах
    for( TemporalAtomicValue next : aValues ) {
      checkValuesOrder( aGwid, first, last, prev, next );
      long timestamp = next.timestamp();
      int nextWriteIndex = getIndexByTime( startTime, syncDT, timestamp );
      IAtomicValue av = next.value();
      values[nextWriteIndex] = (av.isAssigned() ? av.asString() : null);
      for( int writeIndex = prevWriteIndex + 1; writeIndex < nextWriteIndex; writeIndex++ ) {
        values[writeIndex] = null;
      }
      prevWriteIndex = nextWriteIndex;
      prev = next;
    }
    S5HistDataSyncStringBlobEntity2 blob = new S5HistDataSyncStringBlobEntity2( values );
    return new S5HistDataSyncStringEntity2( aTypeInfo, aGwid, startTime, blob );
  }

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataSyncStringEntity2() {
  }

  /**
   * Конструктор
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aStartTime long метка (мсек с начала эпохи) начала данных в блоке
   * @param aBlob {@link S5HistDataSyncStringBlobEntity2} реализация blob-а в котором хранятся значения блока
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException описание данного должно представлять синхронное данное
   */
  S5HistDataSyncStringEntity2( IParameterized aTypeInfo, Gwid aGwid, long aStartTime,
      S5HistDataSyncStringBlobEntity2 aBlob ) {
    super( aTypeInfo, aGwid, aStartTime, aBlob );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  S5HistDataSyncStringEntity2( ResultSet aResultSet ) {
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
  // Реализация абстрактных методов
  //
  @Override
  protected ISequenceBlockEdit<ITemporalAtomicValue> doCreateBlock( IParameterized aTypeInfo, long aStartTime,
      String[] aValues ) {
    S5HistDataSyncStringBlobEntity2 blob = new S5HistDataSyncStringBlobEntity2( aValues );
    return new S5HistDataSyncStringEntity2( aTypeInfo, gwid(), aStartTime, blob );
  }

  @Override
  protected S5HistDataSyncStringBlobEntity2 doCreateBlob( ResultSet aResultSet ) {
    return new S5HistDataSyncStringBlobEntity2( aResultSet );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов (импорт значения)
  //
  @Override
  protected boolean doIsAssigned( int aIndex ) {
    return values()[aIndex] != null;
  }

  @Override
  protected String doAsString( int aIndex ) {
    String value = values()[aIndex];
    if( value == null ) {
      throw new AvUnassignedValueRtException();
    }
    return value;
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
}
