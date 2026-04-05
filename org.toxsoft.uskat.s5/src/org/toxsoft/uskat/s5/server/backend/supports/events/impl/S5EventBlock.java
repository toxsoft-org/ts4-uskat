package org.toxsoft.uskat.s5.server.backend.supports.events.impl;

import static org.toxsoft.uskat.s5.utils.indexes.impl.S5BinaryIndexUtils.*;

import java.sql.*;

import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.s5.server.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.impl.*;
import org.toxsoft.uskat.s5.utils.indexes.*;

import jakarta.persistence.*;

/**
 * Блок хранения событий s5-объекта.
 *
 * @author mvk
 */
@Entity
public class S5EventBlock
    extends S5SequenceAsyncBlock<SkEvent, SkEvent[], S5EventBlob> {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5EventBlock() {
  }

  /**
   * Создает блок истории событий
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа события
   * @param aObjId {@link Gwid} {@link Gwid}-идентификатор объекта события
   * @param aValues {@link ITimedList}&lt;{@link SkEvent}&gt; список событий (история)
   * @return {@link S5EventBlock} блок истории событий
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество событий в блоке = 0
   * @throws TsIllegalArgumentRtException описание события должно представлять асинхронное значение
   * @throws TsIllegalArgumentRtException aObjId должен представлять конкретный идентификатор объекта
   */
  static S5EventBlock create( IParameterized aTypeInfo, Gwid aObjId, ITimedList<SkEvent> aValues ) {
    TsNullArgumentRtException.checkNulls( aTypeInfo, aObjId, aValues );
    TsIllegalArgumentRtException.checkFalse( aValues.size() > 0 );
    TsIllegalArgumentRtException.checkFalse( aObjId.kind() == EGwidKind.GW_CLASS && !aObjId.isAbstract() );
    int count = aValues.size();
    long timestamps[] = new long[count];
    SkEvent values[] = new SkEvent[count];
    SkEvent first = aValues.first();
    SkEvent last = aValues.last();
    SkEvent prev = first;
    // Пробег по списку через итератор, так как IList может быть связанным списком деградирующим на больших размерах
    int index = 0;
    for( SkEvent next : aValues ) {
      checkValuesOrder( aObjId, first, last, prev, next );
      timestamps[index] = next.timestamp();
      values[index] = next;
      prev = next;
      index++;
    }
    S5EventBlob blob = new S5EventBlob( timestamps, values );
    return new S5EventBlock( aTypeInfo, aObjId, blob );
  }

  /**
   * Конструктор
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа события
   * @param aObjId {@link Gwid} {@link Gwid}-идентификатор объекта события
   * @param aBlob {@link S5EventBlob} реализация blob-а в котором хранятся история событий
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException количество меток времени не соответствует количеству значений
   * @throws TsIllegalArgumentRtException метки времени не отсортированы в порядке возрастания
   * @throws TsIllegalArgumentRtException описание данного должно представлять асинхронное данное
   */
  protected S5EventBlock( IParameterized aTypeInfo, Gwid aObjId, S5EventBlob aBlob ) {
    super( aTypeInfo, aObjId, aBlob );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  protected S5EventBlock( ResultSet aResultSet ) {
    super( aResultSet );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequenceBlock
  //
  @Override
  public SkEvent getValue( int aIndex ) {
    return values()[aIndex];
  }

  @Override
  public IS5SequenceCursor<SkEvent> createCursor() {
    return new S5EventCursor( this );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов
  //
  @Override
  protected IS5SequenceBlockEdit<SkEvent> doCreateBlock( IParameterized aTypeInfo, long[] aTimestamps,
      SkEvent[] aValues ) {
    return new S5EventBlock( aTypeInfo, gwid(), new S5EventBlob( aTimestamps, aValues ) );
  }

  @Override
  protected S5EventBlob doCreateBlob( ResultSet aResultSet ) {
    return new S5EventBlob( aResultSet );
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
  // TODO: IS5EventRawReader
  //
  // @Override
  // protected boolean doIsAssigned( int aIndex ) {
  // return values()[aIndex] != null;
  // }
  //
  // @SuppressWarnings( "unchecked" )
  // @Override
  // protected <T> T doAsValobj( int aIndex ) {
  // SkEvent value = values()[aIndex];
  // if( value == null ) {
  // throw new AvUnassignedValueRtException();
  // }
  // return (T)value;
  // }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
}
