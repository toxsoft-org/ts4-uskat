package org.toxsoft.uskat.sysext.realtime.supports.currdata;

import static org.toxsoft.core.tslib.gw.gwid.Gwid.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5BackendObjectsUtils.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.gw.gwid.GwidList;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5ObjectsInterceptor;

import ru.uskat.common.dpu.IDpuObject;
import ru.uskat.core.api.sysdescr.ISkClassInfo;
import ru.uskat.core.api.sysdescr.ISkRtdataInfo;
import ru.uskat.core.common.helpers.sysdescr.ISkSysdescrReader;

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

  private final ISkSysdescrReader           sysdescrReader;
  private final IS5BackendCurrDataSingleton currdataBackend;

  /**
   * Конструктор
   *
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель системного описания
   * @param aCurrdataBackend {@link IS5BackendCurrDataSingleton} backend управления текущими данными системы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5ObjectsInterceptor( ISkSysdescrReader aSysdescrReader, IS5BackendCurrDataSingleton aCurrdataBackend ) {
    TsNullArgumentRtException.checkNulls( aSysdescrReader, aCurrdataBackend );
    sysdescrReader = aSysdescrReader;
    currdataBackend = aCurrdataBackend;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5ObjectsInterceptor
  //
  @Override
  public IDpuObject beforeFindObject( Skid aSkid, IDpuObject aObj ) {
    return aObj;
  }

  @Override
  public IDpuObject afterFindObject( Skid aSkid, IDpuObject aObj ) {
    return aObj;
  }

  @Override
  public void beforeReadObjects( IStringList aClassIds, IListEdit<IDpuObject> aObjs ) {
    // nop
  }

  @Override
  public void afterReadObjects( IStringList aClassIds, IListEdit<IDpuObject> aObjs ) {
    // nop
  }

  @Override
  public void beforeReadObjectsByIds( ISkidList aSkids, IListEdit<IDpuObject> aObjs ) {
    // nop
  }

  @Override
  public void afterReadObjectsByIds( ISkidList aSkids, IListEdit<IDpuObject> aObjs ) {
    // nop
  }

  @Override
  public void beforeWriteObjects( IMap<ISkClassInfo, IList<IDpuObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDpuObject, IDpuObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDpuObject>> aCreatedObjs ) {
    // nop
  }

  @Override
  public void afterWriteObjects( IMap<ISkClassInfo, IList<IDpuObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDpuObject, IDpuObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDpuObject>> aCreatedObjs ) {
    // Список удаляемых данных.
    GwidList removedGwids = new GwidList();
    // Список удаляемых данных.
    removedGwids = new GwidList();
    for( ISkClassInfo classInfo : aRemovedObjs.keys() ) {
      String classId = classInfo.id();
      for( IDpuObject obj : aRemovedObjs.getByKey( classInfo ) ) {
        String strid = obj.strid();
        for( ISkRtdataInfo info : classInfo.rtdInfos() ) {
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
      for( IDpuObject obj : aCreatedObjs.getByKey( classInfo ) ) {
        String strid = obj.strid();
        for( ISkRtdataInfo info : classInfo.rtdInfos() ) {
          if( info.isCurr() ) {
            IDataDef typeDef = sysdescrReader.getType( info.typeId() );
            IAtomicValue value = findTypeDefaultValue( typeDef );
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
