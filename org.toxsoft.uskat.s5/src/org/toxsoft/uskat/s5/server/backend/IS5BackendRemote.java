package org.toxsoft.uskat.s5.server.backend;

import javax.ejb.Remote;

import org.jboss.ejb.client.SessionID;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsRuntimeException;
import org.toxsoft.uskat.s5.common.sessions.IS5SessionInfo;
import org.toxsoft.uskat.s5.server.sessions.init.*;

import ru.uskat.core.api.ISkBackend;

/**
 * Удаленный доступ к {@link ISkBackend} предоставляемый s5-сервером
 *
 * @author mvk
 */
@Remote
public interface IS5BackendRemote
    extends IS5Backend {

  /**
   * Удаленная инициализация (активирует) движка.
   *
   * @param aInitSessionData {@link IS5SessionInitData} данные для инициализации сессии
   * @return {@link S5SessionInitResult} - результат инициализации сессии
   */
  IS5SessionInitResult init( IS5SessionInitData aInitSessionData );

  /**
   * Закрыть сессию пользователя
   *
   * @param aSessionID {@link SessionID} идентификатор сессии {@link IS5SessionInfo#sessionID()}
   * @throws TsIllegalArgumentRtException сессия пользователя не зарегистрирована
   * @throws TsIllegalArgumentRtException недостаточно прав для завершения сессии
   * @throws TsRuntimeException ошибка закрытия сессии
   */
  @Override
  void close( Skid aSessionID );

}
