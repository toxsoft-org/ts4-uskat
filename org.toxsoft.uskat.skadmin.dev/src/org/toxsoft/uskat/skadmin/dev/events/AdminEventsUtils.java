package org.toxsoft.uskat.skadmin.dev.events;

import static org.toxsoft.uskat.skadmin.dev.events.IAdminHardConstants.*;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.primtypes.IStringListEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoEventInfo;

/**
 * Вспомогательные методы пакета
 *
 * @author mvk
 */
class AdminEventsUtils {

  /**
   * Возвращает список {@link Gwid}-идентификаторов событий
   *
   * @param aCoreApi {@link ISkCoreApi} API соединения
   * @param aClassId String идентификатор класса объектов
   * @param aStrid String идентификатор объекта
   * @param aEventId String идентификатор события
   * @return {@link IGwidList} список идентификторов событий
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static IGwidList getEventGwids( ISkCoreApi aCoreApi, String aClassId, String aStrid, String aEventId ) {
    TsNullArgumentRtException.checkNulls( aCoreApi, aClassId, aStrid, aEventId );
    IStringListEdit strids = new StringArrayList( aStrid );
    IStringListEdit eventIds = new StringArrayList( aEventId );
    if( aStrid.equals( MULTI ) ) {
      strids.clear();
      ISkidList skids = aCoreApi.objService().listSkids( aClassId, true );
      for( Skid skid : skids ) {
        strids.add( skid.strid() );
      }
    }
    if( aEventId.equals( MULTI ) ) {
      eventIds.clear();
      IStridablesList<IDtoEventInfo> infos = aCoreApi.sysdescr().getClassInfo( aClassId ).events().list();
      for( IDtoEventInfo info : infos ) {
        eventIds.add( info.id() );
      }
    }
    GwidList retValue = new GwidList();
    for( String strid : strids ) {
      for( String eventId : eventIds ) {
        retValue.add( Gwid.createEvent( aClassId, strid, eventId ) );
      }
    }
    return retValue;
  }

}
