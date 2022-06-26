package org.toxsoft.uskat.core.incub;

import static org.toxsoft.uskat.core.api.rtdserv.EQueryState.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.strid.idgen.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.rtdserv.*;

/**
 * {@link ISkHistoryQuery} base implementation.
 *
 * @author hazard157
 * @param <T> - queries temporal value type
 */
public class SkHistoryQuery<T extends ITemporalValue<T>>
    implements ISkHistoryQuery<T> {

  private static final String IDGEN_PREFIX = "SkHistoryQuery"; //$NON-NLS-1$

  private static final IStridGenerator uuidGenerator =
      new UuidStridGenerator( UuidStridGenerator.createState( IDGEN_PREFIX ) );

  private final GenericChangeEventer genericChangeEventer;

  private final String     queryId = uuidGenerator.nextId();
  private final IOptionSet params;
  private EQueryState      state   = UNPREPARED;

  /**
   * Constructor.
   *
   * @param aParams {@link IOptionSet} - query creation and учусгешщт parameters
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkHistoryQuery( IOptionSet aParams ) {
    TsNullArgumentRtException.checkNull( aParams );
    params = new OptionSet( aParams );
    genericChangeEventer = new GenericChangeEventer( this );
  }

  // ------------------------------------------------------------------------------------
  // IGenericChangeEventCapable
  //

  @Override
  public IGenericChangeEventer genericChangeEventer() {
    return genericChangeEventer;
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //

  @Override
  public void close() {
    if( state() == CLOSED ) {
      return;
    }
    cancel();
    state = CLOSED;
    // FIXME service.whenCloseHistDataQuery( this );
    genericChangeEventer.fireChangeEvent();
  }

  // ------------------------------------------------------------------------------------
  // ISkHistoryQuery
  //

  @Override
  public String queryId() {
    return queryId;
  }

  @Override
  public EQueryState state() {
    // if( state == EXECUTING ) {
    // // Таймаут (мсек) выполнения запроса
    // long timeout = OP_SK_HDQUERY_MAX_EXECUTION_TIME.getValue( options ).asLong();
    // if( timeout >= 0 && System.currentTimeMillis() - queryTimestamp > timeout ) {
    // cancel();
    // state = FAILED;
    // errorCode = -2;
    // errorMessage = String.format( FMT_ERR_QUERY_TIMEOUT, Long.valueOf( timeout ) );
    // genericChangeEventer.fireChangeEvent();
    // }
    // }
    // return state;
    // TODO реализовать SkHistoryQuery.state()
    throw new TsUnderDevelopmentRtException( "SkHistoryQuery.state()" );
  }

  @Override
  public IGwidList listGwids() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setGwids( IGwidList aList ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void exec( IQueryInterval aInterval ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void cancel() {
    // TODO Auto-generated method stub

  }

  @Override
  public ITimedList<T> get( Gwid aGwid ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public IMap<Gwid, ITimedList<T>> getAll() {
    // TODO Auto-generated method stub
    return null;
  }

}
