package org.toxsoft.uskat.skadmin.dev.objects;

import static org.toxsoft.core.tslib.gw.gwid.Gwid.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * Вспомогательные методы пакета
 *
 * @author mvk
 */
public class AdminObjectsUtils {

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
    if( aStrid.equals( STR_MULTI_ID ) ) {
      return aCoreApi.objService().listSkids( aClassId, true );
    }
    return new SkidList( new Skid( aClassId, aStrid ) );
  }

  /**
   * Возвращает список {@link Gwid}-идентификаторов атрибутов
   *
   * @param aCoreApi {@link ISkCoreApi} API соединения
   * @param aClassId String идентификатор класса объектов
   * @param aStrid String идентификатор объекта
   * @param aAttrId String идентификатор атрибута
   * @return {@link IGwidList} список идентификторов атрибутов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static IGwidList getAttrGwids( ISkCoreApi aCoreApi, String aClassId, String aStrid, String aAttrId ) {
    TsNullArgumentRtException.checkNulls( aCoreApi, aClassId, aStrid, aAttrId );
    SkidList objIds = new SkidList();
    if( aStrid.equals( STR_MULTI_ID ) ) {
      objIds.setAll( aCoreApi.objService().listSkids( aClassId, true ) );
    }
    else {
      objIds.add( new Skid( aClassId, aStrid ) );
    }
    IStringListEdit attrIds = new StringArrayList( aAttrId );
    if( aAttrId.equals( STR_MULTI_ID ) ) {
      attrIds.clear();
      IStridablesList<IDtoAttrInfo> infos = aCoreApi.sysdescr().getClassInfo( aClassId ).attrs().list();
      for( IDtoAttrInfo info : infos ) {
        attrIds.add( info.id() );
      }
    }
    GwidList retValue = new GwidList();
    for( Skid objId : objIds ) {
      for( String attrId : attrIds ) {
        retValue.add( Gwid.createAttr( objId.classId(), objId.strid(), attrId ) );
      }
    }
    return retValue;
  }

}
