package org.toxsoft.uskat.s5.utils;

/**
 * Interval timer.
 *
 * @author mvk
 */
public final class S5IntervalTimer {

  private final long interval;
  private long       startTime = System.currentTimeMillis();
  private long       timestamp = startTime;

  /**
   * Constructor.
   *
   * @param aInterval long time interval (msec)
   */
  public S5IntervalTimer( long aInterval ) {
    interval = aInterval;
  }

  /**
   * Returns a timer overflow flag.
   *
   * @return boolean <b>true</b> interval is over; <b>false</b> interval isn't over.
   */
  public boolean isOver() {
    long currTime = System.currentTimeMillis();
    long prevSlot = timestamp / interval;
    long currSlot = currTime / interval;
    boolean retValue = (prevSlot < currSlot);
    return retValue;
  }

  /**
   * Updates the timer state and signals the start of the next interval.
   *
   * @return boolean <b>true</b> the start of next interval. <b>false</> continuation of the current interval.
   */
  public boolean update() {
    boolean nextInterval = isOver();
    if( nextInterval ) {
      reset();
    }
    return nextInterval;
  }

  /**
   * Resets a timer.
   */
  public void reset() {
    timestamp = System.currentTimeMillis() / interval * interval;
  }

  /**
   * Returns the zero-based interval number after timer start.
   *
   * @return int interval number. 0: first interval.
   */
  public int intervalNo() {
    long startSlot = startTime / interval;
    long currSlot = System.currentTimeMillis() / interval;
    return (int)(currSlot - startSlot);
  }

}
