package org.toxsoft.uskat.skadmin.core.plugins;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.impl.DataType;
import org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants;
import org.toxsoft.core.tslib.av.metainfo.IDataType;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils;

/**
 * Вспомогательные методы для разработки реализации конечных плагинов команд
 *
 * @author mvk
 */
public class AdminPluginUtils {

  /**
   * Тип данных: {@link EAtomicType#NONE}, значение по умолчанию - нет.
   */
  public static final IDataType DT_NONE = DDEF_NONE;

  /**
   * {@link IPlexyType}, соответствующий {@link #DT_NONE}.
   */
  public static final IPlexyType PT_NONE = PlexyValueUtils.ptSingleValue( DT_NONE );

  /**
   * Тип данных: {@link EAtomicType#STRING}, значение по умолчанию - пустая строка.
   */
  public static final IDataType DT_STRING_EMPTY = createType( AvUtils.AV_STR_EMPTY );

  /**
   * {@link IPlexyType}, соответствующий {@link #DT_STRING_EMPTY}.
   */
  public static final IPlexyType PT_STRING_EMPTY = PlexyValueUtils.ptSingleValue( DT_STRING_EMPTY );

  /**
   * Тип данных: {@link EAtomicType#STRING}, значение по умолчанию - нет.
   */
  public static final IDataType DT_STRING_NONE = DDEF_STRING;

  /**
   * {@link IPlexyType}, соответствующий {@link #DT_STRING_NONE}.
   */
  public static final IPlexyType PT_STRING_NONE = PlexyValueUtils.ptSingleValue( DT_STRING_NONE );

  /**
   * Тип данных: {@link EAtomicType#BOOLEAN}, значение по умолчанию - false.
   */
  public static final IDataType DT_BOOLEAN_FALSE = createType( AvUtils.AV_FALSE );

  /**
   * {@link IPlexyType}, соответствующий {@link #DT_BOOLEAN_FALSE}.
   */
  public static final IPlexyType PT_BOOLEAN_FALSE = PlexyValueUtils.ptSingleValue( DT_BOOLEAN_FALSE );

  /**
   * Тип данных: {@link EAtomicType#BOOLEAN} с возможностью быть {@link IAtomicValue#NULL}.
   */
  public static final IDataType DT_BOOLEAN_NULLABLE = createNonMandatoryType( EAtomicType.BOOLEAN );

  /**
   * {@link IPlexyType}, соответствующий {@link #DT_BOOLEAN_NULLABLE}.
   */
  public static final IPlexyType PT_BOOLEAN_NULLABLE = PlexyValueUtils.ptSingleValue( DT_BOOLEAN_NULLABLE );

  /**
   * Тип данных: {@link EAtomicType#INTEGER} с возможностью быть {@link IAtomicValue#NULL}.
   */
  public static final IDataType DT_INTEGER_NULLABLE = createNonMandatoryType( EAtomicType.INTEGER );

  /**
   * {@link IPlexyType}, соответствующий {@link #DT_INTEGER_NULLABLE}.
   */
  public static final IPlexyType PT_INTEGER_NULLABLE = PlexyValueUtils.ptSingleValue( DT_INTEGER_NULLABLE );

  /**
   * Тип данных: {@link EAtomicType#FLOATING} с возможностью быть {@link IAtomicValue#NULL}.
   */
  public static final IDataType DT_FLOATING_NULLABLE = createNonMandatoryType( EAtomicType.FLOATING );

  /**
   * {@link IPlexyType}, соответствующий {@link #DT_FLOATING_NULLABLE}.
   */
  public static final IPlexyType PT_FLOATING_NULLABLE = PlexyValueUtils.ptSingleValue( DT_FLOATING_NULLABLE );

  /**
   * Тип данных: {@link EAtomicType#TIMESTAMP} с возможностью быть {@link IAtomicValue#NULL}.
   */
  public static final IDataType DT_TIMESTAMP_NULLABLE = createNonMandatoryType( EAtomicType.TIMESTAMP );

  /**
   * {@link IPlexyType}, соответствующий {@link #DT_TIMESTAMP_NULLABLE}.
   */
  public static final IPlexyType PT_TIMESTAMP_NULLABLE = PlexyValueUtils.ptSingleValue( DT_TIMESTAMP_NULLABLE );

