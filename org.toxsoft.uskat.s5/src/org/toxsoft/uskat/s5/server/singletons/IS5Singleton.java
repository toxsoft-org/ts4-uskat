package org.toxsoft.uskat.s5.server.singletons;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Базовый интерфейс всех синглтонов сервера S5.
 *
 * @author mvk
 */
public interface IS5Singleton
    extends IStridable {

  /**
   * Возвращает конфигурацию службы
   * <p>
   * Тип конфигурации зависит от реализации службы
   *
   * @return IOptionSet конфигурация службы
   * @throws TsIllegalStateRtException аргумент = недопустимый тип запрашиваемой конфиграции
   */
  IOptionSet configuration();

  /**
   * Сохраняет конфигурацию службы в базе данных
   * <p>
   * Существующая конфигурация обновляется, несуществующая - создается.
   * <p>
   * TODO: Генерирует сообщение S5ServerMsgServiceConfigChanged.
   * <p>
   * После перезапуска сервера конфигурация загружается из базы данных.
   *
   * @param aConfiguration {@link IOptionSet} - конфигурация службы
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException аргумент = недопустимый тип устанавливаемой конфиграции
   */
  void saveConfiguration( IOptionSet aConfiguration );
}
