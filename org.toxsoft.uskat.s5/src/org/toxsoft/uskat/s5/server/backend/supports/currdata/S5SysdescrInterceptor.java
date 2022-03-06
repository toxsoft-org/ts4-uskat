package org.toxsoft.uskat.s5.server.backend.supports.currdata;

import static java.lang.String.*;
import static org.toxsoft.core.tslib.gw.gwid.Gwid.*;
import static org.toxsoft.uskat.s5.server.backend.supports.currdata.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5BackendObjectsUtils.*;
import static org.toxsoft.uskat.s5.server.transactions.ES5TransactionResources.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.coll.primtypes.IIntMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.IStringListEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.IntMap;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectEntity;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5ClassesInterceptor;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5TypesInterceptor;
import org.toxsoft.uskat.s5.server.transactions.*;

import ru.uskat.common.dpu.*;
import ru.uskat.core.common.helpers.sysdescr.ISkSysdescrReader;

/**
 * Интерсептор системного описания используемый {@link S5BackendCurrDataSingleton}
 * <p>
 * Решаемые задачи:
 * <ul>
 * <li>Отслеживание изменения типа данных объектов. Установка данного в значение по умолчанию для нового типа;</li>
 * <li>Отслеживание изменения класса объекта. Добавление(значение по умолчанию) и/или удаление текущих данных;</li>
 * </ul>
 *
 * @author mvk
 */
