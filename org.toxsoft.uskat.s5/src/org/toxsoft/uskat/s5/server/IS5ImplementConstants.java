package org.toxsoft.uskat.s5.server;

import javax.ejb.StatefulTimeout;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.transaction.UserTransaction;

import org.toxsoft.uskat.s5.client.local.S5LocalConnectionSingleton;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSession;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSessionControl;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSession;
import org.toxsoft.uskat.s5.server.backend.supports.clobs.S5BackendClobsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.commands.impl.S5BackendCommandSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.events.impl.S5BackendEventSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.S5BackendHistDataSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.links.S5BackendLinksSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.S5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectEntity;
import org.toxsoft.uskat.s5.server.backend.supports.queries.impl.S5BackendQueriesSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.S5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.cluster.S5ClusterManager;
import org.toxsoft.uskat.s5.server.sessions.S5SessionManager;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionMessenger;
import org.toxsoft.uskat.s5.server.startup.*;
import org.toxsoft.uskat.s5.server.transactions.S5TransactionManager;
import org.toxsoft.uskat.s5.utils.IS5HardConstants;

/**
 * Константы реализации s5-сервера
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
public interface IS5ImplementConstants
    extends IS5HardConstants {

  /**
   * Максимальный размер строкового идентификатора
   * <p>
   * Внимание, особенности реализации! <br>
   * Идентификаторы используются при составлении уникальных ключей, например, "classId,strid" в {@link S5ObjectEntity}.
   * Если суммарная длина ключа превысит границы определяемые СУБД, то создание ключа будет провалено.
   */
  int STRID_LENGTH_MAX = 255;

  // ------------------------------------------------------------------------------------
  // Таймауты s5-backend
  //
  /**
   * Таймаут(мсек) {@link StatefulTimeout} по которому SFSB без обращения к ним переходят в невалидное состояние. -1:
   * Бесконечно
   */
  long STATEFULL_TIMEOUT = 2 * 60 * 1000;

  /**
   * Таймаут(мсек) в котором сервер работает в режиме перегрузки после запуска
   */
  long STARTUP_OVERLOAD_TIMEOUT = 10 * 60 * 1000;

  /**
   * Таймаут(мсек) SFSB в течении которого при потоки ожидают освобождение бина
   */
  long ACCESS_TIMEOUT_DEFAULT = 10 * 1000;

  /**
   * Таймаут(мсек) SFSB в течении которого он должны ответить на {@link IS5BackendSessionControl#verify()} прежде чем
   * будет вызвающий код будет определит, что SFSB занят
   */
  long CHECK_ACCESS_TIMEOUT_DEFAULT = 100;

  /**
   * Таймаут(мсек) SFSB в течении которого он должны ответить на {@link IS5BackendSessionControl#removeAsync()} прежде
   * чем будет вызвающий код будет определит, что SFSB занят
   */
  long REMOVE_ACCESS_TIMEOUT_DEFAULT = 100;

  /**
   * Таймаут(мсек) между выполнением фоновых задач службы по умолчанию
   */
  long DEFAULT_JOB_TIMEOUT = 100;

  /**
   * Таймаут(мсек) между выполнением фоновых синглетона backend сервера
   */
  long BACKEND_JOB_TIMEOUT = 100;

  /**
   * Таймаут(мсек) между выполнением фоновых задач службы ICurrDataService
   */
  long CURRDATA_JOB_TIMEOUT = 100;

  /**
   * Таймаут(мсек) проверки целостности списка сессий
   */
  long SESSIONS_JOB_TIMEOUT = 5 * 60 * 1000;

  // ------------------------------------------------------------------------------------
  // Предопределенные имена синглетонов для определения зависимостемй между модулями сервера
  //
  /**
   * 1. Синглетон предоставляющий доступ к транзакциям.
   */
  String TRANSACTION_MANAGER_SINGLETON = S5TransactionManager.TRANSACTION_MANAGER_ID;

  /**
   * 2. Синглетон заргружающий конфигурацию других синглетонов
   */
  String INITIAL_SINGLETON = S5InitialSingleton.INITIAL_ID;

  /**
   * 3. Синглетон предоставляющий начальную, неизменяемую, конфигурацию реализации проекта.
   */
  String PROJECT_INITIAL_IMPLEMENT_SINGLETON = S5InitialImplementSingleton.PROJECT_INITIAL_IMPLEMENT_ID;

  /**
   * 4. Синглетон предоставляющий доступ к API кластера.
   */
  String CLUSTER_MANAGER_SINGLETON = S5ClusterManager.CLUSTER_MANAGER_ID;

  /**
   * 5. Синглетон предоставляющий доступ к API кластера.
   */
  String SESSION_MANAGER_SINGLETON = S5SessionManager.SESSION_MANAGER_ID;

  /**
   * 6. Синглетон предоставляющий ядро бекенда (контейнер модулей поддержки функциональности).
   */
  String BACKEND_CORE_SINGLETON = S5BackendCoreSingleton.BACKEND_CORE_ID;

  /**
   * 7. Синглетон поддержки доступа к системному описанию
   */
  String BACKEND_SYSDESCR_SINGLETON = S5BackendSysDescrSingleton.BACKEND_SYSDESCR_ID;

  /**
   * 8. Синглетон поддержки доступа к объектам
   */
  String BACKEND_OBJECTS_SINGLETON = S5BackendObjectsSingleton.BACKEND_OBJECTS_ID;

  /**
   * 9. Синглетон поддержки доступа к связям между объектами
   */
  String BACKEND_LINKS_SINGLETON = S5BackendLinksSingleton.BACKEND_LINKS_ID;

  /**
   * 10. Синглетон поддержки доступа к большим данным
   */
  String BACKEND_LOBS_SINGLETON = S5BackendClobsSingleton.BACKEND_CLOBS_ID;

  /**
   * 11. Синглетон поддержки доступа к событиям
   */
  String BACKEND_EVENTS_SINGLETON = S5BackendEventSingleton.BACKEND_EVENTS_ID;

  /**
   * 12. Синглетон поддержки доступа к командам
   */
  String BACKEND_COMMANDS_SINGLETON = S5BackendCommandSingleton.BACKEND_COMMANDS_ID;

  /**
   * 13. Синглетон поддержки доступа к текущим данным
   */
  String BACKEND_CURRDATA_SINGLETON = S5BackendHistDataSingleton.BACKEND_HISTDATA_ID;

  /**
   * 14. Синглетон поддержки доступа к хранимым данным
   */
  String BACKEND_HISTDATA_SINGLETON = S5BackendHistDataSingleton.BACKEND_HISTDATA_ID;

  /**
   * 15. Синглетон поддержки доступа к запросам данных
   */
  String BACKEND_QUERIES_SINGLETON = S5BackendQueriesSingleton.BACKEND_QUERIES_ID;

  /**
   * 16. Синглетон предоставляющий локальное соединение.
   */
  String LOCAL_CONNECTIION_SINGLETON = S5LocalConnectionSingleton.LOCAL_CONNECTION_ID;

  /**
   * 17. Синглетон осуществляющий начальную инициализацию системного описания проекта.
   */
  String PROJECT_INITIAL_SYSDESCR_SINGLETON = S5InitialSysdescrSingleton.PROJECT_INITIAL_SYSDESCR_ID;

  /**
   * Имя модуля реализующего сервер проекта
   */
  String BACKEND_SERVER_MODULE_ID = "skat-backend-deploy";

  /**
   * Имя класса (full) представляющий интерфейс для подключения к серверу
   */
  String BACKEND_SESSION_INTERFACE = IS5BackendSession.class.getName();

  /**
   * Имя класса (simple) реализующий сессию
   */
  String BACKEND_SESSION_IMPLEMENTATION = S5BackendSession.class.getSimpleName();

  // ------------------------------------------------------------------------------------
  // Ресурсы s5-backend и их JNDI
  //
  /**
   * Атрибут: IP-адрес
   */
  String JBOSS_ATTR_INET_ADDRESS = "inet-address";

  /**
   * Атрибут: IP-адрес
   */
  String JBOSS_ATTR_RESOLVED_ADDRESS = "resolvedAddress";

  /**
   * Атрибут: порт
   */
  String JBOSS_ATTR_PORT = "port";

  /**
   * Атрибут: порт
   */
  String JBOSS_ATTR_BOUND_PORT = "boundPort";

  /**
   * Имя свойства представляющего имя узла кластера на котором запущен сервер
   */
  String JBOSS_PUBLIC_INTERFACE = "jboss.as:interface=public"; //$NON-NLS-1$

  /**
   * Сокет http
   */
  String JBOSS_SOCKET_BINDING_HTTP = "jboss.as:socket-binding-group=standard-sockets,socket-binding=http";

  /**
   * Сокет PAS
   */
  String JBOSS_SOCKET_BINDING_PAS = "jboss.as:socket-binding-group=standard-sockets,socket-binding=pas";

  /**
   * Сокет messaging
   */
  String JBOSS_SOCKET_BINDING_MESSAGING = "jboss.as:socket-binding-group=standard-sockets,socket-binding=http";

  /**
   * Имя свойства представляющего имя узла кластера на котором запущен сервер
   */
  String JBOSS_NODE_NAME = "jboss.node.name"; //$NON-NLS-1$

  /**
   * JNDI-имя группы в которой работает узел кластера s5-сервера
   */
  String CLUSTER_GROUP = "java:jboss/clustering/group/default";

  /**
   * JNDI-имя кэша целочисленных индексов текущих данных объектов
   */
  String TRANSACTION_MANAGER = "java:/TransactionManager";

  /**
   * JNDI-имя фабрика диспетчера команд выполняемых узлами кластера
   */
  String CLUSTER_COMMAND_DISPATCHER_FACTORY = "java:jboss/clustering/dispatcher/default";

  /**
   * JNDI-имя кэша открытых сессий
   */
  String INFINISPAN_CACHE_OPEN_SESSIONS = "java:jboss/infinispan/cache/s5caches/open_sessions";

  /**
   * JNDI-имя кэша открытых сессий
   */
  String INFINISPAN_CACHE_CLOSED_SESSIONS = "java:jboss/infinispan/cache/s5caches/closed_sessions";

  /**
   * JNDI-имя кэша данных обратных вызовов сессий {@link S5SessionMessenger}
   */
  String INFINISPAN_CACHE_CALLBACK_CONFIGS = "java:jboss/infinispan/cache/s5caches/callback_configs";

  /**
   * JNDI-имя кэша значений текущих данных объектов
   */
  String INFINISPAN_CACHE_CURRDATA_VALUES = "java:jboss/infinispan/cache/s5caches/currdata_values";

  /**
   * JNDI-имя кэша идентификаторов данных последовательности событий
   */
  String INFINISPAN_CACHE_EVENT_GWIDS = "java:jboss/infinispan/cache/s5caches/event_gwids"; //$NON-NLS-1$

  /**
   * JNDI-имя кэша идентификаторов данных последовательности команд
   */
  String INFINISPAN_CACHE_CMD_GWIDS = "java:jboss/infinispan/cache/s5caches/cmd_gwids"; //$NON-NLS-1$

  /**
   * JNDI-имя кэша истории состояний выполняемых команд
   */
  String INFINISPAN_CACHE_CMD_STATES = "java:jboss/infinispan/cache/s5caches/cmd_states"; //$NON-NLS-1$

  /**
   * JNDI-имя кэша идентификаторов данных последовательности хранимых данных
   */
  String INFINISPAN_CACHE_HISTDATA_GWIDS = "java:jboss/infinispan/cache/s5caches/histdata_gwids"; //$NON-NLS-1$

  /**
   * JNDI-имя исполнителя асинхронных задач объединения блоков по умолчанию {@link ManagedExecutorService}
   */
  // String DEFAULT_MANAGED_EXECUTOR_SERVICE_JNDI = "java:comp/DefaultManagedExecutorService"; //$NON-NLS-1$
  // String DEFAULT_MANAGED_EXECUTOR_SERVICE_JNDI = "java:jboss/ee/concurrency/executor/default"; //$NON-NLS-1$

  /**
   * JNDI-имя исполнителя асинхронных задач фоновых потоков служб сервера {@link ManagedExecutorService}
   */
  String DO_JOB_EXECUTOR_JNDI = "java:jboss/ee/concurrency/executor/s5/service/dojob"; //$NON-NLS-1$

  /**
   * JNDI-имя объекта управления транзакциями {@link UserTransaction}
   */
  String USER_TRANSACTION_JNDI = "java:jboss/UserTransaction"; //$NON-NLS-1$

  /**
   * Формат JNDI расширения бекенда
   * <ul>
   * <li>1. beanName;</li>
   * <li>2. interfaceFullQualifiedName.</li>
   * </ul>
   */
  String BACKEND_ADDON_JNDI = "java:global/" + BACKEND_SERVER_MODULE_ID + "/%s!%s";

  /**
   * Имя класса исключения поднимаемого при откатах транзакции
   */
  String ARJUNA_ROLLBACK_EXCEPTION = "com.arjuna.ats.jta.exceptions.RollbackException"; //$NON-NLS-1$
}
