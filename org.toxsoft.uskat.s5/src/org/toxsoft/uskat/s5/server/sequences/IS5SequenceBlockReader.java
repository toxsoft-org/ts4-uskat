package org.toxsoft.uskat.s5.server.sequences;

import org.toxsoft.core.tslib.utils.errors.TsUnderDevelopmentRtException;

/**
 * Читатель значения блока последовательности
 *
 * @author mvk
 */
public interface IS5SequenceBlockReader {

  /**
   * Возвращает признак того, что значение установлено и может быть прочитано
   *
   * @param aIndex индекс значения в блоке
   * @return boolean <b>true</b> значение установлено;<b>false</b> значение не установлено, попытка чтения приведет к
   *         ошибке.
   * @throws ArrayIndexOutOfBoundsException индекс значения за границами блока
   */
  default boolean isAssigned( int aIndex ) {
    throw new TsUnderDevelopmentRtException();
  }
}
