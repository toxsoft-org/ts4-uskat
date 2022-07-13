package org.toxsoft.uskat.s5.server.backend.addons.events;

import java.io.Serializable;

import org.toxsoft.uskat.core.backend.api.IBaEvents;
import org.toxsoft.uskat.s5.server.frontend.IS5BackendAddonData;

/**
 * Данные конфигурации фронтенда для {@link IBaEvents}.
 *
 * @author mvk
 */
public class S5BaEventsData
    implements IS5BackendAddonData, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Конфигурация обработки событий
   */
  public final S5BaEventsSupport events = new S5BaEventsSupport();

}
