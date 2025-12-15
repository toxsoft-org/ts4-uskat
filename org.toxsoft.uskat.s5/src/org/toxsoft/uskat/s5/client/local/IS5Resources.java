package org.toxsoft.uskat.s5.client.local;

/**
 * Константы, локализуемые ресурсы .
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  String TO_STRING_FORMAT = "[module = %s, node = %s]";

  String STR_D_LOCAL_CONNECTION = Messages.getString( "IS5Resources.STR_D_LOCAL_CONNECTION" );

  String STR_N_LOCAL_MODULE = Messages.getString( "IS5Resources.STR_N_LOCAL_MODULE" );
  String STR_D_LOCAL_MODULE = Messages.getString( "IS5Resources.STR_D_LOCAL_MODULE" );

  String STR_N_LOCAL_NODE = Messages.getString( "IS5Resources.STR_N_LOCAL_NODE" );
  String STR_D_LOCAL_NODE = Messages.getString( "IS5Resources.STR_D_LOCAL_NODE" );

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  // String MSG_START_FRONTEND_CALLER = "Запуск поставщика событий бекенда: thread = %s";

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_INIT_ALREADY  = Messages.getString( "IS5Resources.ERR_INIT_ALREADY" );
  String ERR_ACCESS_DENIED = Messages.getString( "IS5Resources.ERR_ACCESS_DENIED" );
  String ERR_TRY_LOCK      = Messages.getString( "IS5Resources.ERR_TRY_LOCK" );
}
