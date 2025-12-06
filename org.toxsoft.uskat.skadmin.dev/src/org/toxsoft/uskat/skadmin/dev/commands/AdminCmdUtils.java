package org.toxsoft.uskat.skadmin.dev.commands;

import static org.toxsoft.core.tslib.gw.gwid.Gwid.*;

import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Вспомогательные методы пакета
 *
 * @author mvk
 */
public class AdminCmdUtils {

  /**
   * Возвращает список {@link Gwid}-идентификаторов объектов
   *
   * @param aCoreApi {@link ISkCoreApi} API соединения
   * @param aClassId String идентификатор класса объектов
   * @param aStrid String идентификатор объекта
   * @return {@link ISkidList} список идентификторов объектов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static ISkidList getObjSkids( ISkCoreApi aCoreApi, String aClassId, String aStrid ) {
    TsNullArgumentRtException.checkNulls( aCoreApi, aClassId, aStrid );
    SkidList retValue = new SkidList();
    ITsThreadExecutor executor = SkThreadExecutorService.getExecutor( aCoreApi );
    executor.syncExec( () -> {
      if( aStrid.equals( STR_MULTI_ID ) ) {
        retValue.addAll( aCoreApi.objService().listSkids( aClassId, true ) );
        return;
      }
      retValue.add( new Skid( aClassId, aStrid ) );
    } );
    return retValue;
  }
}
