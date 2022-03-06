package org.toxsoft.uskat.s5.server.sequences.impl;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.time.ITemporal;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlock;
import org.toxsoft.uskat.s5.server.sequences.ISequenceFragmentInfo;
import org.toxsoft.uskat.s5.server.sequences.maintenance.IS5SequenceUnionStat;

/**
 * Реализация {@link IS5SequenceUnionStat}
 *
 * @author mvk
 * @param <V> тип значений в блоках
 */
final class S5SequenceUnionStat<V extends ITemporal<?>>
    implements IS5SequenceUnionStat, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Общее количество обработанных данных
   */
  private IList<ISequenceFragmentInfo> infoes = IList.EMPTY;

  /**
   * Общее количество обработанных последовательностей у которых произошло объединение
   */
  private int lookupCount = 0;

  /**
   * Общее количество обработанных блоков
   */
  private int dbmsMergedCount = 0;

  /**
   * Общее количество удаленных блоков
   */
  private int dbmsRemovedCount = 0;

  /**
   * Общее количество обработанных значений
   */
  private int valueCount = 0;

  /**
   * Количество ошибок объединения блоков
   */
  private int errors = 0;

  /**
   * Текущий размер очереди данных на дефрагментацию
   */
  private int queueSize = 0;

  /**
   * Последний блок с которым было проведено объединение. null: неопределен
   */
  private ISequenceBlock<V> lastUnitedBlock = null;

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequenceUnionStat
  //
  @Override
  public IList<ISequenceFragmentInfo> infoes() {
    return infoes;
  }

  @Override
  public int lookupCount() {
    return lookupCount;
  }

  @Override
  public int dbmsMergedCount() {
    return dbmsMergedCount;
  }

  @Override
  public int dbmsRemovedCount() {
    return dbmsRemovedCount;
  }

  @Override
  public int valueCount() {
    return valueCount;
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
   * Устанавливает список описаних данных поступивших на обработку
   *
   * @param aInfoes {@link IList}&lt; {@link ISequenceFragmentInfo}&gt; список описаний фрагментированных данных данных
   * @throws TsNullArgumentRtException аргумент = null
   */
  void setInfoes( IList<ISequenceFragmentInfo> aInfoes ) {
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
   * Добавляет количество блоков обновленных в dbms
   *
   * @param aMerged int количество блоков
   */
  void addDbmsMerged( int aMerged ) {
    dbmsMergedCount += aMerged;
  }

  /**
   * Добавляет количество блоков удаленных из dbms
   *
   * @param aRemoved int количество блоков
   */
  void addDbmsRemoved( int aRemoved ) {
    dbmsRemovedCount += aRemoved;
  }

  /**
   * Добавляет количество обработанных значений блоков
   *
   * @param aValue int количество значений
   */
  void addValues( int aValue ) {
    valueCount += aValue;
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

  /**
   * Устанавливает последний блок с которым было проведено объединение
   *
   * @param aBlock {@link ISequenceBlock} блок
   */
  void setUnitedBlock( ISequenceBlock<V> aBlock ) {
    TsNullArgumentRtException.checkNull( aBlock );
    lastUnitedBlock = aBlock;
  }

  /**
   * Возвращает последний блок с которым было проведено объединение
   *
   * @return {@link ISequenceBlock} блок. null: неопределен
   */
  ISequenceBlock<V> lastUnitedBlockOrNull() {
    return lastUnitedBlock;
  }

}
