package org.toxsoft.uskat.s5.server.sequences.reader;

import org.toxsoft.core.tslib.bricks.strid.idgen.IStridGenerator;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.ILongList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.gwid.IGwidList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sequences.IS5Sequence;

/**
 * Читатель значений последовательностей данных {@link IS5Sequence}
 *
 * @author mvk
 * @param <S> тип последовательности значений данного
 * @param <V> тип значения последовательности
 */
public interface IS5SequenceReader<S extends IS5Sequence<V>, V extends ITemporal<?>> {

  /**
   * Возвращает фактическое время начала значений последовательности от указанного времени
   *
   * @param aGwids {@link IGwidList} список идентификаторов данных.
   * @param aAfterTime long время(мсек с начала эпохи) от которого проводится поиск значений. Невключительно
   * @return long время(мсек с начала эпохи) начала значений последовательности. Включительно. Если данных нет, то
   *         возращается {@link TimeUtils#MAX_TIMESTAMP}
   * @throws TsNullArgumentRtException аргумент = null
   */
  long findStartTime( IGwidList aGwids, long aAfterTime );

  /**
   * Возвращает фактическое время начала значений последовательностей от указанного времени
   *
   * @param aGwids {@link IGwidList} список идентификаторов данных.
   * @param aAfterTime long время(мсек с начала эпохи) от которого проводится поиск значений. Невключительно
   * @return {@link ILongList} время(мсек с начала эпохи) начала значений последовательности для каждого данного.
   *         Включительно. Если данных нет, то возращается {@link TimeUtils#MAX_TIMESTAMP}
   * @throws TsNullArgumentRtException аргумент = null
   */
  ILongList findStartTimes( IGwidList aGwids, long aAfterTime );

  /**
   * Возвращает фактическое время завершения значений последовательности перед указанным временем
   *
   * @param aGwids {@link IGwidList} список идентификаторов данных.
   * @param aBeforeTime long время(мсек с начала эпохи) до которого проводится поиск значений. Невключительно
   * @return long время(мсек с начала эпохи) начала значений последовательности. Включительно. Если данных нет, то
   *         возращается {@link TimeUtils#MIN_TIMESTAMP}
   * @throws TsNullArgumentRtException аргумент = null
   */
  long findEndTime( IGwidList aGwids, long aBeforeTime );

  /**
   * Возвращает фактическое время завершения значений последовательностей перед указанным временем
   *
   * @param aGwids {@link IGwidList} список идентификаторов данных.
   * @param aBeforeTime long время(мсек с начала эпохи) до которого проводится поиск значений. Невключительно
   * @return {@link ILongList} время(мсек с начала эпохи) начала значений последовательности для каждого данного.
   *         Включительно. Если данных нет, то возращается {@link TimeUtils#MIN_TIMESTAMP}
   * @throws TsNullArgumentRtException аргумент = null
   */
  ILongList findEndTimes( IGwidList aGwids, long aBeforeTime );

  /**
   * Возвращает генератор идентификаторов запросов
   *
   * @return {@link IStridGenerator} генератор
   */
  IStridGenerator uuidGenerator();

  /**
   * Возвращает список идентификаторов выполяемых запросов чтения
   *
   * @return {@link IStringList} список идентификаторов запросов
   */
  IStringList readQueryIds();

  /**
   * Читает последовательности значений данных в указанном диапазоне времени
   *
   * @param aGwids {@link IGwidList} список идентификаторов данных.
   * @param aInterval {@link IQueryInterval} интервал запроса
   * @param aMaxExecutionTimeout long максимальное время (мсек) выполнения запроса. < 1000: не ограничено
   * @throws TsIllegalArgumentRtException aStartTime > aEndTime
   * @return {@link IList}&lt;{@link IS5Sequence}&gt; список последовательностей значений. Порядок элементы списка
   *         соответствует порядку элементам списка идентификаторов описаний.
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException неверный интервал запроса
   */
  IList<S> readSequences( IGwidList aGwids, IQueryInterval aInterval, long aMaxExecutionTimeout );

  /**
   * Читает последовательности значений данных в указанном диапазоне времени
   *
   * @param aFrontend {@link IS5FrontendRear} фронтенд сформировавший запрос
   * @param aQueryId String идентификатор запроса
   * @param aGwids {@link IGwidList} список идентификаторов данных.
   * @param aInterval {@link IQueryInterval} интервал запроса
   * @param aMaxExecutionTimeout long максимальное время (мсек) выполнения запроса. < 1000: не ограничено
   * @throws TsIllegalArgumentRtException aStartTime > aEndTime
   * @return {@link IList}&lt;{@link IS5Sequence}&gt; список последовательностей значений. Порядок элементы списка
   *         соответствует порядку элементам списка идентификаторов описаний.
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException неверный интервал запроса
   * @throws TsIllegalArgumentRtException запрос уже выполняется
   */
  IList<S> readSequences( IS5FrontendRear aFrontend, String aQueryId, IGwidList aGwids, IQueryInterval aInterval,
      long aMaxExecutionTimeout );

  /**
   * Отменяет выполнение запроса
   * <p>
   * Если запрос не выполняется,то ничего не делает.
   *
   * @param aQueryId String идентификатор запроса
   * @return {@link IS5SequenceReadQuery} отмененный запрос. null: запрос не найден
   * @throws TsNullArgumentRtException аргумент = null
   */
  IS5SequenceReadQuery cancelReadQuery( String aQueryId );
}
