package org.toxsoft.uskat.s5.server.backend.addons;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.backend.ISkBackend;

/**
 * Бекенд сервера s5
 *
 * @author mvk
 */
public interface IS5Backend
    extends ISkBackend {

  /**
   * Возвращает идентификатор сессии под которой подключен бекенд
   *
   * @return {@link Skid} идентификатор сессии {@link ISkSession}.
   */
  Skid sessionID();

  /**
   * Передача сообщения от фротенда к бекенду
   *
   * @param aMessage {@link GtMessage} сообщение для бекенда
   * @throws TsNullArgumentRtException аргумент = null
   */
  void onFrontendMessage( GtMessage aMessage );
}
