package org.toxsoft.uskat.s5.server.backend.supports.histdata;

import javax.ejb.Local;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.sequences.S5HistDataSequenceFactory;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sequences.IS5BackendSequenceSupportSingleton;

import ru.uskat.backend.ISkFrontendRear;
import ru.uskat.backend.messages.SkMessageHistDataQueryFinished;
import ru.uskat.common.dpu.rt.events.DpuWriteHistData;
import ru.uskat.common.dpu.rt.events.DpuWriteHistDataValues;

/**
 * Локальный доступ к поддержки расширения бекенда: "данные реального времени: хранимые данные"
 *
 * @author mvk
 */
@Local
public interface IS5BackendHistDataSingleton
    extends IS5BackendSequenceSupportSingleton<IS5HistDataSequence, ITemporalAtomicValue> {

  /**
   * Записывает все значения данных.
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
   */
  void writeHistData( DpuWriteHistData aValues );

  /**
   * Возвращает фабрику создания последовательностей исторический данных
   *
   * @return {@link S5HistDataSequenceFactory}&lt; фабрика последовательностей
   */
  S5HistDataSequenceFactory histdataSequenceFactory();

  /**
   * Запускает запрос исторических данных.
   * <p>
   * Аргумент aGwids должен содержать только конкретные {@link Gwid}-ы вида {@link EGwidKind#GW_RTDATA} без повторов,
   * иначе поведение метода не определено.
   * <p>
   * Аргумент aParams это опции запроса, как определено в API серсиа РВ-данных.
   * <p>
   * Метод возвращает уникальный в рамках бекенда идентификатор запроса, который применяется при вызове метода
   * {@link #cancelHistDataResult(String)}.
   * <p>
   * По окончании (успешном или нет) или прерывании запроса методом {@link #cancelHistDataResult(String)}, генерирует
   * сообщение фронтенду методом {@link ISkFrontendRear#onGenericMessage(GenericMessage)} (смотите
   * {@link SkMessageHistDataQueryFinished}).
   *
   * @param aFrontend {@link IS5FrontendRear} фронтенд сформировавший запрос
   * @param aQueryId String идентификатор запроса
   * @param aQueryInterval {@link IQueryInterval} - интервал запроса
   * @param aGwids {@link IGwidList} - корректный набор {@link Gwid}-ов
   * @param aParams {@link IOptionSet} - опции запроса
   */
  void execHistData( IS5FrontendRear aFrontend, String aQueryId, IQueryInterval aQueryInterval, IGwidList aGwids,
      IOptionSet aParams );

  /**
   * Прекращает выполнение ранее начатого запроса.
   * <p>
   * Метод прекращает запрос, начатый методлм
   * {@link #execHistData(IS5FrontendRear, String, IQueryInterval, IGwidList, IOptionSet)}.
   * <p>
   * Если запрос с идентификатором aQueryId не выпоняется, метод ничего не делает.
   *
   * @param aQueryId String - идентификатор запроса
   */
  void cancelHistDataResult( String aQueryId );

  // ------------------------------------------------------------------------------------
  // Агрегация значений данных
  //
  /**
   * Читает агрегированные значения данных в указанном диапазоне времени
   *
   * @param aGwids {@link IList}&lt;{@link Pair}, {@link IS5HistDataAggregator}&gt;&gt; список пар: идентификатор
   *          читаемого данного<->агрегатор.
   * @param aInterval {@link IQueryInterval} интервал запрашиваемых данных
   * @throws TsIllegalArgumentRtException aStartTime > aEndTime
   * @return {@link IList}&lt;{@link IList}&lt;{@link ITemporalAtomicValue}&gt;&gt; список агрегированных значений
   *         данных. Порядок элементы списка соответствует порядку элементам списка идентификаторов описаний.
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException количество запрашиваемых данных не соответствует количеству агрегаторов
   * @throws TsIllegalArgumentRtException неверный интервал запроса
   */
  IList<IList<ITemporalAtomicValue>> readAggregatedValues( IList<Pair<Gwid, IS5HistDataAggregator>> aGwids,
      IQueryInterval aInterval );

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
