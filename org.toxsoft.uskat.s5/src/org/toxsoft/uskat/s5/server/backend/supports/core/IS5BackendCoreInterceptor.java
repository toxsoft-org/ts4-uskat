package org.toxsoft.uskat.s5.server.backend.supports.core;

import javax.ejb.*;

import org.toxsoft.core.tslib.bricks.validator.vrl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.s5.server.backend.*;
import org.toxsoft.uskat.s5.server.interceptors.*;

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
   * Вызывается ДО выполнения метода {@link IS5BackendCoreSingleton#setMode(ES5ServerMode)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   * <p>
   * Состояние {@link ES5ServerMode#STARTING} устанавливается до регистрации перехватчиков, поэтому они его не получают.
   *
   * @param aOldMode {@link ES5ServerMode} старое состояние сервера.
   * @param aNewMode {@link ES5ServerMode} новое состояние сервера.
   * @param aValidationList {@link IVrListEdit} список-приемник проверки возможности изменения режима сервера
   */
  void beforeChangeServerMode( ES5ServerMode aOldMode, ES5ServerMode aNewMode, IVrListEdit aValidationList );

  /**
   * Вызывается ПОСЛЕ {@link IS5BackendCoreSingleton#setMode(ES5ServerMode)}, но до завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   * <p>
   * Состояние {@link ES5ServerMode#STARTING} устанавливается до регистрации перехватчиков, поэтому они его не получают.
   *
   * @param aOldMode {@link ES5ServerMode} старое состояние сервера.
   * @param aNewMode {@link ES5ServerMode} новое состояние сервера.
   * @throws TsIllegalStateRtException отменить изменения сделанные методом
   *           {@link IS5BackendCoreSingleton#setMode(ES5ServerMode)} (откат транзакции)
   */
  void afterChangeServerMode( ES5ServerMode aOldMode, ES5ServerMode aNewMode );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendCoreSingleton#setSharedConnection(ISkConnection)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aConnection {@link ISkConnection} новое соединение с ядром бекенда
   * @param aValidationList {@link IVrListEdit} список-приемник проверки возможности замены соединения.
   */
  void beforeSetSharedConnection( ISkConnection aConnection, IVrListEdit aValidationList );

  /**
   * Вызывается ПОСЛЕ {@link IS5BackendCoreSingleton#setSharedConnection(ISkConnection)}, но до завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aConnection {@link ISkConnection} новое соединение с ядром бекенда
   * @throws TsIllegalStateRtException отменить изменения сделанные методом
   *           {@link IS5BackendCoreSingleton#setSharedConnection(ISkConnection)} (откат транзакции)
   */
  void afterSetSharedConnection( ISkConnection aConnection );

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
  /**
   * Вызов перехватчиков операции
   * {@link IS5BackendCoreInterceptor#beforeChangeServerMode(ES5ServerMode, ES5ServerMode, IVrListEdit)}.
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5BackendCoreInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aOldMode {@link ES5ServerMode} старое состояние сервера. null: старого состояние не существует (запуск).
   * @param aNewMode {@link ES5ServerMode} новое состояние сервера.
   * @return {@link IVrList} результат проверки возможности изменения режима.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendCoreSingleton#setMode(ES5ServerMode)}
   */
  static IVrList callBeforeChangeServerModeInterceptors(
      S5InterceptorSupport<IS5BackendCoreInterceptor> aInterceptorSupport, ES5ServerMode aOldMode,
      ES5ServerMode aNewMode ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aOldMode, aNewMode );
    IVrListEdit retValue = new VrList();
    for( IS5BackendCoreInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.beforeChangeServerMode( aOldMode, aNewMode, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5BackendCoreInterceptor#afterChangeServerMode(ES5ServerMode, ES5ServerMode)}.
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5BackendCoreInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aOldMode {@link ES5ServerMode} старое состояние сервера.
   * @param aNewMode {@link ES5ServerMode} новое состояние сервера.
   * @throws TsIllegalStateRtException отменить изменения сделанные методом
   *           {@link IS5BackendCoreSingleton#setMode(ES5ServerMode)} (откат транзакции)
   */
  static void callAfterChangeServerModeInterceptors(
      S5InterceptorSupport<IS5BackendCoreInterceptor> aInterceptorSupport, ES5ServerMode aOldMode,
      ES5ServerMode aNewMode ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aOldMode, aNewMode );
    for( IS5BackendCoreInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.afterChangeServerMode( aOldMode, aNewMode );
    }
  }

  /**
   * Вызов перехватчиков операции
   * {@link IS5BackendCoreInterceptor#beforeSetSharedConnection(ISkConnection, IVrListEdit)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5BackendCoreInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aConnection {@link ISkConnection} новое соединение с ядром бекенда
   * @return {@link IVrList} результат проверки возможности замены соединения.
   * @throws TsNullArgumentRtException любой аргумент (кроме aOldConnection) = null
   * @throws TsIllegalStateRtException запретить выполнение
   *           {@link IS5BackendCoreSingleton#setSharedConnection(ISkConnection)}
   */
  static IVrList callBeforeSetSharedConnectionInterceptors(
      S5InterceptorSupport<IS5BackendCoreInterceptor> aInterceptorSupport, ISkConnection aConnection ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aConnection );
    IVrListEdit retValue = new VrList();
    for( IS5BackendCoreInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.beforeSetSharedConnection( aConnection, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5BackendCoreInterceptor#afterSetSharedConnection(ISkConnection)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5BackendCoreInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aConnection {@link ISkConnection} новое соединение с ядром бекенда
   * @throws TsNullArgumentRtException любой аргумент (кроме aOldConnection) = null
   * @throws TsIllegalStateRtException отменить изменения сделанные методом
   *           {@link IS5BackendCoreSingleton#setSharedConnection(ISkConnection)} (откат транзакции)
   */
  static void callAfterSetSharedConnectionInterceptors(
      S5InterceptorSupport<IS5BackendCoreInterceptor> aInterceptorSupport, ISkConnection aConnection ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aConnection );
    for( IS5BackendCoreInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.afterSetSharedConnection( aConnection );
    }
  }
}