  /**
   * Тип данных: {@link EAtomicType#STRING} с возможностью быть {@link IAtomicValue#NULL}.
   */
  public static final IDataType DT_STRING_NULLABLE = createNonMandatoryType( EAtomicType.STRING );

  /**
   * {@link IPlexyType}, соответствующий {@link #DT_STRING_NULLABLE}.
   */
  public static final IPlexyType PT_STRING_NULLABLE = PlexyValueUtils.ptSingleValue( DT_STRING_NULLABLE );

  /**
   * Создает тип данных со занчением по умолчанию {@link IAtomicValue#NULL}.
   * <p>
   * Создается {@link IDataType} с ограничением {@link IAvMetaConstants#TSID_DEFAULT_VALUE} со значением
   * {@link IAtomicValue#NULL}.
   *
   * @param aAtomicType {@link EAtomicType} - атомарный тип значения
   * @return {@link IDataType} - созданный тип данных
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static IDataType createNonMandatoryType( EAtomicType aAtomicType ) {
    TsNullArgumentRtException.checkNull( aAtomicType );
    IOptionSetEdit typeConstraints = new OptionSet();
    typeConstraints.setValue( TSID_DEFAULT_VALUE, IAtomicValue.NULL );
    return new DataType( aAtomicType, typeConstraints );
  }

  /**
   * Создает тип данных из атомарного значения с указанием значения по умолчанию.
   * <p>
   * Создается {@link IDataType} с ограничением {@link IAvMetaConstants#TSID_DEFAULT_VALUE} со значением aValue.
   * <p>
   * Значения тип {@link EAtomicType#NONE} не допускаются.
   *
   * @param aValue {@link IAtomicValue} - значение (становится значением по умолчанию)
   * @return {@link IDataType} - созданный тип данных
   * @throws TsNullArgumentRtException аргумент = null
   * @throws TsIllegalArgumentRtException аргумент имеет атомарный тип {@link EAtomicType#NONE}
   */
  public static IDataType createType( IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNull( aValue );
    TsIllegalArgumentRtException.checkTrue( aValue.atomicType() == EAtomicType.NONE );
    IOptionSetEdit typeConstraints = new OptionSet();
    typeConstraints.setValue( TSID_DEFAULT_VALUE, aValue );
    return new DataType( aValue.atomicType(), typeConstraints );
  }

  /**
   * Формирует тип значения {@link IAtomicValue}
   *
   * @param aAtomicType {@link EAtomicType} - атомарный тип значения
   * @param aDefaultValue String - значение по умолчанию
   * @return {@link IDataType} тип значения {@link IAtomicValue}.
   * @throws TsNullArgumentRtException любой аргумент =null
   * @throws TsIllegalArgumentRtException значение по умолчанию не может быть приведено к типу {@link IAtomicValue}
   */
  public static IDataType createType( EAtomicType aAtomicType, String aDefaultValue ) {
    TsNullArgumentRtException.checkNulls( aAtomicType, aDefaultValue );
    IOptionSetEdit ops = new OptionSet();
    try {
      switch( aAtomicType ) {
        case BOOLEAN:
          ops.setBool( TSID_DEFAULT_VALUE, Boolean.parseBoolean( aDefaultValue ) );
          break;
        case FLOATING:
          ops.setDouble( TSID_DEFAULT_VALUE, Double.parseDouble( aDefaultValue ) );
          break;
        case INTEGER:
        case TIMESTAMP:
          ops.setLong( TSID_DEFAULT_VALUE, Long.parseLong( aDefaultValue ) );
          break;
        case STRING:
          ops.setStr( TSID_DEFAULT_VALUE, aDefaultValue );
          break;
        case VALOBJ:
          // TODO:
          break;
        case NONE:
          // TODO:
          break;
        default:
          throw new TsNotAllEnumsUsedRtException();
      }
    }
    catch( NumberFormatException e ) {
      throw new TsIllegalArgumentRtException( e, e.getLocalizedMessage() );
    }
    return new DataType( aAtomicType, ops );
  }

}
