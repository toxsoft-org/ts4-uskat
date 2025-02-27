package org.toxsoft.uskat.s5.server.backend.supports.events;

import javax.ejb.*;

import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.s5.server.interceptors.*;

/**
 * Перехватчик операций записи событий в системе .
 * <p>
 * Все методы реализации интерфейса {@link IS5EventInterceptor} должны быть иметь аннатоцию: &#064;TransactionAttribute(
 * TransactionAttributeType.MANDATORY ).
 *
 * @author mvk
 */
@Local
public interface IS5EventInterceptor
    extends IS5Interceptor {

  /**
   * Вызывается ДО записи событий в систему
   * <p>
   * Событие формируется в открытой транзакции которая впоследствии может быть отменена. Поэтому, если необходимо,
   * клиент-перехватчик должен организовать логику восстановления своего состояния при откате транзакции (смотри
   * S5TransactionSingleton}.
   * <p>
   * Внимание: большое значение имеет интервал времени за который записываются события: aEvents.left() -
   * {@link ITimeInterval} . Метод предполагает, что в аргументе за заданный интервал содержатся <b>все</b> события.
   * Например, если интервал сутки, а список пустой, это означает что за заданный интервал нет событий.
   * <p>
   * Идентификатором события может быть только конкретный (с идентификатором объекта) {@link Gwid}-ы имеющий вид
   * {@link EGwidKind#GW_EVENT}. Все другие идентификаторы молча игнорируются.
   *
   * @param aEvents {@link ISkEventList} события.
   * @return boolean <b>true</b> разрешить дальнейшее выполнение операции;<b>false</b> отменить выполнение операции.
   * @throws TsNullArgumentRtException аргумент = null
   */
  boolean beforeWriteEvents( ISkEventList aEvents );

  /**
   * Вызывается ПОСЛЕ записи событий в систему, но ДО завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции, но все попытки ее отмены (через поднятие исключения в
   * {@link #afterWriteEvents(ISkEventList)}) будут игнорироваться.
   * <p>
   * Внимание: большое значение имеет интервал времени за который записываются значения: aEvents.left() -
   * {@link ITimeInterval} . Метод предполагает, что в аргументе за заданный интервал содержатся <b>все</b> события.
   * Например, если интервал сутки, а список пустой, это означает что за заданный интервал нет событий.
   * <p>
   * Идентификатором события может быть только конкретный (с идентификатором объекта) {@link Gwid}-ы имеющий вид
   * {@link EGwidKind#GW_EVENT}. Все другие идентификаторы молча игнорируются.
   *
   * @param aEvents {@link ITimedList}&lt;{@link SkEvent}&gt; события.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void afterWriteEvents( ISkEventList aEvents );

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
  /**
   * Вызов перехватчиков операции {@link IS5EventInterceptor#beforeWriteEvents(ISkEventList)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5EventInterceptor}&gt; поддержка перехватчиков
   * @param aEvents {@link ITimedList}&lt;{@link SkEvent}&gt; события.
   * @return boolean <b>true</b> разрешить дальнейшее выполнение операции;<b>false</b> отменить выполнение операции.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static boolean callBeforeWriteEvents( S5InterceptorSupport<IS5EventInterceptor> aInterceptorSupport,
      ISkEventList aEvents ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aEvents );
    for( IS5EventInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      if( !interceptor.beforeWriteEvents( aEvents ) ) {
        return false;
      }
    }
    return true;
  }

  /**
   * Вызов перехватчиков операции {@link IS5EventInterceptor#afterWriteEvents(ISkEventList)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5EventInterceptor}&gt; поддержка перехватчиков
   * @param aEvents {@link ITimedList}&lt;{@link SkEvent}&gt; события.
   * @param aLogger {@link ILogger} журнал работы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void callAfterWriteEvents( S5InterceptorSupport<IS5EventInterceptor> aInterceptorSupport, ISkEventList aEvents,
      ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aEvents, aLogger );
    for( IS5EventInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      try {
        interceptor.afterWriteEvents( aEvents );
      }
      catch( Throwable e ) {
        aLogger.error( e );
      }
    }
  }
}
