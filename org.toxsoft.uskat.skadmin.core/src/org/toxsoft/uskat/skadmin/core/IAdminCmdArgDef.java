package org.toxsoft.uskat.skadmin.core;

import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;

/**
 * Описание аргумента команды {@link IAdminCmdDef}.
 * <p>
 * Этот интерфейс реализует {@link IStridable}, поля которого имеют следующий смысл:
 * <ul>
 * <li>{@link #id()} - уникальный, в рамках команды,идентификатор аргумента (ИД-имя);</li>
 * <li>{@link #nmName()} - краткое, НЕуникальное, удобочитаемое имя аргумента.;</li>
 * <li>{@link #description()} - удобочитаемое описание смысла аргмента.</li>
 * </ul>
 *
 * @author mvk
 */
public interface IAdminCmdArgDef
    extends IStridable {

  /**
   * Альтернативный идентификатор(ИД-имя) аргумента в краткой форме, уникальный, в рамках команды.
   *
   * @return String идентификатор аргумента в краткой форме
   */
  String alias();

  /**
   * Тип значения аргумента
   *
   * @return {@link IPlexyType} тип значения команды
   */
  IPlexyType type();

}
