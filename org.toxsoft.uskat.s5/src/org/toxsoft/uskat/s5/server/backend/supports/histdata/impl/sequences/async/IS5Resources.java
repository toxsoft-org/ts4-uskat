package org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.async;

/**
 * Константы, локализуемые ресурсы
 *
 * @author mvk
 */
interface IS5Resources {

  // ------------------------------------------------------------------------------------
  // Параметры запросов
  //
  String QPARAM_OBJID  = Messages.getString( "IS5Resources.QPARAM_OBJID" );  //$NON-NLS-1$
  String QPARAM_DATAID = Messages.getString( "IS5Resources.QPARAM_DATAID" ); //$NON-NLS-1$

  String QPARAM_INFO_ID    = Messages.getString( "IS5Resources.QPARAM_INFO_ID" );    //$NON-NLS-1$
  String QPARAM_START_TIME = Messages.getString( "IS5Resources.QPARAM_START_TIME" ); //$NON-NLS-1$
  String QPARAM_END_TIME   = Messages.getString( "IS5Resources.QPARAM_END_TIME" );   //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Имена запросов
  //
  String QUERY_GET_ALL_INFO_IDS         = Messages.getString( "IS5Resources.QUERY_GET_ALL_INFO_IDS" );         //$NON-NLS-1$
  String QUERY_GET_INFO_BY_OBJID_DATAID = Messages.getString( "IS5Resources.QUERY_GET_INFO_BY_OBJID_DATAID" ); //$NON-NLS-1$

  String QUERY_GET_ENCLOSED_BLOCKS = Messages.getString( "IS5Resources.QUERY_GET_ENCLOSED_BLOCKS" ); //$NON-NLS-1$
  String QUERY_GET_CROSSED_BLOCKS  = Messages.getString( "IS5Resources.QUERY_GET_CROSSED_BLOCKS" );  //$NON-NLS-1$

  String Q_WHERE_ENCLOSED = Messages.getString( "IS5Resources.Q_WHERE_ENCLOSED" ) + QPARAM_INFO_ID //$NON-NLS-1$
      + Messages.getString( "IS5Resources.Q_WHERE_ENCLOSED___1" ) //$NON-NLS-1$
      + QPARAM_START_TIME + Messages.getString( "IS5Resources.Q_WHERE_ENCLOSED___2" ) + QPARAM_END_TIME //$NON-NLS-1$
      + Messages.getString( "IS5Resources.Q_WHERE_ENCLOSED___3" ); //$NON-NLS-1$

  String Q_WHERE_CROSSED = Messages.getString( "IS5Resources.Q_WHERE_CROSSED" ) + QPARAM_INFO_ID //$NON-NLS-1$
      + Messages.getString( "IS5Resources.Q_WHERE_CROSSED___1" ) //$NON-NLS-1$
      + QPARAM_START_TIME + Messages.getString( "IS5Resources.Q_WHERE_CROSSED___2" ) + QPARAM_END_TIME //$NON-NLS-1$
      + Messages.getString( "IS5Resources.Q_WHERE_CROSSED___3" ) //$NON-NLS-1$
      + QPARAM_START_TIME + Messages.getString( "IS5Resources.Q_WHERE_CROSSED___4" ) + QPARAM_END_TIME //$NON-NLS-1$
      + Messages.getString( "IS5Resources.Q_WHERE_CROSSED___5" ) //$NON-NLS-1$
      + QPARAM_START_TIME + Messages.getString( "IS5Resources.Q_WHERE_CROSSED___6" ) + QPARAM_END_TIME //$NON-NLS-1$
      + Messages.getString( "IS5Resources.Q_WHERE_CROSSED___7" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_AGGR_THREAD_FINISH = Messages.getString( "IS5Resources.MSG_AGGR_THREAD_FINISH" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_WRONG_OBJID                = Messages.getString( "IS5Resources.ERR_WRONG_OBJID" );                //$NON-NLS-1$
  String ERR_WRONG_TYPE                 = Messages.getString( "IS5Resources.ERR_WRONG_TYPE" );                 //$NON-NLS-1$
  String ERR_TRY_DOUBLE_EDIT_START_TIME = Messages.getString( "IS5Resources.ERR_TRY_DOUBLE_EDIT_START_TIME" ); //$NON-NLS-1$
  String ERR_NOT_IMPORT_DATA            = Messages.getString( "IS5Resources.ERR_NOT_IMPORT_DATA" );            //$NON-NLS-1$
  String ERR_CAST_VALUE                 = Messages.getString( "IS5Resources.ERR_CAST_VALUE" );                 //$NON-NLS-1$
  String ERR_CAST_VALUE_ACCURACY        = Messages.getString( "IS5Resources.ERR_CAST_VALUE_ACCURACY" );        //$NON-NLS-1$
  String ERR_WRONG_IMPORT_CURSOR        = Messages.getString( "IS5Resources.ERR_WRONG_IMPORT_CURSOR" );        //$NON-NLS-1$
  String ERR_NOT_CURSOR_IMPORT_DATA     = Messages.getString( "IS5Resources.ERR_NOT_CURSOR_IMPORT_DATA" );     //$NON-NLS-1$
  String ERR_MSG_NOT_IMPORT_DATA        = Messages.getString( "IS5Resources.ERR_MSG_NOT_IMPORT_DATA" );        //$NON-NLS-1$

  String ERR_AGGR_READ_OUT_OF_MEMORY_TRY = Messages.getString( "IS5Resources.ERR_AGGR_READ_OUT_OF_MEMORY_TRY" ); //$NON-NLS-1$
  String ERR_AGGR_READ_OUT_OF_MEMORY     = Messages.getString( "IS5Resources.ERR_AGGR_READ_OUT_OF_MEMORY" );     //$NON-NLS-1$
  String ERR_AGGR_UNEXPECTED             = Messages.getString( "IS5Resources.ERR_AGGR_UNEXPECTED" );             //$NON-NLS-1$
  String ERR_AGGR_TRY_READ_OVER          = Messages.getString( "IS5Resources.ERR_AGGR_TRY_READ_OVER" );          //$NON-NLS-1$
  String ERR_NO_HISTDATA                 = Messages.getString( "IS5Resources.ERR_NO_HISTDATA" );                 //$NON-NLS-1$
}
