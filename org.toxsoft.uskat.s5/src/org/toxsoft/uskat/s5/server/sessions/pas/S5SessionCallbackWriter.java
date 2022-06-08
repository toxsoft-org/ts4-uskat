package org.toxsoft.uskat.s5.server.sessions.pas;

import static java.lang.String.*;
import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.client.IS5ConnectionParams.*;
import static org.toxsoft.uskat.s5.server.sessions.S5SessionUtils.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;
import static ru.uskat.backend.messages.SkMessageWhenCommandsStateChanged.*;
import static ru.uskat.backend.messages.SkMessageWhenCurrdataChanged.*;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.toxsoft.core.pas.common.PasChannel;
import org.toxsoft.core.pas.json.IJSONNotificationHandler;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.client.remote.connection.pas.S5CallbackOnMessage;
import org.toxsoft.uskat.s5.common.sessions.IS5SessionInfo;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.frontend.S5FrontendData;
import org.toxsoft.uskat.s5.server.sessions.*;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

import ru.uskat.common.dpu.rt.events.SkCurrDataValues;

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

  private final IS5BackendCoreSingleton backend;
  private final ReentrantReadWriteLock  lock               = new ReentrantReadWriteLock();
  private volatile S5RemoteSession      session;
  private final int                     currdataTimeout;
  private final SkCurrDataValues        currdataValues     = new SkCurrDataValues();
  private final S5Lockable              currdataValuesLock = new S5Lockable();
  private long                          lastCurrdataTime   = System.currentTimeMillis();
  private S5SessionCallbackChannel      channel;
  private final ILogger                 logger             = getLogger( getClass() );

  /**
   * Менеджер сессий
   * <p>
   * Устанавливается через lazy-загрузку {@link #sessionManager()} . Это связано с тем, что конструктор писателя может
   * (когда уже есть открытые сессии) быть использан из {@link S5BackendCoreSingleton#doInit} и обращение "назад" к
   * backend вызовет ошибку рекурсивного доступа
   */
  private volatile IS5SessionManager sessionManager;

  /**
   * Конструктор
   *
   * @param aBackend {@link IS5BackendCoreSingleton} backend сервера
   * @param aSession {@link S5RemoteSession} сессия
   * @param aChannel {@link S5SessionCallbackChannel} канал обмена данными
   * @throws TsNullArgumentRtException любой аругмент = null
   */
  public S5SessionCallbackWriter( IS5BackendCoreSingleton aBackend, S5RemoteSession aSession,
      S5SessionCallbackChannel aChannel ) {
    TsNullArgumentRtException.checkNulls( aBackend, aSession, aChannel );

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

    backend = aBackend;
    session = aSession;
    channel = aChannel;
    // Подключение к backend
    // 2021-02-13 mvk перемещено в S5SessionManager
    // backend.attachFrontend( this );
    // Опции клиента
    IOptionSet options = session.info().clientOptions();
    currdataTimeout = OP_CURRDATA_TIMEOUT.getValue( options ).asInt();
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
  public void onGenericMessage( GenericMessage aMessage ) {
    TsNullArgumentRtException.checkNull( aMessage );
    if( WHEN_COMMANDS_STATE_CHANGED.equals( aMessage.messageId() ) ) {
      // Мог изменится список выполняемых команд. Обновим данные сессии (возможно избыточно, неконтролируем)
      sessionManager().updateRemoteSession( session );
    }
    if( WHEN_CURRDATA_CHANGED.equals( aMessage.messageId() ) && //
        currdataTimeout > 0 ) {
      // Текущие данные передаются по таймауту текущих данных (буферизация)
      lockRead( currdataValuesLock );
      try {
        currdataValues.putAll( aMessage.args().getValobj( ARG_VALUES ) );
      }
      finally {
        unlockRead( currdataValuesLock );
      }
      return;
    }
    try {
      // 2020-05-22 mvk
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
  public ReentrantReadWriteLock mainLock() {
    return lock;
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
    long currTime = System.currentTimeMillis();
    // Отправка значений текущих данных по таймауту
    if( currdataTimeout > 0 && currTime - lastCurrdataTime > currdataTimeout ) {
      lockRead( currdataValuesLock );
      try {
        try {
          if( currdataValues.size() > 0 ) {
            IOptionSetEdit args = new OptionSet();
            args.setValobj( ARG_VALUES, currdataValues );
            // 2020-05-22 mvk
            if( channel.isRunning() ) {
              S5CallbackOnMessage.send( channel, new GenericMessage( WHEN_CURRDATA_CHANGED, args ) );
            }
            else {
              // Канал находится не в рабочем состоянии
              logger.error( "doJob(...): pas channel.isRunning() = false, sessionID = %s. close remote connection", //$NON-NLS-1$
                  sessionID() );
              sessionManager().closeRemoteSession( sessionID() );
            }
            currdataValues.clear();
          }
          lastCurrdataTime = currTime;
        }
        catch( Throwable e ) {
          // Обработка ошибки записи обратного вызова
          handleWriteError( this, e, logger );
        }
      }
      finally {
        unlockRead( currdataValuesLock );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //
  @Override
  public void close() {
    // Отключение от backend
    backend.detachFrontend( this );
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
      sessionManager = backend.sessionManager();
    }
    return sessionManager;
  }

  /**
   * Возвращает backend системного описания
   *
   * @return {@link IS5BackendSysDescrSingleton} backend системного описания
   */
  // private IS5BackendSysDescrSingleton sysdescrBackend() {
  // if( sysdescrBackend == null ) {
  // sysdescrBackend = backend.sysdescr();
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
