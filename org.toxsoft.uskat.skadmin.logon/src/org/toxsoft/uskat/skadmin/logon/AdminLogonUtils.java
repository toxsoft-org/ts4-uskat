package org.toxsoft.uskat.skadmin.logon;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.api.ISkBackendInfo;
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
   * @param aBackendInfo {@link ISkBackendInfo} информация о бекенде соединения
   * @return String текстовое представление
   * @throws TsNullArgumentRtException аргумент = null
   */
  static String connectionToString( ISkBackendInfo aBackendInfo ) {
    TsNullArgumentRtException.checkNull( aBackendInfo );
    IS5SessionInfo sessionInfo =
        IS5ServerHardConstants.OP_BACKEND_SESSION_INFO.getValue( aBackendInfo.params() ).asValobj();
    return sessionInfo.toString();
  }

}
