package org.toxsoft.uskat.s5.server.sequences.impl;

import java.io.Serializable;

import org.toxsoft.uskat.s5.server.sequences.maintenance.IS5SequenceValidationStat;

/**
 * Реализация {@link IS5SequenceValidationStat}
 *
 * @author mvk
 */
final class S5SequenceValidationStat
    implements IS5SequenceValidationStat, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Общее количество обработанных данных
   */
  private int infoCount = 0;
  private transient Object infoCountLock = new Object();

  /**
   * Общее количество обновленных блоков
   */
  private int processedCount = 0;
  private transient Object processedCountLock = new Object();

  /**
   * Общее количество предупреждений
   */
  private int warnCount = 0;
  private transient Object warnCountLock = new Object();

  /**
   * Общее количество ошибок
   */
  private int errCount = 0;
  private transient Object errCountLock = new Object();

  /**
   * Общее количество обновленных блоков
   */
  private int dbmsMergedCount = 0;
  private transient Object dbmsMergedCountLock = new Object();

  /**
   * Общее количество удаленных блоков
   */
  private int dbmsRemovedCount = 0;
  private transient Object dbmsRemovedCountLock = new Object();

  /**
   * Общее количество знаений в блоках
   */
  private int valuesCount = 0;
  private transient Object valuesCountLock = new Object();

  /**
   * Общее количество блоков с неэффективным хранением
   */
  private int nonOptimalCount;
  private transient Object nonOptimalCountLock = new Object();

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequenceValidationStat
  //
  @Override
  public int infoCount() {
    return infoCount;
  }

  @Override
  public int processedCount() {
    return processedCount;
  }

  @Override
  public int warnCount() {
    return warnCount;
  }

  @Override
  public int errCount() {
    return errCount;
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
  public int valuesCount() {
    return valuesCount;
  }

  @Override
  public int nonOptimalCount() {
    return nonOptimalCount;
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Добавляет количество обработанных данных
   */
  void addInfo() {
    synchronized (infoCountLock) {
      infoCount++;
    }
  }

  /**
   * Добавляет количество обработанных блоков
   *
   * @param aProcessed int количество обработанных блоков
   */
  void addProcessed( int aProcessed ) {
    synchronized (processedCountLock) {
      processedCount += aProcessed;
    }
  }

  /**
   * Добавляет количество предупреждений
   *
   * @param aWarnings int количество предупреждений
   */
  void addWarnings( int aWarnings ) {
    synchronized (warnCountLock) {
      warnCount += aWarnings;
    }
  }

  /**
   * Добавляет количество ошибок
   *
   * @param aErrors int количество ошибок
   */
  void addErrors( int aErrors ) {
    synchronized (errCountLock) {
      errCount += aErrors;
    }
  }

  /**
   * Добавляет количество блоков обновленных в dbms
   *
   * @param aMerged int количество блоков
   */
  void addDbmsMerged( int aMerged ) {
    synchronized (dbmsMergedCountLock) {
      dbmsMergedCount += aMerged;
    }
  }

  /**
   * Добавляет количество блоков удаленных из dbms
   *
   * @param aRemoved int количество блоков
   */
  void addDbmsRemoved( int aRemoved ) {
    synchronized (dbmsRemovedCountLock) {
      dbmsRemovedCount += aRemoved;
    }
  }

  /**
   * Добавляет количество значений в блоках
   *
   * @param aValues int количество значений в блоках
   */
  void addValues( int aValues ) {
    synchronized (valuesCountLock) {
      valuesCount += aValues;
    }
  }

  /**
   * Добавляет количество блоков с неффективным хранением
   *
   * @param aNonOptimal int количество блоков
   */
  void addNonOptimal( int aNonOptimal ) {
    synchronized (nonOptimalCountLock) {
      nonOptimalCount += aNonOptimal;
    }
  }
}
