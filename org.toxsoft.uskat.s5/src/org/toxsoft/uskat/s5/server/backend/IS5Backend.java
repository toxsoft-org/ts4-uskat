package org.toxsoft.uskat.s5.server.backend;

import org.jboss.ejb.client.SessionID;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsRuntimeException;
import org.toxsoft.uskat.s5.common.sessions.IS5SessionInfo;

import ru.uskat.core.api.ISkBackend;

/**
 * {@link ISkBackend} предоставляемый s5-сервером
 *
 * @author mvk
 */
public interface IS5Backend
    extends ISkBackend {

  /**
   * Закрыть сессию пользователя
   *
   * @param aSessionID {@link SessionID} идентификатор сессии {@link IS5SessionInfo#sessionID()}
   * @throws TsIllegalArgumentRtException сессия пользователя не зарегистрирована
   * @throws TsIllegalArgumentRtException недостаточно прав для завершения сессии
   * @throws TsRuntimeException ошибка закрытия сессии
   */
  void close( Skid aSessionID );
}
