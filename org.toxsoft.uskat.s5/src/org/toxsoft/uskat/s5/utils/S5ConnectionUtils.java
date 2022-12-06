package org.toxsoft.uskat.s5.utils;

import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.api.users.ISkUser;
import org.toxsoft.uskat.core.impl.SkCoreApi;
import org.toxsoft.uskat.s5.common.sessions.IS5SessionInfo;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;

/**
 * Вспомогательные методы для работы с соединением s5-сервером
 *
 * @author mvk
 */
public class S5ConnectionUtils {

  /**
   * Возвращает логин пользователя подключенного к серверу.
   *
   * @param aCoreApi {@link ISkCoreApi} API соединения
   * @return String логин пользователя подключенного к серверу
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException нет открытого соединения с сервером
   */
  public static String getConnectedUserLogin( ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNull( aCoreApi );
    IS5SessionInfo sessionInfo = IS5ServerHardConstants.OP_BACKEND_SESSION_INFO
        .getValue( ((SkCoreApi)aCoreApi).backend().getBackendInfo().params() ).asValobj();
    return sessionInfo.login();
  }

  /**
   * Возвращает пользователя подключенного к серверу.
   *
   * @param aCoreApi {@link ISkCoreApi} API соединения
   * @return {@link ISkUser} пользователь подключенный к серверу
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException нет активного соединения с сервером
   */
  public static ISkUser getConnectedUser( ISkCoreApi aCoreApi ) {
    TsNullArgumentRtException.checkNull( aCoreApi );
    String login = getConnectedUserLogin( aCoreApi );
    return aCoreApi.userService().getUser( login );
  }
}
