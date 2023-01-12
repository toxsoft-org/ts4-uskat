package org.toxsoft.uskat.s5.server.sequences.impl;

import static java.lang.String.*;
import static org.toxsoft.core.tslib.bricks.time.impl.TimeUtils.*;
import static org.toxsoft.uskat.s5.server.sequences.IS5SequenceHardConstants.*;
import static org.toxsoft.uskat.s5.server.sequences.impl.IS5Resources.*;

import java.util.List;

import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.QueryInterval;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.bricks.validator.IValResList;
import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.bricks.validator.impl.ValResList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.sequences.*;

/**
 * Открытые методы доступам к последовательностям значений
 *
 * @author mvk
 */
public class S5SequenceUtils {

  // ------------------------------------------------------------------------------------
  // Методы обработки последовательностей
  //
  /**
   * Возвращает количество значений в последовательности
   *
   * @param aSequence {@link IS5Sequence} последовательность значений
   * @return int количество значений в последовательности
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static int getValuesCount( IS5Sequence<?> aSequence ) {
    TsNullArgumentRtException.checkNull( aSequence );
    int retValue = 0;
    for( int index = 0, n = aSequence.blocks().size(); index < n; index++ ) {
      IS5SequenceBlock<?> block = aSequence.blocks().get( index );
      retValue += block.size();
    }
    return retValue;
  }

  /**
   * Возвращает фактическое время начала данных в указанных последовательностях
   * <p>
   * Фактическое время начала данных определяется как минимальное время блоков входящих в последовательности
   * {@link IS5SequenceBlock#startTime()} . При этом следует учитывать, что для блоков с асинхронными значениями по
   * возвращаемому времени данных может и не быть. Если все последовательности пустые, то возвращается
   * {@link TimeUtils#MAX_TIMESTAMP}.
   *
   * @param aSequences {@link IS5Sequence} последовательности значений
   * @return long время (мсек с начала эпохи) с которого фактически начинаются (включительно) данные
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static long getFactStartTime( IList<IS5Sequence<?>> aSequences ) {
    TsNullArgumentRtException.checkNull( aSequences );
    long retValue = MAX_TIMESTAMP;
    for( IS5Sequence<?> sequence : aSequences ) {
      if( sequence.blocks().size() == 0 ) {
        // Пустая последовательность
        continue;
      }
      // В последовательности есть блоки значений
      IS5SequenceBlock<?> firstBlock = sequence.blocks().first();
      if( retValue > firstBlock.startTime() ) {
        // Нашли блок с более ранним началом значений
        retValue = firstBlock.startTime();
      }
    }
    return retValue;
  }

  /**
   * Возвращает фактическое время завершения данных в указанных последовательностях
   * <p>
   * Фактическое время завершения данных определяется как максимальное время блоков входящих в последовательности
   * {@link IS5SequenceBlock#endTime()} . При этом следует учитывать, что для блоков с асинхронными значениями по
   * возвращаемому времени данных может и не быть. Если все последовательности пустые, то возвращается
   * {@link TimeUtils#MIN_TIMESTAMP}.
   *
   * @param aSequences {@link IS5Sequence} последовательности значений
   * @return long время (мсек с начала эпохи) с которого фактически завершаются (включительно) данные
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static long getFactEndTime( IList<IS5Sequence<?>> aSequences ) {
    TsNullArgumentRtException.checkNull( aSequences );
    long retValue = MIN_TIMESTAMP;
    for( IS5Sequence<?> sequence : aSequences ) {
      if( sequence.blocks().size() == 0 ) {
        // Пустая последовательность
        continue;
      }
      // В последовательности есть блоки значений
      IS5SequenceBlock<?> lastBlock = sequence.blocks().last();
      if( retValue < lastBlock.endTime() ) {
        // Нашли блок с более поздним завершением значений
        retValue = lastBlock.endTime();
      }
    }
    return retValue;
  }

  /**
   * Вовзращает количество блоков которое может быть запрошено при текущем состоянии java-heap
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aBlockSizes {@link List}&lt;Integer&gt; список размеров(байт) читаемых блоков.
   * @param aFromPostion int позиция в aBlockSizes с которой обрабатывать список
   * @param aFreeRatio double макс.граница используемой для запроса свободной памяти (%/100)
   * @param aMemoryLog {@link StringBuilder} журнал для состояния java heap
   * @return int количество блоков
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException aBlockSizes.size() == 0
   * @throws TsIllegalArgumentRtException aFromPosition < 0 или aFromPosition >= aBlockSizes.size()
   * @throws TsIllegalArgumentRtException aFreeRatio <= 0 или aFreeRatio > 1
   */
  public static int calcBlockCountByMemory( IParameterized aTypeInfo, List<Integer> aBlockSizes, int aFromPostion,
      double aFreeRatio, StringBuilder aMemoryLog ) {
    TsNullArgumentRtException.checkNulls( aTypeInfo, aMemoryLog );
    TsIllegalArgumentRtException.checkTrue( aBlockSizes.size() == 0 );
    TsIllegalArgumentRtException.checkTrue( aFromPostion < 0 || aFromPostion >= aBlockSizes.size() );
    TsIllegalArgumentRtException.checkTrue( aFreeRatio <= 0 || aFreeRatio > 1 );
    int mb = 1024 * 1024;
    double maxMemoryBytes = 0;

    // Признак того, что последовательность содержит синхронные значения
    boolean isSync = OP_IS_SYNC.getValue( aTypeInfo.params() ).asBool();
    // Максимальный размер значения
    int valueSize = OP_VALUE_SIZE_MAX.getValue( aTypeInfo.params() ).asInt();
    if( !isSync ) {
      // Учитываем хранение меток времени асинхронных значений
      valueSize += 8;
    }
    Runtime runtime = Runtime.getRuntime();
    double free = runtime.freeMemory();
    double total = runtime.totalMemory();
    double max = runtime.maxMemory();
    double allocated = total - free;
    double presumableFree = max - allocated;

    // /2: допускаем использовать не более половины свободной памяти
    maxMemoryBytes = presumableFree * aFreeRatio;

    int countBlock = 0;
    int sequenceMemoryBytes = 0;
    for( int index = aFromPostion, n = aBlockSizes.size(); index < n; index++ ) {
      int size = aBlockSizes.get( index ).intValue();
      int blockMemoryBytes = size * valueSize;
      if( sequenceMemoryBytes + blockMemoryBytes > maxMemoryBytes ) {
        // Больше нельзя читать блоков
        break;
      }
      sequenceMemoryBytes += blockMemoryBytes;
      countBlock++;
    }
    // Журнал состояния памяти
    Long freeL = Long.valueOf( (long)(free / mb) );
    Long allocatedL = Long.valueOf( (long)(allocated / mb) );
    Long totalL = Long.valueOf( (long)(total / mb) );
    Long maxL = Long.valueOf( (long)(max / mb) );
    Long pfL = Long.valueOf( (long)(presumableFree / mb) );
    String log = format( MSG_CALC_AVAILABLE_BLOCK, freeL, allocatedL, totalL, maxL, pfL, Long.valueOf( countBlock ) );
    aMemoryLog.append( log );

    // Возвращение результата с контролем верхней границы
    return countBlock;
  }

