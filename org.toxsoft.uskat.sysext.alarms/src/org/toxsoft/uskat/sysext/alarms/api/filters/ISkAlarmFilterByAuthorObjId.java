package org.toxsoft.uskat.sysext.alarms.api.filters;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.sysext.alarms.api.filters.ISkResources.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.bricks.filter.ITsFilter;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.uskat.sysext.alarms.api.ISkAlarm;
import org.toxsoft.uskat.sysext.alarms.impl.SkAlarmUtils;

/**
 * Фильтр по полю {@link ISkAlarm#authorId()}.
 * <p>
 * Проверяет, что идентификатор {@link ISkAlarm#authorId()} равен константе {@link #AUTHOR_ID_CONST}.
 *
 * @author goga
 */
public interface ISkAlarmFilterByAuthorObjId
    extends ITsFilter<ISkAlarm> {

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
  IDataDef AUTHOR_ID_CONST = create( "AuthorId", EAtomicType.VALOBJ, // //$NON-NLS-1$
      TSID_NAME, STR_N_LVF_AUTHOR_OBJ_ID_CONST, //
      TSID_DESCRIPTION, STR_D_LVF_AUTHOR_OBJ_ID_CONST, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.avValobj( Skid.NONE ) );

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
  boolean accept( ISkAlarm aElement );

}
