package org.toxsoft.uskat.skadmin.dev.objects;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.primtypes.IStringListEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.core.ISkCoreApi;
import ru.uskat.core.api.sysdescr.ISkAttrInfo;

/**
 * Вспомогательные методы пакета
 *
 * @author mvk
 */
class AdminObjectsUtils {

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
    IStringListEdit strids = new StringArrayList( aStrid );
    IStringListEdit attrIds = new StringArrayList( aAttrId );
    if( aStrid.equals( "*" ) ) {//$NON-NLS-1$
      strids.clear();
      ISkidList skids = aCoreApi.objService().listSkids( aClassId, true );
      for( Skid skid : skids ) {
        strids.add( skid.strid() );
      }
    }
    if( aAttrId.equals( "*" ) ) { //$NON-NLS-1$
      attrIds.clear();
      IStridablesList<ISkAttrInfo> infos = aCoreApi.sysdescr().classInfoManager().getClassInfo( aClassId ).attrInfos();
      for( ISkAttrInfo info : infos ) {
        attrIds.add( info.id() );
      }
    }
    GwidList retValue = new GwidList();
    for( String strid : strids ) {
      for( String attrId : attrIds ) {
        retValue.add( Gwid.createAttr( aClassId, strid, attrId ) );
      }
    }
    return retValue;
  }

}
