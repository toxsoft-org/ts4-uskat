package org.toxsoft.uskat.s5.server.sequences.impl;

import static java.lang.String.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;

import java.lang.reflect.Array;
import java.sql.ResultSet;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Абстрактная реализация хранения данных блоков в blob с метками времени.
 *
 * @author mvk
 * @param <BLOCK> блок данных которому принадлежат значения blob
 * @param <BLOB_ARRAY> тип массива используемый для хранения значений, например double[]
 * @param <BLOB_ARRAY_HOLDER> тип объекта хранящий массив значений
 */
@MappedSuperclass
public class S5SequenceAsyncBlob<BLOCK extends S5SequenceAsyncBlock<?, ?, ?>, BLOB_ARRAY, BLOB_ARRAY_HOLDER>
    extends S5SequenceBlob<BLOCK, BLOB_ARRAY, BLOB_ARRAY_HOLDER> {

  private static final long serialVersionUID = 157157L;

  /**
   * Поле таблицы: первая метка времени (мсек с начала эпохи) хранимая в blob
   */
  public static final String FIELD_TIMES_START = "_timesStart"; //$NON-NLS-1$

  /**
   * Поле таблицы: количество байт используемых для хранения смещения метки времени в blob
   */
  public static final String FIELD_TIMES_UNIT = "_timesUnit"; //$NON-NLS-1$

  /**
   * Поле таблицы: смещения меток времени(мсек с начала эпохи) от начала блока в сериализованном виде
   */
  public static final String FIELD_TIMES_OFFSETS = "_timesOffsets"; //$NON-NLS-1$

  /**
   * Первая метка времени (мсек с начала эпохи) хранимая в blob
   */
  @Column( name = FIELD_TIMES_START, //
      insertable = true,
      updatable = true,
      nullable = false,
      unique = false,
      length = Integer.MAX_VALUE )
  private Long _timesStart;

  /**
   * Количество байт используемых для хранения смещения метки времени в blob
   */
  @Column( name = FIELD_TIMES_UNIT, //
      insertable = true,
      updatable = true,
      nullable = false,
      unique = false,
      length = 1 )
  private Byte _timesUnit;

  /**
   * Смещения меток времени(мсек с начала эпохи) от начала блока в сериализованном виде
   */
  @Column( name = FIELD_TIMES_OFFSETS, //
      insertable = true,
      updatable = true,
      nullable = false,
      unique = false,
      length = Integer.MAX_VALUE )
  private byte[] _timesOffsets;

  /**
   * Сформированные метки времени
   */
  private transient long[] timestamps;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5SequenceAsyncBlob() {
  }

  /**
   * Конструктор blob для нового блока (идентификатор формируется автоматически)
   *
   * @param aTimestamps long[] массив меток времени
   * @param aValues BLOB_ARRAY массив значений
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5SequenceAsyncBlob( long[] aTimestamps, BLOB_ARRAY aValues ) {
    super( aValues, aTimestamps[aTimestamps.length - 1] );
    setTimestamps( aTimestamps );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  protected S5SequenceAsyncBlob( ResultSet aResultSet ) {
    super( aResultSet );
    try {
      _timesStart = Long.valueOf( aResultSet.getLong( FIELD_TIMES_START ) );
      _timesUnit = Byte.valueOf( aResultSet.getByte( FIELD_TIMES_UNIT ) );
      _timesOffsets = aResultSet.getBytes( FIELD_TIMES_OFFSETS );
    }
    catch( Throwable e ) {
      // Неожиданная ошибка создания blob асинхронных значений из курсора dbms
      throw new TsInternalErrorRtException( e, ERR_CREATE_ASYNC_BLOB_FROM_CURSOR, cause( e ) );
    }
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Возвращает метки времени значений блока
   *
   * @return long[] массив меток времени
   */
  long[] timestamps() {
    if( timestamps != null ) {
      return timestamps;
    }
    long startTime = _timesStart.longValue();
    int count = count();
    byte unit = _timesUnit.byteValue();
    timestamps = new long[count];
    for( int timestampIndex = 0; timestampIndex < count; timestampIndex++ ) {
      int unitBase = timestampIndex * unit;
      timestamps[timestampIndex] = 0;
      for( int unitIndex = 0; unitIndex < unit; unitIndex++ ) {
        timestamps[timestampIndex] = timestamps[timestampIndex] << 8;
        // Преобразование (byte)(timestamp & 0xFF) формирует число в дополнительном коде. Делаем обратное преобразование
        int unsignedByte = _timesOffsets[unitBase + unitIndex];
        if( unsignedByte < 0 ) {
          unsignedByte = 256 + unsignedByte;
        }
        timestamps[timestampIndex] |= unsignedByte;
      }
      timestamps[timestampIndex] += startTime;
    }
    return timestamps;
  }

  /**
   * Установить метки времени значений блока
   *
   * @param aTimestamps long[] метки времени значений
   */
  void setTimestamps( long[] aTimestamps ) {
    TsNullArgumentRtException.checkNull( aTimestamps );
    int count = aTimestamps.length;
    long startTime = aTimestamps[0];
    long endTime = aTimestamps[count - 1];
    byte unit = calcUnit( startTime, endTime );
    _timesStart = Long.valueOf( startTime );
    _timesUnit = Byte.valueOf( unit );
    _timesOffsets = new byte[count * unit];
    for( int timestampIndex = 0; timestampIndex < count; timestampIndex++ ) {
      long timestamp = aTimestamps[timestampIndex] - startTime;
      int unitBase = timestampIndex * unit;
      for( int unitIndex = unit - 1; unitIndex >= 0; unitIndex-- ) {
        // Внимание: преобразование (byte)(timestamp & 0xFF) формирует число в дополнительном коде
        _timesOffsets[unitBase + unitIndex] = (byte)(timestamp & 0xFF);
        timestamp = timestamp >> 8;
      }
    }
    timestamps = aTimestamps;
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    BLOB_ARRAY v = values();
    String vs = (v != null ? String.valueOf( Array.getLength( v ) ) : null);
    String ts = (_timesOffsets != null ? String.valueOf( count() ) : null);
    String to = (_timesOffsets != null ? String.valueOf( _timesOffsets.length ) : null);
    return format( BLOB_ASYNC_TO_STRING_FORMAT, getClass().getSimpleName(), id(), vs, ts, to );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5SequenceBlob
  //
  @Override
  protected IStringMapEdit<Object> doInsertQueryParams() {
    IStringMapEdit<Object> retValue = super.doInsertQueryParams();
    retValue.put( FIELD_TIMES_START, _timesStart );
    retValue.put( FIELD_TIMES_UNIT, _timesUnit );
    retValue.put( FIELD_TIMES_OFFSETS, _timesOffsets );
    return retValue;
  }

  @Override
  protected IStringMapEdit<Object> doUpdateQueryParams() {
    IStringMapEdit<Object> retValue = super.doUpdateQueryParams();
    retValue.put( FIELD_TIMES_START, _timesStart );
    retValue.put( FIELD_TIMES_UNIT, _timesUnit );
    retValue.put( FIELD_TIMES_OFFSETS, _timesOffsets );
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
  /**
   * Возвращает количество байтов используемых для хранения метки времени в blob
   *
   * @param aStartTime long метка времени первого значения в blob
   * @param aEndTime long метка времени последнего значения в blob
   * @return byte количество байтов
   * @throws TsIllegalArgumentRtException aEndTime < aStartTime
   */
  private static byte calcUnit( long aStartTime, long aEndTime ) {
    TsIllegalArgumentRtException.checkFalse( aEndTime >= aStartTime );
    long duration = aEndTime - aStartTime;
    if( duration > 0xFFFFFFFFFFFFFFL ) {
      return 8;
    }
    if( duration > 0xFFFFFFFFFFFFL ) {
      return 7;
    }
    if( duration > 0xFFFFFFFFFFL ) {
      return 6;
    }
    if( duration > 0xFFFFFFFFL ) {
      return 5;
    }
    if( duration > 0xFFFFFFL ) {
      return 4;
    }
    if( duration > 0xFFFFL ) {
      return 3;
    }
    if( duration > 0xFFL ) {
      return 2;
    }
    return 1;
  }

  /**
   * Возвращает количество меток времени хранимых в blob
   *
   * @return int количество меток времени
   */
  private int count() {
    return _timesOffsets.length / _timesUnit.byteValue();
  }
}
