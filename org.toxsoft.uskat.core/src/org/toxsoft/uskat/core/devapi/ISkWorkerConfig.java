package org.toxsoft.uskat.core.devapi;

import org.toxsoft.core.tslib.bricks.strid.*;

/**
 * Описание конфигурации компонента обработки данных uskat {@link ISkWorker}.
 * <p>
 * Этот интерфейс реализует {@link IStridable}, поля которого имеют следующий смысл:
 * <ul>
 * <li><b>id</b>() - уникальный идентификатор компонента (ИД-путь);</li>
 * <li><b>description</b>() - удобочитаемое описание компонента;</li>
 * <li><b>nmName</b>() - краткое название компонента.</li>
 * </ul>
 *
 * @author mvk
 */
public interface ISkWorkerConfig
    extends IStridable {

  // nop
}
