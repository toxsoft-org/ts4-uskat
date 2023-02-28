package org.toxsoft.uskat.s5.server.backend.addons.queries;

import static org.toxsoft.uskat.s5.server.backend.addons.queries.ES5QueriesConvoyState.*;
import static org.toxsoft.uskat.s5.server.backend.addons.queries.IS5Resources.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.strid.impl.StridableParameterizedSer;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.hqserv.IDtoQueryParam;
import org.toxsoft.uskat.core.api.hqserv.ISkAsynchronousQuery;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;

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

  private final IS5FrontendRear      frontend;
  private ES5QueriesConvoyState      state        = UNPREPARED;
  private String                     stateMessage = TsLibUtils.EMPTY_STRING;
  private String                     cancelAuthor = TsLibUtils.EMPTY_STRING;
  private IQueryInterval             interval;
  private IStringMap<IDtoQueryParam> args;

  private long startTime = System.currentTimeMillis();

  /**
   * Конструктор
   *
   * @param aFrontend {@link IS5FrontendRear} the frontend, the owner of the query.
   * @param aQueryId String идентификатор запроса {@link ISkAsynchronousQuery#queryId()}.
   * @param aParams {@link IOptionSet} параметры запроса {@link ISkAsynchronousQuery#params()}.
   * @throws TsNullArgumentRtException любой аргумент = null.
   */
  public S5BaQueriesConvoy( IS5FrontendRear aFrontend, String aQueryId, IOptionSet aParams ) {
    super( aQueryId, aParams );
    frontend = TsNullArgumentRtException.checkNull( aFrontend );
  }

  /**
   * Returns the frontend, the owner of the query.
   *
   * @return {@link IS5FrontendRear} query owner.
   */
  public IS5FrontendRear frontend() {
    return frontend;
  }

  /**
   * Returns current query state.
   *
   * @return {@link ES5QueriesConvoyState} - the query state
   */
  public ES5QueriesConvoyState state() {
    return state;
  }

  /**
   * Returns current query state message.
   *
   * @return String - the query state message. empty string - no messages
   */
  public String stateMessage() {
    return stateMessage;
  }

  /**
   * Set curretn query state message
   *
   * @param aStateMessage String - the query state message. empty string - no messages
   */
  public void setStateMessage( String aStateMessage ) {
    TsNullArgumentRtException.checkNull( aStateMessage );
    stateMessage = aStateMessage;
  }

  /**
   * Returns request canceler.
   *
   * @return String - the request canceler. empty string - no cancel
   */
  public String cancelAuthor() {
    return cancelAuthor;
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
   * Mathod shall not ba called when {@link #state()} is {@link ES5QueriesConvoyState#UNPREPARED},
   * {@link ES5QueriesConvoyState#EXECUTING} or {@link ES5QueriesConvoyState#CLOSED}.
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
    if( state != EXECUTING ) {
      return;
    }
    state = (aSuccess ? READY : FAILED);
    if( aSuccess ) {
      stateMessage = MSG_REQUEST_COMPLETED;
    }
  }

  /**
   * Cancel the executng query.
   * <p>
   * Has no effect is query is not in state of execution.
   *
   * @param aAuthor String author of query cancel
   * @throws TsNullArgumentRtException arg = null
   */
  public void cancel( String aAuthor ) {
    TsNullArgumentRtException.checkNull( aAuthor );
    cancelAuthor = aAuthor;
    if( state == CLOSED ) {
      return;
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
    cancel( TsLibUtils.EMPTY_STRING );
    state = CLOSED;
  }

  // ------------------------------------------------------------------------------------
  // private implementation
  //
  private static void checkInvalidState( S5BaQueriesConvoy aQuery, ES5QueriesConvoyState... aInvalidStates ) {
    TsNullArgumentRtException.checkNulls( aQuery, aInvalidStates );
    ES5QueriesConvoyState currState = aQuery.state();
    for( ES5QueriesConvoyState state : aInvalidStates ) {
      if( currState == state ) {
        throw new TsIllegalArgumentRtException( ERR_QUERY_INVALID_STATE, aQuery, currState );
      }
    }
  }
}
