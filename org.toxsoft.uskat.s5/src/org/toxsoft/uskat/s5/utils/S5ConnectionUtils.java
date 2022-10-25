package org.toxsoft.uskat.s5.utils;

import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.users.ISkUser;
import org.toxsoft.uskat.core.connection.ISkConnection;
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
   * @param aConnection {@link ISkConnection} соединение с s5-сервером
   * @return String логин пользователя подключенного к серверу
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException нет соединения с сервером
   */
  public static String getConnectedUserLogin( ISkConnection aConnection ) {
    TsNullArgumentRtException.checkNull( aConnection );
    IS5SessionInfo sessionInfo =
        IS5ServerHardConstants.OP_BACKEND_SESSION_INFO.getValue( aConnection.backendInfo().params() ).asValobj();
    return sessionInfo.login();
  }

  /**
   * Возвращает пользователя подключенного к серверу.
   *
   * @param aConnection {@link ISkConnection} соединение с s5-сервером
   * @return {@link ISkUser} пользователь подключенный к серверу
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException нет соединения с сервером
   */
  public static ISkUser getConnectedUser( ISkConnection aConnection ) {
    TsNullArgumentRtException.checkNull( aConnection );
    String login = getConnectedUserLogin( aConnection );
    return aConnection.coreApi().userService().getUser( login );
  }
}