  /**
   * Вовзращает признак того, что значения последовательности начинаются со значений указанного блока
   *
   * @param aSequence {@link IS5Sequence} последовательность значений
   * @param aBlock {@link IS5SequenceBlock} блок значений
   * @return {@link IValResList} результат проверки значений
   * @param <S> тип последовательности значений данного
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException aBlockSizes.size() == 0
   * @throws TsIllegalArgumentRtException aFromPosition < 0 или aFromPosition >= aBlockSizes.size()
   * @throws TsIllegalArgumentRtException aFreeRatio <= 0 или aFreeRatio > 1
   */
  public static <S extends IS5Sequence<V>, V extends ITemporal<?>> IValResList sequenceStartWithValues( S aSequence,
      IS5SequenceBlock<V> aBlock ) {
    ValResList retValue = new ValResList();
    TsNullArgumentRtException.checkNulls( aSequence, aBlock );
    if( aBlock.size() == 0 ) {
      // Пустой блок
      retValue.add( ValidationResult.error( MSG_EMPTY_BLOCK ) );
      return retValue;
    }
    if( aSequence.blocks().size() == 0 ) {
      // Пустая последовательность
      retValue.add( ValidationResult.error( MSG_EMPTY_SEQUENCE ) );
      return retValue;
    }
    if( !aSequence.gwid().equals( aBlock.gwid() ) ) {
      // У данных разный идентификатор
      retValue.add( ValidationResult.error( MSG_DIFFERENT_BY_DATA_ID ) );
      return retValue;
    }
    ITimeInterval interval = aSequence.interval();
    long startTime = interval.startTime();
    if( startTime < aBlock.startTime() || startTime > aBlock.endTime() ) {
      // Начало последовательности вне интервала блока
      retValue.add( ValidationResult.error( MSG_START_SEQUENCE_OUT_OF_BLOCK ) );
      return retValue;
    }
    // Курсоры значений
    IS5SequenceCursor<V> sequenceCursor = aSequence.createCursor();
    IS5SequenceCursor<V> blockCursor = aBlock.createCursor();
    sequenceCursor.setTime( startTime );
    // Проверка значений
    while( blockCursor.hasNextValue() ) {
      V blockValue = blockCursor.nextValue();
      long blockTime = blockValue.timestamp();
      if( blockTime < startTime ) {
        // Текущее значение блока более ранее
        continue;
      }
      // TODO: 2022-10-23 mvk перевести чтение значений блока через курсор
      V sequenceValue = (sequenceCursor.hasNextValue() ? sequenceCursor.nextValue() : null);
      if( sequenceValue == null ) {
        // В последовательности нет значения соответствующего значению блока
        retValue.add( ValidationResult.error( MSG_AT_SEQUENCE_NOT_FOUND_VALUE ) );
        break;
      }
      long sequenceTime = sequenceValue.timestamp();
      if( sequenceTime != blockTime ) {
        Integer bi = Integer.valueOf( blockCursor.position() );
        String st = timestampToString( sequenceTime );
        String bt = timestampToString( blockTime );
        // Различие по времени
        retValue.add( ValidationResult.error( MSG_DIFFERENT_BY_TIME, bi, bt, st ) );
        break;
      }
      if( !sequenceValue.equals( blockValue ) ) {
        Integer bi = Integer.valueOf( blockCursor.position() );
        String bt = timestampToString( blockTime );
        // Различие по значению
        retValue.add( ValidationResult.error( MSG_DIFFERENT_BY_VALUE, bi, bt, blockValue, sequenceValue ) );
        break;
      }
    }
    return retValue;
  }

