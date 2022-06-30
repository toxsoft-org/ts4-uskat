package org.toxsoft.uskat.s5.server.backend.supports.events.impl;

import static org.toxsoft.uskat.s5.utils.indexes.impl.S5BinaryIndexUtils.*;

import java.sql.ResultSet;

import javax.persistence.Entity;

import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.EGwidKind;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlockEdit;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceAsyncBlock;
import org.toxsoft.uskat.s5.utils.indexes.ILongKey;

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
  // Реализация ISequenceBlock
  //
  @Override
  public SkEvent getValue( int aIndex ) {
    return values()[aIndex];
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов
  //
  @Override
  protected ISequenceBlockEdit<SkEvent> doCreateBlock( IParameterized aTypeInfo, long[] aTimestamps,
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
  // Внутреннее API
  //
}
