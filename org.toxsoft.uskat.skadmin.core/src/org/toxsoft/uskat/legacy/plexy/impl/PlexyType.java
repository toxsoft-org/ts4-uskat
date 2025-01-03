package org.toxsoft.uskat.legacy.plexy.impl;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plexy.*;

/**
 * Неизменяемая реализация {@link IPlexyType}.
 *
 * @author hazard157
 */
class PlexyType
    implements IPlexyType {

  private static final IPlexyType PT_OPSET =
      new PlexyType( EPlexyKind.OPSET, DataType.create( EAtomicType.NONE ), null );

  private final EPlexyKind kind;
  private final IDataType  dataType;
  private final Class<?>   refClass;

  private PlexyType( EPlexyKind aKind, IDataType aDataType, Class<?> aRefClass ) {
    TsNullArgumentRtException.checkNull( aKind );
    TsIllegalArgumentRtException.checkTrue( aDataType == null && aRefClass == null );
    kind = aKind;
    dataType = aDataType;
    refClass = aRefClass;
  }

  static IPlexyType createSingleValueType( IDataType aDataType ) {
    TsNullArgumentRtException.checkNull( aDataType );
    return new PlexyType( EPlexyKind.SINGLE_VALUE, aDataType, null );
  }

  static IPlexyType createValueListType( IDataType aDataType ) {
    TsNullArgumentRtException.checkNull( aDataType );
    return new PlexyType( EPlexyKind.VALUE_LIST, aDataType, null );
  }

  static IPlexyType createSingleRefType( Class<?> aRefClass ) {
    TsNullArgumentRtException.checkNull( aRefClass );
    return new PlexyType( EPlexyKind.SINGLE_REF, null, aRefClass );
  }

  static IPlexyType createRefListType( Class<?> aRefClass ) {
    TsNullArgumentRtException.checkNull( aRefClass );
    return new PlexyType( EPlexyKind.REF_LIST, null, aRefClass );
  }

  static IPlexyType createOpsetType() {
    return PT_OPSET;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IPlexyType
  //

  @Override
  public EPlexyKind kind() {
    return kind;
  }

  @Override
  public IDataType dataType() {
    TsUnsupportedFeatureRtException.checkNull( dataType );
    return dataType;
  }

  @Override
  public Class<?> refClass() {
    TsUnsupportedFeatureRtException.checkNull( refClass );
    return refClass;
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса Object
  //

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return switch( kind ) {
      case SINGLE_VALUE -> dataType.atomicType().id();
      case VALUE_LIST -> dataType.atomicType().id() + "[]";
      case OPSET -> "{OptionSet}";
      case SINGLE_REF -> "Ref:" + refClass.toString();
      case REF_LIST -> "Ref:" + refClass.toString() + "[]";
      default -> throw new TsNotAllEnumsUsedRtException();
    };
  }

  @Override
  public boolean equals( Object aObj ) {
    if( aObj == this ) {
      return true;
    }
    if( aObj instanceof IPlexyType obj ) {
      if( kind == obj.kind() ) {
        return switch( kind ) {
          case OPSET -> true;
          case SINGLE_VALUE, VALUE_LIST -> dataType.atomicType().equals( obj.dataType().atomicType() );
          case REF_LIST, SINGLE_REF -> refClass.equals( obj.refClass() );
          default -> throw new TsNotAllEnumsUsedRtException();
        };
      }
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + kind.hashCode();
    switch( kind ) {
      case OPSET:
        break;
      case SINGLE_VALUE:
      case VALUE_LIST:
        result = TsLibUtils.PRIME * result + dataType.hashCode();
        break;
      case REF_LIST:
      case SINGLE_REF:
        result = TsLibUtils.PRIME * result + refClass.hashCode();
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    return result;
  }

}
