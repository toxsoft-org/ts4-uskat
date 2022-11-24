package org.toxsoft.uskat.s5.server.backend.supports.queries.impl;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.core.api.hqserv.ISkHistoryQueryServiceConstants.*;
import static org.toxsoft.uskat.core.impl.SkAsynchronousQuery.*;
import static org.toxsoft.uskat.s5.server.backend.supports.queries.impl.IS5Resources.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.temporal.TemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.filter.ITsFilter;
import org.toxsoft.core.tslib.bricks.filter.ITsFilterFactoriesRegistry;
import org.toxsoft.core.tslib.bricks.filter.impl.TsCombiFilter;
import org.toxsoft.core.tslib.bricks.filter.impl.TsFilterFactoriesRegistry;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.core.api.hqserv.IDtoQueryParam;
import org.toxsoft.uskat.core.api.hqserv.filter.SkQueryFilterByCommandArg;
import org.toxsoft.uskat.s5.server.backend.supports.queries.IS5BackendQueriesFunction;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceCursor;

/**
 * Функции обработки последовательности команд
 *
 * @author mvk
 */
class S5BackendQueriesCommandsFunctions
    implements IS5BackendQueriesFunction {

  /**
   * Максимльное количество команд в ответе
   */
  private static final int RESULT_COUNT_MAX = 1000000;

  /**
   * Повторить значение полученное на предыдущем интервале агрегации, если на текущем нет значений
   */
  private static final boolean REPEAT_BY_EMPTY = true;

  /**
   * Реестр фильтров, используемых правилами.
   */
  private static final ITsFilterFactoriesRegistry<IDtoCompletedCommand> FILTER_REGISTRY =
      new TsFilterFactoriesRegistry<>( IDtoCompletedCommand.class );

  static {
    FILTER_REGISTRY.register( SkQueryFilterByCommandArg.FACTORY );
  }

  private final Pair<String, IDtoQueryParam>    arg;
  private final ITimeInterval                   interval;
  private final long                            aggregationStep;
  private final long                            aggregationStart;
  private final long                            factAggregationStep;
  private final boolean                         repeatByEmpty;
  private final S5BackendQueriesCounter         rawCounter;
  private final S5BackendQueriesCounter         valuesCounter;
  private final int                             answerSize;
  private final EKnownFunc                      func;
  private final ITsFilter<IDtoCompletedCommand> filter;
  private final ITimedListEdit<ITemporal<?>>    result    = new TimedList<>();
  private IAtomicValue                          lastValue = IAtomicValue.NULL;

  /**
   * Текущее начало интервала усреднения. Включительно
   */
  private long intervalStartTime;

  /**
   * Текущие завершение интервала усреднения. Включительно
   */
  private long intervalEndTime;

  /**
   * Счетчик значений интервала усреднения
   */
  private long intervalCount = 0;

  /**
   * Текущее агрегированное значение на интервале
   */
  private double intervalValue;

  /**
   * Журнал
   */
  @SuppressWarnings( "unused" )
  private final ILogger logger;

  /**
   * Конструктор
   *
   * @param aParamId String идентификатор параметра
   * @param aArg {@link IDtoQueryParam} агумент запроса
   * @param aInterval {@link ITimeInterval} интервал запрашиваемых данных
   * @param aRawCounter {@link S5BackendQueriesCounter} счетчик "сырых" значений
   * @param rawValuesCounter {@link S5BackendQueriesCounter} счетчик агрегированных значений для всего запроса
   * @param aOptions {@link IOptionSet} параметры выполнения запроса
   * @param aLogger {@link ILogger} журнал
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException неверный интервал запроса
   * @throws TsIllegalArgumentRtException недопустимый тип значений для агрегации
   */
  S5BackendQueriesCommandsFunctions( String aParamId, IDtoQueryParam aArg, ITimeInterval aInterval,
      S5BackendQueriesCounter aRawCounter, S5BackendQueriesCounter rawValuesCounter, IOptionSet aOptions,
      ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aArg, aInterval, rawValuesCounter, aOptions );
    arg = new Pair<>( aParamId, aArg );
    switch( aArg.funcId() ) {
      case EMPTY_STRING:
        break;
      // case HQFUNC_ID_MIN:
      // case HQFUNC_ID_MAX:
      // case HQFUNC_ID_AVERAGE:
      // case HQFUNC_ID_SUM:
      case HQFUNC_ID_COUNT:
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    func = switch( aArg.funcId() ) {
      case EMPTY_STRING -> EKnownFunc.NONE;
      // case HQFUNC_ID_MIN -> EKnownFunc.MIN;
      // case HQFUNC_ID_MAX -> EKnownFunc.MAX;
      // case HQFUNC_ID_AVERAGE -> EKnownFunc.AVERAGE;
      // case HQFUNC_ID_SUM -> EKnownFunc.SUM;
      case HQFUNC_ID_COUNT -> EKnownFunc.COUNT;
      default -> throw new TsNotAllEnumsUsedRtException();
    };
    // Фильтр "сырых" значений
    filter = TsCombiFilter.create( aArg.filterParams(), FILTER_REGISTRY );
    // Интервал запроса
    interval = aInterval;
    // Интервал агрегации. 0: на всем интервале
    aggregationStep = HQFUNC_ARG_AGGREGAION_INTERVAL.getValue( aArg.funcArgs() ).asLong();
    // Время начала агрегации
    aggregationStart = HQFUNC_ARG_AGGREGAION_START.getValue( aArg.funcArgs() ).asLong();
    // Фактический интервал(мсек) агрегации. +1: включительно
    factAggregationStep = (aggregationStep == 0 ? interval.endTime() - interval.startTime() + 1 : aggregationStep);
    // Повтор предыдущего значения если на текущем интервале агрегации нет значений
    repeatByEmpty = REPEAT_BY_EMPTY;
    // Текущее начало интервала усреднения. Включительно
    intervalStartTime = interval.startTime();
    // Текущие завершение интервала усреднения. Включительно
    intervalEndTime = (aggregationStep == 0 ? interval.endTime() //
        : (((intervalStartTime / factAggregationStep) * factAggregationStep) + factAggregationStep) - 1);
    // Счетчик значений интервала усреднения
    intervalCount = 0;
    // Текущее агрегированное значение на интервале
    intervalValue = 0;
    // if( func == EKnownFunc.MIN ) {
    // intervalValue = Double.MAX_VALUE;
    // }
    // if( func == EKnownFunc.MAX ) {
    // intervalValue = -Double.MAX_VALUE;
    // }
    // Общий счетчик сырых значений
    rawCounter = aRawCounter;
    // Общий счетчик обработанных значений
    valuesCounter = rawValuesCounter;
    // Максимальное количество значений в ответе
    answerSize = RESULT_COUNT_MAX;
    // Журнал
    logger = aLogger;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5BackendQueriesFunction
  //
  @Override
  public Pair<String, IDtoQueryParam> arg() {
    return arg;
  }

  @Override
  public <T extends ITemporal<?>> ITimedList<T> evaluate( IS5SequenceCursor<?> aCursor ) {
    TsNullArgumentRtException.checkNull( aCursor );
    // Результат
    ITimedListEdit<T> retValue = new TimedList<>();
    // Установка курсора на начало последовательности
    // TODO: Отработать aggregationStart
    aCursor.setTime( interval.startTime() );
    // Обработка значений курсора
    while( aCursor.hasNextValue() ) {
      // Следующее raw-значение последовательности
      IDtoCompletedCommand command = (IDtoCompletedCommand)aCursor.nextValue();
      // Фильтрация
      if( filter.accept( command ) ) {
        retValue.addAll( nextCommand( command ) );
      }
    }
    // Обработка последнего значения
    retValue.addAll( nextCommand( null ) );
    // Результат
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Обработка команды последовательности
   *
   * @param aCommand {@link IDtoCompletedCommand} команда. null: завершение обработки
   * @param <T> тип значения
   * @return {@link ITimedList} список обработанных команд
   */
  @SuppressWarnings( "unchecked" )
  private <T extends ITemporal<?>> ITimedList<T> nextCommand( IDtoCompletedCommand aCommand ) {
    rawCounter.add( 1 );
    if( aCommand == null ) {
      // У последовательности больше нет значений. Формирование последнего значения
      addValue();
      // Дополнение по необходимости пустыми значениями
      // addEmptyValues( interval.endTime() );
      // Передача сформированного результата
      return (ITimedList<T>)result;
    }
    // SkEvent cursorEvent = aEvent;
    if( func == EKnownFunc.NONE ) {
      // Нет агрегации. Просто повторяем входные значения
      // IAtomicValue value = (rawValue.isAssigned() ? AvUtils.avInt( rawValue, type ) : IAtomicValue.NULL);
      // IAtomicValue value = IAtomicValue.NULL;
      // if( cursorValue.isAssigned() ) {
      // value = switch( type ) {
      // case NONE -> IAtomicValue.NULL;
      // case BOOLEAN -> AvUtils.avBool( cursorValue.asBool() );
      // case FLOATING -> AvUtils.avFloat( cursorValue.asFloat() );
      // case INTEGER -> AvUtils.avInt( cursorValue.asInt() );
      // case TIMESTAMP -> AvUtils.avTimestamp( cursorValue.asLong() );
      // case STRING -> AvUtils.avStr( cursorValue.asString() );
      // case VALOBJ -> AvUtils.avValobj( cursorValue.asValobj() );
      // default -> throw new TsNotAllEnumsUsedRtException();
      // };
      // }
      result.add( aCommand );
      return (ITimedList<T>)EMPTY_TIMED_LIST;
    }
    // Метка времени нового значения
    long timestamp = aCommand.timestamp();
    if( timestamp > intervalEndTime ) {
      // Новое значение за интервалом. Завершаем текущий интервал получая агрегированное значение
      addValue();
      // Переход на следующий интервал (выбор начала интервала по первому найденому значению за текущим интервалом)
      intervalStartTime = ((timestamp / factAggregationStep) * factAggregationStep);
      intervalEndTime = (((timestamp / factAggregationStep) * factAggregationStep) + factAggregationStep) - 1;
      intervalCount = 0;
      intervalValue = 0;
      // if( func == EKnownFunc.MIN ) {
      // intervalValue = Double.MAX_VALUE;
      // }
      // if( func == EKnownFunc.MAX ) {
      // intervalValue = -Double.MAX_VALUE;
      // }
      // Дополнение по необходимости пустыми значениями
      // addEmptyValues( intervalStartTime - factAggregationStep );
    }
    // Метка времени попадает в интервал усреднения по завершению интервала
    intervalValue = switch( func ) {
      case NONE -> // Недопустимое состояние
        throw new TsInternalErrorRtException();
      case COUNT -> intervalCount + 1;
      default -> throw new TsNotAllEnumsUsedRtException();
    };
    intervalCount++;
    return (ITimedList<T>)EMPTY_TIMED_LIST;
  }

  /**
   * Создает агрегированное значение на интервале
   *
   * @throws TsIllegalArgumentRtException превышение максимального количества обработанных значений
   */
  private void addValue() {
    if( answerSize > 0 && valuesCounter.current() > answerSize ) {
      // Превышение максимального количества агрегированных значений
      Integer ag = Integer.valueOf( (int)(factAggregationStep / 1000) );
      Integer as = Integer.valueOf( answerSize );
      throw new TsIllegalArgumentRtException( ERR_OUTPUT_SIZE_LIMIT, arg.right().dataGwid(), interval, ag, as );
    }

    // Признак того, что на интервале были найдены значения
    boolean hasIntervalValue = (intervalCount > 0);
    // Обработка флага repeatByEmpty
    if( !hasIntervalValue && aggregationStep > 0 && repeatByEmpty ) {
      // Не было значений на интервале, установлен шаг агрегации, установлено требование повторять последнее значение
      result.add( new TemporalAtomicValue( intervalStartTime, lastValue ) );
    }
    if( !hasIntervalValue && aggregationStep > 0 && !repeatByEmpty ) {
      // Не было значений на интервале, установлен шаг агрегации, установлено требование формировать пустые значения
      result.add( new TemporalAtomicValue( intervalStartTime, IAtomicValue.NULL ) );
    }
    if( intervalCount == 0 ) {
      return;
    }
    lastValue = createValue( EAtomicType.INTEGER, intervalValue );
    result.add( new TemporalAtomicValue( intervalStartTime, lastValue ) );
    valuesCounter.add( 1 );
  }

  /**
   * Создает значение для списка значений с метками времени
   *
   * @param aType {@link EAtomicType} атомарный тип значений
   * @param aValue double значение
   * @return {@link IAtomicValue} атомарное значение
   */
  private static IAtomicValue createValue( EAtomicType aType, double aValue ) {
    switch( aType ) {
      case FLOATING:
        return AvUtils.avFloat( aValue );
      case TIMESTAMP:
        return AvUtils.avTimestamp( (long)aValue );
      case INTEGER:
        return AvUtils.avInt( (long)aValue );
      case BOOLEAN:
        if( aValue < 0.5 ) {
          return AvUtils.AV_FALSE;
        }
        return AvUtils.AV_TRUE;
      case NONE:
      case STRING:
      case VALOBJ:
        throw new TsIllegalArgumentRtException();
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }

  /**
   * Обрабатываемые функции
   */
  private enum EKnownFunc {
    NONE,
    // MIN,
    // MAX,
    // AVERAGE,
    // SUM,
    COUNT;
  }
}
