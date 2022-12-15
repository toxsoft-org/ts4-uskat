package org.toxsoft.uskat.regref.lib;

import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoEventInfo;

/**
 * Доступ к истории редактирования
 *
 * @author goga
 */
public interface ISkRriHistory {

  /**
   * Возвращает описание события редактирования параметра НСИ.
   * <p>
   * TODO структура?
   *
   * @return {@link IDtoEventInfo} - описание события
   */
  IDtoEventInfo getParamEditEventInfo();

  /**
   * Возвращает историю редактирования раздела.
   * <p>
   * Метод возвращает события, описанные методом {@link #getParamEditEventInfo()}.
   *
   * @param aInterval {@link IQueryInterval} - временной интервал запроса
   * @param aSectionId String - идентификатор раздела
   * @return {@link ITimedList}&lt;{@link SkEvent}&gt; - список событий редактирования
   */
  ITimedList<SkEvent> querySectionEditingHistory( IQueryInterval aInterval, String aSectionId );

}
