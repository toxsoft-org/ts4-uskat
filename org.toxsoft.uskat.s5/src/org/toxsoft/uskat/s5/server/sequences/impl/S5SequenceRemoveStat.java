package org.toxsoft.uskat.s5.server.sequences.impl;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.IS5SequenceRemoveInfo;
import org.toxsoft.uskat.s5.server.sequences.maintenance.IS5SequenceRemoveStat;

/**
 * Реализация {@link IS5SequenceRemoveStat}
 *
 * @author mvk
 * @param <V> тип значений в блоках
 */
final class S5SequenceRemoveStat<V extends ITemporal<?>>
    implements IS5SequenceRemoveStat, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Общее количество обработанных данных
   */
  private IList<IS5SequenceRemoveInfo> infoes = IList.EMPTY;

  /**
   * Общее количество обработанных последовательностей у которых произошло удаление
   */
  private int lookupCount = 0;

  /**
   * Общее количество удаленных блоков
   */
  private int removedCount = 0;

  /**
   * Количество ошибок удаления блоков
   */
  private int errors = 0;

  /**
   * Текущий размер очереди данных на удаление
   */
  private int queueSize = 0;

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequenceUnionStat
  //
  @Override
  public IList<IS5SequenceRemoveInfo> infoes() {
    return infoes;
  }

  @Override
  public int lookupCount() {
    return lookupCount;
  }

  @Override
  public int removedCount() {
    return removedCount;
  }

  @Override
  public int errorCount() {
    return errors;
  }

  @Override
  public int queueSize() {
    return queueSize;
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Устанавливает список описаних данных для удаления
   *
   * @param aInfoes {@link IList}&lt; {@link IS5SequenceRemoveInfo}&gt; список описаний данных
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setInfoes( IList<IS5SequenceRemoveInfo> aInfoes ) {
    TsNullArgumentRtException.checkNull( aInfoes );
    infoes = aInfoes;
  }

  /**
   * Добавляет количество последовательностей проанализированных при поиске дефрагментации
   */
  void addLookupCount() {
    lookupCount++;
  }

  /**
   * Добавляет количество блоков удаленных из dbms
   *
   * @param aRemoved int количество блоков
   */
  void addRemoved( int aRemoved ) {
    removedCount += aRemoved;
  }

  /**
   * Добавляет количество ошибок объединения значений блоков
   *
   * @param aErrors int количество ошибок
   */
  void addErrors( int aErrors ) {
    errors += aErrors;
  }

  /**
   * Устанавливает текущий размер очереди на дефрагментацию
   *
   * @param aSize размер очереди
   */
  void setQueueSize( int aSize ) {
    queueSize = aSize;
  }
}
