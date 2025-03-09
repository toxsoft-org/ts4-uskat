package org.toxsoft.uskat.s5.utils;

/**
 * Interval timer.
 *
 * @author mvk
 */
public final class S5IntervalTimer {

  private final long interval;
  private long       timestamp = System.currentTimeMillis();

  /**
   * Constructor.
   *
   * @param aInterval long time interval (msec)
   */
  public S5IntervalTimer( long aInterval ) {
    interval = aInterval;
  }

  /**
   * Updates the timer state and signals the start of the next interval..
   *
   * @return boolean <b>true</b> the start of next interval. <b>false</> continuation of the current interval.
   */
  public boolean update() {
    long currTime = System.currentTimeMillis();
    long prevSlot = timestamp / interval;
    long currSlot = currTime / interval;
    boolean retValue = (prevSlot != currSlot);
    if( retValue ) {
      // Фиксируем время начала текущего временного слота
      timestamp = currTime / interval * interval;
    }
    return retValue;
  }
}
