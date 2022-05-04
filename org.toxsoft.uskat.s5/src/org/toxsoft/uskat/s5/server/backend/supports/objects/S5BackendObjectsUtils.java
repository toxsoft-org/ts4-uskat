package org.toxsoft.uskat.s5.server.backend.supports.objects;

import static java.lang.String.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.common.dpu.*;

/**
 * Вспомогательные методы для работы с объектами и их атрибутами
 *
 * @author mvk
 */
public class S5BackendObjectsUtils {

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
    IAtomicValue defaultValue = typeParams.findValue( TSID_DEFAULT_VALUE );
    if( defaultValue != null ) {
      // У типа определено значение по умолчанию
      return defaultValue;
    }
    if( !typeParams.hasValue( TSID_IS_NULL_ALLOWED ) ) {
      // Отсутствие ограничения IS_NULL_ALLOWED по контракту константы является разрешением null
      return IAtomicValue.NULL;
    }
    defaultValue = (typeParams.getValue( TSID_IS_NULL_ALLOWED ).asBool() ? IAtomicValue.NULL : null);
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
}
