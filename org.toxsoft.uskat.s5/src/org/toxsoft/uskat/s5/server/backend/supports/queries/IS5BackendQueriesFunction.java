package org.toxsoft.uskat.s5.server.backend.supports.queries;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.cmdserv.ISkCommand;
import org.toxsoft.uskat.core.api.evserv.ISkEventList;
import org.toxsoft.uskat.core.api.hqserv.IDtoQueryParam;
import org.toxsoft.uskat.core.api.hqserv.ISkQueryProcessedData;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.impl.sequences.ITemporalValueImporter;

/**
 * Функция обработки "сырых" значений данных
 * <p>
 * Обработка "сырых" значений данных проводится в соответствии аргумента запроса {@link #arg()}.
 *
 * @author mvk
 */
public interface IS5BackendQueriesFunction {

  /**
   * Аргумент запроса значений данного, один из {@link ISkQueryProcessedData#listArgs()}.
   *
   * @return {@link Pair}&lt;{@link IDtoQueryParam}&gt; параметр запроса данного.
   *         <p>
   *         {@link Pair#left()}: идентификатор параметра;<br>
   *         {@link Pair#right()}: описание параметра.
   */
  Pair<String, IDtoQueryParam> arg();

  /**
   * Обработать следующее "сырое" значение данного.
   *
   * @param aImporter {@link ITemporalValueImporter} импортер следущего значения для агрегации.
   *          {@link ITemporalValueImporter#NULL} - больше нет значений
   * @return {@link ITimedList} список сформированных значений. Пустой список - значения еще не сформированы.
   * @param <T> тип хранимых значений, например {@link ITemporalAtomicValue}, {@link ISkEventList}, {@link ISkCommand}
   * @throws TsNullArgumentRtException аргумент = null
   */
  <T extends ITemporal<?>> ITimedList<T> nextValue( ITemporalValueImporter aImporter );

}
