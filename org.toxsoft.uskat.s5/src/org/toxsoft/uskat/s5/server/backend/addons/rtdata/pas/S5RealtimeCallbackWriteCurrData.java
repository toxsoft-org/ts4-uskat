package org.toxsoft.uskat.s5.server.backend.addons.rtdata.pas;

import static org.toxsoft.core.pas.tj.impl.TjUtils.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.server.backend.addons.rtdata.pas.IS5Resources.*;

import org.toxsoft.core.log4j.LoggerWrapper;
import org.toxsoft.core.pas.common.IPasTxChannel;
import org.toxsoft.core.pas.common.PasChannel;
import org.toxsoft.core.pas.json.IJSONNotification;
import org.toxsoft.core.pas.json.IJSONNotificationHandler;
import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.common.sessions.ISkSession;
import org.toxsoft.uskat.s5.legacy.SkCurrDataValues;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.IS5BackendCurrDataSingleton;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionManager;
import org.toxsoft.uskat.s5.server.statistics.IS5StatisticCounter;

/**
 * Вызов клиента: передача текущих данных для записи на сервере
 *
 * @author mvk
 */
public final class S5RealtimeCallbackWriteCurrData
    implements IJSONNotificationHandler<PasChannel> {

  /**
   * Вызов метода: {@link IS5BackendCurrDataSingleton#writeValues(IMap)}
   */
  public static final String WRITE_CURRDATA_METHOD = "writeCurrDataValues"; //$NON-NLS-1$

  /**
   * Текущие данных {@link SkCurrDataValues}
   */
  private static final String CURRDATA_ID = "currdata"; //$NON-NLS-1$

  /**
   * Таймут проверки счетчика статистики
   */
  private static final long STATISTIC_COUNTER_CHECK_TIMEOUT = 10000; // $NON-NLS-1$

  /**
   * Поддержка сервера записи текущих данных
   */
  private final IS5BackendCurrDataSingleton currDataSupport;

  /**
   * Сессия пользователя
   */
  private final Skid sessionID;

  /**
   * Менеджер сессии
   */
  private final IS5SessionManager sessionManager;

  /**
   * Счетчик статистической информации сессии
   */
  private IS5StatisticCounter statisticCounter;

  /**
   * Журнал
   */
  private final ILogger logger = LoggerWrapper.getLogger( getClass() );

  /**
   * Конструктор
   *
   * @param aRtdataSingleton {@link IS5BackendCurrDataSingleton} бекенд поддержки данных реального времени
   * @param aSessionManager {@link IS5SessionManager} менеджер сессии
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession#skid()}
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5RealtimeCallbackWriteCurrData( IS5BackendCurrDataSingleton aRtdataSingleton,
      IS5SessionManager aSessionManager, Skid aSessionID ) {
    TsNullArgumentRtException.checkNulls( aRtdataSingleton, aSessionManager, aSessionID );
    currDataSupport = aRtdataSingleton;
    sessionManager = aSessionManager;
    sessionID = aSessionID;
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Передача по каналу вызова {@link IS5BackendCurrDataSingleton#writeValues(IMap)}
   *
   * @param aChannel {@link IPasTxChannel} канал передачи
   * @param aCurrData {@link SkCurrDataValues} текущие данные
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void send( IPasTxChannel aChannel, SkCurrDataValues aCurrData ) {
    TsNullArgumentRtException.checkNulls( aChannel, aCurrData );
    // Формирование параметров
    IStringMapEdit<ITjValue> notifyParams = new StringMap<>();
    notifyParams.put( CURRDATA_ID, createString( SkCurrDataValues.KEEPER.ent2str( aCurrData ) ) );
    aChannel.sendNotification( WRITE_CURRDATA_METHOD, notifyParams );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IJSONNotificationHandler
  //
  @Override
  public void notify( PasChannel aChannel, IJSONNotification aNotification ) {
    TsNullArgumentRtException.checkNull( aNotification );
    if( !aNotification.method().equals( WRITE_CURRDATA_METHOD ) ) {
      // Уведомление игнорировано
      return;
    }
    // if( aChannel.getRemoteAddress().getHostAddress().equals( "10.150.0.22" ) ) {
    // System.err.println( "S5RealtimeCallbackWriteCurrData. name : " + aChannel.getRemoteAddress() );
    // }
    // TODO: WORKAROUND при массовом подключении клиенту к серверу у некоторых счетчик невалидный
    long currTime = System.currentTimeMillis();
    if( statisticCounter == null ) {
      // Инициализация счетчика статистики в приемнике текущих данных сессии
      statisticCounter = sessionManager.findStatisticCounter( sessionID );
      if( statisticCounter == null ) {
        logger.error( MSG_INIT_STATISTIC_COUNTER, sessionID );
        aChannel.close();
        return;
      }
      if( statisticCounter != null ) {
        logger.info( MSG_INIT_STATISTIC_COUNTER, sessionID );
      }
    }
    if( statisticCounter != null && currTime - statisticCounter.updateTime() > STATISTIC_COUNTER_CHECK_TIMEOUT ) {
      statisticCounter = sessionManager.findStatisticCounter( sessionID );
      // Невалидная статистика приемника текущих данных сессии
      logger.error( ERR_INVALID_STATISTIC_COUNTER, sessionID );
    }
    if( statisticCounter != null ) {
      statisticCounter.onEvent( IS5ServerHardConstants.STAT_SESSION_RECEVIED_CURRDATA, AV_1 );
    }
    SkCurrDataValues currdata = SkCurrDataValues.KEEPER.str2ent( aNotification.params().get( CURRDATA_ID ).asString() );
    currDataSupport.writeCurrData( currdata );
  }
}