class S5SysdescrInterceptor
    implements IS5TypesInterceptor, IS5ClassesInterceptor {

  private final IS5TransactionManagerSingleton txManager;
  private final ISkSysdescrReader              sysdescrReader;
  private final IS5BackendObjectsSingleton     objectsBackend;
  private final IS5BackendCurrDataSingleton    currdataBackend;

  /**
   * Конструктор
   *
   * @param aTransactionManager {@link IS5TransactionManagerSingleton} менеджер транзакций
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель системного описания
   * @param aObjectsBackend {@link IS5BackendObjectsSingleton} менеджер управления объектами
   * @param aCurrdataBackend {@link IS5BackendCurrDataSingleton} backend управления текущими данными системы
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5SysdescrInterceptor( IS5TransactionManagerSingleton aTransactionManager, ISkSysdescrReader aSysdescrReader,
      IS5BackendObjectsSingleton aObjectsBackend, IS5BackendCurrDataSingleton aCurrdataBackend ) {
    TsNullArgumentRtException.checkNulls( aTransactionManager, aSysdescrReader, aObjectsBackend, aCurrdataBackend );
    txManager = aTransactionManager;
    sysdescrReader = aSysdescrReader;
    objectsBackend = aObjectsBackend;
    currdataBackend = aCurrdataBackend;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5TypesInterceptor
  //
  @Override
  public void beforeCreateType( IDpuSdTypeInfo aTypeInfo ) {
    // nop
  }

  @Override
  public void afterCreateType( IDpuSdTypeInfo aTypeInfo ) {
    // nop
  }

  @Override
  public void beforeUpdateType( IDpuSdTypeInfo aPrevTypeInfo, IDpuSdTypeInfo aNewTypeInfo,
      IStridablesList<IDpuSdClassInfo> aDependentClasses ) {
    // null: без проверки текущего значения (общая проверка для всех объектов)
    if( isIgnoredTypeChanges( aPrevTypeInfo, aNewTypeInfo ) ) {
      // Изменения типа не влияют на реализацию значений текущих данных
      return;
    }
    // Список идентификаторов классов объекты которых могут изменить тип текущих данных
    IStringListEdit classIds = new StringArrayList( aDependentClasses.keys().size() );
    for( String classId : aDependentClasses.keys() ) {
      IDpuSdClassInfo classInfo = aDependentClasses.getByKey( classId );
      for( IDpuSdRtdataInfo rtdataInfo : classInfo.rtdataInfos() ) {
        if( !rtdataInfo.isCurr() ) {
          continue;
        }
        if( rtdataInfo.typeId().equals( aPrevTypeInfo.id() ) ) {
          // Объекты класса будут изменять тип текущих данных
          classIds.add( classId );
          break;
        }
      }
    }
    // Список объектов непозволяющих поменять тип текущего данного
    IList<IDpuObject> objs = objectsBackend.readObjects( classIds );
    if( objs.size() == 0 ) {
      // Нет объектов
      return;
    }
    // Значение по умолчанию для нового типа. null: у типа нет значения по умолчанию
    if( findTypeDefaultValue( aNewTypeInfo ) == null ) {
      // Существуют объекты атрибуты которых не допускают установку типа без значения по умолчанию
      throw new TsIllegalStateRtException( ERR_NO_DEFAULT_VALUE, objsToStr( objs, 5 ) );
    }
    // Текущая транзакция
    IS5Transaction tx = txManager.getTransaction();
    // Сохраняем в транзакции список объектов менящих тип текущих данных
    TsIllegalStateRtException.checkNoNull( tx.putResource( TX_UPDATED_OBJS_BY_CURRDATA_TYPE, objs ) );
  }

  @Override
  public void afterUpdateType( IDpuSdTypeInfo aPrevTypeInfo, IDpuSdTypeInfo aNewTypeInfo,
      IStridablesList<IDpuSdClassInfo> aDependentClasses ) {
    String typeId = aPrevTypeInfo.id();
    // Тип значений текущего данного по умолчанию
    IAtomicValue defaultValue = findTypeDefaultValue( aNewTypeInfo );
    // Текущая транзакция
    IS5Transaction tx = txManager.getTransaction();
    // Список объектов меняющих тип атрибутов
    IList<S5ObjectEntity> objs = tx.findResource( TX_UPDATED_OBJS_BY_CURRDATA_TYPE );
    if( objs != null ) {
      IIntMapEdit<IAtomicValue> newValues = new IntMap<>();
      for( S5ObjectEntity obj : objs ) {
        IDpuSdClassInfo classInfo = aDependentClasses.getByKey( obj.classId() );
        for( IDpuSdRtdataInfo rtdataInfo : classInfo.rtdataInfos() ) {
          if( !rtdataInfo.typeId().equals( typeId ) ) {
            // Тип значения не меняется
            continue;
          }
          if( !rtdataInfo.isCurr() ) {
            // Данное не является текущим
            continue;
          }
          // Идентификатор текущего данного
          Gwid gwid = createRtdata( obj.classId(), obj.strid(), rtdataInfo.id() );
          // Предыдущее значение данного
          Pair<Integer, IAtomicValue> prevValue = currdataBackend.getValue( gwid );
          // Проверка текущего значения на необходимость его сброса в значение по умолчанию
          if( needDefaultValue( aPrevTypeInfo, aNewTypeInfo, prevValue.right() ) ) {
            // Изменение типа данного. Установка значения по умолчанию
            newValues.put( prevValue.left().intValue(), defaultValue );
          }
        }
      }
      if( newValues.size() > 0 ) {
        // Запись текущих данных
        currdataBackend.writeCurrData( newValues );
      }
    }
  }

  @Override
  public void beforeDeleteType( IDpuSdTypeInfo aTypeInfo ) {
    // nop
  }

  @Override
  public void afterDeleteType( IDpuSdTypeInfo aTypeInfo ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5ClassesInterceptor
  //
  @Override
  public void beforeCreateClass( IDpuSdClassInfo aClassInfo ) {
    // nop
  }

  @Override
  public void afterCreateClass( IDpuSdClassInfo aClassInfo ) {
    // nop
  }

  @Override
  public void beforeUpdateClass( IDpuSdClassInfo aPrevClassInfo, IDpuSdClassInfo aNewClassInfo,
      IStridablesList<IDpuSdClassInfo> aDescendants ) {
    // Идентификатор изменяемого класса
    String classId = aNewClassInfo.id();
    // Список удаленных текущих данных
    IStridablesListEdit<IDpuSdRtdataInfo> removedRtdata = new StridablesList<>();
    // Список добавленных текущих данных
    IStridablesListEdit<IDpuSdRtdataInfo> addedRtdata = new StridablesList<>();
    // Анализ для формирования списка добавленных и удаленных данных
    loadSysdescrChangedProps( aPrevClassInfo.rtdataInfos(), aNewClassInfo.rtdataInfos(), removedRtdata, addedRtdata );
    // Фильтрация текущих данных
    for( IDpuSdRtdataInfo info : new StridablesList<>( removedRtdata ) ) {
      if( !info.isCurr() ) {
        removedRtdata.remove( info );
      }
    }
    for( IDpuSdRtdataInfo info : new StridablesList<>( addedRtdata ) ) {
      if( !info.isCurr() ) {
        addedRtdata.remove( info );
      }
    }
    // Проверка есть ли добавленные или удаленные текущие данные. Если нет, то проверка не требуется
    if( removedRtdata.size() == 0 && addedRtdata.size() == 0 ) {
      return;
    }
    // Список объектов изменяющих состав текущих данных
    IList<IDpuObject> objs = S5TransactionUtils.txUpdatedClassObjs( txManager, objectsBackend, classId, aDescendants );
    if( objs.size() == 0 ) {
      // Нет объектов
      return;
    }
    // Если есть объекты, то все вновь добавляемые текущие данные ОБЯЗАНЫ иметь значение по умолчанию
    StringBuilder sbError = new StringBuilder();
    for( IDpuSdRtdataInfo rtdataInfo : addedRtdata ) {
      IDataDef type = sysdescrReader.findType( rtdataInfo.typeId() );
      IAtomicValue defaultValue = findTypeDefaultValue( type );
      if( defaultValue == null ) {
        // Текущее данное имеет тип у которого нет значения по умолчанию
        sbError.append( format( ERR_CURRDATA_NOT_HAVE_DEFAULT_VALUE, rtdataInfo.id(), type.id() ) );
      }
    }
    if( sbError.length() > 0 ) {
      // Запрет изменения класса имеющий объекты
      throw new TsIllegalStateRtException( ERR_CANT_CHANGE_CLASS_WITH_OBJS, classId, sbError.toString() );
    }
    // Текущая транзакция
    IS5Transaction tx = txManager.getTransaction();
    // Сохраняем в транзакции список добавляемых и удаляемых текущих данных
    TsIllegalStateRtException.checkNoNull( tx.putResource( TX_REMOVED_CURRDATA, removedRtdata ) );
    TsIllegalStateRtException.checkNoNull( tx.putResource( TX_ADDED_CURRDATA, addedRtdata ) );
  }

  @Override
  public void afterUpdateClass( IDpuSdClassInfo aPrevClassInfo, IDpuSdClassInfo aNewClassInfo,
      IStridablesList<IDpuSdClassInfo> aDescendants ) {
    // Текущая транзакция
    IS5Transaction tx = txManager.getTransaction();
    // Список описаний удаляемых текущих данных
    IStridablesList<IDpuSdRtdataInfo> removedCurrdata = tx.findResource( TX_REMOVED_CURRDATA );
    // Список описаний добавляемых текущих данных
    IStridablesList<IDpuSdRtdataInfo> addedCurrdata = tx.findResource( TX_ADDED_CURRDATA );
    // Список идентификаторов удаляемых данных
    IGwidList removedGwids = IGwidList.EMPTY;
    // Список идентификаторов добавляемых данных и их значений
    IMapEdit<Gwid, IAtomicValue> addedGwids = IMap.EMPTY;
    if( removedCurrdata != null ) {
      IList<S5ObjectEntity> objs = tx.getResource( TX_UPDATED_CLASS_OBJS );
      removedGwids = new GwidList();
      for( S5ObjectEntity obj : objs ) {
        String classId = obj.classId();
        String strid = obj.strid();
        for( IDpuSdRtdataInfo rtdataInfo : removedCurrdata ) {
          ((GwidList)removedGwids).add( createRtdata( classId, strid, rtdataInfo.id() ) );
        }
      }
    }
    if( addedCurrdata != null ) {
      IList<S5ObjectEntity> objs = tx.getResource( TX_UPDATED_CLASS_OBJS );
      addedGwids = new ElemMap<>();
      for( S5ObjectEntity obj : objs ) {
        String classId = obj.classId();
        String strid = obj.strid();
        for( IDpuSdRtdataInfo rtdataInfo : addedCurrdata ) {
          IDataDef type = sysdescrReader.findType( rtdataInfo.typeId() );
          addedGwids.put( createRtdata( classId, strid, rtdataInfo.id() ), findTypeDefaultValue( type ) );
        }
      }
    }
    if( removedGwids.size() > 0 || addedGwids.size() > 0 ) {
      // Запрос на изменение кэша текущих данных
      currdataBackend.reconfigure( removedGwids, addedGwids );
    }
  }

  @Override
  public void beforeDeleteClass( IDpuSdClassInfo aClassInfo ) {
    // Целостность контролируется внешним ключом: S5ObjectEntity -> S5ClassEntity
  }

  @Override
  public void afterDeleteClass( IDpuSdClassInfo aClassInfo ) {
    // Целостность контролируется внешним ключом: S5ObjectEntity -> S5ClassEntity
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
