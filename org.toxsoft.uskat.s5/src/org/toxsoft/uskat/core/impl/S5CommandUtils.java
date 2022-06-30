package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.cmdserv.DtoCommandStateChangeInfo;

/**
 * Вспомогательные методы для работы с командами
 *
 * @author mvk
 */
public class S5CommandUtils {

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Возвращает текстовое представление списка состояний команд
   *
   * @param aCommandStates {@link IList}&lt;{@link DtoCommandStateChangeInfo}&gt;список состояний команд
   * @return String текстовое представление
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static String commandStates2str( IList<DtoCommandStateChangeInfo> aCommandStates ) {
    TsNullArgumentRtException.checkNull( aCommandStates );
    if( aCommandStates.size() == 0 ) {
      return "no command states"; //$NON-NLS-1$
    }
    String retValue = aCommandStates.get( 0 ).toString();
    if( aCommandStates.size() > 1 ) {
      retValue += String.format( "... and also %d command states", Integer.valueOf( aCommandStates.size() - 1 ) ); //$NON-NLS-1$
    }
    return retValue;
  }
}
