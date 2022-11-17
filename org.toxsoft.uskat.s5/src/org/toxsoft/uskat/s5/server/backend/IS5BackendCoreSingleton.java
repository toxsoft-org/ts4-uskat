package org.toxsoft.uskat.s5.server.backend;

import javax.ejb.Local;
import javax.persistence.EntityManagerFactory;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkBackend;
import org.toxsoft.uskat.core.backend.api.ISkBackendInfo;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.s5.server.backend.impl.IS5BackendCoreInterceptor;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterManager;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionManager;
import org.toxsoft.uskat.s5.server.startup.IS5InitialImplementSingleton;
import org.toxsoft.uskat.s5.server.statistics.IS5StatisticCounter;
import org.toxsoft.uskat.s5.server.transactions.IS5TransactionManagerSingleton;

/**
 * Локальный интерфейс синглетона предоставляющего ядро бекенда s5-сервера.
 *
 * @author mvk
 */
@Local
public interface IS5BackendCoreSingleton
    extends IS5BackendSupportRegistry, IS5FrontendAttachable {

  /**
   * Возвращает информацию о сервере.
   *
   * @return {@link ISkBackendInfo} информация о сервере.
   */
  ISkBackendInfo getInfo();

  /**
   * Возвращает фабрика менеджеров постоянства Application Managed Entity Manager (используемых для многопоточной
   * записи)
   * <p>
   * Источники(persitent context + transaction + EntityManager):
   * http://www.kumaranuj.com/2013/06/jpa-2-entitymanagers-transactions-and.html
   * https://docs.oracle.com/cd/E19798-01/821-1841/bnbra/index.html
   *
   * @return {@link EntityManagerFactory} фабрика менеджеров постоянства
   */
  EntityManagerFactory entityManagerFactory();

  /**
   * Возвращает менеджер транзакций
   *
   * @return {@link IS5TransactionManagerSingleton} менеджер транзакций
   */
  IS5TransactionManagerSingleton txManager();

  /**
   * Возвращает менджер кластера s5-сервера
   *
   * @return {@link IS5ClusterManager} менеджер кластера
   */
  IS5ClusterManager clusterManager();

  /**
   * Возвращает менджер сессий s5-сервера
   *
   * @return {@link IS5SessionManager} менеджер сессий
   */
  IS5SessionManager sessionManager();

  /**
   * Возвращает начальную, неизменяемую, проектно-зависимую конфигурация реализации бекенда сервера
   *
   * @return {@link IS5InitialImplementSingleton} конфигурация
   */
  IS5InitialImplementSingleton initialConfig();

  /**
   * Возвращает состояние бекенда.
   * <p>
   * Бекенд, по факту своего существования, уже обеспечивает открытие соединения <code>ISkConnection</code>. То есть,
   * если существует ссылка на имплементацию этого интерфейса {@link ISkBackend}, то соединения с uskat открыто. Но
   * открытое состояние может быть неактивно (например, не связи с сервером для серверного бекенда). В таком случае
   * говорится, что бекенд в неактивном состоянии, и {@link #isActive()} = <code>false</code>. В общем случае,
   * допускается работа с бекендом в нективном состоянии.
   * <p>
   * Надо отметить, что не существует понятия "активировать" или "деактивировать" бекенд. Бекенлд, по факту
   * существования стремится быть активным. Неактивное состояние может наступить только по внешним причинам (например,
   * отсутствие связи с сервером или отсутствие файла-хранилище). При этом бекенд сам предпринимает меры для устранения
   * причин и скорейшей активации.
   * <p>
   * Допускается создание бекенда в нективном состоянии.
   *
   * @return boolean - признак активного (работоспособного) состояния бекенда
   */
  boolean isActive();

  /**
   * Возвращает соединение с локальным узлом сервера
   * <p>
   * Соединение может быть использована различными компонентами сервера в разделяемом режиме
   *
   * @return {@link ISkConnection} потокобезопасное соединение с сервером.
   * @throws TsIllegalArgumentRtException ядро поддержки не активно ({@link #isActive()} == false)
   */
  ISkConnection getConnection();

  /**
   * Устанавливает соединение с локальным узлом сервера
   *
   * @param aConnection {@link ISkConnection} соединение с локальным узлом сервера
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setConnection( ISkConnection aConnection );

  /**
   * Статистика работы узла сервера
   *
   * @return {@link IS5StatisticCounter} счетчик статистики. null: еще неопределен
   */
  IS5StatisticCounter statisticCounter();

  /**
   * Добавляет перехватчика операций ядра бекенда.
   * <p>
   * Если такой перехватчик уже зарегистрирован, то обновляет его приоритет.
   *
   * @param aInterceptor {@link IS5BackendCoreInterceptor} перехватчик операций
   * @param aPriority int приоритет перехватчика. Чем меньше значение, тем выше приоритет.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addBackendCoreInterceptor( IS5BackendCoreInterceptor aInterceptor, int aPriority );

  /**
   * Удаляет перехватчика операций ядра бекенда.
   * <p>
   * Если такой перехватчик не зарегистрирован, то метод ничего не делает.
   *
   * @param aInterceptor {@link IS5BackendCoreInterceptor} перехватчик операций
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeBackendCoreInterceptor( IS5BackendCoreInterceptor aInterceptor );
}
