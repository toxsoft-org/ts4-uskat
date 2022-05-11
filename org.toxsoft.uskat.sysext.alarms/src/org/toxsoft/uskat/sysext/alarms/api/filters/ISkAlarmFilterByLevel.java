package org.toxsoft.uskat.sysext.alarms.api.filters;

import static org.toxsoft.uskat.sysext.alarms.api.filters.ISkResources.*;
import static ru.toxsoft.tslib.datavalue.impl.DvUtils.*;

import org.toxsoft.uskat.sysext.alarms.api.EAlarmPriority;
import org.toxsoft.uskat.sysext.alarms.api.ISkAlarm;
import org.toxsoft.uskat.sysext.alarms.impl.SkAlarmUtils;

import ru.toxsoft.tslib.datavalue.EAtomicType;
import ru.toxsoft.tslib.datavalue.math.AvCompareOpKeeper;
import ru.toxsoft.tslib.datavalue.math.EAvCompareOp;
import ru.toxsoft.tslib.patterns.opinfo.*;
import ru.toxsoft.tslib.polyfilter.ISingleFilter;

/**
 * Фильтр по полю {@link ISkAlarm#level()}.
 *
 * @author goga
 */
public interface ISkAlarmFilterByLevel
    extends ISingleFilter {

  /**
   * Идентификатор фильтра.
   */
  String FILTER_ID = SkAlarmUtils.FILTER_ID_PREFIX_IDPATH + ".LevelFilter"; //$NON-NLS-1$

  /**
   * Параметр: Оператор сравнения приоритета проверямой тревоги с константой.<br>
   * Тип: {@link EAvCompareOp}<br>
   * Применение: Этим оператором сравнивается уровень важности {@link ISkAlarm#level()} проверяемой тревоги и константы
   * {@link #LEVEL_CONST}.<br>
   * Значение по умолчанию: {@link EAvCompareOp#EQ}<br>
   * Соответствует полю {@link #compareOp()}
   */
  IFimbedOptionInfo<EAvCompareOp> COMPARE_OP = new FimbedOptionInfo<>( "CompareOp", //$NON-NLS-1$
      STR_D_LVF_COMPARE_OP, STR_N_LVF_COMPARE_OP, EAvCompareOp.class, AvCompareOpKeeper.KEEPER, EAvCompareOp.EQ,
      false );

  /**
   * Параметр: Целое число, с которым сравнивается уровень приоритета фильтруемой тревоги.<br>
   * Тип: {@link EAtomicType#INTEGER}<br>
   * Применение: С этой константой оператором {@link #COMPARE_OP} сравнивается уровень приоритета фильтруемой тревоги.
   * <br>
   * Значение по умолчанию: {@link EAlarmPriority#NORMAL}.{@link EAlarmPriority#sublevelBase()}<br>
   * Соответствует полю {@link #levelConst()}
   */
  IAtomicOptionInfo LEVEL_CONST = new AtomicOptionInfo( "LevelConst", //$NON-NLS-1$
      STR_D_LVF_LEVEL_CONST, STR_N_LVF_LEVEL_CONST, EAtomicType.INTEGER, avInt( EAlarmPriority.NORMAL.sublevelBase() ),
      true );

  /**
   * Возвращает оператор сравнения приоритета проверямой тревоги с константой.
   * <p>
   * Соответствует параметру {@link #COMPARE_OP}.
   *
   * @return {@link EAvCompareOp} - оператор сравнения уровня приоритета с константой
   */
  EAvCompareOp compareOp();

  /**
   * Возвращает число, с которым сравнивается уровень приоритета фильтруемой тревоги.
   * <p>
   * Соответствует параметру {@link #LEVEL_CONST}.
   *
   * @return int - константа уровня приоритета
   */
  int levelConst();

  /**
   * {@inheritDoc}
   * <p>
   * В кчестве аргумента метод принимает ссылки на {@link ISkAlarm}.
   */
  @Override
  boolean accept( Object aElement );

}
