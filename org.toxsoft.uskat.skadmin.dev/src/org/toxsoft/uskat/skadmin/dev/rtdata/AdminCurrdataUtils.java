package org.toxsoft.uskat.skadmin.dev.rtdata;

import static org.toxsoft.uskat.skadmin.dev.rtdata.IAdminHardConstants.*;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.primtypes.IStringListEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.core.ISkCoreApi;
import ru.uskat.core.api.sysdescr.ISkRtdataInfo;

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
    IStringListEdit strids = new StringArrayList( aStrid );
    IStringListEdit dataIds = new StringArrayList( aDataId );
    if( aStrid.equals( MULTI ) ) {
      strids.clear();
      ISkidList skids = aCoreApi.objService().listSkids( aClassId, true );
      for( Skid skid : skids ) {
        strids.add( skid.strid() );
      }
    }
    if( aDataId.equals( MULTI ) ) {
      dataIds.clear();
      IStridablesList<ISkRtdataInfo> infos = aCoreApi.sysdescr().classInfoManager().getClassInfo( aClassId ).rtdInfos();
      for( ISkRtdataInfo info : infos ) {
        if( aCurrData && info.isCurr() || //
            aHistData && info.isHist() ) {
          dataIds.add( info.id() );
        }
      }
    }
    GwidList retValue = new GwidList();
    for( String strid : strids ) {
      for( String dataId : dataIds ) {
        retValue.add( Gwid.createRtdata( aClassId, strid, dataId ) );
      }
    }
    return retValue;
  }

}
