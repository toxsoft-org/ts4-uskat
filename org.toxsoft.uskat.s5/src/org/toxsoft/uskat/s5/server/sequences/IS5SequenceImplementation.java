package org.toxsoft.uskat.s5.server.sequences;

import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceBlob;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceBlock;

/**
 * Информация о реализации хранения последовательности значений
 *
 * @author mvk
 */
public interface IS5SequenceImplementation {

  /**
   * Возвращает имя класса реализации хранения блока последовательности значений, наследника {@link S5SequenceBlock}
   * <p>
   * Внимание: если хранение данного осуществляется в нескольких таблицах ({@link #tableCount()} > 1), то возвращается
   * префикс имени таблицы без указания индекса.
   *
   * @return String полное имя класса(или префикс без индекса) блока значений
   */
  String blockClassName();

  /**
   * Возвращает имя класса реализации хранения blob последовательности значений, наследника {@link S5SequenceBlob}
   * <p>
   * Внимание: если хранение данного осуществляется в нескольких таблицах ({@link #tableCount()} > 1), то возвращается
   * префикс имени таблицы без указания индекса.
   *
   * @return String полное имя класса(или префикс без индекса) blob значений
   */
  String blobClassName();

  /**
   * Возвращает количество таблиц хранящих последовательности значений
   *
   * @return int количество значений
   */
  int tableCount();
}
