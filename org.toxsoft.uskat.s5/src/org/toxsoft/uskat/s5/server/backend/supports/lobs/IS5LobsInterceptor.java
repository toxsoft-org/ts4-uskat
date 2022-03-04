package org.toxsoft.uskat.s5.server.backend.supports.lobs;

import javax.ejb.Local;

import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.interceptors.IS5Interceptor;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;

import ru.uskat.common.dpu.rt.events.SkEvent;
import ru.uskat.legacy.IdPair;

/**
 * Перехватчик операций над большими объектами (LOB).
 * <p>
 * В отличии от событий {@link SkEvent}, события об операциях {@link IS5LobsInterceptor} передаются в режиме раннего
 * оповещения (в рамках выполняемой транзакции) и позвляют перехватчику повлиять на конечный результат проводимой
 * операции над объектами. Например, клиент(перехватчик) может запретить/отменить выполнение операции.
 * <p>
 * Все методы реализации интерфейса {@link IS5LobsInterceptor} должны быть иметь аннатоцию: &#064;TransactionAttribute(
 * TransactionAttributeType.MANDATORY ).
 *
 * @author mvk
 */
@Local
public interface IS5LobsInterceptor
    extends IS5Interceptor {

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendLobsSingleton#listLobIds()}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aLobs {@link IListEdit}&lt;{link IdPair}&gt; список объектов найденных ранее интерсепторами
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLobsSingleton#listLobIds()}
   */
  void beforeListLobIds( IListEdit<IdPair> aLobs );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendLobsSingleton#listLobIds()}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aLobs {@link IListEdit}&lt;{link IdPair}&gt; список объектов найденных ранее интерсепторами
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLobsSingleton#listLobIds()}
   */
  void afterListLobIds( IListEdit<IdPair> aLobs );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendLobsSingleton#readClob(IdPair)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aId {@link IdPair} идентификатор lob-данного
   * @param aClob String объект текстовое представление lob-данного считанное интерсепторами
   * @return String текстовое представление lob-данного
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLobsSingleton#readClob(IdPair)}
   */
  String beforeReadClob( IdPair aId, String aClob );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendLobsSingleton#readClob(IdPair)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aId {@link IdPair} идентификатор lob-данного
   * @param aClob String объект текстовое представление lob-данного считанное ранее службой или интерсепторами
   * @return String текстовое представление lob-данного
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLobsSingleton#readClob(IdPair)}
   */
  String afterReadClob( IdPair aId, String aClob );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendLobsSingleton#writeClob(IdPair, String)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aId {@link IdPair} идентификатор lob-данного
   * @param aValue String текстовое представление значения
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLobsSingleton#writeClob(IdPair, String)}
   */
  void beforeWriteClob( IdPair aId, String aValue );

  /**
   * Вызывается ПОСЛЕ {@link IS5BackendLobsSingleton#writeClob(IdPair, String)}, но до завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aId {@link IdPair} идентификатор lob-данного
   * @param aValue String текстовое представление значения
   * @throws TsIllegalStateRtException отменить изменения сделанные методом
   *           {@link IS5BackendLobsSingleton#writeClob(IdPair, String)} (откат транзакции)
   */
  void afterWriteClob( IdPair aId, String aValue );

  /**
   * Вызывается ДО выполнения метода {@link IS5BackendLobsSingleton#removeClob(IdPair)}
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aId {@link IdPair} идентификатор lob-данного
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLobsSingleton#removeClob(IdPair)}
   */
  void beforeRemoveClob( IdPair aId );

  /**
   * Вызывается ПОСЛЕ {@link IS5BackendLobsSingleton#removeClob(IdPair)}, но до завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aId {@link IdPair} идентификатор lob-данного
   * @throws TsIllegalStateRtException отменить изменения сделанные методом
   *           {@link IS5BackendLobsSingleton#removeClob(IdPair)} (откат транзакции)
   */
  void afterRemoveClob( IdPair aId );

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
  /**
   * Вызов перехватчиков операции {@link IS5LobsInterceptor#beforeListLobIds(IListEdit)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LobsInterceptor}&gt; поддержка перехватчиков
   * @param aLobs {@link IListEdit}&lt;{link IdPair}&gt; список идентификаторов найденных ранее интерсепторами
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLobsSingleton#listLobIds()}
   */
  static void callBeforeListLobIds( S5InterceptorSupport<IS5LobsInterceptor> aInterceptorSupport,
      IListEdit<IdPair> aLobs ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aLobs );
    for( IS5LobsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.beforeListLobIds( aLobs );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5LobsInterceptor#afterListLobIds(IListEdit)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LobsInterceptor}&gt; поддержка перехватчиков
   * @param aLobs {@link IListEdit}&lt;{link IdPair}&gt; список идентификаторов найденных ранее интерсепторами и службой
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLobsSingleton#listLobIds()}
   */
  static void callAfterListLobIds( S5InterceptorSupport<IS5LobsInterceptor> aInterceptorSupport,
      IListEdit<IdPair> aLobs ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aLobs );
    for( IS5LobsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.afterListLobIds( aLobs );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5LobsInterceptor#beforeReadClob(IdPair, String)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LobsInterceptor}&gt; поддержка перехватчиков
   * @param aId {@link IdPair} идентификатор lob-данного
   * @return String текстовое представление lob-данного
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLobsSingleton#readClob(IdPair)}
   */
  static String callBeforeReadClob( S5InterceptorSupport<IS5LobsInterceptor> aInterceptorSupport, IdPair aId ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aId );
    String retValue = null;
    for( IS5LobsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.beforeReadClob( aId, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5LobsInterceptor#afterReadClob(IdPair, String)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LobsInterceptor}&gt; поддержка перехватчиков
   * @param aId {@link IdPair} идентификатор lob-данного
   * @param aClob String объект текстовое представление lob-данного считанное ранее службой или интерсепторами
   * @return String текстовое представление lob-данного
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLobsSingleton#readClob(IdPair)}
   */
  static String callAfterReadClob( S5InterceptorSupport<IS5LobsInterceptor> aInterceptorSupport, IdPair aId,
      String aClob ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aId );
    String retValue = aClob;
    for( IS5LobsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      retValue = interceptor.afterReadClob( aId, retValue );
    }
    return retValue;
  }

  /**
   * Вызов перехватчиков операции {@link IS5LobsInterceptor#beforeWriteClob(IdPair, String)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LobsInterceptor}&gt; поддержка перехватчиков
   * @param aId {@link IdPair} идентификатор lob-данного
   * @param aValue String текстовое представление значения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLobsSingleton#writeClob(IdPair, String)}
   */
  static void callBeforeWriteClobInterceptors( S5InterceptorSupport<IS5LobsInterceptor> aInterceptorSupport, IdPair aId,
      String aValue ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aId, aValue );
    for( IS5LobsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.beforeWriteClob( aId, aValue );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5LobsInterceptor#afterWriteClob(IdPair, String)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LobsInterceptor}&gt; поддержка перехватчиков
   * @param aId {@link IdPair} идентификатор lob-данного
   * @param aValue String текстовое представление значения
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException отменить изменения сделанные методом
   *           {@link IS5BackendLobsSingleton#writeClob(IdPair, String)} (откат транзакции)
   */
  static void callAfterWriteClobInterceptors( S5InterceptorSupport<IS5LobsInterceptor> aInterceptorSupport, IdPair aId,
      String aValue ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aId, aValue );
    for( IS5LobsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.afterWriteClob( aId, aValue );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5LobsInterceptor#beforeRemoveClob(IdPair)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LobsInterceptor}&gt; поддержка перехватчиков
   * @param aId {@link IdPair} идентификатор lob-данного
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить выполнение {@link IS5BackendLobsSingleton#removeClob(IdPair)}
   */
  static void callBeforeRemoveClobInterceptors( S5InterceptorSupport<IS5LobsInterceptor> aInterceptorSupport,
      IdPair aId ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aId );
    for( IS5LobsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.beforeRemoveClob( aId );
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5LobsInterceptor#afterRemoveClob(IdPair)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5LobsInterceptor}&gt; поддержка перехватчиков
   * @param aId {@link IdPair} идентификатор lob-данного
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException отменить изменения сделанные методом
   *           {@link IS5BackendLobsSingleton#removeClob(IdPair)} (откат транзакции)
   */
  static void callAfterRemoveClobInterceptors( S5InterceptorSupport<IS5LobsInterceptor> aInterceptorSupport,
      IdPair aId ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aId );
    for( IS5LobsInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      interceptor.afterRemoveClob( aId );
    }
  }
}
