package org.toxsoft.uskat.s5.server.backend.supports.skatlets;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  String STR_D_BACKEND_SKATLETS = "Поддержка расширения бекенда: 'скатлеты системы'";

  String STR_LOAD_ORDER   = "Порядок загрузки";
  String STR_LOAD_ORDER_D = "Порядок загрузки скатлетов. Скатлеты не указанные в списке загружаются последними.";

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_DOJOB = "Skatlets backend doJob";

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_SKATLET_CONTAINER_IS_NOT_READY = "skatlet container is not ready!";

}
