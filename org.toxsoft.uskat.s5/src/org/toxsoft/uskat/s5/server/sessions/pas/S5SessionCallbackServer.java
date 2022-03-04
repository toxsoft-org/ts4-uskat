package org.toxsoft.uskat.s5.server.sessions.pas;

import static org.toxsoft.core.log4j.Logger.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackClusterTopology.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackInit.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackVerify.*;
import static org.toxsoft.uskat.s5.utils.platform.S5ServerPlatformUtils.*;

import java.util.concurrent.ExecutorService;

import org.toxsoft.core.pas.common.IPasParams;
import org.toxsoft.core.pas.server.IPasServerParams;
import org.toxsoft.core.pas.server.PasServer;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants;
import org.toxsoft.core.tslib.bricks.ctx.impl.TsContext;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.gw.skid.SkidList;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterManager;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionManager;
import org.toxsoft.uskat.s5.server.sessions.S5RemoteSession;
import org.toxsoft.uskat.s5.server.startup.IS5InitialImplementation;

import ru.uskat.core.api.users.ISkSession;

/**
 * Поставщик PAS-каналов для образования писателей обратных вызовов
 *
 * @author mvk
 */
public final class S5SessionCallbackServer
    implements ICloseable {

  /**
   * Таймаут ожидания сессии для канала
   */
  private static final long WAIT_SESSION_TIMEOUT = 10000;

  /**
   * Менеджер сессий
   */
  private final IS5SessionManager sessionManager;

  /**
   * Сервер каналов используемых передатчиками callback-сообщений
   */
  private PasServer<S5SessionCallbackChannel> pasServer;

  /**
   * Счетчик не найденных каналов
   */
  private int unprovidedCount;

  /**
   * Журнал работы
   */
  private ILogger logger = getLogger( getClass() );

  /**
   * Конструктор
   *
   * @param aInitialImplementation {@link IS5InitialImplementation} начальная, неизменяемая, проектно-зависимая
   *          конфигурация реализации бекенда сервера
   * @param aSessionManager {@link IS5SessionManager} менеджер сессий s5-сервера
   * @param aClusterManager {@link IS5ClusterManager} менеджер кластера s5-сервера
   * @param aExecutor {@link ExecutorService} исполнитель потоков (PasServer.run())
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SessionCallbackServer( IS5InitialImplementation aInitialImplementation, IS5SessionManager aSessionManager,
      IS5ClusterManager aClusterManager, ExecutorService aExecutor ) {
    TsNullArgumentRtException.checkNulls( aInitialImplementation, aSessionManager, aClusterManager, aExecutor );
    sessionManager = aSessionManager;

    String pasAddress = (String)readAttribute( JBOSS_PUBLIC_INTERFACE, JBOSS_ATTR_RESOLVED_ADDRESS );

    // Попытка чтения номера порта создания callback из параметров jvm
    Object port = System.getProperty( OP_BACKEND_CALLBACK_PORT );
    // Если порт не определен, то порт создания callback = EJB_PORT_NO + 1
    int pasPort =
        (port == null ? ((Integer)readAttribute( JBOSS_SOCKET_BINDING_HTTP, JBOSS_ATTR_BOUND_PORT )).intValue() + 1
            : Integer.parseInt( (String)port ));

    // Имя узла кластера
    String node = System.getProperty( JBOSS_NODE_NAME );
    // Создание клиента передачи обратных вызовов
    TsContext ctx = new TsContext();
    ctx.put( S5SessionCallbackServer.class, this );
    ctx.put( IS5InitialImplementation.class, aInitialImplementation );
    // Адрес клиента
    IPasServerParams.OP_PAS_SERVER_ADDRESS.setValue( ctx.params(), avStr( pasAddress ) );
    IPasServerParams.OP_PAS_SERVER_PORT.setValue( ctx.params(), avInt( pasPort ) );
    // Имя PAS-сервера
    IAtomicValue serverName = avStr( node + '[' + pasAddress + ':' + pasPort + ']' );
    // Идентификация канала (узел-сессия)
    IAvMetaConstants.DDEF_NAME.setValue( ctx.params(), serverName );
    IAvMetaConstants.DDEF_DESCRIPTION.setValue( ctx.params(), serverName );
    // Таймаут отказа по факту выставляется клиентом
    IPasParams.OP_PAS_FAILURE_TIMEOUT.setValue( ctx.params(), IAtomicValue.NULL );
    // aExternalDoJobCall = false
    pasServer = new PasServer<>( ctx, S5SessionCallbackChannel.CREATOR, false, getLogger( PasServer.class ) ) {

      private int anonymousCount;
      private int duplicateCount;

      @Override
      public void doJob() {
        super.doJob();
        // Текущее время
        long currTime = System.currentTimeMillis();
        // Список сессий обработанных каналов
        SkidList sessionIds = new SkidList();
        // Проверка того, что все каналы имеют сессию
        for( S5SessionCallbackChannel channel : pasServer.channels()
            .copyTo( new ElemArrayList<S5SessionCallbackChannel>( pasServer.channels().size() ) ) ) {
          Skid sessionID = channel.getSessionID();
          if( sessionID == Skid.NONE && (currTime - channel.getCreationTimestamp() > WAIT_SESSION_TIMEOUT) ) {
            // Канал не имеет сессии и будет закрыт
            logger().error( ERR_NO_CHANNEL_SESSION, channel );
            channel.close();
            anonymousCount++;
          }
          if( sessionID != Skid.NONE ) {
            if( !sessionIds.hasElem( sessionID ) ) {
              sessionIds.add( sessionID );
              continue;
            }
            // Обнаружен дубль-канал для одной и той же сессии. Канал будет закрыт
            logger().error( ERR_FOUND_DUPLICATE_CHANNEL, sessionID, channel );
            channel.setDuplicate( true );
            channel.close();
            duplicateCount++;
          }
        }
        if( logger.isSeverityOn( ELogSeverity.DEBUG ) ) {
          // Вывод в журнал о статусе процесса doJob
          Long time = Long.valueOf( System.currentTimeMillis() - currTime );
          Integer ac = Integer.valueOf( anonymousCount );
          Integer dc = Integer.valueOf( duplicateCount );
          Integer nfc = Integer.valueOf( unprovidedCount );
          logger.debug( MSG_DOJOB_RUN, time, ac, dc, nfc );
        }
      }
    };
    // Регистрация исполнителей запросов и уведомлений
    // @formatter:off
    pasServer.registerNotificationHandler( SESSION_INIT_METHOD, new S5SessionCallbackInit( ) );
    pasServer.registerNotificationHandler( SESSION_VERIFY_METHOD, new S5SessionCallbackVerify( aSessionManager ) );
    pasServer.registerNotificationHandler( SESSION_TOPOLOGY_METHOD, new S5SessionCallbackClusterTopology( aSessionManager ) );
    // @formatter:on
    // Инициализация сервера
    pasServer.init();
    // Запуск сервера
    aExecutor.execute( pasServer );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает канал работающий в указанной сессии
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @param aRemoteAddr String адрес клиента
   * @param aRemotePort int порт клиента
   * @return {@link S5SessionCallbackChannel} PAS-канал
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5SessionCallbackChannel findSessionChannel( Skid aSessionID, String aRemoteAddr, int aRemotePort ) {
    TsNullArgumentRtException.checkNull( aSessionID, aRemoteAddr );
    // 2022-01-15 mvk
    // Из-за специфического создания соединений клиента с сервером (с начала создается pas канал, потом сессия)
    // в пуле pas-сервера может быть несколько каналов с одним и тем же идентификатором сессии. Причем, некоторые уже
    // могут быть завершены, а некоторые еще нет (гонка потоков). Считаем, актуальным только последний (по времени
    // создания)
    S5SessionCallbackChannel retValue = null;
    for( S5SessionCallbackChannel channel : pasServer.channels()
        .copyTo( new ElemArrayList<S5SessionCallbackChannel>( pasServer.channels().size() ) ) ) {
      Skid sessionID = channel.getSessionID();
      if( sessionID.equals( aSessionID ) && //
          channel.getRemoteAddress().getHostAddress().equals( aRemoteAddr ) && //
          channel.getRemotePort() == aRemotePort ) {
        if( retValue == null ) {
          retValue = channel;
          logger.info( "findSessionChannel(...): found a channel with session id = %s. time = %s (remote=%s:%d)", //$NON-NLS-1$
              aSessionID, //
              TimeUtils.timestampToString( retValue.getCreationTimestamp() ), //
              retValue.getRemoteAddress(), Integer.valueOf( retValue.getRemotePort() ) //
          );
          continue;
        }
        logger.warning(
            "findSessionChannel(...): found a channel with same session id = %s. time1 = %s (remote=%s:%d), time2 = %s (remote=%s:%d)", //$NON-NLS-1$
            aSessionID, //
            TimeUtils.timestampToString( retValue.getCreationTimestamp() ), //
            retValue.getRemoteAddress(), Integer.valueOf( retValue.getRemotePort() ), //
            TimeUtils.timestampToString( channel.getCreationTimestamp() ), //
            channel.getRemoteAddress(), Integer.valueOf( channel.getRemotePort() ) //
        );
        if( retValue.getCreationTimestamp() >= channel.getCreationTimestamp() ) {
          retValue = channel;
        }
      }
    }
    if( retValue == null ) {
      unprovidedCount++;
    }
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Возвращает менеджера сессий
   *
   * @return {@link IS5SessionManager} менеджер сессий
   */
  IS5SessionManager sessionManager() {
    return sessionManager;
  }

  /**
   * Обработка события: создан канал сессии
   *
   * @param aChannel {@link S5SessionCallbackChannel} канал соединения
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @throws TsNullArgumentRtException аргумент = null
   */
  void onOpenChannel( S5SessionCallbackChannel aChannel, Skid aSessionID ) {
    TsNullArgumentRtException.checkNulls( aChannel, aSessionID );
    S5SessionCallbackWriter callbackWriter = sessionManager.findCallbackWriter( aSessionID );
    for( S5SessionCallbackChannel channel : pasServer.channels()
        .copyTo( new ElemArrayList<S5SessionCallbackChannel>( pasServer.channels().size() ) ) ) {
      Skid sessionID = channel.getSessionID();
      if( sessionID == Skid.NONE ) {
        // Канал еще не имеет сессии
        continue;
      }
      if( !sessionID.equals( aSessionID ) ) {
        // Канал другой сессии
        continue;
      }
      if( !channel.equals( aChannel ) ) {
        // Найден дубль канал того же клиента
        logger.error( "onOpenChannel(...): close double channel = %s, new channel = %s", channel, aChannel ); //$NON-NLS-1$
        channel.close();
      }
    }
    if( callbackWriter == null ) {
      logger.info( "onOpenChannel(...): callbackWriter = null. Search exist session for %s", aSessionID ); //$NON-NLS-1$
      S5RemoteSession session = sessionManager.findSession( aSessionID );
      if( session == null ) {
        return;
      }
      logger.warning( "onOpenChannel(...): callbackWriter = null. Found session for %s. Try recreate callback writer", //$NON-NLS-1$
          aSessionID );
      // Писатель не был создан для сессии. Попытка создать писателя
      sessionManager.tryCreateCallbackWriter( session );
      return;
    }
    // У сессии уже был писатель, заменяем ему канал. Старый (если он был) закрываем
    S5SessionCallbackChannel oldChannel = callbackWriter.setNewChannel( aChannel );
    if( oldChannel != null ) {
      logger.error( "onOpenChannel(...): close double channel = %s, set new writer channel = %s", oldChannel, //$NON-NLS-1$
          aChannel );
      oldChannel.close();
    }
  }

  /**
   * Обработка события: завершение работы канала
   *
   * @param aChannel {@link S5SessionCallbackChannel} канал на котором закрыто соединение
   * @throws TsNullArgumentRtException аргумент = null
   */
  void onCloseChannel( S5SessionCallbackChannel aChannel ) {
    TsNullArgumentRtException.checkNull( aChannel );
    Skid sessionID = aChannel.getSessionID();
    if( sessionID == Skid.NONE ) {
      // Отказ завершения сессии по завершению работы канала(onCloseChannel). Канал не имеет сессии
      logger.warning( ERR_CANT_CLOSE_NULL_SESSION, aChannel );
      return;
    }
    S5RemoteSession session = sessionManager.findSession( sessionID );
    if( session == null ) {
      // Не найдена сессия разрываемого канала
      aChannel.logger().error( ERR_CLOSED_CHANNEL_SESSION_NOT_FOUND, sessionID, aChannel );
      return;
    }
    // 2020-05-22 mvk
    if( !session.info().closeByRemote() ) {
      // Обнаружен разрыв канала с клиентом. Сессия пользователя будет закрыта
      logger.error( ERR_DETECT_BREAK_CONNECTION, aChannel, session );
    }
    // Завершение работы сессии
    session.backend().removeAsync();
  }

  // ------------------------------------------------------------------------------------
  // Реализация ICloseable
  //
  @Override
  public void close() {
    pasServer.close();
  }
}