  /**
   * Возвращает признак того, что исходная последовательность имеет пересечение с целевой последовательностью и значения
   * в этом пересечении одинаковы (метки времени и сами значения)
   *
   * @param aTarget {@link IS5Sequence} целевая последовательность
   * @param aSource {@link IS5Sequence} исходная последовательность
   * @return <b>true</b> исходная последовать пересекается с целевой; <b>false</b> исходная последовательность не
   *         пересекается с целевой
   * @param <V> тип значения последовательности
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static <V extends ITemporal<?>> boolean sequencesCrossEquals( IS5Sequence<V> aTarget,
      IS5Sequence<V> aSource ) {
    TsNullArgumentRtException.checkNulls( aTarget, aSource );
    ITimeInterval targetInterval = aTarget.interval();
    ITimeInterval sourceInterval = aSource.interval();
    if( targetInterval.startTime() > sourceInterval.endTime() || //
        targetInterval.endTime() < sourceInterval.startTime() ) {
      // Последовательности не пересекаются
      return false;
    }
    // Метка времени с которой начинается анализ значений (включительно)
    long startCrossTime = Math.max( targetInterval.startTime(), sourceInterval.startTime() );
    // Метка времени по которую проводится анализ значений (включительно)
    long endCrossTime = Math.min( targetInterval.endTime(), sourceInterval.endTime() );
    // Курсоры значений
    IS5SequenceCursor<V> targetCursor = aTarget.createCursor();
    IS5SequenceCursor<V> sourceCursor = aSource.createCursor();
    // Установка курсора
    targetCursor.setTime( startCrossTime );
    sourceCursor.setTime( startCrossTime );
    while( targetCursor.hasNextValue() && sourceCursor.hasNextValue() ) {
      V value = targetCursor.nextValue();
      V sourceValue = sourceCursor.nextValue();
      if( !value.equals( sourceValue ) ) {
        // Значения не одинаковы
        break;
      }
      if( value.timestamp() == endCrossTime ) {
        // Анализ завершен. Все значения в интервале оказались эквивалентными
        return true;
      }
    }
    return false;
  }

  /**
   * Возвращает интервал времени который перекрывается указанным списком блоков значений данного
   *
   * @param <V> тип блоков последовательности
   * @param aType {@link EQueryIntervalType} тип интервала
   * @param aBlocks {@link Iterable}&lt{@link IS5SequenceBlock}&gt; список блоков
   * @return {@link ITimeInterval} интервал времени включительно
   */
  public static <V extends ITemporal<?>> IQueryInterval interval( EQueryIntervalType aType,
      Iterable<IS5SequenceBlock<V>> aBlocks ) {
    TsNullArgumentRtException.checkNulls( aType, aBlocks );
    long startTime = MAX_TIMESTAMP;
    long endTime = MIN_TIMESTAMP;
    for( IS5SequenceBlock<?> block : aBlocks ) {
      long blockStartTime = block.startTime();
      long blockEndTime = block.endTime();
      if( startTime > blockStartTime ) {
        startTime = blockStartTime;
      }
      if( endTime < blockEndTime ) {
        endTime = blockEndTime;
      }
    }
    if( startTime == MAX_TIMESTAMP && endTime == MIN_TIMESTAMP ) {
      throw new TsIllegalArgumentRtException();
    }
    return new QueryInterval( aType, startTime, endTime );
  }

