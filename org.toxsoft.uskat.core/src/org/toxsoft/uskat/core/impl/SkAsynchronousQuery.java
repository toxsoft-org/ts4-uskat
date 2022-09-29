package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.core.api.hqserv.ESkQueryState.*;
import static org.toxsoft.uskat.core.api.hqserv.ISkHistoryQueryServiceConstants.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.events.change.GenericChangeEventer;
import org.toxsoft.core.tslib.bricks.events.change.IGenericChangeEventer;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;
import org.toxsoft.uskat.core.api.hqserv.ESkQueryState;
import org.toxsoft.uskat.core.api.hqserv.ISkAsynchronousQuery;
import org.toxsoft.uskat.core.backend.api.IBaQueries;

/**
 * {@link ISkAsynchronousQuery} abstract implementation.
 *
 * @author mvk
 */
public abstract class SkAsynchronousQuery
    implements ISkAsynchronousQuery {

  private final String                        queryId;
  private final IOptionSet                    options;
  private final SkCoreServHistQueryService service;
  private final GenericChangeEventer          eventer = new GenericChangeEventer( this );
  private ESkQueryState                       state   = UNPREPARED;

  private long                                         queryTimestamp = System.currentTimeMillis();
  private IMap<Gwid, ITimedList<ITemporalAtomicValue>> result         = IMap.EMPTY;
  private int                                          errorCode      = 0;
  private String                                       errorMessage   = EMPTY_STRING;

  /**
   * Constructor
   *
   * @param aService {@link SkCoreServHistQueryService} the query service
   * @param aQueryId String the query indentifier.
   * @param aOptions {@link IOptionSet} - optional query execution parameters.
   * @throws TsNullArgumentRtException any argurment = null
   */
  protected SkAsynchronousQuery( SkCoreServHistQueryService aService, String aQueryId, IOptionSet aOptions ) {
    TsNullArgumentRtException.checkNulls( aService, aQueryId, aOptions );
    service = aService;
    queryId = aQueryId;
    options = aOptions;
  }

  // ------------------------------------------------------------------------------------
  // ISkAsynchronousQuery
  //
  @Override
  public final String queryId() {
    return queryId;
  }

  @Override
  public IOptionSet params() {
    return options;
  }

  @Override
  public ESkQueryState state() {
    if( state == EXECUTING ) {
      // Таймаут (мсек) выполнения запроса
      long timeout = OP_SK_MAX_EXECUTION_TIME.getValue( options ).asLong();
      if( timeout >= 0 && System.currentTimeMillis() - queryTimestamp > timeout ) {
        cancel();
        state = FAILED;
        errorCode = -2;
        errorMessage = String.format( FMT_ERR_QUERY_TIMEOUT, Long.valueOf( timeout ) );
        eventer.fireChangeEvent();
      }
    }
    return state;
  }

  @Override
  public void exec( IQueryInterval aInterval ) {
    TsNullArgumentRtException.checkNull( aInterval );
    checkInvalidState( this, UNPREPARED, EXECUTING, CLOSED );
    state = EXECUTING;
    queryTimestamp = System.currentTimeMillis();
    eventer.fireChangeEvent();
    try {
      backend().execQuery( queryId, aInterval );
    }
    catch( Throwable e ) {
      state = FAILED;
      result = IMap.EMPTY;
      errorCode = -1;
      errorMessage = e.getLocalizedMessage();
      eventer.fireChangeEvent();
      throw new TsInternalErrorRtException( e );
    }

  }

  @Override
  public void cancel() {
    if( state == CLOSED ) {
      throw new TsIllegalArgumentRtException( FMT_ERR_QUERY_INVALID_STATE, this, state );
    }
    if( state != EXECUTING ) {
      return;
    }
    try {
      backend().cancel( queryId );
    }
    catch( Exception e ) {
      LoggerUtils.errorLogger().error( e );
    }
    state = PREPARED;
    eventer.fireChangeEvent();
  }

  @Override
  public void close() {
    if( state() == CLOSED ) {
      return;
    }
    service.removeQuery( queryId );
    cancel();
    try {
      backend().close( queryId );
    }
    catch( Exception e ) {
      LoggerUtils.errorLogger().error( e );
    }
    state = CLOSED;
    eventer.fireChangeEvent();
  }

  @Override
  public IGenericChangeEventer genericChangeEventer() {
    return eventer;
  }

  // ------------------------------------------------------------------------------------
  // packet implementation
  //
  /**
   * Process the received data
   *
   * @param aValues {@link IStringMap}&lt;{@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt;&gt; - the values map
   * @param aFinished boolean - finished flag
   * @throws TsNullArgumentRtException argument = null
   */
  void nextData( IStringMap<ITimedList<ITemporalAtomicValue>> aValues, boolean aFinished ) {
    TsNullArgumentRtException.checkNull( aValues );
    doNextData( aValues, aFinished );
    if( aFinished ) {
      state = READY;
    }
    eventer.fireChangeEvent();
  }

  // ------------------------------------------------------------------------------------
  // API for descendants
  //
  /**
   * Return the query service service.
   *
   * @return {@link IBaQueries} the query service service
   */
  protected final IBaQueries backend() {
    return service.backend();
  }

  // ------------------------------------------------------------------------------------
  // abstract implementation
  //
  /**
   * Process the received data
   *
   * @param aValues {@link IStringMap}&lt;{@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt;&gt; - the values map
   * @param aFinished boolean - finished flag
   * @throws TsNullArgumentRtException argument = null
   */
  protected abstract void doNextData( IStringMap<ITimedList<ITemporalAtomicValue>> aValues, boolean aFinished );

  // ------------------------------------------------------------------------------------
  // private implementation
  //
  private static void checkInvalidState( SkAsynchronousQuery aQuery, ESkQueryState... aInvalidStates ) {
    TsNullArgumentRtException.checkNulls( aQuery, aInvalidStates );
    ESkQueryState currState = aQuery.state();
    for( ESkQueryState state : aInvalidStates ) {
      if( currState == state ) {
        throw new TsIllegalArgumentRtException( FMT_ERR_QUERY_INVALID_STATE, aQuery, currState );
      }
    }
  }

}
