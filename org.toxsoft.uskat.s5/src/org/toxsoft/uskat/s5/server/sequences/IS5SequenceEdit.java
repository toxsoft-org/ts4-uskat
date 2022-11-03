package org.toxsoft.uskat.s5.server.sequences;

import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Последовательность значений одного данного с возможностью редактирования
 *
 * @author mvk
 * @param <V> тип значения последовательности
 */
public interface IS5SequenceEdit<V extends ITemporal<?>>
    extends IS5Sequence<V> {

  /**
   * Устанавливает новый интервал времени последовательности.
   * <p>
   * Текущие данные последовательности которые не попадают в новый интервал времени удаляются из последовательности
   * <p>
   * Если текущий курсор не попадает в новый интервал времени, то он устанавливается на начало последовательности
   *
   * @param aInterval {@link IQueryInterval} интервал последовательности, подробности в {@link #interval()}.
   * @throws TsIllegalArgumentRtException неверный интервал времени aStartTime > aEndTime
   */
  void setInterval( IQueryInterval aInterval );

  /**
   * Возвращает признак того, что исходная последовательность содержит значения которые могут изменить целевую
   * последовательность
   *
   * @param aSource {@link IS5Sequence} исходная последовательность значений
   * @return boolean <b>true</b> исходная последовательность может изменить целевую последовательность. <b>false</b> в
   *         исходной последовательности нет значений для изменения целевой последовательности.
   * @throws TsNullArgumentRtException аргумент = null
   */
  boolean editable( IS5Sequence<V> aSource );

  /**
   * Установить(заменить) значения в последовательности
   * <p>
   * Если в последовательности уже были значения, то ни будут заменены на новые
   * <p>
   * Если последовательность синхронных значений, то "пробелы" в слотах между значениями заполняются значениями по
   * умолчанию.
   *
   * @param aValues {@link ITimedList}&lt;V&gt; список значений данного
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException какая-либо метка времени выходит за допустимые значения
   */
  void set( ITimedList<V> aValues );

  /**
   * Редактирует последовательность значениями другой последовательности.
   * <p>
   * В целевой последовательности удаляются все данные за интервал времени исходной последовательности. Блоки исходной
   * последовательности попадающие в интервал целевой последовательности копируются (по ссылке) в целевую
   * последовательность, при необходимости, производится создание новых блоков имеющих части пограничных блоков исходной
   * последовательности.
   *
   * @param aSource {@link IS5Sequence} исходная последовательность
   * @param aRemovedBlocks {@link IListEdit}&lt;{@link IS5SequenceBlock}&gt; список блоков удаленных из последовательности
   * @return boolean <b>true</b> последовательность была отредактирована; <b>false</b> последовательность не была
   *         отредактирована.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException последовательность не может принимать значения другого данного
   */
  boolean edit( IS5Sequence<V> aSource, IListEdit<IS5SequenceBlock<V>> aRemovedBlocks );

  /**
   * Выполняет попытку объединения блоков составляющих последовательность.
   * <p>
   * При объединении блоков синхронных данных допускается ситуация когда блоки имеют разное время выравнивания или
   * данные сфомированны с разным шагом и поэтому после объединения последовательность может быть представлена блоками
   * синхронных данных с разным шагом.
   * <p>
   * Позиция курсора не меняется
   *
   * @return {@link IList}&lt;{@link IS5SequenceBlockEdit}&gt; список блоков выведенных(удаленных) из последовательности
   */
  IList<IS5SequenceBlockEdit<V>> uniteBlocks();

  /**
   * Очистить последовательность (удалить из нее все блоки)
   */
  void clear();

}
