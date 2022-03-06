package org.toxsoft.uskat.s5.utils.progress;

/**
 * Интерфейс долго выполняемой задачи
 *
 * @author mvk
 */
public interface IS5RunnableWithProgress {

  /**
   * Запустить выполнение задачи
   *
   * @param aMonitor {@link IS5ProgressMonitor} монитор прогресса выполнения задачи
   */
  void run( IS5ProgressMonitor aMonitor );
}
