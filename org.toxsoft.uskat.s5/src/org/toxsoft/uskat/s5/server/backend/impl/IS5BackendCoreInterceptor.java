package org.toxsoft.uskat.s5.server.backend.impl;

import javax.ejb.Local;

import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.interceptors.IS5Interceptor;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;

/**
 * Перехватчик операций ядра backend.
 * <p>
 * В отличии от событий {@link SkEvent}, события об операциях {@link IS5BackendCoreInterceptor} передаются в режиме
 * раннего оповещения (в рамках выполняемой транзакции) и позвляют перехватчику повлиять на конечный результат
 * проводимой операции над объектами. Например, клиент(перехватчик) может запретить/отменить выполнение операции.
 * <p>
 * Все методы реализации интерфейса {@link IS5BackendCoreInterceptor} должны быть иметь аннатоцию:
 * &#064;TransactionAttribute( TransactionAttributeType.MANDATORY ).
 *
 * @author mvk
 */
@Local
public interface IS5BackendCoreInterceptor
    extends IS5Interceptor {

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendCoreSingleton#setConnection(ISkConnection)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aOldConnection {@link ISkConnection} старое соединение с ядром бекенда. null: не было установлено
   * @param aNewConnection {@link ISkConnection} новое соединение с ядром бекенда
   * @return boolean <b>true</b> разрешить дальнейшее выполнение операции;<b>false</b> отменить выполнение операции.
   */
  boolean beforeSetConnection( ISkConnection aOldConnection, ISkConnection aNewConnection );

  /**
   * Вызывается ПОСЛЕ {@link IS5BackendCoreSingleton#setConnection(ISkConnection)}, но до завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aOldConnection {@link ISkConnection} старое соединение с ядром бекенда. null: не было установлено
   * @param aNewConnection {@link ISkConnection} новое соединение с ядром бекенда
   * @throws TsIllegalStateRtException отменить изменения сделанные методом
   *           {@link IS5BackendCoreSingleton#setConnection(ISkConnection)} (откат транзакции)
   */
  void afterSetConnection( ISkConnection aOldConnection, ISkConnection aNewConnection );

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
  /**
   * Вызов перехватчиков операции {@link IS5BackendCoreInterceptor#beforeSetConnection(ISkConnection, ISkConnection)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5BackendCoreInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aOldConnection {@link ISkConnection} старое соединение с ядром бекенда. null: не было установлено
   * @param aNewConnection {@link ISkConnection} новое соединение с ядром бекенда
   * @return boolean <b>true</b> разрешить дальнейшее выполнение операции;<b>false</b> отменить выполнение операции.
   * @throws TsNullArgumentRtException любой аргумент (кроме aOldConnection) = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendCoreSingleton#setConnection(ISkConnection)}
   */
  static boolean callBeforeSetConnectionInterceptors(
      S5InterceptorSupport<IS5BackendCoreInterceptor> aInterceptorSupport, ISkConnection aOldConnection,
      ISkConnection aNewConnection ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aNewConnection );
    for( IS5BackendCoreInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      if( !interceptor.beforeSetConnection( aOldConnection, aNewConnection ) ) {
        return false;
      }
    }
    return true;
  }

  /**
   * Вызов перехватчиков операции {@link IS5BackendCoreInterceptor#afterSetConnection(ISkConnection, ISkConnection)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5BackendCoreInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aOldConnection {@link ISkConnection} старое соединение с ядром бекенда. null: не было установлено
   * @param aNewConnection {@link ISkConnection} новое соединение с ядром бекенда
   * @throws TsNullArgumentRtException любой аргумент (кроме aOldConnection) = null
   * @throws TsIllegalStateRtException отменить изменения сделанные методом
   *           {@link IS5BackendCoreSingleton#setConnection(ISkConnection)} (откат транзакции)
   */
  static void callAfterSetConnectionInterceptors( S5InterceptorSupport<IS5BackendCoreInterceptor> aInterceptorSupport,
      ISkConnection aOldConnection, ISkConnection aNewConnection ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aNewConnection );
    for( IS5BackendCoreInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.afterSetConnection( aOldConnection, aNewConnection );
    }
  }
}
