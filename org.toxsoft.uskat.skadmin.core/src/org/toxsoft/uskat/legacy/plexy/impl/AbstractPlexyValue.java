package org.toxsoft.uskat.legacy.plexy.impl;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsUnsupportedFeatureRtException;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;

/**
 * Базовый класс для реализации PlecyValueXxxImpl.
 *
 * @author goga
 */
class AbstractPlexyValue
    implements IPlexyValue {

  private final IPlexyType type;

  /**
   * Конструктор для наследников.
   *
   * @param aType {@link IPlexyType} - тип значения
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected AbstractPlexyValue( IPlexyType aType ) {
    type = TsNullArgumentRtException.checkNull( aType );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IPlexyValue
  //

  @Override
  final public IPlexyType type() {
    return type;
  }

  @Override
  public IAtomicValue singleValue() {
    throw new TsUnsupportedFeatureRtException();
  }

  @Override
  public IList<IAtomicValue> valueList() {
    throw new TsUnsupportedFeatureRtException();
  }

  @Override
  public IOptionSet getOpset() {
    throw new TsUnsupportedFeatureRtException();
  }

  @Override
  public Object singleRef() {
    throw new TsUnsupportedFeatureRtException();
  }

  @Override
  public IList<Object> refList() {
    throw new TsUnsupportedFeatureRtException();
  }

}
