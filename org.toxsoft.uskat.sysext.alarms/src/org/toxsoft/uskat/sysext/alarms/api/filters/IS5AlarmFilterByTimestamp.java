package org.toxsoft.uskat.sysext.alarms.api.filters;

import org.toxsoft.uskat.sysext.alarms.api.ISkAlarm;
import org.toxsoft.uskat.sysext.alarms.impl.SkAlarmUtils;


/**
 * Фильтр по полю {@link ISkAlarm#timestamp()}.
 * <p>
 * Испольует параметры, унаследнованные от {@link IStdTimestampFilter}.
 *
 * @author goga
 */
public interface IS5AlarmFilterByTimestamp
    extends IStdTimestampFilter {

  /**
   * Идентификатор фильтра.
   */
  String FILTER_ID = SkAlarmUtils.FILTER_ID_PREFIX_IDPATH + ".AlarmDefIdFilter"; //$NON-NLS-1$

  /**
   * {@inheritDoc}
   * <p>
   * В качестве аргумента метод принимает ссылки на {@link ISkAlarm}.
   */
  @Override
  boolean accept( Object aElement );

}
