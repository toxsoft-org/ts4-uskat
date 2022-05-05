package org.toxsoft.uskat.s5.server.backend.supports.objects;

import static java.lang.String.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5BackendObjectsUtils.*;
import static org.toxsoft.uskat.s5.server.transactions.ES5TransactionResources.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringListEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringArrayList;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.SkidList;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5ClassesInterceptor;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5TypesInterceptor;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.transactions.*;

import ru.uskat.common.dpu.*;
import ru.uskat.common.dpu.impl.DpuObject;
import ru.uskat.core.common.helpers.sysdescr.ISkSysdescrReader;

/**
 * Интерсептор системного описания используемый {@link S5BackendObjectsSingleton}
 * <p>
 * Решаемые задачи:
 * <ul>
 * <li>Отслеживание изменения типа атрибутов объектов. Установка атрибута в значение по умолчанию для нового типа;</li>
 * <li>Отслеживание изменения класса реализации объекта. Перемещение объектов из одной таблицы в другую;</li>
 * <li>Отслеживание изменения класса объекта. Добавление и/или удаление атрибутов;</li>
 * </ul>
 *
 * @author mvk
 */
class S5SysdescrInterceptor
    implements IS5TypesInterceptor, IS5ClassesInterceptor {

  private final IS5TransactionManagerSingleton txManager;
  private final ISkSysdescrReader              sysdescrReader;
  private final IS5BackendObjectsSingleton     objectsBackend;

  /**
   * Конструктор
   *
   * @param aTransactionManager {@link IS5TransactionManagerSingleton} менеджер транзакций
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель системного описания
   * @param aObjectsBackend {@link IS5BackendObjectsSingleton} backend управления объектами системы
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5SysdescrInterceptor( IS5TransactionManagerSingleton aTransactionManager, ISkSysdescrReader aSysdescrReader,
      IS5BackendObjectsSingleton aObjectsBackend ) {
    txManager = TsNullArgumentRtException.checkNull( aTransactionManager );
    sysdescrReader = TsNullArgumentRtException.checkNull( aSysdescrReader );
    objectsBackend = TsNullArgumentRtException.checkNull( aObjectsBackend );
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
    // Список идентификаторов классов объекты которых могут изменить тип атрибутов
    IStringListEdit classIds = new StringArrayList( aDependentClasses.keys().size() );
    for( String classId : aDependentClasses.keys() ) {
      IDpuSdClassInfo classInfo = aDependentClasses.getByKey( classId );
      for( IDpuSdAttrInfo attrInfo : classInfo.attrInfos() ) {
        if( attrInfo.typeId().equals( aPrevTypeInfo.id() ) ) {
          // Объекты класса будут изменять тип атрибута
          classIds.add( classId );
          break;
        }
      }
    }
    // Список объектов непозволяющих поменять тип атрибута
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
    // Сохраняем в транзакции список объектов менящих тип атрибутов
    TsIllegalStateRtException.checkNoNull( tx.putResource( TX_UPDATED_OBJS_BY_ATTR_TYPE, objs ) );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public void afterUpdateType( IDpuSdTypeInfo aPrevTypeInfo, IDpuSdTypeInfo aNewTypeInfo,
      IStridablesList<IDpuSdClassInfo> aDependentClasses ) {
    String typeId = aPrevTypeInfo.id();
    // Текущая транзакция
    IS5Transaction tx = txManager.getTransaction();
    // Список объектов меняющих тип атрибутов
    IList<S5ObjectEntity> objs = tx.findResource( TX_UPDATED_OBJS_BY_ATTR_TYPE );
    if( objs != null ) {
      for( S5ObjectEntity obj : objs ) {
        IDpuSdClassInfo classInfo = aDependentClasses.getByKey( obj.classId() );
        IOptionSetEdit attrsValues = new OptionSet( obj.attrs() );
        for( IDpuSdAttrInfo attrInfo : classInfo.attrInfos() ) {
          if( !attrInfo.typeId().equals( typeId ) ) {
            continue;
          }
          // Сброс значения текущего данного в значение по умолчанию
          attrsValues.remove( attrInfo.id() );
        }
        obj.setAttrs( attrsValues );
      }
      // Сохранение объектов в базе данных. false: перехват запрещен
      objectsBackend.writeObjects( IS5FrontendRear.NULL, ISkidList.EMPTY, (IList<IDpuObject>)(Object)objs, false );
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
    // Подготовка транзакции к перемещению реализации если необходимо
    boolean isMovingImpl = prepareTransactionMoveObjectImpl( aPrevClassInfo, aNewClassInfo );
    // Список удаленных атрибутов
    IStridablesListEdit<IDpuSdAttrInfo> removedAttrs = new StridablesList<>();
    // Список добавленных атрибутов
    IStridablesListEdit<IDpuSdAttrInfo> addedAttrs = new StridablesList<>();
    // Анализ для формирования списка добавленных и удаленных атрибутов
    loadSysdescrChangedProps( aPrevClassInfo.attrInfos(), aNewClassInfo.attrInfos(), removedAttrs, addedAttrs );
    // Проверка есть ли добавленные или удаленные атрибуты. Если нет, то проверка не требуется
    if( removedAttrs.size() == 0 && addedAttrs.size() == 0 ) {
      return;
    }
    if( isMovingImpl ) {
      // Запрет одновременного изменения реализации хранения объектов и атрибутов класса
      throw new TsIllegalStateRtException( ERR_CANT_CHANGE_IMPL_AND_ATTRS, classId );
    }
    // Список объектов изменяющих хранение
    IList<IDpuObject> objs = S5TransactionUtils.txUpdatedClassObjs( txManager, objectsBackend, classId, aDescendants );
    if( objs.size() == 0 ) {
      // Нет объектов
      return;
    }
    // Если есть объекты, то все вновь добавляемые атрибуты ОБЯЗАНЫ иметь значение по умолчанию
    StringBuilder sbError = new StringBuilder();
    for( IDpuSdAttrInfo attrInfo : addedAttrs ) {
      IDataDef type = sysdescrReader.findType( attrInfo.typeId() );
      IAtomicValue defaultValue = findTypeDefaultValue( type );
      if( defaultValue == null ) {
        // Атрибут имеет тип у которого нет значения по умолчанию
        sbError.append( format( ERR_ATTR_NOT_HAVE_DEFAULT_VALUE, attrInfo.id(), type.id() ) );
      }
    }
    if( sbError.length() > 0 ) {
      // Запрет изменения класса имеющий объекты
      throw new TsIllegalStateRtException( ERR_CANT_CHANGE_CLASS_WITH_OBJS, classId, sbError.toString() );
    }
    // Текущая транзакция
    IS5Transaction tx = txManager.getTransaction();
    // Сохраняем в транзакции список добавляемых и удаляемых атрибутов
    TsIllegalStateRtException.checkNoNull( tx.putResource( TX_REMOVED_ATTRS, removedAttrs ) );
    TsIllegalStateRtException.checkNoNull( tx.putResource( TX_ADDED_ATTRS, addedAttrs ) );
  }

  @Override
  @SuppressWarnings( "unchecked" )
  public void afterUpdateClass( IDpuSdClassInfo aPrevClassInfo, IDpuSdClassInfo aNewClassInfo,
      IStridablesList<IDpuSdClassInfo> aDescendants ) {
    // Текущая транзакция
    IS5Transaction tx = txManager.getTransaction();
    // Список объектов перемещаемых в новую таблицу (реализацию)
    IList<IDpuObject> movingImplObjs = tx.findResource( TX_UPDATED_OBJS_BY_CHANGE_IMPL );
    if( movingImplObjs != null ) {
      // Есть объекты для перемещения. Необходимо просто провести запись, так как описание класса уже было изменено
      // false: перехват запрещен
      objectsBackend.writeObjects( IS5FrontendRear.NULL, ISkidList.EMPTY, movingImplObjs, false );
    }
    // Список объектов на которые влияет изменение класса (добавление/удаление атрибутов)
    IStridablesList<IDpuSdAttrInfo> removedAttrs = tx.findResource( TX_REMOVED_ATTRS );
    if( removedAttrs != null ) {
      IList<S5ObjectEntity> objs = tx.getResource( TX_UPDATED_CLASS_OBJS );
      for( S5ObjectEntity obj : objs ) {
        IOptionSetEdit attrsValues = new OptionSet( obj.attrs() );
        for( IDpuSdAttrInfo attrInfo : removedAttrs ) {
          attrsValues.remove( attrInfo.id() );
        }
        // 2019-10-17: По принятом в ISkConnection правилу - если значение по умолчанию, то оно не хранится
        // for( IDpuSdAttrInfo attrInfo : addedAttrs ) {
        // IStridableDataDef type = typesManager.findType( attrInfo.typeId() );
        // IAtomicValue defaultValue = type.defaultValueOrNull();
        // attrsValues.setValobj( attrInfo.id(), defaultValue );
        // }
      }
      // false: перехват запрещен
      objectsBackend.writeObjects( IS5FrontendRear.NULL, ISkidList.EMPTY, (IList<IDpuObject>)(Object)objs, false );
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
  /**
   * Подготавливает транзакцию к перемещению реализации объектов из одной таблицы базы данных в другую
   *
   * @param aPrevClassInfo {@link IDpuSdClassInfo} описание класса (старая редакция)
   * @param aNewClassInfo {@link IDpuSdClassInfo} описание класса (новая редакция)
   * @return boolean <b>true</b> запущен процесс замены реализации объектов;<b>false</b> нет перемещения реализации
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private boolean prepareTransactionMoveObjectImpl( IDpuSdClassInfo aPrevClassInfo, IDpuSdClassInfo aNewClassInfo ) {
    TsNullArgumentRtException.checkNulls( aPrevClassInfo, aNewClassInfo );
    // Класс реализации хранения значений объекта
    String prevObjectImplClassName = DDEF_OBJECT_IMPL_CLASS.getValue( aPrevClassInfo.params() ).asString();
    String newObjectImplClassName = DDEF_OBJECT_IMPL_CLASS.getValue( aNewClassInfo.params() ).asString();
    // Признак изменения класса реализации объекта (перемещаться могут только объекты измененного класса без наследников
    // - у наследников собственное определение)
    boolean changeObjectImplClass = !prevObjectImplClassName.equals( newObjectImplClassName );
    if( !changeObjectImplClass ) {
      // Реализация не изменилась
      return false;
    }
    // Список перемещаемых объектов
    IList<IDpuObject> objs = objectsBackend.readObjects( new StringArrayList( aPrevClassInfo.id() ) );
    if( objs.size() == 0 ) {
      // Реализация изменилась, но нет объектов изменяющих реализацию
      return false;
    }
    // Список идентификаторов объектов
    SkidList movingImplSkids = new SkidList();
    // Составляем список объектов "оторванных" от реализации
    IListEdit<IDpuObject> movingImplObjs = new ElemArrayList<>( objs.size() );
    for( IDpuObject obj : objs ) {
      movingImplSkids.add( obj.skid() );
      movingImplObjs.add( new DpuObject( obj.skid(), obj.attrs() ) );
    }
    // Текущая транзакция
    IS5Transaction tx = txManager.getTransaction();
    // Сохраняем в транзакции список объектов перемещаемых в новую таблицу (реализацию)
    TsIllegalStateRtException.checkNoNull( tx.putResource( TX_UPDATED_OBJS_BY_CHANGE_IMPL, objs ) );
    try {
      // Удаление объектов из старой таблицы. Если транзакция не завершится, то удаление будет отменено.
      // false: перехват запрещен
      objectsBackend.writeObjects( IS5FrontendRear.NULL, movingImplSkids, IList.EMPTY, false );
    }
    catch( Throwable e ) {
      // Ошибка перемещения объектов из одной таблицы реализации в другую
      String classId = aNewClassInfo.id();
      throw new TsInternalErrorRtException( e, ERR_CHANGE_OBJECT_IMPL, classId, prevObjectImplClassName,
          newObjectImplClassName, cause( e ) );
    }
    return true;
  }
}
