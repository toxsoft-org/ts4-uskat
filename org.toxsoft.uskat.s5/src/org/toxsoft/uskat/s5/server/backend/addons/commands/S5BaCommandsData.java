package org.toxsoft.uskat.s5.server.backend.addons.commands;

import java.io.Serializable;

import org.toxsoft.uskat.core.backend.api.IBaCommands;
import org.toxsoft.uskat.s5.server.frontend.IS5BackendAddonData;

/**
 * Данные конфигурации фронтенда для {@link IBaCommands}.
 *
 * @author mvk
 */
public class S5BaCommandsData
    implements IS5BackendAddonData, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Конфигурация обработки команд
   */
  public final S5BaCommandsSupport commands = new S5BaCommandsSupport();
}
