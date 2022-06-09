package org.toxsoft.uskat.skadmin.logon;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.core.api.users.ISkSession;
import ru.uskat.core.connection.ISkConnection;

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
    ISkSession session = aConnection.sessionInfo();
    return session.toString();
  }

}
