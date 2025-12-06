package org.toxsoft.uskat.legacy.plexy;

import java.io.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plexy.impl.*;

/**
 * Плекси-значение.
 * <p>
 * Плекси (образовано от исковерканного "мултиплексный") является пакетом для работы с плекси-значением, то есть, со
 * значением который может быть единичным {@link IAtomicValue}, списком атоманых значений или набором
 * {@link IOptionSet}.
 * <p>
 * Для создания экземпляров следует использовать методы {@link PlexyValueUtils}.pvXxx().
 *
 * @author hazard157
 */
public interface IPlexyValue {

  /**
   * "Нулеовое" плекси-значение.
   */
  IPlexyValue NULL = new InternalNullPlexyValue();

  /**
   * Возвращает плекси-тип этого плекси-значения :)
   *
   * @return {@link IPlexyType} - плекси-тип
   */
  IPlexyType type();

  /**
   * Возвращает единичное значение для плекси-значения вида {@link EPlexyKind#SINGLE_VALUE}.
   *
   * @return {@link IAtomicValue} - единичное значение
   * @throws TsUnsupportedFeatureRtException плекси-значения НЕ вида {@link EPlexyKind#SINGLE_VALUE}.
   */
  IAtomicValue singleValue();

  /**
   * Возвращает значение-список для плекси-значения вида {@link EPlexyKind#VALUE_LIST}.
   *
   * @return IList&lt;{@link IAtomicValue}&gt; - значение-список
   * @throws TsUnsupportedFeatureRtException плекси-значения НЕ вида {@link EPlexyKind#VALUE_LIST}.
   */
  IList<IAtomicValue> valueList();

  /**
   * Возвращает значение-набор для плекси-значения вида {@link EPlexyKind#OPSET}.
   *
   * @return {@link IAtomicValue} - значение-набор
   * @throws TsUnsupportedFeatureRtException плекси-значения НЕ вида {@link EPlexyKind#OPSET}.
   */
  IOptionSet getOpset();

  /**
   * Возвращает значение-ссылку для плекси-значения вида {@link EPlexyKind#SINGLE_REF}.
   *
   * @return T - значение-ссылка
   * @throws TsUnsupportedFeatureRtException плекси-значения НЕ вида {@link EPlexyKind#SINGLE_REF}.
   */
  <T> T singleRef();

  /**
   * Возвращает значение-список для плекси-значения вида {@link EPlexyKind#REF_LIST}.
   *
   * @return IList&lt;{@link Object}&gt; - значение-список
   * @throws TsUnsupportedFeatureRtException плекси-значения НЕ вида {@link EPlexyKind#REF_LIST}.
   */
  IList<Object> refList();

}

class InternalNullPlexyValue
    implements IPlexyValue, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Метод корректно восстанавливает сериализированный {@link IPlexyValue#NULL}.
   *
   * @return Object объект {@link IPlexyValue#NULL}
   * @throws ObjectStreamException это обявление, оно тут не выбрасывается
   */
  @SuppressWarnings( { "static-method" } )
  private Object readResolve()
      throws ObjectStreamException {
    return IPlexyValue.NULL;
  }

  @Override
  public IPlexyType type() {
    return IPlexyType.NONE;
  }

  @Override
  public IAtomicValue singleValue() {
    return IAtomicValue.NULL;
  }

  @Override
  public IList<IAtomicValue> valueList() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public IOptionSet getOpset() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public <T> T singleRef() {
    throw new TsNullObjectErrorRtException();
  }

  @Override
  public IList<Object> refList() {
    throw new TsNullObjectErrorRtException();
  }

}
