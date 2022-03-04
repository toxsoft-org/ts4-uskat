package org.toxsoft.uskat.s5.server.sequences.impl;

import java.io.Serializable;

import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.writer.IS5DbmsStatistics;

/**
 * Реализация {@link IS5DbmsStatistics}
 *
 * @author mvk
 */
final class S5DbmsStatistics
    implements IS5DbmsStatistics, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Формат toString
   */
  private static final String TO_STRING_FORMAT = "LOADED=%d [%d], ADDED=%d [%d], MERGED=%d [%d], REMOVED=%d [%d]"; //$NON-NLS-1$

  /**
   * Общее количество загруженных блоков и общее время загрузки (msec)
   */
  private volatile int loadedCount = 0;
  private volatile int loadedTime  = 0;

  /**
   * Общее количество добавленных блоков общее время добавления (msec)
   */
  private volatile int insertedCount = 0;
  private volatile int insertedTime  = 0;

  /**
   * Общее количество обновленных блоков общее время обновления (msec)
   */
  private volatile int mergedCount = 0;
  private volatile int mergedTime  = 0;

  /**
   * Общее количество удаленных блоков общее время удаления (msec)
   */
  private volatile int removedCount = 0;
  private volatile int removedTime  = 0;

  /**
   * Количество ошибок записей хранимых данных
   */
  private volatile int writeErrorCount;

  /**
   * Количество проведенных операций дефрагментации
   */
  private volatile int fragmentCount;

  /**
   * Количество прочитанных данных при обработке фрагментации
   */
  private volatile int fragmentLookuped;

  /**
   * Количество дефрагментированных данных
   */
  private volatile int fragmentThreaded;

  /**
   * Количество обработанных фрагментированных блоков данных
   */
  private volatile int fragmentBlocks;

  /**
   * Количество ошибок дефрагментации
   */
  private volatile int fragmentErrorCount;

  /**
   * Конструктор по умолчанию
   */
  S5DbmsStatistics() {
    clear();
  }

  /**
   * Конструктор копирования
   *
   * @param aSource {@link IS5DbmsStatistics} источник
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5DbmsStatistics( IS5DbmsStatistics aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    loadedCount = aSource.loadedCount();
    loadedTime = aSource.loadedTime();
    insertedCount = aSource.insertedCount();
    insertedTime = aSource.insertedTime();
    mergedCount = aSource.mergedCount();
    mergedTime = aSource.mergedTime();
    removedCount = aSource.removedCount();
    removedTime = aSource.removedTime();
    writeErrorCount = aSource.writeErrorCount();
    fragmentCount = aSource.fragmentCount();
    fragmentLookuped = aSource.fragmentLookuped();
    fragmentThreaded = aSource.fragmentThreaded();
    fragmentBlocks = aSource.fragmentBlocks();
    fragmentErrorCount = aSource.fragmentErrorCount();
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5DbmsStatistics
  //
  @Override
  public int loadedCount() {
    return loadedCount;
  }

  @Override
  public int loadedTime() {
    return loadedTime;
  }

  @Override
  public int insertedCount() {
    return insertedCount;
  }

  @Override
  public int insertedTime() {
    return insertedTime;
  }

  @Override
  public int mergedCount() {
    return mergedCount;
  }

  @Override
  public int mergedTime() {
    return mergedTime;
  }

  @Override
  public int removedCount() {
    return removedCount;
  }

  @Override
  public int removedTime() {
    return removedTime;
  }

  @Override
  public int writeErrorCount() {
    return writeErrorCount;
  }

  @Override
  public int fragmentCount() {
    return fragmentCount;
  }

  @Override
  public int fragmentLookuped() {
    return fragmentLookuped;
  }

  @Override
  public int fragmentThreaded() {
    return fragmentThreaded;
  }

  @Override
  public int fragmentBlocks() {
    return fragmentBlocks;
  }

  @Override
  public int fragmentErrorCount() {
    return fragmentErrorCount;
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Инкрементирует счетчик ошибок операций записи
   */
  void addWriteErrorCount() {
    writeErrorCount++;
  }

  /**
   * Добавляет данные статистики о загруженных блоках из базы данных
   *
   * @param aLoadedCount int количество загруженных блоков последовательности
   * @param aLoadedTime int время (мсек) загрузки блоков в последовательность
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void addLoaded( int aLoadedCount, int aLoadedTime ) {
    loadedCount += aLoadedCount;
    loadedTime += aLoadedTime;
  }

  /**
   * Добавляет данные статистики о добавленных(inserted) блоках в базу данных
   *
   * @param aInsertedCount int количество добавленных блоков в dbms
   * @param aInsertedTime int время (мсек) добавления блоков в dbms
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void addInserted( int aInsertedCount, int aInsertedTime ) {
    insertedCount += aInsertedCount;
    insertedTime += aInsertedTime;
  }

  /**
   * Добавляет данные статистики об обновленных(merged) блоках в базе данных
   *
   * @param aMergedCount int количество обновленных блоков в dbms
   * @param aMergedTime long время (мсек) обновления блоков в dbms
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void addMergedCount( int aMergedCount, int aMergedTime ) {
    mergedCount += aMergedCount;
    mergedTime += aMergedTime;
  }

  /**
   * Добавляет данные статистики об удаленных(removed) блоках из базы данных
   *
   * @param aRemovedCount int количество удаленных блоков в dbms
   * @param aRemovedTime int время (мсек) удаленных блоков в dbms
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void addRemovedCount( int aRemovedCount, int aRemovedTime ) {
    removedCount += aRemovedCount;
    removedTime += aRemovedTime;
  }

  /**
   * Инкрементирует счетчик проведенных операций дефрагментации
   */
  void addFragmentCount() {
    fragmentCount++;
  }

  /**
   * Инкрементирует счетчик ошибок проведенных операций дефрагментации
   *
   * @param aErrorCount int количество ошибок дефрагментации
   */
  void addFragmentErrorCount( int aErrorCount ) {
    fragmentErrorCount += aErrorCount;
  }

  /**
   * Добавляет данные статистики о проведенной операции дефрагментации
   *
   * @param aFragmentLookuped int
   * @param aFragmentThreaded int
   * @param aFragmentBlocks int
   */
  void addFragmentStat( int aFragmentLookuped, int aFragmentThreaded, int aFragmentBlocks ) {
    fragmentLookuped += aFragmentLookuped;
    fragmentThreaded += aFragmentThreaded;
    fragmentBlocks += aFragmentBlocks;
  }

  /**
   * Очистить статистику
   */
  void clear() {
    loadedCount = 0;
    loadedTime = 0;
    insertedCount = 0;
    insertedTime = 0;
    mergedCount = 0;
    mergedTime = 0;
    removedCount = 0;
    removedTime = 0;
    writeErrorCount = 0;
    fragmentCount = 0;
    fragmentLookuped = 0;
    fragmentThreaded = 0;
    fragmentBlocks = 0;
    fragmentErrorCount = 0;
  }

  // ------------------------------------------------------------------------------------
  // Реализация Object
  //
  @Override
  public String toString() {
    Long lc = Long.valueOf( loadedCount );
    Long lt = Long.valueOf( loadedTime );
    Long ac = Long.valueOf( insertedCount );
    Long at = Long.valueOf( insertedTime );
    Long mc = Long.valueOf( mergedCount );
    Long mt = Long.valueOf( mergedTime );
    Long rc = Long.valueOf( removedCount );
    Long rt = Long.valueOf( removedTime );
    return String.format( TO_STRING_FORMAT, lt, lc, at, ac, mt, mc, rt, rc );
  }
}
