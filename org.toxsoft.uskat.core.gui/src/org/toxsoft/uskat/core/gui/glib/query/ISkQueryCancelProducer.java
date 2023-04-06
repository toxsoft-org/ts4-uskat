package org.toxsoft.uskat.core.gui.glib.query;

/**
 * Интерфейс обработки отмены
 *
 * @author mvk
 */
public interface ISkQueryCancelProducer {

  /**
   * Установить обработчик отмены выполнения операции
   *
   * @param aCancelHandler {@link ISkQueryCancelHandler} обработчик отмены. null: удалить обработчик
   * @return {@link ISkQueryCancelHandler} предыдущий обработчик. null: не был установлен
   */
  ISkQueryCancelHandler setCancelHandler( ISkQueryCancelHandler aCancelHandler );

}
