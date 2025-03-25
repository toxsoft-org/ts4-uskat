package org.toxsoft.uskat.s5.server.sessions.pas;

import static java.lang.String.*;
import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackClusterTopology.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackInit.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackVerify.*;
import static org.toxsoft.uskat.s5.utils.platform.S5ServerPlatformUtils.*;

import java.util.concurrent.*;

import org.toxsoft.core.pas.common.*;
import org.toxsoft.core.pas.server.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.s5.server.cluster.*;
import org.toxsoft.uskat.s5.server.sessions.*;
import org.toxsoft.uskat.s5.server.startup.*;
import org.toxsoft.uskat.s5.utils.*;

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
   * Таймер статистики
   */
  private final S5IntervalTimer statisticsTimer = new S5IntervalTimer( 10000 );

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
   * @param aExecutor {@link Executor} исполнитель потоков (PasServer.run())
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SessionCallbackServer( IS5InitialImplementation aInitialImplementation, IS5SessionManager aSessionManager,
      IS5ClusterManager aClusterManager, Executor aExecutor ) {
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
            .copyTo( new ElemArrayList<>( pasServer.channels().size() ) ) ) {
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
        if( logger.isSeverityOn( ELogSeverity.DEBUG ) && statisticsTimer.update() ) {
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
  @SuppressWarnings( { "boxing" } )
  public S5SessionCallbackChannel findChannel( Skid aSessionID, String aRemoteAddr, int aRemotePort ) {
    TsNullArgumentRtException.checkNull( aSessionID, aRemoteAddr );
    // 2022-01-15 mvk
    // Из-за специфического создания соединений клиента с сервером (с начала создается pas канал, потом сессия)
    // в пуле pas-сервера может быть несколько каналов с одним и тем же идентификатором сессии. Причем, некоторые уже
    // могут быть завершены, а некоторые еще нет (гонка потоков). Считаем, актуальным только последний (по времени
    // создания)
    IList<S5SessionCallbackChannel> channels = pasServer.channels();
    int count = channels.size();
    S5SessionCallbackChannel retValue = null;
    StringBuilder sb = (logger.isSeverityOn( ELogSeverity.DEBUG ) ? new StringBuilder() : null);
    // Индекс текущего канала
    int index = 0;
    for( S5SessionCallbackChannel channel : channels.copyTo( new ElemArrayList<>( count ) ) ) {
      // Параметры текущего канала
      Skid id = channel.getSessionID();
      String strid = id.strid();
      long time = channel.getCreationTimestamp();
      int port = channel.getRemotePort();
      String addr = channel.getRemoteAddress().getHostAddress();
      boolean isSuitable = (id.equals( aSessionID ) && addr.equals( aRemoteAddr ) && port == aRemotePort);
      if( sb != null ) {
        sb.append( format( FMT_CHANNEL, index++, strid, channel, isSuitable ? FMT_SUITABLE : EMPTY_STRING ) );
      }
      if( isSuitable ) {
        // Признак необходимости замены канала
        boolean needReplaceResult = (retValue == null || retValue.getCreationTimestamp() >= time);
        if( retValue != null ) {
          // Найден еще один канал с той же сессией
          logger.warning( ERR_FOUND_SESSION_DOUBLE, aSessionID.strid(), retValue, channel );
        }
        if( needReplaceResult ) {
          retValue = channel;
        }
      }
    }
    if( sb != null && retValue == null ) {
      sb.append( ERR_CHANNEL_NOT_FOUND );
    }
    if( retValue == null ) {
      unprovidedCount++;
    }
    if( sb != null ) {
      logger.debug( MSG_FIND_CHANNEL, aSessionID.strid(), aRemoteAddr, aRemotePort, count, sb.toString() );
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
    S5SessionMessenger messenger = sessionManager.findMessenger( aSessionID );
    for( S5SessionCallbackChannel channel : pasServer.channels()
        .copyTo( new ElemArrayList<>( pasServer.channels().size() ) ) ) {
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
    if( messenger == null ) {
      logger.info( "onOpenChannel(...): messenger = null. Search exist session for %s", aSessionID ); //$NON-NLS-1$
      S5SessionData session = sessionManager.findSessionData( aSessionID );
      if( session == null ) {
        return;
      }
      logger.warning( "onOpenChannel(...): messenger = null. Found session for %s. Try recreate messenger", //$NON-NLS-1$
          aSessionID );
      // Приемопередачик сообщений не был создан для сессии. Попытка создать приемопередатчик
      sessionManager.tryCreateMessenger( session );
      return;
    }
    // У сессии уже был писатель, заменяем ему канал. Старый (если он был) закрываем
    S5SessionCallbackChannel oldChannel = messenger.setChannel( aChannel );
    if( oldChannel != null ) {
      logger.error( "onOpenChannel(...): close double channel = %s, set new channel = %s", oldChannel, //$NON-NLS-1$
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
    S5SessionData session = sessionManager.findSessionData( sessionID );
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
