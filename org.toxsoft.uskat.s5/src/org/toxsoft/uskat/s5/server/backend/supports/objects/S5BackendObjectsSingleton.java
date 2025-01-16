package org.toxsoft.uskat.s5.server.backend.supports.objects;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.IS5ObjectsInterceptor.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectReflectUtils.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5ObjectsSQL.*;

import java.lang.reflect.*;
import java.sql.*;
import java.util.concurrent.*;

import javax.annotation.*;
import javax.ejb.*;
import javax.persistence.*;
import javax.sql.*;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.common.sysdescr.*;
import org.toxsoft.uskat.s5.server.backend.impl.*;
import org.toxsoft.uskat.s5.server.backend.supports.events.*;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.*;
import org.toxsoft.uskat.s5.server.frontend.*;
import org.toxsoft.uskat.s5.server.interceptors.*;

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
    classesInterceptor = new S5SysdescrInterceptor( transactionManager(), business );
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
  public IDtoObject findObject( Skid aSkid ) {
    TsNullArgumentRtException.checkNull( aSkid );

    // Пред-интерсепция
    IDtoObject retValue = callBeforeFindObject( interceptors, aSkid );

    if( retValue == null ) {
      IList<IDtoObject> objs = readObjectsByIds( new SkidList( aSkid ) );
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
  public IList<IDtoObject> readObjects( IStringList aClassIds ) {
    TsNullArgumentRtException.checkNull( aClassIds );
    // Результат
    IListEdit<IDtoObject> retValue = new ElemLinkedList<>();

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

    // Формирование результата, чтобы избежать ошибок маршалинга ElemLinkedList на больших коллекциях
    return new ElemArrayList<>( retValue );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IList<IDtoObject> readObjectsByIds( ISkidList aSkids ) {
    TsNullArgumentRtException.checkNull( aSkids );
    // Результат
    IListEdit<IDtoObject> retValue = new ElemLinkedList<>();

    // Пред-интерсепция
    callBeforeReadObjectsByIds( interceptors, aSkids, retValue );

    try( Connection dbConnection = dataSource.getConnection() ) {
      // Все объекты найденные в локальном хранилище добавляются к ранее найденным
      retValue.addAll( loadBySkids( dbConnection, sysdescrReader, aSkids ) );
    }
    catch( SQLException e ) {
      // Неожиданная ошибка чтения объектов из базы данных
      throw new TsInternalErrorRtException( e, ERR_READ_UNEXPECTED, cause( e ) );
    }
    // Пост-интерсепция
    callAfterReadObjectsByIds( interceptors, aSkids, retValue );

    // Формирование результата, чтобы избежать ошибок маршалинга ElemLinkedList на больших коллекциях
    return new ElemArrayList<>( retValue );
  }

  @SuppressWarnings( "unchecked" )
  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void writeObjects( IS5FrontendRear aFrontend, ISkidList aRemovedSkids, IList<IDtoObject> aObjects,
      boolean aInterceptable ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aRemovedSkids, aObjects );
    // Время начала выполнения запроса
    long currTime = System.currentTimeMillis();
    // Карта описаний классов по их идентификаторам
    IStringMapEdit<ISkClassInfo> classesByIds = new StringMap<>();
    // Карта классов реализаций объектов по идентификаторам их классов
    IStringMapEdit<Class<S5ObjectEntity>> implByIds = new StringMap<>();
    // Карта удаляемых объектов по классам
    IMap<ISkClassInfo, IList<IDtoObject>> removedObjs = loadObjectsBySkids( aRemovedSkids, classesByIds, implByIds );
    // Карта обновляемых объектов по классам и его readonly-вариант
    IMapEdit<ISkClassInfo, IListEdit<Pair<IDtoObject, IDtoObject>>> updatedObjsEdit = new ElemMap<>();
    IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> updatedObjs =
        (IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>>)(Object)updatedObjsEdit;
    // Карта создаваемых объектов по классам и его readonly-вариант
    IMapEdit<ISkClassInfo, IListEdit<IDtoObject>> createdObjsEdit = new ElemMap<>();
    IMap<ISkClassInfo, IList<IDtoObject>> createdObjs = (IMap<ISkClassInfo, IList<IDtoObject>>)(Object)createdObjsEdit;

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
    for( IList<IDtoObject> classRemovedObjs : removedObjs.values() ) {
      for( IDtoObject removedObj : classRemovedObjs ) {
        changedObjectIds.add( removedObj.skid() );
        entityManager.remove( removedObj );
        removeCount++;
        logger().debug( "writeObjects(...): removed entity %s, rc = %d", removedObj, //$NON-NLS-1$
            Integer.valueOf( removeCount ) );
      }
    }
    // Обновление объектов
    int updateCount = 0;
    for( IList<Pair<IDtoObject, IDtoObject>> objs : updatedObjs.values() ) {
      for( Pair<IDtoObject, IDtoObject> obj : objs ) {
        changedObjectIds.add( obj.right().skid() );
        // 2020-07-23 mvk
        // entityManager.merge( obj.right() );
        ((S5ObjectEntity)entityManager.merge( obj.right() )).setAttrs( obj.right().attrs() );
        // TODO: mvkd experimental
        // updateObject( entityManager, obj.right() );
        updateCount++;
        logger().debug( "writeObjects(...): merge entity %s, uc = %d", obj, Integer.valueOf( updateCount ) ); //$NON-NLS-1$
      }
    }
    // Создание объектов
    int createCount = 0;
    for( IList<IDtoObject> objs : createdObjs.values() ) {
      for( IDtoObject obj : objs ) {
        entityManager.persist( obj );
        // TODO: mvkd experimental
        // createObject( entityManager, obj );
        createCount++;
        logger().debug( "writeObjects(...): persist entity %s, cc = %d", obj, Integer.valueOf( createCount ) ); //$NON-NLS-1$
      }
    }
    long entityManagerTimestamp1 = System.currentTimeMillis();

    // Счетчики для журнала
    Integer rc = Integer.valueOf( removeCount );
    Integer uc = Integer.valueOf( updateCount );
    Integer cc = Integer.valueOf( createCount );

    // Синхронизация с базой данных
    logger().debug( "writeObjects(...): flush before. rc = %d, uc = %d, cc = %d", rc, uc, cc ); //$NON-NLS-1$
    entityManager.flush();
    logger().debug( "writeObjects(...): flush after. rc = %d, uc = %d, cc = %d, time = %d msec", rc, uc, cc, //$NON-NLS-1$
        Long.valueOf( System.currentTimeMillis() - entityManagerTimestamp1 ) );

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
    Long at = Long.valueOf( eventTimestamp - currTime );
    Long lt = Long.valueOf( loadTimestamp - currTime );
    Long it1 = Long.valueOf( interceptorTimestamp1 - loadTimestamp );
    Long et1 = Long.valueOf( entityManagerTimestamp1 - interceptorTimestamp1 );
    Long et2 = Long.valueOf( entityManagerTimestamp2 - entityManagerTimestamp1 );
    Long it2 = Long.valueOf( interceptorTimestamp2 - entityManagerTimestamp2 );
    Long et = Long.valueOf( eventTimestamp - interceptorTimestamp2 );

    // Признак необходимости оповещения frontend об изменениях
    boolean needFrontendNotify = (removeCount > 0 || createCount > 0 || updateCount > 0);

    if( needFrontendNotify ) {
      IList<IS5FrontendRear> frontends = backend().attachedFrontends();
      if( removeCount == 1 ) {
        // Отправление события об удалении класса для frontend
        fireWhenObjectsChanged( frontends, ECrudOp.REMOVE, removedObjs.values().first().first().skid() );
      }
      if( createCount == 1 ) {
        // Отправление события об создании класса для frontend
        fireWhenObjectsChanged( frontends, ECrudOp.CREATE, createdObjs.values().first().first().skid() );
      }
      if( updateCount == 1 ) {
        // Отправление события об создании класса для frontend
        fireWhenObjectsChanged( frontends, ECrudOp.EDIT, updatedObjs.values().first().first().left().skid() );
      }
      if( removeCount + createCount + updateCount > 1 ) {
        fireWhenObjectsChanged( frontends, ECrudOp.LIST, null );
      }
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
   * @return {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link IDtoObject}&gt;&gt; карта объектов наденных в
   *         базе данных.<br>
   *         Ключ: Описание классов;<br>
   *         Значение: Список объектов.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( "unchecked" )
  private IMap<ISkClassInfo, IList<IDtoObject>> loadObjectsBySkids( ISkidList aSkids,
      IStringMapEdit<ISkClassInfo> aClassInfosByIds, IStringMapEdit<Class<S5ObjectEntity>> aImplByIds ) {
    TsNullArgumentRtException.checkNulls( aSkids, aClassInfosByIds, aImplByIds );
    // Формирование карты найденных объектов. Ключ: описание класса; Значение: список объектов класса
    IMapEdit<ISkClassInfo, IListEdit<IDtoObject>> retValue = new ElemMap<>();
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
      IDtoObject obj = entityManager.find( objImplClass, new S5ObjectID( skid ) );
      if( obj == null ) {
        // Объект не найден
        continue;
      }
      IListEdit<IDtoObject> objs = retValue.findByKey( classInfo );
      if( objs == null ) {
        objs = new ElemArrayList<>( aSkids.size() );
        retValue.put( classInfo, objs );
      }
      objs.add( obj );
    }
    return (IMap<ISkClassInfo, IList<IDtoObject>>)(Object)retValue;
  }

  /**
   * Возвращает список объектов которые есть в базе данных и которых нет
   *
   * @param aObjects {@link IList}&lt;{@link IDtoObject}&gt; проверяемый список объектов
   * @param aClassesByIds {@link IStringMapEdit}&lt;{@link ISkClassInfo}&gt; карта описаний классов по их
   *          идентификаторам. <br>
   *          Ключ: идентификатор класса;<br>
   *          Значение: описание класса.
   * @param aImplByIds {@link IStringMapEdit}&lt;Class&gt; карта классов реализаций объектов по описаниям классов. <br>
   *          Ключ: описание класса;<br>
   *          Значение: Класс реализации объекта.
   * @param aUpdatedObjs
   *          {@link IMap}&lt;{@link ISkClassInfo},{@link IListEdit}&lt;{@link Pair}&lt;{@link IDtoObject},{@link IDtoObject}&gt;&gt;&gt;
   *          карта объектов обновляемых в базе данных.<br>
   *          Ключ: Описание классов;<br>
   *          Значение: Список пар: {@link Pair#left()} - старое состояние объекта, {@link Pair#right()} - новое.
   * @param aCreatedObjs {@link IMap}&lt;{@link ISkClassInfo},{@link IList}&lt;{@link IDtoObject}&gt;&gt; карта объектов
   *          создаваемых в базе данных.<br>
   *          Ключ: Описание классов;<br>
   *          Значение: Список объектов.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private void loadObjectsByDpu( IList<IDtoObject> aObjects, IStringMapEdit<ISkClassInfo> aClassesByIds,
      IStringMapEdit<Class<S5ObjectEntity>> aImplByIds,
      IMapEdit<ISkClassInfo, IListEdit<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMapEdit<ISkClassInfo, IListEdit<IDtoObject>> aCreatedObjs ) {
    TsNullArgumentRtException.checkNulls( aObjects, aClassesByIds, aImplByIds, aCreatedObjs, aUpdatedObjs );
    // Карта конструкторов объектов. Ключ: идентификатор класса; Значение: конструктор
    IStringMapEdit<Constructor<S5ObjectEntity>> objectContructors = new StringMap<>();
    for( IDtoObject obj : aObjects ) {
      String classId = obj.classId();
      ISkClassInfo classInfo = aClassesByIds.findByKey( classId );
      if( classInfo == null ) {
        classInfo = sysdescrReader.getClassInfo( classId );
        aClassesByIds.put( classId, classInfo );
      }
      // Класс реализации объекта
      Class<S5ObjectEntity> objImplClass = getObjectImplClass( classInfo, aImplByIds );
      // Создание нового объекта
      IDtoObject newObj = obj;
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
      IDtoObject prevObj = entityManager.find( objImplClass, new S5ObjectID( obj.skid() ) );
      if( prevObj == null ) {
        // Объект не найден значит он создается
        IListEdit<IDtoObject> objs = aCreatedObjs.findByKey( classInfo );
        if( objs == null ) {
          objs = new ElemArrayList<>( aObjects.size() );
          aCreatedObjs.put( classInfo, objs );
        }
        objs.add( newObj );
        continue;
      }
      IListEdit<Pair<IDtoObject, IDtoObject>> objs = aUpdatedObjs.findByKey( classInfo );
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
   * @param aFrontends {@link IS5FrontendRear} список фронтендов подключенных к бекенду
   * @param aOp {@link ECrudOp} тип операции над объектами
   * @param aObjectId {@link Skid} идентификатор объекта или null для {@link ECrudOp#LIST}.
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static void fireWhenObjectsChanged( IList<IS5FrontendRear> aFrontends, ECrudOp aOp, Skid aObjectId ) {
    TsNullArgumentRtException.checkNulls( aFrontends, aOp );
    TsNullArgumentRtException.checkTrue( aOp != ECrudOp.LIST && aObjectId == null );
    GtMessage message = IBaObjectsMessages.makeMessage( aOp, aObjectId );
    for( IS5FrontendRear frontend : aFrontends ) {
      frontend.onBackendMessage( message );
    }
  }
}
