package org.toxsoft.uskat.s5.server.sessions;

import org.jboss.ejb.client.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Вспомогательные методы пакета
 *
 * @author mvk
 */
public class S5SessionUtils {

  /**
   * Возвращает строку представляющую идентификатор сессии
   *
   * @param aSessionID {@link SessionID} идентификатор сессии
   * @param aFormat boolean <b>true</b> форматировать вывод; <b>false</b> не форматировать вывод.
   * @return String строка представляющая сессию
   */
  public static String sessionIDToString( SessionID aSessionID, boolean aFormat ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    String s = aSessionID.toString();
    if( !aFormat ) {
      return s.substring( s.indexOf( '[' ) + 1, s.indexOf( ']' ) );
    }
    int length = s.length();
    return s.substring( length - 5, length - 1 );
  }

  /**
   * Возвращает строку представляющую идентификатор сессии
   *
   * @param aSessionID {@link Skid} идентификатор сессии
   * @param aFormat boolean <b>true</b> форматировать вывод; <b>false</b> не форматировать вывод.
   * @return String строка представляющая сессию
   */
  public static String sessionIDToString( Skid aSessionID, boolean aFormat ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    String s = aSessionID.toString();
    if( !aFormat ) {
      return s.substring( s.indexOf( '[' ) + 1, s.indexOf( ']' ) );
    }
    int length = s.length();
    return s.substring( length - 5, length - 1 );
  }
}
