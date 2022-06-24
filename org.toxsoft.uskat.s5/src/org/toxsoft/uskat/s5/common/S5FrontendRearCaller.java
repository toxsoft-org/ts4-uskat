package org.toxsoft.uskat.s5.common;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.uskat.s5.common.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.events.msg.IGtMessageListener;
import org.toxsoft.core.tslib.coll.derivative.IQueue;
import org.toxsoft.core.tslib.coll.derivative.Queue;
import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.concurrent.S5SynchronizedConnection;
import org.toxsoft.uskat.core.backend.ISkFrontendRear;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

/**
 * Поставщик асинхронных вызовов {@link ISkFrontendRear#onBackendMessage(GtMessage)} для локального и/или удаленного
 * работающий в собственном потоке
 * <p>
 * Обеспечивает развязку между потоками транспорта и {@link ISkConnection}
 *
 * @author mvk
 */
public final class S5FrontendRearCaller
    implements IGtMessageListener, Runnable, ICloseable {

  /**
   * Время (мсек) между отправлением вызовов callback когда они идут серией или когда невозможно получить доступ к
   * {@link S5SynchronizedConnection#getConnectionLock()}
   */
  private static final long FRONTEND_CALL_INTERVAL = 1;

  /**
   * Тайматут (мсек) после ошибки получения блокировки {@link S5SynchronizedConnection#getConnectionLock()}
   */
  private static final long LOCK_TIMEOUT = 1000;

  /**
   * Генератор идентификаторов поставщика
   */
  private static AtomicLong idGenerator = new AtomicLong( 0 );

  /**
   * Идентификатор поставщика вызовов
   */
  private final Long id;

  /**
   * Имя задачи
   */
  private final String name;

  /**
   * Фронтенд соединения
   */
  private final ISkFrontendRear frontend;

  /**
   * Блокировка доступа к {@link #frontend}
   */
  private final S5Lockable frontendLock;

  /**
   * Очередь запросов
   */
  private final IQueue<GtMessage> calls = new Queue<>();

  /**
   * Поток задачи
   */
  private volatile Thread callerThread = null;

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
   * @param aFrontend {@link ISkFrontendRear} фронтенд соединения
   * @param aFrontendLock {@link ReentrantReadWriteLock} блокировка доступа к фронтенду
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5FrontendRearCaller( String aName, ISkFrontendRear aFrontend, ReentrantReadWriteLock aFrontendLock ) {
    TsNullArgumentRtException.checkNulls( aName, aFrontend, aFrontendLock );
    id = Long.valueOf( idGenerator.incrementAndGet() );
    name = String.format( STR_FRONTEND_CALLER, id, aName );
    frontend = TsNullArgumentRtException.checkNull( aFrontend );
    frontendLock = S5Lockable.getLockableFromPool( aFrontendLock );
    logger = getLogger( getClass() );
    Thread thread = new Thread( this, name );
    thread.start();
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает идентификатор поставщика вызовов
   *
   * @return long идентификатор поставщика
   */
  public long id() {
    return id.longValue();
  }

  /**
   * Возвращает имя поставщика вызовов
   *
   * @return String имя поставщика
   */
  public String name() {
    return name;
  }

  // ------------------------------------------------------------------------------------
  // IGtMessageListener
  //
  /**
   * Асинхронная отправка сообщение фроненду
   *
   * @param aMessage {@link GtMessage} сообщениe
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @Override
  public void onGenericTopicMessage( GtMessage aMessage ) {
    TsNullArgumentRtException.checkNull( aMessage );
    synchronized (calls) {
      calls.putTail( aMessage );
      calls.notifyAll();
    }
  }

  // ------------------------------------------------------------------------------------
  // Runnable
  //
  @Override
  public void run() {
    if( callerThread != null || stopQueried ) {
      // Недопустимый usecase
      throw new TsInternalErrorRtException();
    }
    callerThread = Thread.currentThread();
    callerThread.setName( name );
    // Запуск поставщика событий бекенда
    logger.info( MSG_START_FRONTEND_CALLER, callerThread.getName() );
    stopQueried = false;
    try {
      // вступаем в цикл до момента запроса останова методом queryStop()
      while( !stopQueried ) {
        try {
          // Вызов фронтенда. null: нет вызовов
          GtMessage message = null;
          synchronized (calls) {
            if( calls.size() == 0 ) {
              // Вызовов нет, ждем сигнала о добавлении новых вызовов
              calls.wait();
            }
            message = calls.getHeadOrNull();
            // Выход из критической секции
          }
          while( message != null && !stopQueried ) {
            // Попытка получить доступ к SkConnection
            if( !tryLockWrite( frontendLock, LOCK_TIMEOUT ) ) {
              // Ошибка получения блокировки
              logger.warning( ERR_TRY_LOCK, name, frontendLock );
              continue;
            }
            // Получен доступ к блокировке.
            try {
              // Вызов фронтенда
              safeCallFrontend( frontend, message, logger );
            }
            finally {
              message = null;
              // Разблокировка доступа к SkConnection
              unlockWrite( frontendLock );
            }
          }
          // Проверка запроса на завершение работы
          if( stopQueried ) {
            break;
          }
          // Появление в этом месте означает, что есть вызов, но он не был передан по таймауту обращения к ISkConnection
          // или по тому что один вызов был уже обработан
          Thread.sleep( FRONTEND_CALL_INTERVAL );
        }
        catch( @SuppressWarnings( "unused" ) InterruptedException e ) {
          // Остановка поставщика событий бекенда (interrupt)
          logger.info( ERR_FRONTEND_CALLER_INTERRUPT, callerThread.getName() );
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
      logger.info( MSG_FINISH_FRONTEND_CALLER, callerThread.getName() );
      // Поток завершил работу
      callerThread = null;
    }
  }

  // ------------------------------------------------------------------------------------
  // ICloseable
  //
  @Override
  public void close() {
    Thread thread = callerThread;
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
   * Безопасный (без исключений) вызов фронтенда
   *
   * @param aFrontend {@link ISkFrontendRear} вызываемый фронтенд
   * @param aMessage {@link GtMessage} сообщение
   * @param aLogger {@link ILogger} журнал
   */
  private static void safeCallFrontend( ISkFrontendRear aFrontend, GtMessage aMessage, ILogger aLogger ) {
    try {
      TsNullArgumentRtException.checkNulls( aFrontend, aMessage, aLogger );
      aFrontend.onBackendMessage( aMessage );
    }
    catch( Exception e ) {
      aLogger.error( e );
    }
  }

}
