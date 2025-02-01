package org.toxsoft.uskat.s5.client.remote.connection;

import static java.lang.String.*;
import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.client.IS5ConnectionParams.*;
import static org.toxsoft.uskat.s5.client.remote.connection.IS5Resources.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.common.S5HostList.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5ThreadUtils.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;

import javax.ejb.*;
import javax.naming.*;
import javax.security.sasl.*;

import org.jboss.ejb.client.*;
import org.jboss.ejb.client.EJBClientContext.*;
import org.jboss.ejb.protocol.remote.*;
import org.jboss.ejb.protocol.remote.S5ClusterNodeUtils.*;
import org.jboss.marshalling.Pair;
import org.toxsoft.core.pas.common.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.utils.*;
import org.toxsoft.uskat.s5.client.remote.connection.pas.*;
import org.toxsoft.uskat.s5.common.*;
import org.toxsoft.uskat.s5.server.*;
import org.toxsoft.uskat.s5.server.backend.*;
import org.toxsoft.uskat.s5.server.sessions.init.*;
import org.toxsoft.uskat.s5.utils.progress.*;
import org.toxsoft.uskat.s5.utils.threads.impl.*;
import org.wildfly.common.context.*;
import org.wildfly.naming.client.*;
import org.wildfly.naming.client.remote.*;
import org.wildfly.naming.client.util.*;
import org.wildfly.security.auth.client.*;
import org.wildfly.security.sasl.*;

/**
 * Реализация соединение с сервером {@link IS5Connection}.
 *
 * @author mvk
 */
