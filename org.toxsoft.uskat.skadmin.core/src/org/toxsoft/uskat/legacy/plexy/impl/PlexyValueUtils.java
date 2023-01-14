package org.toxsoft.uskat.legacy.plexy.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.errors.AvTypeCastRtException;
import org.toxsoft.core.tslib.av.impl.DataType;
import org.toxsoft.core.tslib.av.metainfo.IDataType;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.coll.basis.ITsCollection;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.legacy.plexy.*;

/**
 * Методы для работы с плекси-значениями.
 *
 * @author hazard157
 */
public class PlexyValueUtils {

  // ------------------------------------------------------------------------------------
  // Преопределенные константы
  //

  /**
   * Плекси-тип единичного значения атомарного типа {@link EAtomicType#BOOLEAN} без органичений типа.
   */
  public static final IPlexyType PT_SINGLE_BOOLEAN = PlexyType.createSingleValueType( DDEF_BOOLEAN );

  /**
   * Плекси-тип единичного значения атомарного типа {@link EAtomicType#INTEGER} без органичений типа.
   */
  public static final IPlexyType PT_SINGLE_INTEGER = PlexyType.createSingleValueType( DDEF_INTEGER );

  /**
   * Плекси-тип единичного значения атомарного типа {@link EAtomicType#FLOATING} без органичений типа.
   */
  public static final IPlexyType PT_SINGLE_FLOATING = PlexyType.createSingleValueType( DDEF_FLOATING );

  /**
   * Плекси-тип единичного значения атомарного типа {@link EAtomicType#STRING} без органичений типа.
   */
  public static final IPlexyType PT_SINGLE_STRING = PlexyType.createSingleValueType( DDEF_STRING );

  /**
   * Плекси-тип единичного значения атомарного типа {@link EAtomicType#TIMESTAMP} без органичений типа.
   */
  public static final IPlexyType PT_SINGLE_TIMESTAMP = PlexyType.createSingleValueType( DDEF_TIMESTAMP );

  /**
   * Плекси-тип единичного значения атомарного типа {@link EAtomicType#VALOBJ} без органичений типа.
   */
  public static final IPlexyType PT_SINGLE_VALOBJ = PlexyType.createSingleValueType( DDEF_VALOBJ );

  /**
   * Плекси-тип значения-списка атомарного типа {@link EAtomicType#BOOLEAN} без органичений типа.
   */
  public static final IPlexyType PT_LIST_BOOLEAN = PlexyType.createValueListType( DDEF_BOOLEAN );

  /**
   * Плекси-тип значения-списка атомарного типа {@link EAtomicType#INTEGER} без органичений типа.
   */
  public static final IPlexyType PT_LIST_INTEGER = PlexyType.createValueListType( DDEF_INTEGER );

  /**
   * Плекси-тип значения-списка атомарного типа {@link EAtomicType#FLOATING} без органичений типа.
   */
  public static final IPlexyType PT_LIST_FLOATING = PlexyType.createValueListType( DDEF_FLOATING );

  /**
   * Плекси-тип значения-списка атомарного типа {@link EAtomicType#STRING} без органичений типа.
   */
  public static final IPlexyType PT_LIST_STRING = PlexyType.createValueListType( DDEF_STRING );

  /**
   * Плекси-тип значения-списка атомарного типа {@link EAtomicType#TIMESTAMP} без органичений типа.
   */
  public static final IPlexyType PT_LIST_TIMESTAMP = PlexyType.createValueListType( DDEF_TIMESTAMP );

  /**
   * Плекси-тип значения-списка атомарного типа {@link EAtomicType#VALOBJ} без органичений типа.
   */
  public static final IPlexyType PT_LIST_VALOBJ = PlexyType.createValueListType( DDEF_VALOBJ );

  /**
   * Плекси-тип вида {@link EPlexyKind#OPSET}.
   */
  public static final IPlexyType PT_OPSET = PlexyType.createOpsetType();

  // ------------------------------------------------------------------------------------
  // Создание IPlexyType
  //

