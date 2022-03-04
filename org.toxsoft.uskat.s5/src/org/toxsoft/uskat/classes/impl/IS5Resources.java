package org.toxsoft.uskat.classes.impl;

/**
 * Локализуемые ресурсы.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  String STR_N_CLASS_SERVER = "Сервер";

  String STR_D_CLASS_SERVER = "Сервер/кластер системы";

  String STR_N_CLASS_NODE = "Узел";

  String STR_D_CLASS_NODE = "Узел кластера сервера";

  String STR_N_LINK_NODE_SERVER = "Сервер";

  String STR_D_LINK_NODE_SERVER = "Сервер/кластер в рамках которого работает узел";

  String STR_N_CLASS_BACKEND = "Backend";

  String STR_D_CLASS_BACKEND = "Бекенд службы работающий в рамках узла кластера";

  String STR_N_LINK_BACKEND_NODE = "Узел";

  String STR_D_LINK_BACKEND_NODE = "Узел кластера, в рамках которого работает бекенд";

  String STR_N_CLASS_HISTORABLE_BACKEND = "Backend";

  String STR_D_CLASS_HISTORABLE_BACKEND =
      "Бекенд службы работающий в рамках узла кластера и формирующий хранимые данные";

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //

  // ------------------------------------------------------------------------------------
  // Строки ошибок
  //

}
