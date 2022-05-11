package org.toxsoft.uskat.sysext.alarms.api.filters;

import org.toxsoft.uskat.sysext.alarms.api.ISkAlarm;
import org.toxsoft.uskat.sysext.alarms.impl.SkAlarmUtils;

import ru.toxsoft.tslib.polyfilter.stdfilters.strid.IStdStridFilter;

/**
 * Фильтр по полю {@link ISkAlarm#alarmDefId()}.
 * <p>
 * Испольует параметры, унаследнованные от {@link IStdStridFilter}.
 *
 * @author goga
 */
public interface ISkAlarmFilterByDefId
    extends IStdStridFilter {

  /**
   * Идентификатор фильтра.
   */
  String FILTER_ID = SkAlarmUtils.FILTER_ID_PREFIX_IDPATH + ".AlarmDefIdFilter"; //$NON-NLS-1$

  /**
   * {@inheritDoc}
   * <p>
   * В качестве аргумента метод принимает только ссылки на {@link ISkAlarm}.
   */
  @Override
  boolean accept( Object aElement );

}
