package org.toxsoft.uskat.s5.server.backend.addons;

import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.uskat.core.backend.ISkBackend;
import org.toxsoft.uskat.s5.common.sessions.ISkSession;

/**
 * Бекенд сервера s5
 *
 * @author mvk
 */
public interface IS5Backend
    extends ISkBackend, ICooperativeMultiTaskable {

  /**
   * Возвращает идентификатор сессии под которой подключен бекенд
   *
   * @return {@link Skid} идентификатор сессии {@link ISkSession}.
   */
  Skid sessionID();
}
