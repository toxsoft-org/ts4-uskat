package org.toxsoft.uskat.s5.server.backend.addons.events;

import java.io.Serializable;

import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.uskat.core.backend.api.IBaEvents;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionAddonInitData;

/**
 * Данные для инициализации расширения backend {@link IBaEvents}.
 *
 * @author mvk
 */
public final class S5BaEventsInitData
    implements IS5SessionAddonInitData, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Идентификаторы событий на которые подписан клиент
   */
  public final GwidList events = new GwidList();

}
