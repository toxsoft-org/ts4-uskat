package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.TsNotAllEnumsUsedRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.api.cmdserv.IDtoCompletedCommand;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.api.hqserv.*;
import org.toxsoft.uskat.core.backend.api.BaMsgQueryNextData;
import org.toxsoft.uskat.core.backend.api.IBaQueries;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;

/**
 * {@link ISkHistoryQueryService} implementation.
 *
 * @author mvk
 */
public class SkCoreServHistQueryService
    extends AbstractSkCoreService
    implements ISkHistoryQueryService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServHistQueryService::new;

  /**
   * Open queries
   */
  private final IStringMapEdit<SkAsynchronousQuery> openQueries = new StringMap<>();

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkCoreServHistQueryService( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkCoreService
  //
  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // nop
  }

  @Override
  protected void doClose() {
    for( ISkAsynchronousQuery query : openQueries.values() ) {
      query.close();
    }
    openQueries.clear();
  }

  @SuppressWarnings( "unchecked" )
  @Override
  protected boolean onBackendMessage( GenericMessage aMessage ) {
    String queryId = BaMsgQueryNextData.INSTANCE.getQueryId( aMessage );
    SkAsynchronousQuery query = openQueries.findByKey( queryId );
    if( query == null ) {
      return false;
    }
    EGwidKind kind = BaMsgQueryNextData.INSTANCE.getGwidKind( aMessage );
    ESkQueryState state = BaMsgQueryNextData.INSTANCE.getState( aMessage );
    switch( kind ) {
      case GW_ATTR:
      case GW_CLASS:
      case GW_CLOB:
      case GW_CMD_ARG:
      case GW_EVENT_PARAM:
      case GW_LINK:
      case GW_RIVET:
        return false;
      case GW_RTDATA:
        IStringMap<ITimedList<ITemporalAtomicValue>> values = BaMsgQueryNextData.INSTANCE.getAtomicValues( aMessage );
        query.nextData( (IStringMap<ITimedList<ITemporal<?>>>)(Object)values, state );
        break;
      case GW_EVENT:
        IStringMap<ITimedList<SkEvent>> events = BaMsgQueryNextData.INSTANCE.getEvents( aMessage );
        query.nextData( (IStringMap<ITimedList<ITemporal<?>>>)(Object)events, state );
        break;
      case GW_CMD:
        IStringMap<ITimedList<IDtoCompletedCommand>> commands = BaMsgQueryNextData.INSTANCE.getCommands( aMessage );
        query.nextData( (IStringMap<ITimedList<ITemporal<?>>>)(Object)commands, state );
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }

    return true;
  }

  // ------------------------------------------------------------------------------------
  // ISkHistoryQueryService
  //
  @Override
  public ISkQueryRawHistory createHistoricQuery( IOptionSet aOptions ) {
    TsNullArgumentRtException.checkNull( aOptions );
    SkQueryRawHistory retValue = new SkQueryRawHistory( this, aOptions );
    openQueries.put( retValue.queryId(), retValue );
    return retValue;
  }

  @Override
  public ISkQueryProcessedData createProcessedQuery( IOptionSet aOptions ) {
    TsNullArgumentRtException.checkNull( aOptions );
    SkQueryProcessedData retValue = new SkQueryProcessedData( this, aOptions );
    openQueries.put( retValue.queryId(), retValue );
    return retValue;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public IStringMap<ISkAsynchronousQuery> listOpenQueries() {
    return new StringMap<>( (IStringMap<ISkAsynchronousQuery>)(Object)openQueries );
  }

  // ------------------------------------------------------------------------------------
  // packet API
  //
  /**
   * Return query service backend.
   *
   * @return {@link IBaQueries} query service backend.
   */
  final IBaQueries backend() {
    return ba().findBackendAddon( IBaQueries.ADDON_ID, IBaQueries.class );
  }

  /**
   * Remove query from internal structures of service.
   *
   * @param aQueryId String query identifier.
   * @throws TsNullArgumentRtException argument = null
   */
  final void removeQuery( String aQueryId ) {
    TsNullArgumentRtException.checkNull( aQueryId );
    openQueries.removeByKey( aQueryId );
  }

  /**
   * Returns all existing single GWIDs covered by the specified GWID.
   * <p>
   * For unexisting GWID returns an empty list.
   * <p>
   * Note: method may be very resource-expensive!
   *
   * @param aGwid {@link Gwid} - the GWID to expand
   * @return {@link IGwidList} - an editable list of GWIDs
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  protected final IGwidList expandGwid( Gwid aGwid ) {
    TsNullArgumentRtException.checkNull( aGwid );
    return coreApi().gwidService().expandGwid( aGwid );
  }

}
