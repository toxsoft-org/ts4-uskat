package org.toxsoft.uskat.skadmin.core;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;

/**
 * Параметр контекста используемый командой при выполнении задачи
 * <p>
 * Интерфейс расширяет {@link IStridable}, поля которого имеют следующий смысл:
 * <ul>
 * <li><b>id</b>() - уникальный идентификатор параметра (ИД-имя);</li>
 * <li><b>nmName</b>() - краткое название параметра;</li>
 * <li><b>description</b>() - удобочитаемое описание параметра.</li>
 * </ul>
 *
 * @author mvk
 */
public interface IAdminCmdContextParam
    extends IStridable {

  /**
   * Возвращает тип значений параметра
   *
   * @return {@link IPlexyType} тип значения
   */
  IPlexyType type();
}
