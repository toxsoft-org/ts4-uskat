package org.toxsoft.uskat.s5.server.backend.supports.currdata.impl;

import static org.toxsoft.core.tslib.gw.gwid.Gwid.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5BackendObjectsUtils.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.metainfo.IDataType;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.objserv.IDtoObject;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoRtdataInfo;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.IS5BackendCurrDataSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5ObjectsInterceptor;

/**
 * Интерсептор операций над объектами используемый {@link S5BackendCurrDataSingleton}
 * <p>
 * Решаемые задачи:
 * <ul>
 * <li>Отслеживание добавления объектов. Установка значения текущих данных по умолчанию;</li>
 * <li>Отслеживание удаления объектов. Удаление текущих данных;</li>
 * </ul>
 *
 * @author mvk
 */
class S5ObjectsInterceptor
    implements IS5ObjectsInterceptor {

  private final IS5BackendCurrDataSingleton currdataBackend;

  /**
   * Конструктор
   *
   * @param aCurrdataBackend {@link IS5BackendCurrDataSingleton} backend управления текущими данными системы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5ObjectsInterceptor( IS5BackendCurrDataSingleton aCurrdataBackend ) {
    TsNullArgumentRtException.checkNulls( aCurrdataBackend );
    currdataBackend = aCurrdataBackend;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5ObjectsInterceptor
  //
  @Override
  public IDtoObject beforeFindObject( Skid aSkid, IDtoObject aObj ) {
    return aObj;
  }

  @Override
  public IDtoObject afterFindObject( Skid aSkid, IDtoObject aObj ) {
    return aObj;
  }

  @Override
  public void beforeReadObjects( IStringList aClassIds, IListEdit<IDtoObject> aObjs ) {
    // nop
  }

  @Override
  public void afterReadObjects( IStringList aClassIds, IListEdit<IDtoObject> aObjs ) {
    // nop
  }

  @Override
  public void beforeReadObjectsByIds( ISkidList aSkids, IListEdit<IDtoObject> aObjs ) {
    // nop
  }

  @Override
  public void afterReadObjectsByIds( ISkidList aSkids, IListEdit<IDtoObject> aObjs ) {
    // nop
  }

  @Override
  public void beforeWriteObjects( IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs ) {
    // nop
  }

  @Override
  public void afterWriteObjects( IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs ) {
    // Список удаляемых данных.
    GwidList removedGwids = new GwidList();
    // Список удаляемых данных.
    removedGwids = new GwidList();
    for( ISkClassInfo classInfo : aRemovedObjs.keys() ) {
      String classId = classInfo.id();
      for( IDtoObject obj : aRemovedObjs.getByKey( classInfo ) ) {
        String strid = obj.strid();
        for( IDtoRtdataInfo info : classInfo.rtdata().list() ) {
          if( info.isCurr() ) {
            removedGwids.add( createRtdata( classId, strid, info.id() ) );
          }
        }
      }
    }
    // Карта добавляемых данных.
    IMapEdit<Gwid, IAtomicValue> addedGwids = new ElemMap<>();
    for( ISkClassInfo classInfo : aCreatedObjs.keys() ) {
      String classId = classInfo.id();
      for( IDtoObject obj : aCreatedObjs.getByKey( classInfo ) ) {
        String strid = obj.strid();
        for( IDtoRtdataInfo info : classInfo.rtdata().list() ) {
          if( info.isCurr() ) {
            IDataType type = info.dataType();
            IAtomicValue value = findTypeDefaultValue( type );
            addedGwids.put( createRtdata( classId, strid, info.id() ), value );
          }
        }
      }
    }
    if( removedGwids.size() > 0 || addedGwids.size() > 0 ) {
      // Изменение кэша текущих данных
      currdataBackend.reconfigure( removedGwids, addedGwids );
    }
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
