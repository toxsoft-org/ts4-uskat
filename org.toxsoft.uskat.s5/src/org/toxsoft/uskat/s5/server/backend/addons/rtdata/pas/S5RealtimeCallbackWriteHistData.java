package ru.uskat.s5.server.backend.addons.realtime.pas;

import static ru.toxsoft.log4j.Logger.*;
import static ru.toxsoft.tslib.datavalue.impl.DvUtils.*;
import static ru.toxsoft.tslib.pas.tj.impl.TjUtils.*;
import static ru.uskat.s5.server.backend.addons.realtime.S5RealtimeUtils.*;
import static ru.uskat.s5.server.backend.addons.realtime.pas.IS5Resources.*;

import ru.toxsoft.tslib.error.TsNullArgumentRtException;
import ru.toxsoft.tslib.greenworld.skid.Skid;
import ru.toxsoft.tslib.pas.common.IPasTxChannel;
import ru.toxsoft.tslib.pas.common.PasChannel;
import ru.toxsoft.tslib.pas.json.IJSONNotification;
import ru.toxsoft.tslib.pas.json.IJSONNotificationHandler;
import ru.toxsoft.tslib.pas.tj.ITjValue;
import ru.toxsoft.tslib.utils.collections.IStringMapEdit;
import ru.toxsoft.tslib.utils.collections.impl.StringMap;
import ru.toxsoft.tslib.utils.logs.ILogger;
import ru.uskat.common.dpu.rt.events.DpuWriteHistData;
import ru.uskat.core.api.users.ISkSession;
import ru.uskat.s5.server.IS5ServerHardConstants;
import ru.uskat.s5.server.backend.IS5BackendLocal;
import ru.uskat.s5.server.backend.supports.histdata.IS5BackendHistDataSingleton;
import ru.uskat.s5.server.sessions.IS5SessionManager;
import ru.uskat.s5.server.statistics.IS5StatisticCounter;

/**
 * Вызов клиента: передача хранимых данных для записи на сервере
 *
 * @author mvk
 */
public final class S5RealtimeCallbackWriteHistData
    implements IJSONNotificationHandler<PasChannel> {

  /**
   * Вызов метода: {@link IS5BackendHistDataSingleton#writeHistData(DpuWriteHistData)}
   */
  public static final String WRITE_HISTDATA_METHOD = REALTIME_METHOD_PREFIX + "writeHistData"; //$NON-NLS-1$

  /**
   * Хранимые данные {@link DpuWriteHistData}
   */
  private static final String HISTDATA_ID = "histdata"; //$NON-NLS-1$

  /**
   * Таймут проверки счетчика статистики
   */
  private static final long STATISTIC_COUNTER_CHECK_TIMEOUT = 10000; // $NON-NLS-1$

  /**
   * Бекенд поддержки данных реального времени: хранимые данные
   */
  private final IS5BackendHistDataSingleton histdataSingleton;

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
  private final ILogger logger = getLogger( getClass() );

  /**
   * Конструктор
   *
   * @param aHistDataSingleton {@link IS5BackendHistDataSingleton} бекенд поддержки хранимых данных реального времени
   * @param aSessionManager {@link IS5SessionManager} менеджер сессии
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession#skid()}
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5RealtimeCallbackWriteHistData( IS5BackendHistDataSingleton aHistDataSingleton,
      IS5SessionManager aSessionManager, Skid aSessionID ) {
    TsNullArgumentRtException.checkNulls( aHistDataSingleton, aSessionManager, aSessionID );
    histdataSingleton = aHistDataSingleton;
    sessionManager = aSessionManager;
    sessionID = aSessionID;
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Передача по каналу вызова {@link IS5BackendLocal#verify()}
   *
   * @param aChannel {@link IPasTxChannel} канал передачи
   * @param aHistData {@link DpuWriteHistData} значения хранимых данных для записи
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void send( IPasTxChannel aChannel, DpuWriteHistData aHistData ) {
    TsNullArgumentRtException.checkNulls( aChannel, aHistData );

    // Формирование параметров
    IStringMapEdit<ITjValue> notifyParams = new StringMap<>();
    notifyParams.put( HISTDATA_ID, createString( DpuWriteHistData.KEEPER.toStr( aHistData ) ) );
    aChannel.sendNotification( WRITE_HISTDATA_METHOD, notifyParams );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IJSONNotificationHandler
  //
  @Override
  public void notify( PasChannel aChannel, IJSONNotification aNotification ) {
    TsNullArgumentRtException.checkNull( aNotification );
    if( aNotification.method().equals( WRITE_HISTDATA_METHOD ) == false ) {
      // Уведомление игнорировано
      return;
    }
    // if( aChannel.getRemoteAddress().getHostAddress().equals( "10.150.0.22" ) ) {
    // System.err.println( "S5RealtimeCallbackWriteHistData. name : " + aChannel.getRemoteAddress() );
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
      statisticCounter.onEvent( IS5ServerHardConstants.STAT_SESSION_RECEVIED_HISTDATA, DV_1 );
    }
    DpuWriteHistData histdata = DpuWriteHistData.KEEPER.fromStr( aNotification.params().get( HISTDATA_ID ).asString() );
    histdataSingleton.writeHistData( histdata );
  }
}