  /**
   * Создает плекси-тип вида {@link EPlexyKind#SINGLE_VALUE}.
   *
   * @param aDataType {@link IDataType} - тип единичного данного
   * @return {@link IPlexyType} - созданный тип
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static IPlexyType ptSingleValue( IDataType aDataType ) {
    return PlexyType.createSingleValueType( aDataType );
  }

  /**
   * Создает плекси-тип вида {@link EPlexyKind#VALUE_LIST}.
   *
   * @param aDataType {@link IDataType} - тип данных элемента списка
   * @return {@link IPlexyType} - созданный тип
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static IPlexyType ptValueList( IDataType aDataType ) {
    return PlexyType.createValueListType( aDataType );
  }

  /**
   * Создает плекси-тип вида {@link EPlexyKind#OPSET}.
   *
   * @return {@link IPlexyType} - созданный тип
   */
  public static IPlexyType ptOpset() {
    return PT_OPSET;
  }

  /**
   * Создает плекси-тип вида {@link EPlexyKind#SINGLE_REF}.
   *
   * @param aRefClass {@link Class} - тип (класс) ссылки
   * @return {@link IPlexyType} - созданный тип
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static IPlexyType ptSingleRef( Class<?> aRefClass ) {
    return PlexyType.createSingleRefType( aRefClass );
  }

  /**
   * Создает плекси-тип вида {@link EPlexyKind#REF_LIST}.
   *
   * @param aRefClass {@link Class} - тип (класс) ссылок в списке
   * @return {@link IPlexyType} - созданный тип
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static IPlexyType ptRefList( Class<?> aRefClass ) {
    return PlexyType.createRefListType( aRefClass );
  }

  // ------------------------------------------------------------------------------------
  // Создание IPlexyValue
  //

  /**
   * Создает единичное атомарное плекси-значения.
   *
   * @param aType {@link IPlexyType} - плекси-тип
   * @param aValue {@link IAtomicValue} - единичное атомарное значение
   * @return {@link IPlexyValue} - созданное плекси-значение
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aType не {@link EPlexyKind#SINGLE_VALUE}
   * @throws AvTypeCastRtException атомарный тип aValue не соответствует атомарному типу aType
   */
  // deprecated
  // public static IPlexyValue pvSingleValue( IPlexyType aType, IAtomicValue aValue ) {
  // return new PlexyValueSingleValueImpl( aType, aValue );
  // }

  /**
   * Создает единичное атомарное плекси-значения, тип которого определяется атомарным типом аргумента.
   *
   * @param aValue {@link IAtomicValue} - единичное атомарное значение
   * @return {@link IPlexyValue} - созданное плекси-значение
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static IPlexyValue pvSingleValue( IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNull( aValue );
    return new PlexyValueSingleValueImpl( ptSingleValue( new DataType( aValue.atomicType() ) ), aValue );
  }

  /**
   * Создает значение-списатоманых значений.
   *
   * @param aType {@link IPlexyType} - плекси-тип
   * @param aValues ITsReferenceCollection&lt;{@link IAtomicValue}&gt; - список атомарных значений
   * @return {@link IPlexyValue} - созданное плекси-значение
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aType не {@link EPlexyKind#VALUE_LIST}
   * @throws AvTypeCastRtException атомарный тип элемента списка не соответствует атомарному типу aType
   */
  public static IPlexyValue pvValueList( IPlexyType aType, ITsCollection<IAtomicValue> aValues ) {
    return new PlexyValueValueListImpl( aType, new ElemArrayList<>( aValues ) );
  }

  /**
   * Создает значение-спискок атомарный значений.
   *
   * @param aType {@link IPlexyType} - плекси-тип
   * @param aValues {@link IAtomicValue} - массив атомарных значений
   * @return {@link IPlexyValue} - созданное плекси-значение
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aType не {@link EPlexyKind#VALUE_LIST}
   * @throws AvTypeCastRtException атомарный тип элемента списка не соответствует атомарному типу aType
   */
  public static IPlexyValue pvValueList( IPlexyType aType, IAtomicValue... aValues ) {
    return new PlexyValueValueListImpl( aType, new ElemArrayList<>( aValues ) );
  }

