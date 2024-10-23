package org.toxsoft.uskat.legacy.plexy;

import java.io.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plexy.impl.*;

/**
 * Плекси-тип.
 * <p>
 * Плекси (образовано от исковерканного "мултиплексный") является пакетом для работы с плекси-значением, то есть, со
 * значением который может быть единичным {@link IAtomicValue}, списком атоманых значений или набором {@link IOptionSet}
 * .
 * <p>
 * Для создания экземпляров следует использовать методы {@link PlexyValueUtils}.ptXxx().
 *
 * @author hazard157
 */
public interface IPlexyType {

  /**
   * Константа "никакого" типа.
   */
  IPlexyType NONE = new InternalNullPlexyType();

  /**
   * Возвращает вид плекси-значения.
   *
   * @return {@link EPlexyKind} - вид значения
   */
  EPlexyKind kind();

  /**
   * Тип данного для {@link EPlexyKind#isAtomic() kind().isAtomic()} = <code>true</code>.
   *
   * @return {@link IDataType} - тип данного в плекси-значении
   * @throws TsUnsupportedFeatureRtException неприменимый вид {@link #kind()} плекси-типа
   */
  IDataType dataType();

  /**
   * Тип ссылки для {@link EPlexyKind#isReference() kind().isReference()} = <code>true</code>.
   *
   * @return {@link Class} - тип (класс) ссылки вплекси-значений
   * @throws TsUnsupportedFeatureRtException неприменимый вид {@link #kind()} плекси-типа
   */
  Class<?> refClass();

}

class InternalNullPlexyType
    implements IPlexyType, Serializable {

  private static final long serialVersionUID = 157157L;
  private IDataType         type             = DataType.create( EAtomicType.NONE );

  /**
   * Метод корректно восстанавливает сериализированный {@link IPlexyType#NONE}.
   *
   * @return Object объект {@link IPlexyType#NONE}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return IPlexyType.NONE;
  }

  @Override
  public EPlexyKind kind() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public IDataType dataType() {
    return type;
  }

  @Override
  public Class<?> refClass() {
    throw new TsNullObjectErrorRtException();
  }

}