public final class S5Connection
    implements IS5Connection, Supplier<AuthenticationContext>, IS5ClusterTopologyListener {

  /**
   * Текстовое представление точки подключения
   */
  public static final String ENTRY_POINT = "%s@%s/%s(%s)"; //$NON-NLS-1$

  /**
   * Текстовое представление потоков соединения
   */
  public static final String THREAD_ID = "%s-%d"; //$NON-NLS-1$

  /**
   * Имена узлов кластера сервера
   */
  private static final String CLUSTER_NODE_NAME = "node%02d"; //$NON-NLS-1$

  /**
   * Тайматут (мсек) ожидания блокировки соединения
   */
  private static final long LOCK_TIMEOUT = 10000;

  /**
   * Параметры создания сессии
   */
  private IOptionSet openArgs = IOptionSet.NULL;

  /**
   * Текущее состояние соединениия
   */
  private volatile EConnectionState state = EConnectionState.DISCONNECTED;

  /**
   * Данные инициализации сессии
   */
  private final S5SessionInitData sessionInitData;

  /**
   * Результаты инициализации сессии
   */
  private final S5SessionInitResult sessionInitResult;

  /**
   * Загрузчик классов используемый соединением
   */
  private final ClassLoader classLoader;

  /**
   * Загрузчик классов используемый соединением
   */
  // private final ClassLoader fooClassLoader;

  /**
   * Блокировка доступа к ресурсам соединения
   */
  private final S5Lockable lock;

  /**
   * frontend
   */
  private final ISkFrontendRear frontend;

  /**
   * Блокировка доступа к ресурсам соединения
   */
  private final S5Lockable frontedLock;

  /**
   * Поток поиска API сервера. null: поиск не проводится
   */
  private volatile Thread lookupApiThread;

  /**
   * Получатель обратных вызовов сервера через-соединение. null: соединение не активно
   */
  private IS5CallbackClient callbackReader;

  /**
   * Удаленный backend s5-сервера. null: нет соединения с сервером
   */
  private IS5BackendSession remoteBackend;

  /**
   * Поток выполнения повторного s5-соединения
   */
  private S5ConnectionThread reconnectThread;

  /**
   * Признак того, что идет процесс завершения соединения.
   * <p>
   * Выставляется в true на время работы метода {@link #closeSession()}, чтобы игнорировать вызов некторых методов
   * класса из слушателей события завершения связи.
   */
  private boolean closingNow;

  /**
   * Список зарегистрированных слушателей изменения состояния установления связи.
   */
  private final IListBasicEdit<IS5ConnectionListener> listeners = new ElemArrayList<>();

  /**
   * Блокировка доступа к ресурсам слушателям соединения
   */
  private final S5Lockable listenersLock = new S5Lockable();

  /**
   * Контекст аутентификации
   */
  private AuthenticationContext authenticationContext;

  /**
   * EJB-контекст клиента
   */
  private EJBClientContext ejbClientContext;

  /**
   * Адрес сервера работающего в standalone-режиме. null: адрес не определен
   */
  private Affinity standaloneAffinity;

  /**
   * Уникальный номер соединения
   */
  private long uniqueId;

  /**
   * Монитор прогресса проводимой операции на соединении
   */
  private IS5ProgressMonitor progressMonitor = IS5ProgressMonitor.NULL;

  /**
   * Счетчик созданных экземпляров соединений
   */
  private volatile static long instanceCount = 0;

  /**
   * Журнал работы
   */
  private ILogger logger;

  /**
   * Конструктор соединения
   *
   * @param aSessionID идентификатор сессии {@link ISkSession}
   * @param aClassLoader {@link ClassLoader} загрузчик классов
   * @param aFrontend {@link ISkFrontendRear} frontend
   * @param aFrontendLock {@link S5Lockable} блокировка доступа к frontend
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5Connection( Skid aSessionID, ClassLoader aClassLoader, ISkFrontendRear aFrontend,
      S5Lockable aFrontendLock ) {
    TsNullArgumentRtException.checkNulls( aSessionID, aClassLoader, aFrontend, aFrontendLock );
    sessionInitData = new S5SessionInitData( aSessionID );
    sessionInitResult = new S5SessionInitResult();
    classLoader = TsNullArgumentRtException.checkNull( aClassLoader );
    // fooClassLoader = ClassLoader.getSystemClassLoader();
    frontend = TsNullArgumentRtException.checkNull( aFrontend );
    lock = TsNullArgumentRtException.checkNull( aFrontendLock );
    frontedLock = aFrontendLock;
    uniqueId = instanceCount++;
    logger = getLogger( getClass() );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Переводит соединение в активное состояние и делает попытку открыть сессию связи (устанавливает соединение) с
   * сервером.
   *
   * @param aConfiguration {@link IOptionSet} конфигурация соединения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException сессия связи уже активна
   * @throws TsIllegalStateRtException соединение завершается (выполняется метод {@link #closeSession()}
   * @throws TsIllegalStateRtException работа с соединением завершена
   * @throws S5ConnectionException ошибка установления связи или инициализации сессии
   */
  public void openSession( IOptionSet aConfiguration )
      throws S5ConnectionException {
    openSession( aConfiguration, IS5ProgressMonitor.NULL );
  }

  /**
   * Переводит соединение в активное состояние и делает попытку открыть сессию связи (устанавливает соединение) с
   * сервером.
   * <p>
   * После успешного установления соединения, вызывается слушатель
   * {@link IS5ConnectionListener#onAfterConnect(IS5Connection)}.
   *
   * @param aOptions {@link IOptionSet} конфигурация соединения
   * @param aProgressMonitor {@link IS5ProgressMonitor} монитор прогресса проводимой операции на соединении
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException сессия связи уже активна
   * @throws TsIllegalStateRtException соединение завершается (выполняется метод {@link #closeSession()}
   * @throws TsIllegalStateRtException работа с соединением завершена
   * @throws S5ConnectionException ошибка установления связи или инициализации сессии
   */
  public void openSession( IOptionSet aOptions, IS5ProgressMonitor aProgressMonitor )
      throws S5ConnectionException {
    TsNullArgumentRtException.checkNull( aOptions );
    TsNullArgumentRtException.checkNull( aProgressMonitor );
    openArgs = new OptionSet( aOptions );
    progressMonitor = aProgressMonitor;
    // Хосты на которых развернуты узлы кластера сервера
    S5HostList hosts = OP_HOSTS.getValue( aOptions ).asValobj();

    if( hosts.size() == 0 ) {
      // Не указаны адреса серверов для соединения
      throw new S5ConnectionException( aOptions, ERR_HOSTS_NOT_DEFINED );
    }
    String host = hosts.first().address();
    progressMonitor.beginTask( format( USER_PROGRESS_CONNECT, host ), -1 );
    progressMonitor.subTask( format( USER_PROGRESS_WAIT_OPEN ) );
    lockWrite( lock );
    try {
      TsIllegalStateRtException.checkTrue( state != EConnectionState.DISCONNECTED, ERR_ALREADY_ACTIVE );
      // Проверка, что соединение не в состоянии завершения
      checkNotClosing();
      // Оповещение о начале активизации соединения
      progressMonitor.subTask( format( USER_PROGRESS_BEFORE_ACTIVE ) );
      fireOnBeforeActiveEvent();
      // Перевод соединения в активное состояние
      progressMonitor.subTask( format( USER_PROGRESS_ACTIVE_ON ) );
      setConnectState( EConnectionState.INACTIVE );
      // Сохранение параметров подключения к серверу (создание копии)
      sessionInitData.setClientOptions( aOptions );
      // Логин подключения
      String login = OP_USERNAME.getValue( aOptions ).asString();
      logger.info( MSG_CONNECTION_ACTIVATED, login );
      // callback-соединение
      callbackReader = new S5CallbackClient( this );
      // Оповещение о завершении активизации соединения
      progressMonitor.subTask( format( USER_PROGRESS_AFTER_ACTIVE ) );
      fireOnAfterActiveEvent();
      // Попытка образовать связь с сервером
      try {
        try {
          tryConnect();
        }
        finally {
          // // 2020-07-23 mvk снимаем возможно установленный признак interrupted
          // boolean wasInterrupted = Thread.interrupted();
          // // Завершение tryConnect в потоке вызова openSession
          // logger.info( MSG_OPEN_CONNECT_COMPLETED, Boolean.valueOf( wasInterrupted ) );
        }
      }
      catch( S5ConnectionException e ) {
        // Ошибка установки соединения
        setConnectState( EConnectionState.DISCONNECTED );
        // Завершение работы читателя
        if( callbackReader != null ) {
          callbackReader.close();
          callbackReader = null;
        }
        fireOnAfterDeActiveEvent();
        throw e;
      }
      catch( Throwable e ) {
        // Ошибка установки соединения
        setConnectState( EConnectionState.DISCONNECTED );
        // Завершение работы читателя
        if( callbackReader != null ) {
          callbackReader.close();
          callbackReader = null;
        }
        fireOnAfterDeActiveEvent();
        throw new TsInternalErrorRtException( e );

        // 2020-08-16 mvk контракт openSession:
        // "поднимать исключение если нет связи, автоматическая установка НОВОГО соединения не предусматривается"
        // if( state != EConnectionState.DISCONNECTED ) {
        // // Соединение осталось активным. Запускаем поток образования соединения
        // reconnectThread = new S5ConnectionThread( this );
        // reconnectThread.setContextClassLoader( classLoader );
        // reconnectThread.start();
        // }
        // throw e;
      }
    }
    finally {
      unlockWrite( lock );
    }
  }

  /**
   * Переводит соединение в неактивное состояние. Перед переводом в неактивное состояние и при наличии связи сервером,
   * связь сервером завершается.
   * <p>
   * В начале этого метода вызывается слушатель {@link IS5ConnectionListener#onBeforeDisconnect(IS5Connection)}, а после
   * разрыва соединения еще и {@link IS5ConnectionListener#onAfterDisconnect(IS5Connection)}.
   * <p>
   * Метод не выбрасывает каких либо исключении, связанных с работой с сервером. Если содеинение не установлено, то
   * метод ничего не делает.
   *
   * @throws TsIllegalStateRtException соединение завершается (выполняется метод {@link #closeSession()}
   */
  public void closeSession() {
    // Попытка остановки потока восстановления связи
    tryShutdownReconnectThread();
    lockWrite( lock );
    try {
      if( state == EConnectionState.DISCONNECTED ) {
        // Соединение уже неактивно
        return;
      }
      // Статус соединения перед завершением связи
      EConnectionState prevState = state;
      // Опции соединения
      IOptionSet options = sessionInitData.clientOptions();
      // Параметры конфигурации
      String login = OP_USERNAME.getValue( options ).asString();
      S5HostList hosts = OP_HOSTS.getValue( options ).asValobj();
      String host = hosts.first().address();
      Integer port = Integer.valueOf( hosts.first().port() );
      String apiIntefaceName = IS5ImplementConstants.BACKEND_SESSION_INTERFACE;
      String apiBeanName = IS5ImplementConstants.BACKEND_SESSION_IMPLEMENTATION;
      String entryPoint = format( ENTRY_POINT, login, host, port, apiIntefaceName, apiBeanName );
      logger.debug( MSG_CLOSE_CONNECTION_QUERY, entryPoint );
      checkNotClosing();
      closingNow = true;
      // Оповещение о начале деактивизации соединения
      fireOnBeforeDeActiveEvent();
      if( state == EConnectionState.CONNECTED ) {
        // Оповещение о предстоящем завершении соединения с сервером
        fireBeforeDisconnectEvent();
      }
      // Перевод соединения в неактивное состояние
      setConnectState( EConnectionState.DISCONNECTED );
      logger.info( MSG_CONNECTION_DEACTIVATED, login );
      // mvk 2019-03-22
      // // Завершение callback-соединения
      // callbackReader.removeExceptionListener( this );
      // callbackReader.disconnected();
      // callbackReader.close();
      // Завершение работы удаленной сессии
      if( prevState == EConnectionState.CONNECTED ) {
        safeCloseRemoteApi();
      }
      // mvk 2019-03-22
      // Завершение callback-соединения
      callbackReader.close();
      closingNow = false;
      // Обработка завершения соединения с сервером
      if( prevState == EConnectionState.CONNECTED ) {
        setConnectState( EConnectionState.DISCONNECTED );
        // Оповещение о завершении соединения с сервером
        fireAfterDisconnectEvent();
        logger.debug( MSG_CONNECTION_CLOSED, entryPoint );
      }
      callbackReader = null;
      // Оповещение о завершении деактивизации соединения
      fireOnAfterDeActiveEvent();
    }
    finally {
      unlockWrite( lock );
    }
  }

  // TODO: 2023-04-10 mvkd
  // boolean debug = false;

  /**
   * Устанавливает признак того, что необходимо восстановить сессию соединения с сервером
   */
  public void restoreSessionQuery() {
    // TODO: 2023-04-10 mvkd
    // debug = true;
    // // 2020-07-23 mvk завершаем работу потока восстановления связи
    // Thread reconnectThread = reconnectThread;
    // if( reconnectThread != null ) {
    // if( reconnectThread instanceof S5ConnectionThread ) {
    // // Работает поток восстановления связи требуем завершить его работу
    // ((S5ConnectionThread)reconnectThread).shutdownQuery();
    // }
    // // Запрещаем блокировки до завершения потока
    // reconnectThread.interrupt();
    // }
    // Принудительно завершаем все текущие вызовы
    logger.warning( ERR_RESTORE_CONNECTION_CALL_CLOSE_REMOTE );
    // 2021-01-19 mvk fix-попытка ошибки восстановления связи с сервером
    for( ;; ) {
      // Пробуем получить блокировку
      if( !tryLockWrite( lock, LOCK_TIMEOUT ) ) {
        // Ошибка получения блокировки
        logger.warning( ERR_RESTORE_CONNECTION_TRY_LOCK, lock );
        // Попытка разблокировать потоки удерживающие блокировку
        lockThreadInterrupt( lock );
        // Если возможно, то прерываем поток поиска сервера
        // Thread lt = lockThread;
        // if( lt != null ) {
        // // Прерывание потока блокировки
        // logger.warning( ERR_RESTORE_CONNECTION_LOCK_INTERRUPT, lt );
        // lt.interrupt();
        // }
        Thread lt = lookupApiThread;
        if( lt != null ) {
          // Прерывание потока поиска API сервера
          logger.warning( ERR_RESTORE_CONNECTION_LOOKUP_INTERRUPT, lt );
          lt.interrupt();
        }
        continue;
      }
      handlingUnexpectedBreak();
      try {
        if( closingNow || state == EConnectionState.DISCONNECTED ) {
          // Соединение в состоянии деактивации или уже стало неактивным
          logger.warning( ERR_CONNECTION_DEACTIVATED, Boolean.valueOf( closingNow ), state );
          return;
        }
        // 2021-01-20 mvk handlingUnexpectedBreak() изменил состояние соединения
        // if( state == EConnectionState.INACTIVE ) {
        // // Процесс восстановления уже запущен
        // String threadName = reconnectThread != null ? reconnectThread.getName() : "null"; //$NON-NLS-1$
        // logger.warning( ERR_RESTORE_ALREADY_RUNNING, threadName );
        // return;
        // }
        if( reconnectThread != null && reconnectThread.isAlive() ) {
          // Поток выполнения уже запущен
          logger.warning( ERR_RESTORE_THREAD_ALREADY_EXIST, reconnectThread.getName() );
          return;
        }
        try {
          // Запускаем поток образования соединения
          reconnectThread = new S5ConnectionThread( this );
          reconnectThread.setContextClassLoader( classLoader );
          reconnectThread.start();
          // Создание потока восстановления завершено
          logger.debug( MSG_CONNECTION_THREAD_STARTED, reconnectThread.getName() );
        }
        catch( Throwable e ) {
          // Ошибка запуска потока восстановления соединения
          logger.error( ERR_CONNECTION_THREAD_START, cause( e ) );
        }
        return;
      }
      finally {
        unlockWrite( lock );
      }
    }
  }

  /**
   * Возвращает подключенный frontend
   *
   * @return {@link ISkFrontendRear} frontend
   */
  public ISkFrontendRear frontend() {
    return frontend;
  }

  /**
   * Возвращает блокировку доступа к {@link #frontend()}
   *
   * @return {@link S5Lockable} блокировка доступа
   */
  // 2020-10-12 mvk doJob + mainLock
  public S5Lockable frontedLock() {
    return frontedLock;
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //
  /**
   * Осуществляет попытку подключения к серверу в соответствии с ранее установленными параметрами соединения
   * <p>
   * Если до вызова {@link #tryConnect()} соединение в состоянии {@link EConnectionState#CONNECTED}, то метод ничего не
   * делает
   * <p>
   * Если соединение установлено, то состояние соединения устанавливается в {@link EConnectionState#CONNECTED}, и
   * формируется сообщение об образовании соединения
   *
   * @throws S5ConnectionException ошибка подключения к серверу
   */
  void tryConnect()
      throws S5ConnectionException {
    progressMonitor.subTask( format( USER_PROGRESS_WAIT_TRY_CONNECT ) );
    IOptionSetEdit options = sessionInitData.clientOptions();
    lockWrite( lock );
    try {
      if( state == EConnectionState.CONNECTED ) {
        // Соединение уже установлено
        if( reconnectThread != null ) {
          // Работает поток восстановления связи требуем завершить его работу
          reconnectThread.shutdownQuery();
          reconnectThread = null;
        }
        return;
      }

      // Оповещение клиента о предстоящей попытки установке связи с сервером
      progressMonitor.subTask( format( USER_PROGRESS_BEFORE_CONNECT, this ) );
      fireOnBeforeConnectEvent();

      // Для подключения к серверу используется wildfly-запись. Позже, в S5BackendSession.init(...) проверяется учетная
      // запись самого пользователя
      String wlogin = OP_WILDFLY_LOGIN.getValue( options ).asString();
      String wpasswd = OP_WILDFLY_PASSWORD.getValue( options ).asString();

      // Передача пароля в hash-code
      String passwdHash = SkHelperUtils.getPasswordHashCode( OP_PASSWORD.getValue( openArgs ).asString() );
      // Замена пароля на хэш-код
      OP_PASSWORD.setValue( options, avStr( passwdHash ) );

      // Адрес сервера
      S5HostList hosts = OP_HOSTS.getValue( options ).asValobj();
      String moduleName = IS5ImplementConstants.BACKEND_SERVER_MODULE_ID;
      String apiIntefaceName = IS5ImplementConstants.BACKEND_SESSION_INTERFACE;
      String apiBeanName = IS5ImplementConstants.BACKEND_SESSION_IMPLEMENTATION;

      // Таймаут создания каналов
      long createTimeout = OP_CONNECT_TIMEOUT.getValue( options ).asInt();
      // Минимальный интервал передачи пакетов через соединение (не может быть больше чем 2/3 таймаута сессии)
      long failureTimeout = OP_FAILURE_TIMEOUT.getValue( options ).asInt();

      String entryPoint = format( ENTRY_POINT, wlogin, hostsToString( hosts, true ), apiIntefaceName, apiBeanName );
      ClassLoader loader = classLoader;
      try {
        // 2021-01-20 mvk необходимо закрыть все соединения чтобы они не мешали потом через callback
        callbackReader.disconnected();
        // Поиск сервера и бина его API
        progressMonitor.subTask( format( USER_LOOKUP_REMOTE_API_START, this ) );
        logger.debug( USER_LOOKUP_REMOTE_API_START, this );
        Pair<SessionID, IS5BackendSession> remote =
            lookupClusterRemoteApi( this, hosts, wlogin, wpasswd, moduleName, apiIntefaceName, apiBeanName, loader );
        // remoteSessionID = remote.getA();
        remoteBackend = remote.getB();
        // Сообщение завершении поиска сервера
        logger.debug( MSG_LOOKUP_REMOTE_API_FINISH, this, sessionInitData.sessionID() );
      }
      catch( ClassNotFoundException e ) {
        // В classpath клиента не найден класс интерфейса API сервера
        throw new S5ConnectionException( e, options, ERR_NO_CLIENT_SUPPORT, entryPoint, cause( e ) );
      }
      catch( Throwable e ) {
        if( e.getCause() instanceof NoSuchEJBException ) {
          // Сервер не поддерживает указанное API
          // mvk 2018-10-11
          // closeSession();
          throw new S5ConnectionException( e, options, ERR_ENTRY_NOT_FOUND, entryPoint, cause( e ) );
        }
        logger.debug( e, ERR_NOT_CONNECTED, entryPoint, cause( e ) );
        throw new S5ConnectionException( e, options, ERR_NOT_CONNECTED, entryPoint, cause( e ) );
      }
      try {
        // Получение адресов доступных узлов кластера, создание каналов обратного вызова
        progressMonitor.subTask( format( USER_INIT_CALLBACKS_START, this ) );
        // Текущая топология кластеров доступных клиенту
        S5ClusterTopology topology = createClusterTopology( ejbClientContext );
        if( topology.nodes().size() == 0 ) {
          // Сервер работает как standalone, без кластера
          String addr = hosts.get( 0 ).address();
          int port = hosts.get( 0 ).port();
          S5ClusterNodeInfo node = new S5ClusterNodeInfo( "no_cluster", "standalone", addr, port ); //$NON-NLS-1$//$NON-NLS-2$
          topology = new S5ClusterTopology( new ElemArrayList<>( node ) );
        }
        // Установка узлов кластера к которым есть доступ у клиента
        sessionInitData.setClusterTopology( topology );

        // 2021-03-31 mvk
        // В tm2, при подключении к системе через проброс порта промежуточных компьютеров и роутеров
        // потребовалось явно устанавливать affinity для каждого вызова.
        // Необходимо проверить что это будет работать на кластерных конфигурациях сервера
        //
        // Определение режима работы серверу и типа подключения к нему
        //
        // 2021-04-12 mvk ММ ФГДП:
        // когда адрес настроен как "10.238.3.190,10.238.3.191", а доступен (работает) только
        // 10.238.3.191, то появляется ошибка, так как подключение работает не как "кластерное"
        // TODO: ??? возможно потребуется определять в настройках соединения отдельный признак "кластер",
        // на тот случай, когда клиенту может быть доступен только один узел кластера
        // if( topology.nodes().size() <= 1 ) {
        if( hosts.size() == 1 && topology.nodes().size() == 1 ) {
          // Один узел, подключаемся как к standalone
          String addr = hosts.get( 0 ).address();
          int port = hosts.get( 0 ).port();
          URI uri = new URI( "remote+http://" + addr + ":" + String.valueOf( port ) ); //$NON-NLS-1$ //$NON-NLS-2$
          standaloneAffinity = Affinity.forUri( uri );
        }

        // Запуск получения обратных вызовов
        InetSocketAddress localAddress = callbackReader.start( topology );
        // Завершение получения адресов доступных узлов кластера, создание каналов обратного вызова
        progressMonitor.subTask( format( USER_INIT_CALLBACKS_FINISH, this ) );

        // Сообщение клиентам о обнаружении сервера
        fireOnAfterDiscoverEvent();

        // Установка локального адреса в опциях соединения
        OP_CLIENT_ADDRESS.setValue( options, avStr( localAddress.getAddress().getHostAddress() ) );
        OP_CLIENT_PORT.setValue( options, avInt( localAddress.getPort() ) );
        // Инициализация сессии пользователя на сервере
        logger.debug( USER_INIT_REMOTE_API_START, this );
        progressMonitor.subTask( format( USER_INIT_REMOTE_API_START, this ) );

        // 2020-07-23 mvk установка таймаута создания соединения
        logger.info( MSG_SET_INVOCATION_CREATE_TIMEOUT, Long.valueOf( createTimeout ) );
        EJBClient.setInvocationTimeout( remoteBackend, createTimeout, TimeUnit.MILLISECONDS );

        // Вывод в журнал текущего загрузчика классов
        logger.info( MSG_TRYCONNECT_USING_CLASSLOADER, Thread.currentThread().getContextClassLoader() );
        // Запрос соединения и его инициализация
        IS5SessionInitResult initResult = remoteBackend.init( sessionInitData );
        sessionInitResult.setAll( initResult );
        logger.debug( MSG_INIT_REMOTE_API_FINISH, this );

        // 2020-07-23 mvk установка таймаута отказа соединения
        logger.info( MSG_SET_INVOCATION_FAILURE_TIMEOUT, Long.valueOf( failureTimeout ) );
        EJBClient.setInvocationTimeout( remoteBackend, failureTimeout, TimeUnit.MILLISECONDS );

        // Разрешение формировать события о состоянии канала
        callbackReader.setNotificationEnabled();
      }
      catch( RequestSendFailedException e ) {
        logger.debug( e, ERR_NOT_CONNECTED, entryPoint, cause( e ) );
        callbackReader.disconnected();
        if( e.getCause() instanceof RequestSendFailedException ) {
          Throwable requestError = e.getCause();
          if( requestError.getCause() instanceof SaslException ) {
            // Неверное имя пользователя или пароль
            throw new S5ConnectionException( e, options, ERR_NO_ACCESS );
          }
          if( requestError.getCause() instanceof java.net.UnknownHostException ) {
            // Сервер не найден
            throw new S5ConnectionException( e, options, ERR_SERVER_NOT_FOUND );
          }
          if( requestError.getCause() instanceof java.net.ConnectException ) {
            // Сервер не найден
            throw new S5ConnectionException( e, options, ERR_SERVER_NOT_FOUND );
          }
        }
        throw new S5ConnectionException( e, options, ERR_INIT_SESSION, entryPoint, cause( e ) );
      }
      catch( Throwable e ) {
        callbackReader.disconnected();
        safeCloseRemoteApi();
        // Вывод журнала
        logger.error( e, ERR_INIT_SESSION, entryPoint, cause( e ) );
        throw new S5ConnectionException( e, options, ERR_INIT_SESSION, entryPoint, cause( e ) );
      }
      setConnectState( EConnectionState.CONNECTED );
      logger.debug( MSG_CREATE_CONNECTION, entryPoint );
      // 2020-08-16 mvk это лишние. tryConnect вызывается в двух вариантах:
      // 1. openSession - потока не должно быть
      // 2. S5ConnectionThread.run() - поток сам завершится если нет исключений (через break)
      // // Соединение установлено и инциализировано. Завершаем поток (если есть) восстановления связи
      // if( reconnectThread != null ) {
      // // Вызов метода - БЕЗОПАСНЕН под lockWrite. Ошибки можно не перехватывать
      // reconnectThread.shutdownQuery();
      // reconnectThread = null;
      // }

      // TODO: 2023-04-10 mvkd
      // if( debug ) {
      // while( true ) {
      // try {
      // LoggerUtils.errorLogger().error( "tryConnect(): debug halt 2" );
      // Thread.sleep( 1000000 );
      // }
      // catch( InterruptedException ex ) {
      // debug = false;
      // LoggerUtils.errorLogger().error( "tryConnect(): debug halt rise InterruptedException" );
      // throw new TsUnderDevelopmentRtException( ex );
      // }
      // // nop
      // }
      // }
    }
    finally {
      unlockWrite( lock );
    }
    progressMonitor.subTask( format( USER_PROGRESS_AFTER_CONNECT_END ) );
    try {
      fireOnAfterConnectEvent();
    }
    catch( RuntimeException e ) {
      // Были ошибки оповщения. Требование передподключения.
      logger.error( "tryConnect(...): fireOnAfterConnectEvent fail. start closing... cause = %s", cause( e ) ); //$NON-NLS-1$
      setConnectState( EConnectionState.INACTIVE );
      // Подготовка контекста соединения
      safeCloseRemoteApi();
      // Завершение текущего callback-соединения
      callbackReader.disconnected();
      remoteBackend = null;
      setConnectState( EConnectionState.INACTIVE );
      logger.error( "tryConnect(...): fireOnAfterConnectEvent fail. end closing... cause = %s", cause( e ) ); //$NON-NLS-1$
      // Необходимо поднять исключение, чтобы информировать клиента об ошибке установке связи
      throw new S5ConnectionException( e, options, "tryConnect(...): fireOnAfterConnectEvent error" ); //$NON-NLS-1$
    }
    try {
      progressMonitor.done();
    }
    catch( Throwable e ) {
      // Неожиданная ошибка монитора прогресса
      logger.error( e, ERR_UNEXPECT_PROGRESS_PROBLEM, cause( e ) );
    }
  }

  /**
   * Возвращает EJB-контекст клиента
   *
   * @return {@link EJBClientContext} контекст клиента
   * @throws TsIllegalStateRtException контекст не установлен
   */
  EJBClientContext ejbClientContext() {
    TsIllegalStateRtException.checkNull( ejbClientContext );
    return ejbClientContext;
  }

  /**
   * Установка EJB-контекста клиента для вызова remoteBackend в вызывающем потоке
   *
   * @throws TsIllegalStateRtException нет соединения с сервером
   */
  // void setEJBContextForCurrentThread() {
  // // TODO: mvk 2020-03-05
  // if( ejbClientContext == null ) {
  // logger().error( "setEJBContextForCurrentThread(): ejbClientContext = null" ); //$NON-NLS-1$
  // return;
  // }
  // ContextManager<EJBClientContext> ejbContextManager = EJBClientContext.getContextManager();
  // // mvk 2019-05-31
  // ejbContextManager.setClassLoaderDefault( classLoader, ejbClientContext );
  // // ejbContextManager.setThreadDefault( ejbClientContext );
  // }

  /**
   * Обработка потери связи с сервером
   */
  void handlingUnexpectedBreak() {
    lockWrite( lock );
    try {
      IOptionSet options = sessionInitData.clientOptions();
      String login = OP_USERNAME.getValue( options ).asString();
      S5HostList hosts = OP_HOSTS.getValue( options ).asValobj();
      String host = hosts.get( 0 ).address();
      Integer port = Integer.valueOf( hosts.get( 0 ).port() );

      String apiIntefaceName = IS5ImplementConstants.BACKEND_SESSION_INTERFACE;
      String apiBeanName = IS5ImplementConstants.BACKEND_SESSION_IMPLEMENTATION;

      String entryPoint = format( ENTRY_POINT, login, host, port, apiIntefaceName, apiBeanName );
      setConnectState( EConnectionState.INACTIVE );
      logger.debug( MSG_CONNECTION_BREAKED, entryPoint );
      // Удаленное API не вызвается, это может быть блокирующим вызовом
      // Подготовка контекста соединения
      safeCloseRemoteApi();
      // Завершение текущего callback-соединения
      callbackReader.disconnected();
      remoteBackend = null;
      fireAfterDisconnectEvent();
    }
    finally {
      unlockWrite( lock );
    }
  }

  /**
   * Возвращает журнал работы
   *
   * @return {@link ILogger} журнал работы
   */
  ILogger logger() {
    return logger;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5Connection
  //
  @Override
  public EConnectionState state() {
    return state;
  }

  @Override
  public IStringMap<String> baCreatorClasses() {
    TsNullArgumentRtException.checkNull( callbackReader );
    return callbackReader.baCreatorClasses();
  }

  @Override
  public S5SessionInitData sessionInitData() {
    return sessionInitData;
  }

  @Override
  public IS5SessionInitResult sessionInitResult() {
    return sessionInitResult;
  }

  @Override
  public IS5BackendSession session() {
    TsIllegalStateRtException.checkTrue( state == EConnectionState.DISCONNECTED, MSG_CONNECTION_NOT_ACTIVE );
    return remoteBackend;
  }

  @Override
  public IPasTxChannel callbackTxChannel() {
    TsIllegalStateRtException.checkTrue( state == EConnectionState.DISCONNECTED, MSG_CONNECTION_NOT_ACTIVE );
    IPasTxChannel retValue = callbackReader.findChannel();
    TsInternalErrorRtException.checkNull( retValue );
    return retValue;
  }

  @Override
  public void addConnectionListener( IS5ConnectionListener aListener ) {
    lockWrite( listenersLock );
    try {
      checkNotClosing();
      if( !listeners.hasElem( aListener ) ) {
        listeners.add( aListener );
      }
    }
    finally {
      unlockWrite( listenersLock );
    }
  }

  @Override
  public void removeConnectionListener( IS5ConnectionListener aListener ) {
    lockWrite( listenersLock );
    try {
      checkNotClosing();
      listeners.remove( aListener );
    }
    finally {
      unlockWrite( listenersLock );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация Supplier<AuthenticationContext>
  //
  @Override
  public AuthenticationContext get() {
    return authenticationContext;
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ClusterTopologyListener
  //
  @Override
  public void onTopologyComplete( String aClusterName, List<Pair<String, InetSocketAddress>> aNodes ) {
    // Создание кластера
    logger().info( MSG_CREATE_CLUSTER, aClusterName, nodesToStr( aNodes ) );
    // Обновление топологии кластеров доступной клиенту
    updateClusterTopology();
  }

  @Override
  public void onTopologyRemoval( String aClusterName ) {
    // Удаление кластера
    logger().info( MSG_REMOVE_CLUSTER, aClusterName );
    // Обновление топологии кластеров доступной клиенту
    updateClusterTopology();
  }

  @Override
  public void onTopologyAddition( String aClusterName, List<Pair<String, InetSocketAddress>> aNodes,
      List<String> aAddedNodes ) {
    // Добавление узлов в кластер
    logger().info( MSG_ADD_CLUSTER_NODE, aClusterName, nodesToStr( aNodes ), toStr( aAddedNodes ) );
    // Обновление топологии кластеров доступной клиенту
    updateClusterTopology();
  }

  @Override
  public void onTopologyNodeRemoval( String aClusterName, List<Pair<String, InetSocketAddress>> aNodes,
      List<String> aRemovedNodes ) {
    // Удаление узлов из кластера
    logger().info( MSG_REMOVE_CLUSTER_NODE, aClusterName, nodesToStr( aNodes ), toStr( aRemovedNodes ) );
    // Обновление топологии кластеров доступной клиенту
    updateClusterTopology();
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    IOptionSet options = sessionInitData.clientOptions();
    String login = OP_USERNAME.getValue( options ).asString();
    S5HostList hosts = OP_HOSTS.getValue( options ).asValobj();
    String host = hosts.get( 0 ).address();
    Integer port = Integer.valueOf( hosts.get( 0 ).port() );
    String apiIntefaceName = IS5ImplementConstants.BACKEND_SESSION_INTERFACE;
    String apiBeanName = IS5ImplementConstants.BACKEND_SESSION_IMPLEMENTATION;
    String entryPoint = format( ENTRY_POINT, login, host, port, apiIntefaceName, apiBeanName );
    return String.valueOf( uniqueId ) + ':' + entryPoint + ", state = " + state; //$NON-NLS-1$
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + (int)(uniqueId ^ (uniqueId >>> 32));
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
    S5Connection other = (S5Connection)aObject;
    if( uniqueId != other.uniqueId ) {
      return false;
    }
    return true;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Установить новое состояние соединения
   *
   * @param aState {@link EConnectionState} новое состояние
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void setConnectState( EConnectionState aState ) {
    TsNullArgumentRtException.checkNull( aState );
    if( state == aState ) {
      return;
    }
    switch( aState ) {
      case CONNECTED:
        break;
      case INACTIVE:
        break;
      case DISCONNECTED:
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    state = aState;
  }

  /**
   * Осуществляет попытку завершения потока восстановления связи (если он запущен)
   */
  private void tryShutdownReconnectThread() {
    // tryLockWrite( lock );
    // try {
    // // Если во время работы открывался поток установки связи. Убедимся, что он завершен
    // if( reconnectThread == null ) {
    // return;
    // }
    // logger.info( MSG_CONNECTION_THREAD_QUERY_FINISH, reconnectThread );
    // reconnectThread.shutdownQuery();
    // // Ожидание завершения потока
    // while( reconnectThread.isAlive() == true ) {
    // try {
    // Thread.sleep( 1 );
    // }
    // catch( InterruptedException e ) {
    // logger.error( e );
    // }
    // }
    // logger.info( MSG_CONNECTION_THREAD_COMPLETED, reconnectThread );
    // reconnectThread = null;
    // }
    // finally {
    // unlockWrite( lock );
    // }
    // Если во время работы открывался поток установки связи. Убедимся, что он завершен
    S5ConnectionThread thread = reconnectThread;
    if( thread == null ) {
      return;
    }
    logger.info( MSG_CONNECTION_THREAD_QUERY_FINISH, thread );
    thread.shutdownQuery();
    // Ожидание завершения потока
    while( thread.isAlive() ) {
      try {
        Thread.sleep( 1 );
      }
      catch( InterruptedException e ) {
        logger.error( e );
      }
    }
    logger.info( MSG_CONNECTION_THREAD_COMPLETED, thread );
    reconnectThread = null;
  }

  /**
   * Проводит поиск удаленного API сервера
   *
   * @param aConnection {@link S5Connection} соединение с сервером
   * @param aHosts {@link S5HostList} список описаний хостов узлов кластера сервера
   * @param aLogin String имя пользователя
   * @param aPassword String пароль пользователя
   * @param aModuleName String имя модуля сервера реализующего API
   * @param aInterfaceName String имя класса интерфейса удаленной ссылки на API
   * @param aBeanName String имя класса бина реализующего API клиента
   * @param aClassLoader {@link ClassLoader} загручик классов для создания proxy API сервера
   * @return {@link Pair}&lt;{@link SessionID},{@link IS5BackendSession}&gt; пара представляющая идентификатор сессии и
   *         удаленную ссылку на backend
   * @throws Exception ошибка создания соединения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws NamingException контекст не найден
   */
  @SuppressWarnings( { "nls", "unchecked" } )
  private Pair<SessionID, IS5BackendSession> lookupClusterRemoteApi( S5Connection aConnection, S5HostList aHosts,
      String aLogin, String aPassword, String aModuleName, String aInterfaceName, String aBeanName,
      ClassLoader aClassLoader )
      throws Exception {
    TsNullArgumentRtException.checkNull( aConnection );
    TsNullArgumentRtException.checkNull( aHosts );
    TsNullArgumentRtException.checkNull( aLogin );
    TsNullArgumentRtException.checkNull( aPassword );
    TsNullArgumentRtException.checkNull( aClassLoader );
    int hostCount = aHosts.size();
    if( hostCount == 0 ) {
      throw new TsIllegalStateRtException( "Неопределен host и port сервера." );
    }
    // Подбор параметров соедниения: https://docs.jboss.org/author/display/EJBCLIENT/Overview+of+Client+properties
    // Properties properties = new Properties();
    FastHashtable<String, Object> properties = new FastHashtable<>();
    // properties.put( Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory" );
    // properties.put( Context.PROVIDER_URL, "http-remoting://localhost:8080" );
    // properties.put( Context.SECURITY_PRINCIPAL, aLogin );
    // properties.put( Context.SECURITY_CREDENTIALS, aPassword );

    // TODO: 2020-03-16 mvk ???
    properties.put( "org.jboss.ejb.client.scoped.context", "true" );
    properties.put( "java.naming.factory.url.pkgs", "org.jboss.ejb.client.naming" );
    // 2020-09-03 mvk ---
    // properties.put( "invocation.timeout", "300000" );
    properties.put( "remote.connections.connect.eager", "false" );

    properties.put( "remote.clusters", "ejb" );
    properties.put( "remote.cluster.ejb.username", aLogin );
    properties.put( "remote.cluster.ejb.password", aPassword );
    properties.put( "remote.cluster.ejb.connect.options.org.xnio.Options.SASL_DISALLOWED_MECHANISMS",
        "JBOSS-LOCAL-USER" );
    properties.put( "remote.cluster.ejb.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false" );

    StringBuilder sb = new StringBuilder();
    for( int index = 0; index < hostCount; index++ ) {
      String nodeId = format( CLUSTER_NODE_NAME, Integer.valueOf( index + 1 ) );
      sb.append( nodeId );
      if( index + 1 < hostCount ) {
        sb.append( ',' );
      }
      String host = aHosts.get( index ).address();
      int port = aHosts.get( index ).port();
      // @formatter:off
      properties.put( format( "remote.connection.%s.host", nodeId ), host );
      properties.put( format( "remote.connection.%s.port", nodeId ), Integer.toString(port) );
      properties.put( format( "remote.connection.%s.username", nodeId ), aLogin );
      properties.put( format( "remote.connection.%s.password", nodeId ), aPassword );
      // properties.put( format( "remote.connection.%s.connect.timeout", nodeId ), "1" );

      properties.put( format( "remote.connection.%s.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", nodeId ), "false" );
      properties.put( format( "remote.connection.%s.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", nodeId ), "false" );
      properties.put( format( "remote.connection.%s.connect.options.org.xnio.Options.SASL_DISALLOWED_MECHANISMS", nodeId ), "JBOSS-LOCAL-USER" );
      // @formatter:on
    }
    properties.put( "remote.connections", sb.toString() );
    properties.put( "remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false" );
    properties.put( "endpoint.name", "s5.endpoint" );

    // properties.put( "invocation.timeout", "2000" );
    // properties.put( "reconnect.tasks.timeout", "3000" );
    // properties.put( "remote.connection.default.connect.timeout", "3000" );
    // properties.put(
    // "remote.connection.default.connect.options.org.jboss.remoting3.RemotingOptions.HEARTBEAT_INTERVAL",
    // 2000 );

    // properties.put( "endpoint.create.options.org.xnio.Options.THREAD_DAEMON", "false" );
    // properties.put( "remote.connection.default.connect.options.org.xnio.Options.KEEP_ALIVE", "true" );
    // properties.put( "endpoint.create.options.org.xnio.Options.WORKER_TASK_MAX_THREADS", "1" );
    // OptionMap.create( Options.THREAD_DAEMON, false ).toString() );

    // properties.put( "remote.connection.default.protocol", "remoting" );
    // properties.put( "endpoint.name", "client-endpoint" );

    try {
      // Формирование контеста аутентификации пользователя
      ProviderEnvironment.Builder builder = new ProviderEnvironment.Builder();
      builder.populateFromEnvironment( properties );
      ProviderEnvironment providerEnvironment = builder.build();
      authenticationContext = providerEnvironment.getAuthenticationContextSupplier().get();

      // 2019-05-30: mvk: try fix server to server connection error. source:
      // https://developer.jboss.org/thread/278645
      AuthenticationConfiguration superUser = AuthenticationConfiguration.empty()
          .setSaslMechanismSelector( SaslMechanismSelector.NONE.addMechanism( "PLAIN" ) ). // //$NON-NLS-1$
          useName( aLogin ).usePassword( aPassword );
      authenticationContext = authenticationContext.with( MatchRule.ALL, superUser );
      // TODO: 2020-03-15 ??? возможно тоже требует замены на setGlobalDefault
      AuthenticationContext.getContextManager().setThreadDefault( authenticationContext );
      // AuthenticationContext.getContextManager().setGlobalDefault( authenticationContext );

      // Код выполняемый в потоке контекста авторизации
      java.util.concurrent.Callable<Pair<SessionID, IS5BackendSession>> callable = () -> {
        // Прокси точки входа на сервер
        Class<IS5BackendSession> view = (Class<IS5BackendSession>)aClassLoader.loadClass( aInterfaceName );
        String appname = "";
        String modulename = aModuleName;
        String beanname = aBeanName;
        String distinctname = "";
        String hostname = null;
        Integer port = null;
        for( S5Host host : aHosts ) {
          String h = host.address();
          int p = host.port();
          if( availableNode( h, p, 1000, logger ) ) {
            hostname = h;
            port = Integer.valueOf( p );
            break;
          }
        }
        if( hostname == null || port == null ) {
          // Нет доступных узлов кластера
          String nodes = hostsToString( aHosts, true );
          logger().error( ERR_NOT_FOUND_AVAILABLE_NODES, nodes );
          throw new TsIllegalStateRtException( ERR_NOT_FOUND_AVAILABLE_NODES, nodes );
        }
        try {
          // Фиксация потока поиска сервера
          lookupApiThread = Thread.currentThread();
          try {
            EJBIdentifier ejbId = new EJBIdentifier( appname, modulename, beanname, distinctname );
            URI uri = new URI( "remote+http://" + hostname + ":" + port.toString() );
            // URI uri = new URI( "cluster://" + hostname + ":" + port.toString() );
            Affinity affinity = Affinity.forUri( uri );
            StatelessEJBLocator<IS5BackendSession> locator = StatelessEJBLocator.create( view, ejbId, affinity );
            // Подготовка и установка контекста EJB для соединения
            EJBClientContext.Builder contextBuilder = new EJBClientContext.Builder();
            // Загрузка поставщиков EJB транспорта
            loadTransportProviders( contextBuilder, RemoteTransportProvider.class.getClassLoader(), logger );
            // Установка перехватчика вызовов
            contextBuilder.addInterceptor( new S5ConnectionInterceptor( S5Connection.this ) );
            // Сборка контекста и установка его по умолчанию для текущего (авторизации) потока
            ejbClientContext = contextBuilder.build();
            // Регистрация слушателя кластера контекста клиента
            S5ClusterNodeUtils.addClusterTopologyListener( ejbClientContext, this );

            ContextManager<EJBClientContext> ejbContextManager = EJBClientContext.getContextManager();
            // 2020-03-19 mvk требуется для работы внутри wildfly (gateways). Иначе возможен AuthenticationException
            ejbContextManager.setClassLoaderDefault( classLoader, ejbClientContext );

            // TODO: 2020-03-19 mvk: как избавиться от setGlobalDefault( ejbClientContext )??? для нескольких соединений
            // ejbContextManager.setClassLoaderDefault( classLoader, ejbClientContext );
            // ejbContextManager.setClassLoaderDefaultSupplier( classLoader, ejbSupplier );
            ejbContextManager.setGlobalDefault( ejbClientContext );
            // ejbContextManager.setGlobalDefaultSupplier( ejbSupplier );
            // ejbContextManager.setGlobalDefaultSupplierIfNotSet( Supplier < Supplier < EJBClientContext >> aSupplier
            // );
            // ejbContextManager.setThreadDefault( ejbClientContext );
            // ejbContextManager.setThreadDefaultSupplier( ejbSupplier );

            // Фабрика имен
            RemoteNamingProviderFactory namingProviderFactory = new RemoteNamingProviderFactory();
            // Поставщик имен
            try( NamingProvider namingProvider =
                namingProviderFactory.createProvider( properties, providerEnvironment ) ) {
              // Создание proxy на точку входа сервера (IServerApi) в контексте клиента
              Pair<SessionID, IS5BackendSession> session =
                  EJBClient.createSessionProxy2( locator, S5Connection.this, namingProvider );
              // Замена прокси на прокси контекста соединения
              // TODO: 2020-03-08 mvk
              // IS5BackendSession proxy =
              // (IS5BackendSession)S5ConnectionInterceptor.replaceContextProxy( S5Connection.this, session.getB() );
              IS5BackendSession proxy = session.getB();
              if( proxy == null ) {
                throw new TsInternalErrorRtException(
                    "lookupClusterRemoteApi(...): EJBClient.createSessionProxy2 return proxy = null " );
              }
              // TODO: 2020-03-16 mvk+
              EJBClient.setStrongAffinity( proxy, new ClusterAffinity( "ejb" ) );

              return Pair.create( session.getA(), proxy );
            }
          }
          finally {
            // Поток поиска сервера завершил свою работу
            lookupApiThread = null;
          }
        }
        catch( RuntimeException e ) {
          throw e;
        }
      };
      // Запуск кода в контексте авторизации
      return authenticationContext.runCallable( callable );
    }
    catch( Exception e ) {
      throw e;
    }
  }

  /**
   * Проводит поиск удаленного API сервера
   *
   * @param aConnection {@link S5Connection} соединение с сервером
   * @param aHost String ip-адрес или имя хоста на котором работает сервер
   * @param aPort Integer порт на котором работает сервер
   * @param aLogin String имя пользователя
   * @param aPassword String пароль пользователя
   * @param aModuleName String имя модуля сервера реализующего API
   * @param aInterfaceName String имя класса интерфейса удаленной ссылки на API
   * @param aBeanName String имя класса бина реализующего API клиента
   * @param aClassLoader {@link ClassLoader} загручик классов для создания proxy API сервера
   * @return {@link InitialContext} контекст имен сервера
   * @throws Exception ошибка создания соединения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws NamingException контекст не найден
   */
  /* 2022-02-03 mvk переход на ejb-client-4.0.44 (wildfly-26.0.1.Final)
   * @formatter:off
  @SuppressWarnings( { "nls", "unchecked", "unused" } )
  private IS5BackendSession lookupRemoteApi( S5Connection aConnection, String aHost, Integer aPort, String aLogin,
      String aPassword, String aModuleName, String aInterfaceName, String aBeanName, ClassLoader aClassLoader )
      throws Exception {
    TsNullArgumentRtException.checkNulls( aConnection, aHost, aPort, aLogin, aPassword, aClassLoader );

    // Подбор параметров соедниения: https://docs.jboss.org/author/display/EJBCLIENT/Overview+of+Client+properties
    // Properties properties = new Properties();
    FastHashtable<String, Object> properties = new FastHashtable<>();
    // properties.put( Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory" );
    // properties.put( Context.SECURITY_PRINCIPAL, aLogin );
    // properties.put( Context.SECURITY_CREDENTIALS, aPassword );
    // properties.put( "remote.connections", "default" );
    properties.put( "remote.connection.default.host", aHost );
    properties.put( "remote.connection.default.port", aPort.toString() );
    properties.put( "remote.connection.default.username", aLogin );
    properties.put( "remote.connection.default.password", aPassword );
    // properties.put( "remote.connection.default.connect.timeout", "1" );

    properties.put( "remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false" );
    properties.put( "remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false" );
    properties.put( "remote.connection.default.connect.options.org.xnio.Options.SASL_DISALLOWED_MECHANISMS",
        "JBOSS-LOCAL-USER" );
    properties.put( "remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false" );

    properties.put( "endpoint.name", "s5.endpoint" );

    // properties.put( "invocation.timeout", "2000" );
    // properties.put( "reconnect.tasks.timeout", "3000" );
    // properties.put( "remote.connection.default.connect.timeout", "3000" );
    // properties.put(
    // "remote.connection.default.connect.options.org.jboss.remoting3.RemotingOptions.HEARTBEAT_INTERVAL",
    // 2000 );

    // properties.put( "endpoint.create.options.org.xnio.Options.THREAD_DAEMON", "false" );
    // properties.put( "remote.connection.default.connect.options.org.xnio.Options.KEEP_ALIVE", "true" );
    // properties.put( "endpoint.create.options.org.xnio.Options.WORKER_TASK_MAX_THREADS", "1" );
    // OptionMap.create( Options.THREAD_DAEMON, false ).toString() );

    // properties.put( "remote.connection.default.protocol", "remoting" );
    // properties.put( "endpoint.name", "client-endpoint" );

    try {
      // WildFlyRootContext ctx = new WildFlyRootContext( properties, ClassLoader.getSystemClassLoader() );
      // if( true ) {
      // IS5BackendSession ejb = (IS5BackendSession)ctx
      // .lookup( "ejb:" + "" + "/" + aModuleName + "/" + aBeanName + "!" + aInterfaceName + "?stateful" );
      // }
      // Формирование контеста аутентификации пользователя
      ProviderEnvironment.Builder builder = new ProviderEnvironment.Builder();
      builder.populateFromEnvironment( properties );
      ProviderEnvironment providerEnvironment = builder.build();
      authenticationContext = providerEnvironment.getAuthenticationContextSupplier().get();

      // 2019-05-30: mvk: try fix server to server connection error. source:
      // https://developer.jboss.org/thread/278645
      AuthenticationConfiguration superUser = AuthenticationConfiguration.empty()
          .setSaslMechanismSelector( SaslMechanismSelector.NONE.addMechanism( "PLAIN" ) ). // //$NON-NLS-1$
          useName( aLogin ).usePassword( aPassword );
      authenticationContext = authenticationContext.with( MatchRule.ALL, superUser );
      AuthenticationContext.getContextManager().setThreadDefault( authenticationContext );

      // Код выполняемый в потоке контекста авторизации
      Callable<IS5BackendSession> callable = () -> {
        // Прокси точки входа на сервер
        Class<IS5BackendSession> view = (Class<IS5BackendSession>)aClassLoader.loadClass( aInterfaceName );
        String appname = "";
        String modulename = aModuleName;
        String beanname = aBeanName;
        String distinctname = "";
        StatelessEJBLocator<IS5BackendSession> locator = null;
        try {
          EJBIdentifier ejbId = new EJBIdentifier( appname, modulename, beanname, distinctname );
          // mvk 2018-10-11
          // locator = StatelessEJBLocator.create( view, ejbId, Affinity.NONE );
          URI uri = new URI( "remote+http://" + aHost + ":" + aPort.toString() );
          Affinity affinity = Affinity.forUri( uri );
          locator = StatelessEJBLocator.create( view, ejbId, affinity );
        }
        catch( RuntimeException e ) {
          throw e;
        }
        try {
          // Подготовка и установка контекста EJB для соединения
          EJBClientContext.Builder contextBuilder = new EJBClientContext.Builder();
          // Загрузка поставщиков EJB транспорта
          loadTransportProviders( contextBuilder, RemoteTransportProvider.class.getClassLoader(), logger );
          // Установка перехватчика вызовов
          contextBuilder.addInterceptor( new S5ConnectionInterceptor( S5Connection.this ) );
          // Сборка контекста и установка его по умолчанию для текущего (авторизации) потока
          ejbClientContext = contextBuilder.build();
          // TODO: ???
          // setEJBContextForCurrentThread();
          if( true ) {
            throw new TsUnderDevelopmentRtException();
          }
          // Фабрика имен
          RemoteNamingProviderFactory namingProviderFactory = new RemoteNamingProviderFactory();
          // Поставщик имен
          try( NamingProvider namingProvider =
              namingProviderFactory.createProvider( properties, providerEnvironment ) ) {
            // Создание proxy на точку входа сервера (IServerApi) в контексте клиента
            Object proxy = EJBClient.createSessionProxy( locator, S5Connection.this, namingProvider );
            // // Замена прокси на прокси контекста соединения
            // IS5BackendSession retValue = (IS5BackendSession)replaceContextProxy( S5Connection.this, proxy );
            return (IS5BackendSession)proxy;
          }
        }
        catch( RuntimeException e ) {
          throw e;
        }
      };
      // Запуск кода в контексте авторизации
      return authenticationContext.runCallable( callable );
    }
    catch( Exception e ) {
      throw e;
    }
  }
  * @formatter:on
  */

  /**
   * Ищет контекст имен сервера
   *
   * @param aConnection {@link S5Connection} соединение с сервером
   * @param aHost String ip-адрес или имя хоста на котором работает сервер
   * @param aPort Integer порт на котором работает сервер
   * @param aLogin String имя пользователя
   * @param aPassword String пароль пользователя
   * @param aModuleName String имя модуля сервера реализующего API
   * @param aInterfaceName String имя класса интерфейса удаленной ссылки на API
   * @param aBeanName String имя класса бина реализующего API клиента
   * @param aClassLoader {@link ClassLoader} загручик классов для создания proxy API сервера
   * @return {@link InitialContext} контекст имен сервера
   * @throws Exception ошибка создания соединения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws NamingException контекст не найден
   */

  @SuppressWarnings( { "nls", "unused" } )
  private static IS5BackendSession lookupRemoteApiByJNDI( S5Connection aConnection, String aHost, Integer aPort,
      String aLogin, String aPassword, String aModuleName, String aInterfaceName, String aBeanName,
      ClassLoader aClassLoader )
      throws Exception {
    TsNullArgumentRtException.checkNulls( aConnection, aHost, aPort, aLogin, aPassword, aClassLoader );

    // Подбор параметров соедниения: https://docs.jboss.org/author/display/EJBCLIENT/Overview+of+Client+properties
    // Properties properties = new Properties();
    FastHashtable<String, Object> properties = new FastHashtable<>();
    // properties.put( Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory" );
    // properties.put( Context.SECURITY_PRINCIPAL, aLogin );
    // properties.put( Context.SECURITY_CREDENTIALS, aPassword );
    // properties.put( "remote.connections", "default" );
    properties.put( "remote.connection.default.host", aHost );
    properties.put( "remote.connection.default.port", aPort.toString() );
    properties.put( "remote.connection.default.username", aLogin );
    properties.put( "remote.connection.default.password", aPassword );
    // properties.put( "remote.connection.default.connect.timeout", "1" );

    properties.put( "remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false" );
    properties.put( "remote.connection.default.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false" );
    properties.put( "remote.connection.default.connect.options.org.xnio.Options.SASL_DISALLOWED_MECHANISMS",
        "JBOSS-LOCAL-USER" );
    properties.put( "remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false" );

    properties.put( "endpoint.name", "s5.endpoint" );

    properties.put( Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming" );
    properties.put( "org.jboss.ejb.client.scoped.context", "true" ); // enable scoping here

    Context context = new InitialContext( properties );
    Context ejbRootNamingContext = (Context)context.lookup( "ejb:" );
    try {
      final IS5BackendSession bean = (IS5BackendSession)ejbRootNamingContext
          .lookup( "ejb:" + "" + "/" + aModuleName + "/" + aBeanName + "!" + aInterfaceName + "?stateful" );
    }
    finally {
      try {
        ejbRootNamingContext.close();
      }
      catch( Exception e ) {
        // nop
      }
      try {
        context.close();
      }
      catch( Exception e ) {
        // nop
      }
    }
    return null;
  }

  // @formatter:off
  /**
   * Ищет контекст имен сервера
   *
   * @param aHost String ip-адрес или имя хоста на котором работает сервер
   * @param aPort Integer порт на котором работает сервер
   * @param aLogin String имя пользователя
   * @param aPassword String пароль пользователя
   * @return {@link InitialContext} контекст имен сервера
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws NamingException контекст не найден
   */
//  @SuppressWarnings( "nls" )
//  private static InitialContext lookupContext( String aHost, Integer aPort, String aLogin, String aPassword,
//      String aModuleName, String aBeanName )
//          throws NamingException {
//    TsNullArgumentRtException.checkNulls( aHost, aPort, aLogin, aPassword );
//    // Подбор параметров соедниения: https://community.jboss.org/thread/198085
//
//    // Работа без файлов jndi.properties, jboss-ejb-client.properties:
//    // http://tapas-tanmoy-bose.blogspot.ru/2013/12/wildfly-ejb-invocations-from-remote.html)
//    if( true ) {
//      // return null;
//    }
//
//    // String providerUrl = format( PROVIDER_URL_FORMAT, aHost, aPort );
//    // Настройка свойств клиента jboss (jboss-ejb-client.properties)
//    Properties clientProperties = new Properties();
//    clientProperties.put( "remote.connections", "default" );
//    clientProperties.put( "remote.connection.default.host", aHost );
//    clientProperties.put( "remote.connection.default.port", aPort.toString() );
//    clientProperties.put( "remote.connection.default.username", aLogin );
//    clientProperties.put( "remote.connection.default.password", aPassword );
//    clientProperties.put( "remote.connection.x1.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT", "false" );
//    clientProperties.put( "remote.connection.x1.connect.options.org.xnio.Options.SASL_POLICY_NOANONYMOUS", "false" );
//    clientProperties.put( "remote.connection.default.connect.options.org.xnio.Options.SASL_DISALLOWED_MECHANISMS",
//        "JBOSS-LOCAL-USER" );
//    clientProperties.put( "remote.connectionprovider.create.options.org.xnio.Options.SSL_ENABLED", "false" );
//    clientProperties.put( "invocation.timeout", "200" );
//    EJBClientContext.setSelector(
//        new ConfigBasedEJBClientContextSelector( new PropertiesBasedEJBClientConfiguration( clientProperties ) ) );
//
//    StatefulEJBLocator<IS5BackendSession> locator;
//    try {
//      locator =
//          EJBClient.createSession( IS5BackendSession.class, "", "tm_server_deploy", "TmServerApiSessionImpl", "" );
//      final IS5BackendSession ejb = EJBClient.createProxy( locator );
//      System.out.println( ejb );
//    }
//    catch( Exception e ) {
//      // TODO Auto-generated catch block
//      e.printStackTrace();
//    }
//
//    if( true ) {
//      return null;
//    }
//    // // Настройка свойств jndi (jndi.properties)
//    // Properties jndiProperties = new Properties();
//    // jndiProperties.put( Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming" );
//    // jndiProperties.put( Context.INITIAL_CONTEXT_FACTORY,
//    // org.jboss.naming.remote.client.InitialContextFactory.class.getName() );
//    // jndiProperties.put( Context.SECURITY_PRINCIPAL, aLogin );
//    // jndiProperties.put( Context.SECURITY_CREDENTIALS, aPassword );
//    // jndiProperties.put( Context.PROVIDER_URL, providerUrl );
//    // jndiProperties.put( "jboss.naming.client.ejb.context", Boolean.valueOf( true ) );
//    // jndiProperties.put( "jboss.naming.client.connect.options.org.xnio.Options.SASL_POLICY_NOPLAINTEXT",
//    // Boolean.FALSE.toString() );
//    // jndiProperties.put( "s5.server.host", aHost );
//    //
//    // return new InitialContext( jndiProperties );
//    return null;
//  }

  /**
   * Ищет в контексте имен соединения ссылку на удаленный интерфейс сервера
   *
   * @param aModuleName String имя модуля приложения
   * @param aJndiName String jndi-имя удаленного интерфейса сервера
   * @return {@link IS5BackendSession} ссылка на удаленный интерфейс соединения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws NamingException ссылка не найдена
   * @throws TsIllegalArgumentRtException найденная ссылка не является ссылкой удаленного интерфейса s5-сервера
   */
  // @formatter:off
//  private static IS5BackendSession lookupRemoteApi( Context aInitialContext, String aModuleName, String aJndiName )
//      throws NamingException {
//    TsNullArgumentRtException.checkNulls( aInitialContext, aModuleName, aJndiName );
//    String app = EMPTY_STRING;
//    String module = aModuleName;
//    String distinct = EMPTY_STRING;
//    String jndiName = aJndiName;
//    String lookupName = format( JNDI_LOOKUP_FORMAT, app, module, distinct, jndiName );
//    Object ref = aInitialContext.lookup( lookupName );
//
//    if( !(ref instanceof IServerApi) ) {
//      String refClassName = (ref != null ? ref.getClass().getName() : MSG_NULL_REF);
//      throw new TsIllegalArgumentRtException( MSG_ERR_NOT_SERVER_API, aJndiName, refClassName );
//    }
//    return (IS5BackendSession)ref;
//  }
  // @formatter:on

  /**
   * Безопасное (без исключений) закрытие удаленного интерфейса сервера
   */
  private void safeCloseRemoteApi() {
    if( remoteBackend == null ) {
      return;
    }
    try {
      try {
        logger.debug( MSG_CALL_REMOTE_API_CLOSE, this );
        if( ejbClientContext != null ) {
          // Дерегистрация слушателя кластера контекста клиента
          S5ClusterNodeUtils.removeClusterTopologyListener( ejbClientContext, this );
        }
        // TODO: проверить актуальность следующего комментария:
        // mvk: сервер закрывает сессию по своим событиям (по разрыву p2p, по таймауту, по "грязным" исключениям
        // сессии). Делать вызов remoteBackend.close() - не нужно (приведет к конкуретному доступу на SFBS (API) и
        // неизбежному появлению исключения или AccessTimeout или NoSuchMethod

        // 2024-03-20 mvk: --- при выгрузке сервера (исчез навсегда) - это может вызвать длительное ожидание
        // (IS5ConnectionParams.FAILURE_TIMEOUT). minimal value = 1 msec
        EJBClient.setInvocationTimeout( remoteBackend, 1, TimeUnit.MILLISECONDS );
        remoteBackend.close();
      }
      catch( Throwable e ) {
        logger.error( e );
      }
    }
    finally {
      // Снимаем возможное состояние прерывания потока
      boolean isInterrupted = Thread.interrupted();
      if( isInterrupted ) {
        logger.warning( "safeCloseRemoteApi(...): clear interrupted state" ); //$NON-NLS-1$
      }
      remoteBackend = null;
      ejbClientContext = null;
    }
  }

  /**
   * Если соединение в состоянии завершения, то выьрасывает {@link TsIllegalStateRtException}.
   */
  private void checkNotClosing() {
    if( closingNow ) {
      throw new TsIllegalStateRtException( ERR_CONNECTION_IS_CLOSING );
    }
  }

  // FIXME goga: не устраивает экранирование исключений только в логер, без вывода на stderr (хотя бы по умолчанию)

  /**
   * Извещает слушателей {@link IS5ConnectionListener#onBeforeActivate(IS5Connection)}.
   * <p>
   * Этот методо никогда не выбрасывает никаких исключений.
   */
  private void fireOnBeforeActiveEvent() {
    for( IS5ConnectionListener listener : threadSafeList( listeners, listenersLock ) ) {
      try {
        listener.onBeforeActivate( this );
      }
      catch( Throwable e ) {
        // Снимаем возможное состояние прерывания потока
        boolean isInterrupted = Thread.interrupted();
        if( isInterrupted ) {
          logger.warning( "S5Connection.fireOnBeforeActiveEvent(). clear interrupted state" ); //$NON-NLS-1$
        }
        logger.error( e );
      }
    }
  }

  /**
   * Извещает слушателей {@link IS5ConnectionListener#onBeforeActivate(IS5Connection)}.
   * <p>
   * Этот методо никогда не выбрасывает никаких исключений.
   */
  private void fireOnAfterActiveEvent() {
    for( IS5ConnectionListener listener : threadSafeList( listeners, listenersLock ) ) {
      try {
        listener.onAfterActivate( this );
      }
      catch( Throwable e ) {
        // Снимаем возможное состояние прерывания потока
        boolean isInterrupted = Thread.interrupted();
        if( isInterrupted ) {
          logger.warning( "S5Connection.fireOnAfterActiveEvent(). clear interrupted state" ); //$NON-NLS-1$
        }
        logger.error( e );
      }
    }
  }

  /**
   * Извещает слушателей {@link IS5ConnectionListener#onBeforeDeactivate(IS5Connection)}.
   * <p>
   * Этот методо никогда не выбрасывает никаких исключений.
   */
  private void fireOnBeforeDeActiveEvent() {
    for( IS5ConnectionListener listener : threadSafeList( listeners, listenersLock ) ) {
      try {
        listener.onBeforeDeactivate( this );
      }
      catch( Throwable e ) {
        // Снимаем возможное состояние прерывания потока
        boolean isInterrupted = Thread.interrupted();
        if( isInterrupted ) {
          logger.warning( "S5Connection.fireOnBeforeDeActiveEvent(). clear interrupted state" ); //$NON-NLS-1$
        }
        logger.error( e );
      }
    }
  }

  /**
   * Извещает слушателей {@link IS5ConnectionListener#onAfterDeactivate(IS5Connection)}.
   * <p>
   * Этот методо никогда не выбрасывает никаких исключений.
   */
  private void fireOnAfterDeActiveEvent() {
    for( IS5ConnectionListener listener : threadSafeList( listeners, listenersLock ) ) {
      try {
        listener.onAfterDeactivate( this );
      }
      catch( Throwable e ) {
        // Снимаем возможное состояние прерывания потока
        boolean isInterrupted = Thread.interrupted();
        if( isInterrupted ) {
          logger.warning( "S5Connection.fireOnAfterDeActiveEvent(). clear interrupted state" ); //$NON-NLS-1$
        }
        logger.error( e );
      }
    }
  }

  /**
   * Извещает слушателей {@link IS5ConnectionListener#onBeforeConnect(IS5Connection)}.
   * <p>
   * Этот метод никогда не выбрасывает никаких исключений.
   */
  private void fireOnBeforeConnectEvent() {
    for( IS5ConnectionListener listener : threadSafeList( listeners, listenersLock ) ) {
      try {
        listener.onBeforeConnect( this );
      }
      catch( Throwable e ) {
        // Снимаем возможное состояние прерывания потока
        boolean isInterrupted = Thread.interrupted();
        if( isInterrupted ) {
          logger.warning( "S5Connection.fireOnBeforeConnectEvent(). clear interrupted state" ); //$NON-NLS-1$
        }
        logger.error( e );
      }
    }
  }

  /**
   * Извещает слушателей {@link IS5ConnectionListener#onAfterDiscover(IS5Connection)}.
   * <p>
   * Этот метод никогда не выбрасывает никаких исключений.
   */
  private void fireOnAfterDiscoverEvent() {
    for( IS5ConnectionListener listener : threadSafeList( listeners, listenersLock ) ) {
      try {
        listener.onAfterDiscover( this );
      }
      catch( Throwable e ) {
        // Снимаем возможное состояние прерывания потока
        boolean isInterrupted = Thread.interrupted();
        if( isInterrupted ) {
          logger.warning( "S5Connection.fireOnAfterDiscoverEvent(). clear interrupted state" ); //$NON-NLS-1$
        }
        logger.error( e );
      }
    }
  }

  /**
   * Извещает слушателей {@link IS5ConnectionListener#onAfterConnect(IS5Connection)}.
   * <p>
   * Этот методо никогда не выбрасывает никаких исключений.
   */
  private void fireOnAfterConnectEvent() {
    RuntimeException error = null;
    for( IS5ConnectionListener listener : threadSafeList( listeners, listenersLock ) ) {
      try {
        listener.onAfterConnect( this );
      }
      catch( RuntimeException e ) {
        // Снимаем возможное состояние прерывания потока
        boolean isInterrupted = Thread.interrupted();
        if( isInterrupted ) {
          logger.warning( "S5Connection.fireOnAfterConnectEvent(). clear interrupted state" ); //$NON-NLS-1$
        }
        logger.error( e );
        if( error == null ) {
          // Запоминаем только первую ошибку
          error = e;
        }
      }
    }
    if( error != null ) {
      throw error;
    }
  }

  /**
   * Извещает слушателей {@link IS5ConnectionListener#onBeforeDisconnect(IS5Connection)}.
   * <p>
   * Этот методо никогда не выбрасывает никаких исключений.
   */
  private void fireBeforeDisconnectEvent() {
    for( IS5ConnectionListener listener : threadSafeList( listeners, listenersLock ) ) {
      try {
        listener.onBeforeDisconnect( this );
      }
      catch( Throwable e ) {
        // Снимаем возможное состояние прерывания потока
        boolean isInterrupted = Thread.interrupted();
        if( isInterrupted ) {
          logger.warning( "S5Connection.fireBeforeDisconnectEvent(). clear interrupted state" ); //$NON-NLS-1$
        }
        logger.error( e );
      }
    }
  }

  /**
   * Извещает слушателей {@link IS5ConnectionListener#onAfterDisconnect(IS5Connection)}.
   * <p>
   * Этот методо никогда не выбрасывает никаких исключений.
   */
  private void fireAfterDisconnectEvent() {
    for( IS5ConnectionListener listener : threadSafeList( listeners, listenersLock ) ) {
      try {
        listener.onAfterDisconnect( this );
      }
      catch( Throwable e ) {
        // Снимаем возможное состояние прерывания потока
        boolean isInterrupted = Thread.interrupted();
        if( isInterrupted ) {
          logger.warning( "S5Connection.fireAfterDisconnectEvent(). clear interrupted state" ); //$NON-NLS-1$
        }
        logger.error( e );
      }
    }
  }

  /**
   * Зазгрузка в построитель контекста EJB доступных транспортных поставщиков
   * <p>
   * Код метода заимствован из одноименного метода класса ConfigurationBasedEJBClientContextSelector библиотеки
   * ejb-client-4.0.11.Final
   *
   * @param aBuilder {@link Builder} построитель контекста EJB
   * @param aClassLoader загрузчик классов
   * @param aLogger {@link ILogger} журнал работы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void loadTransportProviders( final EJBClientContext.Builder aBuilder, final ClassLoader aClassLoader,
      ILogger aLogger ) {
    TsNullArgumentRtException.checkNull( aBuilder );
    TsNullArgumentRtException.checkNull( aClassLoader );
    TsNullArgumentRtException.checkNull( aLogger );
    final ServiceLoader<EJBTransportProvider> serviceLoader =
        ServiceLoader.load( EJBTransportProvider.class, aClassLoader );
    Iterator<EJBTransportProvider> iterator = serviceLoader.iterator();
    int transportCount = 0;
    for( ;; ) {
      try {
        if( !iterator.hasNext() ) {
          break;
        }
        final EJBTransportProvider transportProvider = iterator.next();
        aBuilder.addTransportProvider( transportProvider );
        transportCount++;
      }
      catch( ServiceConfigurationError e ) {
        // Ошибка загрузки службы транспорта
        aLogger.error( e, ERR_TRANSOPRT_LOAD );
      }
      if( transportCount == 0 ) {
        // Не найдены поставщики транспорта EJB
        throw new TsIllegalStateRtException( ERR_TRANSOPRT_NOT_FOUND );
      }
    }
  }

  /**
   * Создание описания топологии кластеров открытого соединения в указанном контексте клиента
   *
   * @param aContext {@link EJBClientContext} контекст клиента
   * @return {@link S5ClusterTopology} описание топологии
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static S5ClusterTopology createClusterTopology( EJBClientContext aContext ) {
    TsNullArgumentRtException.checkNull( aContext );
    Map<String, List<Pair<String, InetSocketAddress>>> nodesByClusters = S5ClusterNodeUtils.getNodeAddrs( aContext );
    IListEdit<IS5ClusterNodeInfo> nodes = new ElemLinkedList<>();
    for( String clusterName : nodesByClusters.keySet() ) {
      for( Pair<String, InetSocketAddress> node : nodesByClusters.get( clusterName ) ) {
        String nodeName = node.getA();
        InetSocketAddress addr = node.getB();
        nodes.add( new S5ClusterNodeInfo( clusterName, nodeName, addr.getHostString(), addr.getPort() ) );
      }
    }
    return new S5ClusterTopology( nodes );
  }

  /**
   * Обновление топологии кластеров доступных клиенту
   */
  private void updateClusterTopology() {
    try {
      if( state == EConnectionState.CONNECTED ) {
        // Обновление топологии кластеров доступных клиенту
        logger().info( MSG_UPDATE_CLUSTER_TOPOLOGY, this );
        S5ClusterTopology topology = createClusterTopology( ejbClientContext );
        // Отправка топологии через callback
        callbackReader.updateClusterTopology( topology );
      }
    }
    catch( Throwable e ) {
      logger().error( e );
    }
  }

  /**
   * Проводит проверку доступности узла кластера сервера {@link URL}.
   * <p>
   * Для проверки используется отправка пакетов 'HEAD request' и обработка кода ответа в диапазоне 200-399.
   *
   * @param aHostname String имя хоста на котором работает узел
   * @param aPort int номер порта на котором работает узел
   * @param aTimeout int таймаут в мсек ожидания узла
   * @param aLogger {@link ILogger} журнал работы
   * @return <b>true</b> указанный узел доступен для обращения; <b>false</b> указанный узел недоступен для обращения.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static boolean availableNode( String aHostname, int aPort, int aTimeout, ILogger aLogger ) {
    TsNullArgumentRtException.checkNull( aHostname );
    TsNullArgumentRtException.checkNull( aLogger );
    try( Socket socket = new Socket() ) {
      socket.setReuseAddress( true );
      SocketAddress sa = new InetSocketAddress( aHostname, aPort );
      socket.connect( sa, aTimeout );
      return true;
    }
    catch( @SuppressWarnings( "unused" ) Throwable e ) {
      aLogger.debug( "Узел %s:%d недоступен", aHostname, Integer.valueOf( aPort ) ); //$NON-NLS-1$
    }
    return false;
  }

  /**
   * Возвращает текстовое представление списка узлов кластера
   *
   * @param aNodes {@link List}&lt;{@link Pair}&lt;String,{@link InetSocketAddress}&gt;&gt; список узлов кластера
   * @return String текстовое представление списка
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static String nodesToStr( List<Pair<String, InetSocketAddress>> aNodes ) {
    TsNullArgumentRtException.checkNulls( aNodes );
    StringBuilder retValue = new StringBuilder();
    for( int index = 0, n = aNodes.size(); index < n; index++ ) {
      Pair<String, InetSocketAddress> pair = aNodes.get( index );
      String nodeName = pair.getA();
      InetSocketAddress addr = pair.getB();
      retValue.append( format( "%s:[%s:%d]", nodeName, addr.getHostString(), Integer.valueOf( addr.getPort() ) ) ); //$NON-NLS-1$
      if( index + 1 < n ) {
        retValue.append( ',' );
      }
    }
    return retValue.toString();
  }

  /**
   * Возвращает текстовое представление списка строк
   *
   * @param aItems {@link List}&lt;String&gt; список строк
   * @return String текстовое представление списка
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static String toStr( List<String> aItems ) {
    TsNullArgumentRtException.checkNull( aItems );
    StringBuilder retValue = new StringBuilder();
    for( int index = 0, n = aItems.size(); index < n; index++ ) {
      retValue.append( aItems.get( index ) );
      if( index + 1 < n ) {
        retValue.append( ',' );
      }
    }
    return retValue.toString();
  }

  /**
   * Возвращает {@link Affinity}-адрес для подключения к "одиночному", не кластерному серверу
   *
   * @return {@link Affinity} адрес. null: не определен.
   */
  Affinity getStandaloneAffinityOrNull() {
    return standaloneAffinity;
  }
}
