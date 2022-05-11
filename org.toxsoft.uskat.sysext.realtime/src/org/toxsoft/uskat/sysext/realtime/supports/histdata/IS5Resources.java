package org.toxsoft.uskat.sysext.realtime.supports.histdata;

/**
 * Константы, локализуемые ресурсы реализации.
 *
 * @author mvk
 */
interface IS5Resources {

  String STR_D_BACKEND_HISTDATA = Messages.getString( "IS5Resources.STR_D_BACKEND_HISTDATA" ); //$NON-NLS-1$

  String STR_N_BLOCK_SIZE_MAX = Messages.getString( "IS5Resources.STR_N_BLOCK_SIZE_MAX" ); //$NON-NLS-1$
  String STR_D_BLOCK_SIZE_MAX = Messages.getString( "IS5Resources.STR_D_BLOCK_SIZE_MAX" ); //$NON-NLS-1$

  String STR_N_VALUE_SIZE_MAX = Messages.getString( "IS5Resources.STR_N_VALUE_SIZE_MAX" ); //$NON-NLS-1$
  String STR_D_VALUE_SIZE_MAX = Messages.getString( "IS5Resources.STR_D_VALUE_SIZE_MAX" ); //$NON-NLS-1$

  String STR_N_BLOCK_IMPL_CLASS = Messages.getString( "IS5Resources.STR_N_BLOCK_IMPL_CLASS" ); //$NON-NLS-1$
  String STR_D_BLOCK_IMPL_CLASS = Messages.getString( "IS5Resources.STR_D_BLOCK_IMPL_CLASS" ); //$NON-NLS-1$

  String STR_N_BLOB_IMPL_CLASS = Messages.getString( "IS5Resources.STR_N_BLOB_IMPL_CLASS" ); //$NON-NLS-1$
  String STR_D_BLOB_IMPL_CLASS = Messages.getString( "IS5Resources.STR_D_BLOB_IMPL_CLASS" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_CACHE_ALREADY_INITED                  = Messages.getString( "IS5Resources.MSG_CACHE_ALREADY_INITED" );  //$NON-NLS-1$
  String MSG_CACHE_INITED                          = Messages.getString( "IS5Resources.MSG_CACHE_INITED" );          //$NON-NLS-1$
  String MSG_WRITE_HISTDATA_VALUES                 = Messages.getString( "IS5Resources.MSG_WRITE_HISTDATA_VALUES" ); //$NON-NLS-1$
  String MSG_REJECT_CONFIGURE_BY_INTERCEPTORS      =
      Messages.getString( "IS5Resources.MSG_REJECT_CONFIGURE_BY_INTERCEPTORS" );                                     //$NON-NLS-1$
  String MSG_REJECT_READER_BY_INTERCEPTORS         =
      Messages.getString( "IS5Resources.MSG_REJECT_READER_BY_INTERCEPTORS" );                                        //$NON-NLS-1$
  String MSG_REJECT_WRITER_BY_INTERCEPTORS         =
      Messages.getString( "IS5Resources.MSG_REJECT_WRITER_BY_INTERCEPTORS" );                                        //$NON-NLS-1$
  String MSG_REJECT_HISTDATA_WRITE_BY_INTERCEPTORS =
      Messages.getString( "IS5Resources.MSG_REJECT_HISTDATA_WRITE_BY_INTERCEPTORS" );                                //$NON-NLS-1$

  String MSG_AGGR_READ_SEQUENCE_START = Messages.getString( "IS5Resources.MSG_AGGR_READ_SEQUENCE_START" ); //$NON-NLS-1$
  String MSG_AGGR_READ_SEQUENCE_TIME  = Messages.getString( "IS5Resources.MSG_AGGR_READ_SEQUENCE_TIME" );  //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_CANT_CHANGE_CLASS_WITH_OBJS = Messages.getString( "IS5Resources.ERR_CANT_CHANGE_CLASS_WITH_OBJS" ); //$NON-NLS-1$
  String ERR_HISTDATA_CANCEL_QUERY       = Messages.getString( "IS5Resources.ERR_HISTDATA_CANCEL_QUERY" );       //$NON-NLS-1$
  String ERR_WRITE_UNEXPECTED            = Messages.getString( "IS5Resources.ERR_WRITE_UNEXPECTED" );            //$NON-NLS-1$
}
