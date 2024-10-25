package org.toxsoft.uskat.s5.server.sequences;

import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.validator.vrl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.s5.server.sequences.impl.*;

/**
 * Блок значений последовательности {@link IS5Sequence} с возможностью редактирования
 *
 * @author mvk
 * @param <V> тип значения последовательности
 */
public interface IS5SequenceBlockEdit<V extends ITemporal<?>>
    extends IS5SequenceBlock<V> {

  /**
   * Создает новый блок и инициализирует его данными целевого блока. Если блок невозможно создать для указанного
   * интервала (получается пустой блок), то возвращает null
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aStartTime long время(мсек с начала эпохи) начала значений в блоке (включительно)
   * @param aEndTime long время(мсек с начала эпохи) окончания значений в блоке (включительно)
   * @return {@link IS5SequenceBlockEdit}&lt;T&gt; созданный блок. null: невозможно создать блок для интервала (в
   *         указанном интервале нет значений)
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException aStartTime > aEndTime
   */
  IS5SequenceBlockEdit<V> createBlockOrNull( IParameterized aTypeInfo, long aStartTime, long aEndTime );

  /**
   * Редактирует диапазон времени блока изменяя время завершения значений
   * <p>
   * Время начала значений запрещено менять у блока так как оно используется как часть составного первичного ключа
   * {@link S5DataID}.
   *
   * @param aEndTime long время(мсек с начала эпохи) окончания редактируемых значений в блоке
   * @throws TsIllegalArgumentRtException aStartTime > aEndTime
   */
  void editEndTime( long aEndTime );

  /**
   * Осуществляет попытку объединения значений блока со значениями указанного списка блоков.
   *
   * @param aFactory {@link IS5SequenceValueFactory} фабрика формирования последовательности
   * @param aBlocks {@link IList}&lt;{@link IS5SequenceBlockEdit}&lt;I&gt;&gt; список блоков
   * @param aLogger {@link ILogger} журнал объединения блоков
   * @return int количество блоков от начала указанного списка с которыми произошло объединение.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  int uniteBlocks( IS5SequenceFactory<V> aFactory, IList<IS5SequenceBlockEdit<V>> aBlocks, ILogger aLogger );

  /**
   * Проводит валидацию (исправление содержимого блока) если это необходимо
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @return {@link IVrList} результаты валидации
   * @throws TsNullArgumentRtException аргумент = null
   */
  IVrList validation( IParameterized aTypeInfo );
}
