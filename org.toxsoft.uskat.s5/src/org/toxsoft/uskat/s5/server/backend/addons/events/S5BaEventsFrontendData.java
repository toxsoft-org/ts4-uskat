package org.toxsoft.uskat.s5.server.backend.addons.events;

import java.io.Serializable;

import org.toxsoft.uskat.core.backend.api.IBaEvents;
import org.toxsoft.uskat.core.impl.S5EventSupport;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendAddonData;

/**
 * Данные конфигурации фронтенда для {@link IBaEvents}.
 *
 * @author mvk
 */
public class S5BaEventsFrontendData
    implements IS5FrontendAddonData, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Конфигурация обработки событий
   */
  public final S5EventSupport events = new S5EventSupport();

}
