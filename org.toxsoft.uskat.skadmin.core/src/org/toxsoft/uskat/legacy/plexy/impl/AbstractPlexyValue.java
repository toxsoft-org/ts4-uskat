package org.toxsoft.uskat.legacy.plexy.impl;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plexy.*;

/**
 * Базовый класс для реализации PlecyValueXxxImpl.
 *
 * @author hazard157
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
  public <T> T singleRef() {
    throw new TsUnsupportedFeatureRtException();
  }

  @Override
  public IList<Object> refList() {
    throw new TsUnsupportedFeatureRtException();
  }

}
