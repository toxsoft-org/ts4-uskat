package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.api.hqserv.ESkQueryState.*;
import static org.toxsoft.uskat.core.api.hqserv.ISkHistoryQueryServiceConstants.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.change.GenericChangeEventer;
import org.toxsoft.core.tslib.bricks.events.change.IGenericChangeEventer;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
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

  /**
   * Пустой список {@link ITimedList}
   */
  public static final ITimedList<?> EMPTY_TIMED_LIST = new TimedList<>();

  private final String                     queryId;
  private final IOptionSet                 options;
  private final SkCoreServHistQueryService service;
  private final GenericChangeEventer       eventer      = new GenericChangeEventer( this );
  private ESkQueryState                    state        = UNPREPARED;
  private String                           stateMessage = TsLibUtils.EMPTY_STRING;

  private long queryTimestamp = System.currentTimeMillis();

  /**
   * Constructor
   *
   * @param aService {@link SkCoreServHistQueryService} the query service
   * @param aOptions {@link IOptionSet} - optional query execution parameters.
   * @throws TsNullArgumentRtException any argurment = null
   */
  protected SkAsynchronousQuery( SkCoreServHistQueryService aService, IOptionSet aOptions ) {
    TsNullArgumentRtException.checkNulls( aService, aOptions );
    service = aService;
    options = aOptions;
    queryId = backend().createQuery( aOptions );
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
    checkThread();
    if( state == EXECUTING ) {
      // Таймаут (мсек) выполнения запроса
      long timeout = OP_SK_MAX_EXECUTION_TIME.getValue( options ).asLong();
      if( timeout >= 0 && System.currentTimeMillis() - queryTimestamp > timeout ) {
        cancel();
        changeState( FAILED );
      }
    }
    return state;
  }

  @Override
  public String stateMessage() {
    return stateMessage;
  }

  @Override
  public void exec( IQueryInterval aInterval ) {
    checkThread();
    TsNullArgumentRtException.checkNull( aInterval );
    checkInvalidState( this, UNPREPARED, EXECUTING, CLOSED );
    queryTimestamp = System.currentTimeMillis();
    changeState( EXECUTING );
    try {
      backend().execQuery( queryId, aInterval );
    }
    catch( Throwable e ) {
      changeState( FAILED );
      throw new TsInternalErrorRtException( e );
    }

  }

  @Override
  public void cancel() {
    checkThread();
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
    changeState( PREPARED );
  }

  @Override
  public void close() {
    checkThread();
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
    changeState( CLOSED );
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
   * @param aValues {@link IStringMap}&lt;{@link ITimedList}&lt;{@link ITemporal}&gt;&gt; - the values map
   * @param aState {@link ESkQueryState} - query state
   * @param aStateMessage String - query state message
   * @throws TsNullArgumentRtException argument = null
   */
  void nextData( IStringMap<ITimedList<ITemporal<?>>> aValues, ESkQueryState aState, String aStateMessage ) {
    TsNullArgumentRtException.checkNulls( aValues, aState );
    doNextData( aValues, aState );
    changeState( aState, aStateMessage );
  }

  // ------------------------------------------------------------------------------------
  // API for descendants
  //
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
    return service.expandGwid( aGwid );
  }

  /**
   * Return the query service service.
   *
   * @return {@link IBaQueries} the query service service
   */
  protected final IBaQueries backend() {
    return service.backend();
  }

  /**
   * Изменить состояние запроса
   *
   * @param aState {@link ESkQueryState} новое состояние
   * @throws TsNullArgumentRtException аргумент = nll
   */
  protected final void changeState( ESkQueryState aState ) {
    changeState( aState, TsLibUtils.EMPTY_STRING );
  }

  /**
   * Изменить состояние запроса
   *
   * @param aState {@link ESkQueryState} новое состояние
   * @param aStateMessage String текстовая информация по новому состоянию
   * @throws TsNullArgumentRtException аргумент = nll
   */
  protected final void changeState( ESkQueryState aState, String aStateMessage ) {
    TsNullArgumentRtException.checkNulls( aState, aStateMessage );
    state = aState;
    stateMessage = aStateMessage;
    eventer.fireChangeEvent();
  }

  /**
   * Проверяет что вызов производится из API-потока
   *
   * @throws TsIllegalStateRtException invalid thread access
   */
  protected final void checkThread() {
    service.checkThread();
  }

  // ------------------------------------------------------------------------------------
  // abstract implementation
  //
  /**
   * Process the received data
   *
   * @param aValues {@link IStringMap}&lt;{@link ITimedList}&lt;{@link ITemporal}&gt;&gt; - the values map
   * @param aState {@link ESkQueryState} - query state
   * @throws TsNullArgumentRtException argument = null
   */
  protected abstract void doNextData( IStringMap<ITimedList<ITemporal<?>>> aValues, ESkQueryState aState );

  // ------------------------------------------------------------------------------------
  // api for descedents
  //
  protected static void checkInvalidState( SkAsynchronousQuery aQuery, ESkQueryState... aInvalidStates ) {
    TsNullArgumentRtException.checkNulls( aQuery, aInvalidStates );
    ESkQueryState currState = aQuery.state();
    for( ESkQueryState state : aInvalidStates ) {
      if( currState == state ) {
        throw new TsIllegalArgumentRtException( FMT_ERR_QUERY_INVALID_STATE, aQuery, currState );
      }
    }
  }

}
