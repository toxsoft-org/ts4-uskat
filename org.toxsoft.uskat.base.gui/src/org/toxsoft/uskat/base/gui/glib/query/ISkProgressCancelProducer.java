package org.toxsoft.uskat.base.gui.glib.query;

/**
 * Интерфейс обработки отмены
 *
 * @author mvk
 */
public interface ISkProgressCancelProducer {

  /**
   * Установить обработчик отмены выполнения операции
   *
   * @param aCancelHandler {@link ISkProgressCancelHandler} обработчик отмены. null: удалить обработчик
   * @return {@link ISkProgressCancelHandler} предыдущий обработчик. null: не был установлен
   */
  ISkProgressCancelHandler setCancelHandler( ISkProgressCancelHandler aCancelHandler );

}
