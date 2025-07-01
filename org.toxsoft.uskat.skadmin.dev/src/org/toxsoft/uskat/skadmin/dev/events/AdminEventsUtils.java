package org.toxsoft.uskat.skadmin.dev.events;

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
    SkidList objIds = new SkidList();
    if( aStrid.equals( STR_MULTI_ID ) ) {
      objIds.setAll( aCoreApi.objService().listSkids( aClassId, true ) );
    }
    else {
      objIds.add( new Skid( aClassId, aStrid ) );
    }
    IStringListEdit eventIds = new StringArrayList( aEventId );
    if( aEventId.equals( STR_MULTI_ID ) ) {
      eventIds.clear();
      IStridablesList<IDtoEventInfo> infos = aCoreApi.sysdescr().getClassInfo( aClassId ).events().list();
      for( IDtoEventInfo info : infos ) {
        eventIds.add( info.id() );
      }
    }
    GwidList retValue = new GwidList();
    for( Skid objId : objIds ) {
      for( String eventId : eventIds ) {
        retValue.add( Gwid.createEvent( objId.classId(), objId.strid(), eventId ) );
      }
    }
    return retValue;
  }

}
