package org.toxsoft.uskat.s5.server.sessions;

import javax.ejb.Local;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.common.info.IS5SessionsInfos;
import org.toxsoft.uskat.s5.common.sessions.IS5SessionInfo;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackWriter;
import org.toxsoft.uskat.s5.server.statistics.IS5StatisticCounter;

import ru.uskat.core.api.users.ISkSession;

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
   * Установить backend сервера
   *
   * @param aBackend {@link IS5BackendCoreSingleton} backend
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException backend уже установлен
   */
  void setBackend( IS5BackendCoreSingleton aBackend );

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
  IList<S5RemoteSession> openSessions();

  /**
   * Возвращает список закрытых сессий
   *
   * @return {@link IList}&lt;{@link IS5SessionInfo}&gt; список сессий
   */
  IList<S5RemoteSession> closedSessions();

  /**
   * Возвращает описание сессии пользователя
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @return {@link S5RemoteSession} сессия пользователя. null: сессия не существует
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5RemoteSession findSession( Skid aSessionID );

  /**
   * Регистрация сессии с удаленным пользователем
   * <p>
   * Если сессия была уже зарегистрирована, то обновляются ее параметры
   *
   * @param aSession {@link S5RemoteSession} сессия пользователя
   * @return boolean <b>true</b> сессия создана;<b>false</b> сессия была создана ранее, параметры обновлены
   * @throws TsNullArgumentRtException аргумент = null
   */
  boolean createRemoteSession( S5RemoteSession aSession );

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
   * Определяет описание открытой сессии удаленного пользователя
   * <p>
   * Если сессия не существует, то ничего не делает
   *
   * @param aSession {@link S5RemoteSession} сессия пользователя
   * @throws TsNullArgumentRtException аргумент = null
   */
  void updateRemoteSession( S5RemoteSession aSession );

  /**
   * Определяет описание открытой сессии локального пользователя
   * <p>
   * Если сессия не существует, то ничего не делает
   *
   * @param aSession {@link S5LocalSession} сессия пользователя
   * @throws TsNullArgumentRtException аргумент = null
   */
  void updateLocalSession( S5LocalSession aSession );

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
   * Возвращает писателя обратных вызовов для указанной сессии
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @return {@link S5SessionCallbackWriter} писатель обратных вызовов. null: писатель не найден
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5SessionCallbackWriter findCallbackWriter( Skid aSessionID );

  /**
   * Возвращает писателя обратных вызовов для указанной сессии
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @return {@link S5SessionCallbackWriter} писатель обратных вызовов
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException сессия не найдена
   */
  S5SessionCallbackWriter getCallbackWriter( Skid aSessionID );

  /**
   * Создает писателя обратных вызовов для указанной сессии
   *
   * @param aSession {@link S5RemoteSession} сессия клиента
   * @return {@link S5SessionCallbackWriter} писатель обратных вызовов
   * @throws TsNullArgumentRtException любой аругмент = null
   * @throws TsIllegalArgumentRtException ошибка установки соединения с клиентом
   */
  S5SessionCallbackWriter createCallbackWriter( S5RemoteSession aSession );

  /**
   * Делает попытку создания писателя обратных вызовов для указанной сессии
   * <p>
   * Если писатель для указанной сессии уже создан, то обновляется его информация о сессии
   *
   * @param aSession {@link S5RemoteSession} сессия клиента
   * @return {@link S5SessionCallbackWriter} писатель обратных вызовов. null: удаленный клиент недоступен
   * @throws TsNullArgumentRtException любой аругмент = null
   * @throws TsIllegalArgumentRtException ошибка установки соединения с клиентом
   */
  S5SessionCallbackWriter tryCreateCallbackWriter( S5RemoteSession aSession );

  /**
   * Завершает работу писателя обратных вызовов для указанной сессии
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @return {@link S5SessionCallbackWriter} писатель обратных вызовов. null: писатель не был зарегистрирован для сессии
   * @throws TsNullArgumentRtException любой аругмент = null
   */
  IS5FrontendRear closeCallbackWriter( Skid aSessionID );

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