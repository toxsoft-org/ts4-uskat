package org.toxsoft.uskat.sysext.alarms.api.filters;

import org.toxsoft.uskat.sysext.alarms.api.ISkAlarm;
import org.toxsoft.uskat.sysext.alarms.impl.SkAlarmUtils;

import ru.toxsoft.tslib.polyfilter.stdfilters.text.IStdTextFilter;

/**
 * Фильтр по полю {@link ISkAlarm#message()}.
 * <p>
 * Испольует параметры, унаследнованные от {@link IStdTextFilter}.
 *
 * @author goga
 */
public interface ISkAlarmFilterByMessage
    extends IStdTextFilter {

  /**
   * Идентификатор фильтра.
   */
  String FILTER_ID = SkAlarmUtils.FILTER_ID_PREFIX_IDPATH + ".MessageFilter"; //$NON-NLS-1$

  /**
   * {@inheritDoc}
   * <p>
   * В кчестве аргумента метод принимает ссылки на {@link ISkAlarm}.
   */
  @Override
  boolean accept( Object aElement );

}
