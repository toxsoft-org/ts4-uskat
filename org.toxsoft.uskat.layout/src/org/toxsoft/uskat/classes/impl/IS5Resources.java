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
  String STR_CLASS_SERVER   = "Server";
  String STR_CLASS_SERVER_D = "Сервер/кластер системы";

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
  String STR_CLASS_CLUSTERNODE   = "ServerNode";
  String STR_CLASS_CLUSTERNODE_D = "Узел кластера сервера";

  String STR_LNKID_CLUSTERNODE_SERVER   = "Сервер";
  String STR_LNKID_CLUSTERNODE_SERVER_D = "Сервер/кластер в рамках которого работает узел кластера";

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //

  // ------------------------------------------------------------------------------------
  // Строки ошибок
  //

}
