package org.toxsoft.uskat.s5.utils.platform;

/**
 * Информация о целевой платформе на которой работает сервер
 *
 * @author mvk
 */
public final class S5PlatformInfo {

  private final long timestamp;
  private final double loadAverage;
  private final long freePhysicalMemory;
  private final long maxHeapMemory;
  private final long usedHeapMemory;
  private final long maxNonHeapMemory;
  private final long usedNonHeapMemory;

  /**
   * Конструктор (видимость пакета)
   *
   * @param aLoadAverage double средняя загрузка операционной системы
   * @param aFreePhysicalMemory long объем (байты) свободной памяти операционной системы
   * @param aMaxHeapMemory long максимальный объем (байты) heap памяти
   * @param aUsedHeapMemory long используемый объем (байты) heap памяти
   * @param aMaxNonHeapMemory long максимальный объем (байты) non-heap памяти
   * @param aUsedNonHeapMemory long используемый объем (байты) non-heap памяти
   */
  S5PlatformInfo( double aLoadAverage, long aFreePhysicalMemory, long aMaxHeapMemory, long aUsedHeapMemory,
      long aMaxNonHeapMemory, long aUsedNonHeapMemory ) {
    timestamp = System.currentTimeMillis();
    loadAverage = aLoadAverage;
    freePhysicalMemory = aFreePhysicalMemory;
    maxHeapMemory = aMaxHeapMemory;
    usedHeapMemory = aUsedHeapMemory;
    maxNonHeapMemory = aMaxNonHeapMemory;
    usedNonHeapMemory = aUsedNonHeapMemory;
  }

  /**
   * Метка времени формирования информации
   *
   * @return long (мсек с начала эпохи)
   */
  public long timestamp() {
    return timestamp;
  }

  /**
   * Средняя загрузка операционной системы
   *
   * @return double средняя загрузка
   */
  public double loadAverage() {
    return loadAverage;
  }

  /**
   * Объем свободной памяти операционной системы
   *
   * @return long объем памяти (байты)
   */
  public long freePhysicalMemory() {
    return freePhysicalMemory;
  }

  /**
   * Максимальный объем heap памяти
   *
   * @return long объем памяти (байты)
   */
  public long maxHeapMemory() {
    return maxHeapMemory;
  }

  /**
   * Используемый объем heap памяти
   *
   * @return long объем памяти (байты)
   */
  public long usedHeapMemory() {
    return usedHeapMemory;
  }

  /**
   * Максимальный объем non-heap памяти
   *
   * @return long объем памяти (байты)
   */
  public long maxNonHeapMemory() {
    return maxNonHeapMemory;
  }

  /**
   * Используемый объем non-heap памяти
   *
   * @return long объем памяти (байты)
   */
  public long usedNonHeapMemory() {
    return usedNonHeapMemory;
  }

}
