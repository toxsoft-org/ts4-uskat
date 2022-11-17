package org.toxsoft.uskat.s5.server.sessions.pas;

import static java.lang.String.*;
import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.server.sessions.S5SessionUtils.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.IS5Resources.*;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.client.remote.connection.pas.S5CallbackOnBackendMessage;
import org.toxsoft.uskat.s5.common.sessions.IS5SessionInfo;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.frontend.S5FrontendData;
import org.toxsoft.uskat.s5.server.sessions.*;
import org.toxsoft.uskat.s5.server.statistics.IS5StatisticCounter;

/**
 * Механизм приема/передачи сообщений от клиента работающий в рамках его сессии на сервере
 *
 * @author mvk
 */
public class S5SessionMessenger
    implements IS5FrontendRear, ICooperativeMultiTaskable, ICloseable {

  /**
   * Формат текстового представления
   */
  private static final String TO_STRING_FORMAT = "%s@%s[%s]"; //$NON-NLS-1$

  private final IS5BackendCoreSingleton backendCoreSingleton;
  private volatile S5SessionData        sessionData;
  private S5SessionCallbackChannel      channel;
  private final GtMessageEventer        eventer = new GtMessageEventer();
  private final ILogger                 logger  = getLogger( getClass() );

  /**
   * Менеджер сессий
   * <p>
   * Устанавливается через lazy-загрузку {@link #sessionManager()} . Это связано с тем, что конструктор
   * приемопередатчика может (когда уже есть открытые сессии) быть использан из {@link S5BackendCoreSingleton#doInit} и
   * обращение "назад" к backendCoreSingleton вызовет ошибку рекурсивного доступа
   */
  private volatile IS5SessionManager sessionManager;

  /**
   * Статистика работы
   */
  private volatile IS5StatisticCounter statistics;

  /**
   * Конструктор
   *
   * @param aBackendCoreSingleton {@link IS5BackendCoreSingleton} backendCoreSingleton сервера
   * @param aSession {@link S5SessionData} сессия
   * @param aChannel {@link S5SessionCallbackChannel} канал обмена данными
   * @throws TsNullArgumentRtException любой аругмент = null
   */
  public S5SessionMessenger( IS5BackendCoreSingleton aBackendCoreSingleton, S5SessionData aSession,
      S5SessionCallbackChannel aChannel ) {
    TsNullArgumentRtException.checkNulls( aBackendCoreSingleton, aSession, aChannel );

    // // TODO:
    // // http port
    // Object port = ManagementFactory.getPlatformMBeanServer()
    // .getAttribute( new ObjectName( "jboss.as:socket-binding-group=standard-sockets,socket-binding=http" ), "port" );
    //
    // // http adress
    // Object addr = ManagementFactory.getPlatformMBeanServer()
    // .getAttribute( new ObjectName( "jboss.as:interface=public" ), "inet-address" );

    // // Имя узла кластера
    // String node = System.getProperty( JBOSS_NODE_NAME );

    backendCoreSingleton = aBackendCoreSingleton;
    sessionData = aSession;
    setChannel( aChannel );

    IAtomicValue remoteAddress = avStr( sessionData.info().remoteAddress() );
    IAtomicValue remotePort = avInt( sessionData.info().remotePort() );
    // Создание писателя обратных вызовов
    logger.info( MSG_CREATE_CALLBACK_WRITER, remoteAddress, remotePort );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает данные сессии передатичика
   *
   * @return {@link S5SessionData} сессия
   */
  public S5SessionData sessionData() {
    return sessionData;
  }

  /**
   * Установить канал передачи для писателя
   * <p>
   * Метод используется {@link S5SessionCallbackServer#onOpenChannel(S5SessionCallbackChannel, Skid)} при определении
   * факта появления дублей каналов от одного и того же клиента.
   *
   * @param aChannel {@link S5SessionCallbackChannel} новый канал писателя
   * @return {@link S5SessionCallbackChannel} старый канал писателя. null: канал не изменился
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5SessionCallbackChannel setChannel( S5SessionCallbackChannel aChannel ) {
    TsNullArgumentRtException.checkNull( aChannel );
    // 2022-09-04 mvk
    // if( aChannel.equals( channel ) ) {
    if( aChannel == channel ) {
      // Канал не изменился
      return null;
    }
    S5SessionCallbackChannel retValue = channel;
    channel = aChannel;

    // Регистрация слушателя сообщений от фронтенда
    channel.registerNotificationHandler( S5CallbackOnFrontendMessage.ON_MESSAGE_METHOD,
        new S5CallbackOnFrontendMessage() {

          @Override
          protected void onFrontendMessage( GtMessage aMessage ) {
            logger.info( "onFrontendMessage recevied: %s", aMessage ); //$NON-NLS-1$
            S5SessionInfo.onReceviedEvent( statistics() );
            eventer.sendMessage( aMessage );
          }
        } );

    return retValue;
  }

  /**
   * Обновление данных сессии
   *
   * @param aSessionData {@link S5SessionData} сессия у которой изменились данных
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void updateSessionData( S5SessionData aSessionData ) {
    TsNullArgumentRtException.checkNull( aSessionData );
    sessionData = aSessionData;
  }

  // ------------------------------------------------------------------------------------
  // IS5FrontendRear
  //
  @Override
  public Skid sessionID() {
    return sessionData.info().sessionID();
  }

  @Override
  public boolean isLocal() {
    return false;
  }

  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    TsNullArgumentRtException.checkNull( aMessage );
    try {
      if( channel.isRunning() ) {
        logger.info( "onBackendMessage sended: %s", aMessage ); //$NON-NLS-1$
        S5CallbackOnBackendMessage.send( channel, aMessage );
        S5SessionInfo.onSendEvent( statistics() );
      }
      else {
        // Канал находится не в рабочем состоянии
        logger.error( "onMessage(...): pas channel.isRunning() = false, sessionID = %s. close remote connection", //$NON-NLS-1$
            sessionID() );
        sessionManager().closeRemoteSession( sessionID() );
      }
    }
    catch( Throwable e ) {
      // Обработка ошибки записи обратного вызова
      handleWriteError( this, e, logger );
      // Попытка записать информацию в статистику о сессии
      try {
        S5SessionInfo.onErrorEvent( statistics() );
      }
      catch( Throwable e2 ) {
        logger.error( e2 );
      }
    }
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public S5FrontendData frontendData() {
    return sessionData.frontendData();
  }

  @Override
  public IGtMessageEventer gtMessageEventer() {
    return eventer;
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //
  @Override
  public void doJob() {
    if( !channel.isRunning() ) {
      logger.error( "doJob(...): pas channel.isRunning() = false, sessionID = %s. close remote connection", //$NON-NLS-1$
          sessionID() );
      sessionManager().closeRemoteSession( sessionID() );
      return;
    }
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //
  @Override
  public void close() {
    // Отключение от backendCoreSingleton
    backendCoreSingleton.detachFrontend( this );
    // Завершение работы канала передачи
    channel.close();
  }

  // ------------------------------------------------------------------------------------
  // Object
  //
  @Override
  public String toString() {
    IS5SessionInfo info = sessionData.info();
    return format( TO_STRING_FORMAT, info.login(), info.remoteAddress(), sessionIDToString( info.sessionID(), true ) );
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + sessionData.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object aObject ) {
    if( this == aObject ) {
      return true;
    }
    if( aObject == null ) {
      return false;
    }
    if( getClass() != aObject.getClass() ) {
      return false;
    }
    S5SessionMessenger other = (S5SessionMessenger)aObject;
    if( !sessionData.equals( other.sessionData ) ) {
      return false;
    }
    return true;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает менеджер сессий s5-сервера
   *
   * @return {@link IS5SessionManager} менеджер сессий
   */
  private IS5SessionManager sessionManager() {
    if( sessionManager == null ) {
      sessionManager = backendCoreSingleton.sessionManager();
    }
    return sessionManager;
  }

  /**
   * Возвращает статистику работы
   *
   * @return {@link IS5StatisticCounter} счетчик статистической информации
   */
  private IS5StatisticCounter statistics() {
    if( statistics == null || statistics.isClosed() ) {
      statistics = sessionManager().findStatisticCounter( sessionID() );
    }
    return statistics;
  }

  /**
   * Возвращает backendCoreSingleton системного описания
   *
   * @return {@link IS5BackendSysDescrSingleton} backendCoreSingleton системного описания
   */
  // private IS5BackendSysDescrSingleton sysdescrBackend() {
  // if( sysdescrBackend == null ) {
  // sysdescrBackend = backendCoreSingleton.sysdescr();
  // }
  // return sysdescrBackend;
  // }

  /**
   * Обработка ошибки записи обратного вызова
   *
   * @param aWriter {@link S5SessionMessenger} писатель обратных вызовов
   * @param aError {@link Throwable} произошедшая ошибка
   * @param aLogger {@link ILogger} журнал работы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void handleWriteError( S5SessionMessenger aWriter, Throwable aError, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aWriter, aError, aLogger );
    aLogger.error( aError );
    try {
      // Любые ошибки записи вызывают завершение соединения с клиентом
      aWriter.sessionManager().closeRemoteSession( aWriter.sessionID() );
    }
    catch( Throwable e2 ) {
      aLogger.error( e2 );
    }
  }
}
