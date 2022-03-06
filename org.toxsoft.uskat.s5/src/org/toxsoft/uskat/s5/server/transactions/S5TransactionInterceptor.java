package org.toxsoft.uskat.s5.server.transactions;

import java.io.Serializable;
import java.lang.reflect.Method;

import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;

import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSession;

/**
 * Интерсептор синглтона мониторинга транзакциями.
 * <p>
 * TODO: mvk ??? {@link Serializable}: каким-то образом {@link S5TransactionInterceptor} попадает в контекст сессии
 * {@link S5BackendSession} и требует чтобы он мог сериализоваться - пока это не мешает, но надо разобраться что
 * происходит
 *
 * @author mvk
 */
public class S5TransactionInterceptor
    implements Serializable {

  private static final long serialVersionUID = 157157L;

  @EJB
  IS5TransactionDetectorSingleton transactionDetector;

  /**
   * Перехват доступка к бизнес методам компонента
   *
   * @param aContext {@link InvocationContext} - контекст вызова
   * @return {@link InvocationContext#proceed()}
   * @throws Exception - исключение других интерсепторов или бизнес логики компонента.
   * @throws TsIllegalStateRtException - монопольный доступ предоставлен другому клиенту
   * @throws TsIllegalStateRtException - монопольный доступ не был предоставлен вызывающему клиенту
   */
  @AroundInvoke
  public Object interceptor( InvocationContext aContext )
      throws Exception {
    Object target = aContext.getTarget();
    Method method = aContext.getMethod();
    Object params[] = aContext.getParameters();
    transactionDetector.onCallBusinessMethod( target, method, params );
    return aContext.proceed();
  }

}
