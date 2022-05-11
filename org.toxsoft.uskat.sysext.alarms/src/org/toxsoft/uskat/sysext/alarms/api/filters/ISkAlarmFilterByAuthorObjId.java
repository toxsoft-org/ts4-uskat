package org.toxsoft.uskat.sysext.alarms.api.filters;

import static org.toxsoft.uskat.sysext.alarms.api.filters.ISkResources.*;
import static ru.toxsoft.tslib.datavalue.impl.DvUtils.*;

import org.toxsoft.uskat.sysext.alarms.api.ISkAlarm;
import org.toxsoft.uskat.sysext.alarms.impl.SkAlarmUtils;

import ru.toxsoft.tslib.datavalue.EAtomicType;
import ru.toxsoft.tslib.greenworld.skid.Skid;
import ru.toxsoft.tslib.patterns.opinfo.AtomicOptionInfo;
import ru.toxsoft.tslib.patterns.opinfo.IAtomicOptionInfo;
import ru.toxsoft.tslib.polyfilter.ISingleFilter;

/**
 * Фильтр по полю {@link ISkAlarm#authorId()}.
 * <p>
 * Проверяет, что идентификатор {@link ISkAlarm#authorId()} равен константе {@link #AUTHOR_ID_CONST}.
 *
 * @author goga
 */
public interface ISkAlarmFilterByAuthorObjId
    extends ISingleFilter {

  /**
   * Идентификатор фильтра.
   */
  String FILTER_ID = SkAlarmUtils.FILTER_ID_PREFIX_IDPATH + ".AuthorId"; //$NON-NLS-1$

  /**
   * Параметр: Идентификатор, с которым сравнивается идентификатор объекта - автора тревоги.<br>
   * Тип: {@link EAtomicType#VALOBJ}<br>
   * Применение: фильтр принимает тревоги, у которых автор имеет этот идентификатор<br>
   * Значение по умолчанию: {@link Skid#NONE}<br>
   * Соответствует полю {@link #authorId()}
   */
  IAtomicOptionInfo AUTHOR_ID_CONST = new AtomicOptionInfo( "AuthorId", //$NON-NLS-1$
      STR_D_LVF_AUTHOR_OBJ_ID_CONST, STR_N_LVF_AUTHOR_OBJ_ID_CONST, EAtomicType.VALOBJ, avValobj( Skid.NONE ), true );

  /**
   * Возвращает идентификатор, с которым сравнивается идентификатор объекта - автора тревоги.
   * <p>
   * Соответствует параметру {@link #AUTHOR_ID_CONST}.
   *
   * @return int - идентифиатор желаемого объекта - автора тревоги
   */
  Skid authorId();

  /**
   * {@inheritDoc}
   * <p>
   * В кчестве аргумента метод принимает ссылки на {@link ISkAlarm}.
   */
  @Override
  boolean accept( Object aElement );

}
