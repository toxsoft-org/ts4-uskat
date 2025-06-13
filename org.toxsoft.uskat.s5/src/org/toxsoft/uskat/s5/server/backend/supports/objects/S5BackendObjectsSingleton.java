package org.toxsoft.uskat.s5.server.backend.supports.objects;

import static org.toxsoft.core.tslib.coll.impl.TsCollectionsUtils.*;
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
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
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
  private EntityManager em;

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

  @SuppressWarnings( { "unchecked", "nls", "boxing" } )
  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void writeObjects( IS5FrontendRear aFrontend, ISkidList aRemovedSkids, IList<IDtoObject> aObjects,
      boolean aInterceptable ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aRemovedSkids, aObjects );
    // Время начала выполнения запроса
    long currTime = System.currentTimeMillis();
    // Карта описаний классов (кэш) по их идентификаторам
    IStringMapEdit<ISkClassInfo> classesByIds = new StringMap<>();
    // Карта классов реализаций объектов (кэш) по идентификаторам их классов
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
    int rc = 0;
    for( IList<IDtoObject> classRemovedObjs : removedObjs.values() ) {
      for( IDtoObject removedObj : classRemovedObjs ) {
        changedObjectIds.add( removedObj.skid() );
        // removing obj rivets
        rc += removingRivets( removedObj, em, sysdescrReader, classesByIds, implByIds, logger() );
        // removing obj
        em.remove( removedObj );
        rc++;
        if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
          logger().debug( "writeObjects(...): removed entity %s, rc = %d", removedObj, rc );
        }
      }
    }
    // Обновление объектов
    int uc = 0;
    for( IList<Pair<IDtoObject, IDtoObject>> objs : updatedObjs.values() ) {
      for( Pair<IDtoObject, IDtoObject> obj : objs ) {
        changedObjectIds.add( obj.right().skid() );
        // updating obj rivets
        uc += updateRivets( obj.left(), obj.right(), em, sysdescrReader, classesByIds, implByIds, logger() );
        // 2020-07-23 mvk
        // em.merge( obj.right() );
        S5ObjectEntity changedObj = ((S5ObjectEntity)em.merge( obj.right() ));
        changedObj.setAttrs( obj.right().attrs() );
        changedObj.setRivets( obj.right().rivets() );
        // Восстановление списка obj.left(!) обратных склепок
        changedObj.setRivetRevs( obj.left().rivetRevs() );
        // TODO: mvkd experimental
        // updateObject( em, obj.right() );
        uc++;
        // TODO: updating rivetRevs
        if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
          logger().debug( "writeObjects(...): merge entity %s, uc = %d", obj, uc );
        }
      }
    }
    // Создание объектов
    int cc = 0;
    for( IList<IDtoObject> objs : createdObjs.values() ) {
      for( IDtoObject obj : objs ) {
        em.persist( obj );
        // TODO: mvkd experimental
        // createObject( em, obj );
        cc++;
        // добавление obj rivets
        cc += creatingRivets( obj, em, sysdescrReader, classesByIds, implByIds, logger() );
        // TODO: updating rivetRevs
        if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
          logger().debug( "writeObjects(...): persist entity %s, cc = %d", obj, cc );
        }
      }
    }
    long entityManagerTimestamp1 = System.currentTimeMillis();

    // Синхронизация с базой данных
    if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      logger().debug( "writeObjects(...): flush before. rc = %d, uc = %d, cc = %d", rc, uc, cc );
    }
    em.flush();
    if( logger().isSeverityOn( ELogSeverity.DEBUG ) ) {
      long t = System.currentTimeMillis() - entityManagerTimestamp1;
      logger().debug( "writeObjects(...): flush after. rc = %d, uc = %d, cc = %d, time = %d msec", rc, uc, cc, t );
    }

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
    long at = eventTimestamp - currTime;
    long lt = loadTimestamp - currTime;
    long it1 = interceptorTimestamp1 - loadTimestamp;
    long et1 = entityManagerTimestamp1 - interceptorTimestamp1;
    long et2 = entityManagerTimestamp2 - entityManagerTimestamp1;
    long it2 = interceptorTimestamp2 - entityManagerTimestamp2;
    long et = eventTimestamp - interceptorTimestamp2;

    // Признак необходимости оповещения frontend об изменениях
    boolean needFrontendNotify = (rc > 0 || cc > 0 || uc > 0);

    if( needFrontendNotify ) {
      IList<IS5FrontendRear> frontends = backend().attachedFrontends();
      if( rc == 1 ) {
        // Отправление события об удалении объекта
        fireWhenObjectsChanged( frontends, ECrudOp.REMOVE, removedObjs.values().first().first().skid() );
      }
      if( cc == 1 ) {
        // Отправление события об создании объекта
        fireWhenObjectsChanged( frontends, ECrudOp.CREATE, createdObjs.values().first().first().skid() );
      }
      if( uc == 1 ) {
        // Отправление события об обновлении объекта
        fireWhenObjectsChanged( frontends, ECrudOp.EDIT, updatedObjs.values().first().first().left().skid() );
      }
      if( rc + cc + uc > 1 ) {
        // Отправление события об обновлении объектов
        fireWhenObjectsChanged( frontends, ECrudOp.LIST, null );
      }
    }
    if( logger().isSeverityOn( ELogSeverity.INFO ) ) {
      logger().info( MSG_WRITE_OBJECTES, rc, uc, cc, at, lt, it1, et1, et2, it2, et );
    }
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
      IDtoObject obj = em.find( objImplClass, new S5ObjectID( skid ) );
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
      // Поиск и если необходимо кэширование описания класса
      ISkClassInfo classInfo = findAndCacheClassInfo( sysdescrReader, classId, aClassesByIds );
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
      IDtoObject prevObj = em.find( objImplClass, new S5ObjectID( obj.skid() ) );
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
   * Проводит попытку прочитать описание класса из представленного кэша классов. Если описание класса в кэше не найдно,
   * то попытка найти класс через представленный читатель и размещение его в кэше.
   *
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель классов.
   * @param aClassId String идентификатор класса для поиска описания.
   * @param aClassesByIds {@link IStringMapEdit}&lt;{@link ISkClassInfo}&gt; редактируемый кэш описаний классов.<br>
   *          Ключ: идентификтор класса.
   * @return {@link ISkClassInfo} найденное описание класса. null: класс не существует.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static ISkClassInfo findAndCacheClassInfo( ISkSysdescrReader aSysdescrReader, String aClassId,
      IStringMapEdit<ISkClassInfo> aClassesByIds ) {
    TsNullArgumentRtException.checkNulls( aSysdescrReader, aClassId, aClassesByIds );
    ISkClassInfo classInfo = aClassesByIds.findByKey( aClassId );
    if( classInfo == null ) {
      classInfo = aSysdescrReader.getClassInfo( aClassId );
      aClassesByIds.put( aClassId, classInfo );
    }
    return classInfo;
  }

  /**
   * Добавляет склепки указанного объекта на другие объекты.
   *
   * @param aObj {@link IDtoObject} добавленный объект.
   * @param aEntityManager {@link EntityManager} менеджер постоянства.
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель классов.
   * @param aClassesByIds {@link IStringMapEdit}&lt;{@link ISkClassInfo}&gt; редактируемый кэш описаний классов.<br>
   *          Ключ: идентификтор класса.
   * @param aImplByIds {@link IStringMapEdit}&lt;Class&gt; карта классов реализаций объектов по описаниям классов. <br>
   *          Ключ: описание класса;<br>
   *          Значение: Класс реализации объекта.
   * @return int количество обновленных объектов.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static int creatingRivets( IDtoObject aObj, EntityManager aEntityManager, ISkSysdescrReader aSysdescrReader,
      IStringMapEdit<ISkClassInfo> aClassesByIds, IStringMapEdit<Class<S5ObjectEntity>> aImplByIds, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aObj, aEntityManager, aSysdescrReader, aClassesByIds, aImplByIds );
    int retValue = 0;
    ISkClassInfo classInfo = aSysdescrReader.findClassInfo( aObj.classId() );
    Skid leftObjId = aObj.skid();
    IMappedSkids rivets = aObj.rivets();
    RivetRevEditor rivetRevEditor = new RivetRevEditor( aLogger );
    // Необходимо пройти по всем объектам склепок и добавить обратные склепки на добавленный объект
    for( IDtoRivetInfo rivetInfo : classInfo.rivets().list() ) {
      String rivetId = rivetInfo.id();
      ISkClassInfo rivetClassInfo = classInfo.rivets().findSuperDeclarer( rivetId );
      String rivetClassId = rivetClassInfo.id();
      // Установка в редакторе описания склепки
      rivetRevEditor.setRivet( rivetClassId, rivetId );

      ISkidList rightObjIds = rivets.map().getByKey( rivetId );
      for( Skid rightObjId : rightObjIds ) {
        S5ObjectEntity rightObj = findObject( rightObjId, aEntityManager, aSysdescrReader, aClassesByIds, aImplByIds );
        if( rightObj == null ) {
          aLogger.error( ERR_RIVET_REVS_RIGHT_OBJ_NOT_FOUND, METHOD_CREATING_RIVETS, rightObjId );
          continue;
        }
        // Установка правого объекта в редакторе обратных склепок. aCreating = true
        SkidList skidListEdit = rivetRevEditor.setRightObj( rightObj, true );
        if( skidListEdit.hasElem( leftObjId ) ) {
          aLogger.error( ERR_RIVERT_REVS_ALREADY_EXIST, METHOD_CREATING_RIVETS, rightObjId, rivetId, leftObjId );
          continue;
        }
        skidListEdit.add( leftObjId );
        if( rivetRevEditor.commit() ) {
          retValue++;
        }
      }
    }
    return retValue;
  }

  /**
   * Удаляет склепки указанного объекта на другие объекты.
   *
   * @param aObj {@link IDtoObject} удаляемый объект.
   * @param aEntityManager {@link EntityManager} менеджер постоянства.
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель классов.
   * @param aClassesByIds {@link IStringMapEdit}&lt;{@link ISkClassInfo}&gt; редактируемый кэш описаний классов.<br>
   *          Ключ: идентификтор класса.
   * @param aImplByIds {@link IStringMapEdit}&lt;Class&gt; карта классов реализаций объектов по описаниям классов. <br>
   *          Ключ: описание класса;<br>
   *          Значение: Класс реализации объекта.
   * @return int количество обновленных объектов.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static int removingRivets( IDtoObject aObj, EntityManager aEntityManager, ISkSysdescrReader aSysdescrReader,
      IStringMapEdit<ISkClassInfo> aClassesByIds, IStringMapEdit<Class<S5ObjectEntity>> aImplByIds, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aObj, aEntityManager, aSysdescrReader, aClassesByIds, aImplByIds );
    int retValue = 0;
    ISkClassInfo classInfo = aSysdescrReader.findClassInfo( aObj.classId() );
    Skid leftObjId = aObj.skid();
    IMappedSkids rivets = aObj.rivets();
    RivetRevEditor rivetRevEditor = new RivetRevEditor( aLogger );
    // Необходимо пройти по всем объектам склепок и удалить обратные склепки на удаляемый объект
    for( IDtoRivetInfo rivetInfo : classInfo.rivets().list() ) {
      String rivetId = rivetInfo.id();
      ISkClassInfo rivetClassInfo = classInfo.rivets().findSuperDeclarer( rivetId );
      String rivetClassId = rivetClassInfo.id();
      // Установка в редакторе описания склепки
      rivetRevEditor.setRivet( rivetClassId, rivetId );

      ISkidList rightObjIds = rivets.map().getByKey( rivetId );
      for( Skid rightObjId : rightObjIds ) {
        S5ObjectEntity rightObj = findObject( rightObjId, aEntityManager, aSysdescrReader, aClassesByIds, aImplByIds );
        if( rightObj == null ) {
          aLogger.error( ERR_RIVET_REVS_RIGHT_OBJ_NOT_FOUND, METHOD_REMOVING_RIVETS, rightObjId );
          continue;
        }
        // Установка правого объекта в редакторе обратных склепок. aCreating = false
        SkidList skidListEdit = rivetRevEditor.setRightObj( rightObj, false );
        if( skidListEdit.remove( leftObjId ) == 0 ) {
          aLogger.error( ERR_RIVET_REVS_LEFT_OBJ_NOT_FOUND, METHOD_REMOVING_RIVETS, rightObjId, rivetId, leftObjId );
          continue;
        }
        if( rivetRevEditor.commit() ) {
          retValue++;
        }
      }
    }
    return retValue;
  }

  /**
   * Обновляет склепки указанного объекта на другие объекты.
   *
   * @param aPrevObj {@link IDtoObject} предыдущее состояние объекта.
   * @param aNewObj {@link IDtoObject} новое состояние объекта.
   * @param aEntityManager {@link EntityManager} менеджер постоянства.
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель классов.
   * @param aClassesByIds {@link IStringMapEdit}&lt;{@link ISkClassInfo}&gt; редактируемый кэш описаний классов.<br>
   *          Ключ: идентификтор класса.
   * @param aImplByIds {@link IStringMapEdit}&lt;Class&gt; карта классов реализаций объектов по описаниям классов. <br>
   *          Ключ: описание класса;<br>
   *          Значение: Класс реализации объекта.
   * @return int количество обновленных объектов.
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException предыдущее и новое состояние должны принадлежать одному объекту
   */
  private static int updateRivets( IDtoObject aPrevObj, IDtoObject aNewObj, EntityManager aEntityManager,
      ISkSysdescrReader aSysdescrReader, IStringMapEdit<ISkClassInfo> aClassesByIds,
      IStringMapEdit<Class<S5ObjectEntity>> aImplByIds, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aPrevObj, aNewObj, aEntityManager, aSysdescrReader, aClassesByIds,
        aImplByIds );
    TsIllegalArgumentRtException.checkFalse( aPrevObj.skid().equals( aNewObj.skid() ) );
    int retValue = 0;
    ISkClassInfo classInfo = aSysdescrReader.findClassInfo( aPrevObj.classId() );
    // Идентфикатор объекта
    Skid leftObjId = aPrevObj.skid();
    // Предыдущее состояние склепок
    IMappedSkids prevRivets = aPrevObj.rivets();
    // Новое состояние склепок
    IMappedSkids newRivets = aNewObj.rivets();
    // Редактор обратных склепок правых объектов
    RivetRevEditor rivetRevEditor = new RivetRevEditor( aLogger );
    // Необходимо пройти по всем объектам склепок и удалить обратные склепки на удаляемый объект
    for( IDtoRivetInfo rivetInfo : classInfo.rivets().list() ) {
      String rivetId = rivetInfo.id();
      ISkClassInfo rivetClassInfo = classInfo.rivets().findSuperDeclarer( rivetId );
      String rivetClassId = rivetClassInfo.id();
      // Установка в редакторе описания склепки
      rivetRevEditor.setRivet( rivetClassId, rivetId );

      ISkidList prevRightObjIds = prevRivets.map().getByKey( rivetId );
      ISkidList newRightObjIds = newRivets.map().getByKey( rivetId );
      // Удаление объектов из склепок
      for( Skid rightObjId : subtract( new SkidList( prevRightObjIds ), newRightObjIds ) ) {
        S5ObjectEntity rightObj = findObject( rightObjId, aEntityManager, aSysdescrReader, aClassesByIds, aImplByIds );
        if( rightObj == null ) {
          aLogger.error( ERR_RIVET_REVS_RIGHT_OBJ_NOT_FOUND, METHOD_UPDATING_RIVETS, rightObjId );
          continue;
        }
        // Установка правого объекта в редакторе обратных склепок. aCreating = false
        SkidList skidListEdit = rivetRevEditor.setRightObj( rightObj, false );
        if( skidListEdit.remove( leftObjId ) == 0 ) {
          aLogger.error( ERR_RIVET_REVS_LEFT_OBJ_NOT_FOUND, METHOD_UPDATING_RIVETS, rightObjId, rivetId, leftObjId );
          continue;
        }
        if( rivetRevEditor.commit() ) {
          retValue++;
        }
      }
      // Добавление объектов в склепки
      for( Skid rightObjId : subtract( new SkidList( newRightObjIds ), prevRightObjIds ) ) {
        S5ObjectEntity rightObj = findObject( rightObjId, aEntityManager, aSysdescrReader, aClassesByIds, aImplByIds );
        if( rightObj == null ) {
          aLogger.error( ERR_RIVET_REVS_RIGHT_OBJ_NOT_FOUND, METHOD_UPDATING_RIVETS, rightObjId );
          continue;
        }
        // Установка правого объекта в редакторе обратных склепок. aCreating = false
        SkidList skidListEdit = rivetRevEditor.setRightObj( rightObj, false );
        if( skidListEdit.hasElem( leftObjId ) ) {
          aLogger.error( ERR_RIVERT_REVS_ALREADY_EXIST, METHOD_UPDATING_RIVETS, rightObjId, rivetId, leftObjId );
          continue;
        }
        skidListEdit.add( leftObjId );
        if( rivetRevEditor.commit() ) {
          retValue++;
        }
      }
    }
    return retValue;
  }

  /**
   * Редактор обратных склепок объекта
   *
   * @author mvk
   */
  private static class RivetRevEditor {

    private final IStringMapEdit<IMappedSkids> newRivetRevs   = new StringMap<>();
    private final MappedSkids                  newMappedSkids = new MappedSkids();
    private final SkidList                     newSkidList    = new SkidList();
    private final ILogger                      logger;

    private String rivetClassId;
    private String rivetId;

    private S5ObjectEntity obj;

    /**
     * Constructor.
     *
     * @param aLogger {@link ILogger} журнал
     * @throws TsNullArgumentRtException аргумент = null
     */
    RivetRevEditor( ILogger aLogger ) {
      TsNullArgumentRtException.checkNull( aLogger );
      logger = aLogger;
    }

    /**
     * Установка описания редактируемой склепки
     *
     * @param aRivetClassId String класс в котором определяется склепка
     * @param aRivetId String идентификатор склепки
     * @throws TsNullArgumentRtException любой аргумент = null
     */
    void setRivet( String aRivetClassId, String aRivetId ) {
      TsNullArgumentRtException.checkNulls( aRivetClassId, aRivetId );
      rivetClassId = aRivetClassId;
      rivetId = aRivetId;
      newRivetRevs.clear();
      newMappedSkids.map().clear();
      newSkidList.clear();
      newRivetRevs.put( aRivetClassId, newMappedSkids );
      newMappedSkids.map().put( aRivetId, newSkidList );
    }

    /**
     * Установка объекта для редактирования (правый объект в склепке).
     *
     * @param aRightObj {@link S5ObjectEntity} объект с обратными склепками
     * @param aCreating boolean <b>true</b> редактор нового объекта; редактор существующего объекта.
     * @return {@link SkidList} редактируемый список идентификторов объектов входящих в обратную склепку.
     * @throws TsNullArgumentRtException любой аргумент = null
     * @throws TsIllegalArgumentRtException не установлено описание редактируемой склепки
     */
    SkidList setRightObj( S5ObjectEntity aRightObj, boolean aCreating ) {
      TsNullArgumentRtException.checkNull( aRightObj );
      TsIllegalStateRtException.checkNull( rivetClassId );
      setRivet( rivetClassId, rivetId );

      // Попытка инициализировать редактор предыдущими значениями
      IStringMap<IMappedSkids> prevRivetRevs = aRightObj.rivetRevs();
      IMappedSkids prevMappedSkids = prevRivetRevs.findByKey( rivetClassId );
      if( prevMappedSkids == null && !aCreating ) {
        logger.error( ERR_RIVET_REVS_EDITOR_CLASS_NOT_FOUND, aRightObj.skid(), rivetClassId );
      }
      if( prevMappedSkids != null ) {
        newMappedSkids.setAll( prevMappedSkids.map() );
        ISkidList prevSkidList = prevMappedSkids.map().findByKey( rivetId );
        if( prevSkidList == null && !aCreating ) {
          logger.error( ERR_RIVET_REVS_EDITOR_RIVET_NOT_FOUND, aRightObj.skid(), rivetId );
        }
        if( prevSkidList != null ) {
          newSkidList.setAll( prevSkidList );
        }
      }
      return newSkidList;
    }

    /**
     * Если необходимо, сохраняет изменения в объекте {@link #setRightObj(S5ObjectEntity, boolean)}.
     *
     * @return boolean <b>true</b> данные были сохранены. <b>false</b> данные не изменились.
     */
    boolean commit() {
      TsIllegalStateRtException.checkNull( obj );
      boolean retValue = false;
      if( !obj.rivetRevs().equals( newRivetRevs ) ) {
        obj.setRivetRevs( newRivetRevs );
        retValue = true;
      }
      obj = null;
      return retValue;
    }
  }

  /**
   * Находит объект по указанному идентификатору используя и обновляя кэши классов и реализаций объектов.
   *
   * @param aObjId {@link Skid} идентификатор объекта для поиска.
   * @param aEntityManager {@link EntityManager} менеджер постоянства.
   * @param aSysdescrReader {@link ISkSysdescrReader} читатель классов.
   * @param aClassesByIds {@link IStringMapEdit}&lt;{@link ISkClassInfo}&gt; редактируемый кэш описаний классов.<br>
   *          Ключ: идентификтор класса.
   * @param aImplByIds {@link IStringMapEdit}&lt;Class&gt; карта классов реализаций объектов по описаниям классов. <br>
   *          Ключ: описание класса;<br>
   *          Значение: Класс реализации объекта.
   * @return {@link S5ObjectEntity} найденный объект. null: объект не существует.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static S5ObjectEntity findObject( Skid aObjId, EntityManager aEntityManager,
      ISkSysdescrReader aSysdescrReader, IStringMapEdit<ISkClassInfo> aClassesByIds,
      IStringMapEdit<Class<S5ObjectEntity>> aImplByIds ) {
    TsNullArgumentRtException.checkNulls( aEntityManager, aObjId, aSysdescrReader, aClassesByIds );
    // Идентификатор класса объекта
    String classId = aObjId.classId();
    // Поиск и если необходимо кэширование описания класса
    ISkClassInfo classInfo = findAndCacheClassInfo( aSysdescrReader, classId, aClassesByIds );
    // Класс реализации объекта
    Class<S5ObjectEntity> objImplClass = getObjectImplClass( classInfo, aImplByIds );
    // Поиск объекта
    S5ObjectEntity retValue = aEntityManager.find( objImplClass, new S5ObjectID( aObjId ) );
    return retValue;
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