  /**
   * Вывод в строку текстового представления последовательностей находящихся в списке
   *
   * @param aGwids {@link IList}&lt;{@link Gwid}&gt; список последовательностей
   * @param aMax int максимальное количество выводимых идентификаторов
   * @return String текстовое представление
   * @throws TsNullArgumentRtException аргумент = null
   */
  @SuppressWarnings( "nls" )
  public static String gwidsToString( IList<Gwid> aGwids, int aMax ) {
    TsNullArgumentRtException.checkNull( aGwids );
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aGwids.size(); index < n; index++ ) {
      Gwid gwid = aGwids.get( index );
      sb.append( gwid );
      if( index + 1 < n ) {
        if( index + 1 == aMax ) {
          sb.append( "..." );
          break;
        }
        sb.append( ',' );
      }
    }
    return sb.append( "[" + aGwids.size() + "]" ).toString();
  }

  /**
   * Вывод в строку текстового представления последовательностей находящихся в списке
   *
   * @param aSequences {@link IList}&lt;S&gt; список последовательностей
   * @param <S> тип последовательности
   * @return String текстовое представление
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static <S extends IS5Sequence<?>> String sequencesToString( IList<S> aSequences ) {
    TsNullArgumentRtException.checkNull( aSequences );
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = aSequences.size(); index < n; index++ ) {
      S s = aSequences.get( index );
      sb.append( "   [" ); //$NON-NLS-1$
      sb.append( index );
      sb.append( "]: " ); //$NON-NLS-1$
      sb.append( s );
      if( index + 1 < n ) {
        sb.append( "\n" ); //$NON-NLS-1$
      }
    }
    return sb.toString();
  }

  /**
   * Проводит проверку того, что указанные значения могут быть размещены в указанном интервале
   *
   * @param aInterval {@link IQueryInterval} интервал времени значений, подробности {@link IS5Sequence#interval()}
   * @param aMinTimeDelta long слот (мсек) меток времени значений
   * @param aValues {@link ITimedList} список значений
   * @throws TsIllegalArgumentRtException значения вне интервала
   */
  public static void checkIntervalValues( IQueryInterval aInterval, long aMinTimeDelta, ITimedList<?> aValues ) {
    // Количество значений
    int count = aValues.size();
    if( count == 0 ) {
      // Нет значений
      return;
    }
    // Границы интервала устанавливаемых значений
    long minTime = aValues.first().timestamp();
    long maxTime = aValues.last().timestamp();
    // Проверка допустимости меток времени значений
    long startTime = aInterval.startTime();
    long endTime = aInterval.endTime();
    // Признак того, что метки времени значений попадают в интервал последовательности
    boolean passed = false;
    // Признак прохождения значений по закрытому интервалу
    boolean closePassed = (startTime <= minTime && maxTime <= endTime);
    // Признак прохождения значений интервалу закрытому слева
    boolean leftPassed =
        ((count < 2 || startTime - aValues.get( 1 ).timestamp() < aMinTimeDelta) && maxTime <= endTime);
    // Признак прохождения значений интервалу закрытому справа
    boolean rightPassed =
        (startTime <= minTime && (count < 2 || aValues.get( count - 2 ).timestamp() - endTime < aMinTimeDelta));
    passed = switch( aInterval.type() ) {
      case CSCE -> closePassed;
      case OSCE -> leftPassed;
      case CSOE -> rightPassed;
      case OSOE -> (leftPassed || closePassed || rightPassed);
      default -> throw new TsNotAllEnumsUsedRtException();
    };
    if( !passed ) {
      // Метки времени значений вне диапазона последовательности
      String fromTime = timestampToString( minTime );
      String toTime = timestampToString( maxTime );
      throw new TsIllegalArgumentRtException( ERR_SEQUENCE_OUT, fromTime, toTime, aInterval );
    }
  }
}
