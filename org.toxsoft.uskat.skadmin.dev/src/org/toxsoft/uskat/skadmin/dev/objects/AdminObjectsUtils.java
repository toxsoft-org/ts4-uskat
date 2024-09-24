package org.toxsoft.uskat.skadmin.dev.objects;

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
class AdminObjectsUtils {

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
    IStringListEdit strids = new StringArrayList( aStrid );
    if( aStrid.equals( "*" ) ) {//$NON-NLS-1$
      strids.clear();
      ISkidList skids = aCoreApi.objService().listSkids( aClassId, true );
      for( Skid skid : skids ) {
        strids.add( skid.strid() );
      }
    }
    SkidList retValue = new SkidList();
    for( String strid : strids ) {
      retValue.add( new Skid( aClassId, strid ) );
    }
    return retValue;
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
      IStridablesList<IDtoAttrInfo> infos = aCoreApi.sysdescr().getClassInfo( aClassId ).attrs().list();
      for( IDtoAttrInfo info : infos ) {
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
