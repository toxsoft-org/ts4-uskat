package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.gwid.EGwidKind;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.ISkServiceCreator;
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

  @Override
  protected boolean onBackendMessage( GenericMessage aMessage ) {
    String queryId = BaMsgQueryNextData.INSTANCE.getQueryId( aMessage );
    SkAsynchronousQuery query = openQueries.findByKey( queryId );
    if( query == null ) {
      return false;
    }
    EGwidKind kind = BaMsgQueryNextData.INSTANCE.getGwidKind( aMessage );
    boolean finished = BaMsgQueryNextData.INSTANCE.getIsFinished( aMessage );
    switch( kind ) {
      case GW_ATTR:
      case GW_CLASS:
      case GW_CLOB:
      case GW_CMD:
      case GW_CMD_ARG:
      case GW_EVENT:
      case GW_EVENT_PARAM:
      case GW_LINK:
      case GW_RIVET:
        return false;
      case GW_RTDATA:
        IStringMap<ITimedList<ITemporalAtomicValue>> values = BaMsgQueryNextData.INSTANCE.getAtomicValues( aMessage );
        query.nextData( values, finished );
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
    // TODO Auto-generated method stub
    throw new TsUnderDevelopmentRtException();
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
}
