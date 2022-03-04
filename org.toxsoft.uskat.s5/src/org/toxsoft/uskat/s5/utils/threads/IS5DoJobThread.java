package org.toxsoft.uskat.s5.utils.threads;

import org.toxsoft.uskat.s5.utils.jobs.IS5ServerJob;

/**
 * Поток вызывающий метод {@link IS5ServerJob#doJob()}
 *
 * @author mvk
 */
public interface IS5DoJobThread
    extends IS5Thread {

  /**
   * Возвращает таймаут (мсек) вызова метода {@link IS5ServerJob#doJob()}
   *
   * @return long таймаут между вызовами (мсек)
   */
  long doJobTimeout();
}
