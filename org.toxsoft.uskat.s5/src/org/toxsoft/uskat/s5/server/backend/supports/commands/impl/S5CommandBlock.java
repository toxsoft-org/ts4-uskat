package org.toxsoft.uskat.s5.server.backend.supports.commands.impl;

import static org.toxsoft.uskat.s5.utils.indexes.impl.S5BinaryIndexUtils.*;

import java.sql.ResultSet;

import javax.persistence.Entity;

import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.EGwidKind;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.s5.server.backend.supports.commands.sequences.S5CommandCursor;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceBlockEdit;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceCursor;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceAsyncBlock;
import org.toxsoft.uskat.s5.utils.indexes.ILongKey;

/**
 * Блок хранения команд s5-объекта.
 *
 * @author mvk
 */
@Entity
public class S5CommandBlock
    extends S5SequenceAsyncBlock<IDtoCompletedCommand, IDtoCompletedCommand[], S5CommandBlob> {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5CommandBlock() {
  }

  /**
   * Создает блок истории команд
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа команды
   * @param aObjId {@link Gwid} {@link Gwid}-идентификатор объекта события
   * @param aValues {@link ITimedList}&lt;{@link IDtoCompletedCommand}&gt; список выполненных команд (история)
   * @return {@link S5CommandBlock} блок истории команд
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество команд в блоке = 0
   * @throws TsIllegalArgumentRtException описание команды должно представлять асинхронное значение
   * @throws TsIllegalArgumentRtException aObjId должен представлять конкретный идентификатор объекта
   */
  static S5CommandBlock create( IParameterized aTypeInfo, Gwid aObjId, ITimedList<IDtoCompletedCommand> aValues ) {
    TsNullArgumentRtException.checkNulls( aTypeInfo, aObjId, aValues );
    TsIllegalArgumentRtException.checkFalse( aValues.size() > 0 );
    TsIllegalArgumentRtException.checkFalse( aObjId.kind() == EGwidKind.GW_CLASS && !aObjId.isAbstract() );
    int count = aValues.size();
    long timestamps[] = new long[count];
    IDtoCompletedCommand values[] = new IDtoCompletedCommand[count];
    IDtoCompletedCommand first = aValues.first();
    IDtoCompletedCommand last = aValues.last();
    IDtoCompletedCommand prev = first;
    // Пробег по списку через итератор, так как IList может быть связанным списком деградирующим на больших размерах
    int index = 0;
    for( IDtoCompletedCommand next : aValues ) {
      checkValuesOrder( aObjId, first, last, prev, next );
      timestamps[index] = next.timestamp();
      values[index] = next;
      prev = next;
      index++;
    }
    S5CommandBlob blob = new S5CommandBlob( timestamps, values );
    return new S5CommandBlock( aTypeInfo, aObjId, blob );
  }

  /**
   * Конструктор
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа команды
   * @param aObjId {@link Gwid} {@link Gwid}-идентификатор объекта команды
   * @param aBlob {@link S5CommandBlob} реализация blob-а в котором хранятся история событий
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException количество меток времени не соответствует количеству значений
   * @throws TsIllegalArgumentRtException метки времени не отсортированы в порядке возрастания
   * @throws TsIllegalArgumentRtException описание данного должно представлять асинхронное данное
   */
  protected S5CommandBlock( IParameterized aTypeInfo, Gwid aObjId, S5CommandBlob aBlob ) {
    super( aTypeInfo, aObjId, aBlob );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  protected S5CommandBlock( ResultSet aResultSet ) {
    super( aResultSet );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequenceBlock
  //
  @Override
  public IDtoCompletedCommand getValue( int aIndex ) {
    return values()[aIndex];
  }

  @Override
  public IS5SequenceCursor<IDtoCompletedCommand> createCursor() {
    return new S5CommandCursor( this );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов
  //
  @Override
  protected IS5SequenceBlockEdit<IDtoCompletedCommand> doCreateBlock( IParameterized aTypeInfo, long[] aTimestamps,
      IDtoCompletedCommand[] aValues ) {
    return new S5CommandBlock( aTypeInfo, gwid(), new S5CommandBlob( aTimestamps, aValues ) );
  }

  @Override
  protected S5CommandBlob doCreateBlob( ResultSet aResultSet ) {
    return new S5CommandBlob( aResultSet );
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
  // TODO: IS5CommandRawReader
  //
  // @Override
  // protected boolean doIsAssigned( int aIndex ) {
  // return values()[aIndex] != null;
  // }
  //
  // @SuppressWarnings( "unchecked" )
  // @Override
  // protected <T> T doAsValobj( int aIndex ) {
  // IDtoCompletedCommand value = values()[aIndex];
  // if( value == null ) {
  // throw new AvUnassignedValueRtException();
  // }
  // return (T)value;
  // }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
}
