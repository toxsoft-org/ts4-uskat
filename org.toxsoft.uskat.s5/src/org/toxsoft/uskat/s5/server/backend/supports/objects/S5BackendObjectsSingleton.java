package org.toxsoft.uskat.s5.server.backend.supports.objects;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.IS5ObjectsInterceptor.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectReflectUtils.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectsSQL.*;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.events.IS5BackendEventSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;

import ru.uskat.backend.messages.SkMessageWhenObjectsChanged;
import ru.uskat.common.dpu.IDpuObject;
import ru.uskat.core.api.sysdescr.ISkClassInfo;
import ru.uskat.core.common.helpers.sysdescr.ISkSysdescrReader;

/**
 * Реализация {@link IS5BackendObjectsSingleton}.
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    BACKEND_SYSDESCR_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendObjectsSingleton
    extends S5BackendSupportSingleton
    implements IS5BackendObjectsSingleton {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_OBJECTS_ID = "S5BackendObjectsSingleton"; //$NON-NLS-1$

  /**
   * Менеджер постоянства
   */
  @PersistenceContext
  private EntityManager entityManager;

  /**
   * База данных
   */
  @Resource
  private DataSource dataSource;

  /**
   * backend управления классами системы
   */
  @EJB
  private IS5BackendSysDescrSingleton sysdescrBackend;

  /**
   * backend событий
   */
  @EJB
  private IS5BackendEventSingleton eventsBackend;

  /**
   * Читатель системного описания
   */
  private ISkSysdescrReader sysdescrReader;

  /**
   * Интерспетор операций проводимых над классами
   */
  private S5SysdescrInterceptor classesInterceptor;

  /**
   * Поддержка интерсепторов операций проводимых над объектами
   */
  private final S5InterceptorSupport<IS5ObjectsInterceptor> interceptors = new S5InterceptorSupport<>();

  /**
   * Конструктор.
   */
  public S5BackendObjectsSingleton() {
    super( BACKEND_OBJECTS_ID, STR_D_BACKEND_OBJECTS );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendSupportSingleton
  //
  @Override
  protected void doInitSupport() {
    sysdescrReader = sysdescrBackend.getReader();
    IS5BackendObjectsSingleton business = sessionContext().getBusinessObject( IS5BackendObjectsSingleton.class );
    classesInterceptor = new S5SysdescrInterceptor( transactionManager(), sysdescrReader, business );
    sysdescrBackend.addClassInterceptor( classesInterceptor, 1 );
  }

  @Override
  protected void doCloseSupport() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5BackendObjectsSingleton
  //
  @Override
  public void addObjectsInterceptor( IS5ObjectsInterceptor aInterceptor, int aPriority ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.add( aInterceptor, aPriority );
  }

  @Override
  public void removeObjectsInterceptor( IS5ObjectsInterceptor aInterceptor ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    interceptors.remove( aInterceptor );
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ISkBackendObjectsManagement
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IDpuObject findObject( Skid aSkid ) {
    TsNullArgumentRtException.checkNull( aSkid );

    // Пред-интерсепция
    IDpuObject retValue = callBeforeFindObject( interceptors, aSkid );

    if( retValue == null ) {
      IList<IDpuObject> objs = readObjectsByIds( new SkidList( aSkid ) );
      if( objs.size() > 0 ) {
        TsInternalErrorRtException.checkTrue( objs.size() > 1 );
        // Если объект найден в локальном хранилище, то он заменяет ранее найденные
        retValue = objs.get( 0 );
      }
    }

    // Пост-интерсепция
    retValue = callAfterFindObject( interceptors, aSkid, retValue );

    return retValue;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IList<IDpuObject> readObjects( IStringList aClassIds ) {
    TsNullArgumentRtException.checkNull( aClassIds );
    // Результат
    IListEdit<IDpuObject> retValue = new ElemLinkedList<>();

    // Пред-интерсепция
    callBeforeReadObjects( interceptors, aClassIds, retValue );

    try( Connection dbConnection = dataSource.getConnection() ) {
      // Все объекты найденные в локальном хранилище добавляются к ранее найденым
      retValue.addAll( loadByClasses( dbConnection, sysdescrReader, aClassIds ) );
    }
    catch( SQLException e ) {
      // Неожиданная ошибка чтения объектов из базы данных
      throw new TsInternalErrorRtException( e, ERR_READ_UNEXPECTED, cause( e ) );
    }
    // Пост-интерсепция
    callAfterReadObjects( interceptors, aClassIds, retValue );

    return retValue;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IList<IDpuObject> readObjectsByIds( ISkidList aSkids ) {
    TsNullArgumentRtException.checkNull( aSkids );
    // Результат
    IListEdit<IDpuObject> retValue = new ElemLinkedList<>();

    // Пред-интерсепция
    callBeforeReadObjectsByIds( interceptors, aSkids, retValue );

    try( Connection dbConnection = dataSource.getConnection() ) {
      // Все объекты найденные в локальном хранилище добавляются к ранее найденым
      retValue.addAll( loadBySkids( dbConnection, sysdescrReader, aSkids ) );
    }
    catch( SQLException e ) {
      // Неожиданная ошибка чтения объектов из базы данных
      throw new TsInternalErrorRtException( e, ERR_READ_UNEXPECTED, cause( e ) );
    }
    // Пост-интерсепция
    callAfterReadObjectsByIds( interceptors, aSkids, retValue );

    return retValue;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void writeObjects( IS5FrontendRear aFrontend, ISkidList aRemovedSkids, IList<IDpuObject> aObjects,
      boolean aInterceptable ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aRemovedSkids, aObjects );
    // Время начала выполнения запроса
    long currTime = System.currentTimeMillis();
    // Карта описаний классов по их идентификаторам
    IStringMapEdit<ISkClassInfo> classesByIds = new StringMap<>();
    // Карта классов реализаций объектов по идентификаторам их классов
    IStringMapEdit<Class<S5ObjectEntity>> implByIds = new StringMap<>();
    // Карта удаляемых объектов по классам
    IMap<ISkClassInfo, IList<IDpuObject>> removedObjs = loadObjectsBySkids( aRemovedSkids, classesByIds, implByIds );
    // Карта обновляемых объектов по классам и его readonly-вариант
    IMapEdit<ISkClassInfo, IListEdit<Pair<IDpuObject, IDpuObject>>> updatedObjsEdit = new ElemMap<>();
    IMap<ISkClassInfo, IList<Pair<IDpuObject, IDpuObject>>> updatedObjs =
        (IMap<ISkClassInfo, IList<Pair<IDpuObject, IDpuObject>>>)(Object)updatedObjsEdit;
    // Карта создаваемых объектов по классам и его readonly-вариант
    IMapEdit<ISkClassInfo, IListEdit<IDpuObject>> createdObjsEdit = new ElemMap<>();
    IMap<ISkClassInfo, IList<IDpuObject>> createdObjs = (IMap<ISkClassInfo, IList<IDpuObject>>)(Object)createdObjsEdit;

    // Анализ существования объектов
    loadObjectsByDpu( aObjects, classesByIds, implByIds, updatedObjsEdit, createdObjsEdit );
    // Время загрузки текущего состояния объектов
    long loadTimestamp = System.currentTimeMillis();

    if( aInterceptable ) {
      // Пред-интерсепция
      callBeforeWriteObjectsInterceptors( interceptors, removedObjs, updatedObjs, createdObjs );
    }
    long interceptorTimestamp1 = System.currentTimeMillis();

    // Список объектов изменивших атрибуты или удаленных из системы
    IListEdit<Skid> changedObjectIds = new SkidList();
    // Удаление объектов
    int removeCount = 0;
    for( IList<IDpuObject> classRemovedObjs : removedObjs.values() ) {
      for( IDpuObject removedObj : classRemovedObjs ) {
        changedObjectIds.add( removedObj.skid() );
        entityManager.remove( removedObj );
        removeCount++;
      }
    }
    // Обновление объектов
    int updateCount = 0;
    for( IList<Pair<IDpuObject, IDpuObject>> objs : updatedObjs.values() ) {
      for( Pair<IDpuObject, IDpuObject> obj : objs ) {
        changedObjectIds.add( obj.right().skid() );
        // 2020-07-23 mvk
        // entityManager.merge( obj.right() );
        ((S5ObjectEntity)entityManager.merge( obj.right() )).setAttrs( obj.right().attrs() );
        // TODO: mvkd experimental
        // updateObject( entityManager, obj.right() );
        updateCount++;
      }
    }
    // Создание объектов
    int createCount = 0;
    for( IList<IDpuObject> objs : createdObjs.values() ) {
      for( IDpuObject obj : objs ) {
        entityManager.persist( obj );
        // TODO: mvkd experimental
        // createObject( entityManager, obj );
        createCount++;
      }
    }
    long entityManagerTimestamp1 = System.currentTimeMillis();
    // Синхронизация с базой данных
    entityManager.flush();
    long entityManagerTimestamp2 = System.currentTimeMillis();

    if( aInterceptable ) {
      // Пост-интерсепция
      callAfterWriteObjectsInterceptors( interceptors, removedObjs, updatedObjs, createdObjs );
    }
    long interceptorTimestamp2 = System.currentTimeMillis();

    // Формирование события
    if( aInterceptable ) {
      // TODO: сформировать событие
      // events.add( createEvent( currTime, removedByClasses ) );
      // eventsBackend.writeEvents( events );
    }
    long eventTimestamp = System.currentTimeMillis();

    // Запись в журнал
    Integer rc = Integer.valueOf( removeCount );
    Integer uc = Integer.valueOf( updateCount );
    Integer cc = Integer.valueOf( createCount );
    Long at = Long.valueOf( eventTimestamp - currTime );
    Long lt = Long.valueOf( loadTimestamp - currTime );
    Long it1 = Long.valueOf( interceptorTimestamp1 - loadTimestamp );
    Long et1 = Long.valueOf( entityManagerTimestamp1 - interceptorTimestamp1 );
    Long et2 = Long.valueOf( entityManagerTimestamp2 - entityManagerTimestamp1 );
    Long it2 = Long.valueOf( interceptorTimestamp2 - entityManagerTimestamp2 );
    Long et = Long.valueOf( eventTimestamp - interceptorTimestamp2 );
    if( changedObjectIds.size() > 0 ) {
      // Отправление события для frontend
      fireWhenObjectsChanged( aFrontend, backend().attachedFrontends(), new SkidList( changedObjectIds ) );
    }
    logger().info( MSG_WRITE_OBJECTES, rc, uc, cc, at, lt, it1, et1, et2, it2, et );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Возвращает список объектов которые есть в базе данных
   *
   * @param aSkids {@link ISkidList} список загружаемых объектов
   * @param aClassInfosByIds {@link IStringMapEdit}&lt;{@link ISkClassInfo}&gt; карта описаний классов по их
   *          идентификаторам. <br>
   *          Ключ: идентификатор класса;<br>
   *          Значение: описание класса.
   * @param aImplByIds {@link IStringMapEdit}&lt;Class&gt; карта классов реализаций объектов по описаниям классов. <br>
   *          Ключ: описание класса;<br>
   *          Значение: Класс реализации объекта.
   * @return {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link IDpuObject}&gt;&gt; карта объектов наденных в
   *         базе данных.<br>
   *         Ключ: Описание классов;<br>
   *         Значение: Список объектов.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  private IMap<ISkClassInfo, IList<IDpuObject>> loadObjectsBySkids( ISkidList aSkids,
      IStringMapEdit<ISkClassInfo> aClassInfosByIds, IStringMapEdit<Class<S5ObjectEntity>> aImplByIds ) {
    TsNullArgumentRtException.checkNulls( aSkids, aClassInfosByIds, aImplByIds );
    // Формирование карты найденных объектов. Ключ: описание класса; Значение: список объектов класса
    IMapEdit<ISkClassInfo, IListEdit<IDpuObject>> retValue = new ElemMap<>();
    for( Skid skid : aSkids ) {
      String classId = skid.classId();
      ISkClassInfo classInfo = aClassInfosByIds.findByKey( classId );
      if( classInfo == null ) {
        classInfo = sysdescrReader.findClassInfo( classId );
        if( classInfo == null ) {
          // Класса (и объекта) нет в системе
          continue;
        }
        aClassInfosByIds.put( classId, classInfo );
      }
      // Класс реализации объекта
      Class<S5ObjectEntity> objImplClass = getObjectImplClass( classInfo, aImplByIds );
      // Поиск объекта
      IDpuObject obj = entityManager.find( objImplClass, new S5ObjectID( skid ) );
      if( obj == null ) {
        // Объект не найден
        continue;
      }
      IListEdit<IDpuObject> objs = retValue.findByKey( classInfo );
      if( objs == null ) {
        objs = new ElemArrayList<>( aSkids.size() );
        retValue.put( classInfo, objs );
      }
      objs.add( obj );
    }
    return (IMap<ISkClassInfo, IList<IDpuObject>>)(Object)retValue;
  }

  /**
   * Возвращает список объектов которые есть в базе данных и которых нет
   *
   * @param aObjects {@link IList}&lt;{@link IDpuObject}&gt; проверяемый список объектов
   * @param aClassesByIds {@link IStringMapEdit}&lt;{@link ISkClassInfo}&gt; карта описаний классов по их
   *          идентификаторам. <br>
   *          Ключ: идентификатор класса;<br>
   *          Значение: описание класса.
   * @param aImplByIds {@link IStringMapEdit}&lt;Class&gt; карта классов реализаций объектов по описаниям классов. <br>
   *          Ключ: описание класса;<br>
   *          Значение: Класс реализации объекта.
   * @param aUpdatedObjs
   *          {@link IMap}&lt;{@link ISkClassInfo},{@link IListEdit}&lt;{@link Pair}&lt;{@link IDpuObject},{@link IDpuObject}&gt;&gt;&gt;
   *          карта объектов обновляемых в базе данных.<br>
   *          Ключ: Описание классов;<br>
   *          Значение: Список пар: {@link Pair#left()} - старое состояние объекта, {@link Pair#right()} - новое.
   * @param aCreatedObjs {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link IDpuObject}&gt;&gt; карта объектов
   *          создаваемых в базе данных.<br>
   *          Ключ: Описание классов;<br>
   *          Значение: Список объектов.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private void loadObjectsByDpu( IList<IDpuObject> aObjects, IStringMapEdit<ISkClassInfo> aClassesByIds,
      IStringMapEdit<Class<S5ObjectEntity>> aImplByIds,
      IMapEdit<ISkClassInfo, IListEdit<Pair<IDpuObject, IDpuObject>>> aUpdatedObjs,
      IMapEdit<ISkClassInfo, IListEdit<IDpuObject>> aCreatedObjs ) {
    TsNullArgumentRtException.checkNulls( aObjects, aClassesByIds, aImplByIds, aCreatedObjs, aUpdatedObjs );
    // Карта конструкторов объектов. Ключ: идентификатор класса; Значение: конструктор
    IStringMapEdit<Constructor<S5ObjectEntity>> objectContructors = new StringMap<>();
    for( IDpuObject obj : aObjects ) {
      String classId = obj.classId();
      ISkClassInfo classInfo = aClassesByIds.findByKey( classId );
      if( classInfo == null ) {
        classInfo = sysdescrReader.getClassInfo( classId );
        aClassesByIds.put( classId, classInfo );
      }
      // Класс реализации объекта
      Class<S5ObjectEntity> objImplClass = getObjectImplClass( classInfo, aImplByIds );
      // Создание нового объекта
      IDpuObject newObj = obj;
      // Если объект не может быть маппирован на базу данных, то создаем копию объекта
      if( newObj.getClass() != objImplClass ) {
        // Конструктор объекта
        Constructor<S5ObjectEntity> objectConstructor = objectContructors.findByKey( classId );
        if( objectConstructor == null ) {
          // Конструктор еще неопределен
          objectConstructor = getConstructorBySource( objImplClass );
          objectContructors.put( classId, objectConstructor );
        }
        // Создание копии
        newObj = createObjectEntity( objectConstructor, obj );
      }
      // Поиск существующего объекта
      IDpuObject prevObj = entityManager.find( objImplClass, new S5ObjectID( obj.skid() ) );
      if( prevObj == null ) {
        // Объект не найден значит он создается
        IListEdit<IDpuObject> objs = aCreatedObjs.findByKey( classInfo );
        if( objs == null ) {
          objs = new ElemArrayList<>( aObjects.size() );
          aCreatedObjs.put( classInfo, objs );
        }
        objs.add( newObj );
        continue;
      }
      IListEdit<Pair<IDpuObject, IDpuObject>> objs = aUpdatedObjs.findByKey( classInfo );
      if( objs == null ) {
        objs = new ElemArrayList<>( aObjects.size() );
        aUpdatedObjs.put( classInfo, objs );
      }
      objs.add( new Pair<>( prevObj, newObj ) );
    }
  }

  /**
   * Формирование события: произошло изменение объектов системы
   *
   * @param aFrontend {@link IS5FrontendRear} frontend выполняющий операцию по изменению объектов системы
   * @param aFrontends {@link IS5FrontendRear} список фронтендов подключенных к бекенду
   * @param aObjectIds {@link ISkidList} идентификаторы объектов изменивших свои атрибуты или удаленных из системы.
   *          {@link ISkidList#EMPTY} все объекты системы
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static void fireWhenObjectsChanged( IS5FrontendRear aFrontend, IList<IS5FrontendRear> aFrontends,
      ISkidList aObjectIds ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aFrontends, aObjectIds );
    for( IS5FrontendRear frontend : aFrontends ) {
      // 2022-04-11 mvk не всегда frontend может отслеживать свои изменения объектов (или это получается криво как при
      // использовании ISkBatchOperationService), поэтому извещение производится для всех frontend
      // if( aFrontend == frontend ) {
      // В свой frontend событие не отправляется (frontend сам отслеживает свои изменения)
      // continue;
      // }
      SkMessageWhenObjectsChanged.send( frontend, aObjectIds );
    }
  }

}
