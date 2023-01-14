package org.toxsoft.uskat.legacy.plexy.impl;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.errors.AvTypeCastRtException;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.EPlexyKind;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;

/**
 * Реализация поекси-значения вида {@link EPlexyKind#SINGLE_VALUE}.
 *
 * @author hazard157
 */
class PlexyValueSingleValueImpl
    extends AbstractPlexyValue {

  private final IAtomicValue value;

  /**
   * Конструктор единичного значения.
   *
   * @param aType {@link IPlexyType} - плекси-тип
   * @param aValue {@link IAtomicValue} - единичное значение
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aType не {@link EPlexyKind#SINGLE_VALUE}
   * @throws AvTypeCastRtException атомарный тип aValue не соответствует атомарному типу aType
   */
  PlexyValueSingleValueImpl( IPlexyType aType, IAtomicValue aValue ) {
    super( aType );
    TsIllegalArgumentRtException.checkTrue( aType.kind() != EPlexyKind.SINGLE_VALUE );
    value = TsNullArgumentRtException.checkNull( aValue );
    if( aValue != IAtomicValue.NULL ) {
      AvTypeCastRtException.checkCanAssign( aType.dataType().atomicType(), aValue.atomicType() );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса PlexyValueSingleImpl
  //

  @Override
  public IAtomicValue singleValue() {
    return value;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса Object
  //

  @Override
  public String toString() {
    return value.asString();
  }

  @Override
  public boolean equals( Object aObj ) {
    if( aObj == this ) {
      return true;
    }
    if( aObj instanceof PlexyValueSingleValueImpl obj ) {
      if( type().equals( obj.type() ) ) {
        return value.equals( obj.value );
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + type().hashCode();
    result = TsLibUtils.PRIME * result + value.hashCode();
    return result;
  }

}
