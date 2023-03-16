package org.toxsoft.uskat.s5.server.backend.supports.queries;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.hqserv.IDtoQueryParam;
import org.toxsoft.uskat.core.api.hqserv.ISkQueryProcessedData;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceCursor;

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
   * Обработка значений последовательности
   *
   * @param aCursors {@link IList}&lt;{@link IS5SequenceCursor}&gt; список курсоров последовательности значений. Если
   *          данные запрашивались по конкретному {@link Gwid} то в списке только один курсор.
   * @param <T> тип значений
   * @return {@link IList} список обработанных значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  <T> IList<T> evaluate( IList<IS5SequenceCursor<?>> aCursors );

}
