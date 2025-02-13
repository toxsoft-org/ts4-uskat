package org.toxsoft.uskat.s5.server.sessions;

import javax.ejb.Local;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.s5.common.info.IS5SessionsInfos;
import org.toxsoft.uskat.s5.common.sessions.*;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionMessenger;
import org.toxsoft.uskat.s5.server.statistics.IS5StatisticCounter;

/**
 * Управление сессиями пользователей сервера
 *
 * @author mvk
 */
@Local
public interface IS5SessionManager {

  /**
   * Генерация идентификатора для новой сессии
   *
   * @param aModuleName String имя модуля создающего сессию
   * @param aModuleNode String имя узла на котором запущен модуль создающий сессию
   * @return {@link Skid} идентификатор сессии {@link ISkSession}
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  Skid generateSessionID( String aModuleName, String aModuleNode );

  /**
   * Возвращает текущую информацию о сессиях
   *
   * @return {@link IS5SessionsInfos} информация о сессиях
   */
  IS5SessionsInfos getInfos();

  /**
   * Возвращает текущее количество открытых сессий
   * <p>
   * Реализация {@link #openSessionCount()} более легковесна, чем {@link #openSessions()}.
   *
   * @return int количество открытых сессий
   */
  int openSessionCount();

  /**
   * Возвращает список открытых сессий
   *
   * @return {@link IList}&lt;{@link IS5SessionInfo}&gt; список сессий
   */
  IList<S5SessionData> openSessions();

  /**
   * Возвращает список закрытых сессий
   *
   * @return {@link IList}&lt;{@link IS5SessionInfo}&gt; список сессий
   */
  IList<S5SessionData> closedSessions();

  /**
   * Возвращает данные сессии пользователя
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @return {@link S5SessionData} данные сессии пользователя. null: сессия не существует
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5SessionData findSessionData( Skid aSessionID );

  /**
   * Регистрация сессии с удаленным пользователем
   * <p>
   * Если сессия была уже зарегистрирована, то обновляются ее параметры
   *
   * @param aSession {@link S5SessionData} сессия пользователя
   * @return boolean <b>true</b> сессия создана;<b>false</b> сессия была создана ранее, параметры обновлены
   * @throws TsNullArgumentRtException аргумент = null
   */
  boolean createRemoteSession( S5SessionData aSession );

  /**
   * Регистрация сессии с локальным пользователем
   * <p>
   * Если сессия была уже зарегистрирована, то обновляются ее параметры
   *
   * @param aSession {@link S5LocalSession} сессия пользователя
   * @return boolean <b>true</b> сессия создана;<b>false</b> сессия была создана ранее, параметры обновлены
   * @throws TsNullArgumentRtException аргумент = null
   */
  boolean createLocalSession( S5LocalSession aSession );

  /**
   * Сохраняет данные сессии в кластере
   * <p>
   * Метод сохраняет данные в кэше открытых сессий (infinispan) и оповещает все узлы кластера об изменении данных сессии
   *
   * @param aSessionData {@link S5SessionData} данные сессии пользователя
   * @throws TsNullArgumentRtException аргумент = null
   */
  void writeSessionData( S5SessionData aSessionData );

  // 2022-07-09 mvk
  // /**
  // * Определяет описание открытой сессии локального пользователя
  // * <p>
  // * Если сессия не существует, то ничего не делает
  // *
  // * @param aSession {@link S5LocalSession} сессия пользователя
  // * @throws TsNullArgumentRtException аргумент = null
  // */
  // void updateLocalSession( S5LocalSession aSession );

  /**
   * Удаление сессии удаленного пользователя
   * <p>
   * Если сессии нет, то ничего не делает
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @throws TsNullArgumentRtException аргумент = null
   */
  void closeRemoteSession( Skid aSessionID );

  /**
   * Удаление сессии локального пользователя
   * <p>
   * Если сессии нет, то ничего не делает
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @throws TsNullArgumentRtException аргумент = null
   */
  void closeLocalSession( Skid aSessionID );

  /**
   * Возвращает приемопередатчик сообщений для указанной сессии
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @return {@link S5SessionMessenger} приемопередатчик сообщений. null: не найден
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5SessionMessenger findMessenger( Skid aSessionID );

  /**
   * Возвращает приемопередатчик сообщений для указанной сессии
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @return {@link S5SessionMessenger} приемопередатчик сообщений
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException сессия не найдена
   */
  S5SessionMessenger getMessenger( Skid aSessionID );

  /**
   * Создает приемопередатчик сообщений для указанной сессии
   *
   * @param aSession {@link S5SessionData} сессия клиента
   * @return {@link S5SessionMessenger} приемопередатчик сообщений
   * @throws TsNullArgumentRtException любой аругмент = null
   * @throws TsIllegalArgumentRtException ошибка установки соединения с клиентом
   */
  S5SessionMessenger createMessenger( S5SessionData aSession );

  /**
   * Делает попытку создания приемопередатчик сообщений для указанной сессии
   * <p>
   * Если приемопередатчик сообщений для указанной сессии уже создан, то обновляется его информация о сессии
   *
   * @param aSession {@link S5SessionData} сессия клиента
   * @return {@link S5SessionMessenger} приемопередатчик сообщений. null: удаленный клиент недоступен
   * @throws TsNullArgumentRtException любой аругмент = null
   * @throws TsIllegalArgumentRtException ошибка установки соединения с клиентом
   */
  S5SessionMessenger tryCreateMessenger( S5SessionData aSession );

  /**
   * Завершает работу приемопередатчик сообщений для указанной сессии
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @return {@link S5SessionMessenger} приемопередатчик сообщений. null: приемопередатчик не был зарегистрирован
   * @throws TsNullArgumentRtException любой аругмент = null
   */
  IS5FrontendRear closeMessenger( Skid aSessionID );

  /**
   * Возвращает счетчик статистистических данных работы сессии
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @return {@link IS5StatisticCounter} счетчик статистики. null: сессия не найдена
   * @throws TsNullArgumentRtException аргумент = null
   */
  IS5StatisticCounter findStatisticCounter( Skid aSessionID );

  // ------------------------------------------------------------------------------------
  // Интерсепция
  //
  /**
   * Добавляет перехватчика операций проводимых над сессиями.
   * <p>
   * Если такой перехватчик уже зарегистрирован, то обновляет его приоритет.
   *
   * @param aInterceptor {@link IS5SessionInterceptor} перехватчик операций
   * @param aPriority int приоритет перехватчика. Чем меньше значение, тем выше приоритет.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addSessionInterceptor( IS5SessionInterceptor aInterceptor, int aPriority );

  /**
   * Удаляет перехватчика операций проводимых над сессиями.
   * <p>
   * Если такой перехватчик не зарегистрирован, то метод ничего не делает.
   *
   * @param aInterceptor {@link IS5SessionInterceptor} перехватчик операций
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeSessionInterceptor( IS5SessionInterceptor aInterceptor );
}
