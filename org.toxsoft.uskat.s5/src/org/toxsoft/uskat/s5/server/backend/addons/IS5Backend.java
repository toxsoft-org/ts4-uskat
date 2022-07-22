package org.toxsoft.uskat.s5.server.backend.addons;

import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkBackend;

/**
 * Бекенд сервера s5
 *
 * @author mvk
 */
public interface IS5Backend
    extends ISkBackend, ICooperativeMultiTaskable {

  /**
   * Передача сообщения от фротенда к бекенду
   *
   * @param aMessage {@link GtMessage} сообщение для бекенда
   * @throws TsNullArgumentRtException аргумент = null
   */
  void onFrontendMessage( GtMessage aMessage );
}
