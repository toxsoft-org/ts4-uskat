package org.toxsoft.uskat.skadmin.dev.rtdata;

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
class AdminCurrdataUtils {

  /**
   * Возвращает список {@link Gwid}-идентификаторов данных
   *
   * @param aCoreApi {@link ISkCoreApi} API соединения
   * @param aClassId String идентификатор класса объектов
   * @param aStrid String идентификатор объекта
   * @param aDataId String идентификатор данного
   * @param aCurrData boolean <b>true</b> добавлять текущие данные. <b>false</b> не добавлять текущие данные.
   * @param aHistData boolean <b>true</b> добавлять хранимые данные. <b>false</b> не добавлять хранимые данные.
   * @return {@link IGwidList} список идентификторов текущих данных
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static IGwidList getDataGwids( ISkCoreApi aCoreApi, String aClassId, String aStrid, String aDataId, boolean aCurrData,
      boolean aHistData ) {
    TsNullArgumentRtException.checkNulls( aCoreApi, aClassId, aStrid, aDataId );
    SkidList objIds = new SkidList();
    if( aStrid.equals( STR_MULTI_ID ) ) {
      objIds.setAll( aCoreApi.objService().listSkids( aClassId, true ) );
    }
    else {
      objIds.add( new Skid( aClassId, aStrid ) );
    }
    IStringListEdit dataIds = new StringArrayList( aDataId );
    if( aDataId.equals( STR_MULTI_ID ) ) {
      dataIds.clear();
      IStridablesList<IDtoRtdataInfo> infos = aCoreApi.sysdescr().getClassInfo( aClassId ).rtdata().list();
      for( IDtoRtdataInfo info : infos ) {
        if( aCurrData && info.isCurr() || //
            aHistData && info.isHist() ) {
          dataIds.add( info.id() );
        }
      }
    }
    GwidList retValue = new GwidList();
    for( Skid objId : objIds ) {
      for( String dataId : dataIds ) {
        retValue.add( Gwid.createRtdata( objId.classId(), objId.strid(), dataId ) );
      }
    }
    return retValue;
  }

}
