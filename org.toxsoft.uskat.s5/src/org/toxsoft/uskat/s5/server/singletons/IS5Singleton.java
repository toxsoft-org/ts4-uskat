package org.toxsoft.uskat.s5.server.singletons;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Базовый интерфейс всех синглтонов сервера S5.
 *
 * @author mvk
 */
public interface IS5Singleton
    extends IStridable {

  /**
   * Устанавливает значение константы конфигурации.
   * <p>
   * Внимание! Значение константы не сохраняется и используется до перезапуска сервера.
   *
   * @param aConstant {@link IDataDef} определение константы
   * @param aValue {@link IAtomicValue} атомарное значение константы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  void setConfigurationConstant( IDataDef aConstant, IAtomicValue aValue );

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
   * Генерирует сообщение {@link S5SingletonBase#onConfigChanged(IOptionSet, IOptionSet)}.
   * <p>
   * После перезапуска сервера конфигурация загружается из базы данных.
   *
   * @param aConfiguration {@link IOptionSet} - конфигурация службы
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalStateRtException аргумент = недопустимый тип устанавливаемой конфиграции
   */
  void saveConfiguration( IOptionSet aConfiguration );
}
