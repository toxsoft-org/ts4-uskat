package org.toxsoft.uskat.legacy.plugins;

import org.toxsoft.core.tslib.utils.TsVersion;

/**
 * Минимальная информация, необходимая и достаточная для идентификации подключаемого модуля.
 *
 * @author goga
 */
public interface IPluginBasicInfo {

  /**
   * Возвращает версию подключаемого модуля (плагина).
   *
   * @return TsVersion - версия плагина
   */
  TsVersion pluginVersion();

  /**
   * Возвращает строку-идентификатор типа модуля. Идентификатор типа должен быть уникален в рамках разрабатываемой
   * системы.<br>
   * Вариант использования - полное имя базового класса/интерфейса всех модулей данного типа.
   *
   * @return строка ненулевой длины, составленный по правилам именования Java-классов с полным указанием пакетов
   */
  String pluginType();

  /**
   * Возвращает строку-идентификатор модуля. Идентификатор модуля должен быть уникальным внутри типа.<br>
   * Вариант использования - полное имя класса модуля.
   *
   * @return строка ненулевой длины, составленный по правилам именования Java-классов с полным указанием пакетов
   */
  String pluginId();

}