package org.toxsoft.uskat.s5.utils.threads.impl;

/**
 * Константы, локализуемые ресурсы
 *
 * @author mvk+
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  /**
   * Формат формирования имени потоков чтения последовательностей
   */
  String THREAD_ID_FORMAT = Messages.getString( "IS5Resources.THREAD_ID_FORMAT" );

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_THREADS_STATE         = Messages.getString( "IS5Resources.MSG_THREADS_STATE" );
  String MSG_THREAD_FINISH         = Messages.getString( "IS5Resources.MSG_THREAD_FINISH" );
  String MSG_THREAD_MANAGER_FINISH = Messages.getString( "IS5Resources.MSG_THREAD_MANAGER_FINISH" );

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_THREAD_RUN       = Messages.getString( "IS5Resources.ERR_THREAD_RUN" );
  String ERR_THREAD_CLOSE     = Messages.getString( "IS5Resources.ERR_THREAD_CLOSE" );
  String ERR_THREAD_CANCEL    = Messages.getString( "IS5Resources.ERR_THREAD_CANCEL" );
  String ERR_THREADS_RUNNING  = Messages.getString( "IS5Resources.ERR_THREADS_RUNNING" );
  String ERR_THREADS_CANCELED = Messages.getString( "IS5Resources.ERR_THREADS_CANCELED" );

  String ERR_INTERRUPT_THREADS      = "lockThreadInterrupt(): %s, interrupt reads(%d) & writes(%d) threads";
  String ERR_INTERRUPT_WRITE_THREAD = "lockThreadInterrupt(): %s, interrupt write thread: %s";
  String ERR_INTERRUPT_READ_THREAD  = "lockThreadInterrupt(): %s, interrupt read thread: %s";

  String ERR_LOCK             = Messages.getString( "IS5Resources.ERR_LOCK" );
  String ERR_LINE             = Messages.getString( "IS5Resources.ERR_LINE" );
  String ERR_WRITE_AFTER_READ = Messages.getString( "IS5Resources.ERR_WRITE_AFTER_READ" )
      + Messages.getString( "IS5Resources.ERR_WRITE_AFTER_READ___1" )
      + Messages.getString( "IS5Resources.ERR_WRITE_AFTER_READ___2" )
      + Messages.getString( "IS5Resources.ERR_WRITE_AFTER_READ___3" );
  String ERR_READ             =
      Messages.getString( "IS5Resources.ERR_READ" ) + Messages.getString( "IS5Resources.ERR_READ___1" )
          + Messages.getString( "IS5Resources.ERR_READ___2" ) + Messages.getString( "IS5Resources.ERR_READ___3" );
  String ERR_WRITE            =
      Messages.getString( "IS5Resources.ERR_WRITE" ) + Messages.getString( "IS5Resources.ERR_WRITE___1" )
          + Messages.getString( "IS5Resources.ERR_WRITE___2" ) + Messages.getString( "IS5Resources.ERR_WRITE___3" );
  String ERR_INTERRUPT        = Messages.getString( "IS5Resources.ERR_INTERRUPT" );
}
