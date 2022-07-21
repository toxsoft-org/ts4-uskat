package org.toxsoft.uskat.s5.server.backend.supports.currdata.impl;

import javax.ejb.Local;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.IS5BackendCurrDataSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.interceptors.IS5Interceptor;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;

/**
 * Перехватчик операций определения текущих данных в системе и записи их значений.
 * <p>
 * В отличии от событий {@link SkEvent}, события об операциях {@link IS5CurrDataInterceptor} передаются в режиме раннего
 * оповещения (в рамках выполняемой транзакции) и позвляют перехватчику повлиять на конечный результат проводимой
 * операции над объектами. Например, клиент(перехватчик) может запретить/отменить выполнение операции.
 * <p>
 * Все методы реализации интерфейса {@link IS5CurrDataInterceptor} должны быть иметь аннатоцию:
 * &#064;TransactionAttribute( TransactionAttributeType.MANDATORY ).
 *
 * @author mvk
 */
@Local
public interface IS5CurrDataInterceptor
    extends IS5Interceptor {

  /**
   * Вызывается ДО изменения набора текущих данных в системе
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   *
   * @param aRemovedGwids {@link IGwidList} список идентификаторов удаляемых данных
   * @param aAddedGwids {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; карта добавляемых текущих данных.<br>
   *          Ключ: идентификтор данного;<br>
   *          Значение: значение текущего данного (по умолчанию) {@link Gwid} идентификатор РВданного;<br>
   *          Integer индекс РВданного в родительском наборе
   * @return boolean <b>true</b> разрешить дальнейшее выполнение операции;<b>false</b> отменить выполнение операции.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  boolean beforeReconfigureCurrData( IGwidList aRemovedGwids, IMap<Gwid, IAtomicValue> aAddedGwids );

  /**
   * Вызывается ПОСЛЕ изменения набора текущих данных в системе, но ДО завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции, но все попытки ее отмены (через поднятие исключения в
   * {@link #afterReconfigureCurrData(IGwidList, IMap)}) будут игнорироваться.
   *
   * @param aRemovedGwids {@link IGwidList} список идентификаторов удаляемых данных
   * @param aAddedGwids {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; карта добавляемых текущих данных.<br>
   *          Ключ: идентификтор данного;<br>
   *          Значение: значение текущего данного (по умолчанию) {@link Gwid} идентификатор РВданного;<br>
   *          Integer индекс РВданного в родительском наборе
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void afterReconfigureCurrData( IGwidList aRemovedGwids, IMap<Gwid, IAtomicValue> aAddedGwids );

  /**
   * Вызывается ДО конфигурирования набора текущих данных читаемых клиентом
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   * <p>
   * Внимание: пустой список или <code>null</code> в качестве первого аргумента <code>aToRemove</code> имеют совершенно
   * разный смысл! Пустой список означает, что никакие РВданные не удаляются из списка интересующих клиента, в то время,
   * как <code>null</code> означает, что <b>все</b> до этого интересующие РВданные более не нужны, и должны быть удалены
   * из списка интересующих клиента.
   *
   * @param aFrontend {@link IS5FrontendRear} фронтенд представляющий клиента
   * @param aToRemove {@link IGwidList} - список ключей удаляемых РВданных или <code>null</code> для удаления всех
   * @param aToAdd {@link IGwidList} - список интересующих клиента данных
   * @return boolean <b>true</b> разрешить дальнейшее выполнение операции;<b>false</b> отменить выполнение операции.
   * @throws TsNullArgumentRtException <code>aToAdd</code> == null
   */
  boolean beforeConfigureCurrDataReader( IS5FrontendRear aFrontend, IGwidList aToRemove, IGwidList aToAdd );

  /**
   * Вызывается ПОСЛЕ конфигурирования набора текущих данных читаемых клиентом.
   * <p>
   * Событие формируется в открытой транзакции, но все попытки ее отмены (через поднятие исключения в
   * {@link #afterConfigureCurrDataReader(IS5FrontendRear, IGwidList, IGwidList)}) будут игнорироваться.
   * <p>
   * <p>
   * Внимание: пустой список или <code>null</code> в качестве первого аргумента <code>aToRemove</code> имеют совершенно
   * разный смысл! Пустой список означает, что никакие РВданные не удаляются из списка интересующих клиента, в то время,
   * как <code>null</code> означает, что <b>все</b> до этого интересующие РВданные более не нужны, и должны быть удалены
   * из списка интересующих клиента.
   *
   * @param aFrontend {@link IS5FrontendRear} фронтенд представляющий клиента
   * @param aToRemove {@link IGwidList} - список ключей удаляемых РВданных или <code>null</code> для удаления всех
   * @param aToAdd {@link IGwidList} - список интересующих клиента данных
   * @throws TsNullArgumentRtException <code>aToAdd</code> == null
   */
  void afterConfigureCurrDataReader( IS5FrontendRear aFrontend, IGwidList aToRemove, IGwidList aToAdd );

  /**
   * Вызывается ДО конфигурирования набора текущих данных формируемых клиентом.
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   * <p>
   * Внимание: пустой список или <code>null</code> в качестве первого аргумента <code>aToRemove</code> имеют совершенно
   * разный смысл! Пустой список означает, что никакие РВданные не удаляются из списка интересующих клиента, в то время,
   * как <code>null</code> означает, что <b>все</b> до этого интересующие РВданные более не нужны, и должны быть удалены
   * из списка интересующих клиента.
   *
   * @param aFrontend {@link IS5FrontendRear} фронтенд представляющий клиента
   * @param aToRemove {@link IGwidList} - список ключей удаляемых РВданных или <code>null</code> для удаления всех
   * @param aToAdd {@link IGwidList} - список интересующих клиента данных
   * @return boolean <b>true</b> разрешить дальнейшее выполнение операции;<b>false</b> отменить выполнение операции.
   * @throws TsNullArgumentRtException <code>aToAdd</code> == null
   */
  boolean beforeConfigureCurrDataWriter( IS5FrontendRear aFrontend, IGwidList aToRemove, IGwidList aToAdd );

  /**
   * Вызывается ПОСЛЕ конфигурирования набора текущих данных формируемых клиентом.
   * <p>
   * Событие формируется в открытой транзакции, но все попытки ее отмены (через поднятие исключения в
   * {@link #afterConfigureCurrDataWriter(IS5FrontendRear, IGwidList, IGwidList)}) будут игнорироваться.
   * <p>
   * <p>
   * Внимание: пустой список или <code>null</code> в качестве первого аргумента <code>aToRemove</code> имеют совершенно
   * разный смысл! Пустой список означает, что никакие РВданные не удаляются из списка интересующих клиента, в то время,
   * как <code>null</code> означает, что <b>все</b> до этого интересующие РВданные более не нужны, и должны быть удалены
   * из списка интересующих клиента.
   *
   * @param aFrontend {@link IS5FrontendRear} фронтенд представляющий клиента
   * @param aToRemove {@link IGwidList} - список ключей удаляемых РВданных или <code>null</code> для удаления всех
   * @param aToAdd {@link IGwidList} - список интересующих клиента данных
   * @throws TsNullArgumentRtException <code>aToAdd</code> == null
   */
  void afterConfigureCurrDataWriter( IS5FrontendRear aFrontend, IGwidList aToRemove, IGwidList aToAdd );

  /**
   * Вызывается ДО записи текущих данных в систему
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   * <p>
   * Аргументом является карта, в которой ключи, это назначенные ранее сервером int-ы, присланные в качестве ответа на
   * {@link IS5BackendCurrDataSingleton#reconfigure(IGwidList, IMap)} или
   * {@link IS5BackendCurrDataSingleton#configureCurrDataWriter(IS5FrontendRear, IGwidList, IGwidList)}.
   *
   * @param aValues {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; - записываемые значения
   * @return boolean <b>true</b> разрешить дальнейшее выполнение операции;<b>false</b> отменить выполнение операции.
   * @throws TsNullArgumentRtException аргумент = null
   */
  boolean beforeWriteCurrData( IMap<Gwid, IAtomicValue> aValues );

  /**
   * Вызывается ПОСЛЕ записи текущих данных в систему, но ДО завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции, но все попытки ее отмены (через поднятие исключения в
   * {@link #afterWriteCurrData(IMap)}) будут игнорироваться.
   * <p>
   * Аргументом является карта, в которой ключи, это назначенные ранее сервером int-ы, присланные в качестве ответа на
   * {@link IS5BackendCurrDataSingleton#reconfigure(IGwidList, IMap)} или
   * {@link IS5BackendCurrDataSingleton#configureCurrDataWriter(IS5FrontendRear, IGwidList, IGwidList)}.
   *
   * @param aValues {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; - записываемые значения
   * @throws TsNullArgumentRtException аргумент = null
   */
  void afterWriteCurrData( IMap<Gwid, IAtomicValue> aValues );

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
  /**
   * Вызов перехватчиков операции {@link IS5CurrDataInterceptor#beforeReconfigureCurrData(IGwidList, IMap)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5CurrDataInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aRemovedGwids {@link IGwidList} список идентификаторов удаляемых данных
   * @param aAddedGwids {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; карта добавляемых текущих данных.<br>
   *          Ключ: идентификтор данного;<br>
   *          Значение: значение текущего данного (по умолчанию) {@link Gwid} идентификатор РВданного;<br>
   *          Integer индекс РВданного в родительском наборе
   * @return boolean <b>true</b> разрешить дальнейшее выполнение операции;<b>false</b> отменить выполнение операции.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static boolean callBeforeReconfigureCurrData( S5InterceptorSupport<IS5CurrDataInterceptor> aInterceptorSupport,
      IGwidList aRemovedGwids, IMap<Gwid, IAtomicValue> aAddedGwids ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aRemovedGwids, aAddedGwids );
    for( IS5CurrDataInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      if( !interceptor.beforeReconfigureCurrData( aRemovedGwids, aAddedGwids ) ) {
        return false;
      }
    }
    return true;
  }

  /**
   * Вызов перехватчиков операции {@link IS5CurrDataInterceptor#afterReconfigureCurrData(IGwidList, IMap)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5CurrDataInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aRemovedGwids {@link IGwidList} список идентификаторов удаляемых данных
   * @param aAddedGwids {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; карта добавляемых текущих данных.<br>
   *          Ключ: идентификтор данного;<br>
   *          Значение: значение текущего данного (по умолчанию) {@link Gwid} идентификатор РВданного;<br>
   *          Integer индекс РВданного в родительском наборе
   * @param aLogger {@link ILogger} журнал работы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void callAfterReconfigureCurrData( S5InterceptorSupport<IS5CurrDataInterceptor> aInterceptorSupport,
      IGwidList aRemovedGwids, IMap<Gwid, IAtomicValue> aAddedGwids, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aRemovedGwids, aAddedGwids, aLogger );
    for( IS5CurrDataInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      try {
        interceptor.afterReconfigureCurrData( aRemovedGwids, aAddedGwids );
      }
      catch( Throwable e ) {
        aLogger.error( e );
      }
    }
  }

  /**
   * Вызов перехватчиков операции
   * {@link IS5CurrDataInterceptor#beforeConfigureCurrDataReader(IS5FrontendRear, IGwidList, IGwidList)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5CurrDataInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aFrontend {@link IS5FrontendRear} фронтенд представляющий клиента
   * @param aToRemove {@link IGwidList} - список ключей удаляемых РВданных или <code>null</code> для удаления всех
   * @param aToAdd {@link IGwidList} - список интересующих клиента данных
   * @return boolean <b>true</b> разрешить дальнейшее выполнение операции;<b>false</b> отменить выполнение операции.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static boolean callBeforeConfigureCurrDataReader( S5InterceptorSupport<IS5CurrDataInterceptor> aInterceptorSupport,
      IS5FrontendRear aFrontend, IGwidList aToRemove, IGwidList aToAdd ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aFrontend, aToAdd );
    for( IS5CurrDataInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      if( !interceptor.beforeConfigureCurrDataReader( aFrontend, aToRemove, aToAdd ) ) {
        return false;
      }
    }
    return true;
  }

  /**
   * Вызов перехватчиков операции
   * {@link IS5CurrDataInterceptor#afterConfigureCurrDataReader(IS5FrontendRear, IGwidList, IGwidList)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5CurrDataInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aFrontend {@link IS5FrontendRear} фронтенд представляющий клиента
   * @param aToRemove {@link IGwidList} - список ключей удаляемых РВданных или <code>null</code> для удаления всех
   * @param aToAdd {@link IGwidList} - список интересующих клиента данных
   * @param aLogger {@link ILogger} журнал работы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void callAfterConfigureCurrDataReader( S5InterceptorSupport<IS5CurrDataInterceptor> aInterceptorSupport,
      IS5FrontendRear aFrontend, IGwidList aToRemove, IGwidList aToAdd, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aFrontend, aToAdd );
    for( IS5CurrDataInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      try {
        interceptor.afterConfigureCurrDataReader( aFrontend, aToRemove, aToAdd );
      }
      catch( Throwable e ) {
        aLogger.error( e );
      }
    }
  }

  /**
   * Вызов перехватчиков операции
   * {@link IS5CurrDataInterceptor#beforeConfigureCurrDataWriter(IS5FrontendRear, IGwidList, IGwidList)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5CurrDataInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aFrontend {@link IS5FrontendRear} фронтенд представляющий клиента
   * @param aToRemove {@link IGwidList} - список ключей удаляемых РВданных или <code>null</code> для удаления всех
   * @param aToAdd {@link IGwidList} - список интересующих клиента данных
   * @return boolean <b>true</b> разрешить дальнейшее выполнение операции;<b>false</b> отменить выполнение операции.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static boolean callBeforeConfigureCurrDataWriter( S5InterceptorSupport<IS5CurrDataInterceptor> aInterceptorSupport,
      IS5FrontendRear aFrontend, IGwidList aToRemove, IGwidList aToAdd ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aFrontend, aToAdd );
    for( IS5CurrDataInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      if( !interceptor.beforeConfigureCurrDataWriter( aFrontend, aToRemove, aToAdd ) ) {
        return false;
      }
    }
    return true;
  }

  /**
   * Вызов перехватчиков операции
   * {@link IS5CurrDataInterceptor#afterConfigureCurrDataWriter(IS5FrontendRear, IGwidList, IGwidList)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5CurrDataInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aFrontend {@link IS5FrontendRear} фронтенд представляющий клиента
   * @param aToRemove {@link IGwidList} - список ключей удаляемых РВданных или <code>null</code> для удаления всех
   * @param aToAdd {@link IGwidList} - список интересующих клиента данных
   * @param aLogger {@link ILogger} журнал работы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void callAfterConfigureCurrDataWriter( S5InterceptorSupport<IS5CurrDataInterceptor> aInterceptorSupport,
      IS5FrontendRear aFrontend, IGwidList aToRemove, IGwidList aToAdd, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aFrontend, aToAdd );
    for( IS5CurrDataInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      try {
        interceptor.afterConfigureCurrDataWriter( aFrontend, aToRemove, aToAdd );
      }
      catch( Throwable e ) {
        aLogger.error( e );
      }
    }
  }

  /**
   * Вызов перехватчиков операции {@link IS5CurrDataInterceptor#beforeWriteCurrData(IMap)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5CurrDataInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aValues {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; - записываемые значения
   * @return boolean <b>true</b> разрешить дальнейшее выполнение операции;<b>false</b> отменить выполнение операции.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalStateRtException запретить запись текущих данных
   */
  static boolean callBeforeWriteCurrData( S5InterceptorSupport<IS5CurrDataInterceptor> aInterceptorSupport,
      IMap<Gwid, IAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aValues );
    for( IS5CurrDataInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      if( !interceptor.beforeWriteCurrData( aValues ) ) {
        return false;
      }
    }
    return true;
  }

  /**
   * Вызов перехватчиков операции {@link IS5CurrDataInterceptor#afterWriteCurrData(IMap)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5CurrDataInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aValues {@link IMap}&lt;{@link Gwid},{@link IAtomicValue}&gt; - записываемые значения
   * @param aLogger {@link ILogger} журнал работы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void callAfterWriteCurrData( S5InterceptorSupport<IS5CurrDataInterceptor> aInterceptorSupport,
      IMap<Gwid, IAtomicValue> aValues, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aValues, aLogger );
    for( IS5CurrDataInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      try {
        interceptor.afterWriteCurrData( aValues );
      }
      catch( Throwable e ) {
        aLogger.error( e );
      }
    }
  }
}
