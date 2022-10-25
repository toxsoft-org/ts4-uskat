package org.toxsoft.uskat.s5.server.sequences;

import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.EGwidKind;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.ITemporalValueImporter;

/**
 * Последовательность значений одного данного.
 *
 * @author mvk
 * @param <V> тип значения последовательности
 */
public interface IS5Sequence<V extends ITemporal<?>> {

  /**
   * Возвращает НЕабстрактный {@link Gwid}-идентификатор данного значения которого хранятся в последовательности
   * <ul>
   * <li>Для данных реального времени - {@link EGwidKind#GW_RTDATA};</li>
   * <li>Для событий и истории команд - {@link EGwidKind#GW_CLASS}.</li>
   * </ul>
   *
   * @return {@link Gwid} идентификатор данного
   */
  Gwid gwid();

  /**
   * Возвращает параметризованное описание типа данного значения которого хранятся в последовательности
   *
   * @return {@link IParameterized} параметризованное описание типа
   */
  IParameterized typeInfo();

  /**
   * Возвращает интервал времени значений данных представленных в последовательности
   * <p>
   * Пояснение по типам интервала для последовательности значений данного:
   * <ul>
   * <li>{@link EQueryIntervalType#CSCE}: интервал закрыт слева и справа. В последовательности находятся значения данных
   * метки времени которых находятся внутри интервала или на его границах;</li>
   * <li>{@link EQueryIntervalType#OSCE}: интервал открыт слева, но закрыт справа. В последовательности находятся
   * значения данных метки времени которых находятся внутри интервала или на его границах. Кроме этого в
   * последовательности может быть одно(!) значение перед левой границей интервала если на этой границе нет
   * значения;</li>
   * <li>{@link EQueryIntervalType#CSOE}: интервал закрыт слева, но открыт справа. В последовательности находятся
   * значения данных метки времени которых находятся внутри интервала или на его границах. Кроме этого в
   * последовательности может быть одно(!) значение после правой границы интервала если на этой границе нет
   * значения;</li>
   * <li>{@link EQueryIntervalType#OSOE}: интервал открыт слева и справа. В последовательности находятся значения данных
   * метки времени которых находятся внутри интервала или на его границах. Кроме этого в последовательности может быть
   * по одному(!) значению перед левой и после правой границы интервала если на соответствующей границе нет
   * значения.</li>
   * </ul>
   *
   * @return {@link IQueryInterval} интервал времени
   */
  IQueryInterval interval();

  /**
   * Возвращает список блоков последовательности
   *
   * @return {@link Iterable}&lt;{@link ISequenceBlock}&gt; итератор блоков
   */
  IList<ISequenceBlock<V>> blocks();

  /**
   * Находит индекс блока в диапазон которого попадает указанная метка времени. Если такого блока нет, то возвращает
   * индекс ближайшего блока к искомой метке. Если блоков нет, то -1
   * <p>
   * Клиент должен проверять найденный блок на попадание в него метки времени, так как может возвращаться блок к
   * которому метка находится ближе(слева или справа), но не попадает в него.
   *
   * @param aTimestamp long метка времени для которой определяется индекс.
   * @return индекс блока в который попадает метка времени или индекс ближайшего блока. < 0: пустой массив меток
   */
  int findBlockIndex( long aTimestamp );

  /**
   * Возвращает текущую метку времени для получения данных с помощью метода {@link #nextValue()}.
   *
   * @return метка времени значений
   */
  long getCurrTime();

  /**
   * Задает начальную метку времени для получения данных с помощью метода {@link #nextValue()}.
   * <p>
   * Для вновь созданного набора метка времени установлена в {@link ITimeInterval#startTime()}.
   * <p>
   * Если метка времени попадает в интервал последовательности значений, но в последовательности нет значений с такой
   * меткой времени, то возвращается ближайшее к ней(может быть "слева", может быть "справа"). Если значений в
   * последовательности нет, то ничего не делает.
   *
   * @param aCurrTime long - начальная метка времени в пределах {@link #interval()} включительно.
   * @throws TsIllegalArgumentRtException аргумент выходит за допустимые пределы
   */
  void setCurrTime( long aCurrTime );

  /**
   * Определяет, есть ли очередное значение данного.
   * <p>
   * Этот метод <b>не</b> сдвигает метку текущего времени для этого данного к следующему значению. Сколько угодно подряд
   * вызвов этого метода (без промежуточного вызова {@link #nextValue()} для этого же индекса) будет возвращать одно и
   * то же значение.
   * <p>
   * Получить данное методом {@link #nextValue()} без исключения возможно, только если {@link #hasNext()} возвращает
   * <code>true</code>.
   *
   * @return boolean - признак наличия следующего значения (признак возможности вызова {@link #nextValue()})
   */
  boolean hasNext();

  /**
   * Возвращает очередное историческое данное.
   * <p>
   * Этот метод сдвигает метку текущего времени для этого данного к следующему значению пока следующее значение не
   * оакжется за меткой времени конца набора {@link ITimeInterval#endTime()}.
   *
   * @return V - значение данного
   * @throws TsIllegalStateRtException нет очередного данного
   */
  V nextValue();

  /**
   * Возвращает все исторические значения данного за указанный интервал времени.
   *
   * @param aInterval {@link IQueryInterval} интервал времени запроса, подробности смотри {@link #interval()}
   * @return {@link ITimedList}&lt;V&gt; - список значений данного
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException время начала / окончания выходит за допустимые пределы
   * @throws TsIllegalArgumentRtException aStartTime > aEndTime
   * @throws TsIllegalStateRtException невозможно получить данные за указанный интервал
   */
  ITimedList<V> get( IQueryInterval aInterval );

  /**
   * Установить начальную метку времени для импорта значений
   * <p>
   * Если в последовательности нет значения точно по указанной метке, то метка устанавливается на первое значение за
   * указанной меткой
   *
   * @param aTimestamp long метка времени (мсек с начала эпохи) с которой будет производиться импорт значений
   */
  void setImportTime( long aTimestamp );

  /**
   * Возвращает признак того, что импорт значений может быть продолжен вызовом {@link #nextImport()}
   *
   * @return <b>true</b> есть данные для импорта. <b>false</b> нет данных для импорта
   */
  boolean hasImport();

  /**
   * Импортировать следующее значение
   *
   * @return {@link ITemporalValueImporter} способ получения значений
   * @throws TsIllegalArgumentRtException нет больше данных для импорта
   */
  ITemporalValueImporter nextImport();
}
