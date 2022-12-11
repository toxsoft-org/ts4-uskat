package org.toxsoft.uskat.regref.lib.impl;

import static org.toxsoft.uskat.regref.lib.impl.ISkRegRefServiceHardConstants.*;

import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.api.evserv.ISkEventService;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.ISkSysdescr;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoEventInfo;
import org.toxsoft.uskat.regref.lib.ISkRriHistory;

/**
 * {@link ISkRriHistory} implementation.
 *
 * @author goga
 */
class SkRriHistory
    implements ISkRriHistory {

  private final SkRegRefInfoService rriService;
  private final ISkCoreApi          coreApi;

  SkRriHistory( SkRegRefInfoService aOwner ) {
    rriService = aOwner;
    coreApi = rriService.coreApi();
  }

  @Override
  public IDtoEventInfo getParamEditEventInfo() {
    ISkSysdescr cim = coreApi.sysdescr();
    ISkClassInfo cinf = cim.getClassInfo( CLASSID_RRI_SECTION );
    return cinf.events().list().getByKey( EVID_RRI_PARAM_CHANGE );
  }

  @Override
  public ITimedList<SkEvent> querySectionEditingHistory( IQueryInterval aInterval, String aSectionId ) {
    TsNullArgumentRtException.checkNull( aInterval );
    ISkEventService evs = coreApi.eventService();
    Gwid gwid = Gwid.createEvent( CLASSID_RRI_SECTION, aSectionId, EVID_RRI_PARAM_CHANGE );
    return evs.queryObjEvents( aInterval, gwid );
  }

}
