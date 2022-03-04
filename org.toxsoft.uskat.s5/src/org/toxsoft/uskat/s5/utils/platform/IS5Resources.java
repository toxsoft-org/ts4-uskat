package org.toxsoft.uskat.s5.utils.platform;

/**
 * Константы, локализуемые ресурсы .
 *
 * @author mvk
 */
interface IS5Resources {

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_CREATE_SINGLETON = Messages.getString( "IS5Resources.MSG_CREATE_SINGLETON" ); //$NON-NLS-1$
  String MSG_INIT_SINGLETON   = Messages.getString( "IS5Resources.MSG_INIT_SINGLETON" );   //$NON-NLS-1$
  String MSG_CLOSE_SINGLETON  = Messages.getString( "IS5Resources.MSG_CLOSE_SINGLETON" );  //$NON-NLS-1$

  String MSG_TRANSACTION          = Messages.getString( "IS5Resources.MSG_TRANSACTION" );          //$NON-NLS-1$
  String MSG_FIRST_TRANSACTION    = Messages.getString( "IS5Resources.MSG_FIRST_TRANSACTION" );    //$NON-NLS-1$
  String MSG_READ_ATTRIBUTE_VALUE = Messages.getString( "IS5Resources.MSG_READ_ATTRIBUTE_VALUE" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String MSG_ERR_DO_GC_UNEXPECTED    = Messages.getString( "IS5Resources.MSG_ERR_DO_GC_UNEXPECTED" );    //$NON-NLS-1$
  String MSG_ERR_GET_INFO_UNEXPECTED = Messages.getString( "IS5Resources.MSG_ERR_GET_INFO_UNEXPECTED" ); //$NON-NLS-1$
}
