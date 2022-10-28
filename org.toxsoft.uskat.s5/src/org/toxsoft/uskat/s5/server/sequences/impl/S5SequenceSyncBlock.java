package org.toxsoft.uskat.s5.server.sequences.impl;

import static org.toxsoft.core.tslib.bricks.time.impl.TimeUtils.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;

import java.beans.*;
import java.lang.reflect.Array;
import java.sql.*;

import javax.persistence.*;

import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.*;
import org.toxsoft.uskat.s5.server.sequences.*;

/**
 * Блок хранения синхронных данных.
 * <p>
 *
 * @author mvk
 * @param <V> тип значения последовательности
 * @param <BLOB_ARRAY> тип массива blob-a в котором хранятся значения блока
 * @param <BLOB> реализация blob-а используемого для хранения значений блока
 */
@MappedSuperclass
public abstract class S5SequenceSyncBlock<V extends ITemporal<?>, BLOB_ARRAY, BLOB extends S5SequenceSyncBlob<?, BLOB_ARRAY, ?>>
    extends S5SequenceBlock<V, BLOB_ARRAY, BLOB> {

  private static final long serialVersionUID = 157157L;

  /**
   * Поле таблицы: интервал синхронных данных (мсек)
   */
  public static final String FIELD_SYNC_DATA_DELTA = "syncDataDelta"; //$NON-NLS-1$

  /**
   * Интервал времени (мсек) между отсчетами. То же самое что и {@link IS5SequenceHardConstants#OP_SYNC_DT}
   */
  @Column( name = FIELD_SYNC_DATA_DELTA, //
      insertable = true,
      updatable = true,
      nullable = false,
      unique = false )
  private Long syncDataDelta;

  @Transient
  private transient int importIndex = -1;

  @Transient
  private transient boolean hasImport = false;

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5SequenceSyncBlock() {
    super();
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
  protected S5SequenceSyncBlock( IParameterized aTypeInfo, Gwid aGwid, long aStartTime, BLOB aBlob ) {
    super( aGwid, aStartTime, aBlob );
    TsIllegalArgumentRtException.checkFalse( OP_IS_SYNC.getValue( aTypeInfo.params() ).asBool() );
    syncDataDelta = Long.valueOf( OP_SYNC_DT.getValue( aTypeInfo.params() ).asLong() );
    setValues( aBlob.values() );
  }

  /**
   * Конструктор блока из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  protected S5SequenceSyncBlock( ResultSet aResultSet ) {
    super( aResultSet );
    try {
      syncDataDelta = Long.valueOf( aResultSet.getLong( FIELD_SYNC_DATA_DELTA ) );
    }
    catch( Throwable e ) {
      throw new TsInternalErrorRtException( e );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISequenceBlock
  //
  @Override
  public boolean isSync() {
    return true;
  }

  @Override
  public long timestamp( int aIndex ) {
    if( aIndex < 0 || aIndex >= size() ) {
      Integer index = Integer.valueOf( aIndex );
      Integer length = Integer.valueOf( size() );
      throw new TsIllegalArgumentRtException( ERR_SYNC_WRONG_TIMESTAMP_INDEX, index, length );
    }
    return startTime() + aIndex * syncDataDelta.longValue();
  }

  @Override
  public int firstByTime( long aTimestamp ) {
    // Возвращаем индекс метки времени в блоке
    return getIndexByTime( aTimestamp );
  }

  @Override
  public int lastByTime( long aTimestamp ) {
    // Возвращаем индекс метки времени в блоке
    return getIndexByTime( aTimestamp );
  }

  @Override
  public final void setImportTime( long aTimestamp ) {
    // Слот (мсек)
    long sdd = syncDataDelta();
    // Метка времени выравненная по слоту блока
    long timestamp = ((aTimestamp / sdd) * sdd);
    importIndex = firstByTime( timestamp );
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
  // Реализация шаблонных методов
  //
  @Override
  protected final ISequenceBlockEdit<V> doCreateBlock( IParameterized aTypeInfo, long aStartTime, long aEndTime,
      int aStartIndex, int aEndIndex, BLOB_ARRAY aValues ) {
    // Создание конкретного блока
    return doCreateBlock( aTypeInfo, aStartTime, aValues );
  }

  @Override
  protected final void doSetEndTime( long aEndTime, int aEditStart, int aEditEnd, int aSrcStart, int aSrcCount,
      BLOB_ARRAY aValues ) {
    // Устанавливаем новые значения
    setValues( aValues );
  }

  @Override
  protected void doSetValues( BLOB_ARRAY aValues ) {
    // Проверка выравнивания по слоту
    checkAlignByDDT( syncDataDelta.longValue(), startTime() );
    // Вызов родительского метода
    super.doSetValues( aValues );
  }

  @Override
  protected final void doUpdate( S5SequenceBlock<V, ?, ?> aSource ) {
    // nop
  }

  @Override
  public IValResList doValidation( IParameterized aTypeInfo ) {
    // FIXME: FIXED goga занести все сразу одним методом
    ValResList retValue = new ValResList( super.doValidation( aTypeInfo ).results() );
    // for( ValidationResult vr : super.doValidation( aTypeInfo ).results() ) {
    // retValue.add( vr );
    // }
    if( !retValue.isOk() ) {
      return retValue;
    }
    long blockStartTime = startTime();
    long blockEndTime = endTime();
    long blockSyncDelta = syncDataDelta.longValue();
    int blockSize = size();
    // Проверка количества значений в блоке.
    long expectedSize = (blockEndTime - blockStartTime + blockSyncDelta) / blockSyncDelta;
    if( expectedSize != blockSize ) {
      Long expected = Long.valueOf( expectedSize );
      Long received = Long.valueOf( blockSize );
      retValue.add( ValidationResult.error( ERR_VALIDATION_SYNC_WRONG_SIZE, this, expected, received ) );
    }
    // TODO:
    return retValue;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  protected int doUniteBlocks( ISequenceFactory<V> aFactory, IList<ISequenceBlockEdit<V>> aBlocks, ILogger aLogger ) {
    if( aBlocks.size() == 0 ) {
      // Не с чем объединять
      return 0;
    }
    IParameterized typeInfo = aFactory.typeInfo( gwid() );
    long newEndTime = endTime();
    long syncDelta = syncDataDelta.longValue();
    // Новый размер блока
    int newSize = size();
    // Двойной максимальный размер блока
    int doubleMaxSize = 2 * OP_BLOCK_SIZE_MAX.getValue( typeInfo.params() ).asInt();
    // Индекс блока с которого необходимо проводить объединение
    int unionFrom = 0;
    // Количество блоков годных для объединения
    int readyUnionCount = 0;
    // Количество объединенных блоков
    int unionedCount = 0;
    for( int index = 0, n = aBlocks.size(); index < n; index++ ) {
      S5SequenceSyncBlock<V, BLOB_ARRAY, BLOB> block = ((S5SequenceSyncBlock<V, BLOB_ARRAY, BLOB>)aBlocks.get( index ));
      final long blockStartTime = block.startTime();
      final long blockEndTime = block.endTime();
      final long blockSyncDelta = block.syncDataDelta.longValue();
      final int blockSize = block.size();
      if( blockSize == 0 ) {
        // Блок пустой
        continue;
      }
      if( newSize + blockSize >= doubleMaxSize ) {
        // Блок не может быть объединен, так как будет вдвое превышен максимальный размер блока
        break;
      }
      // Проверка диапазона времени. Окончание данных в блоке
      TsIllegalArgumentRtException.checkTrue( blockStartTime > blockEndTime );
      if( blockStartTime < newEndTime ) {
        // Блоки в списке должны идти по возрастанию времени
        String et = timestampToString( newEndTime );
        String st = timestampToString( blockStartTime );
        throw new TsIllegalArgumentRtException( ERR_SYNC_WRONG_SEQUENCE, gwid(), et, st );
      }
      // Проверка количества значений в блоке.
      long expectedSize = (blockEndTime - blockStartTime + blockSyncDelta) / blockSyncDelta;
      if( expectedSize != blockSize ) {
        Long expectedSizeL = Long.valueOf( expectedSize );
        Long realSizeL = Long.valueOf( blockSize );
        throw new TsIllegalArgumentRtException( ERR_SYNC_WRONG_SIZE, gwid(), expectedSizeL, realSizeL );
      }
      if( blockSyncDelta != syncDelta ) {
        // Неэффективное хранение: изменяется интервал синхронных значений. Дальнейшее объединение невозможно
        Long dd = Long.valueOf( syncDelta );
        Long ndd = Long.valueOf( blockSyncDelta );
        String lastEndTime = timestampToString( newEndTime );
        String nextStartTime = timestampToString( blockStartTime );
        aLogger.warning( ERR_SYNC_DELTA_DIFFERENT, this, dd, ndd, lastEndTime, nextStartTime );
        break;
      }
      // TODO: WORKAROUND mvk 2018-08-09 ошибка целостности последовательности данных
      // На tm проявилась следующая ситуация:
      // - Ошибка дефрагментации(union) СИНХРОННЫХ float-значений (приборы)
      // - В базе два соседних блока.
      // -- Первый: ~15 значений (count). Интервал с t0 по t1 включительно. t0 < t1.
      // -- Второй: 1 значение (count). Интервал с t1 по t1 включительно.
      // Таким образом, происходит "наезд" второго блока (count=1), на хвост первого (count =~15).
      boolean hasWorkarroundCase =
          (index > 0 && blockSize == 1 && aBlocks.get( index - 1 ).endTime() == blockStartTime);
      // Метка времени следующего ожидаемого значения
      long expectedStartTime = newEndTime + blockSyncDelta;
      if( expectedStartTime > blockStartTime && !hasWorkarroundCase ) {
        // Ошибка, найдены неупорядочные блоки
        String pet = timestampToString( newEndTime );
        String nst = timestampToString( blockStartTime );
        throw new TsInternalErrorRtException( ERR_SYNC_WRONG_SEQUENCE, gwid(), pet, nst );
      }
      if( expectedStartTime < blockStartTime ) {
        // Начало нового блока не соответствует ожидаемому. Определяем количество добавляемых NULL_SESSION значений
        int nullValueCount = (int)((blockStartTime - expectedStartTime) / blockSyncDelta);
        // Определяем можно ли дополнить блок null-значениями, чтобы потом в него добавить значения другого блока
        if( newSize + nullValueCount + blockSize >= doubleMaxSize ) {
          // Добавить null-значения невозможно (двойное превышение размера блока)
          break;
        }
        // Исходнные значения для дополнения null-значениями
        BLOB_ARRAY sourceValues = blob().values();
        if( readyUnionCount > 0 ) {
          // Добавление значений всех блоков которые уже готовы к объединению
          sourceValues = addBlocksValues( aFactory, typeInfo, sourceValues, aBlocks, unionFrom, readyUnionCount );
          // Фискация того, что значения блоков были скопированы
          unionFrom += readyUnionCount;
          readyUnionCount = 0;
        }
        // Добавление null-значений
        BLOB_ARRAY newValues = addNullValues( aFactory, typeInfo, sourceValues, nullValueCount );
        // Время завершения блока
        newEndTime += nullValueCount * blockSyncDelta;
        // Установка новых значений блока
        setValues( newValues );
        // Журнал: дефрагментация хранения синхронных значений данных. Добавленных null-значений
        aLogger.warning( MSG_SYNC_ADD_NULLS, this, Long.valueOf( nullValueCount ), Long.valueOf( size() ) );
      }
      unionedCount++;
      readyUnionCount++;
      newEndTime = blockEndTime;
      newSize += blockSize;
    }
    if( readyUnionCount == 0 ) {
      // Нет блоков с которыми можно провести объединение
      return unionedCount;
    }
    // Формирование нового массива значений блока
    BLOB_ARRAY newValues = addBlocksValues( aFactory, typeInfo, blob().values(), aBlocks, unionFrom, readyUnionCount );
    // Установка новых значений блока
    setValues( newValues );
    // Возвращаем количество блоков с которыми произошло объединение
    return unionedCount;
  }

  @Override
  protected IStringMapEdit<Object> doInsertQueryParams() {
    IStringMapEdit<Object> retValue = super.doInsertQueryParams();
    retValue.put( FIELD_SYNC_DATA_DELTA, syncDataDelta );
    return retValue;
  }

  @Override
  protected IStringMapEdit<Object> doUpdateQueryParams() {
    IStringMapEdit<Object> retValue = super.doUpdateQueryParams();
    retValue.put( FIELD_SYNC_DATA_DELTA, syncDataDelta );
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // API для наследников
  //
  /**
   * Возвращает интервал времени (мсек) между отсчетами (размер слота синхронного данного). То же самое что и
   * {@link IS5SequenceHardConstants#OP_SYNC_DT}
   *
   * @return long интервал времени (мсек)
   */
  protected final long syncDataDelta() {
    return syncDataDelta.longValue();
  }

  // ------------------------------------------------------------------------------------
  // Методы для реализации наследниками
  //
  /**
   * Создает новый блок с указанными параметрами времени и значениями
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aStartTime long время(мсек с начала эпохи) начала значений в блоке (включительно)
   * @param aValues BLOB_ARRAY массив значений блока
   * @return {@link ISequenceBlockEdit} созданный блок
   */
  protected abstract ISequenceBlockEdit<V> doCreateBlock( IParameterized aTypeInfo, long aStartTime,
      BLOB_ARRAY aValues );

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Проводит проверку последовательности блоков синхронных значений
   * <p>
   * При проверке проверяется: целостность и эффективность хранения блоков и их последовательности
   *
   * @param <V> тип значения последовательности
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aBlocks {@link IList} последовательность блоков
   * @param aLogger {@link ILogger} журнал для вывода сообщений
   * @return int счетчик блоков последовательности с неэффективным хранением
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException конфликт меток времени начала и завершения одного из блоков
   * @throws TsIllegalArgumentRtException несоответствие размера блока со временем начала и завершения одного из блоков
   */
  static <V extends ITemporal<?>> int checkBlockSequence( IParameterized aTypeInfo,
      IList<S5SequenceSyncBlock<V, ?, ?>> aBlocks, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aTypeInfo, aBlocks, aLogger );
    if( aBlocks.size() == 0 ) {
      // Пустая последовательность
      return 0;
    }
    // Двойной максимальный размер блока
    int doubleMaxSize = 2 * OP_BLOCK_SIZE_MAX.getValue( aTypeInfo.params() ).asInt();
    // Параметры предыдущего блока
    S5SequenceSyncBlock<V, ?, ?> firstBlock = aBlocks.get( 0 );
    long prevBlockEndTime = firstBlock.endTime();
    long prevBlockSyncDelta = firstBlock.syncDataDelta.longValue();
    long prevBlockSize = firstBlock.size();
    // Счетчик неэффективных блоков в последовательности
    int nonOptimalCount = 0;
    for( int index = 1, n = aBlocks.size(); index < n; index++ ) {
      S5SequenceSyncBlock<V, ?, ?> block = aBlocks.get( index );
      long blockStartTime = block.startTime();
      long blockEndTime = block.endTime();
      long blockSyncDelta = block.syncDataDelta.longValue();
      int blockSize = block.size();
      if( blockSize == 0 ) {
        // Блок пустой
        nonOptimalCount++;
        prevBlockEndTime = blockEndTime;
        prevBlockSyncDelta = blockSyncDelta;
        prevBlockSize = blockSize;
        continue;
      }
      // Проверка диапазона времени. Окончание данных в блоке
      TsIllegalArgumentRtException.checkTrue( blockStartTime > blockEndTime );
      if( blockStartTime < prevBlockEndTime ) {
        // Блоки в списке должны идти по возрастанию времени
        String et = timestampToString( prevBlockEndTime );
        String st = timestampToString( blockStartTime );
        throw new TsIllegalArgumentRtException( ERR_SYNC_WRONG_SEQUENCE, block.gwid(), et, st );
      }
      // Проверка количества значений в блоке.
      long expectedSize = (blockEndTime - blockStartTime + blockSyncDelta) / blockSyncDelta;
      if( expectedSize != blockSize ) {
        Long expected = Long.valueOf( expectedSize );
        Long received = Long.valueOf( blockSize );
        throw new TsIllegalArgumentRtException( ERR_SYNC_WRONG_SIZE, block.gwid(), expected, received );
      }
      if( blockSyncDelta != prevBlockSyncDelta ) {
        // Различается интервал между отсчетами
        Long dd = Long.valueOf( prevBlockSyncDelta );
        Long ndd = Long.valueOf( blockSyncDelta );
        String lastEndTime = timestampToString( prevBlockEndTime );
        String nextStartTime = timestampToString( blockStartTime );
        aLogger.warning( ERR_SYNC_DELTA_DIFFERENT, block, dd, ndd, lastEndTime, nextStartTime );
        nonOptimalCount++;
      }
      // Признак того, что предыдущий блок является может быть использван для объединения
      boolean prevUnionable = (prevBlockSize < doubleMaxSize
          && (blockStartTime - prevBlockEndTime) / blockSyncDelta + blockSize < doubleMaxSize);
      if( prevUnionable && (prevBlockEndTime + blockSyncDelta) != blockStartTime ) {
        // Начало нового блока не соответствует ожидаемому
        Long dd = Long.valueOf( prevBlockSyncDelta );
        String lastEndTime = timestampToString( prevBlockEndTime );
        String nextStartTime = timestampToString( blockStartTime );
        aLogger.warning( ERR_SYNC_SEQUENCE_BREAK, block, dd, lastEndTime, nextStartTime );
        nonOptimalCount++;
      }
      // Сохранение параметров предыдущего блока
      prevBlockEndTime = blockEndTime;
      prevBlockSyncDelta = blockSyncDelta;
      prevBlockSize = blockSize;
    }
    return nonOptimalCount;
  }

  /**
   * Возвращает индекс значения по метке времени
   *
   * @param aStartTime long метка времени (мсек с начала эпохи) начала значений в блоке
   * @param aDataDelta long количество мсек в слоте
   * @param aTimestamp long метка времени (мсек с начала эпохи)
   * @return int индекс значения
   * @throws TsIllegalArgumentRtException aDataDelta <= 0
   * @throws TsIllegalArgumentRtException aStartTime < aTimestamp
   */
  protected static int getIndexByTime( long aStartTime, long aDataDelta, long aTimestamp ) {
    TsNullArgumentRtException.checkFalse( aDataDelta > 0 );
    TsNullArgumentRtException.checkFalse( aStartTime <= aTimestamp );
    int index = (int)((aTimestamp - aStartTime) / aDataDelta);
    return index;
  }

  // ------------------------------------------------------------------------------------
  // Внутреннее API
  //
  /**
   * Возвращает индекс значения по метке времени
   *
   * @param aTimestamp long метка времени (мсек с начала эпохи)
   * @return int индекс значения
   * @throws TsIllegalArgumentRtException метка не попадает в диапазон времени блока
   * @throws TsIllegalArgumentRtException метка времени должна быть выравнена по syncDataDelta
   */
  private int getIndexByTime( long aTimestamp ) {
    // Проверка метки на попадание в диапазон
    checkTimestamp( aTimestamp );
    long startTime = startTime();
    long dataDelta = syncDataDelta.longValue();
    long index = (aTimestamp - startTime) / dataDelta;
    if( index * dataDelta != aTimestamp - startTime ) {
      // Метка времени значения синхроного блока должна быть выравнена по границе dataDeltaT
      String ts = timestampToString( aTimestamp );
      String st = timestampToString( startTime );
      throw new TsIllegalArgumentRtException( ERR_SYNC_WRONG_ALIGN, ts, st, syncDataDelta );
    }
    return (int)index;
  }

  /**
   * Создает массив значений последовательно копируя значения исходного массива и значения указанных блоков
   *
   * @param aFactory {@link ISequenceValueFactory} фабрика
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aSource BLOB_ARRAY исходный массив значений
   * @param aBlocks {@link IList}&lt;{@link ISequenceBlockEdit}&lt;&lt; список блоков поставляющих новые значения
   * @param aFrom int индекс блока в списке блоков с которого начинать копирование
   * @param aCount int количество копируемых блоков
   * @return BLOB_TYPE массив значений
   * @param <V> тип значения последовательности
   * @param <BLOB_ARRAY> тип массива blob-a в котором хранятся значения блока
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aSource не является массиовом
   */
  @SuppressWarnings( "unchecked" )
  private static <V extends ITemporal<?>, BLOB_ARRAY> //
  BLOB_ARRAY addBlocksValues( ISequenceValueFactory aFactory, IParameterized aTypeInfo, BLOB_ARRAY aSource,
      IList<ISequenceBlockEdit<V>> aBlocks, int aFrom, int aCount ) {
    TsNullArgumentRtException.checkNulls( aFactory, aTypeInfo, aSource, aBlocks );
    // Размер исходного массива
    int oldSize = Array.getLength( aSource );
    // Расчет размера массива
    int size = oldSize;
    for( int index = aFrom, n = aFrom + aCount; index < n; index++ ) {
      S5SequenceSyncBlock<V, BLOB_ARRAY, ?> block = (S5SequenceSyncBlock<V, BLOB_ARRAY, ?>)aBlocks.get( index );
      // TODO: WORKAROUND mvk 2018-08-09 ошибка целостности последовательности данных
      // На tm проявилась следующая ситуация:
      // - Ошибка дефрагментации(union) СИНХРОННЫХ float-значений (приборы)
      // - В базе два соседних блока.
      // -- Первый: ~15 значений (count). Интервал с t0 по t1 включительно. t0 < t1.
      // -- Второй: 1 значение (count). Интервал с t1 по t1 включительно.
      // Таким образом, происходит "наезд" второго блока (count=1), на хвост первого (count =~15).
      boolean hasWorkarroundCase =
          (index > 0 && block.size() == 1 && aBlocks.get( index - 1 ).endTime() == block.startTime());
      if( hasWorkarroundCase ) {
        // Игнорирование блока hasWorkarroundCase
        continue;
      }
      size += block.size();
    }
    // Итоговый массив значений
    BLOB_ARRAY newValues = (BLOB_ARRAY)aFactory.createValueArray( aTypeInfo, size );
    // Копирование старых значений
    System.arraycopy( aSource, 0, newValues, 0, oldSize );
    // Копирование значений объединяемых блоков
    int destPosition = oldSize;
    for( int index = aFrom, n = aFrom + aCount; index < n; index++ ) {
      S5SequenceSyncBlock<V, BLOB_ARRAY, ?> block = (S5SequenceSyncBlock<V, BLOB_ARRAY, ?>)aBlocks.get( index );
      // TODO: WORKAROUND mvk 2018-08-09 ошибка целостности последовательности данных
      // На tm проявилась следующая ситуация:
      // - Ошибка дефрагментации(union) СИНХРОННЫХ float-значений (приборы)
      // - В базе два соседних блока.
      // -- Первый: ~15 значений (count). Интервал с t0 по t1 включительно. t0 < t1.
      // -- Второй: 1 значение (count). Интервал с t1 по t1 включительно.
      // Таким образом, происходит "наезд" второго блока (count=1), на хвост первого (count =~15).
      boolean hasWorkarroundCase =
          (index > 0 && block.size() == 1 && aBlocks.get( index - 1 ).endTime() == block.startTime());
      if( hasWorkarroundCase ) {
        // Игнорирование блока hasWorkarroundCase
        continue;
      }
      int blockSize = block.size();
      System.arraycopy( block.blob().values(), 0, newValues, destPosition, blockSize );
      destPosition += blockSize;
    }
    return newValues;
  }

  /**
   * Создает массив значений последовательно копируя значения исходного массива и добавляя null-значения в указанном
   * количестве
   *
   * @param aFactory {@link ISequenceValueFactory} фабрика
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aSource BLOB_ARRAY исходный массив значений
   * @param aCount int количество копируемых null-значений
   * @return BLOB_ARRAY массив значений
   * @param <V> тип значения последовательности
   * @param <BLOB_ARRAY> тип массива blob-a в котором хранятся значения блока
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aSource не является массиовом
   */
  @SuppressWarnings( "unchecked" )
  private static <V extends ITemporal<?>, BLOB_ARRAY> //
  BLOB_ARRAY addNullValues( ISequenceFactory<V> aFactory, IParameterized aTypeInfo, BLOB_ARRAY aSource, int aCount ) {
    TsNullArgumentRtException.checkNulls( aFactory, aTypeInfo, aSource );
    if( !aSource.getClass().isArray() ) {
      // значения должны представлять массив
      throw new TsIllegalArgumentRtException( ERR_NOT_ARRAY, aSource.getClass().getName() );
    }
    // Размер исходного массива
    int oldSize = Array.getLength( aSource );
    // Размер итогового массива
    int newSize = oldSize + aCount;
    // Итоговый массив значений
    BLOB_ARRAY newValues = (BLOB_ARRAY)nullValues( aFactory, aTypeInfo, newSize );
    // Копирование старых значений
    System.arraycopy( aSource, 0, newValues, 0, oldSize );
    // Возвращаение результата
    return newValues;
  }

  /**
   * Проверяет метку времени на выравнивание по слоту (Data Delta Time)
   *
   * @param aSyncDataDelta long количество мсек в слоте
   * @param aStartTime long проверяемая метка времени (мсек с начала эпохи)
   * @throws TsIllegalArgumentRtException aSyncDataDelta <= 0
   * @throws TsIllegalArgumentRtException метка времени не выравнена по слоту
   */
  private static void checkAlignByDDT( long aSyncDataDelta, long aStartTime ) {
    TsIllegalArgumentRtException.checkFalse( aSyncDataDelta > 0 );
    long slotIndex = aStartTime / aSyncDataDelta;
    TsIllegalArgumentRtException.checkFalse( slotIndex * aSyncDataDelta == aStartTime );
  }
}
