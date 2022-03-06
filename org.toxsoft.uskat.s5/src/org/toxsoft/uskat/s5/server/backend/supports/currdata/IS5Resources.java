package org.toxsoft.uskat.s5.server.backend.supports.currdata;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  String STR_D_BACKEND_CURRDATA = Messages.getString( "IS5Resources.STR_D_BACKEND_CURRDATA" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_CACHE_ALREADY_INITED                  = Messages.getString( "IS5Resources.MSG_CACHE_ALREADY_INITED" );  //$NON-NLS-1$
  String MSG_CACHE_INITED                          = Messages.getString( "IS5Resources.MSG_CACHE_INITED" );          //$NON-NLS-1$
  String MSG_WRITE_CURRDATA_VALUES                 = Messages.getString( "IS5Resources.MSG_WRITE_CURRDATA_VALUES" ); //$NON-NLS-1$
  String MSG_WRITE_HISTDATA_VALUES                 = Messages.getString( "IS5Resources.MSG_WRITE_HISTDATA_VALUES" ); //$NON-NLS-1$
  String MSG_REJECT_CONFIGURE_BY_INTERCEPTORS      =
      Messages.getString( "IS5Resources.MSG_REJECT_CONFIGURE_BY_INTERCEPTORS" );                                     //$NON-NLS-1$
  String MSG_REJECT_READER_BY_INTERCEPTORS         =
      Messages.getString( "IS5Resources.MSG_REJECT_READER_BY_INTERCEPTORS" );                                        //$NON-NLS-1$
  String MSG_REJECT_WRITER_BY_INTERCEPTORS         =
      Messages.getString( "IS5Resources.MSG_REJECT_WRITER_BY_INTERCEPTORS" );                                        //$NON-NLS-1$
  String MSG_REJECT_CURRDATA_WRITE_BY_INTERCEPTORS =
      Messages.getString( "IS5Resources.MSG_REJECT_CURRDATA_WRITE_BY_INTERCEPTORS" );                                //$NON-NLS-1$
  String MSG_REJECT_HISTDATA_WRITE_BY_INTERCEPTORS =
      Messages.getString( "IS5Resources.MSG_REJECT_HISTDATA_WRITE_BY_INTERCEPTORS" );                                //$NON-NLS-1$
  String MSG_WAIT_LOCK_INFOES                      = Messages.getString( "IS5Resources.MSG_WAIT_LOCK_INFOES" );      //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_NO_DEFAULT_VALUE                = Messages.getString( "IS5Resources.ERR_NO_DEFAULT_VALUE" );                //$NON-NLS-1$
  String ERR_WRONG_CACHE_SIZE                = Messages.getString( "IS5Resources.ERR_WRONG_CACHE_SIZE" );                //$NON-NLS-1$
  String ERR_CURRDATA_NOT_FOUND              = Messages.getString( "IS5Resources.ERR_CURRDATA_NOT_FOUND" );              //$NON-NLS-1$
  String ERR_CURRDATA_NOT_HAVE_DEFAULT_VALUE = Messages.getString( "IS5Resources.ERR_CURRDATA_NOT_HAVE_DEFAULT_VALUE" ); //$NON-NLS-1$
  String ERR_CANT_CHANGE_CLASS_WITH_OBJS     = Messages.getString( "IS5Resources.ERR_CANT_CHANGE_CLASS_WITH_OBJS" );     //$NON-NLS-1$
  String ERR_HISTDATA_CANCEL_QUERY           = Messages.getString( "IS5Resources.ERR_HISTDATA_CANCEL_QUERY" );           //$NON-NLS-1$
  String ERR_WRITE_UNEXPECTED                = Messages.getString( "IS5Resources.ERR_WRITE_UNEXPECTED" );                //$NON-NLS-1$
  String ERR_REMOTE_ACCESS                   = Messages.getString( "IS5Resources.ERR_REMOTE_ACCESS" );                   //$NON-NLS-1$
  String ERR_LOCK_INFOES_TIMEOUT             = Messages.getString( "IS5Resources.ERR_LOCK_INFOES_TIMEOUT" );             //$NON-NLS-1$
  String ERR_CACHE_VALUE_NOT_FOUND           = Messages.getString( "IS5Resources.ERR_CACHE_VALUE_NOT_FOUND" );           //$NON-NLS-1$
  String ERR_WRONG_VALUE_TYPE                =
      "Недопустимый тип значения для текущего данного. valueIndex=%d, gwid=%s, dataType=%s, valueType=%s(%s)";
}
