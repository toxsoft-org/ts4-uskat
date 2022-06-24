package org.toxsoft.uskat.s5.server.backend;

import javax.ejb.Remote;

import org.toxsoft.uskat.core.backend.ISkBackend;
import org.toxsoft.uskat.s5.server.sessions.init.*;

/**
 * Удаленный доступ к {@link ISkBackend} предоставляемый s5-сервером
 *
 * @author mvk
 */
@Remote
public interface IS5BackendSession
    extends ISkBackend {

  /**
   * Удаленная инициализация (активирует) движка.
   *
   * @param aInitSessionData {@link IS5SessionInitData} данные для инициализации сессии
   * @return {@link S5SessionInitResult} - результат инициализации сессии
   */
  IS5SessionInitResult init( IS5SessionInitData aInitSessionData );

}
