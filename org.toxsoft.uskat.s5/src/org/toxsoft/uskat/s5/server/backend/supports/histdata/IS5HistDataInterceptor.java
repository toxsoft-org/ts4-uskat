package org.toxsoft.uskat.s5.server.backend.supports.histdata;

import javax.ejb.Local;

import org.toxsoft.core.tslib.gw.gwid.EGwidKind;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.interceptors.IS5Interceptor;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;

import ru.uskat.common.dpu.rt.events.*;

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
   * Внимание: большое значение имеет {@link DpuWriteHistDataValues#interval()}. Метод предполагает, что в аргументе за
   * заданный интервал содержатся <b>все</b> значения. Например, если интервал сутки, а список пустой, это означает что
   * за заданный интервал значение не менялось.
   * <p>
   * Идентификатором данного может быть только конкретный (с идентификатором объекта) {@link Gwid}-ы имеющий вид
   * {@link EGwidKind#GW_RTDATA}. Все другие идентификаторы молча игнорируются.
   *
   * @param aValues {@link DpuWriteHistData} значения хранимых данных для записи.
   *          <p>
   *          Ключ: идентификатор данного;<br>
   *          Значение: список значений данного для записи.
   * @return boolean <b>true</b> разрешить дальнейшее выполнение операции;<b>false</b> отменить выполнение операции.
   * @throws TsNullArgumentRtException аргумент = null
   */
  boolean beforeWriteHistData( DpuWriteHistData aValues );

  /**
   * Вызывается ПОСЛЕ записи хранимых данных в систему, но ДО завершения транзакции.
   * <p>
   * Событие формируется в открытой транзакции, но все попытки ее отмены (через поднятие исключения в
   * {@link #afterWriteHistData(DpuWriteHistData)}) будут игнорироваться.
   * <p>
   * Внимание: большое значение имеет {@link DpuWriteHistDataValues#interval()}. Метод предполагает, что в аргументе за
   * заданный интервал содержатся <b>все</b> значения. Например, если интервал сутки, а список пустой, это означает что
   * за заданный интервал значение не менялось.
   * <p>
   * Идентификатором данного может быть только конкретный (с идентификатором объекта) {@link Gwid}-ы имеющий вид
   * {@link EGwidKind#GW_RTDATA}. Все другие идентификаторы молча игнорируются.
   *
   * @param aValues {@link DpuWriteHistData} значения хранимых данных для записи.
   *          <p>
   *          Ключ: идентификатор данного;<br>
   *          Значение: список значений данного для записи.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void afterWriteHistData( DpuWriteHistData aValues );

  // ------------------------------------------------------------------------------------
  // Вспомогательные методы
  //
  /**
   * Вызов перехватчиков операции {@link IS5HistDataInterceptor#beforeWriteHistData(DpuWriteHistData)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5HistDataInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aValues {@link DpuWriteHistData} значения хранимых данных для записи.
   *          <p>
   *          Ключ: идентификатор данного;<br>
   *          Значение: список значений данного для записи.
   * @return boolean <b>true</b> разрешить дальнейшее выполнение операции;<b>false</b> отменить выполнение операции.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static boolean callBeforeWriteHistData( S5InterceptorSupport<IS5HistDataInterceptor> aInterceptorSupport,
      DpuWriteHistData aValues ) {
    TsNullArgumentRtException.checkNulls( aInterceptorSupport, aValues );
    for( IS5HistDataInterceptor interceptor : aInterceptorSupport.interceptors() ) {
      if( !interceptor.beforeWriteHistData( aValues ) ) {
        return false;
      }
    }
    return true;
  }

  /**
   * Вызов перехватчиков операции {@link IS5HistDataInterceptor#afterWriteHistData(DpuWriteHistData)}
   *
   * @param aInterceptorSupport {@link S5InterceptorSupport}&lt;{@link IS5HistDataInterceptor}&gt; поддержка
   *          перехватчиков
   * @param aValues {@link DpuWriteHistData} значения хранимых данных для записи.
   *          <p>
   *          Ключ: идентификатор данного;<br>
   *          Значение: список значений данного для записи.
   * @param aLogger {@link ILogger} журнал работы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  static void callAfterWriteHistData( S5InterceptorSupport<IS5HistDataInterceptor> aInterceptorSupport,
      DpuWriteHistData aValues, ILogger aLogger ) {
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
