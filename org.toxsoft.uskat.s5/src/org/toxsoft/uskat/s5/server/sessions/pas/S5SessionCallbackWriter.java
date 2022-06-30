package org.toxsoft.uskat.s5.server.sessions.pas;

import static java.lang.String.*;
import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.server.sessions.S5SessionUtils.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.IS5Resources.*;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.toxsoft.core.pas.common.PasChannel;
import org.toxsoft.core.pas.json.IJSONNotificationHandler;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.client.remote.connection.pas.S5CallbackOnMessage;
import org.toxsoft.uskat.s5.common.sessions.IS5SessionInfo;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonCreator;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.frontend.S5FrontendData;
import org.toxsoft.uskat.s5.server.sessions.*;

/**
 * Передача обратных вызовов от сервера к клиенту
 *
 * @author mvk
 */
public class S5SessionCallbackWriter
    implements IS5FrontendRear, ICooperativeMultiTaskable, ICloseable {

  /**
   * Формат текстового представления
   */
  private static final String TO_STRING_FORMAT = "%s@%s[%s]"; //$NON-NLS-1$

  private final IS5BackendCoreSingleton    backendCoreSingleton;
  private volatile S5RemoteSession         session;
  private S5SessionCallbackChannel         channel;
  private final IList<IS5MessageProcessor> messageProcessors;
  private final ILogger                    logger = getLogger( getClass() );

  /**
   * Менеджер сессий
   * <p>
   * Устанавливается через lazy-загрузку {@link #sessionManager()} . Это связано с тем, что конструктор писателя может
   * (когда уже есть открытые сессии) быть использан из {@link S5BackendCoreSingleton#doInit} и обращение "назад" к
   * backendCoreSingleton вызовет ошибку рекурсивного доступа
   */
  private volatile IS5SessionManager sessionManager;

  /**
   * Конструктор
   *
   * @param aBackendCoreSingleton {@link IS5BackendCoreSingleton} backendCoreSingleton сервера
   * @param aSession {@link S5RemoteSession} сессия
   * @param aChannel {@link S5SessionCallbackChannel} канал обмена данными
   * @throws TsNullArgumentRtException любой аругмент = null
   */
  public S5SessionCallbackWriter( IS5BackendCoreSingleton aBackendCoreSingleton, S5RemoteSession aSession,
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
    session = aSession;
    channel = aChannel;

    // Получение процессоров сообщений от расширений бекенда
    IListEdit<IS5MessageProcessor> mp = new ElemArrayList<>();
    for( IS5BackendAddonCreator baCreator : backendCoreSingleton.initialConfig().impl().baCreators() ) {
      IS5MessageProcessor processor = baCreator.messageProcessor();
      if( processor != IS5MessageProcessor.NULL ) {
        mp.add( processor );
      }
    }
    messageProcessors = mp;

    IAtomicValue remoteAddress = avStr( session.info().remoteAddress() );
    IAtomicValue remotePort = avInt( session.info().remotePort() );
    // Создание писателя обратных вызовов
    logger.info( MSG_CREATE_CALLBACK_WRITER, remoteAddress, remotePort );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает сессию передатичика
   *
   * @return {@link S5RemoteSession} сессия
   */
  public S5RemoteSession session() {
    return session;
  }

  /**
   * Установить новый канал передачи для писателя
   * <p>
   * Метод используется {@link S5SessionCallbackServer#onOpenChannel(S5SessionCallbackChannel, Skid)} при определении
   * факта появления дублей каналов от одного и того же клиента.
   *
   * @param aChannel {@link S5SessionCallbackChannel} новый канал писателя
   * @return {@link S5SessionCallbackChannel} старый канал писателя. null: канал не изменился
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5SessionCallbackChannel setNewChannel( S5SessionCallbackChannel aChannel ) {
    TsNullArgumentRtException.checkNull( aChannel );
    if( aChannel.equals( channel ) ) {
      // Канал не изменился
      return null;
    }
    S5SessionCallbackChannel retValue = channel;
    channel = aChannel;
    return retValue;
  }

  /**
   * Обновление данных сессии
   *
   * @param aSession {@link S5RemoteSession} сессия у которой изменились данных
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void updateSession( S5RemoteSession aSession ) {
    TsNullArgumentRtException.checkNull( aSession );
    session = aSession;
  }

  /**
   * Регистрация обработчика уведомления
   *
   * @param aMethodName String имя метода уведомления
   * @param aHandler {@link IJSONNotificationHandler}
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public void registerNotificationHandler( String aMethodName, IJSONNotificationHandler<PasChannel> aHandler ) {
    TsNullArgumentRtException.checkNull( aMethodName );
    TsNullArgumentRtException.checkNull( aHandler );
    channel.registerNotificationHandler( aMethodName, aHandler );
  }

  // ------------------------------------------------------------------------------------
  // IS5FrontendRear
  //
  @Override
  public Skid sessionID() {
    return session.info().sessionID();
  }

  @Override
  public boolean isLocal() {
    return false;
  }

  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    TsNullArgumentRtException.checkNull( aMessage );
    // Обработка сообщений процессором
    for( IS5MessageProcessor processor : messageProcessors ) {
      if( processor.processMessage( aMessage ) ) {
        // Сообщение обработанно процессором
        return;
      }
    }
    try {
      if( channel.isRunning() ) {
        S5CallbackOnMessage.send( channel, aMessage );
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
        S5SessionInfo.onErrorEvent( sessionManager(), session().info().sessionID() );
      }
      catch( Throwable e2 ) {
        logger.error( e2 );
      }
    }
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public S5FrontendData frontendData() {
    return session.frontendData();
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
    // Передача накопленных сообщений
    for( IS5MessageProcessor processor : messageProcessors ) {
      GtMessage message = processor.getHeadOrNull();
      while( message != null ) {
        S5CallbackOnMessage.send( channel, message );
        message = processor.getHeadOrNull();
      }
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
    IS5SessionInfo info = session.info();
    return format( TO_STRING_FORMAT, info.login(), info.remoteAddress(), sessionIDToString( info.sessionID(), true ) );
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + session.hashCode();
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
    S5SessionCallbackWriter other = (S5SessionCallbackWriter)aObject;
    if( !session.equals( other.session ) ) {
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
   * @param aWriter {@link S5SessionCallbackWriter} писатель обратных вызовов
   * @param aError {@link Throwable} произошедшая ошибка
   * @param aLogger {@link ILogger} журнал работы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void handleWriteError( S5SessionCallbackWriter aWriter, Throwable aError, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aWriter, aError, aLogger );
    try {
      // Любые ошибки записи вызывают завершение соединения с клиентом
      aWriter.sessionManager().closeRemoteSession( aWriter.sessionID() );
    }
    catch( @SuppressWarnings( "unused" ) Throwable e2 ) {
      aLogger.error( aError );
    }
  }
}
