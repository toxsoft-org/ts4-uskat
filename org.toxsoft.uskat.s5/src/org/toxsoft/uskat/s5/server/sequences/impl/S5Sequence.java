package org.toxsoft.uskat.s5.server.sequences.impl;

import static java.lang.String.*;
import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.bricks.time.impl.TimeUtils.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceBlock.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceUtils.*;

import java.io.*;

import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.legacy.QueryInterval;
import org.toxsoft.uskat.s5.server.sequences.*;

/**
 * Последовательность значений данных
 * <p>
 * Реализация является потокобезопасной.
 *
 * @author mvk
 * @param <V> тип значения
 */
public abstract class S5Sequence<V extends ITemporal<?>>
    implements IS5SequenceEdit<V>, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Формат toString()
   */
  private static final String TO_STRING_FORMAT = "%s [%s, %s, blocks=%d, values=%d]"; //$NON-NLS-1$

  /**
   * Параметризованное описание типа данного
   */
  private final IParameterized typeInfo;

  /**
   * Идентификатор данного
   */
  private final Gwid gwid;

  /**
   * Список блоков исторических данных отсортированный по startTime
   */
  private final IListEdit<ISequenceBlockEdit<V>> blocks;

  /**
   * Интервал времени последовательности значений
   */
  private IQueryInterval queryInterval;

  /**
   * Фабрика последовательностей
   */
  private transient ISequenceFactory<V> factory;

  /**
   * Текущее время курсора
   */
  private transient long cursorTime;

  /**
   * Индекс блока на котором находится курсор. < 0: в последовательности нет блоков
   */
  private transient int cursorBlockIndex;

  /**
   * Блок на котором находится курсор. null: в последовательности нет блоков
   */
  private transient ISequenceBlockEdit<V> cursorBlock;

  /**
   * Позиция курсора в блоке. < 0: время курсора попадает в блок, но блок пустой
   */
  private transient int cursorBlockValueIndex;

  /**
   * Общий журнал работы
   */
  private transient ILogger logger;

  /**
   * Журнал дефрагментации последовательностей
   */
  private transient ILogger uniterLogger;

  /**
   * Журнал проверки последовательностей
   */
  private transient ILogger validatorLogger;

  /**
   * Признак того после последнего вызова {@link #edit(IS5Sequence, IListEdit)}, произошло фактическое изменение
   * последовательности
   */
  private boolean edited;

  /**
   * Конструктор (используется для при загрузке блоков из dbms)
   *
   * @param aFactory {@link ISequenceFactory} фабрика последовательностей значений
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aInterval {@link IQueryInterval} интервал времени последовательности, подробности в {@link #interval()}
   * @param aBlocks {@link Iterable}&lt;{@link ISequenceBlock}&gt; список блоков представляющих последовательность
   * @throw {@link TsNullArgumentRtException} любой аргумент = null
   */
  protected S5Sequence( ISequenceFactory<V> aFactory, Gwid aGwid, IQueryInterval aInterval,
      Iterable<ISequenceBlockEdit<V>> aBlocks ) {
    TsNullArgumentRtException.checkNulls( aFactory, aGwid, aInterval, aBlocks );
    factory = aFactory;
    typeInfo = aFactory.typeInfo( aGwid );
    gwid = aGwid;
    long startTime = alignByDDT( typeInfo, aInterval.startTime() );
    long endTime = alignByDDT( typeInfo, aInterval.endTime() );
    queryInterval = new QueryInterval( aInterval.type(), startTime, endTime );
    blocks = new ElemArrayList<>();
    for( ISequenceBlockEdit<V> block : aBlocks ) {
      if( startTime > block.startTime() || endTime < block.endTime() ) {
        // Блок добавляемых значений должен быть подмножеством последовательности
        String st = timestampToString( startTime );
        String et = timestampToString( endTime );
        String bst = timestampToString( block.startTime() );
        String bet = timestampToString( block.endTime() );
        throw new TsIllegalArgumentRtException( ERR_WRONG_SUBBLOCK, bst, bet, st, et );
      }
      // Добавление блоков в последовательность с проверкой их диапазона времени и порядка возрастания
      safeAddBlock( typeInfo, gwid, blocks, block, logger() );
    }
    // Устанавливаем курсор на начало последовательности
    setCurrTime( startTime );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5Sequence
  //
  @Override
  public IParameterized typeInfo() {
    return typeInfo;
  }

  @Override
  public Gwid gwid() {
    return gwid;
  }

  @Override
  public IQueryInterval interval() {
    return queryInterval;
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public IList<ISequenceBlock<V>> blocks() {
    return ((IList<ISequenceBlock<V>>)(Object)blocks);
  }

  @Override
  public int findBlockIndex( long aTimestamp ) {
    return binarySearch( blocks, aTimestamp );
  }

  @Override
  public long getCurrTime() {
    return cursorTime;
  }

  @Override
  public void setCurrTime( long aCurrTime ) {
    long currTime = alignByDDT( typeInfo, aCurrTime );
    if( currTime == MIN_TIMESTAMP ) {
      currTime += 1;
    }
    if( currTime == MAX_TIMESTAMP ) {
      currTime -= 1;
    }
    // Текущие параметры интервала
    long factStartTime = alignByDDT( typeInfo, factStartTime( this ) );
    long factEndTime = alignByDDT( typeInfo, factEndTime( this ) );
    if( currTime < factStartTime ) {
      // Курсор не может быть установлен за границами последовательности
      String st = timestampToString( interval().startTime() );
      String et = timestampToString( interval().endTime() );
      String fst = timestampToString( factStartTime );
      String fet = timestampToString( factEndTime );
      String ct = timestampToString( currTime );
      throw new TsIllegalArgumentRtException( ERR_WRONG_CURSOR, st, et, fst, fet, ct );
    }
    if( factEndTime < currTime ) {
      // Курсор не может быть установлен за границами последовательности
      String st = timestampToString( interval().startTime() );
      String et = timestampToString( interval().endTime() );
      String fst = timestampToString( factStartTime );
      String fet = timestampToString( factEndTime );
      String ct = timestampToString( currTime );
      throw new TsIllegalArgumentRtException( ERR_WRONG_CURSOR, st, et, fst, fet, ct );
    }
    // Инициализация курсора
    cursorTime = currTime;
    cursorBlockIndex = -1;
    cursorBlock = null;
    cursorBlockValueIndex = -1;
    // Индекс ближайщего блока в зоне aCurrTime
    int nearest = binarySearch( blocks, currTime );
    ISequenceBlockEdit<V> block = (nearest < 0 ? null : blocks.get( nearest ));
    if( block == null ) {
      // Блоков нет
      return;
    }
    if( currTime < block.startTime() ) {
      // Курсор оказался "слева" от блока. Пробуем получить первый элемент в любом следующем, непустом блоке
      for( int index = nearest, n = blocks.size(); index < n; index++ ) {
        block = blocks.get( index );
        int blockValuesCount = block.size();
        if( blockValuesCount == 0 ) {
          continue;
        }
        cursorBlockIndex = index;
        cursorBlock = block;
        cursorBlockValueIndex = 0;
        return;
      }
    }
    if( block.endTime() < currTime ) {
      // Курсор оказался "справа" от блока. Пробуем получить последний элемент в любом предыдущем,непустом блоке
      for( int index = nearest; index >= 0; index-- ) {
        block = blocks.get( index );
        int blockValuesCount = block.size();
        if( blockValuesCount > 0 ) {
          continue;
        }
        cursorBlockIndex = index;
        cursorBlock = block;
        cursorBlockValueIndex = blockValuesCount - 1;
        return;
      }
      // Не найдены блоки из которых могут быть получены значения
      return;
    }
    cursorBlockIndex = nearest;
    cursorBlock = block;
    cursorBlockValueIndex = cursorBlock.firstByTime( alignByDDT( typeInfo, currTime ) );
  }

  @Override
  public boolean hasNext() {
    if( cursorBlock == null || cursorBlockValueIndex < 0 ) {
      // Курсор невалиден. Движение невозможно
      return false;
    }
    // Разрешено чтение значения
    return true;
  }

  @Override
  public V nextValue() {
    if( cursorBlock == null || cursorBlockValueIndex < 0 ) {
      // Курсор невалиден. Движение невозможно
      String ct = timestampToString( cursorTime );
      throw new TsIllegalStateRtException( ERR_NOT_CURSOR_DATA, ct, queryInterval );
    }
    // Чтение значения
    V retValue = cursorBlock.getValue( cursorBlockValueIndex );
    // Текущее время курсора
    cursorTime = retValue.timestamp();
    // Определяем есть ли следующее значение
    if( cursorBlockValueIndex + 1 < cursorBlock.size() ) {
      // Есть возможность движения по текущему блоку курсора
      cursorBlockValueIndex++;
    }
    else {
      // В текущем блоке больше нет значений
      cursorBlock = null;
      cursorBlockValueIndex = -1;
      // Пробуем найти следующий блок
      for( int index = cursorBlockIndex + 1, n = blocks.size(); index < n; index++ ) {
        ISequenceBlockEdit<V> block = blocks.get( index );
        if( block.size() > 0 ) {
          // Есть возможность перехода на первое значение следующего блока
          cursorBlockIndex = index;
          cursorBlock = block;
          cursorBlockValueIndex = 0;
          break;
        }
      }
      if( cursorBlockValueIndex < 0 ) {
        // Значений больше нет. Устраняем противоречивость
        cursorBlockIndex = -1;
      }
    }
    return retValue;
  }

  @Override
  public ITimedList<V> get( IQueryInterval aInterval ) {
    TsNullArgumentRtException.checkNull( aInterval );
    long queryStartTime = alignByDDT( typeInfo, aInterval.startTime() );
    long queryEndTime = alignByDDT( typeInfo, aInterval.endTime() );
    // Текущие параметры интервала последовательности
    // TODO: fix by query tm kino
    // long startTime = queryInterval.startTime();
    // long endTime = queryInterval.endTime();
    long startTime = alignByDDT( typeInfo, queryInterval.startTime() );
    long endTime = alignByDDT( typeInfo, queryInterval.endTime() );
    // Проверка интервала времени
    checkIntervalArgs( queryStartTime, queryEndTime );
    if( queryStartTime < startTime || endTime < queryEndTime ) {
      // Последовательность добавляемых значений должна быть подмножеством текущей последовательности
      String st = timestampToString( startTime );
      String et = timestampToString( endTime );
      String act = timestampToString( queryStartTime );
      String aet = timestampToString( queryEndTime );
      throw new TsIllegalArgumentRtException( ERR_WRONG_QUERY, act, aet, st, et );
    }
    // Признак необходимости выбирать одно значение перед левой границей
    boolean includeBefore = false;
    // Признак необходимости выбирать одно значение за правой границей
    boolean includeAfter = false;
    switch( aInterval.type() ) {
      case CSCE:
        break;
      case OSCE:
        includeBefore = true;
        break;
      case CSOE:
        includeAfter = true;
        break;
      case OSOE:
        includeBefore = true;
        includeAfter = true;
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    // Инициализация запроса
    ITimedListEdit<V> retValue = new TimedList<>();
    int blockIndex = -1;
    int blockValueIndex = -1;
    // Индекс первого блока в последовательности который имеет значения запроса. < 0: нет блоков
    int firstBlockIndex = binarySearch( blocks, queryStartTime );
    if( firstBlockIndex < 0 ) {
      // Нет блоков
      return retValue;
    }
    // Первый блок в последовательности который имеет значения запроса. null: нет значений
    ISequenceBlockEdit<V> firstBlock = null;
    // При разрешении получать значения до aStartTime смещаемся на один блок назад до первого не пустого блока
    for( int index = Math.min( firstBlockIndex + 1, blocks.size() - 1 ); index >= 0; index-- ) {
      ISequenceBlockEdit<V> block = blocks.get( index );
      if( queryStartTime <= block.endTime() ) {
        firstBlock = block;
        firstBlockIndex = index;
        continue;
      }
      if( queryStartTime > block.endTime() ) {
        if( includeBefore ) {
          firstBlock = block;
          firstBlockIndex = index;
        }
        break;
      }
    }
    if( firstBlock == null ) {
      // Не найдены значения для выполнения запроса
      return retValue;
    }
    if( queryStartTime < firstBlock.startTime() ) {
      // Блок ЗА началом запроса
      blockIndex = firstBlockIndex;
      blockValueIndex = 0;
    }
    if( firstBlock.endTime() < queryStartTime && includeBefore ) {
      // Блок ПЕРЕД началом запроса при РАЗРЕШЕНИИ получать значения до aStartTime
      blockIndex = firstBlockIndex;
      blockValueIndex = firstBlock.size() - 1;
    }
    if( firstBlock.startTime() <= queryStartTime && queryStartTime <= firstBlock.endTime() ) {
      long lookupTime = Math.max( queryStartTime, firstBlock.startTime() );
      blockIndex = firstBlockIndex;
      blockValueIndex = firstBlock.firstByTime( alignByDDT( typeInfo, lookupTime ) );
      if( includeBefore && queryStartTime < firstBlock.timestamp( blockValueIndex ) ) {
        // При РАЗРЕШЕНИИ получать значения до aStartTime смещаемся на одно значение назад
        blockValueIndex--;
      }
    }
    // Ошибка логики
    TsInternalErrorRtException.checkTrue( blockIndex < 0 || blockValueIndex < 0 );
    // Обработка
    for( ; blockIndex < blocks.size(); blockIndex++ ) {
      ISequenceBlockEdit<V> block = blocks.get( blockIndex );
      for( ; blockValueIndex < blocks.get( blockIndex ).size(); blockValueIndex++ ) {
        V value = block.getValue( blockValueIndex );
        long timestamp = value.timestamp();
        if( timestamp < queryStartTime && !includeBefore ) {
          // Значение еще не попадает в запрос
          continue;
        }
        if( timestamp > queryEndTime ) {
          // Найдено первое значение за интервалом набора. Добавляем в результат (открыта правая граница)
          if( includeAfter ) {
            retValue.add( value );
          }
          return retValue;
        }
        // Добавление очередного значения
        retValue.add( value );
      }
      blockValueIndex = 0;
    }
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequenceEdit
  //
  @Override
  public void setInterval( IQueryInterval aInterval ) {
    TsNullArgumentRtException.checkNull( aInterval );
    // Параметры нового интервала и сам интервал выравненный по слоту
    EQueryIntervalType newType = aInterval.type();
    long newStartTime = alignByDDT( typeInfo, aInterval.startTime() );
    long newEndTime = alignByDDT( typeInfo, aInterval.endTime() );
    // Параметры предыдущего интервала
    EQueryIntervalType type = queryInterval.type();
    long prevStartTime = queryInterval.startTime();
    long prevEndTime = queryInterval.endTime();

    if( prevStartTime == newStartTime && prevEndTime == newEndTime && type == newType ) {
      // Ничего не изменилось
      return;
    }
    // Установка нового интервала
    queryInterval = new QueryInterval( newType, newStartTime, newEndTime );
    if( newStartTime < prevStartTime && prevEndTime < newEndTime ) {
      // Старый интервал является подмножеством нового
      return;
    }
    if( newEndTime < prevStartTime || prevEndTime < newStartTime ) {
      // Не один блок последовательности не попадает в новый интервал времени
      blocks.clear();
      setCurrTime( newStartTime );
      return;
    }
    // Количество блоков в последовательности
    if( blocks.size() == 0 ) {
      // Нет блоков. Курсор на начало последовательности
      setCurrTime( newStartTime );
      return;
    }
    // Список блоков которые будут составлять будущую последовательность
    IListEdit<ISequenceBlockEdit<V>> oldBlocks = new ElemLinkedList<>( blocks );
    // Очистка списка блоков последовательности
    blocks.clear();
    // Индекс первого блока
    int firstBlockIndex = binarySearch( oldBlocks, newStartTime );
    for( int index = firstBlockIndex, n = oldBlocks.size(); index < n; index++ ) {
      // Исходный блок
      ISequenceBlockEdit<V> sourceBlock = oldBlocks.get( index );
      // Целевой блок
      ISequenceBlockEdit<V> targetBlock = null;
      if( sourceBlock.endTime() < newStartTime ) {
        // Блок завершается раньше последовательности
        continue;
      }
      if( newEndTime < sourceBlock.startTime() ) {
        // Начало блока позже завершения последовательности
        break;
      }
      if( sourceBlock.startTime() < newStartTime ) {
        // Блок начинается раньше последовательности. Берем его часть(хвост)
        long blockStartTime = alignByDDT( typeInfo, newStartTime );
        long blockEndTime = alignByDDT( typeInfo, Math.min( sourceBlock.endTime(), newEndTime ) );
        targetBlock = sourceBlock.createBlockOrNull( typeInfo, blockStartTime, blockEndTime );
      }
      if( newEndTime < sourceBlock.endTime() ) {
        // Блок завершается позже последовательности. Берем его часть(голову)
        long blockStartTime = alignByDDT( typeInfo, Math.max( sourceBlock.startTime(), newStartTime ) );
        long blockEndTime = alignByDDT( typeInfo, newEndTime );
        targetBlock = sourceBlock.createBlockOrNull( typeInfo, blockStartTime, blockEndTime );
      }
      if( sourceBlock.startTime() >= newStartTime && newEndTime >= sourceBlock.endTime() ) {
        // Блок целиком находится в диапазоне последовательности. Перемещение
        targetBlock = sourceBlock;
      }
      if( targetBlock == null ) {
        // Попытка создать новый блок для указанных интервалов провалилась (нет значений, получается пустой блок)
        continue;
      }
      // Добавляем блок в последовательность
      safeAddBlock( typeInfo, gwid, blocks, targetBlock, logger() );
    }
    if( cursorTime < newStartTime || cursorTime > newEndTime ) {
      // Курсор оказался за пределами нового интервала. Позиционируем на начало последовательности
      setCurrTime( newStartTime );
    }
  }

  @Override
  public boolean editable( IS5Sequence<V> aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    // Фактические(с учетом типа интервала) границы исходной и целевой последовательностей
    long sourceFactStartTime = factStartTime( aSource );
    long sourceFactEndTime = factEndTime( aSource );
    long targetFactStartTime = factStartTime( this );
    long targetFactEndTime = factEndTime( this );
    if( sourceFactEndTime < targetFactStartTime || targetFactEndTime < sourceFactStartTime ) {
      // Частный случай: последовательности не пересекаются. Нечего редактировать
      return false;
    }
    return true;
  }

  @Override
  public void set( ITimedList<V> aValues ) {
    TsNullArgumentRtException.checkNull( aValues );
    int valueCount = aValues.size();
    if( valueCount == 0 ) {
      // Нет значений
      return;
    }
    // Формирование блока для новых значений
    ISequenceBlockEdit<V> block = factory().createBlock( gwid, aValues );
    // Интервал блока
    long blockStartTime = block.startTime();
    long blockEndTime = block.endTime();
    // Проверка того, что блок попадает в последовательность
    if( blockStartTime < alignByDDT( typeInfo, queryInterval.startTime() )
        || alignByDDT( typeInfo, queryInterval.endTime() ) < blockEndTime ) {
      // Попытка установить список значений в последовательности вне ее интервала
      String fv = aValues.get( 0 ).toString();
      String lv = aValues.get( valueCount - 1 ).toString();
      String bst = timestampToString( blockStartTime );
      String est = timestampToString( blockEndTime );
      throw new TsIllegalArgumentRtException( ERR_OUT_VALUE_SEQENCE, typeInfo, fv, lv, bst, est, queryInterval );
    }
    try {
      // Удаление старых значений с удалением блоков полностью попадающих в интервал удаления
      remove( blockStartTime, blockEndTime, new ElemLinkedList<>() );
      // Индекс ближайщего блока в зоне blockStartTime
      int nearest = binarySearch( blocks, blockStartTime );
      ISequenceBlockEdit<V> nearestBlock = (nearest < 0 ? null : blocks.get( nearest ));
      // Добавление блока в последовательность
      if( nearestBlock == null ) {
        safeAddBlock( typeInfo, gwid, blocks, block, logger() );
        return;
      }
      // Проверяем можем ли мы добавить значение в найденный блок
      if( blockStartTime < nearestBlock.startTime() ) {
        // Добавление перед найденным блоком
        safeInsertBlock( typeInfo, gwid, blocks, nearest, block );
        return;
      }
      if( nearestBlock.endTime() < blockStartTime ) {
        // Добавление после найденного блока
        safeInsertBlock( typeInfo, gwid, blocks, nearest + 1, block );
        return;
      }
      // Недопустимая логика
      throw new TsInternalErrorRtException();
    }
    finally {
      // Восстанавливаем позицию курсора
      setCurrTime( cursorTime );
    }
  }

  @Override
  public boolean edit( IS5Sequence<V> aSource, IListEdit<ISequenceBlock<V>> aRemovedBlocks ) {
    TsNullArgumentRtException.checkNulls( aSource, aRemovedBlocks );
    if( !aSource.gwid().equals( gwid ) ) {
      // Нельзя редактировать последовательность значениями другого данного
      throw new TsIllegalArgumentRtException( ERR_WRONG_SOURCE, gwid, aSource.gwid() );
    }
    if( !editable( aSource ) ) {
      // Частный случай: последовательности не пересекаются. Нечего редактировать
      return false;
    }
    // Сброс признака фактического редактирования
    edited = false;
    try {
      // Интервал значений которые попадают в целевую последовательность
      long fetchStartTime = alignByDDT( typeInfo, getFetchStartTime( this, aSource ) );
      long fetchEndTime = alignByDDT( typeInfo, getFetchEndTime( this, aSource ) );
      int targetIndex = remove( fetchStartTime, fetchEndTime, aRemovedBlocks );
      if( aSource.blocks().size() == 0 ) {
        // Частный случай: в исходной последовательности нет блоков. Задан только интервал (удаление пустой
        // последовательностью через ее интервал).
        return edited;
      }
      // Признак того, что последовательность содержит синхронные значения
      boolean isSync = OP_IS_SYNC.getValue( typeInfo.params() ).asBool();
      // Интервал (мсек) синхронных значений. Для асинхронных: 1
      long syncDT = (isSync ? OP_SYNC_DT.getValue( typeInfo.params() ).asLong() : 1);
      for( int index = 0, n = aSource.blocks().size(); index < n; index++ ) {
        // Исходный блок
        ISequenceBlockEdit<V> sourceBlock = (ISequenceBlockEdit<V>)aSource.blocks().get( index );
        // Целевой блок
        ISequenceBlockEdit<V> targetBlock = null;

        if( sourceBlock.size() == 0 ) {
          // В блоке нет значений
          continue;
        }
        if( fetchStartTime > sourceBlock.endTime() ) {
          // Блок завершается раньше последовательности
          continue;
        }
        if( fetchEndTime < sourceBlock.startTime() ) {
          // Начало блока позже завершения последовательности
          break;
        }
        if( sourceBlock.startTime() < fetchStartTime && fetchEndTime < sourceBlock.endTime() ) {
          // Интервал блока полностью покрывает интервал последовательности. Вырезаем из блока интервал
          // последовательности
          targetBlock = sourceBlock.createBlockOrNull( typeInfo, fetchStartTime, fetchEndTime );
        }
        if( targetBlock == null && fetchStartTime <= sourceBlock.startTime()
            && sourceBlock.endTime() <= fetchEndTime ) {
          // Блок целиком находится в диапазоне последовательности. Перемещение
          targetBlock = sourceBlock;
        }
        if( targetBlock == null && sourceBlock.startTime() < fetchStartTime ) {
          // Блок начинается раньше последовательности. Берем его часть(хвост)
          targetBlock = sourceBlock.createBlockOrNull( typeInfo, fetchStartTime, sourceBlock.endTime() );
        }
        if( targetBlock == null && fetchEndTime < sourceBlock.endTime() ) {
          // Блок завершается позже последовательности. Берем его часть(голову)
          targetBlock = sourceBlock.createBlockOrNull( typeInfo, sourceBlock.startTime(), fetchEndTime );
        }
        if( targetBlock == null ) {
          // Попытка создать новый блок для указанных интервалов провалилась (нет значений, получается пустой блок)
          continue;
        }
        // Проверка того, что частота значений синхронных данных не меньше определенного описанием данного
        if( isSync ) {
          ISequenceBlockEdit<V> prevBlock = (targetIndex > 0 ? blocks.get( targetIndex - 1 ) : null);
          if( prevBlock != null && (prevBlock.endTime() + syncDT > targetBlock.startTime()) ) {
            // Метки времени значений идут чаще чем определяет параметр DataDelta для синхронных данных
            Long dd = Long.valueOf( syncDT );
            String st = timestampToString( targetBlock.startTime() );
            String et = timestampToString( targetBlock.endTime() );
            String pst = timestampToString( prevBlock.startTime() );
            String pet = timestampToString( prevBlock.endTime() );
            logger().warning( ERR_SYNC_INEFFECTIVE, typeInfo, dd, st, et, pst, pet );
          }
        }
        // Вставляем блок в целевую последовательность
        blocks.insert( targetIndex++, targetBlock );
        // Признак проведенного редактирования (частично избыточно - он мог быть уже установлен при удалении блоков)
        edited = true;
      }
      return edited;
    }
    finally {
      // Восстанавливаем позицию курсора
      setCurrTime( cursorTime );
    }
  }

  @Override
  public IList<ISequenceBlockEdit<V>> uniteBlocks() {
    try {
      if( blocks.size() <= 0 ) {
        // Нечего объединять
        return IList.EMPTY;
      }
      // Количество значений полного блока (количество значений)
      int blockSizeMax = OP_BLOCK_SIZE_MAX.getValue( typeInfo.params() ).asInt();
      // Двойной максимальный размерр блока
      Integer doubleMaxSize = Integer.valueOf( 2 * blockSizeMax );
      // Количество блоков в последовательности
      int blockQtty = blocks.size();
      // Список всех блоков которые были объединены и должны быть выведены (удалены) из последовательности
      IListEdit<ISequenceBlockEdit<V>> allRemoved = new ElemLinkedList<>();
      // Список новых блоков составляющих последовательность
      IListEdit<ISequenceBlockEdit<V>> newBlocks = new ElemLinkedList<>();
      // targetIndex + 1: последний блок последовательности не с чем объединять
      for( int targetIndex = 0; targetIndex < blockQtty; targetIndex++ ) {
        // Блок с которым будет объединение
        ISequenceBlockEdit<V> targetBlock = blocks.get( targetIndex );
        try {
          if( targetBlock.size() > doubleMaxSize.intValue() ) {
            // Количество в блоке больше допустимого более чем в 2 раза (изменилось описание). Требуется необъединять,
            // а разделить блок Разделяемый блок сохраняем в будущей последовательности
            safeAddBlock( typeInfo, gwid, newBlocks, targetBlock, uniterLogger() );
            while( targetBlock.size() > doubleMaxSize.intValue() ) {
              int nextBlockStartIndex = doubleMaxSize.intValue();
              int nextBlockEndIndex = targetBlock.size() - 1;
              Integer oldSize = Integer.valueOf( targetBlock.size() );
              // Создаем новый блок
              long blockStartTime = targetBlock.timestamp( nextBlockStartIndex );
              long blockEndTime = targetBlock.timestamp( nextBlockEndIndex );
              ISequenceBlockEdit<V> newBlock = targetBlock.createBlockOrNull( typeInfo, blockStartTime, blockEndTime );
              // newBlock не может быть = null так как интервал был сформирован по индексам значений этого же блока
              TsInternalErrorRtException.checkNull( newBlock );
              // Редактируем разделямый блок
              targetBlock.editEndTime( targetBlock.timestamp( nextBlockStartIndex - 1 ) );
              // Добавляем блок в последовательность
              safeAddBlock( typeInfo, gwid, newBlocks, newBlock, uniterLogger() );
              // Сообщение для журнала
              uniterLogger().warning( MSG_SPLIT_BLOCK, typeInfo, oldSize, doubleMaxSize, Long.valueOf( blockSizeMax ),
                  targetBlock );
              // Меняем цель
              targetBlock = newBlock;
            }
            continue;
          }
        }
        catch( Throwable e ) {
          // Неожиданная ошибка дефрагментации
          throw new TsInternalErrorRtException( e, ERR_UNION_BLOCKS_UNEXPECTED, cause( e ) );
        }
        // Кандидаты на объединение с targetBlock
        IListEdit<ISequenceBlockEdit<V>> candidates = new ElemLinkedList<>();
        for( int index = targetIndex + 1; index < blockQtty; index++ ) {
          candidates.add( blocks.get( index ) );
        }
        // Проводим объединение и получаем количество объединенных блоков
        int unitedQtty = targetBlock.uniteBlocks( factory(), candidates, uniterLogger() );
        // Блок с которым было объединение сохраняем в будущей последовательности
        safeAddBlock( typeInfo, gwid, newBlocks, targetBlock, uniterLogger() );
        // Блоки которые были объединены перемещаем в общий список удаляемых(объединенных) блоков
        for( int index = 0; index < unitedQtty; index++ ) {
          allRemoved.add( candidates.get( index ) );
        }
        // Переходим на следующий первый блок с которым будет объединение
        targetIndex += unitedQtty;
      }
      // Замещаем список блоков составляющих последовательность
      blocks.clear();
      blocks.addAll( newBlocks );
      return allRemoved;
    }
    finally {
      // Восстанавливаем позицию курсора
      setCurrTime( cursorTime );
    }
  }

  @Override
  public void clear() {
    blocks.clear();
    setCurrTime( queryInterval.startTime() );
  }

  // ------------------------------------------------------------------------------------
  // API для наследников
  //
  /**
   * Возвращает фабрику формирования последовательности
   *
   * @return {@link ISequenceFactory} фабрика. null: последовательность только для чтения
   */
  protected final ISequenceFactory<V> factory() {
    if( factory == null ) {
      // Последовательность значений доступна только для чтения
      throw new TsIllegalStateRtException( ERR_SEQUENCE_READONLY );
    }
    return factory;
  }

  /**
   * Установить фабрику формирования последовательности
   *
   * @param aFactory {@link ISequenceFactory} фабрика
   * @throws TsNullArgumentRtException аргумент = null
   */
  public final void setFactory( ISequenceFactory<V> aFactory ) {
    TsNullArgumentRtException.checkNull( aFactory );
    factory = aFactory;
  }

  /**
   * Возвращает общий журнал работы
   *
   * @return {@link ILogger} журнал работы
   */
  protected final ILogger logger() {
    if( logger == null ) {
      logger = getLogger( getClass() );
    }
    return logger;
  }

  /**
   * * Возвращает журнал дефрагментации последовательностей
   *
   * @return {@link ILogger} журнал работы
   */
  protected final ILogger uniterLogger() {
    if( uniterLogger == null ) {
      uniterLogger = getLogger( LOG_UNITER_ID );
    }
    return uniterLogger;
  }

  /**
   * * Возвращает журнал проверки последовательностей
   *
   * @return {@link ILogger} журнал работы
   */
  protected final ILogger validatorLogger() {
    if( validatorLogger == null ) {
      validatorLogger = getLogger( LOG_VALIDATOR_ID );
    }
    return validatorLogger;
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    Integer blockCount = Integer.valueOf( blocks.size() );
    Integer valueCount = Integer.valueOf( getValuesCount( this ) );
    return format( TO_STRING_FORMAT, getClass().getSimpleName(), gwid, queryInterval, blockCount, valueCount );
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + typeInfo.hashCode();
    result = TsLibUtils.PRIME * result + gwid.hashCode();
    result = TsLibUtils.PRIME * result + queryInterval.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object aObj ) {
    if( aObj == this ) {
      return true;
    }
    if( !(aObj instanceof IS5Sequence<?> other) ) {
      return false;
    }
    if( !gwid.equals( gwid ) ) {
      return false;
    }
    if( !queryInterval.equals( other.interval() ) ) {
      return false;
    }
    return true;
  }

  // ------------------------------------------------------------------------------------
  // Сериализация S5Sequence
  //
  private void readObject( ObjectInputStream aIn )
      throws IOException,
      ClassNotFoundException {
    aIn.defaultReadObject();
    // Инициализация курсора после десериализации.
    setCurrTime( queryInterval.startTime() );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Удаляет из последовательности данные за указанный интервал времени
   *
   * @param aStartTime long время(мсек с начала эпохи) начала (включительно) удаления данных.
   * @param aEndTime long время(мсек с начала эпохи) завершения (включительно) удаления данных.
   * @param aRemovedBlocks {@link IListEdit} редактируемый список удаленных блоков последовательности
   * @return int индекс блока последовательности следующий за первым блоком не попадащим в интервал
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException aStartTime > aEndTime
   */
  @SuppressWarnings( "unchecked" )
  private int remove( long aStartTime, long aEndTime, IListEdit<ISequenceBlock<V>> aRemovedBlocks ) {
    // Проверка интервала времени
    checkIntervalArgs( aStartTime, aEndTime );
    if( blocks.size() == 0 ) {
      // Частный случай: нет блоков
      return 0;
    }
    if( aStartTime <= blocks.get( 0 ).startTime() && blocks.get( blocks.size() - 1 ).endTime() <= aEndTime ) {
      // Частный случай: удаляются все блоки
      aRemovedBlocks.addAll( (IList<ISequenceBlock<V>>)(Object)blocks );
      blocks.clear();
      edited = true;
      return 0;
    }
    // Признак того, что последовательность содержит синхронные значения
    boolean isSync = OP_IS_SYNC.getValue( typeInfo.params() ).asBool();
    // Интервал (мсек) синхронных значений. Для асинхронных: 1
    long syncDT = (isSync ? OP_SYNC_DT.getValue( typeInfo.params() ).asLong() : 1);
    // Возвращаемый результат
    int retValue = blocks.size();
    for( int index = blocks.size() - 1; index >= 0; index-- ) {
      ISequenceBlockEdit<V> block = blocks.get( index );
      if( block.endTime() < aStartTime ) {
        // Блок завершается раньше интервала удаления. Дальше искать не имеет смысла
        retValue = index + 1;
        break;
      }
      if( aEndTime < block.startTime() ) {
        // Начало блока позже интервала удаления
        retValue = index;
        continue;
      }
      if( block.size() == 0 ) {
        // Блок пустой
        aRemovedBlocks.add( blocks.removeByIndex( index ) );
        retValue = index;
        edited = true;
        continue;
      }
      if( aStartTime <= block.startTime() && block.endTime() <= aEndTime ) {
        // Блок удаляется целиком
        aRemovedBlocks.add( blocks.removeByIndex( index ) );
        retValue = index;
        edited = true;
        continue;
      }
      if( block.startTime() < aStartTime && aEndTime < block.endTime() ) {
        // Блок полностью покрывает интервал удаления
        ISequenceBlockEdit<V> tailBlock = block.createBlockOrNull( typeInfo, aEndTime + syncDT, block.endTime() );
        safeInsertBlock( typeInfo, gwid, blocks, index + 1, tailBlock );
        block.editEndTime( aStartTime - syncDT );
        retValue = index + 1;
        edited = true;
        break;
      }
      if( block.startTime() < aStartTime ) {
        // Блок начинается раньше интервала удаления. Удаляем его часть(хвост)
        block.editEndTime( aStartTime - syncDT );
        retValue = index + 1;
        edited = true;
        break;
      }
      if( aEndTime < block.endTime() ) {
        // Блок завершается позже интервала удаления. Удаляем его часть(голову)

        // TODO: 2019-11-08 mvk в API блока больше нет метода который позволял бы менять startTime (он первичный ключ)
        // Создаем копию блока без головы, старый блок удаляем
        // block.editBlock( typeInfo, aEndTime + syncDT, block.endTime() );
        ISequenceBlockEdit<V> tailBlock = block.createBlockOrNull( typeInfo, aEndTime + syncDT, block.endTime() );
        safeInsertBlock( typeInfo, gwid, blocks, index + 1, tailBlock );
        aRemovedBlocks.add( blocks.removeByIndex( index ) );

        retValue = index;
        edited = true;
        continue;
      }
      // Ошибка в определении условий (алгоритм не должен войти в это место)
      throw new TsInternalErrorRtException();
    }
    return retValue;
  }

  /**
   * С помощью бинарного поиска находит индекс блока в диапазон которого попадает указанная метка времени. Если такого
   * блока нет, то возвращает индекс ближайшего блока к искомой метке. Если блоков нет, то -1
   * <p>
   * Клиент должен проверять найденный блок на попадание в него метки времени, так как может возвращаться блок к
   * которому метка находится ближе(слева или справа), но не попадает в него.
   *
   * @param <V> тип значения
   * @param aBlocks {@link IList}&lt;{@link ISequenceBlockEdit}&gt; список блоков
   * @param aTimestamp long метка времени для которой определяется индекс.
   * @return индекс блока в который попадает метка времени или индекс ближайшего блока. < 0: пустой массив меток
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected static <V extends ITemporal<?>> int binarySearch( IList<ISequenceBlockEdit<V>> aBlocks, long aTimestamp ) {
    TsNullArgumentRtException.checkNull( aBlocks );
    int size = aBlocks.size();
    if( size == 0 ) {
      // Блоков нет
      return -1;
    }
    int lowIndex = 0;
    int highIndex = size - 1;
    while( lowIndex <= highIndex ) {
      int middleIndex = lowIndex + (highIndex - lowIndex) / 2;
      ISequenceBlockEdit<V> block = aBlocks.get( middleIndex );
      if( aTimestamp < block.startTime() ) {
        // Смещение к началу
        highIndex = middleIndex - 1;
      }
      else {
        if( aTimestamp > block.endTime() ) {
          // Смещение к концу
          if( lowIndex == size - 1 ) {
            // Смещение к концу уже невозможно (выход за границы)
            break;
          }
          lowIndex = middleIndex + 1;
        }
        else {
          // Точное совпадение (нашли блок в диапазоне которого находится метка)
          return middleIndex;
        }
      }
    }
    // Блок в который должна входить метка - не найден. Возвращаем ближайший блок
    return lowIndex;
  }

  /**
   * Возвращает фактическое (с учетом типа интервала {@link IQueryInterval#type()}) время начала последовательности
   *
   * @param aSequence {@link IS5Sequence} последовательность
   * @return long метка времени (мсек с начала эпохи) начала последовательности
   * @throws TsNullArgumentRtException аргумент = null;
   */
  private static long factStartTime( IS5Sequence<?> aSequence ) {
    TsNullArgumentRtException.checkNull( aSequence );
    int count = aSequence.blocks().size();
    long startTime = aSequence.interval().startTime();
    if( startTime == MIN_TIMESTAMP ) {
      startTime += 1;
    }
    if( startTime == MAX_TIMESTAMP ) {
      startTime -= 1;
    }
    // Фактические(с учетом типа интервала) границы исходной и целевой последовательностей
    return (count > 0 ? Math.min( startTime, aSequence.blocks().first().startTime() ) : startTime);
  }

  /**
   * Возвращает фактическое (с учетом типа интервала {@link IQueryInterval#type()}) время завершения последовательности
   *
   * @param aSequence {@link IS5Sequence} последовательность
   * @return long метка времени (мсек с начала эпохи) завершения последовательности
   * @throws TsNullArgumentRtException аргумент = null;
   */
  private static long factEndTime( IS5Sequence<?> aSequence ) {
    TsNullArgumentRtException.checkNull( aSequence );
    int count = aSequence.blocks().size();
    long endTime = aSequence.interval().endTime();
    if( endTime == MIN_TIMESTAMP ) {
      endTime += 1;
    }
    if( endTime == MAX_TIMESTAMP ) {
      endTime -= 1;
    }
    // Фактические(с учетом типа интервала) границы исходной и целевой последовательностей
    return (count > 0 ? Math.max( endTime, aSequence.blocks().last().endTime() ) : endTime);
  }

  /**
   * Возвращает время начала выборки данных в целевую последовательсть из исходной
   *
   * @param aTarget {@link IS5Sequence} целевая последовательность
   * @param aSource {@link IS5Sequence} исходная последовательность
   * @return long время (мсек с начала эпохи) начала выборки
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static long getFetchStartTime( IS5Sequence<?> aTarget, IS5Sequence<?> aSource ) {
    TsNullArgumentRtException.checkNulls( aTarget, aSource );
    // Целевой интервал
    IQueryInterval targetInterval = aTarget.interval();
    // Метка начала интервала целевой последовательности
    long targetStartTime = targetInterval.startTime();
    // Исходный интервал
    IQueryInterval sourceInterval = aSource.interval();
    // Метка начала интервала исходной последовательности
    long sourceStartTime = sourceInterval.startTime();
    if( targetStartTime <= sourceStartTime ) {
      // Точное попадание левых границ последовательностей или исходная последовательность начинается позже целевой
      return sourceStartTime;
    }
    switch( targetInterval.type() ) {
      case CSCE:
      case CSOE:
        // Закрытая левая граница
        return targetStartTime;
      case OSCE:
      case OSOE:
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    // Проход по блокам исходной последовательности с поиском метки времени на левой границе или перед ней
    for( int blockIndex = aSource.blocks().size() - 1; blockIndex >= 0; blockIndex-- ) {
      ISequenceBlock<?> block = aSource.blocks().get( blockIndex );
      if( block.startTime() > targetStartTime ) {
        // Блок до левой границы
        continue;
      }
      // Проход по меткам блока с поиском метки на границе целевой последовательности или перед ней
      for( int index = block.size() - 1; index >= 0; index-- ) {
        long timestamp = block.timestamp( index );
        if( timestamp <= targetStartTime ) {
          return timestamp;
        }
      }
    }
    return targetStartTime;
  }

  /**
   * Возвращает время завершения выборки данных в целевую последовательсть из исходной
   *
   * @param aTarget {@link IS5Sequence} целевая последовательность
   * @param aSource {@link IS5Sequence} исходная последовательность
   * @return long время (мсек с начала эпохи) завершения выборки
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static long getFetchEndTime( IS5Sequence<?> aTarget, IS5Sequence<?> aSource ) {
    TsNullArgumentRtException.checkNulls( aTarget, aSource );
    // Целевой интервал
    IQueryInterval targetInterval = aTarget.interval();
    // Метка завершения интервала целевой последовательности
    long targetEndTime = targetInterval.endTime();
    // Исходный интервал
    IQueryInterval sourceInterval = aSource.interval();
    // Метка завершения интервала исходной последовательности
    long sourceEndTime = sourceInterval.endTime();
    if( targetEndTime >= sourceEndTime ) {
      // Точное попадание правых границ последовательностей или исходная последовательность заканчивается раньше целевой
      return sourceEndTime;
    }
    switch( targetInterval.type() ) {
      case CSCE:
      case OSCE:
        // Закрытая правая граница
        return targetEndTime;
      case CSOE:
      case OSOE:
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    // Проход по блокам исходной последовательности с поиском метки времени на левой границе или перед ней
    for( int blockIndex = 0, n = aSource.blocks().size(); blockIndex < n; blockIndex++ ) {
      ISequenceBlock<?> block = aSource.blocks().get( blockIndex );
      if( block.endTime() < targetEndTime ) {
        // Блок до правой границы
        continue;
      }
      // Проход по меткам блока с поиском метки на границе целевой последовательности или перед ней
      for( int index = 0, m = block.size(); index < m; index++ ) {
        long timestamp = block.timestamp( index );
        if( timestamp >= targetEndTime ) {
          return timestamp;
        }
      }
    }
    return targetEndTime;
  }

  /**
   * Добавляет блок в список с контролем порядка следования блоков (времени начала и конца)
   *
   * @param <V> тип значения
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aBlocks {@link IListEdit}&lt;{@link ISequenceBlockEdit}&lt;?&gt;&gt; список блоков
   * @param aBlock {@link ISequenceBlockEdit} добавляемый блок
   * @param aLogger {@link ILogger} журнал последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException попытка добавить в последовательность блок другого данного
   * @throws TsIllegalArgumentRtException невозможно добавить блок в конец списка
   */
  private static <V extends ITemporal<?>> void safeAddBlock( IParameterized aTypeInfo, Gwid aGwid,
      IListEdit<ISequenceBlockEdit<V>> aBlocks, ISequenceBlockEdit<V> aBlock, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aTypeInfo, aGwid, aBlocks, aBlock, aLogger );
    if( !aGwid.equals( aBlock.gwid() ) ) {
      // Попытка добавить значения в последовательность данных блок другого данного
      throw new TsIllegalArgumentRtException( ERR_ALIEN_BLOCK, aGwid, aBlock.gwid() );
    }
    if( aBlocks.size() == 0 ) {
      // Пустой список. Просто добавляем блок
      aBlocks.add( aBlock );
      return;
    }
    // Минимальный интервал времени между блоками значений
    // long minDataDeltaT = (aInfo.isSync() ? aInfo.syncDataDelta() : 1);
    long minDataDeltaT = 1;
    int size = aBlocks.size();
    if( size > 0 ) {
      ISequenceBlockEdit<V> prevBlock = aBlocks.get( size - 1 );
      if( prevBlock.endTime() + minDataDeltaT > aBlock.startTime() ) {
        // TODO: WORKAROUND mvk 2018-08-09 ошибка целостности последовательности данных
        // На tm проявилась следующая ситуация:
        // - Ошибка дефрагментации(union) СИНХРОННЫХ float-значений (приборы)
        // - В базе два соседних блока.
        // -- Первый: ~15 значений (count). Интервал с t0 по t1 включительно. t0 < t1.
        // -- Второй: 1 значение (count). Интервал с t1 по t1 включительно.
        // Таким образом, происходит "наезд" второго блока (count=1), на хвост первого (count =~15).
        boolean hasWorkarroundCase = (aBlock.size() == 1 && prevBlock.endTime() == aBlock.startTime());
        if( !hasWorkarroundCase ) {
          throw new TsIllegalArgumentRtException( ERR_CANT_ADD_PREV, aGwid, aBlock, prevBlock );
        }
        aLogger.error( ERR_CANT_ADD_PREV, aGwid, aBlock, prevBlock );
      }
      boolean isSync = OP_IS_SYNC.getValue( aTypeInfo.params() ).asBool();
      long syncDT = OP_SYNC_DT.getValue( aTypeInfo.params() ).asLong();
      if( isSync && (prevBlock.endTime() + syncDT > aBlock.startTime()) ) {
        // Метки времени значений идут чаще чем определяет параметр syncDT для синхронных данных
        Long dd = Long.valueOf( syncDT );
        String st = timestampToString( aBlock.startTime() );
        String et = timestampToString( aBlock.endTime() );
        String pst = timestampToString( prevBlock.startTime() );
        String pet = timestampToString( prevBlock.endTime() );
        aLogger.warning( ERR_SYNC_INEFFECTIVE, aGwid, dd, st, et, pst, pet );
      }
    }
    aBlocks.add( aBlock );
  }

  /**
   * Вставляет блок в список по индексу с контролем порядка следования блоков (времени начала и конца)
   *
   * @param <V> тип значения
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aGwid {@link Gwid} идентификатор данного
   * @param aBlocks {@link IListEdit}&lt;{@link ISequenceBlockEdit}&lt;?&gt;&gt; список блоков
   * @param aIndex int индекс по которому размещается блок
   * @param aBlock {@link ISequenceBlockEdit} добавляемый блок
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException попытка добавить в последовательность блок другого данного
   * @throws TsIllegalArgumentRtException невозможно добавить блок в конец списка
   */
  private static <V extends ITemporal<?>> void safeInsertBlock( IParameterized aTypeInfo, Gwid aGwid,
      IListEdit<ISequenceBlockEdit<V>> aBlocks, int aIndex, ISequenceBlockEdit<V> aBlock ) {
    TsNullArgumentRtException.checkNulls( aTypeInfo, aGwid, aBlocks, aBlock );
    if( !aGwid.equals( aBlock.gwid() ) ) {
      // Попытка добавить значения в последовательность данных блок другого данного
      throw new TsIllegalArgumentRtException( ERR_ALIEN_BLOCK, aGwid, aBlock.gwid() );
    }
    if( aBlocks.size() == 0 ) {
      // Пустой список. Просто вставляем блок
      aBlocks.insert( aIndex, aBlock );
      return;
    }
    boolean isSync = OP_IS_SYNC.getValue( aTypeInfo.params() ).asBool();
    // Минимальный интервал времени между блоками значений
    long dataDeltaT = (isSync ? OP_SYNC_DT.getValue( aTypeInfo.params() ).asLong() : 1);
    int size = aBlocks.size();
    if( size > 0 && aIndex > 0 && aIndex < size - 1 ) {
      ISequenceBlockEdit<V> prevBlock = aBlocks.get( aIndex - 1 );
      if( prevBlock.endTime() + dataDeltaT > aBlock.startTime() ) {
        throw new TsIllegalArgumentRtException( ERR_CANT_ADD_PREV, aGwid, aBlock, prevBlock );
      }
    }
    if( size > 0 && aIndex > 0 && aIndex + 1 < size ) {
      ISequenceBlockEdit<V> nextBlock = aBlocks.get( aIndex + 1 );
      if( aBlock.endTime() + dataDeltaT > nextBlock.startTime() ) {
        String st = timestampToString( aBlock.startTime() );
        String et = timestampToString( aBlock.endTime() );
        String nst = timestampToString( nextBlock.startTime() );
        String net = timestampToString( nextBlock.endTime() );
        throw new TsIllegalArgumentRtException( ERR_CANT_ADD_NEXT, aGwid, st, et, nst, net );
      }
    }
    aBlocks.insert( aIndex, aBlock );
  }
}
