package org.toxsoft.uskat.s5.server.backend.addons.events;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.SkEvent;

/**
 * Вспомогательные методы для работы с событиями
 *
 * @author mvk
 */
public class S5BaEventsUtils {

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Возвращает текстовое представление списка событий
   *
   * @param aEvents {@link IList}&lt;{@link SkEvent}&gt;список событий
   * @return String текстовое представление
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static String events2str( IList<SkEvent> aEvents ) {
    TsNullArgumentRtException.checkNull( aEvents );
    if( aEvents.size() == 0 ) {
      return "no events"; //$NON-NLS-1$
    }
    String retValue = aEvents.get( 0 ).toString();
    if( aEvents.size() > 1 ) {
      retValue += String.format( "... and also %d events", Integer.valueOf( aEvents.size() - 1 ) ); //$NON-NLS-1$
    }
    return retValue;
  }

}
