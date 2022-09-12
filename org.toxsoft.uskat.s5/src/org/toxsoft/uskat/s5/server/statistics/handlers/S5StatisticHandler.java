package org.toxsoft.uskat.s5.server.statistics.handlers;

import static org.toxsoft.uskat.s5.server.statistics.EStatisticInterval.*;

import org.toxsoft.core.log4j.LoggerWrapper;
import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.legacy.S5Stridable;
import org.toxsoft.uskat.s5.server.statistics.EStatisticInterval;
import org.toxsoft.uskat.s5.server.statistics.IS5StatisticInterval;

// ------------------------------------------------------------------------------------
// Обработчики статистики
//
/**
 * Обработчик статистики по одному параметру
 */
public abstract class S5StatisticHandler
    extends S5Stridable {

  private static final long serialVersionUID = 157157L;

  private IS5StatisticInterval interval;
  private EAtomicType          type;
  private IAtomicValue         value;
  private boolean              wasValueAfterUpdate;
  private IAtomicValue         initValue;
  private long                 timestamp;
  private transient ILogger    logger;

  /**
   * Конструктор
   *
   * @param aId String идентификатор параметра
   * @param aType {@link EAtomicType} атомарный тип поступающих значений
   * @param aInterval {@link IS5StatisticInterval} интервал статистической обработки
   * @param aInitValue {@link IAtomicValue} начальное статистическое значение при создании обработчика.
   *          {@link IAtomicValue#NULL}: нет значения по умолчанию
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected S5StatisticHandler( String aId, EAtomicType aType, IS5StatisticInterval aInterval,
      IAtomicValue aInitValue ) {
    super( aId );
    type = TsNullArgumentRtException.checkNull( aType );
    interval = TsNullArgumentRtException.checkNull( aInterval );
    value = TsNullArgumentRtException.checkNull( aInitValue );
    wasValueAfterUpdate = false;
    initValue = aInitValue;
    timestamp = System.currentTimeMillis();
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает атомарный тип получаемых значений для обработки
   *
   * @return {@link EAtomicType} атомарный тип.
   */
  public final EAtomicType type() {
    return type;
  }

  /**
   * Возвращает интервал статистической обработки
   *
   * @return {@link IS5StatisticInterval} атомарный тип.
   */
  public final IS5StatisticInterval interval() {
    return interval;
  }

  /**
   * Возвращает последнее расчитанное значение
   *
   * @return {@link IAtomicValue} значение параметра статистики.
   */
  public final IAtomicValue value() {
    return value;
  }

  /**
   * Отработать значение параметра
   *
   * @param aValue {@link IAtomicValue} значение параметра
   * @return boolean <b>true</b> данные параметра изменились;<b>false</b> данные параметра не изменились
   * @throws TsNullArgumentRtException аргумент = 0
   * @throws TsIllegalArgumentRtException недопустимый тип значения
   */
  public final boolean onValue( IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNull( aValue );
    if( !aValue.isAssigned() ) {
      return false;
    }
    if( aValue.atomicType() != type ) {
      throw new TsIllegalArgumentRtException();
    }
    // Фиксация факта поступления значений
    wasValueAfterUpdate = true;
    // Расчет текущего значения
    return doOnValue( aValue );
  }

  /**
   * Обновление состояние счетчика
   * <p>
   * Предполагается, что метод {@link #update()} будет вызываться не реже интервала формирования статистики
   *
   * @return {@link IAtomicValue} сформированное значение статистики. {@link IAtomicValue#NULL}: значение не изменилось
   */
  public final IAtomicValue update() {
    // Текущее время
    long currTime = System.currentTimeMillis();
    // Интервал выравнивания (мсек)
    int ADJUST_INTERVAL = 1000;
    // Значение выравнивания
    int adjustValue = (int)(interval != ALL ? currTime / ADJUST_INTERVAL % (interval.milli() / ADJUST_INTERVAL) : 0);
    // Признак начала интервала
    boolean isStartInterval = (adjustValue == 0 && currTime - timestamp > interval.milli() / 4);
    // Признак завершения интервала
    boolean completed = (isAllInterval() || isStartInterval);
    if( !completed ) {
      return IAtomicValue.NULL;
    }
    // Фиксируем время последнего вызова update
    timestamp = currTime;
    if( !wasValueAfterUpdate ) {
      // Не было значений с момента прошлого вызова update. Возвращаем значение по умолчанию
      return initValue;
    }
    // Сброс признака получения значений между вызовами update
    wasValueAfterUpdate = false;
    // Расчет нового статистического значения
    value = doCalcValue();
    // Если интервал не "все время", то сброс текущего значения
    if( !isAllInterval() ) {
      doReset();
    }
    // Возвращение нового статистического значения
    return value;
  }

  /**
   * Привести обработчик в начальное состояние
   */
  public final void reset() {
    doReset();
    value = initValue;
    timestamp = System.currentTimeMillis();
  }

  // ------------------------------------------------------------------------------------
  // Шаблонные методы для реализации наследниками
  //
  /**
   * Отработать значение параметра
   *
   * @param aValue {@link IAtomicValue} значение параметра, не может быть {@link IAtomicValue#isAssigned() == false}
   * @return boolean <b>true</b> данные параметра изменились;<b>false</b> данные параметра не изменились
   */
  protected abstract boolean doOnValue( IAtomicValue aValue );

  /**
   * Выполнить расчет нового статистического значения на интервале
   * <p>
   * Базовый класс вызывающий метод {@link #doCalcValue()} гарантирует вызов только тогда, когда обработчик поступило не
   * менее одного значения
   *
   * @return {@link IAtomicValue} расчитанное значение. {@link IAtomicValue#NULL}: значение не расчитано
   */
  protected abstract IAtomicValue doCalcValue();

  /**
   * Привести обработчик в начальное состояние
   */
  protected abstract void doReset();

  // ------------------------------------------------------------------------------------
  // Методы для наследников
  //
  /**
   * Возвращает журнал работы
   *
   * @return {@link ILogger} журнал работы
   */
  protected final ILogger logger() {
    if( logger == null ) {
      logger = LoggerWrapper.getLogger( getClass() );
    }
    return logger;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Вспомогательный метод:<br>
   * Возвращает признак того, что расчет параметра идет по всему времени
   *
   * @return boolean <br>
   *         <b>true</b> расчет производится по всему времени;<br>
   *         <b>false</b> расчет интервалу заданому через {@link #interval()}.
   */
  private boolean isAllInterval() {
    return (interval == EStatisticInterval.ALL);
  }

}
