package org.toxsoft.uskat.s5.server.backend.supports.clobs;

import javax.ejb.Local;

import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.s5.server.interceptors.IS5Interceptor;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;

/**
 * Перехватчик операций над большими объектами (LOB).
 * <p>
 * В отличии от событий {@link SkEvent}, события об операциях {@link IS5ClobsInterceptor} передаются в режиме раннего
 * оповещения (в рамках выполняемой транзакции) и позвляют перехватчику повлиять на конечный результат проводимой
 * операции над объектами. Например, клиент(перехватчик) может запретить/отменить выполнение операции.
 * <p>
 * Все методы реализации интерфейса {@link IS5ClobsInterceptor} должны быть иметь аннатоцию: &#064;TransactionAttribute(
 * TransactionAttributeType.MANDATORY ).
 *
 * @author mvk
 */
@Local
public interface IS5ClobsInterceptor
    extends IS5Interceptor {

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendClobsSingleton#readClob(Gwid)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aGwid {@link Gwid} идентификатор конкретного clob-данного
   * @param aClob String объект текстовое представление lob-данного считанное интерсепторами
   * @return String текстовое представление lob-данного
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendClobsSingleton#readClob(Gwid)}
   */
  String beforeReadClob( Gwid aGwid, String aClob );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendClobsSingleton#readClob(Gwid)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aGwid {@link Gwid} идентификатор конкретного clob-данного
   * @param aClob String объект текстовое представление lob-данного считанное ранее службой или интерсепторами
   * @return String текстовое представление lob-данного
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendClobsSingleton#readClob(Gwid)}
   */
  String afterReadClob( Gwid aGwid, String aClob );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendClobsSingleton#writeClob(Gwid, String)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aGwid {@link Gwid} идентификатор конкретного clob-данного
   * @param aValue String текстовое представление значения
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendClobsSingleton#writeClob(Gwid, String)}
   */
  void beforeWriteClob( Gwid aGwid, String aValue );

  /**
   * Вызывается ПОСЛЕ {@link IS5BackendClobsSingleton#writeClob(Gwid, String)}, но до завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aGwid {@link Gwid} идентификатор конкретного clob-данного
   * @param aValue String текстовое представление значения
   * @throws TsIllegalStateRtException отменить изменения сделанные методом
   *           {@link IS5BackendClobsSingleton#writeClob(Gwid, String)} (откат транзакции)
   */
  void afterWriteClob( Gwid aGwid, String aValue );

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
  /**
   * Вызов перехватчиков операции {@link IS5ClobsInterceptor#beforeReadClob(Gwid , String)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ClobsInterceptor}&gt; поддержка перехватчиков
   * @param aGwid {@link Gwid} идентификатор конкретного clob-данного
   * @return String текстовое представление lob-данного
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendClobsSingleton#readClob(Gwid)}
   */
  static String callBeforeReadClob( S5InterceptorSupport<IS5ClobsInterceptor> aInterceptorSupport, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aGwid );
    String retValue = null;
    for( IS5ClobsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.beforeReadClob( aGwid, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5ClobsInterceptor#afterReadClob(Gwid, String)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ClobsInterceptor}&gt; поддержка перехватчиков
   * @param aGwid {@link Gwid} идентификатор конкретного clob-данного
   * @param aClob String объект текстовое представление lob-данного считанное ранее службой или интерсепторами
   * @return String текстовое представление lob-данного
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendClobsSingleton#readClob(Gwid)}
   */
  static String callAfterReadClob( S5InterceptorSupport<IS5ClobsInterceptor> aInterceptorSupport, Gwid aGwid,
      String aClob ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aGwid );
    String retValue = aClob;
    for( IS5ClobsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.afterReadClob( aGwid, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5ClobsInterceptor#beforeWriteClob(Gwid, String)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ClobsInterceptor}&gt; поддержка перехватчиков
   * @param aGwid {@link Gwid} идентификатор конкретного clob-данного
   * @param aValue String текстовое представление значения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendClobsSingleton#writeClob(Gwid, String)}
   */
  static void callBeforeWriteClobInterceptors( S5InterceptorSupport<IS5ClobsInterceptor> aInterceptorSupport,
      Gwid aGwid, String aValue ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aGwid, aValue );
    for( IS5ClobsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.beforeWriteClob( aGwid, aValue );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5ClobsInterceptor#afterWriteClob(Gwid, String)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5ClobsInterceptor}&gt; поддержка перехватчиков
   * @param aGwid {@link Gwid} идентификатор конкретного clob-данного
   * @param aValue String текстовое представление значения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException отменить изменения сделанные методом
   *           {@link IS5BackendClobsSingleton#writeClob(Gwid, String)} (откат транзакции)
   */
  static void callAfterWriteClobInterceptors( S5InterceptorSupport<IS5ClobsInterceptor> aInterceptorSupport, Gwid aGwid,
      String aValue ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aGwid, aValue );
    for( IS5ClobsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.afterWriteClob( aGwid, aValue );
    }
  }

}
