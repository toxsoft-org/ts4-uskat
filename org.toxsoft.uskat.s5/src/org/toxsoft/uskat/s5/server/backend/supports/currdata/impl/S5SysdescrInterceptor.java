package org.toxsoft.uskat.s5.server.backend.supports.currdata.impl;

import static java.lang.String.*;
import static org.toxsoft.core.tslib.gw.gwid.Gwid.*;
import static org.toxsoft.uskat.s5.server.backend.supports.currdata.impl.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5BackendObjectsUtils.*;
import static org.toxsoft.uskat.s5.server.transactions.ES5TransactionResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.*;
import org.toxsoft.uskat.s5.server.backend.supports.objects.*;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.*;
import org.toxsoft.uskat.s5.server.transactions.*;

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
    implements IS5ClassesInterceptor {

  private final IS5TransactionManagerSingleton txManager;
  private final IS5BackendObjectsSingleton     objectsBackend;
  private final IS5BackendCurrDataSingleton    currdataBackend;

  /**
   * Конструктор
   *
   * @param aTransactionManager {@link IS5TransactionManagerSingleton} менеджер транзакций
   * @param aObjectsBackend {@link IS5BackendObjectsSingleton} менеджер управления объектами
   * @param aCurrdataBackend {@link IS5BackendCurrDataSingleton} backend управления текущими данными системы
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5SysdescrInterceptor( IS5TransactionManagerSingleton aTransactionManager, IS5BackendObjectsSingleton aObjectsBackend,
      IS5BackendCurrDataSingleton aCurrdataBackend ) {
    TsNullArgumentRtException.checkNulls( aTransactionManager, aObjectsBackend, aCurrdataBackend );
    txManager = aTransactionManager;
    objectsBackend = aObjectsBackend;
    currdataBackend = aCurrdataBackend;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5ClassesInterceptor
  //
  @Override
  public void beforeCreateClass( IDtoClassInfo aClassInfo ) {
    // nop
  }

  @Override
  public void afterCreateClass( IDtoClassInfo aClassInfo ) {
    // nop
  }

  @Override
  public void beforeUpdateClass( IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo,
      IStridablesList<IDtoClassInfo> aDescendants ) {
    // Идентификатор изменяемого класса
    String classId = aNewClassInfo.id();
    // Список удаленных текущих данных
    IStridablesListEdit<IDtoRtdataInfo> removedRtdata = new StridablesList<>();
    // Список добавленных текущих данных
    IStridablesListEdit<IDtoRtdataInfo> addedRtdata = new StridablesList<>();
    // Анализ для формирования списка добавленных и удаленных данных
    loadSysdescrChangedProps( aPrevClassInfo.rtdataInfos(), aNewClassInfo.rtdataInfos(), removedRtdata, addedRtdata );
    // Фильтрация текущих данных
    for( IDtoRtdataInfo info : new StridablesList<>( removedRtdata ) ) {
      if( !info.isCurr() ) {
        removedRtdata.remove( info );
      }
    }
    for( IDtoRtdataInfo info : new StridablesList<>( addedRtdata ) ) {
      if( !info.isCurr() ) {
        addedRtdata.remove( info );
      }
    }
    // Проверка есть ли добавленные или удаленные текущие данные. Если нет, то проверка не требуется
    if( removedRtdata.size() == 0 && addedRtdata.size() == 0 ) {
      return;
    }
    // Список объектов изменяющих состав текущих данных
    IList<IDtoObject> objs = S5TransactionUtils.txUpdatedClassObjs( txManager, objectsBackend, classId, aDescendants );
    if( objs.size() == 0 ) {
      // Нет объектов
      return;
    }
    // Если есть объекты, то все вновь добавляемые текущие данные ОБЯЗАНЫ иметь значение по умолчанию
    StringBuilder sbError = new StringBuilder();
    for( IDtoRtdataInfo rtdataInfo : addedRtdata ) {
      IDataType type = rtdataInfo.dataType();
      IAtomicValue defaultValue = findTypeDefaultValue( type );
      if( defaultValue == null ) {
        // Текущее данное имеет тип у которого нет значения по умолчанию
        sbError.append( format( ERR_CURRDATA_NOT_HAVE_DEFAULT_VALUE, rtdataInfo.id(), type ) );
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
  public void afterUpdateClass( IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo,
      IStridablesList<IDtoClassInfo> aDescendants ) {
    // Текущая транзакция
    IS5Transaction tx = txManager.getTransaction();
    // Список описаний удаляемых текущих данных
    IStridablesList<IDtoRtdataInfo> removedCurrdata = tx.findResource( TX_REMOVED_CURRDATA );
    // Список описаний добавляемых текущих данных
    IStridablesList<IDtoRtdataInfo> addedCurrdata = tx.findResource( TX_ADDED_CURRDATA );
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
        for( IDtoRtdataInfo rtdataInfo : removedCurrdata ) {
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
        for( IDtoRtdataInfo rtdataInfo : addedCurrdata ) {
          IDataType type = rtdataInfo.dataType();
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
  public void beforeDeleteClass( IDtoClassInfo aClassInfo ) {
    // Целостность контролируется внешним ключом: S5ObjectEntity -> S5ClassEntity
  }

  @Override
  public void afterDeleteClass( IDtoClassInfo aClassInfo ) {
    // Целостность контролируется внешним ключом: S5ObjectEntity -> S5ClassEntity
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Формирует список добавленных и удаленных элементов класса (атрибуты, данные, связи, события, команды) из двух
   * представленных списков - старая и новая редакция
   *
   * @param <T> тип элемента
   * @param aPrevPropInfos {@link IStridablesList}&lt;{@link IDtoClassPropInfoBase}&gt; список элементов (старая
   *          редакция)
   * @param aNewPropInfos {@link IStridablesList}&lt;{@link IDtoClassPropInfoBase}&gt; список элементов (новая редакция)
   * @param aRemovedProps {@link IStridablesListEdit}&lt;{@link IDtoClassPropInfoBase}&gt; список удаленных элементов
   * @param aAddedProps {@link IStridablesListEdit}&lt;{@link IDtoClassPropInfoBase}&gt; список добавленных элементов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static <T extends IDtoRtdataInfo> void loadSysdescrChangedProps( IStridablesList<T> aPrevPropInfos,
      IStridablesList<T> aNewPropInfos, IStridablesListEdit<T> aRemovedProps, IStridablesListEdit<T> aAddedProps ) {
    TsNullArgumentRtException.checkNulls( aPrevPropInfos, aNewPropInfos, aRemovedProps, aAddedProps );
    for( T prevPropInfo : aPrevPropInfos ) {
      T newPropInfo = aNewPropInfos.findByKey( prevPropInfo.id() );
      if( newPropInfo == null || //
          !newPropInfo.isCurr() || //
          newPropInfo.dataType().atomicType() != prevPropInfo.dataType().atomicType() ) {
        aRemovedProps.put( prevPropInfo );
      }
    }
    for( T newPropInfo : aNewPropInfos ) {
      if( !newPropInfo.isCurr() ) {
        continue;
      }
      T prevPropInfo = aPrevPropInfos.findByKey( newPropInfo.id() );
      if( prevPropInfo == null || //
          newPropInfo.dataType().atomicType() != prevPropInfo.dataType().atomicType() ) {
        aAddedProps.put( newPropInfo );
      }
    }
  }
}
