package org.toxsoft.uskat.s5.server.backend.supports.core;

import javax.ejb.*;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Локальный интерфейс синглетона реализующего передачу сообщений бекенда.
 *
 * @author mvk
 */
@Local
public interface IS5BackendEventer {

  /**
   * Формирование сообщение бекенда.
   *
   * @param aMessage {@link GtMessage} - сообщение бекенда.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void fireBackendMessage( GtMessage aMessage );
}
