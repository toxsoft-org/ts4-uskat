package org.toxsoft.uskat.s5.server.backend.supports.objects;

import static java.lang.String.*;
import static org.toxsoft.core.tslib.av.math.EAvCompareOp.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.math.AvComparatorStrict;
import org.toxsoft.core.tslib.av.math.IAvComparator;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;

import ru.uskat.common.dpu.*;
import ru.uskat.common.dpu.impl.IDpuHardConstants;

/**
 * Вспомогательные методы для работы с объектами и их атрибутами
 *
 * @author mvk
 */
public class S5BackendObjectsUtils {

  private static final IAvComparator avComparator = AvComparatorStrict.INSTANCE;

  /**
   * Возвращает признак того, что изменения описания типа не влияют на реализацию объектов в object management
   * <p>
   * Изменения типа влияющие на хранение объектов:
   * <ul>
   * <li>{@link IDpuHardConstants#OP_ATOMIC_TYPE};</li>
   * <li>{@link ETsConstraintNames#IS_NULL_ALLOWED} для любых примитивных типов;</li>
   * <li>{@link ETsConstraintNames#MAX_INCLUSIVE} для INTEGER, FLOATING и TIMESTAMP;</li>
   * <li>{@link ETsConstraintNames#MIN_INCLUSIVE} для INTEGER, FLOATING и TIMESTAMP;</li>
   * <li>{@link ETsConstraintNames#MAX_EXCLUSIVE} для INTEGER, FLOATING и TIMESTAMP;</li>
   * <li>{@link ETsConstraintNames#IS_NAN_ALLOWED} для FLOATING;</li>
   * <li>{@link ETsConstraintNames#IS_INF_ALLOWED} для FLOATING;</li>
   * <li>{@link ETsConstraintNames#MAX_CHARS} для STRING;</li>
   * </ul>
   *
   * @param aPrevTypeInfo {@link IDpuSdTypeInfo} предыдущее описание типа
   * @param aNewTypeInfo {@link IDpuSdTypeInfo} новое описание типа
   * @return boolean <b>true</b> изменения игнорируются;<b>fasle</b> изменения не могут быть игнорированы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static boolean isIgnoredTypeChanges( IDpuSdTypeInfo aPrevTypeInfo, IDpuSdTypeInfo aNewTypeInfo ) {
    return !needDefaultValue( aPrevTypeInfo, aNewTypeInfo, null );
  }

  /**
   * Возвращает признак того, что изменения описания типа требуют установить значение по умолчанию
   * <p>
   * Изменения типа влияющие на хранение объектов:
   * <ul>
   * <li>{@link IDpuHardConstants#OP_ATOMIC_TYPE};</li>
   * <li>{@link ETsConstraintNames#IS_NULL_ALLOWED} для любых примитивных типов;</li>
   * <li>{@link ETsConstraintNames#MAX_INCLUSIVE} для INTEGER, FLOATING и TIMESTAMP;</li>
   * <li>{@link ETsConstraintNames#MIN_INCLUSIVE} для INTEGER, FLOATING и TIMESTAMP;</li>
   * <li>{@link ETsConstraintNames#MAX_EXCLUSIVE} для INTEGER, FLOATING и TIMESTAMP;</li>
   * <li>{@link ETsConstraintNames#IS_NAN_ALLOWED} для FLOATING;</li>
   * <li>{@link ETsConstraintNames#IS_INF_ALLOWED} для FLOATING;</li>
   * <li>{@link ETsConstraintNames#MAX_CHARS} для STRING;</li>
   * </ul>
   *
   * @param aPrevTypeInfo {@link IDpuSdTypeInfo} предыдущее описание типа
   * @param aNewTypeInfo {@link IDpuSdTypeInfo} новое описание типа
   * @param aValueOrNull {@link IAtomicValue} текущее значение или null (не проверять значение)
   * @return boolean <b>true</b> изменения игнорируются;<b>fasle</b> изменения не могут быть игнорированы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static boolean needDefaultValue( IDpuSdTypeInfo aPrevTypeInfo, IDpuSdTypeInfo aNewTypeInfo,
      IAtomicValue aValueOrNull ) {
    TsNullArgumentRtException.checkNulls( aPrevTypeInfo, aNewTypeInfo );
    // Параметры типа старой редакции
    IOptionSet prevParams = aPrevTypeInfo.params();
    // Параметры типа новой редакции
    IOptionSet params = aNewTypeInfo.params();
    EAtomicType type = aNewTypeInfo.atomicType();
    if( !aPrevTypeInfo.atomicType().equals( type ) ) {
      return true;
    }
    if( needDefaultByChangeConstaint( prevParams, params, IS_NULL_ALLOWED, aValueOrNull ) ) {
      return true;
    }
    if( (type == EAtomicType.INTEGER || type == EAtomicType.FLOATING || type == EAtomicType.TIMESTAMP) && //
        (needDefaultByChangeConstaint( prevParams, params, MAX_INCLUSIVE, aValueOrNull ) == true || //
            needDefaultByChangeConstaint( prevParams, params, MIN_INCLUSIVE, aValueOrNull ) == true || //
            needDefaultByChangeConstaint( prevParams, params, MAX_EXCLUSIVE, aValueOrNull ) == true || //
            needDefaultByChangeConstaint( prevParams, params, MIN_EXCLUSIVE, aValueOrNull ) == true) ) {
      return true;
    }
    if( (type == EAtomicType.FLOATING) && //
        (needDefaultByChangeConstaint( prevParams, params, IS_NAN_ALLOWED, aValueOrNull ) == true || //
            needDefaultByChangeConstaint( prevParams, params, IS_INF_ALLOWED, aValueOrNull ) == true) ) {
      return true;
    }
    if( (type == EAtomicType.STRING)
        && (needDefaultByChangeConstaint( prevParams, params, MAX_CHARS, aValueOrNull ) == true) ) {
      return true;
    }
    return false;
  }

  /**
   * Формирует список добавленных и удаленных элементов класса (атрибуты, данные, связи, события, команды) из двух
   * представленных списков - старая и новая редакция
   *
   * @param <T> тип элемента
   * @param aPrevPropInfos {@link IStridablesList}&lt;{@link IDpuSdAttrInfo}&gt; список элементов (старая редакция)
   * @param aNewPropInfos {@link IStridablesList}&lt;{@link IDpuSdAttrInfo}&gt; список элементов (новая редакция)
   * @param aRemovedProps {@link IStridablesListEdit}&lt;{@link IDpuSdAttrInfo}&gt; список удаленных элементов
   * @param aAddedProps {@link IStridablesListEdit}&lt;{@link IDpuSdAttrInfo}&gt; список добавленных элементов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static <T extends IDpuSdPropInfoBase> void loadSysdescrChangedProps( IStridablesList<T> aPrevPropInfos,
      IStridablesList<T> aNewPropInfos, IStridablesListEdit<T> aRemovedProps, IStridablesListEdit<T> aAddedProps ) {
    TsNullArgumentRtException.checkNulls( aPrevPropInfos, aNewPropInfos, aRemovedProps, aAddedProps );
    for( T propInfo : aPrevPropInfos ) {
      if( !aNewPropInfos.hasElem( propInfo ) ) {
        aRemovedProps.put( propInfo );
      }
    }
    for( T propInfo : aNewPropInfos ) {
      if( !aPrevPropInfos.hasElem( propInfo ) ) {
        aAddedProps.put( propInfo );
      }
    }
  }

  /**
   * Формирует список добавленных и удаленных из двух представленных списков - старая и новая редакция
   *
   * @param aPrevProps {@link ISkidList} список элементов объекта (старая редакция)
   * @param aNewProps {@link ISkidList} список элементов объекта (новая редакция)
   * @param aRemovedProps {@link SkidList} список удаленных элементов
   * @param aCreatedProps {@link SkidList} список добавленных элементов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void loadSkidsChanges( ISkidList aPrevProps, ISkidList aNewProps, SkidList aRemovedProps,
      SkidList aCreatedProps ) {
    TsNullArgumentRtException.checkNulls( aPrevProps, aNewProps, aRemovedProps, aCreatedProps );
    for( Skid prop : aPrevProps ) {
      if( !aNewProps.hasElem( prop ) ) {
        aRemovedProps.add( prop );
      }
    }
    for( Skid prop : aNewProps ) {
      if( !aPrevProps.hasElem( prop ) ) {
        aCreatedProps.add( prop );
      }
    }
  }

  /**
   * Возвращает значение по умолчанию для типа
   *
   * @param aTypeInfo {@link IParameterized} описание типа с помощью параметров
   * @return {@link IAtomicValue} атомарное значение, возможно {@link IAtomicValue#NULL}. null: нет значения по
   *         умолчанию
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static IAtomicValue findTypeDefaultValue( IParameterized aTypeInfo ) {
    TsNullArgumentRtException.checkNull( aTypeInfo );
    // Параметры типа
    IOptionSet typeParams = aTypeInfo.params();
    // Значение по умолчанию для атрибута меняющего тип
    IAtomicValue defaultValue = typeParams.findValue( DEFAULT_VALUE );
    if( defaultValue != null ) {
      // У типа определено значение по умолчанию
      return defaultValue;
    }
    if( !typeParams.hasValue( IS_NULL_ALLOWED ) ) {
      // Отсутствие ограничения IS_NULL_ALLOWED по контракту константы является разрешением null
      return IAtomicValue.NULL;
    }
    defaultValue = (typeParams.getValue( IS_NULL_ALLOWED ).asBool() ? IAtomicValue.NULL : null);
    return defaultValue;
  }

  /**
   * Возвращает список объектов в строковом виде
   *
   * @param aObjs {@link IList}&lt; {@link IDpuObject}&gt; список объектов
   * @param aMaxCount int максимальное количество объектов возможных при выводе
   * @return String список объектов в текстовом виде
   * @throws TsNullArgumentRtException аргумент = null
   */
  public static String objsToStr( IList<IDpuObject> aObjs, int aMaxCount ) {
    TsNullArgumentRtException.checkNull( aObjs );
    StringBuilder sb = new StringBuilder();
    int count = 0;
    for( IDpuObject obj : aObjs ) {
      sb.append( format( "%s[%s]", obj.classId(), obj.strid() ) ); //$NON-NLS-1$
      if( count + 1 < aObjs.size() ) {
        sb.append( ", " ); //$NON-NLS-1$
      }
      if( ++count >= aMaxCount ) {
        if( count < aObjs.size() ) {
          sb.append( "..." ); //$NON-NLS-1$
        }
        return sb.toString();
      }
    }
    return sb.toString();
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает признак того, что изменение ограничения типа требует установки значения по умолчанию
   *
   * @param aPrevParams {@link IOptionSet} параметры типа старой редакции
   * @param aNewParams {@link IOptionSet} параметры типа новой редакции
   * @param aConstraint {@link ETsConstraintNames} проверяемое ограничение
   * @param aValueOrNull {@link IAtomicValue} текущее значение или null (не проверять значение)
   * @return boolean <b>true</b>требуется значение по умолчанию; <b>false</b>значение по умолчанию не требуется
   * @throws TsNullArgumentRtException любой аргумент (кроме aValueOrNull) = null
   */
  private static boolean needDefaultByChangeConstaint( IOptionSet aPrevParams, IOptionSet aNewParams,
      ETsConstraintNames aConstraint, IAtomicValue aValueOrNull ) {
    TsNullArgumentRtException.checkNulls( aPrevParams, aNewParams, aConstraint );
    IAtomicValue prevValue = aPrevParams.findValue( aConstraint );
    IAtomicValue newValue = aNewParams.findValue( aConstraint );
    // Признак того, что ограничение появляется в новой редакции
    boolean newConstraint = (prevValue == null && newValue != null);
    // Признак того, что ограничение было в прошлой редакции, есть в новой, но поменяло свое значение
    boolean editConstraint = (prevValue != null && newValue != null && !prevValue.equals( newValue ));
    switch( aConstraint ) {
      case MIN_EXCLUSIVE:
        if( aValueOrNull != null ) {
          return (newValue != null && aValueOrNull.isAssigned()
              && !avComparator.avCompare( newValue, LT, aValueOrNull ));
        }
        return (newConstraint || editConstraint);
      case MAX_EXCLUSIVE:
        if( aValueOrNull != null ) {
          return (newValue != null && aValueOrNull.isAssigned()
              && !avComparator.avCompare( newValue, GT, aValueOrNull ));
        }
        return (newConstraint || editConstraint);
      case MIN_INCLUSIVE:
        if( aValueOrNull != null ) {
          return (newValue != null && aValueOrNull.isAssigned()
              && !avComparator.avCompare( newValue, LE, aValueOrNull ));
        }
        return (newConstraint || editConstraint);
      case MAX_INCLUSIVE:
        if( aValueOrNull != null ) {
          return (newValue != null && aValueOrNull.isAssigned()
              && !avComparator.avCompare( newValue, GE, aValueOrNull ));
        }
        return (newConstraint || editConstraint);
      case IS_NULL_ALLOWED:
        if( aValueOrNull != null ) {
          return (newValue != null && !newValue.asBool() && !aValueOrNull.isAssigned());
        }
        return (newValue != null && !newValue.asBool()) && (newConstraint || editConstraint);
      case IS_INF_ALLOWED:
        if( aValueOrNull != null ) {
          return (newValue != null && !newValue.asBool() && //
              aValueOrNull.asDouble() == Double.NEGATIVE_INFINITY
              || aValueOrNull.asDouble() == Double.POSITIVE_INFINITY);
        }
        return (newValue != null && !newValue.asBool()) && (newConstraint || editConstraint);
      case IS_NAN_ALLOWED:
        if( aValueOrNull != null ) {
          return (newValue != null && !newValue.asBool() && aValueOrNull.asDouble() == Double.NaN);
        }
        return (newValue != null && !newValue.asBool()) && (newConstraint || editConstraint);
      case MAX_CHARS:
        if( aValueOrNull != null ) {
          return (newValue != null && aValueOrNull.isAssigned() && newValue.asInt() < aValueOrNull.asString().length());
        }
        return (newConstraint || editConstraint);
      case NAME:
      case DESCRIPTION:
      case DEFAULT_VALUE:
      case DEFAULT_FORMAT_STRING:
      case STRING_MASK:
      case MEASURE_UNIT_ID:
        throw new TsIllegalArgumentRtException();
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  }
}
