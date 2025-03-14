package org.toxsoft.uskat.s5.client.remote.connection;

import static java.lang.String.*;
import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.bricks.threadexec.TsThreadExecutorUtils.*;
import static org.toxsoft.uskat.s5.client.remote.connection.IS5Resources.*;
import static org.toxsoft.uskat.s5.client.remote.connection.S5Connection.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.s5.utils.threads.impl.*;

/**
 * runtime-поток выполнения соединения s5
 *
 * @author mvk
 */
class S5ConnectionThread
    extends Thread {

  private static long        ERROR_TIMEOUT = 5000;
  private final S5Connection connection;
  private boolean            shutdown;
  private ILogger            logger;

  /**
   * Конструктор
   *
   * @param aConnection {@link S5Connection} s5-соединение с которым работает поток
   */
  S5ConnectionThread( S5Connection aConnection ) {
    TsNullArgumentRtException.checkNull( aConnection );
    setName( format( THREAD_ID, getClass().getSimpleName(), Long.valueOf( threadId() ) ) );
    setDaemon( true );
    connection = aConnection;
    logger = getLogger( getClass() );
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Требование завершить поток
   */
  void shutdownQuery() {
    // Получен запрос на завершение потока
    logger.info( MSG_QUERY_CLOSE_THREAD, getName(), this );
    if( logger.isSeverityOn( ELogSeverity.DEBUG ) ) {
      // Вывод стека запроса завершения
      logger.debug( MSG_STACK, getName(), this, threadStackToString( Thread.currentThread() ) );
    }
    shutdown = true;
    // Прерывание работы потока
    interrupt();
  }

  // ------------------------------------------------------------------------------------
  // Переопределение Thread
  //
  @Override
  public synchronized void start() {
    logger.debug( MSG_CONNECTION_THREAD_START, getName() );
    super.start();
  }

  @Override
  public void run() {
    // Обработка обрыва связи
    // 2021-01-20 mvk
    // connection.handlingUnexpectedBreak();
    while( !shutdown ) {
      try {
        // 2021-01-19 mvk fix-попытка ошибки существования потока восстановления связи закрытого соединения
        S5Lockable lock = connection.frontedLock();
        if( lock == null ) {
          // Не найдена блокировка соединения в пуле блокировок. Завершение потока восстановления соединения
          logger.warning( ERR_CLOSE_BY_LOCK_NOT_FOUND, getName() );
          break;
        }
        // 2020-10-12 mvk doJob + mainLock
        lockWrite( connection.frontedLock() );
        try {
          try {
            connection.tryConnect();
          }
          finally {
            // Сброс возможно установленного признака прерывания потока
            interrupted();
          }
        }
        finally {
          // 2020-10-12 mvk doJob + mainLock
          unlockWrite( connection.frontedLock() );
        }
        break;
      }
      catch( Throwable e ) {
        if( logger.isSeverityOn( ELogSeverity.DEBUG ) ) {
          logger.error( ERR_CREATE_CONNECTION_WITH_STACK, cause( e ), threadStackToString( Thread.currentThread() ) );
        }
        else {
          logger.error( ERR_CREATE_CONNECTION, cause( e ) );
        }
      }
      try {
        Thread.sleep( ERROR_TIMEOUT );
      }
      catch( InterruptedException e ) {
        logger.debug( MSG_CONNECTION_THREAD_TRY_INTERRUPT, getName(), cause( e ) );
        // Сброс возможно установленного признака прерывания потока
        interrupted();
      }
    }
    logger.debug( MSG_CONNECTION_THREAD_FINISH, getName() );
  }
}
