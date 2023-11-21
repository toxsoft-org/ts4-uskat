package org.toxsoft.uskat.s5.server.backend.supports.histdata;

import javax.ejb.Local;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.IMap;
import org.toxsoft.core.tslib.gw.gwid.EGwidKind;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.s5.server.sequences.IS5BackendSequenceSupportSingleton;

/**
 * Локальный интерфейс синглетона запросов к хранимым данным предоставляемый s5-сервером.
 *
 * @author mvk
 */
@Local
public interface IS5BackendHistDataSingleton
    extends IS5BackendSequenceSupportSingleton<IS5HistDataSequence, ITemporalAtomicValue> {

  /**
   * Асинхронная запись значений хранимых данных
   * <p>
   * Управление возвращается немедленно, без ожидания записи данных в базу данных.
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
   *          - значения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void asyncWriteValues( IMap<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> aValues );

  /**
   * Синхронная запись значений хранимых данных.
   * <p>
   * Управление возвращается после записи значений в базу данных или при возникновении ошибки записи.
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
   *          - значения
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void syncWriteValues( IMap<Gwid, Pair<ITimeInterval, ITimedList<ITemporalAtomicValue>>> aValues );

  /**
   * Возвращает историю значений по указанному данном за указанный период времени
   *
   * @param aInterval {@link IQueryInterval} - интервал времени
   * @param aGwid {@link Gwid} - идентификатор конкретного данного
   * @return {@link ITimedList}&lt;{@link SkEvent}&gt; - список значений данного
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid );

  // ------------------------------------------------------------------------------------
  // Интерсепция
  //
  /**
   * Добавляет перехватчика операций проводимых над данными.
   * <p>
   * Если такой перехватчик уже зарегистрирован, то обновляет его приоритет.
   *
   * @param aInterceptor {@link IS5HistDataInterceptor } перехватчик операций
   * @param aPriority int приоритет перехватчика. Чем меньше значение, тем выше приоритет.
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addHistDataInterceptor( IS5HistDataInterceptor aInterceptor, int aPriority );

  /**
   * Удаляет перехватчика операций проводимых над данными.
   * <p>
   * Если такой перехватчик не зарегистрирован, то метод ничего не делает.
   *
   * @param aInterceptor {@link IS5HistDataInterceptor } перехватчик операций
   * @throws TsNullArgumentRtException аргумент = null
   */
  void removeHistDataInterceptor( IS5HistDataInterceptor aInterceptor );
}
