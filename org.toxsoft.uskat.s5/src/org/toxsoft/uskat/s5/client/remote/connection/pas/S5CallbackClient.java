package org.toxsoft.uskat.s5.client.remote.connection.pas;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.pas.server.IPasServerParams.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.s5.client.remote.connection.pas.IS5Resources.*;
import static org.toxsoft.uskat.s5.client.remote.connection.pas.S5CallbackOnGetBackendAddonInfos.*;
import static org.toxsoft.uskat.s5.client.remote.connection.pas.S5CallbackOnMessage.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;

import org.toxsoft.core.pas.client.PasClient;
import org.toxsoft.core.pas.common.*;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.bricks.ctx.ITsContext;
import org.toxsoft.core.tslib.bricks.ctx.impl.TsContext;
import org.toxsoft.core.tslib.bricks.events.msg.IGenericMessageListener;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;
import org.toxsoft.uskat.s5.client.remote.connection.*;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddon;
import org.toxsoft.uskat.s5.server.sessions.pas.*;

/**
 * Реализация {@link IS5CallbackClient}
 *
 * @author mvk
 */
public final class S5CallbackClient
    implements IS5CallbackClient, ICooperativeMultiTaskable {

  /**
   * Минимальный таймаут работы задачи doJob
   */
  private static final int DOJOB_TIMEOUT_MIN = 10;

  /**
   * Максимальный таймаут работы задачи doJob
   */
  private static final int DOJOB_TIMEOUT_MAX = 1000;

  /**
   * Соединение с сервером в рамках которого работает jms-соединение
   */
  private final S5Connection connection;

  /**
   * Фоновая задача
   */
  private final PasDoJob doJob;

  /**
   * Список PAS-соединений с узлами сервера
   */
  private final IListEdit<PasClient<S5CallbackChannel>> pasClients = new ElemLinkedList<>();

  /**
   * Таймаут (мсек) создания каналов
   */
  private final long createTimeout;

  /**
   * Блокировать формирование уведомлений до вызова
   */
  private boolean notificationEnabled;

  /**
   * Список описаний расширений {@link IS5BackendAddon} бекенда поддерживаемых сервером
   * <p>
   * Ключ: идентификатор расширения {@link IS5BackendAddon#id()};<br>
   * Значение: полное имя java-класса реализующий расширение {@link IS5BackendAddon};<br>
   */
  private IStringMap<String> serverBackendAddonInfos;

  /**
   * Признак завершения работы соединения
   */
  private boolean shutdown;

  /**
   * Время последней проверки соединений с узлами кластера
   */
  private long lastCheckTime = System.currentTimeMillis();

  /**
   * Журнал
   */
  private ILogger logger = getLogger( getClass() );

  /**
   * Конструктор
   *
   * @param aConnection {@link S5Connection} соединение с сервером в рамках которого работает jms-соединение
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5CallbackClient( S5Connection aConnection ) {
    TsNullArgumentRtException.checkNull( aConnection );
    connection = aConnection;
    // Конфигурация соединения
    IOptionSet options = aConnection.sessionInitData().connectionOptions();
    // Таймаут создания каналов
    createTimeout = IS5ConnectionParams.OP_CONNECT_TIMEOUT.getValue( options ).asInt();
    // 2021-03-27 mvk
    // Определение интервал doJob задачи
    long failureTimeout = IS5ConnectionParams.OP_FAILURE_TIMEOUT.getValue( options ).asInt();
    long doJobTimeout = Math.max( Math.min( (failureTimeout) / 3, DOJOB_TIMEOUT_MAX ), DOJOB_TIMEOUT_MIN );
    doJob = new PasDoJob( "S5CallbackClient doJob", this, doJobTimeout, logger ); //$NON-NLS-1$
    S5CallbackReaderExecutor.INSTANCE.execute( doJob );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5CallbackClient
  //
  @Override
  public synchronized InetSocketAddress start( S5ClusterTopology aTopology ) {
    TsNullArgumentRtException.checkNull( aTopology );
    // Завершение текущих соединений
    disconnected();
    // Конфигурация соединения
    IOptionSet options = connection.sessionInitData().connectionOptions();
    // Результат
    InetSocketAddress retValue = null;
    // Метка времени начала установки pas-соединений с узлами кластера сервера
    long startTime = System.currentTimeMillis();
    // Создание клиентов PAS-каналов
    synchronized (pasClients) {
      // Запрет уведомлений до вызова setNotificationEnabled
      notificationEnabled = false;
      // Создание каналов с узлами сервера
      for( IS5ClusterNodeInfo node : aTopology.nodes() ) {
        pasClients.add( createClient( connection, this, node, logger ) );
      }
      try {
        // Ожидание открытия каналов
        pasClients.wait( createTimeout );
        // Время ожидания соединения
        Long waitTime = Long.valueOf( System.currentTimeMillis() - startTime );
        // Список каналов с установленным соединением
        IList<S5CallbackChannel> connected = getConnectedChannels( pasClients );
        // Ожидание callback-каналов
        logger.info( MSG_CALLBACKS_WAIT, waitTime, Integer.valueOf( connected.size() ) );
        if( connected.size() != pasClients.size() ) {
          // Нет pas-подключений к узлам кластера s5-сервера. Завершаем все соединения
          disconnected();
          Integer cc = Integer.valueOf( connected.size() );
          Integer rc = Integer.valueOf( aTopology.nodes().size() );
          throw new S5ConnectionException( options, MSG_NODES_NOT_FOUND, connection, cc, rc );
        }
        S5CallbackChannel channel = connected.first();
        retValue = new InetSocketAddress( channel.getLocalAddress(), channel.getLocalPort() );
      }
      catch( InterruptedException e ) {
        throw new TsIllegalStateRtException( e );
      }
    }
    return retValue;
  }

  @Override
  public void setNotificationEnabled() {
    synchronized (pasClients) {
      IList<S5CallbackChannel> connected = getConnectedChannels( pasClients );
      if( connected.size() != pasClients.size() ) {
        // Формирование сообщения о завершении соединений
        logger.error( ERR_CLOSE_BY_NOT_CONNECTION );
        connection.restoreSessionQuery();
        return;
      }
      notificationEnabled = true;
    }
  }

  @Override
  public IPasTxChannel findChannel() {
    synchronized (pasClients) {
      return getConnectedChannels( pasClients ).first();
    }
  }

  @Override
  public IStringMap<String> backendAddonInfos() {
    return TsIllegalStateRtException.checkNull( serverBackendAddonInfos );
  }

  @Override
  public synchronized void updateClusterTopology( S5ClusterTopology aTopology ) {
    TsNullArgumentRtException.checkNull( aTopology );
    // Синхронизация каналов с узлами кластера с новой топологией
    synchronizeWithTopology( aTopology );
    // Поиск канала и передача по нему новой топологии для сессии клиента на сервере
    synchronized (pasClients) {
      for( PasClient<S5CallbackChannel> client : pasClients ) {
        S5CallbackChannel channel = client.getChannelOrNull();
        if( channel == null ) {
          // Отказ передачи топологии кластеров - нет канала связи с сервером
          logger.debug( ERR_CLUSTER_NO_CHANNEL );
          continue;
        }
        // Проверка того, что в топологии есть узел с адресом канала
        for( IS5ClusterNodeInfo node : aTopology.nodes() ) {
          if( node.address().equals( channel.getRemoteAddress().getHostAddress() )
              && node.port() + 1 == channel.getRemotePort() ) {
            // Передача топологии
            S5SessionCallbackClusterTopology.send( channel, aTopology );
            // Передача на сервер топологии кластеров доступных клиенту
            logger.info( MSG_SET_CLUSTER_TOPOLOGY, channel, aTopology );
            return;
          }
        }
        // Отказ передачи топологии кластеров - не найден канал в топологии
        logger.error( ERR_CLUSTER_NO_CHANNEL_BY_TOPOLOGY, aTopology );
      }
    }
    // Отказ передачи топологии кластеров - нет связи с сервером
    logger.error( ERR_CLUSTER_NO_CONNECT );
  }

  @Override
  public void disconnected() {
    try {
      synchronized (pasClients) {
        for( PasClient<S5CallbackChannel> pasClient : pasClients.copyTo( new ElemArrayList<>( pasClients.size() ) ) ) {
          pasClient.close();
        }
        pasClients.clear();
      }
    }
    finally {
      // Снимаем возможное состояние прерывания потока
      boolean isInterrupted = Thread.interrupted();
      if( isInterrupted ) {
        logger.warning( "S5CallbackClient.disconnected(). clear interrupted state" ); //$NON-NLS-1$
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация ICooperativeMultiTaskable
  //
  @Override
  public void doJob() {
    long currTime = System.currentTimeMillis();
    // Признак необходимости проверить сессию
    boolean needVerify = (currTime - lastCheckTime > 2 * STATEFULL_TIMEOUT / 3);
    if( needVerify ) {
      lastCheckTime = currTime;
    }
    synchronized (pasClients) {
      IList<PasClient<S5CallbackChannel>> clients = pasClients.copyTo( new ElemArrayList<>( pasClients.size() ) );

      // 2020-08-15 mvkd
      // if( needVerify == true ) {
      // System.err.println( "S5CallBackClient.doJob" );
      // connection.restoreSessionQuery();
      // return;
      // }

      for( PasClient<S5CallbackChannel> pasClient : clients ) {
        pasClient.doJob();
        S5CallbackChannel channel = pasClient.getChannelOrNull();
        if( channel == null ) {
          continue;
        }
        if( currTime - channel.getCreationTimestamp() > createTimeout ) {
          // Период подключения к серверу завершен
          if( channel.needRegularFailureTimeout() ) {
            // Установка регулярного таймаута отказа работоспособности канала
            IOptionSet options = connection.sessionInitData().connectionOptions();
            long failureTimeout = IS5ConnectionParams.OP_FAILURE_TIMEOUT.getValue( options ).asInt();
            if( failureTimeout < 0 || failureTimeout > (2 * STATEFULL_TIMEOUT) / 3 ) {
              failureTimeout = (2 * STATEFULL_TIMEOUT) / 3;
            }
            channel.setRegularFailureTimeout( failureTimeout );
          }
        }

        if( needVerify ) {
          // Передача session-verify уведомления
          logger.info( MSG_SEND_VERIFY, channel );
          S5SessionCallbackVerify.send( channel );
        }
      }
    }
    if( logger.isSeverityOn( ELogSeverity.DEBUG ) ) {
      // Вывод в журнал о завершении работы doJob
      Long time = Long.valueOf( System.currentTimeMillis() - currTime );
      Integer cc = null;
      synchronized (pasClients) {
        cc = Integer.valueOf( getConnectedChannels( pasClients ).size() );
      }
      logger.debug( MSG_DOJOB_RUN, time, connection, cc );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация ICloseable
  //
  @Override
  public void close() {
    if( shutdown ) {
      return;
    }
    shutdown = true;
    disconnected();
    doJob.close();
    // TODO: ??? вызов создает состояние interrupted на текущем потоке
    // executor.shutdownNow();
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //
  /**
   * Обработка события: открытие канала
   *
   * @param aChannel {@link S5CallbackChannel} канал
   * @throws TsNullArgumentRtException аргумент = null
   */
  void onOpenChannel( S5CallbackChannel aChannel ) {
    TsNullArgumentRtException.checkNull( aChannel );
    // Инициализация сессии (передача sessionID)
    S5SessionCallbackInit.send( aChannel, connection.sessionInitData().sessionID() );
  }

  /**
   * Обработка события: завершение работы канала
   *
   * @param aChannel {@link S5CallbackChannel} канал
   * @throws TsNullArgumentRtException аргумент = null
   */
  void onCloseChannel( S5CallbackChannel aChannel ) {
    TsNullArgumentRtException.checkNull( aChannel );
    synchronized (pasClients) {
      if( !notificationEnabled ) {
        // Уведомление игнорируется - уведомления запрещены
        logger.warning( ERR_CLOSE_NOTIFICATION_IGNORED );
        return;
      }
      if( getConnectedChannels( pasClients ).size() <= 1 ) {
        // Закрывается последнее callback-соединение с серверов. Пересоздание соединения
        logger.error( ERR_CLOSE_BY_NOT_CONNECTION );
        connection.restoreSessionQuery();
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Ищет каналы с установленным соединением
   *
   * @param aClients {@link IList} список клиентов
   * @return {link IList}&lt;{link S5CallbackChannel}&gt; список каналов с установленным соединением
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static IList<S5CallbackChannel> getConnectedChannels( IList<PasClient<S5CallbackChannel>> aClients ) {
    TsNullArgumentRtException.checkNull( aClients );
    int count = aClients.size();
    IListEdit<S5CallbackChannel> retValue = new ElemArrayList<>( count );
    for( PasClient<S5CallbackChannel> pasClient : aClients.copyTo( new ElemArrayList<>( count ) ) ) {
      S5CallbackChannel channel = pasClient.getChannelOrNull();
      if( channel != null ) {
        retValue.add( channel );
      }
    }
    return retValue;
  }

  /**
   * Создает PAS-клиента
   *
   * @param aConnection {@link S5Connection} s5-соединение с сервером
   * @param aReader {@link S5CallbackClient} читатель обратных вызовов сервера
   * @param aNode {@link IS5ClusterNodeInfo} описание узла кластера сервера
   * @param aLogger {@link ILogger} журнал работы
   * @return {@link PasClient} PAS-клиент
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static PasClient<S5CallbackChannel> createClient( S5Connection aConnection, S5CallbackClient aReader,
      IS5ClusterNodeInfo aNode, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aConnection, aReader, aNode, aLogger );
    // Конфигурация соединения
    IOptionSet options = aConnection.sessionInitData().connectionOptions();
    // Сетевой адрес подключаемого узла
    String hostname = aNode.address();
    // Порт подключаемого узла
    int port = getPasPort( aNode );
    // Создание сервера приема соединений обратных вызовов
    ITsContext ctx = new TsContext();
    ctx.put( S5CallbackClient.class, aReader );
    OP_PAS_SERVER_ADDRESS.setValue( ctx.params(), avStr( hostname ) );
    OP_PAS_SERVER_PORT.setValue( ctx.params(), avInt( port ) );
    // Имя канала
    String channelName = aConnection.toString() + '[' + aNode.toString() + ']';
    // Идентификация канала (узел-сессия)
    DDEF_NAME.setValue( ctx.params(), avStr( channelName ) );
    DDEF_DESCRIPTION.setValue( ctx.params(), avStr( channelName ) );
    // Чтобы отработать "шторм подключения" после запуска сервера, на создании канала failureTimeout = createTimeout
    long failureTimeout = IS5ConnectionParams.OP_FAILURE_TIMEOUT.getValue( options ).asInt();
    // Минимальный интервал передачи пакетов через соединение (не может быть больше чем 2/3 таймаута сессии)
    IPasParams.OP_PAS_FAILURE_TIMEOUT.setValue( ctx.params(), avInt( failureTimeout ) );
    // aExternalDoJobCall = true: НЕ создавать внутренний поток для doJob
    PasClient<S5CallbackChannel> retValue = new PasClient<>( ctx, S5CallbackChannel.CREATOR, true, aLogger );
    // Регистрация обработчиков
    IGenericMessageListener frontend = aConnection.frontend();
    retValue.registerNotificationHandler( ON_MESSAGE_METHOD, new S5CallbackOnMessage( frontend ) );
    retValue.registerNotificationHandler( ON_GET_BACKEND_ADDON_INFOS_METHOD, new S5CallbackOnGetBackendAddonInfos() {

      @Override
      protected void doWhenGetBackendAddonIds( IStringMap<String> aBackendAddonInfos ) {
        // Получение от узла информации о реализации бекенда
        aReader.serverBackendAddonInfos = aBackendAddonInfos;
        synchronized (aReader.pasClients) {
          IList<S5CallbackChannel> connected = getConnectedChannels( aReader.pasClients );
          if( connected.size() == aReader.pasClients.size() ) {
            // Есть связь со всеми узлами кластера, выдаем сигнал о завершении подключения к серверу по PAS-каналу
            aReader.pasClients.notifyAll();
          }
        }
      }
    } );
    retValue.init();
    // Запуск потока
    S5CallbackReaderExecutor.INSTANCE.execute( retValue );
    return retValue;
  }

  /**
   * Синхронизация каналов подключения к узлам кластера сервера с указанной топологией
   *
   * @param aTopology {@link S5ClusterTopology} топология кластеров доступная клиенту
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void synchronizeWithTopology( S5ClusterTopology aTopology ) {
    TsNullArgumentRtException.checkNull( aTopology );
    // Поиск канала и передача по нему новой топологии для сессии клиента на сервере
    synchronized (pasClients) {
      for( IS5ClusterNodeInfo node : aTopology.nodes() ) {
        boolean needConnect = true;
        for( PasClient<S5CallbackChannel> client : pasClients ) {
          if( node.address().equals( client.remoteAddress() ) && getPasPort( node ) == client.remotePort() ) {
            needConnect = false;
            break;
          }
        }
        if( needConnect ) {
          // Обновление топологии. Подключение к новому узлу
          logger.info( MSG_CONNECT_NODE, connection, aTopology, node );
          pasClients.add( createClient( connection, this, node, logger ) );
        }
      }
      // Проверка и если требуется завершение соединений с отключенными узлами кластера
      for( PasClient<S5CallbackChannel> client : pasClients
          .copyTo( new ElemArrayList<PasClient<S5CallbackChannel>>() ) ) {
        boolean needDisconnect = true;
        for( IS5ClusterNodeInfo node : aTopology.nodes() ) {
          if( node.address().equals( client.remoteAddress() ) && getPasPort( node ) == client.remotePort() ) {
            needDisconnect = false;
            break;
          }
        }
        if( needDisconnect ) {
          // Обновление топологии. Отключение от узла
          logger.info( MSG_DISCONNECT_NODE, connection, aTopology, getClientString( client ) );
          client.close();
          pasClients.remove( client );
        }
      }
    }
  }

  /**
   * Возвращает номер порта PAS для указанного узла кластера сервера
   *
   * @param aNode {@link IS5ClusterNodeInfo} узел кластера сервера
   * @return int номер порта
   * @throws TsNullArgumentRtException аргумент = null;
   */
  private static int getPasPort( IS5ClusterNodeInfo aNode ) {
    TsNullArgumentRtException.checkNull( aNode );
    return aNode.port() + 1;
  }

  /**
   * Возвращает текствое представление клиента
   *
   * @param aClient {@link PasClient} PAS-клиент
   * @return String текствое представление клиента
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static String getClientString( PasClient<S5CallbackChannel> aClient ) {
    TsNullArgumentRtException.checkNull( aClient );
    return String.format( "%s:%d", aClient.remoteAddress(), Integer.valueOf( aClient.remotePort() ) ); //$NON-NLS-1$
  }

  /**
   * Исполнитель потоков {@link S5CallbackClient}
   */
  private static class S5CallbackReaderExecutor
      implements Executor {

    static Executor INSTANCE = new S5CallbackReaderExecutor();

    private static int execCount;

    private ILogger logger = getLogger( getClass() );

    /**
     * Закрытый конструктор
     */
    private S5CallbackReaderExecutor() {
    }

    // ------------------------------------------------------------------------------------
    // Реализация Executor
    //
    @Override
    public void execute( Runnable aCommand ) {
      TsNullArgumentRtException.checkNull( aCommand );
      String threadId = String.format( "callback pas client %d", Integer.valueOf( ++execCount ) ); //$NON-NLS-1$
      Thread thread = new Thread( aCommand, threadId );
      thread.start();
      logger.info( "Start pas client thread = %s", threadId ); //$NON-NLS-1$
    }

  }
}
