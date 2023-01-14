package org.toxsoft.uskat.legacy.plexy.impl;

import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.EPlexyKind;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;

/**
 * Реализация поекси-значения вида {@link EPlexyKind#SINGLE_REF}.
 *
 * @author hazard157
 */
public class PlexyValueSingleRefImpl
    extends AbstractPlexyValue {

  private final Object ref;

  /**
   * Конструктор значения-ссылки.
   * <p>
   * Конструктор не создает копию аргумента, а просто запоминает ссылку aRef.
   *
   * @param aType {@link IPlexyType} - плекси-тип
   * @param aRef {@link Object} - ссылка на объект
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aType не {@link EPlexyKind#SINGLE_REF}
   * @throws ClassCastException aRef не является классом {@link IPlexyType#refClass()}
   */
  public PlexyValueSingleRefImpl( IPlexyType aType, Object aRef ) {
    super( aType );
    TsIllegalArgumentRtException.checkTrue( aType.kind() != EPlexyKind.SINGLE_REF );
    ref = TsNullArgumentRtException.checkNull( aRef );
    if( !aType.refClass().isInstance( aRef ) ) {
      throw new ClassCastException();
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса PlexyValueSingleImpl
  //

  @Override
  public Object singleRef() {
    return ref;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса Object
  //

  @Override
  public String toString() {
    return ref.toString();
  }

  @Override
  public boolean equals( Object aObj ) {
    if( aObj == this ) {
      return true;
    }
    if( aObj instanceof PlexyValueSingleRefImpl obj ) {
      if( type().equals( obj.type() ) ) {
        return ref.equals( obj.ref );
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + type().hashCode();
    result = TsLibUtils.PRIME * result + ref.hashCode();
    return result;
  }

}
