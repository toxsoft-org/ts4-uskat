package org.toxsoft.uskat.s5.server.backend.supports.core;

import javax.ejb.*;
import javax.persistence.*;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.s5.server.backend.*;
import org.toxsoft.uskat.s5.server.cluster.*;
import org.toxsoft.uskat.s5.server.sessions.*;
import org.toxsoft.uskat.s5.server.startup.*;
import org.toxsoft.uskat.s5.server.statistics.*;
import org.toxsoft.uskat.s5.server.transactions.*;

/**
 * Локальный интерфейс синглетона предоставляющего ядро бекенда s5-сервера.
 *
 * @author mvk
 */
@Local
public interface IS5BackendCoreSingleton
    extends IS5BackendSupportRegistry, IS5FrontendAttachable, IS5BackendEventer {

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
   * Установить текущее состояние сервера.
   * <p>
   * Если у сервера уже установлено указанное состояние, то ничего не делает.
   *
   * @param aMode {@link ES5ServerMode} новое состояние сервера.
   * @return <b>true</b> состояние сервера изменено или уже было установлено ранее; <b>false</b> один из модулей сервера
   *         запретил изменение состояния.
   */
  boolean setMode( ES5ServerMode aMode );

  /**
   * Возвращает текущее состояние сервера.
   *
   * @return {@link ES5ServerMode} текущее состояние сервера.
   */
  ES5ServerMode mode();

  /**
   * Возвращает общее(разделяемое между модулями сервера) локальное соединение.
   * <p>
   * Следует учитывать природу общего(разделяемого между модулями системы) соединения - в жизненном цикле сервера оно
   * создается при запуске сервера и завершается при его остановке.
   *
   * @return {@link ISkConnection} потокобезопасное соединение с сервером.
   * @throws TsIllegalArgumentRtException ядро поддержки не активно ({@link #isActive()} == false)
   */
  ISkConnection getSharedConnection();

  /**
   * Устанавливает общее(разделяемое между модулями сервера) локальное соединение.
   * <p>
   * Следует учитывать природу общего(разделяемого между модулями системы) соединения - в жизненном цикле сервера оно
   * создается при запуске сервера и завершается при его остановке.
   *
   * @param aConnection {@link ISkConnection} соединение с локальным узлом сервера
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsItemAlreadyExistsRtException общее соединение уже установлено
   */
  void setSharedConnection( ISkConnection aConnection );

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
