package org.toxsoft.uskat.s5.common.info;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.uskat.s5.common.sessions.IS5SessionInfo;

/**
 * Информация об открытых и завершенных сессиях пользователей.
 *
 * @author mvk
 */
public interface IS5SessionsInfos {

  /**
   * Возвращает список описаний открытых сессий пользователей
   *
   * @return {@link IList}&lt;{@link IS5SessionInfo};&gt; список описаний сессий
   */
  IList<IS5SessionInfo> openInfos();

  /**
   * Возвращает список закрытых сессий пользователя
   * <p>
   * Количество возвращаемых описаний определяется реализаций. Реализация по умолчанию обеспечивает возвращение 10
   * описаний последних закрытых сессий если такие имеются.
   *
   * @return {@link IList}&lt;{@link IS5SessionInfo};&gt; список описаний сессий
   */
  IList<IS5SessionInfo> closeInfos();
}
