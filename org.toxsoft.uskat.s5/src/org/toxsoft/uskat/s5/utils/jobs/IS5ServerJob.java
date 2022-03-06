package org.toxsoft.uskat.s5.utils.jobs;

import javax.ejb.Local;

import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.utils.ICloseable;

/**
 * Фоновая задача выполняемая на сервере в режиме кооперативной многозадачности
 * <p>
 * При реализации метода {@link IS5ServerJob#doJob()} следует учитывать, что если поднимается какое-либо исключение, то
 * дальнейшее выполнение задачи прекращается без вызова {@link IS5ServerJob#close()}. Можно воспользоваться
 * {@link S5ServerJobWrapper} с параметром safe = <b>true</b>, чтобы изменить подобное поведение.
 *
 * @author mvk
 */
@Local
public interface IS5ServerJob
    extends ICooperativeMultiTaskable, ICloseable {

  /**
   * Подготовить фоновую задачу к запуску
   * <p>
   * Метод {@link #doJobPrepare()} вызывается один раз перед первым вызовом {@link #doJob()}.
   */
  default void doJobPrepare() {
    // nop
  }

  /**
   * Выполнить очередную небольшую порцию работы.
   */
  @Override
  void doJob();

  /**
   * Возвращает признак того, что задача завершена
   *
   * @return <b>true<b> выполнение задачи завершено или прекращено методом {@link #close()}; <b>false</b> задача еще
   *         выполняется.
   */
  boolean completed();

  /**
   * Завершить выполнение задачи
   */
  @Override
  void close();
}
