package org.toxsoft.uskat.core.utils;

import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * Вспомогательные методы для работы с командами.
 *
 * @author mvk
 */
public class SkCommandUtils {

  /**
   * Обновление(регистрация) обработчика команд на выполнение команд для объектов указанного класса.
   *
   * @param aCoreApi {@link ISkCoreApi} API сервера
   * @param aExecutor {@link ISkCommandExecutor} исполнитель команд
   * @param aClassId String список идентификатор класса объектов выполняемых команд
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void defineCmdExecutor( ISkCoreApi aCoreApi, ISkCommandExecutor aExecutor, String aClassId ) {
    defineCmdExecutor( aCoreApi, aExecutor, new StringArrayList( aClassId ) );
  }

  /**
   * Обновление(регистрация) обработчика команд на выполнение команд для объектов указанных классов.
   *
   * @param aCoreApi {@link ISkCoreApi} API сервера
   * @param aExecutor {@link ISkCommandExecutor} исполнитель команд
   * @param aClassIds {@link IStringList} список идентификаторов классов объектов выполняемых команд
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void defineCmdExecutor( ISkCoreApi aCoreApi, ISkCommandExecutor aExecutor, IStringList aClassIds ) {
    TsNullArgumentRtException.checkNulls( aCoreApi, aExecutor, aClassIds );
    GwidList gwids = new GwidList();
    for( String classId : aClassIds ) {
      ISkClassInfo classInfo = aCoreApi.sysdescr().getClassInfo( classId );
      ISkidList objIds = aCoreApi.objService().listSkids( classId, true ); // aIncludeSubclasses = true
      for( Skid objId : objIds ) {
        for( String cmdId : classInfo.cmds().list().keys() ) {
          gwids.add( Gwid.createCmd( objId, cmdId ) );
        }
      }
    }
    aCoreApi.cmdService().registerExecutor( aExecutor, gwids );
  }

  /**
   * Дерегистрация обработчика команд.
   *
   * @param aCoreApi {@link ISkCoreApi} API сервера
   * @param aExecutor {@link ISkCommandExecutor} исполнитель команд
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void removeCmdExecutor( ISkCoreApi aCoreApi, ISkCommandExecutor aExecutor ) {
    TsNullArgumentRtException.checkNulls( aCoreApi, aExecutor );
    aCoreApi.cmdService().unregisterExecutor( aExecutor );
  }

  private SkCommandUtils() {

  }

}
