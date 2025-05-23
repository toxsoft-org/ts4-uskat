package org.toxsoft.uskat.classes.impl;

import org.toxsoft.uskat.classes.*;

/**
 * Локализуемые ресурсы.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  /**
   * {@link ISkNetNode}
   */
  String STR_CLASS_NETNODE   = "Узел";
  String STR_CLASS_NETNODE_D = "Сетевой узел системы";

  String STR_LNKID_NETNODE_RESOURCES   = "Ресурсы";
  String STR_LNKID_NETNODE_RESOURCES_D = "Список ресурсов системы подключаемых к сетевому узлу";

  String STR_RTD_NETNODE_ONLINE   = "Связь";
  String STR_RTD_NETNODE_ONLINE_D = "С узлом установлена связь";

  String STR_RTD_NETNODE_HEALTH   = "Состояние";
  String STR_RTD_NETNODE_HEALTH_D =
      "Интегральная оценка состояния подключенных ресурсов. 0 - нет связи, 100 - все подключено и работает.";

  /**
   * {@link ISkServer}
   */
  String STR_CLASS_SERVER   = "Сервер";
  String STR_CLASS_SERVER_D = "Сервер системы";

  String STR_EV_PARAM_LOGIN   = "Логин";              //
  String STR_EV_PARAM_LOGIN_D = "Логин пользователя"; //

  String STR_EV_PARAM_IP   = "IP";                    //
  String STR_EV_PARAM_IP_D = "IP-адрес пользователя"; //

  String STR_EV_SESSION_ID   = "Сессия";                            //
  String STR_EV_SESSION_ID_D = "Идентификатор сессии пользователя"; //

  String STR_EV_LOGIN_FAILED   = "Неудачная попытка подключения";                  //
  String STR_EV_LOGIN_FAILED_D = "Неудачная попытка создания сессии пользователя"; //

  String STR_EV_SESSION_CREATED   = "Создание сессии";              //
  String STR_EV_SESSION_CREATED_D = "Создание сессии пользователя"; //

  String STR_EV_SESSION_CLOSED   = "Завершение сессии";              //
  String STR_EV_SESSION_CLOSED_D = "Завершение сессии пользователя"; //

  String STR_EV_SESSION_BREAKED   = "Разрыв сессии";              //
  String STR_EV_SESSION_BREAKED_D = "Разрыв сессии пользователя"; //

  String STR_EV_SESSION_RESTORED   = "Восстановление сессии";              //
  String STR_EV_SESSION_RESTORED_D = "Восстановление сессии пользователя"; //

  String STR_EV_PARAM_USER   = "Пользователь";                 //
  String STR_EV_PARAM_USER_D = "Пользователь системы (login)"; //

  String STR_EV_PARAM_DESCR   = "Изменения";                                   //
  String STR_EV_PARAM_DESCR_D = "Описание изменений (вводится пользователем)"; //

  String STR_EV_PARAM_EDITOR   = "Редактир";           //
  String STR_EV_PARAM_EDITOR_D = "Описание редактора"; //

  String STR_EV_SYSDESCR_CHANGED   = "Изменено описание";                   //
  String STR_EV_SYSDESCR_CHANGED_D = "Изменено системное описание сервера"; //

  /**
   * {@link ISkServerNode}
   */
  String STR_CLASS_SERVERNODE   = "ServerNode";
  String STR_CLASS_SERVERNODE_D = "Узел сервера/кластера";

  String STR_LNKID_SERVERNODE_SERVER   = "Сервер";
  String STR_LNKID_SERVERNODE_SERVER_D = "Сервер/кластер в рамках которого работает узел кластера";

  /**
   * {@link ISkServerBackend}
   */
  String STR_CLASS_SERVERBACKEND   = "ServerBackend";
  String STR_CLASS_SERVERBACKEND_D = "Бекенд службы работающий в рамках узла кластера";

  String STR_LNKID_SERVERBACKEND_NODE   = "Узел";
  String STR_LNKID_SERVERBACKEND_NODE_D = "Узел кластера, в рамках которого работает бекенд";

  /**
   * {@link ISkServerHistorable}
   */
  String STR_CLASS_SERVERHISTORABLE   = "ServerHistorable";
  String STR_CLASS_SERVERHISTORABLE_D = "Бекенд службы работающий в рамках узла кластера и формирующий хранимые данные";

  /**
   * {@link ISkSession}
   */
  String STR_CLASS_SESSION   = "Сессия";
  String STR_CLASS_SESSION_D = "Сессия пользователя подключенного к серверу";

  String STR_ATRID_SESSION_STARTTIME   = "Открыта";
  String STR_ATRID_SESSION_STARTTIME_D = "Метка времени (мсек с начала эпохи) открытия сессии";

  String STR_ATRID_SESSION_ENDTIME   = "Закрыта";
  String STR_ATRID_SESSION_ENDTIME_D = "Метка времени (мсек с начала эпохи) завершения сессии";

  String STR_ATRID_SESSION_BACKEND_SPECIFIC_PARAMS   = "Backend";
  String STR_ATRID_SESSION_BACKEND_SPECIFIC_PARAMS_D = "Специфичные для бекенда параметры";

  String STR_ATRID_SESSION_CONNECTION_CREATION_PARAMS   = "Connection";
  String STR_ATRID_SESSION_CONNECTION_CREATION_PARAMS_D = "Параметры создания соединения";

  String STR_LNKID_SESSION_USER   = "Пользователь";
  String STR_LNKID_SESSION_USER_D = "Пользователь, который вошел в систему";

  String STR_RTDID_SESSION_STATE   = "Состояние";
  String STR_RTDID_SESSION_STATE_D = "Состояние активности";

  String STR_ROOT_USER   = "Root";             //$NON-NLS-1$
  String STR_ROOT_USER_D = "Root - superuser"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //

  // ------------------------------------------------------------------------------------
  // Строки ошибок
  //

}
