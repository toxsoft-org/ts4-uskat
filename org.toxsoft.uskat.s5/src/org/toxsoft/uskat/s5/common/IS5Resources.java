package org.toxsoft.uskat.s5.common;

/**
 * Константы, локализуемые ресурсы .
 *
 * @author mvk
 */
interface IS5Resources {

  String STR_FRONTEND_CALLER = Messages.getString( "IS5Resources.STR_FRONTEND_CALLER" ); //$NON-NLS-1$
  String STR_DOJOB_THREAD    = Messages.getString( "IS5Resources.STR_DOJOB_THREAD" );    //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_START_FRONTEND_CALLER  = Messages.getString( "IS5Resources.MSG_START_FRONTEND_CALLER" );  //$NON-NLS-1$
  String MSG_FINISH_FRONTEND_CALLER = Messages.getString( "IS5Resources.MSG_FINISH_FRONTEND_CALLER" ); //$NON-NLS-1$
  String MSG_START_DOJOB_THREAD     = Messages.getString( "IS5Resources.MSG_START_DOJOB_THREAD" );     //$NON-NLS-1$
  String MSG_FINISH_DOJOB_THREAD    = Messages.getString( "IS5Resources.MSG_FINISH_DOJOB_THREAD" );    //$NON-NLS-1$
  String MSG_QUERY_CLOSE_THREAD     = Messages.getString( "IS5Resources.MSG_QUERY_CLOSE_THREAD" );     //$NON-NLS-1$
  String MSG_STACK                  = Messages.getString( "IS5Resources.MSG_STACK" );                  //$NON-NLS-1$
  String STR_N_VERSION              = Messages.getString( "STR_N_VERSION" );                           //$NON-NLS-1$
  String STR_D_VERSION              = Messages.getString( "STR_D_VERSION" );                           //$NON-NLS-1$
  String STR_N_DEPENDS              = Messages.getString( "STR_N_DEPENDS" );                           //$NON-NLS-1$
  String STR_D_DEPENDS              = Messages.getString( "STR_D_DEPENDS" );                           //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_TRY_LOCK                  = Messages.getString( "IS5Resources.ERR_TRY_LOCK" );                  //$NON-NLS-1$
  String ERR_FRONTEND_CALLER_INTERRUPT = Messages.getString( "IS5Resources.ERR_FRONTEND_CALLER_INTERRUPT" ); //$NON-NLS-1$
  String ERR_DOJOB_THREAD_INTERRUPT    = Messages.getString( "IS5Resources.ERR_DOJOB_THREAD_INTERRUPT" );    //$NON-NLS-1$

  String MSG_ERR_BLANK_ADDRESS   = Messages.getString( "MSG_ERR_BLANK_ADDRESS" );   //$NON-NLS-1$
  String FMT_ERR_INV_ADDRESS_URL = Messages.getString( "FMT_ERR_INV_ADDRESS_URL" ); //$NON-NLS-1$
  String FMT_ERR_INV_PORN_NO     = Messages.getString( "FMT_ERR_INV_PORN_NO" );     //$NON-NLS-1$

}
