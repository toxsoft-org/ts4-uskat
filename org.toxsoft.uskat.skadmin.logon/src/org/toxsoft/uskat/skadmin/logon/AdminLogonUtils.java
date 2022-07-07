package org.toxsoft.uskat.skadmin.logon;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.s5.common.sessions.IS5SessionInfo;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;

/**
 * Всмомогательные методы пакета
 *
 * @author mvk
 */
public class AdminLogonUtils {

  /**
   * Возвращает текстовое представление соединения
   *
   * @param aConnection {@link ISkConnection} соединение
   * @return String текстовое представление
   * @throws TsNullArgumentRtException аргумент = null
   */
  static String connectionToString( ISkConnection aConnection ) {
    TsNullArgumentRtException.checkNull( aConnection );
    IS5SessionInfo sessionInfo =
        IS5ServerHardConstants.OP_BACKEND_SESSION_INFO.getValue( aConnection.backendInfo().params() ).asValobj();
    return sessionInfo.toString();
  }

}
