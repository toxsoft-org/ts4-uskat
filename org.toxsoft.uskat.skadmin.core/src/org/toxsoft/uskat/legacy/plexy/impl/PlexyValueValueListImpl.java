package org.toxsoft.uskat.legacy.plexy.impl;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.errors.AvTypeCastRtException;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.EPlexyKind;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;

/**
 * Реализация поекси-значения вида {@link EPlexyKind#VALUE_LIST}.
 *
 * @author goga
 */
class PlexyValueValueListImpl
    extends AbstractPlexyValue {

  private final IList<IAtomicValue> values;

  /**
   * Конструктор значения-списка.
   * <p>
   * Конструктор не создает копию списка-аргумента, а просто запоминает ссылку на aValues.
   *
   * @param aType {@link IPlexyType} - плекси-тип
   * @param aValues IList&lt;{@link IAtomicValue}&gt; - список значений
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aType не {@link EPlexyKind#VALUE_LIST}
   * @throws AvTypeCastRtException атомарный тип элемента списка не соответствует атомарному типу aType
   */
  PlexyValueValueListImpl( IPlexyType aType, IList<IAtomicValue> aValues ) {
    super( aType );
    TsIllegalArgumentRtException.checkTrue( aType.kind() != EPlexyKind.VALUE_LIST );
    for( int i = 0, count = aValues.size(); i < count; i++ ) {
      IAtomicValue av = aValues.get( i );
      if( av != IAtomicValue.NULL ) {
        AvTypeCastRtException.checkCanAssign( aType.dataType().atomicType(), av.atomicType() );
      }
    }
    values = TsNullArgumentRtException.checkNull( aValues );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса PlexyValueSingleImpl
  //

  @Override
  public IList<IAtomicValue> valueList() {
    return values;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса Object
  //

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    if( values.isEmpty() ) {
      return type().dataType().atomicType().id() + "[]";
    }
    return "[" + values.get( 0 ).asString() + ",...]";
  }

  @Override
  public boolean equals( Object aObj ) {
    if( aObj == this ) {
      return true;
    }
    if( aObj instanceof PlexyValueValueListImpl obj ) {
      if( type().equals( obj.type() ) ) {
        return values.equals( obj.values );
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + type().hashCode();
    result = TsLibUtils.PRIME * result + values.hashCode();
    return result;
  }

}
