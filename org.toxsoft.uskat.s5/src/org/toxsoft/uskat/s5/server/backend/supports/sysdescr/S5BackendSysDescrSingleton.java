package org.toxsoft.uskat.s5.server.backend.supports.sysdescr;

import static org.toxsoft.core.tslib.gw.IGwHardConstants.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5ClassesInterceptor.*;
import static org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.backend.supports.sysdescr.S5ClassEntity.*;
import static org.toxsoft.uskat.s5.server.backend.supports.sysdescr.S5ClassesSQL.*;
import static org.toxsoft.uskat.s5.server.transactions.ES5TransactionResources.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.*;
import javax.persistence.*;

import org.hibernate.exception.ConstraintViolationException;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.helpers.ECrudOp;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringListEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringLinkedBundleList;
import org.toxsoft.core.tslib.gw.IGwHardConstants;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;
import org.toxsoft.uskat.core.backend.api.IBaClassesMessages;
import org.toxsoft.uskat.core.impl.dto.DtoClassInfo;
import org.toxsoft.uskat.s5.common.sysdescr.ISkSysdescrReader;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.interceptors.S5InterceptorSupport;
import org.toxsoft.uskat.s5.server.startup.IS5InitialImplementSingleton;
import org.toxsoft.uskat.s5.server.transactions.IS5Transaction;
import org.toxsoft.uskat.s5.server.transactions.IS5TransactionManagerSingleton;

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
    addClassInterceptor( sysdescrReader, 100 );
    // Инициализация читателя, далее обновления будут проводиться через интерсепцию
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
  // IS5BackendSysDescrSingleton
  //
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
  // ISkBackendSystemDescription
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public IStridablesList<IDtoClassInfo> readClassInfos() {
    // Выполнение запроса
    TypedQuery<S5ClassEntity> query = entityManager.createNamedQuery( QUERY_NAME_GET_CLASSES, S5ClassEntity.class );
    List<S5ClassEntity> entities = query.getResultList();
    IStridablesListEdit<IDtoClassInfo> retValue = new StridablesList<>();
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
  public void writeClassInfos( IStringList aRemoveClassIdsOrNull, IStridablesList<IDtoClassInfo> aUpdateClassInfos ) {
    TsNullArgumentRtException.checkNull( aUpdateClassInfos );
    // Время начала выполнения запроса
    // long currTime = System.currentTimeMillis();
    // Запрос всех классов зарегистрированных в системе
    @SuppressWarnings( "unchecked" )
    IStridablesListEdit<S5ClassEntity> allClassInfos =
        new StridablesList<>( (IStridablesList<S5ClassEntity>)(Object)sysdescrReader.readClassInfos() );
    // Список описаний удаляемых классов в порядке сначала потомок, потом родитель
    IStridablesList<IDtoClassInfo> removeClassInfos =
        orderByDependencies( filterClassInfos( allClassInfos, aRemoveClassIdsOrNull ) );
    // Список идентификаторов удаленных классов
    IStringListEdit removeClassIds = new StringLinkedBundleList();
    // Список идентификаторов удаленных классов
    IStringListEdit newClassIds = new StringLinkedBundleList();
    // Список идентификаторов обновленных классов
    IStringListEdit updateClassIds = new StringLinkedBundleList();

    // Удаление классов в порядке сначала удаляется потомок, потом родитель
    for( int index = removeClassInfos.size() - 1; index >= 0; index-- ) {
      String classId = removeClassInfos.get( index ).id();
      // Список классов зависимых от удаляемого класса
      IStridablesList<IDtoClassInfo> dc = getDescendantClasses( allClassInfos, classId );
      if( dc.size() > 0 ) {
        // Запрещено удалять класс имеющий классы-потомки
        throw new TsIllegalArgumentRtException( MSG_ERR_HAS_DESCENDANTS, classId, toString( dc ) );
      }
      if( sysdescrReader.findClassInfo( classId ) != null && deleteClass( classId ) ) {
        // Класс найден и удален
        allClassInfos.removeByKey( classId );
        // Требование оповещения frontend
        removeClassIds.add( classId );
      }
    }
    // Добавление/обновление классов
    for( IDtoClassInfo classInfo : orderByDependencies( aUpdateClassInfos ) ) {
      // Идентификатор класса
      String classId = classInfo.id();
      // Предыдущее определение типа. null: новый тип
      IDtoClassInfo prevClassInfo = sysdescrReader.readClassInfos().findByKey( classId );
      if( prevClassInfo != null && prevClassInfo.equals( classInfo ) ) {
        // Определение класса не изменилось
        continue;
      }
      // Список классов зависимых от типа
      IStridablesList<IDtoClassInfo> dc = getDescendantClasses( allClassInfos, classId );
      // Попытка создать или обновить класс
      boolean created = defineClass( classInfo, dc );
      if( created ) {
        allClassInfos.add( entityManager.find( S5ClassEntity.class, classId ) );
        newClassIds.add( classId );
        continue;
      }
      updateClassIds.add( classId );
    }
    // Признак необходимости оповещения frontend об изменениях
    boolean needFrontendNotify = (removeClassIds.size() > 0 || newClassIds.size() > 0 || updateClassIds.size() > 0);

    if( needFrontendNotify ) {
      if( removeClassIds.size() == 1 ) {
        // Отправление события об удалении класса для frontend
        fireWhenSysdescrChanged( backend().attachedFrontends(), ECrudOp.REMOVE, removeClassIds.first() );
      }
      if( newClassIds.size() == 1 ) {
        // Отправление события об создании класса для frontend
        fireWhenSysdescrChanged( backend().attachedFrontends(), ECrudOp.CREATE, newClassIds.first() );
      }
      if( updateClassIds.size() == 1 ) {
        // Отправление события об создании класса для frontend
        fireWhenSysdescrChanged( backend().attachedFrontends(), ECrudOp.EDIT, updateClassIds.first() );
      }
      IStringListEdit allClassIds = new StringLinkedBundleList();
      allClassIds.addAll( removeClassIds );
      allClassIds.addAll( newClassIds );
      allClassIds.addAll( updateClassIds );
      if( allClassIds.size() > 1 ) {
        fireWhenSysdescrChanged( backend().attachedFrontends(), ECrudOp.LIST, null );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkClassHierarchyExplorer
  //
  @Override
  public boolean isSuperclassOf( String aClassId, String aSubclassId ) {
    TsNullArgumentRtException.checkNulls( aClassId, aSubclassId );
    ISkClassInfo classInfo = sysdescrReader.findClassInfo( aSubclassId );
    while( classInfo.parent() != null ) {
      if( aClassId.equals( classInfo.parent().id() ) ) {
        return true;
      }
      classInfo = sysdescrReader.findClassInfo( classInfo.parent().id() );
    }
    return false;
  }

  @Override
  public boolean isAssignableFrom( String aClassId, String aSubclassId ) {
    TsNullArgumentRtException.checkNulls( aClassId, aSubclassId );
    return (aClassId.equals( aSubclassId ) || isSuperclassOf( aClassId, aSubclassId ));
  }

  @Override
  public boolean isSubclassOf( String aClassId, String aSuperclassId ) {
    TsNullArgumentRtException.checkNulls( aClassId, aSuperclassId );
    return isSuperclassOf( aSuperclassId, aClassId );
  }

  @Override
  public boolean isAssignableTo( String aClassId, String aSuperclassId ) {
    TsNullArgumentRtException.checkNulls( aClassId, aSuperclassId );
    return isAssignableFrom( aSuperclassId, aClassId );
  }

  @Override
  public boolean isOfClass( String aClassId, IStringList aClassIdsList ) {
    TsNullArgumentRtException.checkNulls( aClassId, aClassIdsList );
    for( String classId : aClassIdsList ) {
      if( classId.equals( aClassId ) && isSubclassOf( classId, aClassId ) ) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String findCommonRootClassId( IStringList aClassIds ) {
    TsNullArgumentRtException.checkNull( aClassIds );
    // search for first existing class ID
    ISkClassInfo h = null;
    for( String cid : aClassIds ) {
      h = sysdescrReader.findClassInfo( cid );
      if( h != null ) {
        break;
      }
    }
    if( h == null ) { // no exsiting class ID in argument
      return IGwHardConstants.GW_ROOT_CLASS_ID;
    }
    // iterate over superclasses of found class and determine which is common root
    IStringList ancestorIdLists = h.listSuperclasses( true ).ids();
    // from found class up to (but not incluring) root class
    for( int i = ancestorIdLists.size() - 1; i >= 1; i-- ) {
      String ansId = ancestorIdLists.get( i );
      if( isCommonSuperclass( ansId, aClassIds ) ) {
        return ansId;
      }
    }
    return IGwHardConstants.GW_ROOT_CLASS_ID;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
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
   * @param aClassInfo {@link IDtoClassInfo} описание класса
   * @param aDescendantClasses {@link IList}&lt;IDtoClassInfo&gt; классы-наследники (пустой для нового класса)
   * @return boolean <b>true</b> класс был создан; <b>false</b> класс был обновлен
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private boolean defineClass( IDtoClassInfo aClassInfo, IStridablesList<IDtoClassInfo> aDescendantClasses ) {
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
    // DtoClassInfo единственная реализация IDtoClassInfo
    DtoClassInfo classInfo = (DtoClassInfo)aClassInfo;
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
   * @return {@link IStridablesListEdit}&lt;{@link IDtoClassInfo}&gt; отфильтрованный список описаний классов
   * @throws TsNullArgumentRtException aClassInfos = null
   */
  @SuppressWarnings( "unchecked" )
  private static IStridablesList<IDtoClassInfo> filterClassInfos( IStridablesList<S5ClassEntity> aClassInfos,
      IStringList aClassIdsOrNull ) {
    TsNullArgumentRtException.checkNull( aClassInfos );
    IStridablesListEdit<IDtoClassInfo> retValue = new StridablesList<>();
    if( aClassIdsOrNull == null ) {
      retValue.setAll( (IStridablesList<IDtoClassInfo>)(Object)aClassInfos );
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
   * @param aInfos {@link IStridablesList}&lt; {@link IDtoClassInfo}&gt; список описаний классов
   * @return {@link IStridablesList} список классов в требуемом порядке
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static IStridablesList<IDtoClassInfo> orderByDependencies( IStridablesList<IDtoClassInfo> aInfos ) {
    TsNullArgumentRtException.checkNull( aInfos );
    IStridablesListEdit<IDtoClassInfo> remaining = new StridablesList<>( aInfos );
    IStridablesListEdit<IDtoClassInfo> retValue = new StridablesList<>();
    while( remaining.size() > 0 ) {
      for( IDtoClassInfo info : new StridablesList<>( remaining ) ) {
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

  private boolean isCommonSuperclass( String aAncestorId, IStringList aClassIds ) {
    for( String cid : aClassIds ) {
      if( sysdescrReader.findClassInfo( aAncestorId ) != null ) {
        ISkClassInfo h = sysdescrReader.getClassInfo( cid );
        if( !h.listSuperclasses( true ).ids().hasElem( aAncestorId ) ) {
          return false;
        }
      }
    }
    return true;
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
   * @param aOp {@link ECrudOp} тип операции над классами
   * @param aClassId String идентификатор класса или null для {@link ECrudOp#LIST}.
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static void fireWhenSysdescrChanged( IList<IS5FrontendRear> aFrontends, ECrudOp aOp, String aClassId ) {
    TsNullArgumentRtException.checkNulls( aFrontends, aOp );
    TsNullArgumentRtException.checkTrue( aOp != ECrudOp.LIST && aClassId == null );
    GtMessage message = IBaClassesMessages.makeMessage( aOp, aClassId );
    for( IS5FrontendRear frontend : aFrontends ) {
      frontend.onBackendMessage( message );
    }
  }

}
