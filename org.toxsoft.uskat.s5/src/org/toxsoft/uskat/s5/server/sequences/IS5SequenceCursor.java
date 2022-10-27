package org.toxsoft.uskat.s5.server.sequences;

import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.supports.histdata.IS5HistDataSequence;

/**
 * Курсор чтения "сырых" данных.
 * <p>
 * Курсор используется для последовательного доступа к значениям без создания экземляров самого значения, например
 * {@link ITemporalAtomicValue} для {@link IS5HistDataSequence}.
 * <p>
 * Используется при переработке большого количества значений
 *
 * @author mvk
 * @param <T> тип данных возвращаемых курсором
 */
public interface IS5SequenceCursor<T extends ITemporal<?>> {

  /**
   * Подготовка к чтению с указанного времени
   *
   * @param aFromTime long метка времени с которого начинается чтение значений
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException недопустимое время
   */
  void setTime( long aFromTime );

  /**
   * Возвращает признак того, что курсор может прочитать следующее значение методом {@link #nextValue()}.
   *
   * @return <b>true</b> курсор может прочитать следующее значение
   */
  boolean hasNextValue();

  /**
   * Возвращает следующее значение последовательности
   * <p>
   * Клиент может использовать значение для обработки и копирования. Запрещается хранение полученного значения по
   * возвращаемой ссылке.
   *
   * @return T значение считанное курсором
   * @throws TsIllegalArgumentRtException нет значения
   */
  T nextValue();

  /**
   * Возвращает текущую позицию курсора в последовательности.
   * <p>
   * Позиция увеличивается после каждого вызова {@link #nextValue()}.
   *
   * @return int позиция курсора. < 0: не было вызовов {@link #nextValue()}.
   */
  int position();
}
