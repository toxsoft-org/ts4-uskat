package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences;

import static org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.IS5Resources.*;

import java.nio.ByteBuffer;
import java.sql.ResultSet;

import javax.persistence.MappedSuperclass;

import org.toxsoft.core.tslib.av.errors.AvTypeCastRtException;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceSyncBlob;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceSyncBlock;

/**
 * Блок хранения синхронных исторических данных s5-объекта .
 *
 * @param <BLOB_ARRAY> тип массива blob-a в котором хранятся значения блока
 * @param <BLOB> реализация blob-а используемого для хранения значений блока
 * @author mvk
 */
@MappedSuperclass
public abstract class S5HistDataSyncBlock<BLOB_ARRAY, BLOB extends S5SequenceSyncBlob<?, BLOB_ARRAY, ?>>
    extends S5SequenceSyncBlock<ITemporalAtomicValue, BLOB_ARRAY, BLOB>
    implements IHistDataBlock {

  private static final long serialVersionUID = 157157L;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5HistDataSyncBlock() {
  }

  /**
   * Конструктор
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aStartTime long метка (мсек с начала эпохи) начала данных в блоке
   * @param aBlob BLOB реализация blob-а в котором хранятся значения блока
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException неверная метка времени начала значений
   * @throws TsIllegalArgumentRtException описание данного должно представлять синхронное данное
   */
  protected S5HistDataSyncBlock( IParameterized aTypeInfo, Gwid aGwid, long aStartTime, BLOB aBlob ) {
    super( aTypeInfo, aGwid, aStartTime, aBlob );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  protected S5HistDataSyncBlock( ResultSet aResultSet ) {
    super( aResultSet );
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
  @Override
  abstract protected boolean doIsAssigned( int aIndex );

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения
   * @return boolean значение
   */
  @Override
  protected boolean doAsBool( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, "boolean" ); //$NON-NLS-1$
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения
   * @return int значение
   */
  @Override
  protected int doAsInt( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, "int" ); //$NON-NLS-1$
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения
   * @return long значение
   */
  @Override
  protected long doAsLong( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, "long" ); //$NON-NLS-1$
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения
   * @return float значение
   */
  @Override
  protected float doAsFloat( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, "float" ); //$NON-NLS-1$
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения
   * @return double значение
   */
  @Override
  protected double doAsDouble( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, "double" ); //$NON-NLS-1$
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения
   * @return String значение
   */
  @Override
  protected String doAsString( int aIndex ) {
    return getValue( aIndex ).toString();
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения
   * @return {@link ByteBuffer} значение
   */
  protected ByteBuffer doAsBuffer( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, "ByteBuffer" ); //$NON-NLS-1$
  }

  /**
   * Возвращает значение по индексу
   *
   * @param aIndex индекс значения
   * @return {@link Object} значение
   * @param <T> тип возвращаемого значения
   */
  @Override
  protected <T> T doAsValobj( int aIndex ) {
    throw new AvTypeCastRtException( ERR_CAST_VALUE, this, "valobj" ); //$NON-NLS-1$
  }
}
