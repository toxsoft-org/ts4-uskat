package org.toxsoft.uskat.s5.server.sequences.impl;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.maintenance.IS5SequencePartitionStat;
import org.toxsoft.uskat.s5.server.sequences.maintenance.S5PartitionOperation;

/**
 * Реализация {@link IS5SequencePartitionStat}
 *
 * @author mvk
 * @param <V> тип значений в блоках
 */
final class S5SequencePartitionStat<V extends ITemporal<?>>
    implements IS5SequencePartitionStat, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Операции над разделами
   */
  private IList<S5PartitionOperation> operations = IList.EMPTY;

  /**
   * Общее количество обработанных операций
   */
  private int lookupCount = 0;

  /**
   * Общее количество добавленных разделов
   */
  private int addedCount = 0;

  /**
   * Общее количество удаленных разделов
   */
  private int removedPartitionCount = 0;

  /**
   * Общее количество удаленных блоков
   */
  private int removedBlockCount = 0;

  /**
   * Количество ошибок операций над разделами
   */
  private int errors = 0;

  /**
   * Текущий размер очереди операций
   */
  private int queueSize = 0;

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequenceUnionStat
  //
  @Override
  public IList<S5PartitionOperation> operations() {
    return operations;
  }

  @Override
  public int lookupCount() {
    return lookupCount;
  }

  @Override
  public int addedCount() {
    return addedCount;
  }

  @Override
  public int removedPartitionCount() {
    return removedPartitionCount;
  }

  @Override
  public int removedBlockCount() {
    return removedBlockCount;
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
   * Устанавливает список операций над разделами
   *
   * @param aInfoes {@link IList}&lt;{@link S5PartitionOperation}&gt; список операций
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setInfoes( IList<S5PartitionOperation> aInfoes ) {
    TsNullArgumentRtException.checkNull( aInfoes );
    operations = aInfoes;
  }

  /**
   * Добавляет количество таблиц проанализированных при поиске операций над разделами
   *
   * @param aAdded int количество таблиц
   */
  void addLookupCount( int aAdded ) {
    lookupCount += aAdded;
  }

  /**
   * Добавляет количество разделов таблиц добавленных в dbms
   *
   * @param aAdded int количество разделов
   */
  void addAdded( int aAdded ) {
    addedCount += aAdded;
  }

  /**
   * Добавляет количество разделов таблиц удаленных из dbms
   *
   * @param aRemoved int количество разделов
   */
  void addRemovedPartitions( int aRemoved ) {
    removedPartitionCount += aRemoved;
  }

  /**
   * Добавляет количество блоков удаленных из dbms
   *
   * @param aRemoved int количество разделов
   */
  void addRemovedBlocks( int aRemoved ) {
    removedBlockCount += aRemoved;
  }

  /**
   * Добавляет количество ошибок операций над разделами
   *
   * @param aErrors int количество ошибок
   */
  void addErrors( int aErrors ) {
    errors += aErrors;
  }

  /**
   * Устанавливает текущий размер очереди операций над разделами
   *
   * @param aSize размер очереди
   */
  void setQueueSize( int aSize ) {
    queueSize = aSize;
  }
}
