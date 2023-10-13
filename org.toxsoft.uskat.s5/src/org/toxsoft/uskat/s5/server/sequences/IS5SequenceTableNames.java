package org.toxsoft.uskat.s5.server.sequences;

/**
 * Имена таблиц хранения значений
 *
 * @author mvk
 */
public interface IS5SequenceTableNames {

  /**
   * Имя таблицы хранения блоков
   *
   * @return String имя таблицы
   */
  String blockTableName();

  /**
   * Имя таблицы хранения blob
   *
   * @return String имя таблицы
   */
  String blobTableName();
}
