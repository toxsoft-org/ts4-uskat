package org.toxsoft.uskat.s5.server.backend.addons.queries;

import static org.toxsoft.uskat.core.api.hqserv.ESkQueryState.*;
import static org.toxsoft.uskat.s5.server.backend.addons.queries.IS5Resources.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.strid.impl.StridableParameterizedSer;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.hqserv.*;

/**
 * Серверный конвой-объект для выполнения запросов {@link ISkAsynchronousQuery}
 * <p>
 * {@link S5BaQueriesConvoy#id()} представляет идентификтор запроса {@link ISkAsynchronousQuery#queryId()}.
 * <p>
 * {@link S5BaQueriesConvoy#params()} представляет параметры запроса {@link ISkAsynchronousQuery#params()}.
 *
 * @author mvk
 */
public class S5BaQueriesConvoy
    extends StridableParameterizedSer
    implements ICloseable {

  private static final long serialVersionUID = 157157L;

  private ESkQueryState              state = UNPREPARED;
  private IQueryInterval             interval;
  private IStringMap<IDtoQueryParam> args;

  private long startTime = System.currentTimeMillis();

  /**
   * Конструктор
   *
   * @param aQueryId String идентификатор запроса {@link ISkAsynchronousQuery#queryId()}.
   * @param aParams {@link IOptionSet} параметры запроса {@link ISkAsynchronousQuery#params()}.
   * @throws TsNullArgumentRtException любой аргумент = null.
   */
  public S5BaQueriesConvoy( String aQueryId, IOptionSet aParams ) {
    super( aQueryId, aParams );
  }

  /**
   * Returns current query state.
   *
   * @return {@link ESkQueryState} - the query state
   */
  public ESkQueryState state() {
    return state;
  }

  /**
   * Возвращает аргументы запроса
   *
   * @return {@link IStringMap}&lt;{@link IDtoQueryParam}&gt; аргументы запроса.
   * @throws TsIllegalStateRtException запрос не подготовлен
   */
  public IStringMap<IDtoQueryParam> args() {
    TsIllegalStateRtException.checkTrue( state == UNPREPARED );
    return args;
  }

  /**
   * Интервал запрашиваемых данных
   *
   * @return {@link IQueryInterval} интервал запрашиваемых данных.
   * @throws TsIllegalStateRtException запрос не выполняется
   */
  public IQueryInterval interval() {
    TsIllegalStateRtException.checkTrue( state == UNPREPARED || state == PREPARED );
    return interval;
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Время создания запроса
   *
   * @return long время (мсек с начала эпохи) создания запроса
   */
  public long createTime() {
    return startTime;
  }

  /**
   * Prepares query to be executed.
   *
   * @param aArgs {@link IStringMap}&lt;{@link IDtoQueryParam}&gt; - data request parameters
   */
  public void prepareQuery( IStringMap<IDtoQueryParam> aArgs ) {
    TsNullArgumentRtException.checkNull( aArgs );
    checkInvalidState( this, EXECUTING, CLOSED );
    args = aArgs;
    state = PREPARED;
  }

  /**
   * Queries the data for the specified time interval.
   * <p>
   * Mathod shall not ba called when {@link #state()} is {@link ESkQueryState#UNPREPARED},
   * {@link ESkQueryState#EXECUTING} or {@link ESkQueryState#CLOSED}.
   *
   * @param aInterval {@link IQueryInterval} - asked interval of time
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalStateRtException illegal {@link #state()}
   */
  public void exec( IQueryInterval aInterval ) {
    TsNullArgumentRtException.checkNull( aInterval );
    checkInvalidState( this, UNPREPARED, EXECUTING, CLOSED );
    state = EXECUTING;
    startTime = System.currentTimeMillis();
    interval = aInterval;
  }

  /**
   * Завершение выполнения запроса
   *
   * @param aSuccess boolean <b>true</b> запрос успешно завершен; <b>false</b> ошибка выполнения запроса
   */
  public void execFinished( boolean aSuccess ) {
    checkInvalidState( this, UNPREPARED, PREPARED, READY, FAILED, CLOSED );
    state = (aSuccess ? READY : FAILED);
  }

  /**
   * Cancel the executng query.
   * <p>
   * Has no effect is query is not in state of execution.
   */
  public void cancel() {
    if( state == CLOSED ) {
      throw new TsIllegalArgumentRtException( FMT_ERR_QUERY_INVALID_STATE, this, state );
    }
    if( state != EXECUTING ) {
      return;
    }
    state = PREPARED;
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
  }

  // ------------------------------------------------------------------------------------
  // private implementation
  //
  private static void checkInvalidState( S5BaQueriesConvoy aQuery, ESkQueryState... aInvalidStates ) {
    TsNullArgumentRtException.checkNulls( aQuery, aInvalidStates );
    ESkQueryState currState = aQuery.state();
    for( ESkQueryState state : aInvalidStates ) {
      if( currState == state ) {
        throw new TsIllegalArgumentRtException( FMT_ERR_QUERY_INVALID_STATE, aQuery, currState );
      }
    }
  }
}
