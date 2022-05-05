package org.toxsoft.uskat.sysext.refbooks;

import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.common.dpu.rt.events.SkEvent;
import ru.uskat.core.api.sysdescr.ISkEventInfo;

/**
 * История правок справочника и данные о событиях правки.
 *
 * @author goga
 */
public interface ISkRefbookHistory {

  /**
   * Возвращает описание события редактирования справочника.
   * <p>
   * Это событие описывает изменение в элементах справочника - создание или удаление элементов, редактирование
   * существующих. Событие не оповещает о создании/удалении/редактировании самого справочника, только его элементов.
   *
   * @return {@link ISkEventInfo} - события правки справочника
   */
  ISkEventInfo getRefbookItemEditEventInfo();

  /**
   * Возвращает историю редактирования справочника.
   * <p>
   * Метод возвращает события, описанные методом {@link #getRefbookItemEditEventInfo()}.
   *
   * @param aInterval {@link IQueryInterval} - временной интервал запроса
   * @param aRefbookId String - идентификатор справочника
   * @return {@link ITimedList}&lt;{@link SkEvent}&gt; - список событий редактирования
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException нет такого справочника
   */
  ITimedList<SkEvent> queryRefbookEditingHistory( IQueryInterval aInterval, String aRefbookId );

}
