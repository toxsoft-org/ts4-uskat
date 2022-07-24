package org.toxsoft.uskat.s5.server.backend.supports.histdata;

import javax.ejb.Local;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.gw.gwid.EGwidKind;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.s5.server.interceptors.IS5Interceptor;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;

/**
 * Перехватчик операций записи хранимых данных в системе .
 * <p>
 * В отличии от событий {@link SkEvent}, события об операциях {@link IS5HistDataInterceptor} передаются в режиме раннего
 * оповещения (в рамках выполняемой транзакции) и позвляют перехватчику повлиять на конечный результат проводимой
 * операции над объектами. Например, клиент(перехватчик) может запретить/отменить выполнение операции.
 * <p>
 * Все методы реализации интерфейса {@link IS5HistDataInterceptor} должны быть иметь аннатоцию:
 * &#064;TransactionAttribute( TransactionAttributeType.MANDATORY ).
 *
 * @author mvk
 */
@Local
public interface IS5HistDataInterceptor
    extends IS5Interceptor {

  /**
   * Вызывается ДО записи хранимых данных в систему
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   * <p>
   * Внимание: большое значение имеет интервал времени за который записываются значения: aValues.left() -
   * {@link ITimeInterval} . Метод предполагает, что в аргументе за заданный интервал содержатся <b>все</b> значения.
   * Например, если интервал сутки, а список пустой, это означает что за заданный интервал значение не менялось.
   * <p>
   * Идентификатором данного может быть только конкретный (с идентификатором объекта) {@link Gwid}-ы имеющий вид
   * {@link EGwidKind#GW_RTDATA}. Все другие идентификаторы молча игнорируются.
   *
   * @param aValues
   *          {@link IMap}&lt;{@link Gwid},{@link Pair}&lt;{@link ITimeInterval},{@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt;&gt;&gt;
   *          значения
   * @return boolean <b>true</b> разрешить дальнейшее выполнение операции;<b>false</b> отменить выполнение операции.
   * @throws TsNullArgumentRtException аргумент = null
   */
  boolean beforeWriteHistData( IMap<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> aValues );

  /**
   * Вызывается ПОСЛЕ записи хранимых данных в систему, но ДО завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции, но все попытки ее отмены (через поднятие исключения в
   * {@link #afterWriteHistData(IMap)}) будут игнорироваться.
   * <p>
   * Внимание: большое значение имеет интервал времени за который записываются значения: aValues.left() -
   * {@link ITimeInterval} . Метод предполагает, что в аргументе за заданный интервал содержатся <b>все</b> значения.
   * Например, если интервал сутки, а список пустой, это означает что за заданный интервал значение не менялось.
   * <p>
   * Идентификатором данного может быть только конкретный (с идентификатором объекта) {@link Gwid}-ы имеющий вид
   * {@link EGwidKind#GW_RTDATA}. Все другие идентификаторы молча игнорируются.
   *
   * @param aValues
   *          {@link IMap}&lt;{@link Gwid},{@link Pair}&lt;{@link ITimeInterval},{@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt;&gt;&gt;
   *          значения
   * @throws TsNullArgumentRtException аргумент = null
   */
  void afterWriteHistData( IMap<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> aValues );

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
  /**
   * Вызов перехватчиков операции {@link IS5HistDataInterceptor#beforeWriteHistData(IMap)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5HistDataInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aValues
   *          {@link IMap}&lt;{@link Gwid},{@link Pair}&lt;{@link ITimeInterval},{@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt;&gt;&gt;
   *          значения
   * @return boolean <b>true</b> разрешить дальнейшее выполнение операции;<b>false</b> отменить выполнение операции.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static boolean callBeforeWriteHistData( S5InterceptorSupport<IS5HistDataInterceptor> aInterceptorSupport,
      IMap<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> aValues ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aValues );
    for( IS5HistDataInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      if( !interceptor.beforeWriteHistData( aValues ) ) {
        return false;
      }
    }
    return true;
  }

  /**
   * Вызов перехватчиков операции {@link IS5HistDataInterceptor#afterWriteHistData(IMap)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5HistDataInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aValues
   *          {@link IMap}&lt;{@link Gwid},{@link Pair}&lt;{@link ITimeInterval},{@link ITimedList}&lt;{@link ITemporalAtomicValue}&gt;&gt;&gt;
   *          значения
   * @param aLogger {@link ILogger} журнал работы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void callAfterWriteHistData( S5InterceptorSupport<IS5HistDataInterceptor> aInterceptorSupport,
      IMap<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> aValues, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aValues, aLogger );
    for( IS5HistDataInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      try {
        interceptor.afterWriteHistData( aValues );
      }
      catch( Throwable e ) {
        aLogger.error( e );
      }
    }
  }
}
