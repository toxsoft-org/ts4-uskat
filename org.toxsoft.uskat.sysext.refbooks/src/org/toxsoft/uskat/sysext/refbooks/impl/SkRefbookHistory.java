package org.toxsoft.uskat.sysext.refbooks.impl;

import static org.toxsoft.uskat.sysext.refbooks.ISkRefbookServiceHardConstants.*;

import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.sysext.refbooks.ISkRefbookHistory;

import ru.uskat.common.dpu.rt.events.SkEvent;
import ru.uskat.core.ISkCoreApi;
import ru.uskat.core.api.events.ISkEventService;
import ru.uskat.core.api.sysdescr.*;

/**
 * {@link ISkRefbookHistory} implementation.
 *
 * @author hazard157
 */
class SkRefbookHistory
    implements ISkRefbookHistory {

  private final SkRefbookService owner;
  private final ISkCoreApi       coreApi;

  /**
   * Constructor.
   *
   * @param aOwner {@link SkRefbookService} - owner service
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkRefbookHistory( SkRefbookService aOwner ) {
    TsNullArgumentRtException.checkNull( aOwner );
    owner = aOwner;
    coreApi = owner.coreApi();
  }

  // ------------------------------------------------------------------------------------
  // ISkRefbookHistory
  //

  @Override
  public ISkEventInfo getRefbookItemEditEventInfo() {
    ISkClassInfoManager cim = coreApi.sysdescr().classInfoManager();
    ISkClassInfo cinf = cim.getClassInfo( CLASSID_REFBOOK );
    return cinf.eventInfos().getByKey( EVID_REFBOOK_ITEM_CHANGE );
  }

  @Override
  public ITimedList<SkEvent> queryRefbookEditingHistory( IQueryInterval aInterval, String aRefbookId ) {
    TsNullArgumentRtException.checkNull( aInterval );
    ISkEventService evs = coreApi.eventService();
    Skid refbookSkid = makeRefbookObjSkid( aRefbookId );
    Gwid gwid = Gwid.createEvent( refbookSkid.classId(), refbookSkid.strid(), EVID_REFBOOK_ITEM_CHANGE );
    return evs.history().query( aInterval, gwid );
  }

}
