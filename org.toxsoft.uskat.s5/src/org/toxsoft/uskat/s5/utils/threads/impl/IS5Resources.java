package org.toxsoft.uskat.s5.utils.threads.impl;

/**
 * Константы, локализуемые ресурсы
 *
 * @author mvk+
 */
interface IS5Resources {

  /**
   * Формат формирования имени потоков чтения последовательностей
   */
  String THREAD_ID_FORMAT = Messages.getString( "IS5Resources.THREAD_ID_FORMAT" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_THREADS_STATE         = Messages.getString( "IS5Resources.MSG_THREADS_STATE" );         //$NON-NLS-1$
  String MSG_THREAD_FINISH         = Messages.getString( "IS5Resources.MSG_THREAD_FINISH" );         //$NON-NLS-1$
  String MSG_THREAD_MANAGER_FINISH = Messages.getString( "IS5Resources.MSG_THREAD_MANAGER_FINISH" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_THREAD_RUN       = Messages.getString( "IS5Resources.ERR_THREAD_RUN" );       //$NON-NLS-1$
  String ERR_THREAD_CLOSE     = Messages.getString( "IS5Resources.ERR_THREAD_CLOSE" );     //$NON-NLS-1$
  String ERR_THREAD_CANCEL    = Messages.getString( "IS5Resources.ERR_THREAD_CANCEL" );    //$NON-NLS-1$
  String ERR_THREADS_RUNNING  = Messages.getString( "IS5Resources.ERR_THREADS_RUNNING" );  //$NON-NLS-1$
  String ERR_THREADS_CANCELED = Messages.getString( "IS5Resources.ERR_THREADS_CANCELED" ); //$NON-NLS-1$

  String ERR_LOCK             = Messages.getString( "IS5Resources.ERR_LOCK" );                         //$NON-NLS-1$
  String ERR_LINE             = Messages.getString( "IS5Resources.ERR_LINE" );                         //$NON-NLS-1$
  String ERR_WRITE_AFTER_READ = Messages.getString( "IS5Resources.ERR_WRITE_AFTER_READ" )              //$NON-NLS-1$
      + Messages.getString( "IS5Resources.ERR_WRITE_AFTER_READ___1" )                                  //$NON-NLS-1$
      + Messages.getString( "IS5Resources.ERR_WRITE_AFTER_READ___2" )                                  //$NON-NLS-1$
      + Messages.getString( "IS5Resources.ERR_WRITE_AFTER_READ___3" );                                 //$NON-NLS-1$
  String ERR_READ             = Messages.getString( "IS5Resources.ERR_READ" )                          //$NON-NLS-1$
      + Messages.getString( "IS5Resources.ERR_READ___1" )                                              //$NON-NLS-1$
      + Messages.getString( "IS5Resources.ERR_READ___2" )                                              //$NON-NLS-1$
      + Messages.getString( "IS5Resources.ERR_READ___3" );                                             //$NON-NLS-1$
  String ERR_WRITE            = Messages.getString( "IS5Resources.ERR_WRITE" )                         //$NON-NLS-1$
      + Messages.getString( "IS5Resources.ERR_WRITE___1" )                                             //$NON-NLS-1$
      + Messages.getString( "IS5Resources.ERR_WRITE___2" )                                             //$NON-NLS-1$
      + Messages.getString( "IS5Resources.ERR_WRITE___3" );                                            //$NON-NLS-1$
  String ERR_INTERRUPT        = Messages.getString( "IS5Resources.ERR_INTERRUPT" );                    //$NON-NLS-1$
}
