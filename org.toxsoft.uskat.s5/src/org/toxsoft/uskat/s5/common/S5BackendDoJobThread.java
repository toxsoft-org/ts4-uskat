package org.toxsoft.uskat.s5.common;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.common.IS5Resources.*;

import java.util.concurrent.atomic.AtomicLong;

import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ELogSeverity;
import org.toxsoft.core.tslib.utils.logs.ILogger;

/**
 * Задача (поток) обслуживания потребностей бекенда {@link ICooperativeMultiTaskable#doJob()}
 *
 * @author mvk
 */
public final class S5BackendDoJobThread
    implements Runnable, ICloseable {

  /**
   * Время между вызовами
   */
  private static final long BACKEND_DOJOB_INTERVAL = 1;

  /**
   * Генератор идентификаторов поставщика
   */
  private static AtomicLong idGenerator = new AtomicLong( 0 );

  /**
   * Идентификатор задачи
   */
  private final Long id;

  /**
   * Имя задачи
   */
  private final String name;

  /**
   * Обслуживаемый бекенд
   */
  private final ICooperativeMultiTaskable backend;

  /**
   * Поток задачи
   */
  private volatile Thread doJobThread = null;

  /**
   * Признак требования завершить работу фоновую задачу
   */
  private volatile boolean stopQueried = false;

  /**
   * Журнал работы
   */
  private final ILogger logger;

  /**
   * Конструктор
   *
   * @param aName String имя задачи
   * @param aBackend {@link ICooperativeMultiTaskable} обслуживаемый бекенд
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5BackendDoJobThread( String aName, ICooperativeMultiTaskable aBackend ) {
    TsNullArgumentRtException.checkNulls( aName, aBackend );
    id = Long.valueOf( idGenerator.incrementAndGet() );
    name = String.format( STR_DOJOB_THREAD, id, aName );
    backend = TsNullArgumentRtException.checkNull( aBackend );
    logger = getLogger( getClass() );
    Thread thread = new Thread( this, name );
    thread.start();
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает идентификатор задачи
   *
   * @return long идентификатор
   */
  public long id() {
    return id.longValue();
  }

  // ------------------------------------------------------------------------------------
  // Runnable
  //
  @Override
  public void run() {
    if( doJobThread != null || stopQueried ) {
      // Недопустимый usecase
      throw new TsInternalErrorRtException();
    }
    doJobThread = Thread.currentThread();
    doJobThread.setName( name );
    // Запуск поставщика событий бекенда
    logger.info( MSG_START_DOJOB_THREAD, doJobThread.getName() );
    stopQueried = false;
    try {
      // вступаем в цикл до момента запроса останова методом queryStop()
      while( !stopQueried ) {
        try {
          // Вызов бекенда
          safeDojobBackend( backend, logger );
          // Проверка запроса на завершение работы
          if( stopQueried ) {
            break;
          }
          // Появление в этом месте означает, что есть вызов, но он не был передан по таймауту обращения к ISkConnection
          // или по тому что один вызов был уже обработан
          Thread.sleep( BACKEND_DOJOB_INTERVAL );
        }
        catch( @SuppressWarnings( "unused" ) InterruptedException e ) {
          // Остановка потока doJob бекенда (interrupt)
          logger.info( ERR_DOJOB_THREAD_INTERRUPT, doJobThread.getName() );
        }
      }
    }
    catch( Exception e ) {
      logger.error( e );
    }
    finally {
      // Снимаем с потока doJob состояние interrupted возможно установленное при close
      Thread.interrupted();
      // Завершение работы поставщика событий бекенда (finish)
      logger.info( MSG_FINISH_DOJOB_THREAD, doJobThread.getName() );
      // Поток завершил работу
      doJobThread = null;
    }
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //
  @Override
  public void close() {
    Thread thread = doJobThread;
    // Получен запрос на завершение потока
    logger.info( MSG_QUERY_CLOSE_THREAD, name, thread );
    if( logger.isSeverityOn( ELogSeverity.DEBUG ) ) {
      // Вывод стека запроса завершения
      logger.debug( MSG_STACK, name, thread, currentThreadStackToString() );
    }
    if( thread == null ) {
      return;
    }
    stopQueried = true;
    try {
      // Поток канала будет находится в состоянии 'interrupted' поэтому любые обращения к блокировкам в этом
      // потоке будет поднимать исключение InterruptedException (например завершение работы с s5). Чтобы не создавать
      // отдельный поток "завершения" мы даем наследнику возможность освободить ресурсы до перехода в состояние
      // 'interrupted'
      // doClose();
    }
    catch( Exception e ) {
      logger.error( e );
    }
    // Прерывание блокирующих вызовов потока
    thread.interrupt();
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Безопасный (без исключений) вызов бекенда
   *
   * @param aBackend {@link ICooperativeMultiTaskable} обслуживаемый бекенд
   * @param aLogger {@link ILogger} журнал
   */
  private static void safeDojobBackend( ICooperativeMultiTaskable aBackend, ILogger aLogger ) {
    try {
      TsNullArgumentRtException.checkNulls( aBackend, aLogger );
      aBackend.doJob();
    }
    catch( Exception e ) {
      aLogger.error( e );
    }
  }
}
