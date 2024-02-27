package org.toxsoft.uskat.s5.server.sequences.impl;

import static org.toxsoft.core.tslib.bricks.strid.impl.StridUtils.*;
import static org.toxsoft.core.tslib.bricks.time.impl.TimeUtils.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.util.Arrays;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.bricks.validator.IValResList;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.bricks.validator.impl.ValResList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceBlockEdit;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceFactory;
import org.toxsoft.uskat.s5.server.sequences.maintenance.S5Partition;
import org.toxsoft.uskat.s5.utils.indexes.ILongKey;

/**
 * Блок хранения асинхронных данных.
 * <p>
 *
 * @author mvk
 * @param <V> тип значения последовательности
 * @param <BLOB_ARRAY> тип массива blob-a в котором хранятся значения блока
 * @param <BLOB> реализация blob-а используемого для хранения значений блока
 */
@MappedSuperclass
public abstract class S5SequenceAsyncBlock<V extends ITemporal<?>, BLOB_ARRAY, BLOB extends S5SequenceAsyncBlob<?, BLOB_ARRAY, ?>>
    extends S5SequenceBlock<V, BLOB_ARRAY, BLOB> {

  private static final long serialVersionUID = 157157L;

  /**
   * Ключ индекса доступа к значениям по метке времени
   */
  @Transient
  private transient ILongKey timeKey;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5SequenceAsyncBlock() {
    super();
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
  protected S5SequenceAsyncBlock( IParameterized aTypeInfo, Gwid aGwid, BLOB aBlob ) {
    super( aGwid, TsNullArgumentRtException.checkNull( aBlob ).timestamps()[0], aBlob );
    TsIllegalArgumentRtException.checkTrue( OP_IS_SYNC.getValue( aTypeInfo.params() ).asBool() );
    checkTimestamps( gwid(), aBlob );
    setValues( aBlob.values() );
  }

  /**
   * Создать блок из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  protected S5SequenceAsyncBlock( ResultSet aResultSet ) {
    super( aResultSet );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequenceBlock
  //
  @Override
  public boolean isSync() {
    return false;
  }

  @Override
  public long timestamp( int aIndex ) {
    long[] timestamps = blob().timestamps();
    if( aIndex < 0 || aIndex >= timestamps.length ) {
      Integer index = Integer.valueOf( aIndex );
      Integer length = Integer.valueOf( timestamps.length );
      throw new TsIllegalArgumentRtException( ERR_ASYNC_WRONG_TIMESTAMP_INDEX, index, length );
    }
    return timestamps[aIndex];
  }

  @Override
  public int firstByTime( long aTimestamp ) {
    // Проверка метки на попадание в диапазон
    checkTimestamp( aTimestamp );
    // Ключ доступа к значениям по метками времени
    ILongKey key = timeKey( true );
    // Индекс ближайшего элемента в котором будет установлено значение (индекс может быть восстановлен)
    int nearest = key.findIndex( aTimestamp );
    if( nearest < 0 ) {
      // Пустой блок
      return -1;
    }
    // Метки времени текущих значений блока
    long[] timestamps = blob().timestamps();
    // Признак того, что была найдена метка точно совпадающая с аргументом
    boolean foundEquals = false;
    // Перемещаем индекс к началу, пока время больше или равно метке
    for( int index = nearest; index >= 0; index-- ) {
      // TODO: поиск ошибки ArrayIndexOutOfBoundsException
      try {
        long timestamp = timestamps[index];
        if( timestamp == aTimestamp ) {
          // Нашли точно совпадающую метку
          foundEquals = true;
        }
        if( timestamp < aTimestamp ) {
          if( foundEquals ) {
            // Была найдена точно совпадающая метка. Откат к ней
            nearest++;
          }
          break;
        }
      }
      catch( Throwable e ) {
        throw new TsInternalErrorRtException( e,
            "ЛОВУШКА: aTimestamp = %s, values.size = %d, timestamps.size = %d, nearest = %d, index = %d, watermark = %d. Причина  : %s", //$NON-NLS-1$
            TimeUtils.timestampToString( aTimestamp ), Long.valueOf( size() ), Long.valueOf( timestamps.length ),
            Long.valueOf( nearest ), Long.valueOf( index ), Integer.valueOf( key.watermark() ), cause( e ) );
      }
      nearest--;
    }
    return (nearest < 0 ? 0 : nearest);
  }

  @Override
  public int lastByTime( long aTimestamp ) {
    // Проверка метки на попадание в диапазон
    checkTimestamp( aTimestamp );
    // Индекс ближайшего элемента в котором будет установлено значение (индекс может быть восстановлен)
    int nearest = timeKey( true ).findIndex( aTimestamp );
    if( nearest < 0 ) {
      // Пустой блок
      return -1;
    }
    // Метки времени текущих значений блока
    long[] timestamps = blob().timestamps();
    // Признак того, что была найдена метка точно совпадающая с аргументом
    boolean foundEquals = false;
    // Перемещаем индекс к концу, пока время меньше или равно метке
    for( int index = nearest, n = timestamps.length; index < n; index++ ) {
      long timestamp = timestamps[index];
      if( timestamp == aTimestamp ) {
        // Нашли точно совпадающую метку
        foundEquals = true;
      }
      if( timestamp > aTimestamp ) {
        if( foundEquals ) {
          // Была найдена точно совпадающая метка. Откат к ней
          nearest--;
        }
        break;
      }
      nearest++;
    }
    return (nearest >= timestamps.length ? nearest - 1 : nearest);
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов
  //
  @Override
  protected final IS5SequenceBlockEdit<V> doCreateBlock( IParameterized aTypeInfo, long aStartTime, long aEndTime,
      int aStartIndex, int aEndIndex, BLOB_ARRAY aValues ) {
    // Метки времени текущих значений блока
    long[] timestamps = blob().timestamps();
    // Получаем часть меток времени блока или пустой массив (это допустимо)
    long[] newTimestamps = copyValuesOfRange( timestamps, aStartIndex, aEndIndex );
    // Создание конкретного блока
    return doCreateBlock( aTypeInfo, newTimestamps, aValues );
  }

  @Override
  protected final void doSetEndTime( long aEndTime, int aEditStart, int aEditEnd, int aSrcStart, int aSrcCount,
      BLOB_ARRAY aValues ) {
    // Метки времени текущих значений блока
    long[] timestamps = blob().timestamps();
    // Получаем часть меток времени блока или ...
    long[] newTimestamps = editArray( timestamps, aEditStart, aEditEnd, timestamps, aSrcStart, aSrcCount );
    // TODO: иногда в newTimestamps попадают метки времени за границей блока (endTime + 1(DDT)). РАЗОБРАТЬСЯ как это
    // может быть!!! В этом случае checkTimestamps подымет исключение сохранив целостность блока, но новые данные
    // будут потеряны

    // Проверка меток времени на вхождение в диапазон блока и их порядка возрастания.
    checkTimestamps( gwid(), startTime(), aEndTime, newTimestamps );
    // Установка новых меток времени
    blob().setTimestamps( newTimestamps );
    // Устанавливаем новые значения
    setValues( aValues );
    // Сброс старого индекса
    timeKey = null;
  }

  @Override
  protected void doSetValues( BLOB_ARRAY aValues ) {
    // Проверка метки начала значений в блоке
    if( timestamp( 0 ) != startTime() ) {
      String tp = timestampToString( timestamp( 0 ) );
      String st = timestampToString( startTime() );
      logger.error( ERR_WRONG_STARTTIME, this, tp, st );
    }
    // Вызов родительского метода
    super.doSetValues( aValues );
  }

  @SuppressWarnings( "unchecked" )
  @Override
  protected final void doUpdate( S5SequenceBlock<V, ?, ?> aSource ) {
    // Копирование меток времени
    blob().setTimestamps( ((S5SequenceAsyncBlob<?, BLOB_ARRAY, ?>)aSource.blob()).timestamps() );
    // Сброс старого индекса
    timeKey = null;
  }

  @Override
  public IValResList doValidation( IParameterized aTypeInfo ) {
    ValResList retValue = new ValResList();
    retValue.addValResList( super.doValidation( aTypeInfo ) );
    if( !retValue.isOk() ) {
      return retValue;
    }
    int size = size();
    // Идентификатор данного
    Gwid gwid = gwid();
    // Метки времени текущих значений блока
    long[] timestamps = blob().timestamps();
    if( timestamps.length != size ) {
      // Размер массива меток времени не соотвествует размеру массиву значений
      Integer s = Integer.valueOf( size );
      Integer l = Integer.valueOf( timestamps.length );
      retValue.add( ValidationResult.warn( ERR_WRONG_TIMESTAMPS_SIZE, this, l, s ) );
      // Попытка исправить ошибку
      boolean success = tryRestoreTimestamps( retValue );
      if( !success ) {
        // Невозможно исправить ошибку. Ранний выход, чтобы не было ошибок адресации по неверному массиву меток
        return retValue;
      }
      // Ошибка поправлена
    }
    long firstTimeStamp = timestamps[0];
    long lastTimeStamp = timestamps[size - 1];
    if( firstTimeStamp < startTime() || lastTimeStamp > endTime() ) {
      // Метки первого и/или последнего значений не попадают в интервал блока (неисправимая ошибка)
      String ft = TimeUtils.timestampToString( firstTimeStamp );
      String lt = TimeUtils.timestampToString( lastTimeStamp );
      retValue.add( ValidationResult.error( ERR_VALIDATION_WRONG_TIMESTAMPS, this, ft, lt ) );
    }
    try {
      checkTimestamps( gwid, startTime(), endTime(), timestamps );
    }
    catch( TsIllegalArgumentRtException e ) {
      // Нарушение порядка следования меток времени значений в блоке (неисправимая ошибка)
      retValue.add( ValidationResult.error( ERR_VALIDATION_CHECK_TIMESTAMPS, this, cause( e ) ) );
    }
    // TODO:
    return retValue;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public int doUniteBlocks( IS5SequenceFactory<V> aFactory, IList<IS5SequenceBlockEdit<V>> aBlocks, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aFactory, aBlocks, aLogger );
    if( aBlocks.size() == 0 ) {
      // Не с чем объединять
      return 0;
    }
    // Новая метка времени завершения данных в блоке
    long newEndTime = endTime();
    // Идентификатор данного
    Gwid gwid = gwid();
    // Тип данного
    IParameterized typeInfo = aFactory.typeInfo( gwid );
    // Новый размер блока
    int newSize = size();
    // Максимальное количество значений в дефрагментированном блоке
    int blockSizeMax = OP_BLOCK_SIZE_MAX.getValue( typeInfo.params() ).asInt();

    // Имя таблицы хранения блоков
    String blockTableName = getLast( OP_BLOCK_IMPL_CLASS.getValue( typeInfo.params() ).asString() );
    // Глубина хранения значений в таблице
    int depth = aFactory.getTableDepth( blockTableName );
    // Максимальное время завершения данных в блоке с учетом механизма хранения блоков в разделах таблиц
    long nextPartitionTime = S5Partition.calcPartitionEndTime( newEndTime, depth );

    // Количество объединенных блоков
    int unionedCount = 0;
    for( int index = 0, n = aBlocks.size(); index < n; index++ ) {
      S5SequenceAsyncBlock<V, BLOB_ARRAY, BLOB> block = (S5SequenceAsyncBlock<V, BLOB_ARRAY, BLOB>)aBlocks.get( index );
      long blockStartTime = block.startTime();
      long blockEndTime = block.endTime();
      long[] blockTimestamps = block.timestamps();
      int blockSize = block.size();
      if( blockSize == 0 ) {
        // Блок пустой
        continue;
      }
      if( blockEndTime >= nextPartitionTime ) {
        // Завершение блока попадает в другой раздел таблицы хранения блоков. Объединение с ним невозможно так как
        // невозможно обеспечить гарантию глубины хранения значений в БД
        break;
      }
      if( newSize + blockSize >= blockSizeMax ) {
        // Блок не может быть объединен, так как будет превышен максимальный размер блока
        break;
      }
      // Проверка меток времени блока и его значений на вхождение в диапазон блока и их порядка возрастания.
      checkTimestamps( gwid, blockStartTime, blockEndTime, blockTimestamps );
      if( blockStartTime < newEndTime ) {
        // Блоки в списке должны идти по возрастанию времени
        String et = timestampToString( newEndTime );
        String st = timestampToString( blockStartTime );
        throw new TsIllegalArgumentRtException( ERR_ASYNC_WRONG_SEQUENCE, gwid, et, st );
      }
      unionedCount++;
      newEndTime = blockEndTime;
      newSize += blockSize;
    }
    if( unionedCount == 0 ) {
      // Невозможно объединить блок ни с одним из блоков. Установка метки того, что данные блока дефрагментированы
      setUnionMark();
      return unionedCount;
    }
    // Метки времени текущих значений блока
    long[] timestamps = blob().timestamps();
    // Итоговый буфер значений
    BLOB_ARRAY newValues = aFactory.createValueArray( typeInfo, newSize );
    long[] newTimestamps = new long[newSize];
    // Копирование старых значений текущего блока
    int size = size();
    System.arraycopy( blob().values(), 0, newValues, 0, size );
    System.arraycopy( timestamps, 0, newTimestamps, 0, size );
    // Копирование значений объединяемых блоков
    int destPosition = size;
    for( int index = 0, n = unionedCount; index < n; index++ ) {
      S5SequenceAsyncBlock<V, BLOB_ARRAY, BLOB> block = (S5SequenceAsyncBlock<V, BLOB_ARRAY, BLOB>)aBlocks.get( index );
      BLOB_ARRAY values = block.blob().values();
      int blockSize = block.size();
      System.arraycopy( values, 0, newValues, destPosition, blockSize );
      System.arraycopy( block.timestamps(), 0, newTimestamps, destPosition, blockSize );
      destPosition += blockSize;
    }
    // Проверка меток времени на вхождение в диапазон блока и их порядка возрастания.
    checkTimestamps( gwid, startTime(), newEndTime, newTimestamps );
    // Установка меток времени их значений
    blob().setTimestamps( newTimestamps );
    // Сброс старого индекса
    timeKey = null;
    setValues( newValues );
    // Установка метки того, что данные блока дефрагментированы
    setUnionMark();
    // Возвращаем количество блоков с которыми произошло объединение
    return unionedCount;
  }

  // ------------------------------------------------------------------------------------
  // Методы для реализации наследниками
  //
  /**
   * Создает новый блок с указанными параметрами времени и значениями
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aTimestamps long[] метки времени значений
   * @param aValues BLOB_ARRAY массив значений блока
   * @return {@link IS5SequenceBlockEdit} созданный блок
   */
  protected abstract IS5SequenceBlockEdit<V> doCreateBlock( IParameterized aTypeInfo, long[] aTimestamps,
      BLOB_ARRAY aValues );

  /**
   * Создать индекс доступа к значениям по меткам врмени
   *
   * @param aRestore boolean <b>true</b>восстановить индекс;<b>false</b> создать новый индекс
   * @return {@link ILongKey} ключ индекса доступа к значениям блока
   */
  protected abstract ILongKey doTimestampIndex( boolean aRestore );

  // ------------------------------------------------------------------------------------
  // API для наследников
  //
  /**
   * Возвращает ключ индекса доступа произвольных значений по их метке времени
   *
   * @param aRestore boolean <b>true</b>если индекса нет, то восстановить его;<b>false</b> если индекса нет, то создать
   *          новый
   * @return {@link ILongKey} ключ индекса доступа к значениям
   */
  protected final ILongKey timeKey( boolean aRestore ) {
    if( timeKey == null ) {
      // Создаем или восстанавливаем индекс без проверки (по соображениям производительности)
      timeKey = doTimestampIndex( aRestore );
    }
    return timeKey;
  }

  /**
   * Возвращает метки времени значений блока
   *
   * @return long[] метки времени(мсек с начала эпохи)
   */
  protected final long[] timestamps() {
    return blob().timestamps();
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
  /**
   * Проводит проверку меток времени blob асинхронных значений
   *
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aBlob BLOB_ARRAY проверяемый blob
   * @return long метка времени (мсек с начала эпохи) начала значений
   * @param <BLOB_ARRAY> тип массива blob-a в котором хранятся значения блока
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException количество меток времени не соответствует количеству значений
   * @throws TsIllegalArgumentRtException метки времени не отсортированы в порядке возрастания
   */
  private static <BLOB_ARRAY> long checkTimestamps( Gwid aGwid, S5SequenceAsyncBlob<?, BLOB_ARRAY, ?> aBlob ) {
    TsNullArgumentRtException.checkNulls( aGwid, aBlob );
    BLOB_ARRAY values = aBlob.values();
    long[] timestamps = aBlob.timestamps();
    // Проверка количества меток времени
    TsIllegalArgumentRtException.checkFalse( timestamps.length > 0 );
    TsIllegalArgumentRtException.checkFalse( timestamps.length == Array.getLength( values ) );
    // Проверка меток времени на порядок возрастания.
    checkTimestamps( aGwid, timestamps[0], timestamps[timestamps.length - 1], timestamps );
    return timestamps[0];
  }

  /**
   * Проверка меток времени на попадание в указанный диапазон и порядок возрастания
   *
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aStartTime long время (мсек с начала эпохи) начала диапазона
   * @param aEndTime long время (мсек с начала эпохи) завершения диапазона
   * @param aTimestamps long[] массив меток времени
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException недпостимый интервал времени
   * @throws TsIllegalArgumentRtException метка времени находится вне блока
   */
  private static void checkTimestamps( Gwid aGwid, long aStartTime, long aEndTime, long[] aTimestamps ) {
    TsNullArgumentRtException.checkNulls( aGwid, aTimestamps );
    checkIntervalArgs( aStartTime, aEndTime );
    long minTime = aStartTime;
    for( int index = 0, n = aTimestamps.length; index < n; index++ ) {
      long time = aTimestamps[index];
      if( time < aStartTime || aEndTime < time ) {
        // Метка вне диапазона блока
        String t = timestampToString( time );
        String st = timestampToString( aStartTime );
        String et = timestampToString( aEndTime );
        throw new TsIllegalArgumentRtException( ERR_ASYNC_OUT, aGwid, t, st, et );
      }
      if( time < minTime ) {
        // Нарушение порядка возрастания
        String t = timestampToString( time );
        String ms = timestampToString( minTime );
        throw new TsIllegalArgumentRtException( ERR_ASYNC_WRONG_TIMES_ORDER, aGwid, t, ms );
      }
      minTime = time;
    }
  }

  /**
   * Выполняет попытку восстановления массива меток значений
   *
   * @param aValidationResult {@link ValResList} редактируемый список результатов обработки валидации
   * @return {@link Boolean} <b>true</b> массив восстановлен;<b>false</b> неустранимая ошибка
   * @throws TsNullArgumentRtException аргумент = null
   */
  private boolean tryRestoreTimestamps( ValResList aValidationResult ) {
    TsNullArgumentRtException.checkNull( aValidationResult );
    // Метки времени текущих значений блока
    long[] timestamps = blob().timestamps();
    if( timestamps == null ) {
      aValidationResult.add( ValidationResult.error( ERR_RESTORE_TIMESTAMPS, this, ERR_NULL_TIMESTAMPS ) );
      return false;
    }
    if( timestamps.length == size() ) {
      return true;
    }
    if( size() == 0 ) {
      // Частный случай, пустой блок
      blob().setTimestamps( new long[0] );
      timeKey = null;
      aValidationResult.add( ValidationResult.warn( ERR_RESTORE_TIMESTAMPS_SUCCESS, this ) );
      return true;
    }
    try {
      long[] newTimestamps = Arrays.copyOf( timestamps, size() );
      // Заполняем все неопределенные метки (Arrays.copyOf устанавливает их в 0) меткой времени конца блока
      for( int index = size(); index < newTimestamps.length; index++ ) {
        newTimestamps[index] = endTime();
      }
      // Проверка меток времени на вхождение в диапазон блока и их порядка возрастания.
      checkTimestamps( gwid(), startTime(), endTime(), newTimestamps );
      blob().setTimestamps( newTimestamps );
      aValidationResult.add( ValidationResult.warn( ERR_RESTORE_TIMESTAMPS_SUCCESS, this ) );
      timeKey = null;
      return true;
    }
    catch( Throwable e ) {
      aValidationResult.add( ValidationResult.warn( ERR_RESTORE_TIMESTAMPS, this, cause( e ) ) );
      return false;
    }
  }

}
