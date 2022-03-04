package org.toxsoft.uskat.s5.utils.threads;

import org.toxsoft.core.tslib.utils.ICloseable;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.utils.threads.impl.S5AbstractThread;

/**
 * Исполнитель s5-потоков {@link IS5Thread}
 * <p>
 * Интерфейс расширяет {@link ICloseable}. Следует учитывать, что вызов {@link #close()} блокирует поток выполнения до
 * завершения исполнителя потоков.
 *
 * @author mvk
 * @param <THREAD_TYPE> тип потока
 */
public interface IS5ThreadExecutor<THREAD_TYPE extends IS5Thread>
    extends ICloseable {

  /**
   * Возвращает признак того, что исполнитель и его потоки завершили свою работу
   *
   * @return <b>true</b> исполнитель завершил свою работу; <b>false</b> исполнитель не завершил свою работу
   */
  boolean completed();

  /**
   * Добавляет поток на выполнение
   * <p>
   * Метод размещает поток в очереди потоков на выполнение. Потоки будут выполняться после вызова
   * {@link #run(boolean, boolean)}
   *
   * @param aThread {@link IS5Thread} добавляемый поток
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException поток должен быть наследником {@link S5AbstractThread}
   * @throws TsIllegalStateRtException исполнитель уже работает или завершил свою работу
   */
  void add( THREAD_TYPE aThread );

  /**
   * Количество зарегистированных потоков
   *
   * @return int количество потоков
   */
  int threadCount();

  /**
   * Количество потоков завершивших (штатно или по ошибке) свое выполнение
   *
   * @return int количество потоков
   */
  int threadCompletedCount();

  /**
   * Запускает исполнитель и все добавленные потоки
   * <p>
   * Если исполнитель уже запущен то ничего не делает
   *
   * @param aWait boolean <b>true</b> ожидать завершения выполнения потоков; <b>false</b> не ожидать завершения.
   * @param aThrowable boolean <b>true</b> поднимать исключение при появлении ошибки в любом из потоков; <b>false</b>
   *          игнорировать ошибки возникающие при выполнеии потоков.
   * @throws TsIllegalStateRtException исполнитель завершил свою работу
   */
  void run( boolean aWait, boolean aThrowable );
}
