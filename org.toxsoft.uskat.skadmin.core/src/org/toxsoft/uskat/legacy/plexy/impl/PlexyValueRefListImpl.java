package org.toxsoft.uskat.legacy.plexy.impl;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.basis.ITsCollection;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.EPlexyKind;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;

/**
 * Реализация поекси-значения вида {@link EPlexyKind#REF_LIST}.
 *
 * @author hazard157
 */
class PlexyValueRefListImpl
    extends AbstractPlexyValue {

  private final IListEdit<Object> refList;

  /**
   * Конструктор значения-списка ссылок.
   * <p>
   * Конструктор создает копию списка, но не копии элементов списка.
   *
   * @param aType {@link IPlexyType} - плекси-тип
   * @param aRefList ITsReferenceCollection&lt;{@link Object}&gt; - список ссылок
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aType не {@link EPlexyKind#REF_LIST}
   * @throws ClassCastException в аргументе есть объекты не класса {@link IPlexyType#refClass()}
   */
  PlexyValueRefListImpl( IPlexyType aType, ITsCollection<Object> aRefList ) {
    super( aType );
    TsIllegalArgumentRtException.checkTrue( aType.kind() != EPlexyKind.REF_LIST );
    refList = new ElemArrayList<>( aRefList.size() );
    for( Object o : aRefList ) {
      if( !aType.refClass().isInstance( o ) ) {
        throw new ClassCastException();
      }
      refList.add( o );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса PlexyValueSingleImpl
  //

  @Override
  public IList<Object> refList() {
    return refList;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса Object
  //

  @Override
  public String toString() {
    return refList.toString();
  }

  @Override
  public boolean equals( Object aObj ) {
    if( aObj == this ) {
      return true;
    }
    if( aObj instanceof PlexyValueRefListImpl obj ) {
      if( type().equals( obj.type() ) ) {
        return refList.equals( obj.refList );
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + type().hashCode();
    result = TsLibUtils.PRIME * result + refList.hashCode();
    return result;
  }

}
