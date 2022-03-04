package org.toxsoft.uskat.s5.server.backend.supports.sysdescr;

import static org.toxsoft.core.tslib.gw.IGwHardConstants.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5ClassesInterceptor.*;
import static org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5TypesInterceptor.*;
import static org.toxsoft.uskat.s5.server.backend.supports.sysdescr.S5ClassEntity.*;
import static org.toxsoft.uskat.s5.server.backend.supports.sysdescr.S5ClassesSQL.*;
import static org.toxsoft.uskat.s5.server.transactions.ES5TransactionResources.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.*;
import javax.persistence.*;

import org.hibernate.exception.ConstraintViolationException;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.bricks.time.ITimedListEdit;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;
import org.toxsoft.uskat.s5.server.startup.IS5InitialImplementSingleton;
import org.toxsoft.uskat.s5.server.transactions.IS5Transaction;
import org.toxsoft.uskat.s5.server.transactions.IS5TransactionManagerSingleton;

import ru.uskat.backend.messages.SkMessageWhenSysdescrChanged;
import ru.uskat.common.dpu.IDpuSdClassInfo;
import ru.uskat.common.dpu.IDpuSdTypeInfo;
import ru.uskat.common.dpu.impl.DpuSdClassInfo;
import ru.uskat.common.dpu.rt.events.SkEvent;
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
    BACKEND_CORE_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendSysDescrSingleton
    extends S5BackendSupportSingleton
    implements IS5BackendSysDescrSingleton {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_SYSDESCR_ID = "S5BackendSysDescrSingleton"; //$NON-NLS-1$

  /**
   * Менеджер постоянства
   */
  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Конфигурация бекенда
   */
  @EJB
  private IS5InitialImplementSingleton backendConfig;

  /**
   * Поддержка интерсепторов операций проводимых над типами
   */
  private final S5InterceptorSupport<IS5TypesInterceptor> typesInterceptors = new S5InterceptorSupport<>();

  /**
   * Поддержка интерсепторов операций проводимых над классами
   */
  private final S5InterceptorSupport<IS5ClassesInterceptor> classesInterceptors = new S5InterceptorSupport<>();

  /**
   * Читатель данных системного описания
   */
  private S5BackendSysdescrReader sysdescrReader;

  /**
   * Конструктор.
   */
  public S5BackendSysDescrSingleton() {
    super( BACKEND_SYSDESCR_ID, STR_D_BACKEND_SYSDESCR );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendSupportSingleton
  //
  @Override
  protected void doInitSupport() {
    IS5BackendSysDescrSingleton sysdescr = sessionContext().getBusinessObject( IS5BackendSysDescrSingleton.class );
    sysdescrReader = new S5BackendSysdescrReader( entityManager, sysdescr );
    // Регистрация на получение извещений об изменениях системного описания
    addTypeInterceptor( sysdescrReader, 100 );
    addClassInterceptor( sysdescrReader, 100 );
    // Инициализация читателя, далее обновления будут проводиться через интерсепцию
    sysdescrReader.setTypeInfos( S5BackendSysdescrReader.detach( entityManager, readTypeInfos() ) );
    sysdescrReader.setClassInfos( S5BackendSysdescrReader.detach( entityManager, readClassInfos() ) );
    // Проверка существования корневого класса
    S5ClassEntity rootClass = entityManager.find( S5ClassEntity.class, GW_ROOT_CLASS_ID );
    // Создание корневого класса - это необходимо сделать до создания любых других классов (внешний ключ)
    if( rootClass == null ) {
      logger().warning( MSG_CREATE_ROOT_CLASS_START, GW_ROOT_CLASS_ID );
      rootClass = new S5ClassEntity( GW_ROOT_CLASS_ID, STR_N_GW_ROOT_CLASS, STR_D_GW_ROOT_CLASS );
      entityManager.persist( rootClass );
      entityManager.flush();
      // Сброс кэша классов
      sysdescrReader.invalidateCache();
      // Завершение создания корневого класса
      logger().warning( MSG_CREATE_ROOT_CLASS_FINISH, GW_ROOT_CLASS_ID );
    }
  }

  @Override
  protected void doCloseSupport() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса IS5BackendSysDescrSingleton
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public boolean isAncestor( String aParentClassId, String aClassId ) {
    TsNullArgumentRtException.checkNulls( aParentClassId, aClassId );
    return sysdescrReader.isAncestor( aParentClassId, aClassId );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void addTypeInterceptor( IS5TypesInterceptor aInterceptor, int aPriority ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    typesInterceptors.add( aInterceptor, aPriority );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void removeTypeInterceptor( IS5TypesInterceptor aInterceptor ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    typesInterceptors.remove( aInterceptor );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void addClassInterceptor( IS5ClassesInterceptor aInterceptor, int aPriority ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    classesInterceptors.add( aInterceptor, aPriority );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public void removeClassInterceptor( IS5ClassesInterceptor aInterceptor ) {
    TsNullArgumentRtException.checkNull( aInterceptor );
    classesInterceptors.remove( aInterceptor );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public ISkSysdescrReader getReader() {
    return sysdescrReader;
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ISkBackendSystemDescription
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IStridablesList<IDpuSdTypeInfo> readTypeInfos() {
    // Выполнение запроса
    TypedQuery<S5TypeEntity> query = entityManager.createNamedQuery( QUERY_NAME_GET_TYPES, S5TypeEntity.class );
    List<S5TypeEntity> entities = query.getResultList();
    IStridablesListEdit<IDpuSdTypeInfo> retValue = new StridablesList<>();
    for( S5TypeEntity entity : entities ) {
      retValue.add( entity );
    }
    return retValue;
  }

  @Override

  @Lock( LockType.WRITE )
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void writeTypeInfos( IStringList aRemoveTypeIdsOrNull, IList<IDpuSdTypeInfo> aNewlyDefinedTypeInfos ) {
    TsNullArgumentRtException.checkNull( aNewlyDefinedTypeInfos );
    // Время начала выполнения запроса
    // long currTime = System.currentTimeMillis();
    // Запрос всех классов зарегистрированных в системе
    @SuppressWarnings( "unchecked" )
    IStridablesListEdit<S5ClassEntity> allClassInfos =
        new StridablesList<>( (IStridablesList<S5ClassEntity>)(Object)sysdescrReader.readClassInfos() );
    // Список событий сформированный при выполнении запроса
    ITimedListEdit<SkEvent> events = new TimedList<>();
    // Список идентификаторов удаляемых типов
    IStringList removeTypeIds = aRemoveTypeIdsOrNull;
    if( removeTypeIds == null ) {
      removeTypeIds = sysdescrReader.readTypeInfos().ids();
    }
    // Удаление типов
    for( String typeId : removeTypeIds ) {
      // Список классов зависимых от типа
      IStridablesList<IDpuSdClassInfo> dc = getClassesDependsFromType( allClassInfos, typeId );
      if( dc.size() > 0 ) {
        // Запрещено удалять тип данных используемый для описания классов системы
        throw new TsIllegalArgumentRtException( MSG_ERR_HAS_TYPE_DEPENDENT_CLASSES, typeId, toString( dc ) );
      }
      if( sysdescrReader.findType( typeId ) != null && deleteType( typeId ) ) {
        // Тип найден и удален. Формирование события
        // TODO: сформировать событие
        // eventService().fireEvent( createEvent( currTime, null, null ) );
      }
    }
    // Добавление/обновление типов
    for( IDpuSdTypeInfo typeInfo : aNewlyDefinedTypeInfos ) {
      // Идентификатор типа
      String typeId = typeInfo.id();
      // Предыдущее определение типа. null: новый тип
      IDpuSdTypeInfo prevTypeInfo = sysdescrReader.readTypeInfos().findByKey( typeId );
      if( prevTypeInfo != null && prevTypeInfo.equals( typeInfo ) ) {
        // Определение типа не изменилось
        continue;
      }
      // 2021-01-27 mvk в sitrol-tm справочники создают новые типы данных, не понятно почему был отключен код
      // Список классов зависимых от типа
      IStridablesList<IDpuSdClassInfo> dc = getClassesDependsFromType( allClassInfos, typeId );
      // Попытка создать или обновить тип
      boolean created = defineType( typeInfo, dc );
      // TODO: сформировать событие используя флаг created
      // eventService().fireEvent( createEvent( currTime, null, null ) );
    }
    if( events.size() > 0 ) {
      // Отправление события для frontend
      fireWhenSysdescrChanged( backend().attachedFrontends() );
    }
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IStridablesList<IDpuSdClassInfo> readClassInfos() {
    // Выполнение запроса
    TypedQuery<S5ClassEntity> query = entityManager.createNamedQuery( QUERY_NAME_GET_CLASSES, S5ClassEntity.class );
    List<S5ClassEntity> entities = query.getResultList();
    IStridablesListEdit<IDpuSdClassInfo> retValue = new StridablesList<>();
    for( S5ClassEntity entity : entities ) {
      // TODO: в SkClassInfoManager.loadBackend поднимается ошибка если передавать root-класс. Это нормально???
      if( !entity.parentId().equals( TsLibUtils.EMPTY_STRING ) ) {
        retValue.add( entity );
      }
    }
    return retValue;
  }

  @Lock( LockType.WRITE )
  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void writeClassInfos( IStringList aRemoveClassIdsOrNull, IStridablesList<IDpuSdClassInfo> aUpdateClassInfos ) {
    TsNullArgumentRtException.checkNull( aUpdateClassInfos );
    // Время начала выполнения запроса
    // long currTime = System.currentTimeMillis();
    // Запрос всех классов зарегистрированных в системе
    @SuppressWarnings( "unchecked" )
    IStridablesListEdit<S5ClassEntity> allClassInfos =
        new StridablesList<>( (IStridablesList<S5ClassEntity>)(Object)sysdescrReader.readClassInfos() );
    // Список описаний удаляемых классов в порядке сначала потомок, потом родитель
    IStridablesList<IDpuSdClassInfo> removeClassInfos =
        orderByDependencies( filterClassInfos( allClassInfos, aRemoveClassIdsOrNull ) );
    // Признак необходимости оповещения frontend об изменениях
    boolean needFrontendNotify = false;
    // Удаление классов в порядке сначала удаляется потомок, потом родитель
    for( int index = removeClassInfos.size() - 1; index >= 0; index-- ) {
      String classId = removeClassInfos.get( index ).id();
      // Список классов зависимых от удаляемого класса
      IStridablesList<IDpuSdClassInfo> dc = getDescendantClasses( allClassInfos, classId );
      if( dc.size() > 0 ) {
        // Запрещено удалять класс имеющий классы-потомки
        throw new TsIllegalArgumentRtException( MSG_ERR_HAS_DESCENDANTS, classId, toString( dc ) );
      }
      if( sysdescrReader.findType( classId ) != null && deleteClass( classId ) ) {
        // Класс найден и удален
        allClassInfos.removeByKey( classId );
        // Требование оповещения frontend
        needFrontendNotify = true;
        // Формирование события
        // TODO: сформировать событие
        // eventService().fireEvent( createEvent( currTime, null, null ) );
      }
    }
    // Добавление/обновление классов
    for( IDpuSdClassInfo classInfo : orderByDependencies( aUpdateClassInfos ) ) {
      // Идентификатор класса
      String classId = classInfo.id();
      // Предыдущее определение типа. null: новый тип
      IDpuSdClassInfo prevClassInfo = sysdescrReader.readClassInfos().findByKey( classId );
      if( prevClassInfo != null && prevClassInfo.equals( classInfo ) ) {
        // Определение класса не изменилось
        continue;
      }
      // Список классов зависимых от типа
      IStridablesList<IDpuSdClassInfo> dc = getDescendantClasses( allClassInfos, classId );
      // Попытка создать или обновить класс
      boolean created = defineClass( classInfo, dc );
      if( created ) {
        allClassInfos.add( entityManager.find( S5ClassEntity.class, classId ) );
      }
      // Требование оповещения frontend
      needFrontendNotify = true;
      // TODO: сформировать событие используя флаг created (класс создан или обновлен)
      // eventService().fireEvent( createEvent( currTime, null, null ) );
    }
    if( needFrontendNotify ) {
      // Отправление события для frontend
      fireWhenSysdescrChanged( backend().attachedFrontends() );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация интерфейса ISkClassHierarchyProvider
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public boolean isAssignableFrom( String aParentClassId, String aChildClassId ) {
    TsNullArgumentRtException.checkNulls( aParentClassId, aChildClassId );
    return (aParentClassId.equals( aChildClassId ) || isAncestor( aParentClassId, aChildClassId ));
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Удаление типа
   *
   * @param aTypeId String идентификатор типа
   * @return boolean <b>true</b> тип удален; <b>false</b> тип незарегистрирован в системе
   * @throws TsNullArgumentRtException аргумент = null
   */
  private boolean deleteType( String aTypeId ) {
    // Проверка контракта
    StridUtils.checkValidIdPath( aTypeId );
    try {
      // Выполнение запроса
      S5TypeEntity entity = entityManager.find( S5TypeEntity.class, aTypeId );
      if( entity == null ) {
        // Тип незарегистрирован в системе
        return false;
      }
      // Пред-интерсепция
      callBeforeDeleteTypeInteceptors( typesInterceptors, entity );
      // Удаление
      entityManager.remove( entity );
      // Синхронизация с базой данных (возможно появление ConstraintViolationException)
      entityManager.flush();
      // Сброс кэша классов
      sysdescrReader.invalidateCache();
      // Пост-интерсепция
      callAfterDeleteTypeInteceptors( typesInterceptors, entity );
      return true;
    }
    catch( PersistenceException e ) {
      if( e.getCause() instanceof ConstraintViolationException ) {
        // Обработка исключений по constraints
        handleConstraintViolations( aTypeId, (ConstraintViolationException)e.getCause() );
      }
      // Неизвестное исключение
      throw e;
    }
  }

  /**
   * Создает или обновляет тип данных
   *
   * @param aTypeInfo {@link IDpuSdTypeInfo} описание типа
   * @param aDependentClasses {@link IList}&lt;IDpuSdClassInfo&gt; список описаний классов зависимых от данного типа
   * @return boolean <b>true</b> тип был создан; <b>false</b> тип был обновлен
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private boolean defineType( IDpuSdTypeInfo aTypeInfo, IStridablesList<IDpuSdClassInfo> aDependentClasses ) {
    TsNullArgumentRtException.checkNulls( aTypeInfo, aDependentClasses );
    // Выполнение запроса
    S5TypeEntity newEntity = new S5TypeEntity( aTypeInfo );
    S5TypeEntity prevEntity = entityManager.find( S5TypeEntity.class, aTypeInfo.id() );
    if( prevEntity == null ) {
      // Создание нового типа. Пред-интерсепция
      callBeforeCreateTypeInteceptors( typesInterceptors, newEntity );
      // Сохранение в базе данных
      entityManager.persist( newEntity );
      // Без вызова запрос будет закэширован до закрытия транзакции и не будет ошибки по ключу
      entityManager.flush();
      // Сброс кэша классов
      sysdescrReader.invalidateCache();
      // Пост-интерсепция
      callAfterCreateTypeInteceptors( typesInterceptors, newEntity );
      return true;
    }
    // Обновление существующего типа. Пред-интерсепция
    callBeforeUpdateTypeInteceptors( typesInterceptors, prevEntity, newEntity, aDependentClasses );
    // Обновление в базе данных
    entityManager.merge( newEntity );
    // Без вызова запрос будет закэширован до закрытия транзакции и не будет ошибки по ключу
    entityManager.flush();
    // Сброс кэша классов
    sysdescrReader.invalidateCache();
    // Пост-интерсепция
    callAfterUpdateTypeInteceptors( typesInterceptors, prevEntity, newEntity, aDependentClasses );
    return false;
  }

  /**
   * Удаление класса
   *
   * @param aClassId String идентификатор класса
   * @return boolean <b>true</b> класс удален; <b>false</b> класс незарегистрирован в системе
   * @throws TsNullArgumentRtException аргумент = null
   */
  private boolean deleteClass( String aClassId ) {
    // Проверка контракта
    StridUtils.checkValidIdPath( aClassId );
    TsIllegalArgumentRtException.checkTrue( GW_ROOT_CLASS_ID.equals( aClassId ), MSG_ERR_DELETE_ROOT, aClassId );
    try {
      // Выполнение запроса
      S5ClassEntity entity = entityManager.find( S5ClassEntity.class, aClassId );
      if( entity == null ) {
        // Тип незарегистрирован в системе
        return false;
      }
      try {
        // Пред-интерсепция
        callBeforeDeleteClassInterceptors( classesInterceptors, entity );
        // Удаление
        entityManager.remove( entity );
        // Синхронизация с базой данных (возможно появление ConstraintViolationException)
        entityManager.flush();
        // Сброс кэша классов
        sysdescrReader.invalidateCache();
        // Пост-интерсепция
        callAfterDeleteClassInterceptors( classesInterceptors, entity );
      }
      finally {
        // Очистка ресурсов текущей транзакции
        clearTransaction( transactionManager() );
      }
      return true;
    }
    catch( PersistenceException e ) {
      if( e.getCause() instanceof ConstraintViolationException ) {
        // Обработка исключений по constraints
        handleConstraintViolations( aClassId, (ConstraintViolationException)e.getCause() );
      }
      // Неизвестное исключение
      throw e;
    }
  }

  /**
   * Создает или обновляет класс
   *
   * @param aClassInfo {@link IDpuSdClassInfo} описание класса
   * @param aDescendantClasses {@link IList}&lt;IDpuSdClassInfo&gt; классы-наследники (пустой для нового класса)
   * @return boolean <b>true</b> тип был создан; <b>false</b> класс был обновлен
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private boolean defineClass( IDpuSdClassInfo aClassInfo, IStridablesList<IDpuSdClassInfo> aDescendantClasses ) {
    TsNullArgumentRtException.checkNulls( aClassInfo, aDescendantClasses );
    // Выполнение запроса
    S5ClassEntity parent = entityManager.find( S5ClassEntity.class, aClassInfo.parentId() );
    // Признак изменения корневого класса (запрещено только удаление корневого класса
    boolean isRootClass = aClassInfo.id().equals( GW_ROOT_CLASS_ID );
    if( parent == null && !isRootClass ) {
      // Для класса не найден родительский класс
      throw new TsIllegalArgumentRtException( MSG_ERR_PARENT_NOT_FOUND, aClassInfo.id(), aClassInfo.parentId() );
    }
    // Описание класса существующее в базе. null: нет в базе
    S5ClassEntity dbEntity = entityManager.find( S5ClassEntity.class, aClassInfo.id() );
    // DpuSdClassInfo единственная реализация IDpuSdClassInfo
    DpuSdClassInfo classInfo = (DpuSdClassInfo)aClassInfo;
    // Неизменяемые параметры класса.
    classInfo.params().addAll( backendConfig.impl().projectSpecificCreateClassParams( aClassInfo.id() ).params() );
    // Описание класса с новыми значениями
    S5ClassEntity newEntity = new S5ClassEntity( parent, aClassInfo );
    try {
      if( dbEntity == null ) {
        // Запрещенное состояние (нельзя добавлять/удалять корневой класс - он есть всегда)
        TsInternalErrorRtException.checkTrue( isRootClass );
        // Создание нового класса. Пред-интерсепция
        callBeforeCreateClassInterceptors( classesInterceptors, aClassInfo );
        // Сохранение в базе данных
        entityManager.persist( newEntity );
        // Без вызова запрос будет закэширован до закрытия транзакции и не будет ошибки по ключу
        entityManager.flush();
        // Сброс кэша классов
        sysdescrReader.invalidateCache();
        // Пост-интерсепция
        callAfterCreateClassInterceptors( classesInterceptors, newEntity );
        return true;
      }
      // Описание класса со старыми значениями
      S5ClassEntity prevEntity = new S5ClassEntity( parent, dbEntity );
      // Проверка отсутствия повторных описаний (дублей)
      checkDuplicateInfos( parent, aClassInfo );
      // Обновление существующего класса. Пред-интерсепция
      callBeforeUpdateClassInterceptors( classesInterceptors, dbEntity, newEntity, aDescendantClasses );

      // Обновление в базе данных
      // 2020-07-21 mvk
      // entityManager.merge( newEntity );
      // 2021-03-02 mvk prevEntity при таком использовании изменяется тоже, нам это не нужно
      // entityManager.merge( prevEntity ).update( newEntity );
      entityManager.merge( dbEntity ).update( newEntity );
      // Без вызова запрос будет закэширован до закрытия транзакции и не будет ошибки по ключу
      entityManager.flush();
      // Сброс кэша классов
      sysdescrReader.invalidateCache();
      // Пост-интерсепция
      callAfterUpdateClassInterceptors( classesInterceptors, prevEntity, newEntity, aDescendantClasses );
    }
    finally {
      // Очистка ресурсов текущей транзакции
      clearTransaction( transactionManager() );
    }
    return false;
  }

  /**
   * Фильтрует представленный список описаний классов возвращая только требуемые описания
   *
   * @param aClassInfos {@link IStridablesList}&lt; {@link S5ClassEntity}&lt; список описаний для фильтрации
   * @param aClassIdsOrNull {@link IStringList} список идентификаторов классов (фильтр). null: описания всех классов
   * @return {@link IStridablesListEdit}&lt;{@link IDpuSdClassInfo}&gt; отфильтрованный список описаний классов
   * @throws TsNullArgumentRtException aClassInfos = null
   */
  @SuppressWarnings( "unchecked" )
  private static IStridablesList<IDpuSdClassInfo> filterClassInfos( IStridablesList<S5ClassEntity> aClassInfos,
      IStringList aClassIdsOrNull ) {
    TsNullArgumentRtException.checkNull( aClassInfos );
    IStridablesListEdit<IDpuSdClassInfo> retValue = new StridablesList<>();
    if( aClassIdsOrNull == null ) {
      retValue.setAll( (IStridablesList<IDpuSdClassInfo>)(Object)aClassInfos );
      return retValue;
    }
    for( S5ClassEntity classInfo : aClassInfos ) {
      if( aClassIdsOrNull.hasElem( classInfo.id() ) ) {
        retValue.add( classInfo );
      }
    }
    return retValue;
  }

  /**
   * Возвращает указанный список в порядке при котором в начале следуют описания классов которые не зависят от классов
   * которые следуют далее.
   *
   * @param aInfos {@link IStridablesList}&lt; {@link IDpuSdClassInfo}&gt; список описаний классов
   * @return {@link IStridablesList} список классов в требуемом порядке
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static IStridablesList<IDpuSdClassInfo> orderByDependencies( IStridablesList<IDpuSdClassInfo> aInfos ) {
    TsNullArgumentRtException.checkNull( aInfos );
    IStridablesListEdit<IDpuSdClassInfo> remaining = new StridablesList<>( aInfos );
    IStridablesListEdit<IDpuSdClassInfo> retValue = new StridablesList<>();
    while( remaining.size() > 0 ) {
      for( IDpuSdClassInfo info : new StridablesList<>( remaining ) ) {
        String parentId = info.parentId();
        if( !remaining.hasKey( parentId ) ) {
          retValue.add( info );
          remaining.remove( info );
          break;
        }
      }
    }
    return retValue;
  }

  /**
   * Возвращает текстовое представление списка {@link IStridable}
   *
   * @param aStridables {@link IStridablesList} список сущностей
   * @return String текствое представление
   * @throws TsNullArgumentRtException аругмент = null
   */
  private static String toString( IStridablesList<?> aStridables ) {
    TsNullArgumentRtException.checkNull( aStridables );
    StringBuilder sb = new StringBuilder();
    for( IStridable stridable : aStridables ) {
      sb.append( stridable.id() );
      sb.append( '\n' );
    }
    return sb.toString();
  }

  /**
   * Обработка исключений по нарушению constraints dbms
   *
   * @param aClassId String - класс объекта
   * @param aException {@link ConstraintViolationException} - исходное исключение
   * @throws TsItemAlreadyExistsRtException объект уже существует (по идентфикатору или имени в классе)
   * @throws TsIllegalArgumentRtException пользовательское ограничение на запись атрибутов объекта
   */
  private static void handleConstraintViolations( String aClassId, ConstraintViolationException aException ) {
    Throwable cause = aException.getCause();
    if( cause != null && cause.getMessage() != null ) {
      String causeMsg = aException.getCause().getMessage();
      if( hasStringSequence( causeMsg, S5ClassEntity.FK_PARENTID_TO_CLASS ) ) {
        throw new TsItemAlreadyExistsRtException( aException, MSG_ERR_DELETE_HAS_LINKED_CLS, aClassId );
      }
      // if( hasStringSequence( causeMsg, S5ObjectEntity.FK_CLASSID_TO_CLASS ) ) {
      // throw new TsItemAlreadyExistsRtException( aException, MSG_ERR_DELETE_HAS_LINKED_OJBS, aClassId );
      // }
      throw new TsIllegalArgumentRtException( aException, MSG_ERR_USER_CONSTRAINTS, aClassId, cause( cause ) );
    }
    throw new TsIllegalArgumentRtException( aException, MSG_ERR_USER_CONSTRAINTS, aClassId, cause( aException ) );
  }

  /**
   * Возвращает признак того, что в исходной строке есть последовательность подстрок в указанном порядке
   *
   * @param aSource String - исходная строка
   * @param aSequence последовательность подстрок
   * @return <b>true</b> есть последовательность; <b>false</b> нет последовательности.
   * @throws TsNullArgumentRtException любой аргумент null
   */
  private static boolean hasStringSequence( String aSource, String... aSequence ) {
    TsNullArgumentRtException.checkNulls( aSource, aSequence );
    int fromIndex = 0;
    for( int index = 0; index < aSequence.length; index++ ) {
      String subString = aSequence[index];
      TsNullArgumentRtException.checkNull( subString );
      if( aSource.indexOf( subString, fromIndex ) < 0 ) {
        return false;
      }
      fromIndex += subString.length();
    }
    return true;
  }

  /**
   * Очистка текущей транзакции от ресурсов связанных с работой backend sysdescr
   * <p>
   * Если нет текущей транзакции, то ничего не делает
   *
   * @param aTxManager {@link IS5TransactionManagerSingleton} менеджер транзакций
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static void clearTransaction( IS5TransactionManagerSingleton aTxManager ) {
    TsNullArgumentRtException.checkNull( aTxManager );
    IS5Transaction tx = aTxManager.findTransaction();
    if( tx == null ) {
      return;
    }
    tx.removeResource( TX_REMOVED_CLASS_OBJS );
    tx.removeResource( TX_UPDATED_CLASS_OBJS );
    tx.removeResource( TX_UPDATED_OBJS_BY_ATTR_TYPE );
    tx.removeResource( TX_UPDATED_OBJS_BY_CURRDATA_TYPE );
    tx.removeResource( TX_ADDED_ATTRS );
    tx.removeResource( TX_REMOVED_ATTRS );
    tx.removeResource( TX_ADDED_CURRDATA );
    tx.removeResource( TX_REMOVED_CURRDATA );
    tx.removeResource( TX_UPDATED_OBJS_BY_CHANGE_IMPL );
    tx.removeResource( TX_UPDATED_FWD_LINKS_BY_CHANGE_IMPL );
    tx.removeResource( TX_UPDATED_REV_LINKS_BY_CHANGE_IMPL );
  }

  /**
   * Формирование события: произошло изменение описания системы
   *
   * @param aFrontends {@link IS5FrontendRear} список фронтендов подключенных к бекенду
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static void fireWhenSysdescrChanged( IList<IS5FrontendRear> aFrontends ) {
    TsNullArgumentRtException.checkNull( aFrontends );
    for( IS5FrontendRear frontend : aFrontends ) {
      SkMessageWhenSysdescrChanged.send( frontend );
    }
  }
}
