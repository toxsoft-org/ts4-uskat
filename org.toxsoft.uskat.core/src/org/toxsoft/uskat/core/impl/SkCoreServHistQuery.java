package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.hqserv.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.devapi.*;

/**
 * {@link ISkHistoryQueryService} implementation.
 *
 * @author mvk
 */
public class SkCoreServHistQuery
    extends AbstractSkCoreService
    implements ISkHistoryQueryService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServHistQuery::new;

  /**
   * Open queries
   */
  private final IStringMapEdit<SkAsynchronousQuery> openQueries = new StringMap<>();

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkCoreServHistQuery( IDevCoreApi aCoreApi ) {
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
    String stateMessage = BaMsgQueryNextData.INSTANCE.getStateMessage( aMessage );
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
        long st = System.currentTimeMillis();
        LoggerUtils.defaultLogger().info( "SkCoreServHistQueryService.onBackendMessage(...) NextData recv" ); //$NON-NLS-1$
        IStringMap<ITimedList<ITemporalAtomicValue>> values = BaMsgQueryNextData.INSTANCE.getAtomicValues( aMessage );
        long et = System.currentTimeMillis();
        LoggerUtils.defaultLogger().info(
            "SkCoreServHistQueryService.onBackendMessage(...) values read time = %d (msec)", // //$NON-NLS-1$
            Long.valueOf( et - st ) );
        query.nextData( (IStringMap<ITimedList<ITemporal<?>>>)(Object)values, state, stateMessage );
        break;
      case GW_EVENT:
        IStringMap<ITimedList<SkEvent>> events = BaMsgQueryNextData.INSTANCE.getEvents( aMessage );
        query.nextData( (IStringMap<ITimedList<ITemporal<?>>>)(Object)events, state, stateMessage );
        break;
      case GW_CMD:
        IStringMap<ITimedList<IDtoCompletedCommand>> commands = BaMsgQueryNextData.INSTANCE.getCommands( aMessage );
        query.nextData( (IStringMap<ITimedList<ITemporal<?>>>)(Object)commands, state, stateMessage );
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
    checkThread();
    TsNullArgumentRtException.checkNull( aOptions );
    SkQueryRawHistory retValue = new SkQueryRawHistory( this, aOptions );
    openQueries.put( retValue.queryId(), retValue );
    return retValue;
  }

  @Override
  public ISkQueryProcessedData createProcessedQuery( IOptionSet aOptions ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aOptions );
    SkQueryProcessedData retValue = new SkQueryProcessedData( this, aOptions );
    openQueries.put( retValue.queryId(), retValue );
    return retValue;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public IStringMap<ISkAsynchronousQuery> listOpenQueries() {
    checkThread();
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
    return ba().baQueries();
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
