package org.toxsoft.uskat.s5.server.backend.supports.queries.impl;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.core.api.hqserv.ISkHistoryQueryServiceConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.queries.impl.IS5Resources.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.av.temporal.TemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.filter.ITsFilter;
import org.toxsoft.core.tslib.bricks.filter.ITsFilterFactoriesRegistry;
import org.toxsoft.core.tslib.bricks.filter.impl.TsCombiFilter;
import org.toxsoft.core.tslib.bricks.filter.impl.TsFilterFactoriesRegistry;
import org.toxsoft.core.tslib.bricks.filter.std.av.StdFilterAtimicValueVsConst;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.core.api.hqserv.IDtoQueryParam;
import org.toxsoft.uskat.s5.server.backend.addons.queries.ES5QueriesConvoyState;
import org.toxsoft.uskat.s5.server.backend.addons.queries.S5BaQueriesConvoy;
import org.toxsoft.uskat.s5.server.backend.supports.queries.IS5BackendQueriesFunction;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceCursor;

/**
 * Функции обработки атомарных значений
 *
 * @author mvk
 */
class S5BackendQueriesAtomicValueFunctions
    implements IS5BackendQueriesFunction {

  /**
   * Максимльное количество значений в ответе
   */
  private static final int RESULT_COUNT_MAX = 1000000;

  /**
   * Повторить значение полученное на предыдущем интервале агрегации, если на текущем нет значений
   */
  private static final boolean REPEAT_BY_EMPTY = true;

  /**
   * Реестр фильтров, используемых правилами.
   */
  private static final ITsFilterFactoriesRegistry<IAtomicValue> FILTER_REGISTRY =
      new TsFilterFactoriesRegistry<>( IAtomicValue.class );

  static {
    FILTER_REGISTRY.register( StdFilterAtimicValueVsConst.FACTORY );
  }

  private final S5BaQueriesConvoy               query;
  private final Pair<String, IDtoQueryParam>    arg;
  private final EAtomicType                     type;
  private final ITimeInterval                   interval;
  private final long                            aggregationStep;
  @SuppressWarnings( "unused" )
  private final long                            aggregationStart;
  private final long                            factAggregationStep;
  private final boolean                         repeatByEmpty;
  private final S5BackendQueriesCounter         rawCounter;
  private final S5BackendQueriesCounter         valuesCounter;
  private final int                             answerSize;
  private final EKnownFunc                      func;
  private final ITsFilter<IAtomicValue>         filter;
  private final IListEdit<ITemporalAtomicValue> result    = new ElemLinkedList<>();
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
  private final ILogger logger;

  /**
   * Конструктор
   *
   * @param aQuery {@link S5BaQueriesConvoy} конвой-объект запроса
   * @param aParamId String идентификатор параметра
   * @param aArg {@link IDtoQueryParam} агумент запроса
   * @param aType тип значений
   * @param aInterval {@link ITimeInterval} интервал запрашиваемых данных
   * @param aRawCounter {@link S5BackendQueriesCounter} счетчик "сырых" значений
   * @param rawValuesCounter {@link S5BackendQueriesCounter} счетчик агрегированных значений для всего запроса
   * @param aOptions {@link IOptionSet} параметры выполнения запроса
   * @param aLogger {@link ILogger} журнал
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException неверный интервал запроса
   * @throws TsIllegalArgumentRtException недопустимый тип значений для агрегации
   */
  S5BackendQueriesAtomicValueFunctions( S5BaQueriesConvoy aQuery, String aParamId, IDtoQueryParam aArg,
      EAtomicType aType, ITimeInterval aInterval, S5BackendQueriesCounter aRawCounter,
      S5BackendQueriesCounter rawValuesCounter, IOptionSet aOptions, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aQuery, aArg, aType, aInterval, rawValuesCounter, aOptions );
    query = aQuery;
    arg = new Pair<>( aParamId, aArg );
    switch( aArg.funcId() ) {
      case EMPTY_STRING:
        break;
      case HQFUNC_ID_MIN:
      case HQFUNC_ID_MAX:
      case HQFUNC_ID_AVERAGE:
      case HQFUNC_ID_SUM:
      case HQFUNC_ID_COUNT:
        if( aType == EAtomicType.STRING || aType == EAtomicType.VALOBJ ) {
          // Недопустимый тип значений для выбранной агрегации
          throw new TsIllegalArgumentRtException( ERR_WRONG_TYPE, aType );
        }
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    type = switch( aArg.funcId() ) {
      case EMPTY_STRING, HQFUNC_ID_MIN, HQFUNC_ID_MAX, HQFUNC_ID_SUM -> aType;
      case HQFUNC_ID_COUNT -> EAtomicType.INTEGER;
      case HQFUNC_ID_AVERAGE -> (aType == EAtomicType.BOOLEAN ? EAtomicType.BOOLEAN : EAtomicType.FLOATING);
      default -> throw new TsNotAllEnumsUsedRtException();
    };
    func = switch( aArg.funcId() ) {
      case EMPTY_STRING -> EKnownFunc.NONE;
      case HQFUNC_ID_MIN -> EKnownFunc.MIN;
      case HQFUNC_ID_MAX -> EKnownFunc.MAX;
      case HQFUNC_ID_AVERAGE -> EKnownFunc.AVERAGE;
      case HQFUNC_ID_SUM -> EKnownFunc.SUM;
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
    if( func == EKnownFunc.MIN ) {
      intervalValue = Double.MAX_VALUE;
    }
    if( func == EKnownFunc.MAX ) {
      intervalValue = -Double.MAX_VALUE;
    }
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
  public <T> IList<T> evaluate( IList<IS5SequenceCursor<?>> aCursors ) {
    TsNullArgumentRtException.checkNull( aCursors );
    if( aCursors.size() > 1 ) {
      throw new TsUnderDevelopmentRtException(
          "S5BackendQueriesAtomicValueFunctions.evalute(...): multi-gwid handling not implemented yet" ); //$NON-NLS-1$
    }
    StringBuilder sbLog = (logger.isSeverityOn( ELogSeverity.DEBUG ) ? new StringBuilder() : null);
    // Курсор
    IS5SequenceCursor<?> cursor = aCursors.first();
    // Результат
    IListEdit<T> retValue = new ElemLinkedList<>();
    // Установка курсора на начало последовательности
    // TODO: Отработать aggregationStart
    cursor.setTime( interval.startTime() );
    // Обработка значений курсора
    while( cursor.hasNextValue() ) {
      if( query.state() != ES5QueriesConvoyState.EXECUTING ) {
        // Запрос был отменен
        break;
      }
      // Следующее raw-значение последовательности
      ITemporalAtomicValue value = (ITemporalAtomicValue)cursor.nextValue();
      // Признак того, что значение было допущено фильтрами для обработки
      boolean accepted = filter.accept( value.value() );
      // Фильтрация
      if( accepted ) {
        retValue.addAll( nextValue( value ) );
      }
      if( sbLog != null ) {
        sbLog.append( String.format( "%s %s\n", value, accepted ? TsLibUtils.EMPTY_STRING : " FILTERED!" ) ); //$NON-NLS-1$//$NON-NLS-2$
      }
    }
    // Обработка последнего значения
    retValue.addAll( nextValue( null ) );
    // Вывод журнала
    if( sbLog != null ) {
      logger.debug( "evaluate(...): %s, result count = %d, values =\n%s", arg.right().dataGwid(), //$NON-NLS-1$
          Integer.valueOf( retValue.size() ), sbLog.toString() );
    }
    // Результат
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Обработка текущего "сырого" значения
   *
   * @param aValue {@link ITemporalAtomicValue} текущее "сырое" значение. null: завершение обработки
   * @param <T> тип значения
   * @return {@link IList} список обработанных значений
   */
  @SuppressWarnings( "unchecked" )
  private <T> IList<T> nextValue( ITemporalAtomicValue aValue ) {
    rawCounter.add( 1 );
    if( aValue == null ) {
      // У последовательности больше нет значений. Формирование последнего значения
      addValue();
      // Дополнение по необходимости пустыми значениями
      // addEmptyValues( interval.endTime() );
      // Передача сформированного результата
      return (IList<T>)result;
    }
    IAtomicValue cursorValue = aValue.value();
    if( !cursorValue.isAssigned() ) {
      // null-значения не обрабатываются
      return IList.EMPTY;
    }
    if( func == EKnownFunc.NONE ) {
      // Нет агрегации. Просто повторяем входные значения
      // IAtomicValue value = (rawValue.isAssigned() ? AvUtils.avInt( rawValue, type ) : IAtomicValue.NULL);
      IAtomicValue value = IAtomicValue.NULL;
      if( cursorValue.isAssigned() ) {
        value = switch( type ) {
          case NONE -> IAtomicValue.NULL;
          case BOOLEAN -> AvUtils.avBool( cursorValue.asBool() );
          case FLOATING -> AvUtils.avFloat( cursorValue.asFloat() );
          case INTEGER -> AvUtils.avInt( cursorValue.asInt() );
          case TIMESTAMP -> AvUtils.avTimestamp( cursorValue.asLong() );
          case STRING -> AvUtils.avStr( cursorValue.asString() );
          case VALOBJ -> AvUtils.avValobj( cursorValue.asValobj() );
          default -> throw new TsNotAllEnumsUsedRtException();
        };
      }
      result.add( new TemporalAtomicValue( aValue.timestamp(), value ) );
      return IList.EMPTY;
    }
    // Метка времени нового значения
    long timestamp = aValue.timestamp();
    if( timestamp > intervalEndTime ) {
      // Новое значение за интервалом. Завершаем текущий интервал получая агрегированное значение
      addValue();
      // Переход на следующий интервал (выбор начала интервала по первому найденому значению за текущим интервалом)
      intervalStartTime = ((timestamp / factAggregationStep) * factAggregationStep);
      intervalEndTime = (((timestamp / factAggregationStep) * factAggregationStep) + factAggregationStep) - 1;
      intervalCount = 0;
      intervalValue = 0;
      if( func == EKnownFunc.MIN ) {
        intervalValue = Double.MAX_VALUE;
      }
      if( func == EKnownFunc.MAX ) {
        intervalValue = -Double.MAX_VALUE;
      }
      // Дополнение по необходимости пустыми значениями
      // addEmptyValues( intervalStartTime - factAggregationStep );
    }
    // Метка времени попадает в интервал усреднения по завершению интервала
    double value = cursorValue.asDouble();
    switch( func ) {
      case NONE:
        // Недопустимое состояние
        throw new TsInternalErrorRtException();
      case MIN:
        intervalValue = (intervalValue > value ? value : intervalValue);
        break;
      case MAX:
        intervalValue = (intervalValue < value ? value : intervalValue);
        break;
      case COUNT:
        intervalValue = intervalCount + 1;
        break;
      case SUM:
        if( type != EAtomicType.BOOLEAN ) {
          intervalValue += value;
          break;
        }
        //$FALL-THROUGH$
      case AVERAGE:
        intervalValue = (intervalValue * intervalCount / (intervalCount + 1) + value / (intervalCount + 1));
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    intervalCount++;
    return IList.EMPTY;
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
    lastValue = createValue( type, intervalValue );
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
    MIN,
    MAX,
    AVERAGE,
    SUM,
    COUNT;
  }
}
