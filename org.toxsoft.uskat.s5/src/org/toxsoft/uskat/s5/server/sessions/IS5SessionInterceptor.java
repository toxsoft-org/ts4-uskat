package org.toxsoft.uskat.s5.server.sessions;

import javax.ejb.Local;

import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.s5.server.interceptors.IS5Interceptor;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;

/**
 * Перехватчик операций (создание, удаление, изменение) сессий пользователей подключенных к системе.
 * <p>
 * В отличии от событий {@link SkEvent}, события об операциях {@link IS5SessionInterceptor} передаются в режиме раннего
 * оповещения (в рамках выполняемой транзакции) и позвляют перехватчику повлиять на конечный результат проводимой
 * операции над объектами. Например, клиент(перехватчик) может запретить/отменить выполнение операции.
 * <p>
 * Все методы реализации интерфейса {@link IS5SessionInterceptor} должны быть иметь аннатоцию:
 * &#064;TransactionAttribute( TransactionAttributeType.MANDATORY ).
 *
 * @author mvk
 */
@Local
public interface IS5SessionInterceptor
    extends IS5Interceptor {

  /**
   * Вызывается ДО создания сессии на сервере
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}.
   * @throws TsIllegalStateRtException запретить создание сессии
   */
  void beforeCreateSession( Skid aSessionID );

  /**
   * Вызывается ПОСЛЕ создания сессии, но ДО завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции, но все попытки ее отмены (через поднятие исключения в
   * {@link #afterCloseSession(Skid)}) будут игнорироваться.
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}.
   */
  void afterCreateSession( Skid aSessionID );

  /**
   * Вызывается ДО завершения сессии на сервере
   * <p>
   * Событие формируется в открытой транзакции, но все попытки ее отмены (через поднятие исключения в
   * {@link #beforeCloseSession(Skid)}) будут игнорироваться.
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}.
   */
  void beforeCloseSession( Skid aSessionID );

  /**
   * Вызывается ПОСЛЕ завершения сессии, но ДО завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции, но все попытки ее отмены (через поднятие исключения в
   * {@link #afterCloseSession(Skid)}) будут игнорироваться.
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}.
   */
  void afterCloseSession( Skid aSessionID );

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
  /**
   * Вызов перехватчиков операции {@link IS5SessionInterceptor#beforeCreateSession(Skid)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5SessionInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить создание сессии
   */
  static void callBeforeCreateSession( S5InterceptorSupport<IS5SessionInterceptor> aInterceptorSupport,
      Skid aSessionID ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aSessionID );
    for( IS5SessionInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.beforeCreateSession( aSessionID );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5SessionInterceptor#afterCreateSession(Skid)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5SessionInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}.
   * @param aLogger {@link ILogger} журнал работы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void callAfterCreateSession( S5InterceptorSupport<IS5SessionInterceptor> aInterceptorSupport, Skid aSessionID,
      ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aSessionID, aLogger );
    for( IS5SessionInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      try {
        interceptor.afterCreateSession( aSessionID );
      }
      catch( Throwable e ) {
        aLogger.error( e );
      }
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5SessionInterceptor#beforeCloseSession(Skid)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5SessionInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}.
   * @param aLogger {@link ILogger} журнал работы
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить завершение сессии при aCanCancel = true
   */
  static void callBeforeCloseSession( S5InterceptorSupport<IS5SessionInterceptor> aInterceptorSupport, Skid aSessionID,
      ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aSessionID, aLogger );
    for( IS5SessionInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      try {
        interceptor.beforeCloseSession( aSessionID );
      }
      catch( Throwable e ) {
        aLogger.error( e );
      }
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5SessionInterceptor#afterCloseSession(Skid)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5SessionInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}.<br>
   * @param aLogger {@link ILogger} журнал работы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void callAfterCloseSession( S5InterceptorSupport<IS5SessionInterceptor> aInterceptorSupport, Skid aSessionID,
      ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aSessionID, aLogger );
    for( IS5SessionInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      try {
        interceptor.afterCloseSession( aSessionID );
      }
      catch( Throwable e ) {
        aLogger.error( e );
      }
    }
  }

}
