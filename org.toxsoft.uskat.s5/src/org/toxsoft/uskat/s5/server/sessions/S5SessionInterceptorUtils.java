package org.toxsoft.uskat.s5.server.sessions;

import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.s5.server.interceptors.*;

/**
 * Вспомогательные методы для работы с интерсепторами сессий
 *
 * @author mvk
 */
class S5SessionInterceptorUtils {

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
