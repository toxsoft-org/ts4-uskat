package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.async;

import static org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.async.IS5Resources.*;

import java.sql.ResultSet;

import javax.persistence.MappedSuperclass;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.errors.AvTypeCastRtException;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.IHistDataBlock;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.ITemporalValueImporter;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceAsyncBlob;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceAsyncBlock;

/**
 * Блок хранения асинхронных исторических данных s5-объекта
 *
 * @param <BLOB_ARRAY> тип массива blob-a в котором хранятся значения блока
 * @param <BLOB> реализация blob-а используемого для хранения значений блока
 * @author mvk
 */
@MappedSuperclass
public abstract class S5HistDataAsyncBlock<BLOB_ARRAY, BLOB extends S5SequenceAsyncBlob<?, BLOB_ARRAY, ?>>
    extends S5SequenceAsyncBlock<ITemporalAtomicValue, BLOB_ARRAY, BLOB>
    implements IHistDataBlock, ITemporalValueImporter {

  private static final long serialVersionUID = 157157L;
  private transient int     importIndex      = -1;
  private transient boolean hasImport        = false;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataAsyncBlock() {
  }

  /**
   * Конструктор
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aBlob BLOB реализация blob-а в котором хранятся значения блока и их метки времени
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException количество меток времени не соответствует количеству значений
   * @throws TsIllegalArgumentRtException метки времени не отсортированы в порядке возрастания
   * @throws TsIllegalArgumentRtException описание данного должно представлять асинхронное данное
   */
  protected S5HistDataAsyncBlock( IParameterized aTypeInfo, Gwid aGwid, BLOB aBlob ) {
    super( aTypeInfo, aGwid, aBlob );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  protected S5HistDataAsyncBlock( ResultSet aResultSet ) {
    super( aResultSet );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IHistDataBlock
  //
  @Override
  public final void setImportTime( long aTimestamp ) {
    importIndex = firstByTime( aTimestamp );
    if( importIndex >= 0 ) {
      hasImport = true;
      // Декремент индекса так как он будет поправлен при первом вызове nextImport
      importIndex--;
      return;
    }
    // Нет данных для импорта
    hasImport = false;
    importIndex = -1;
  }

  @Override
  public final boolean hasImport() {
    return hasImport;
  }

  @Override
  public final ITemporalValueImporter nextImport() {
    if( !hasImport ) {
      throw new TsIllegalArgumentRtException( ERR_NOT_IMPORT_DATA, this );
    }
    importIndex++;
    if( importIndex + 1 >= size() ) {
      // Достижение конца блока. Больше нет данных для импорта
      hasImport = false;
    }
    return this;
  }

  // ------------------------------------------------------------------------------------
  // Реализация ITemporalValueImporter
  //
  @Override
  public final long timestamp() {
    return timestamp( importIndex );
  }

  @Override
  public final boolean isAssigned() {
    return doIsAssigned( importIndex );
  }

  @Override
  public final boolean asBool() {
    return doAsBool( importIndex );
  }

  @Override
  public final int asInt() {
    return doAsInt( importIndex );
  }

  @Override
  public final long asLong() {
    return doAsLong( importIndex );
  }

  @Override
  public final float asFloat() {
    return doAsFloat( importIndex );
  }

  @Override
  public final double asDouble() {
    return doAsDouble( importIndex );
  }

  @Override
  public final String asString() {
    return doAsString( importIndex );
  }

  @Override
  public final <T> T asValobj() {
    return doAsValobj( importIndex );
  }

  // ------------------------------------------------------------------------------------
  // Методы для реализации наследниками
  //
  /**
   * Возвращает признак того, что значение установлено и может быть прочитано
   *
   * @param aIndex индекс значения
   * @return boolean <b>true</b> значение установлено;<b>false</b> значение не установлено, попытка чтения приведет к
   *         ошибке.
   */
  abstract protected boolean doIsAssigned( int aIndex );

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения
   * @return boolean значение
   */
  protected boolean doAsBool( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, EAtomicType.BOOLEAN );
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения
   * @return int значение
   */
  protected int doAsInt( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, EAtomicType.INTEGER );
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения
   * @return long значение
   */
  protected long doAsLong( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, EAtomicType.INTEGER );
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения
   * @return float значение
   */
  protected float doAsFloat( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, EAtomicType.FLOATING );
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения
   * @return double значение
   */
  protected double doAsDouble( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, EAtomicType.FLOATING );
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения
   * @return String значение
   */
  protected String doAsString( int aIndex ) {
    return getValue( aIndex ).toString();
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения
   * @return {@link Object} значение
   * @param <T> тип возвращаемого значения
   */
  protected <T> T doAsValobj( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, EAtomicType.VALOBJ );
  }
}
