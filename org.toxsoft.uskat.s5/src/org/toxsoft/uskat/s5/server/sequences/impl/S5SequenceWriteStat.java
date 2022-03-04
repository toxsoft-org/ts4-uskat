package org.toxsoft.uskat.s5.server.sequences.impl;

import static org.toxsoft.core.tslib.bricks.time.impl.TimeUtils.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.impl.TimeInterval;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.sequences.writer.IS5SequenceWriteStat;

/**
 * Реализация {@link IS5SequenceWriteStat}
 *
 * @author mvk
 */
public final class S5SequenceWriteStat
    implements IS5SequenceWriteStat, Serializable {

  private static final long serialVersionUID = 157157L;

  private volatile long createTime = System.currentTimeMillis();
  private volatile long startTime  = System.currentTimeMillis();
  private volatile long endTime    = System.currentTimeMillis();
  private volatile int  dataCount;

  /**
   * Общее количество сохраненных блоков в последовательностях и общее время их сохранения (msec)
   */
  private volatile long writedBlockStartTime = MIN_TIMESTAMP;
  private volatile long writedBlockEndTime   = MIN_TIMESTAMP;
  private volatile int  writedBlockCount     = 0;

  /**
   * Статистика ввода/вывода dbms
   */
  private final S5DbmsStatistics dbmsStatistics = new S5DbmsStatistics();

  // ------------------------------------------------------------------------------------
  // Реализация IS5SequenceWriteStat
  //
  @Override
  public long createTime() {
    return createTime;
  }

  @Override
  public long startTime() {
    return startTime;
  }

  @Override
  public long endTime() {
    return endTime;
  }

  @Override
  public int dataCount() {
    return dataCount;
  }

  @Override
  public int writedBlockCount() {
    return writedBlockCount;
  }

  @Override
  public ITimeInterval writedBlockInterval() {
    return new TimeInterval( writedBlockStartTime, writedBlockEndTime );
  }

  @Override
  public S5DbmsStatistics dbmsStatistics() {
    return dbmsStatistics;
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Устанавливает время начала процесса записи
   *
   * @param aStartTime long (мсек с начала эпохи) начала записи
   */
  void setStartTime( long aStartTime ) {
    startTime = aStartTime;
  }

  /**
   * Устанавливает время завершения процесса записи (НЕ транзакции!)
   *
   * @param aEndTime long (мсек с начала эпохи) начала записи
   */
  void setEndTime( long aEndTime ) {
    endTime = aEndTime;
  }

  /**
   * Устанавливает количество записываемых данных
   *
   * @param aCount количество данных
   */
  void setDataCount( int aCount ) {
    dataCount = aCount;
  }

  /**
   * Добавляет данные статистики о проведенной операции записи
   *
   * @param aWritedInterval {@link ITimeInterval} совокупный интервал блоков записанных последователей
   * @param aWriteCount int количество записанных блоков в последовательность
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void addWriteBlocks( ITimeInterval aWritedInterval, int aWriteCount ) {
    TsNullArgumentRtException.checkNull( aWritedInterval );
    long st = aWritedInterval.startTime();
    long et = aWritedInterval.endTime();
    if( writedBlockStartTime == MIN_TIMESTAMP || writedBlockStartTime > st ) {
      writedBlockStartTime = st;
    }
    if( writedBlockEndTime == MIN_TIMESTAMP || writedBlockEndTime > et ) {
      writedBlockEndTime = et;
    }
    writedBlockCount += aWriteCount;
  }
}