  /**
   * Создает значение-набор.
   *
   * @param aOptionSet {@link IOptionSet}&gt; - набор значений
   * @return {@link IPlexyValue} - созданное плекси-значение
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static IPlexyValue pvOpset( IOptionSet aOptionSet ) {
    return new PlexyValueOpsetImpl( aOptionSet );
  }

  /**
   * Создает значение-ссылку на объект.
   *
   * @param aType {@link IPlexyType} - плекси-тип
   * @param aRef {@link Object} - ссылка
   * @return {@link IPlexyValue} - созданное плекси-значение
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws ClassCastException aRef не является классом {@link IPlexyType#refClass()}
   */
  public static IPlexyValue pvSingleRef( IPlexyType aType, Object aRef ) {
    return new PlexyValueSingleRefImpl( aType, aRef );
  }

  /**
   * Создает значение-ссылку на объект.
   * <p>
   * Класс ссылки {@link IPlexyType#refClass()} задается как класс аргумента {@link Object#getClass() aRef.getClass()}.
   *
   * @param aRef {@link Object} - ссылка
   * @return {@link IPlexyValue} - созданное плекси-значение
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static IPlexyValue pvSingleRef( Object aRef ) {
    TsNullArgumentRtException.checkNull( aRef );
    IPlexyType type = ptSingleRef( aRef.getClass() );
    return new PlexyValueSingleRefImpl( type, aRef );
  }

  /**
   * Создает значение-список ссылок.
   *
   * @param aType {@link IPlexyType} - плекси-тип
   * @param aRefList ITsReferenceCollection&lt;{@link Object}&gt; - список ссылок
   * @return {@link IPlexyValue} - созданное плекси-значение
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException aType не {@link EPlexyKind#REF_LIST}
   * @throws ClassCastException в аргументе есть объекты не класса {@link IPlexyType#refClass()}
   */
  public static IPlexyValue pvRefList( IPlexyType aType, ITsCollection<Object> aRefList ) {
    return new PlexyValueRefListImpl( aType, aRefList );
  }

  /**
   * Создает единичное значение.
   *
   * @param aValue boolean - значение
   * @return {@link IPlexyValue} - созданное значение
   */
  public static IPlexyValue pvsBool( boolean aValue ) {
    return pvSingleValue( avBool( aValue ) );
  }

  /**
   * Создает единичное значение.
   *
   * @param aValue int - значение
   * @return {@link IPlexyValue} - созданное значение
   */
  public static IPlexyValue pvsInt( int aValue ) {
    return pvSingleValue( avInt( aValue ) );
  }

  /**
   * Создает единичное значение.
   *
   * @param aValue long - значение
   * @return {@link IPlexyValue} - созданное значение
   */
  public static IPlexyValue pvsLong( long aValue ) {
    return pvSingleValue( avInt( aValue ) );
  }

  /**
   * Создает единичное значение.
   *
   * @param aValue long - значение
   * @return {@link IPlexyValue} - созданное значение
   */
  public static IPlexyValue pvsTimestamp( long aValue ) {
    return pvSingleValue( avTimestamp( aValue ) );
  }

  /**
   * Создает единичное значение.
   *
   * @param aValue float - значение
   * @return {@link IPlexyValue} - созданное значение
   */
  public static IPlexyValue pvsFloat( float aValue ) {
    return pvSingleValue( avFloat( aValue ) );
  }

  /**
   * Создает единичное значение.
   *
   * @param aValue double - значение
   * @return {@link IPlexyValue} - созданное значение
   */
  public static IPlexyValue pvsFloat( double aValue ) {
    return pvSingleValue( avFloat( aValue ) );
  }

  /**
   * Создает единичное значение.
   *
   * @param aValue String - значение
   * @return {@link IPlexyValue} - созданное значение
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IPlexyValue pvsStr( String aValue ) {
    return pvSingleValue( avStr( aValue ) );
  }

  /**
   * Создает единичное значение из {@link IStridable#id()}.
   *
   * @param aStridable {@link IStridable} - идентифицируемая сущность
   * @return {@link IPlexyValue} - созданное значение
   */
  public static IPlexyValue pvsStr( IStridable aStridable ) {
    TsNullArgumentRtException.checkNull( aStridable );
    return pvSingleValue( avStr( aStridable.id() ) );
  }

  /**
   * Запрет на создание экземпляров.
   */
  private PlexyValueUtils() {
    // nop
  }

}
