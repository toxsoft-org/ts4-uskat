package org.toxsoft.uskat.sysext.realtime.addon;

import ru.uskat.backend.addons.realtime.ISkBackendAddonRealtime;

/**
 * Локализуемые ресурсы реализации.
 *
 * @author mvk
 */
interface IS5Resources {

  String STR_D_BACKEND_ADDON_REALTIME =
      Messages.getString( "IS5Resources.STR_D_BACKEND_ADDON_REALTIME" ) + ISkBackendAddonRealtime.SK_BACKEND_ADDON_ID //$NON-NLS-1$
          + Messages.getString( "IS5Resources.STR_D_BACKEND_ADDON_REALTIME___1" ); //$NON-NLS-1$

  String STR_N_BACKEND_REALTIME = Messages.getString( "IS5Resources.STR_N_BACKEND_REALTIME" ); //$NON-NLS-1$
  String STR_D_BACKEND_REALTIME = Messages.getString( "IS5Resources.STR_D_BACKEND_REALTIME" ); //$NON-NLS-1$

  String STR_N_REALTIME_STAT_CURRDATA_RECEVIED =
      Messages.getString( "IS5Resources.STR_N_REALTIME_STAT_CURRDATA_RECEVIED" ); //$NON-NLS-1$
  String STR_D_REALTIME_STAT_CURRDATA_RECEVIED =
      Messages.getString( "IS5Resources.STR_D_REALTIME_STAT_CURRDATA_RECEVIED" ); //$NON-NLS-1$

  String STR_N_REALTIME_STAT_HISTDATA_RECEVIED =
      Messages.getString( "IS5Resources.STR_N_REALTIME_STAT_HISTDATA_RECEVIED" ); //$NON-NLS-1$
  String STR_D_REALTIME_STAT_HISTDATA_RECEVIED =
      Messages.getString( "IS5Resources.STR_D_REALTIME_STAT_HISTDATA_RECEVIED" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_CACHE_ALREADY_INITED = Messages.getString( "IS5Resources.MSG_CACHE_ALREADY_INITED" ); //$NON-NLS-1$
  String MSG_CACHE_INITED         = Messages.getString( "IS5Resources.MSG_CACHE_INITED" );         //$NON-NLS-1$
  String MSG_TRANSMIT_VALUES      = Messages.getString( "IS5Resources.MSG_TRANSMIT_VALUES" );      //$NON-NLS-1$
  String MSG_CURRDATA_VALUE       = Messages.getString( "IS5Resources.MSG_CURRDATA_VALUE" );       //$NON-NLS-1$
  String MSG_HISTDATA_VALUE       = Messages.getString( "IS5Resources.MSG_HISTDATA_VALUE" );       //$NON-NLS-1$
  String MSG_CD_READ_TABLE        = Messages.getString( "IS5Resources.MSG_CD_READ_TABLE" );        //$NON-NLS-1$
  String MSG_CD_WRITE_TABLE       = Messages.getString( "IS5Resources.MSG_CD_WRITE_TABLE" );       //$NON-NLS-1$
  String MSG_REGISTER_CMD_GWIDS   = Messages.getString( "IS5Resources.MSG_REGISTER_CMD_GWIDS" );   //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_NO_DEFAULT_VALUE                = Messages.getString( "IS5Resources.ERR_NO_DEFAULT_VALUE" );                //$NON-NLS-1$
  String ERR_WRONG_CACHE_SIZE                = Messages.getString( "IS5Resources.ERR_WRONG_CACHE_SIZE" );                //$NON-NLS-1$
  String ERR_CURRDATA_NOT_FOUND              = Messages.getString( "IS5Resources.ERR_CURRDATA_NOT_FOUND" );              //$NON-NLS-1$
  String ERR_CURRDATA_NOT_HAVE_DEFAULT_VALUE = Messages.getString( "IS5Resources.ERR_CURRDATA_NOT_HAVE_DEFAULT_VALUE" ); //$NON-NLS-1$
  String ERR_CANT_CHANGE_CLASS_WITH_OBJS     = Messages.getString( "IS5Resources.ERR_CANT_CHANGE_CLASS_WITH_OBJS" );     //$NON-NLS-1$
  String ERR_CURRDATA_WRONG_INDEXES          = Messages.getString( "IS5Resources.ERR_CURRDATA_WRONG_INDEXES" );          //$NON-NLS-1$
  String ERR_DATA_NOT_FOUND                  = "Не найдено текущее данное с указанным индексом. aIndex = %d";            //$NON-NLS-1$
}
