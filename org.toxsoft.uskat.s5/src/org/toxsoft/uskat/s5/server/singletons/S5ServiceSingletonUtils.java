package org.toxsoft.uskat.s5.server.singletons;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.singletons.IS5Resources.*;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Открытые методы доступам к последовательностям значений
 *
 * @author mvk
 */
public class S5ServiceSingletonUtils {

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает исполнителя {@link ManagedExecutorService} из контекста имен JNDI
   *
   * @param aJndiExecutorName String имя исполнителя
   * @return {@link ManagedExecutorService} исполнитель
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException исполнитель не найден
   */
  public static ManagedExecutorService lookupExecutor( String aJndiExecutorName ) {
    TsNullArgumentRtException.checkNull( aJndiExecutorName );
    try {
      // Поиск исполнителя сервера
      return InitialContext.doLookup( aJndiExecutorName );
    }
    catch( NamingException e ) {
      throw new TsIllegalArgumentRtException( e, MSG_ERR_EXECUTOR_NOT_FOUND, aJndiExecutorName, cause( e ) );
    }
    catch( Throwable e ) {
      throw new TsIllegalArgumentRtException( e, MSG_ERR_EXECUTOR_UNEXPECTED, aJndiExecutorName, cause( e ) );
    }
  }
}
