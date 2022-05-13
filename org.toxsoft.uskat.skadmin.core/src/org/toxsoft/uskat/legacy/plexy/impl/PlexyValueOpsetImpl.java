package org.toxsoft.uskat.legacy.plexy.impl;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.EPlexyKind;

/**
 * Реализация поекси-значения вида {@link EPlexyKind#OPSET}.
 *
 * @author goga
 */
class PlexyValueOpsetImpl
    extends AbstractPlexyValue {

  private final IOptionSet opset;

  /**
   * Конструктор значения-набора.
   * <p>
   * Конструктор не создает копию набора-аргумента, а просто запоминает ссылку на aValues.
   *
   * @param aOpSet {@link IOptionSet}&gt; - набор значений
   * @throws TsNullArgumentRtException аргумент = null
   */
  PlexyValueOpsetImpl( IOptionSet aOpSet ) {
    super( PlexyValueUtils.PT_OPSET );
    opset = TsNullArgumentRtException.checkNull( aOpSet );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса PlexyValueSingleImpl
  //

  @Override
  public IOptionSet getOpset() {
    return opset;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса Object
  //

  @Override
  public String toString() {
    return opset.toString();
  }

  @Override
  public boolean equals( Object aObj ) {
    if( aObj == this ) {
      return true;
    }
    if( aObj instanceof PlexyValueOpsetImpl obj ) {
      return opset.equals( obj.opset );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + opset.hashCode();
    return result;
  }

}
