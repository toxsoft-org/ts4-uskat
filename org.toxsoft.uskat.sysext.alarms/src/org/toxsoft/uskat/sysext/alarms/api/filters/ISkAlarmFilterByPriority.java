package org.toxsoft.uskat.sysext.alarms.api.filters;

import static org.toxsoft.uskat.sysext.alarms.api.filters.ISkResources.*;

import org.toxsoft.uskat.sysext.alarms.api.EAlarmPriority;
import org.toxsoft.uskat.sysext.alarms.api.ISkAlarm;
import org.toxsoft.uskat.sysext.alarms.impl.SkAlarmPriorityKeeper;
import org.toxsoft.uskat.sysext.alarms.impl.SkAlarmUtils;

import ru.toxsoft.tslib.datavalue.math.AvCompareOpKeeper;
import ru.toxsoft.tslib.datavalue.math.EAvCompareOp;
import ru.toxsoft.tslib.patterns.opinfo.FimbedOptionInfo;
import ru.toxsoft.tslib.patterns.opinfo.IFimbedOptionInfo;
import ru.toxsoft.tslib.polyfilter.ISingleFilter;

/**
 * Фильтр по полю {@link ISkAlarm#priority()}.
 *
 * @author goga
 */
public interface ISkAlarmFilterByPriority
    extends ISingleFilter {

  /**
   * Идентификатор фильтра.
   */
  String FILTER_ID = SkAlarmUtils.FILTER_ID_PREFIX_IDPATH + ".PriorityFilter"; //$NON-NLS-1$

  /**
   * Параметр: Оператор сравнения приоритета проверямой тревоги с константой.<br>
   * Тип: {@link EAvCompareOp}<br>
   * Применение: Этим оператором сравнивается приоритеты (точнее,численное значение
   * {@link EAlarmPriority#sublevelBase()}) проверяемой тревоги и константы {@link #PRIORITY_CONST}.<br>
   * Значение по умолчанию: {@link EAvCompareOp#EQ}<br>
   * Соответствует полю {@link #compareOp()}
   */
  IFimbedOptionInfo<EAvCompareOp> COMPARE_OP = new FimbedOptionInfo<>( "CompareOp", //$NON-NLS-1$
      STR_D_PRF_COMPARE_OP, STR_N_PRF_COMPARE_OP, EAvCompareOp.class, AvCompareOpKeeper.KEEPER, EAvCompareOp.EQ,
      false );

  /**
   * Параметр: Константа важности, с которым сравнивается приоритет фильтруемой тревоги.<br>
   * Тип: {@link EAlarmPriority}<br>
   * Применение: С этой константой оператором {@link #COMPARE_OP} сравнивается численное значение приоритета фильтруемой
   * тревоги.<br>
   * Значение по умолчанию: {@link EAlarmPriority#NORMAL}<br>
   * Соответствует полю {@link #priorityConst()}
   */
  IFimbedOptionInfo<EAlarmPriority> PRIORITY_CONST = new FimbedOptionInfo<>( "PriorityConst", //$NON-NLS-1$
      STR_D_PRF_PRIORITY_CONST, STR_N_PRF_PRIORITY_CONST, EAlarmPriority.class, SkAlarmPriorityKeeper.KEEPER,
      EAlarmPriority.NORMAL, false );

  /**
   * Возвращает оператор сравнения приоритета проверямой тревоги с константой.
   * <p>
   * Соответствует параметру {@link #COMPARE_OP}.
   *
   * @return {@link EAvCompareOp} - оператор сравнения уровня приоритета с константой
   */
  EAvCompareOp compareOp();

  /**
   * Возвращает приоритет, с которым сравнивается уровень приоритета фильтруемой тревоги.
   * <p>
   * Соответствует параметру {@link #PRIORITY_CONST}.
   *
   * @return int - константа уровня приоритета
   */
  EAlarmPriority priorityConst();

  /**
   * {@inheritDoc}
   * <p>
   * В кчестве аргумента метод принимает ссылки на {@link ISkAlarm}.
   */
  @Override
  boolean accept( Object aElement );

}
