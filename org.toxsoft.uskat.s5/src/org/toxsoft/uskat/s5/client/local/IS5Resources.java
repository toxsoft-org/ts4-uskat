package org.toxsoft.uskat.s5.client.local;

/**
 * Константы, локализуемые ресурсы .
 *
 * @author mvk
 */
interface IS5Resources {

  String STR_D_LOCAL_CONNECTION = Messages.getString( "IS5Resources.STR_D_LOCAL_CONNECTION" ); //$NON-NLS-1$

  String STR_N_LOCAL_MODULE = Messages.getString( "IS5Resources.STR_N_LOCAL_MODULE" ); //$NON-NLS-1$
  String STR_D_LOCAL_MODULE = Messages.getString( "IS5Resources.STR_D_LOCAL_MODULE" ); //$NON-NLS-1$

  String STR_N_LOCAL_NODE = Messages.getString( "IS5Resources.STR_N_LOCAL_NODE" ); //$NON-NLS-1$
  String STR_D_LOCAL_NODE = Messages.getString( "IS5Resources.STR_D_LOCAL_NODE" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  // String MSG_START_FRONTEND_CALLER = "Запуск поставщика событий бекенда: thread = %s";

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_INIT_ALREADY  = Messages.getString( "IS5Resources.ERR_INIT_ALREADY" );  //$NON-NLS-1$
  String ERR_ACCESS_DENIED = Messages.getString( "IS5Resources.ERR_ACCESS_DENIED" ); //$NON-NLS-1$
  String ERR_TRY_LOCK      = Messages.getString( "IS5Resources.ERR_TRY_LOCK" );      //$NON-NLS-1$
}
