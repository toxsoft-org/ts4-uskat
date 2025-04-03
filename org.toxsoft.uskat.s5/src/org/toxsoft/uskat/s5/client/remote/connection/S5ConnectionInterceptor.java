package org.toxsoft.uskat.s5.client.remote.connection;

import static org.toxsoft.uskat.s5.client.IS5ConnectionParams.*;
import static org.toxsoft.uskat.s5.client.remote.connection.IS5Resources.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;

import java.lang.reflect.*;
import java.util.concurrent.*;
import java.util.function.*;

import org.jboss.ejb.client.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.sessions.init.*;
import org.wildfly.security.auth.client.*;

/**
 * Перехват удаленных вызовов к серверу:
 * <li>1. Замена proxy remote-служб на proxy в контексте клиента;</li>
 * <li>2. Перехват ошибки javax.ejb.NoSuchEJBException и запрос на пересоздание соединения;</li>
 *
 * @author mvk
 */
class S5ConnectionInterceptor
    implements EJBClientInterceptor {

  /**
   * Соединение в рамках которого работает интерсептор
   */
  private final S5Connection connection;

  /**
   * Контекст создания соединения (перспектива)
   */
  private EJBSessionCreationInvocationContext creationContext;

  /**
   * Идентификатор сессии (перспектива)
   */
  private SessionID sessionID;

  /**
   * Журнал работы
   */
  private final ILogger logger;

  /**
   * Конструктор
   *
   * @param aConnection {@link S5Connection} соединение сервером
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5ConnectionInterceptor( S5Connection aConnection ) {
    TsNullArgumentRtException.checkNulls( aConnection );
    connection = aConnection;
    logger = connection.logger();
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса EJBClientInterceptor
  //
  @Override
  public SessionID handleSessionCreation( EJBSessionCreationInvocationContext aContext )
      throws Exception {
    creationContext = aContext;
    try {
      sessionID = aContext.proceed();
      return sessionID;
    }
    catch( Exception e ) {
      connection.logger().error( e );
      throw e;
    }
  }

  @Override
  public void handleInvocation( final EJBClientInvocationContext aInvocationContext )
      throws Exception {
    // ContextManager<EJBClientContext> ejbContextManager = EJBClientContext.getContextManager();
    // ejbContextManager.setGlobalDefault( connection.ejbClientContext() );
    // AuthenticationContext.getContextManager().setGlobalDefault( connection.get() );

    // Установка контекста вызывающего потока
    // AuthenticationContext.getContextManager().setThreadDefault( connection.get() );
    // connection.setEJBContextForCurrentThread();
    // AuthenticationContext.getContextManager().setThreadDefault( connection.get() );
    // Affinity invocationAffinity = aInvocationContext.getWeakAffinity();
    // Method invocationMethod = aInvocationContext.getInvokedMethod();
    // Affinity backendAffinity = EJBClient.getWeakAffinity( connection.backend() );
    // aInvocationContext.setWeakAffinity( backendAffinity );
    // // Вызов метода. Замена affinity
    // connection.logger().debug( MSG_CHANGE_CALL_AFFINITY, invocationMethod, invocationAffinity, backendAffinity );
    // Просто пропускаем запрос к серверу

    // 2021-03-31 mvk
    // В tm2, при подключении к системе через проброс порта промежуточных компьютеров и роутеров
    // потребовалось явно устанавливать affinity для каждого вызова.
    // Необходимо проверить что это будет работать на кластерных конфигурациях сервера
    Affinity standaloneAffinity = connection.getStandaloneAffinityOrNull();
    if( standaloneAffinity != null ) {
      aInvocationContext.setWeakAffinity( standaloneAffinity );
    }
    aInvocationContext.sendRequest();
  }

  @Override
  public Object handleInvocationResult( final EJBClientInvocationContext aInvocationContext )
      throws Exception {
    Object originalResult = null;
    try {
      originalResult = aInvocationContext.getResult();
    }
    catch( javax.ejb.NoSuchEJBException e ) {
      if( connection.state() == EConnectionState.DISCONNECTED ) {
        // Невозможно найти EJB при завершении работы соединения
        Method method = aInvocationContext.getInvokedMethod();
        connection.logger().debug( ERR_NO_EJB_RETRY_BY_CLOSE, connection, method, cause( e ) );
        return originalResult;
      }
      // noSuchEJBExceptionCount++;
      // Параметры ошибки: При вызове удаленного метода сервера была обнаружена потеря связи с сервером
      IOptionSet options = connection.sessionInitData().clientOptions();
      // if( noSuchEJBExceptionCount < NO_SUCH_EJB_EXCEPTION_COUNT_MAX ) {
      // // Очередная ошибка
      // Integer c = Integer.valueOf( noSuchEJBExceptionCount );
      // connection.logger().error( ERR_NO_EJB_RETRY, connection, c, cause( e ) );
      // throw e;
      // }
      // // TODO: ? пробуем избежать deadlock при восстановлении связи restoreSessionQuery()
      // Thread.sleep( ERROR_TIMEOUT );
      // Запрос на пересоздание соединения с сервером
      connection.restoreSessionQuery();
      connection.logger().error( e, ERR_UNEXPECTED_CLOSE, connection, cause( e ) );
      throw new S5ConnectionException( e, options, ERR_UNEXPECTED_CLOSE, connection, cause( e ) );
    }
    if( originalResult instanceof S5SessionInitResult ) {
      // Замена affinity у прокси расширений бекенда
      // (требуется для работы соединения внутри wildfly с удаленным сервером)
      try {
        IOptionSet options = connection.sessionInitData().clientOptions();
        // 2020-09-03 mvk +++
        // Минимальный интервал передачи пакетов через соединение (не может быть больше чем 2/3 таймаута сессии)
        long failureTimeout = OP_FAILURE_TIMEOUT.getValue( options ).asInt();

        Affinity backendAffinity = EJBClient.getWeakAffinity( connection.session() );
        S5SessionInitResult initResult = (S5SessionInitResult)originalResult;
        IStringMap<IS5BackendAddonSession> prevAddonProxies = initResult.baSessions();
        IStringMapEdit<IS5BackendAddonSession> newAddonProxies = new StringMap<>();
        for( String addonId : prevAddonProxies.keys() ) {
          Object addonProxy = prevAddonProxies.getByKey( addonId );
          Affinity addonAffinity = EJBClient.getWeakAffinity( addonProxy );
          IS5BackendAddonSession proxy = (IS5BackendAddonSession)replaceContextProxy( connection, addonProxy );
          // 127.0.0.1: При failover не может переключится на работающий узел
          EJBClient.setWeakAffinity( proxy, backendAffinity );
          newAddonProxies.put( addonId, proxy );
          // Расширение backend. Замена affinity
          connection.logger().debug( MSG_CHANGE_ADDON_AFFINITY, addonId, addonAffinity, backendAffinity );

          // 2020-09-03 mvk +++
          logger.debug( MSG_INTERCEPTOR_SET_INVOCATION_TIMEOUT, Long.valueOf( failureTimeout ) );
          EJBClient.setInvocationTimeout( proxy, failureTimeout, TimeUnit.MILLISECONDS );

        }
        initResult.setAddons( newAddonProxies );
        return initResult;
      }
      catch( Throwable e ) {
        connection.logger().error( e );
        throw new TsInternalErrorRtException( e );
      }
    }
    return originalResult;
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //
  /**
   * Возвращает контекст создания соединения
   *
   * @return {@link EJBSessionCreationInvocationContext} контекст. null: неопределен
   */
  EJBSessionCreationInvocationContext creationContext() {
    return creationContext;
  }

  /**
   * Возвращает идентификатор сессии
   *
   * @return {@link SessionID} идентификатор сессии
   */
  SessionID sessionID() {
    return sessionID;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Производит замену proxy remote-службы в контексте клиента
   *
   * @param aContextSupplier {@link Supplier}&lt;{@link AuthenticationContext}&gt; поставщик контекста клиента в рамках
   *          которого производится вызов методов сервера
   * @param aOriginalResult Object исходный proxy remote-службы
   * @return proxy Object в контексте клиента
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static Object replaceContextProxy( Supplier<AuthenticationContext> aContextSupplier, Object aOriginalResult ) {
    TsNullArgumentRtException.checkNulls( aContextSupplier, aOriginalResult );
    // at this point we have identified the invocation to be "getEJBHome()" method invocation on the EJBObject view.
    // So we now update that returned EJB home proxy to use the EJB client context identifier that's applicable for the
    // now recreate the returned EJB home proxy with the EJB client context identifier
    try {
      final Object ejbProxy = aOriginalResult;
      // // we *don'debug* change the locator of the original proxy
      final EJBLocator<?> ejbLocator = EJBClient.getLocatorFor( ejbProxy );
      Object ejb = EJBClient.createProxy3( ejbLocator, aContextSupplier );
      return ejb;
    }
    catch( Throwable e ) {
      throw new TsInternalErrorRtException( e, ERR_REPLACE_PROXY, cause( e ) );
    }
  }
}
