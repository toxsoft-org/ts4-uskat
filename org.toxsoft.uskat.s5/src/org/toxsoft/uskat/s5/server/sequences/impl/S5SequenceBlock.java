package org.toxsoft.uskat.s5.server.sequences.impl;

import static java.lang.String.*;
import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.bricks.time.impl.TimeUtils.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5DataID.*;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Arrays;

import javax.persistence.*;

import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.bricks.validator.IValResList;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.bricks.validator.impl.ValResList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.sequences.*;

/**
 * Базовый блок хранения данных последовательности.
 *
 * @author mvk
 * @param <V> тип значения последовательности
 * @param <BLOB_ARRAY> тип массива blob-a в котором хранятся значения блока
 * @param <BLOB> реализация blob-а используемого для хранения значений блока
 */
@MappedSuperclass
@Inheritance( strategy = InheritanceType.TABLE_PER_CLASS )
public abstract class S5SequenceBlock<V extends ITemporal<?>, BLOB_ARRAY, BLOB extends S5SequenceBlob<?, BLOB_ARRAY, ?>>
    implements IS5SequenceBlockEdit<V>, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Поле таблицы: время (мсек с начала эпохи) завершения данных (включительно)
   */
  public static final String FIELD_END_TIME = "endTime"; //$NON-NLS-1$

  /**
   * Поле таблицы: количество значений в блоке
   */
  public static final String FIELD_SIZE = "size"; //$NON-NLS-1$

  /**
   * Поле таблицы: время (мсек с начала эпохи) начала данных (включительно) для проведения отладки
   */
  public static final String FIELD_DEBUG_START_TIME = "debugStartTime"; //$NON-NLS-1$

  /**
   * Поле таблицы: время (мсек с начала эпохи) завершения данных (включительно) для проведения отладки
   */
  public static final String FIELD_DEBUG_END_TIME = "debugEndTime"; //$NON-NLS-1$

  /**
   * Поле таблицы: значения блока
   */
  public static final String FIELD_BLOB = "_blob"; //$NON-NLS-1$

  /**
   * Имя статического метода создания блока значений.
   * <p>
   * TODO: ??? Метод имеет сигнатуру IS5SequenceBlockEdit&lt;V&gt;create( I aInfo, IList&ltV&gt; aValues ) и
   * определяется в конечных наследниках
   */
  public static final String BLOCK_CREATE_METHOD = "create"; //$NON-NLS-1$

  /**
   * Первичный составной (gwid,startTime) ключ
   */
  @EmbeddedId
  private S5DataID id;

  /**
   * Время (мсек с начала эпохи) окончания данных (включительно)
   */
  @Column( name = FIELD_END_TIME, //
      insertable = true,
      updatable = true,
      nullable = false,
      unique = false )
  private Long endTime;

  /**
   * Количество элементов в блоке. < 0: если значения блока были объединены с другими блоками (дефрагментация)
   */
  @Column( name = FIELD_SIZE, //
      insertable = true,
      updatable = true,
      nullable = false,
      unique = false )
  private Integer size;

  /**
   * Время начала значений блока
   * <p>
   * TODO: mvkd: только для отладки
   */
  @Column( name = FIELD_DEBUG_START_TIME, //
      insertable = true,
      updatable = true,
      nullable = false,
      unique = false )
  private Timestamp debugStartTime;

  /**
   * Время завершения значений блока
   * <p>
   * TODO: mvkd: только для отладки
   */
  @Column( name = FIELD_DEBUG_END_TIME, //
      insertable = true,
      updatable = true,
      nullable = false,
      unique = false )
  private Timestamp debugEndTime;

  /**
   * Значения блока
   */
  @PrimaryKeyJoinColumn( name = FIELD_BLOB )
  @OneToOne( cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER )
  private BLOB _blob;

  /**
   * Признак того это новый блок - был создан с помощью конструктора
   * {@link #S5SequenceBlock(Gwid, long, S5SequenceBlob)}
   * <p>
   * (!): Значение не сохраняется в базе данных
   */
  @Transient
  private boolean created;

  /**
   * Признак того это существующий блок был загружен из базы данных с помощью конструктора
   * {@link #S5SequenceBlock(ResultSet)} и впоследствии в нем были изменены значения
   * <p>
   * (!): Значение не сохраняется в базе данных
   */
  @Transient
  private boolean merged;

  /**
   * Журнал (общий для всех блоков)
   */
  protected static ILogger logger = getLogger( "S5SequenceBlock" ); //$NON-NLS-1$

  /**
   * Конструктор без параметров (для JPA)
   */
  protected S5SequenceBlock() {
  }

  /**
   * Конструктор
   *
   * @param aGwid {@link Gwid} НЕабстрактный {@link Gwid}-идентификатор данного
   * @param aStartTime long время (мсек с начала эпохи) начала данных (включительно)
   * @param aBlob BLOB реализация blob-а в котором хранятся значения блока
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException gwid идентификатор не может быть абстрактным (без объекта)
   * @throws TsIllegalArgumentRtException количество значений в блоке = 0
   * @throws TsIllegalArgumentRtException неверная метка времени начала значений
   */
  protected S5SequenceBlock( Gwid aGwid, long aStartTime, BLOB aBlob ) {
    id = new S5DataID( aGwid, aStartTime );
    _blob = aBlob;
    _blob.setBlockEntity( this );
    created = true;
    merged = false;
  }

  /**
   * Конструктор блока из текущей записи курсора dbms
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsInternalErrorRtException ошибка создания блока
   */
  protected S5SequenceBlock( ResultSet aResultSet ) {
    if( aResultSet == null ) {
      throw new TsNullArgumentRtException();
    }
    try {
      id = new S5DataID( aResultSet );
      endTime = Long.valueOf( aResultSet.getLong( FIELD_END_TIME ) );
      size = Integer.valueOf( aResultSet.getInt( FIELD_SIZE ) );
      debugStartTime = aResultSet.getTimestamp( FIELD_DEBUG_START_TIME );
      debugEndTime = aResultSet.getTimestamp( FIELD_DEBUG_END_TIME );
      _blob = doCreateBlob( aResultSet );
      _blob.setBlockEntity( this );
      created = false;
      merged = false;
    }
    catch( Throwable e ) {
      // Неожиданная ошибка создания блока из курсора dbms
      throw new TsInternalErrorRtException( e, ERR_CREATE_BLOCK_FROM_CURSOR, cause( e ) );
    }
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //
  /**
   * Возвращает первичный, составной(gwid, startTime) ключ блока
   *
   * @return {@link S5DataID} первичный ключ
   */
  final S5DataID id() {
    return id;
  }

  /**
   * Возвращает признак того, что это новый блок - был создан с помощью конструктора
   * {@link #S5SequenceBlock(Gwid, long, S5SequenceBlob)}
   *
   * @return boolean <b>true</b> это новый блок;<b>false</b> это не новый блок
   */
  final boolean created() {
    return created;
  }

  /**
   * Возвращает признак того, что это существующий блок был загружен из базы данных с помощью конструктора
   * {@link #S5SequenceBlock(ResultSet)} и впоследствии в нем были изменены значения.
   *
   * @return boolean <b>true</b> это новый блок;<b>false</b> это не новый блок
   */
  final boolean merged() {
    return merged;
  }

  final void afterWrite() {
    created = false;
    merged = false;
  }

  /**
   * Сохраняет блок в базе данных
   *
   * @param aEntityManager {@link EntityManager} менеджер постоянства
   * @return boolean <b>true</b> запись выполнена; <b>false</b> запись не требуется
   * @throws TsNullArgumentRtException аргумент = null
   */
  final boolean write( EntityManager aEntityManager ) {
    TsNullArgumentRtException.checkNull( aEntityManager );
    TsInternalErrorRtException.checkTrue( created && merged );
    if( created ) {
      // executeInsert( aEntityManager );
      aEntityManager.persist( this );
      return true;
    }
    if( merged ) {
      // executeUpdate( aEntityManager );
      aEntityManager.merge( this );
      return true;
    }
    return false;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequenceBlock
  //
  @Override
  public final Gwid gwid() {
    return id.gwid();
  }

  @Override
  public abstract boolean isSync();

  @Override
  public final int size() {
    return Math.abs( size.intValue() );
  }

  @Override
  public abstract long timestamp( int aIndex );

  @Override
  public abstract V getValue( int aIndex );

  @Override
  public final long startTime() {
    return id.startTime().longValue();
  }

  @Override
  public final long endTime() {
    return endTime.longValue();
  }

  @Override
  public abstract int firstByTime( long aTimestamp );

  @Override
  public abstract int lastByTime( long aTimestamp );

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequenceBlockEdit
  //
  @Override
  public final IS5SequenceBlockEdit<V> createBlockOrNull( IParameterized aTypeInfo, long aStartTime, long aEndTime ) {
    TsNullArgumentRtException.checkNull( aTypeInfo );
    TimeUtils.checkIntervalArgs( aStartTime, aEndTime );
    if( aStartTime < startTime() || endTime() < aEndTime ) {
      String st = timestampToString( aStartTime );
      String et = timestampToString( aEndTime );
      String bst = timestampToString( startTime() );
      String bet = timestampToString( endTime.longValue() );
      throw new TsIllegalArgumentRtException( ERR_CREATE_BLOCK, bst, bet, st, et, size );
    }
    // Индексы начала и конца данных из исходного блока
    int startIndex = firstByTime( aStartTime );
    int endIndex = lastByTime( aEndTime );
    if( startIndex >= 0 && endIndex >= 0 ) {
      if( timestamp( startIndex ) < aStartTime ) {
        // Найденное значение до границы создаваемого сблока. Попытка получить индекс следующего значения в блоке
        // startIndex = (startIndex < size() - 1 ? startIndex + 1 : -1);
        startIndex = (startIndex < size() - 1 ? startIndex + 1 : startIndex);
      }
      if( timestamp( endIndex ) > aEndTime ) {
        // Найденное значение после границы создаваемого сблока. Попытка получить индекс предыдущего значения в блоке
        // endIndex = (endIndex > 0 ? endIndex - 1 : -1);
        endIndex = (endIndex > 0 ? endIndex - 1 : endIndex);
      }
    }
    if( startIndex > endIndex ) {
      // В указанном интервале нет значений. Невозможно создать блок
      return null;
    }
    // Проверка логики выбранной для реализации алгоритма
    if( startIndex >= 0 == endIndex < 0 ) {
      String st = timestampToString( aStartTime );
      String et = timestampToString( aEndTime );
      String bst = timestampToString( startTime() );
      String bet = timestampToString( endTime.longValue() );
      Long sidx = Long.valueOf( startIndex );
      Long eidx = Long.valueOf( endIndex );
      throw new TsInternalErrorRtException( ERR_INTERNAL_CREATE_BLOCK, st, et, sidx, eidx, bst, bet, size );
    }
    // Индекс включения последнего значения
    int includeEndIndex = endIndex;
    if( includeEndIndex >= 0 ) {
      // +1: aStartTime и aEndTime "включительно"
      includeEndIndex++;
    }
    // Получаем часть значений блока или пустой массив (это допустимо)
    BLOB_ARRAY newValues = copyValuesOfRange( values(), startIndex, includeEndIndex );
    // Создание конкретного блока наследниками
    long blockStartTime = aStartTime;
    long blockEndTime = aEndTime;
    if( startIndex >= 0 ) {
      blockStartTime = timestamp( startIndex );
    }
    if( endIndex >= 0 ) {
      blockEndTime = timestamp( endIndex );
    }
    if( blockStartTime > blockEndTime ) {
      // Ошибка расчета интервала значений блока
      String st = TimeUtils.timestampToString( aStartTime );
      String et = TimeUtils.timestampToString( aEndTime );
      Long stl = Long.valueOf( aStartTime );
      Long etl = Long.valueOf( aEndTime );
      String bst = TimeUtils.timestampToString( blockStartTime );
      String bet = TimeUtils.timestampToString( blockEndTime );
      Long bstl = Long.valueOf( blockStartTime );
      Long betl = Long.valueOf( blockEndTime );
      Long sti = Long.valueOf( startIndex );
      Long eti = Long.valueOf( endIndex );
      String err = format( ERR_CREATE_INTERVAL, this, st, stl, et, etl, bst, bstl, bet, betl, sti, eti );
      throw new TsInternalErrorRtException( err );
    }
    return doCreateBlock( aTypeInfo, blockStartTime, blockEndTime, startIndex, includeEndIndex, newValues );
  }

  @Override
  public final void editEndTime( long aEndTime ) {
    long startTime = startTime();
    TsIllegalArgumentRtException.checkTrue( startTime > aEndTime );
    // Исходный массив значений
    BLOB_ARRAY sourceValues = values();
    // Индексы начала и конца данных из исходного блока
    // 2019-11-07 mvk начало блока не может быть изменено (первичный ключ)
    // int startIndex = firstByTime( startTime );
    int startIndex = 0;
    int endIndex = lastByTime( aEndTime );
    if( startIndex < 0 || endIndex < 0 ) {
      // Формируется пустой блок
      BLOB_ARRAY newValues = editArray( sourceValues, -1, -1, sourceValues, -1, 0 );
      doSetEndTime( aEndTime, -1, -1, -1, 0, newValues );
      return;
    }
    // Признак того, что найденно значение начала
    boolean foundStart;
    // Признак того, что найденно значение завершения
    boolean foundEnd = false;

    // Установка признаков начала и завершения
    // 2019-11-07 mvk начало блока не может быть изменено (первичный ключ)
    // for( int index = startIndex, n = size(); index < n; index++ ) {
    // if( timestamp( index ) >= startTime ) {
    // startIndex = index;
    // foundStart = true;
    // break;
    // }
    // }
    startIndex = 0;
    foundStart = true;

    for( int index = endIndex; index >= 0; index-- ) {
      if( timestamp( index ) <= aEndTime ) {
        endIndex = index;
        foundEnd = true;
        break;
      }
    }
    if( !foundStart || !foundEnd ) {
      // Формируется пустой блок
      BLOB_ARRAY newValues = editArray( sourceValues, -1, -1, sourceValues, -1, 0 );
      doSetEndTime( aEndTime, -1, -1, -1, 0, newValues );
      return;
    }
    // Индексы начала и конца данных из исходного блока
    int srcStart = startIndex;
    int srcEnd = endIndex;
    // Количество записываемых значений "включительно"(+1)
    int srcCount = srcEnd - srcStart + 1;
    // Позиции редактирования блока (весь блок, от начала до конца)
    int editStart = 0;
    int editFinish = size();
    // Получаем часть значений блока
    BLOB_ARRAY newValues = editArray( sourceValues, editStart, editFinish, sourceValues, srcStart, srcCount );
    // Редактирование конкретного блока наследниками
    doSetEndTime( aEndTime, editStart, editFinish, srcStart, srcCount, newValues );
  }

  @Override
  public final int uniteBlocks( IS5SequenceFactory<V> aFactory, IList<IS5SequenceBlockEdit<V>> aBlocks,
      ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aFactory, aBlocks, aLogger );
    int retValue = doUniteBlocks( aFactory, aBlocks, aLogger );
    // 2024-02-27 mvk --- (replace by setUnionMark())
    // if( retValue > 0 ) {
    // // Установка признака того, что была проведена дефрагментация
    // size = Integer.valueOf( -size() );
    // }
    return retValue;
  }

  @Override
  public final IValResList validation( IParameterized aTypeInfo ) {
    TsNullArgumentRtException.checkNull( aTypeInfo );
    return doValidation( aTypeInfo );
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    return format( BLOCK_TO_STRING_FORMAT, getClass().getSimpleName(), id.gwid(), timestampToString( startTime() ),
        timestampToString( endTime.longValue() ), size, _blob );
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + id.hashCode();
    result = TsLibUtils.PRIME * result + (int)(endTime.longValue() ^ (endTime.longValue() >>> 32));
    result = TsLibUtils.PRIME * result + (size() ^ (size() >>> 32));
    return result;
  }

  @Override
  public boolean equals( Object aObj ) {
    if( aObj == this ) {
      return true;
    }
    if( !(aObj instanceof S5SequenceBlock<?, ?, ?> other) ) {
      return false;
    }
    if( !id.equals( other.id ) ) {
      return false;
    }
    if( endTime.longValue() != other.endTime() ) {
      return false;
    }
    if( size() != other.size() ) {
      return false;
    }
    return true;
  }

  // ------------------------------------------------------------------------------------
  // Методы для реализации наследниками
  //
  /**
   * Создает новый блок и инициализирует его указанными данными целевого блока
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aStartTime long время(мсек с начала эпохи) начала значений в блоке (включительно)
   * @param aEndTime long время(мсек с начала эпохи) окончания значений в блоке (включительно)
   * @param aStartIndex int индекс первого значения в исходном блоке. Включительно
   * @param aEndIndex int индекс последнего значения в исходном блоке. Невключительно
   * @param aValues BLOB_ARRAY массив значений блока
   * @return {@link IS5SequenceBlockEdit} созданный блок
   */
  protected abstract IS5SequenceBlockEdit<V> doCreateBlock( IParameterized aTypeInfo, long aStartTime, long aEndTime,
      int aStartIndex, int aEndIndex, BLOB_ARRAY aValues );

  /**
   * Создает blob из набора данных
   *
   * @param aResultSet {@link ResultSet} курсор dbms
   * @return BLOB созданный blob
   */
  protected abstract BLOB doCreateBlob( ResultSet aResultSet );
  // {
  // TODO: ??? мы не можем здесь восстановить blob. Только внешний ключ (long)
  // _blob.setValues( (BLOB_ARRAY)new ObjectInputStream( aResultSet.getBinaryStream( "_blob" ) ).readObject() );
  // TODO Auto-generated method stub
  // return null;
  // }

  /**
   * Редактирует диапазон времени блока
   * <p>
   * Если aSrcCount == 0, то из блока удаляются все значения (образуется пустой блок)
   *
   * @param aEndTime long время(мсек с начала эпохи) окончания редактируемых значений в блоке
   * @param aEditStart int индекс первого значения в целевом блоке который подвергается редактированию. Включительно
   * @param aEditEnd int индекс последнего значения в целевом блоке который подвергается редактированию. Невключительно
   * @param aSrcStart int индекс первого значения в исходном блоке. Включительно
   * @param aSrcCount int количество редактируемых значений Object массив значений блока. aSrcCount = 0 формируется
   *          пустой блок
   * @param aValues BLOB_ARRAY массив новых значений для блока
   */
  protected abstract void doSetEndTime( long aEndTime, int aEditStart, int aEditEnd, int aSrcStart, int aSrcCount,
      BLOB_ARRAY aValues );

  /**
   * Вызывается при устанавке новых значений блока
   *
   * @param aValues новые значения блока
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException попытка изменения идентификатора блока
   * @throws TsIllegalArgumentRtException количество значений = 0
   * @throws TsIllegalArgumentRtException неверная метка времени начала значений
   */
  protected void doSetValues( BLOB_ARRAY aValues ) {
    // nop
  }

  /**
   * Обновить значения блока
   *
   * @param aSource {@link S5SequenceBlock} исходный блок
   */
  protected void doUpdate( S5SequenceBlock<V, ?, ?> aSource ) {
    // nop
  }

  /**
   * Проводит валидацию (исправление содержимого блока) если это необходимо
   * <p>
   * Важно: при переопределении наследник должен вызвать метод {@link #doValidation(IParameterized)} базового класса
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @return {@link IValResList} результаты валидации
   */
  protected IValResList doValidation( IParameterized aTypeInfo ) {
    ValResList retValue = new ValResList();
    if( blob() == null ) {
      // Значения не определены. Критическая ошибка
      retValue.add( ValidationResult.error( ERR_VALIDATION_NULL_BLOB, this ) );
      return retValue;
    }
    // Проверка blob
    retValue.addValResList( blob().validation() );
    // Проверка согласованности значений и описания блока
    BLOB_ARRAY values = values();
    if( values == null ) {
      // Значения не определены. Критическая ошибка
      retValue.add( ValidationResult.error( ERR_VALIDATION_NULL_VALUES, this ) );
    }
    int oldSize = size();
    if( oldSize == 0 ) {
      // Пустой размер блока (предупреждение)
      retValue.add( ValidationResult.error( ERR_VALIDATION_ZERO_SIZE, this ) );
      return retValue;
    }
    int newSize = Array.getLength( values );
    if( newSize != oldSize ) {
      // Размер блока не соответствует фактическому количеству значений. Считаем это критической ошибкой, так
      // восстановление уже невозможно
      retValue.add( ValidationResult.error( ERR_VALIDATION_WRONG_SIZE, this, Integer.valueOf( oldSize ),
          Integer.valueOf( newSize ) ) );
    }
    if( startTime() > endTime() ) {
      // Недопустимый интервал значений (неисправимая ошибка)
      retValue.add( ValidationResult.error( ERR_VALIDATION_WRONG_INTERVAL, this,
          TimeUtils.timestampToString( startTime() ), TimeUtils.timestampToString( endTime() ) ) );
    }
    return retValue;
  }

  /**
   * Осуществляет попытку объединения значений блока со значениями указанного списка блоков.
   *
   * @param aFactory {@link IS5SequenceValueFactory} фабрика формирования последовательности
   * @param aBlocks {@link IList}&lt;{@link IS5SequenceBlockEdit}&lt;I&gt;&gt; список блоков
   * @param aLogger {@link ILogger} журнал объединения блоков
   * @return int количество блоков от начала указанного списка с которыми произошло объединение.
   */
  protected abstract int doUniteBlocks( IS5SequenceFactory<V> aFactory, IList<IS5SequenceBlockEdit<V>> aBlocks,
      ILogger aLogger );

  /**
   * Возвращает параметры необходимые для выполнения запроса записи блока в БД
   * <p>
   * Наследники могут переопределять метод для добавления собственных параметров к уже определенным в базовом классе
   *
   * @return {@link IStringMap} карта параметров с возможностью добавления. <br>
   *         Ключ: имя поля в таблице;<br>
   *         Значение: значение поля.
   */
  protected IStringMapEdit<Object> doInsertQueryParams() {
    IStringMapEdit<Object> retValue = new StringMap<>();
    retValue.put( FIELD_GWID, id.gwid().toString() );
    retValue.put( FIELD_START_TIME, id.startTime() );
    retValue.put( FIELD_END_TIME, endTime );
    retValue.put( FIELD_SIZE, size );
    retValue.put( FIELD_DEBUG_START_TIME, debugStartTime );
    retValue.put( FIELD_DEBUG_END_TIME, debugEndTime );
    return retValue;
  }

  /**
   * Возвращает параметры необходимые для выполнения запроса обновления блока в БД
   * <p>
   * Наследники могут переопределять метод для добавления собственных параметров к уже определенным в базовом классе
   *
   * @return {@link IStringMapEdit} карта параметров с возможностью добавления. <br>
   *         Ключ: имя поля в таблице;<br>
   *         Значение: значение поля.
   */
  protected IStringMapEdit<Object> doUpdateQueryParams() {
    IStringMapEdit<Object> retValue = new StringMap<>();
    retValue.put( FIELD_END_TIME, endTime );
    retValue.put( FIELD_SIZE, size );
    retValue.put( FIELD_DEBUG_START_TIME, debugStartTime );
    retValue.put( FIELD_DEBUG_END_TIME, debugEndTime );
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // API для наследников
  //
  /**
   * Возвращает blob значений элементов блока
   *
   * @return BLOB blob массива значений
   */
  protected final BLOB blob() {
    return _blob;
  }

  /**
   * Возвращает значения блока
   *
   * @return BLOB_ARRAY значения блока
   */
  protected final BLOB_ARRAY values() {
    return _blob.values();
  }

  /**
   * Устанавливает значения блока
   *
   * @param aValues значения блока
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException попытка изменения идентификатора блока
   * @throws TsIllegalArgumentRtException количество значений = 0
   * @throws TsIllegalArgumentRtException неверная метка времени начала значений
   */
  protected final void setValues( BLOB_ARRAY aValues ) {
    TsNullArgumentRtException.checkNull( aValues );
    int newSize = Array.getLength( aValues );
    TsNullArgumentRtException.checkFalse( newSize > 0, ERR_WRONG_SIZE );
    _blob.setValues( aValues );
    // выставляем size, чтобы избежать ошибки "cannot be null"
    size = Integer.valueOf( size == null || size.intValue() > 0 ? newSize : -newSize );
    endTime = Long.valueOf( timestamp( newSize - 1 ) );
    // TODO: mvkd: только для отладки
    debugStartTime = new Timestamp( startTime() );
    debugEndTime = new Timestamp( endTime.longValue() );
    // Вызов методов установки наследника
    doSetValues( aValues );
    // Установка признака изменения значений для существующего блока
    if( !created ) {
      merged = true;
    }
  }

  /**
   * Установка пометки того, что данные в блоке были объединены с другими блоками и больше объединение не требуется
   */
  protected final void setUnionMark() {
    if( size.intValue() > 0 ) {
      // Установка признака того, что была проведена дефрагментация
      size = Integer.valueOf( -size() );
      // Установка признака изменения значений для существующего блока
      merged = true;
    }
  }

  /**
   * Обновить значения блока
   * <p>
   * Используется {@link S5AbstractSequenceWriter#writeBlocksToDbms(EntityManager, Iterable, ILogger, S5DbmsStatistics)}
   * при выполнении операций {@link EntityManager#merge(Object)}.
   *
   * @param aSource {@link S5SequenceBlock} исходный блок
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException недопустимый первичный, составной(gwid, startTime) ключ блока
   * @throws TsIllegalArgumentRtException запрет обновления только что созданного блока
   */
  @SuppressWarnings( "unchecked" )
  protected final void update( S5SequenceBlock<V, ?, ?> aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    TsIllegalArgumentRtException.checkFalse( id.equals( aSource.id ) );
    TsIllegalArgumentRtException.checkTrue( created );
    _blob.setValues( (BLOB_ARRAY)aSource.values() );
    size = aSource.size;
    endTime = aSource.endTime;
    // TODO: mvkd: только для отладки
    debugStartTime = aSource.debugStartTime;
    debugEndTime = aSource.debugEndTime;
    // Обработка наследниками
    doUpdate( aSource );
  }

  /**
   * Проверяет, что указанная метка находится в диапазоне блока(включительно)
   *
   * @param aTimestamp метка времени
   * @throws TsIllegalArgumentRtException метка находится вне диапазона
   */
  protected final void checkTimestamp( long aTimestamp ) {
    if( aTimestamp < startTime() || endTime.longValue() < aTimestamp ) {
      // Метка вне блока
      String t = timestampToString( aTimestamp );
      String st = timestampToString( startTime() );
      String et = timestampToString( endTime.longValue() );
      if( isSync() ) {
        throw new TsIllegalArgumentRtException( ERR_SYNC_OUT, id.gwid(), t, st, et );
      }
      throw new TsIllegalArgumentRtException( ERR_ASYNC_OUT, id.gwid(), t, st, et );
    }
  }

  /**
   * Замещает значения в массиве aOldValues значениями из массива aNewValues
   * <p>
   * Если aNewCount == 0, то возвращается пустой массив значений
   *
   * @param aOldValues BLOB_ARRAY массив значений со старыми значениями
   * @param aOldStartEdit int позиция в массиве со старыми значениями с которой будет проводится редактирование значений
   * @param aOldFinishEdit int позиция в массиве со старыми значениями с которой будут идти старые данные
   * @param aNewValues BLOB_ARRAY массив копируемых(новых) значений
   * @param aNewStart int позиция в массиве копируемых(новых) значений с которой берутся значения для редактирования
   * @param aNewCount int количество копируемых значений. 0: значения не копируются (удаление элементов из массива)
   * @return BLOB_ARRAY полученный массив значений
   * @param <BLOB_ARRAY> тип массива blob-a в котором хранятся значения блока
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException исходный или целевой массив не является массивом
   * @throws TsIllegalArgumentRtException не совпадают типы исходного и целевого массива
   * @throws TsIllegalArgumentRtException неверные индексы начала и завершения копирования из исходного массива
   */
  @SuppressWarnings( "unchecked" )
  protected static <BLOB_ARRAY> //
  BLOB_ARRAY editArray( BLOB_ARRAY aOldValues, int aOldStartEdit, int aOldFinishEdit, BLOB_ARRAY aNewValues,
      int aNewStart, int aNewCount ) {
    TsNullArgumentRtException.checkNull( aOldValues );
    Class<?> srcClass = aNewValues.getClass();
    Class<?> destClass = aOldValues.getClass();
    if( !srcClass.isArray() ) {
      throw new TsIllegalArgumentRtException( ERR_SRC_NOT_ARRAY, srcClass.getName() );
    }
    if( !destClass.isArray() ) {
      throw new TsIllegalArgumentRtException( ERR_DEST_NOT_ARRAY, destClass.getName() );
    }
    if( !destClass.equals( srcClass ) ) {
      String srcItemTypeName = srcClass.getComponentType().getName();
      String destItemTypeName = destClass.getComponentType().getName();
      throw new TsIllegalArgumentRtException( ERR_NOT_EQUALS_ARRAY_TYPES, srcItemTypeName, destItemTypeName );
    }
    if( aNewCount == 0 ) {
      // Требуется скопировать 0 значений. Возвращаем пустой массив
      return (BLOB_ARRAY)Array.newInstance( destClass.getComponentType(), 0 );
    }
    int count = Array.getLength( aOldValues );
    int targetCount = count - (aOldFinishEdit - aOldStartEdit) + aNewCount;
    // Новый массив
    BLOB_ARRAY target = (BLOB_ARRAY)Array.newInstance( aOldValues.getClass().getComponentType(), targetCount );
    // Копируем старые данные
    if( aOldStartEdit > 0 ) {
      // "Голова"
      try {
        System.arraycopy( aOldValues, 0, target, 0, aOldStartEdit );
      }
      catch( ArrayStoreException e ) {
        String v = aOldValues.toString();
        Long vl = Long.valueOf( Array.getLength( aOldValues ) );
        String t = target.toString();
        Long tl = Long.valueOf( Array.getLength( target ) );
        Long se = Long.valueOf( aOldStartEdit );
        throw new TsInternalErrorRtException( e, ERR_COPY_HEAD_ARRAY, v, vl, t, tl, se );
      }
    }
    if( count > aOldFinishEdit ) {
      // "Хвост"
      try {
        System.arraycopy( aOldValues, aOldFinishEdit, target, aOldStartEdit + aNewCount, count - aOldFinishEdit );
      }
      catch( ArrayStoreException e ) {
        String v = aOldValues.toString();
        Long vl = Long.valueOf( Array.getLength( aOldValues ) );
        Long fe = Long.valueOf( aOldFinishEdit );
        String t = target.toString();
        Long tl = Long.valueOf( Array.getLength( target ) );
        Long se = Long.valueOf( aOldStartEdit );
        Long cc = Long.valueOf( aNewCount );
        Long c = Long.valueOf( count );
        throw new TsInternalErrorRtException( e, ERR_COPY_TAIL_ARRAY, v, vl, fe, t, tl, se, cc, c, fe );
      }
    }
    // Копируем новые данные
    if( aNewCount > 0 ) {
      try {
        System.arraycopy( aNewValues, aNewStart, target, aOldStartEdit, aNewCount );
      }
      catch( ArrayStoreException e ) {
        String s = aNewValues.toString();
        Long sl = Long.valueOf( Array.getLength( aNewValues ) );
        Long cs = Long.valueOf( aNewStart );
        String t = target.toString();
        Long tl = Long.valueOf( Array.getLength( target ) );
        Long se = Long.valueOf( aOldStartEdit );
        Long cc = Long.valueOf( aNewCount );
        throw new TsInternalErrorRtException( e, ERR_COPY_ARRAY, s, sl, cs, t, tl, se, cc );
      }
    }
    return target;
  }

  /**
   * Копирует из указанного массива значения в указанном диапазоне
   * <p>
   * Если aStartIndex == 0 && aEndIndex == 0, то возвращается пустой массив значений
   *
   * @param aValues BLOB_ARRAY исходный массив значений
   * @param aStartIndex int индекс начала копирования. Включительно
   * @param aEndIndex int индекс завершения копирования. Невключительно
   * @return BLOB_ARRAY массив значений указанного диапазона
   * @param <BLOB_ARRAY> тип массива blob-a в котором хранятся значения блока
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException исходный массив не является массивом
   * @throws TsIllegalArgumentRtException неверные индексы начала и завершения копирования из исходного массива
   */
  @SuppressWarnings( "unchecked" )
  protected static <BLOB_ARRAY> //
  BLOB_ARRAY copyValuesOfRange( BLOB_ARRAY aValues, int aStartIndex, int aEndIndex ) {
    TsNullArgumentRtException.checkNull( aValues );
    Class<?> arrayClass = aValues.getClass();
    if( !arrayClass.isArray() ) {
      throw new TsIllegalArgumentRtException( ERR_NOT_ARRAY, aValues.getClass().getName() );
    }
    try {
      // Тип значений массива
      Class<?> componentType = arrayClass.getComponentType();
      if( aStartIndex == 0 && aEndIndex == 0 ) {
        // Пустой массив значений
        return (BLOB_ARRAY)Array.newInstance( componentType, 0 );
      }
      if( componentType == boolean.class ) {
        return (BLOB_ARRAY)Arrays.copyOfRange( (boolean[])aValues, aStartIndex, aEndIndex );
      }
      if( componentType == long.class ) {
        return (BLOB_ARRAY)Arrays.copyOfRange( (long[])aValues, aStartIndex, aEndIndex );
      }
      if( componentType == double.class ) {
        return (BLOB_ARRAY)Arrays.copyOfRange( (double[])aValues, aStartIndex, aEndIndex );
      }
      if( componentType == int.class ) {
        return (BLOB_ARRAY)Arrays.copyOfRange( (int[])aValues, aStartIndex, aEndIndex );
      }
      if( componentType == short.class ) {
        return (BLOB_ARRAY)Arrays.copyOfRange( (short[])aValues, aStartIndex, aEndIndex );
      }
      if( componentType == char.class ) {
        return (BLOB_ARRAY)Arrays.copyOfRange( (char[])aValues, aStartIndex, aEndIndex );
      }
      if( componentType == byte.class ) {
        return (BLOB_ARRAY)Arrays.copyOfRange( (byte[])aValues, aStartIndex, aEndIndex );
      }
      if( componentType == float.class ) {
        return (BLOB_ARRAY)Arrays.copyOfRange( (float[])aValues, aStartIndex, aEndIndex );
      }
      return (BLOB_ARRAY)Arrays.copyOfRange( (Object[])aValues, aStartIndex, aEndIndex );

    }
    catch( @SuppressWarnings( "unused" ) IllegalArgumentException e ) {
      String className = aValues.getClass().getName();
      Long startIndex = Long.valueOf( aStartIndex );
      Long endIndex = Long.valueOf( aEndIndex );
      Long length = Long.valueOf( Array.getLength( aValues ) );
      throw new TsIllegalArgumentRtException( ERR_ARRAY_COPY, className, startIndex, endIndex, length );
    }
  }

  /**
   * Проверка порядка следования значений последовательности имеющих метку времени
   *
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aFirst {@link ITemporal} первое значение в последовательности
   * @param aLast {@link ITemporal} последнее значение в последовательности
   * @param aPrev {@link ITemporal} предыдущее значение в последовательности
   * @param aNext {@link ITemporal} следующее значение в последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException значения не отсортированы по времени
   */
  protected static void checkValuesOrder( Gwid aGwid, ITemporal<?> aFirst, ITemporal<?> aLast, ITemporal<?> aPrev,
      ITemporal<?> aNext ) {
    TsNullArgumentRtException.checkNulls( aGwid, aFirst, aLast, aPrev, aNext );
    long firstTime = aFirst.timestamp();
    long lastTime = aLast.timestamp();
    if( firstTime > lastTime ) {
      // Неверные границы последовательности значений
      throw new TsIllegalArgumentRtException( ERR_WRONG_FIRST_LAST_ORDER, aGwid, aFirst, aLast );
    }
    long prevTime = aPrev.timestamp();
    long nextTime = aNext.timestamp();
    if( prevTime < firstTime || prevTime > lastTime || //
        nextTime < firstTime || nextTime > lastTime || //
        prevTime > nextTime ) {
      throw new TsIllegalArgumentRtException( ERR_WRONG_ORDER, aGwid, aFirst, aLast, aPrev, aNext );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //

  // ------------------------------------------------------------------------------------
  // Открытые служебные методы
  //
  /**
   * Проводит выравнивание метки времени по границе интервала синхронных данных.
   * <p>
   * Для асинхронных значений ничего не делается и возвращается aTime.
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aTime long метка времени
   * @return long выравненная метка времени
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException метка времени находится вне диапазона блока
   */
  public static long alignByDDT( IParameterized aTypeInfo, long aTime ) {
    TsNullArgumentRtException.checkNull( aTypeInfo );
    if( !OP_IS_SYNC.getValue( aTypeInfo.params() ).asBool() ) {
      // Асинхронные значения всегда выравнены
      return aTime;
    }
    // Выравнивание синхронного значения
    long dataDelta = OP_SYNC_DT.getValue( aTypeInfo.params() ).asLong();
    long index = aTime / dataDelta;
    return index * dataDelta;
  }

  /**
   * Возвращает массив значений по умолчанию для данного с указанной фабрикой и описанием
   *
   * @param aFactory {@link IS5SequenceFactory} фабрика формирования последовательностей
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aCount int количество элементов в массиве
   * @param <V> тип значений последовательности
   * @return Object массив значений
   * @param <BLOB_ARRAY> тип массива blob-a в котором хранятся значения блока
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  public static <V extends ITemporal<?>, BLOB_ARRAY> //
  BLOB_ARRAY defaultValues( IS5SequenceFactory<V> aFactory, IParameterized aTypeInfo, int aCount ) {
    TsNullArgumentRtException.checkNulls( aFactory, aTypeInfo );
    BLOB_ARRAY valueArray = aFactory.createValueArray( aTypeInfo, aCount );
    if( !OP_IS_SYNC.getValue( aTypeInfo.params() ).asBool() ) {
      // Инициализируются только синхронные значения
      return valueArray;
    }
    Class<?> componentType = valueArray.getClass().getComponentType();
    // Инициализация блоков синхронных значений
    BLOB_ARRAY defaultValue = (BLOB_ARRAY)aFactory.getSyncDefaultValue( aTypeInfo );
    if( !componentType.isPrimitive() ) {
      return defaultValue;
    }
    // Обработка примитивных типов
    if( componentType == boolean.class ) {
      Arrays.fill( (boolean[])valueArray, ((Boolean)defaultValue).booleanValue() );
      return valueArray;
    }
    if( componentType == long.class ) {
      Arrays.fill( (long[])valueArray, ((Long)defaultValue).longValue() );
      return valueArray;
    }
    if( componentType == double.class ) {
      Arrays.fill( (double[])valueArray, ((Double)defaultValue).doubleValue() );
      return valueArray;
    }
    if( componentType == int.class ) {
      Arrays.fill( (int[])valueArray, ((Integer)defaultValue).intValue() );
      return valueArray;
    }
    if( componentType == short.class ) {
      Arrays.fill( (short[])valueArray, ((Short)defaultValue).shortValue() );
      return valueArray;
    }
    if( componentType == char.class ) {
      Arrays.fill( (char[])valueArray, ((Character)defaultValue).charValue() );
      return valueArray;
    }
    if( componentType == byte.class ) {
      Arrays.fill( (byte[])valueArray, ((Byte)defaultValue).byteValue() );
      return valueArray;
    }
    if( componentType == float.class ) {
      Arrays.fill( (float[])valueArray, ((Float)defaultValue).floatValue() );
      return valueArray;
    }
    Arrays.fill( (Object[])valueArray, defaultValue );
    return valueArray;
  }

  /**
   * Возвращает массив null-значений для данного с указанной фабрикой и описанием
   *
   * @param aFactory {@link IS5SequenceFactory} фабрика формирования последовательностей
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aCount int количество элементов в массиве
   * @param <V> тип значений последовательности
   * @return BLOB_ARRAY массив значений
   * @param <BLOB_ARRAY> тип массива blob-a в котором хранятся значения блока
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  public static <V extends ITemporal<?>, BLOB_ARRAY> //
  BLOB_ARRAY nullValues( IS5SequenceFactory<V> aFactory, IParameterized aTypeInfo, int aCount ) {
    TsNullArgumentRtException.checkNulls( aFactory, aTypeInfo );
    BLOB_ARRAY valueArray = aFactory.createValueArray( aTypeInfo, aCount );
    Class<?> componentType = valueArray.getClass().getComponentType();
    // null-значение для типа
    BLOB_ARRAY nullValue = (BLOB_ARRAY)aFactory.getSyncNullValue( aTypeInfo );
    // Обработка примитивных типов
    if( componentType == boolean.class ) {
      Arrays.fill( (boolean[])valueArray, ((Boolean)nullValue).booleanValue() );
      return valueArray;
    }
    if( componentType == long.class ) {
      Arrays.fill( (long[])valueArray, ((Long)nullValue).longValue() );
      return valueArray;
    }
    if( componentType == double.class ) {
      Arrays.fill( (double[])valueArray, ((Double)nullValue).doubleValue() );
      return valueArray;
    }
    if( componentType == int.class ) {
      Arrays.fill( (int[])valueArray, ((Integer)nullValue).intValue() );
      return valueArray;
    }
    if( componentType == short.class ) {
      Arrays.fill( (short[])valueArray, ((Short)nullValue).shortValue() );
      return valueArray;
    }
    if( componentType == char.class ) {
      Arrays.fill( (char[])valueArray, ((Character)nullValue).charValue() );
      return valueArray;
    }
    if( componentType == byte.class ) {
      Arrays.fill( (byte[])valueArray, ((Byte)nullValue).byteValue() );
      return valueArray;
    }
    if( componentType == float.class ) {
      Arrays.fill( (float[])valueArray, ((Float)nullValue).floatValue() );
      return valueArray;
    }
    // Обработка объектных типов
    Arrays.fill( (Object[])valueArray, nullValue );
    return valueArray;
  }
}
