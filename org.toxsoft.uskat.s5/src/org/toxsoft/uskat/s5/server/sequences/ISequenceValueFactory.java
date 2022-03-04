package org.toxsoft.uskat.s5.server.sequences;

import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.strid.IStridable;

/**
 * Фабрика формирования значений для последовательностей {@link IS5Sequence}
 *
 * @author mvk
 */
public interface ISequenceValueFactory
    extends IStridable {

  /**
   * Сформировать массив значений для блока
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @param aSize int количество значений в массиве
   * @return Object массив значений
   * @param <BLOB_ARRAY> тип массива blob-a в котором хранятся значения блока
   */
  <BLOB_ARRAY> BLOB_ARRAY createValueArray( IParameterized aTypeInfo, int aSize );

  /**
   * Возвращает значение по умолчанию используемое для инициализации синхронных значений в блоках
   * <p>
   * Если тип значений является примитивным типом, то для него возвращается соответствующая оболочка для примитивного
   * типа
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @return Object значение по умолчанию.
   */
  Object getSyncDefaultValue( IParameterized aTypeInfo );

  /**
   * Возвращает null-значение используемое для синхронных значений в блоках
   * <p>
   * Если тип значений является примитивным типом, то для него возвращается соответствующая оболочка для примитивного
   * типа
   *
   * @param aTypeInfo {@link IParameterized} параметризованное описание типа данного
   * @return Object значение по умолчанию.
   */
  Object getSyncNullValue( IParameterized aTypeInfo );
}
