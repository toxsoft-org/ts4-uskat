package org.toxsoft.uskat.dataquality.s5.supports;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.dataquality.lib.ISkDataQualityService.*;
import static org.toxsoft.uskat.dataquality.s5.supports.IS5Resources.*;
import static org.toxsoft.uskat.dataquality.s5.supports.S5DataQualityServiceUtils.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.*;

import org.infinispan.Cache;
import org.infinispan.commons.util.CloseableIterator;
import org.jboss.ejb.client.SessionID;
import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.DataType;
import org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants;
import org.toxsoft.core.tslib.av.metainfo.IDataType;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemLinkedList;
import org.toxsoft.core.tslib.coll.impl.ElemMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.gwids.ISkGwidService;
import org.toxsoft.uskat.dataquality.lib.ISkDataQualityTicket;
import org.toxsoft.uskat.dataquality.lib.impl.*;
import org.toxsoft.uskat.dataquality.s5.S5DataQualtiyValobjUtils;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionInterceptor;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionManager;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

/**
 * Реализация {@link IS5BackendDataQualitySingleton}.
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    PROJECT_INITIAL_IMPLEMENT_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@ConcurrencyManagement( ConcurrencyManagementType.BEAN )
// @ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendDataQualitySingleton
    extends S5BackendSupportSingleton
    implements IS5BackendDataQualitySingleton, IS5SessionInterceptor {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_DATA_QUALITY_ID = "S5BackendDataQualitySingleton"; //$NON-NLS-1$

  /**
   * JNDI-имя кэша установленных меток тикетов у данных
   */
  private static final String INFINISPAN_CACHE_MARKS = "java:jboss/infinispan/cache/s5caches/dataquality_marks"; //$NON-NLS-1$

  // /**
  // * JNDI-имя кэша идентификаторов данных по сессиям
  // */
  // private static final String INFINISPAN_CACHE_SESSIONS_GWIDS =
  // "java:jboss/infinispan/cache/s5caches/dataquality_sessions_gwids"; //$NON-NLS-1$

  /**
   * Менеджер сессий
   */
  @EJB
  private IS5SessionManager sessionManager;

  /**
   * Синглетон ядра
   */
  @EJB
  private IS5BackendCoreSingleton backendCore;

  // /**
  // * Поставщик локальных соединений с сервером
  // */
  // @EJB
  // private IS5LocalConnectionSingleton localConnectionProvider;
  //
  // /**
  // * Соединение с сервером. null: соединение не открывалось
  // */
  // private ISkConnection connection;

  /**
   * Служба обработки {@link Gwid}-идентификаторов
   */
  private ISkGwidService gwidService;

  /**
   * Встроенный тикет: нет связи с поставщиком данных
   */
  private ISkDataQualityTicket notConnectedTicket;

  /**
   * Список зарегистрированных тикетов
   */
  private final IStridablesListEdit<ISkDataQualityTicket> registeredTickets = new StridablesList<>();

  /**
   * Карта наборов установленых пометок тикетов по идентификаторам {@link Gwid}
   * <p>
   * Ключ: идентификатор ресурса (данного одного объекта) {@link Gwid}.<br>
   * Значение: набор пометок тикетов установленных для данного.
   */
  @Resource( lookup = INFINISPAN_CACHE_MARKS )
  private Cache<Gwid, Pair<Set<Skid>, IOptionSetEdit>> marksByGwidsCache;

  /**
   * Блокировка доступа к данным класса
   */
  private final S5Lockable lock = new S5Lockable();

  static {
    // Регистрация хранителей данных
    S5DataQualtiyValobjUtils.registerS5Keepers();
  }

  /**
   * Конструктор.
   */
  public S5BackendDataQualitySingleton() {
    super( BACKEND_DATA_QUALITY_ID, STR_D_BACKEND_DATA_QUALITY );
  }

  // ------------------------------------------------------------------------------------
  // Определение шаблонных методов S5BackendSupportSingleton
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void saveConfiguration( IOptionSet aConfiguration ) {
    TsNullArgumentRtException.checkNull( aConfiguration );
    super.saveConfiguration( aConfiguration );
    S5DataQualityTicketList prevTickets = IS5DataQualitySupportConfig.TICKETS.getValue( configuration() ).asValobj();
    S5DataQualityTicketList newTickets = IS5DataQualitySupportConfig.TICKETS.getValue( aConfiguration ).asValobj();
    if( !newTickets.equals( prevTickets ) ) {
      // Фактическое сохранение
      super.saveConfiguration( aConfiguration );
      // Изменение набора тикетов. Удаление тех IUgwi которые больше не отслеживаются
      updateTickets( newTickets );
    }
  }

  @Override
  protected void doInitSupport() {
    // TsIllegalStateRtException.checkNoNull( connection );
    // connection = createSynchronizedConnection( localConnectionProvider.open( id() ) );
    // gwidService = connection.coreApi().gwidService();

    // Установка встроенных тикетов
    notConnectedTicket =
        addBuiltInTicket( TICKET_ID_NO_CONNECTION, STR_D_NOT_CONNECTED, STR_N_NOT_CONNECTED, BOOLEAN, avBool( true ) );
    // Загрузка конфигурации
    IOptionSet config = configuration();
    S5DataQualityTicketList tickets = IS5DataQualitySupportConfig.TICKETS.getValue( config ).asValobj();
    updateTickets( tickets );

    // Установка перехватчика событий сессий пользователей
    IS5SessionInterceptor sessionInterceptor = sessionContext().getBusinessObject( IS5SessionInterceptor.class );
    sessionManager.addSessionInterceptor( sessionInterceptor, 100 );
  }

  @Override
  protected void doCloseSupport() {
    IS5SessionInterceptor sessionInterceptor = sessionContext().getBusinessObject( IS5SessionInterceptor.class );
    sessionManager.removeSessionInterceptor( sessionInterceptor );
    // if( connection != null ) {
    // connection.close();
    // connection = null;
    // }
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkBackendAddonDataQuality
  //
  @Override
  public IOptionSet getResourceMarks( Gwid aResource ) {
    TsNullArgumentRtException.checkNull( aResource );
    checkDataGwid( aResource );
    if( aResource.isMulti() ) {
      // Ресурс указанный через групповой идентификатор(Gwid.isMulti() == true) не допускается
      throw new TsIllegalArgumentRtException( ERR_MULTI_RESOURCE_NOT_ALLOWED, aResource );
    }
    lockRead( lock );
    try {
      Pair<Set<Skid>, IOptionSetEdit> retValue = marksByGwidsCache.get( aResource );
      if( retValue == null ) {
        // В данный момент с ресурсом не установлена связь
        IOptionSetEdit marks = new OptionSet();
        // Установка маркера "нет связи" не требуется, так значение по умолчанию = true
        return marks;
      }
      return retValue.right();
    }
    finally {
      unlockRead( lock );
    }
  }

  @Override
  public IMap<Gwid, IOptionSet> getResourcesMarks( IGwidList aResources ) {
    TsNullArgumentRtException.checkNull( aResources );
    IMapEdit<Gwid, IOptionSet> retValue = new ElemMap<>();
    // Разгруппировка идентификатора. true: проверка существования
    IGwidList gwids = ungroupGwids( gwidService(), aResources, true );
    for( Gwid gwid : gwids ) {
      IOptionSet marks = getResourceMarks( gwid );
      retValue.put( gwid, marks );
    }
    return retValue;
  }

  @Override
  public IMap<Gwid, IOptionSet> queryMarkedUgwies( ITsCombiFilterParams aQueryParams ) {
    TsNullArgumentRtException.checkNull( aQueryParams );
    // TODO Auto-generated method stub
    throw new TsUnderDevelopmentRtException();
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  public GwidList getConnectedResources( Skid aSessionID ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    IListEdit<Gwid> retValue = new ElemLinkedList<>();
    try( CloseableIterator<Entry<Gwid, Pair<Set<Skid>, IOptionSetEdit>>> iterator =
        marksByGwidsCache.entrySet().iterator() ) {
      while( iterator.hasNext() ) {
        Entry<Gwid, Pair<Set<Skid>, IOptionSetEdit>> entry = iterator.next();
        if( aSessionID == Skid.NONE || entry.getValue().left().contains( aSessionID ) ) {
          retValue.add( entry.getKey() );
        }
      }
    }
    return new GwidList( retValue );
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void addConnectedResources( Skid aSessionID, IGwidList aResources ) {
    TsNullArgumentRtException.checkNulls( aSessionID, aResources );
    // Признак необходимости формирования события
    boolean needSendEvent = false;
    lockWrite( lock );
    try {
      needSendEvent = addResources( aSessionID, aResources );
    }
    finally {
      unlockWrite( lock );
    }
    Integer size = Integer.valueOf( aResources.size() );
    Boolean needEvent = Boolean.valueOf( needSendEvent );
    logger().info( MSG_ADD_SESSION_RESOURCES, aSessionID, size, needEvent );
    if( needSendEvent ) {
      fireResourceChangedEvent( backend().attachedFrontends(), notConnectedTicket.id() );
    }
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void removeConnectedResources( Skid aSessionID, IGwidList aResources ) {
    // Признак необходимости формирования события
    boolean needSendEvent = false;
    lockWrite( lock );
    try {
      needSendEvent = removeResources( aSessionID, aResources );
    }
    finally {
      unlockWrite( lock );
    }
    Integer size = Integer.valueOf( aResources.size() );
    Boolean needEvent = Boolean.valueOf( needSendEvent );
    logger().info( MSG_REMOVE_SESSION_RESOURCES, aSessionID, size, needEvent );
    if( needSendEvent ) {
      fireResourceChangedEvent( backend().attachedFrontends(), notConnectedTicket.id() );
    }
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public IGwidList setConnectedResources( Skid aSessionID, IGwidList aResources ) {
    TsNullArgumentRtException.checkNulls( aSessionID );
    IGwidList retValue = IGwidList.EMPTY;
    // Признак необходимости формирования события
    boolean needSendEvent = false;
    lockWrite( lock );
    try {
      retValue = getConnectedResources( aSessionID );
      needSendEvent = removeResources( aSessionID, retValue );
      needSendEvent |= addResources( aSessionID, aResources );
    }
    finally {
      unlockWrite( lock );
    }
    Integer prevSize = Integer.valueOf( retValue.size() );
    Integer newSize = Integer.valueOf( aResources.size() );
    Boolean needEvent = Boolean.valueOf( needSendEvent );
    logger().info( MSG_SET_SESSION_RESOURCES, aSessionID, prevSize, newSize, needEvent );
    if( needSendEvent ) {
      fireResourceChangedEvent( backend().attachedFrontends(), notConnectedTicket.id() );
    }
    return retValue;
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void setMarkValue( String aTicketId, IAtomicValue aValue, IGwidList aResources ) {
    TsNullArgumentRtException.checkNulls( aTicketId, aValue, aResources );
    StridUtils.checkValidIdPath( aTicketId );
    // Признак необходимости формирования события
    boolean needSendEvent = false;
    lockWrite( lock );
    try {
      ISkDataQualityTicket ticket = registeredTickets.findByKey( aTicketId );
      if( ticket == null ) {
        // Тикет не зарегистрирован
        throw new TsIllegalArgumentRtException( ERR_TICKET_NOT_FOUND, aTicketId );
      }
      if( ticket.isBuiltin() ) {
        // Попытка установки метки встроенного тикета
        throw new TsIllegalArgumentRtException( ERR_MARK_BUILTIN_TICKET, aTicketId );
      }
      EAtomicType valueType = aValue.atomicType();
      EAtomicType ticketType = ticket.dataType().atomicType();
      if( valueType != NONE && ticketType != NONE && valueType != ticketType ) {
        // Недопустимое значение метки для тикета
        throw new TsIllegalArgumentRtException( ERR_WRONG_TICKET_MARK, aValue, valueType, aTicketId, ticketType );
      }
      // Разгруппировка ugwi. true: проверка существования классов, объектов и данных
      IGwidList gwids = ungroupGwids( gwidService(), aResources, true );
      // Сессия
      SessionID sessionID = null;
      // Журналирование
      logger().info( MSG_SET_MARK, aTicketId, aValue, Integer.valueOf( aResources.size() ), sessionID,
          toString( aResources ) );
      // Проход по всем ресурсам с установкой тикета значения тикета
      for( Gwid gwid : gwids ) {
        Pair<Set<Skid>, IOptionSetEdit> entry = marksByGwidsCache.get( gwid );
        if( entry == null ) {
          // Игнорирование попытки установить тикет для ресурса с которым нет связи
          logger().warning( ERR_IGNORE_TICKET_FOR_UNDEF_RESOURCE, aTicketId, aValue, gwid );
          continue;
        }
        // Установка значения в наборе IOptionSetEdit
        entry.right().setValue( ticket.id(), aValue );
        // Обновление кэша
        marksByGwidsCache.put( gwid, entry );
        // Требование отправки события
        needSendEvent = true;
        // Журналирование
        logger().debug( MSG_SET_MARK_RESOURCE, gwid, aTicketId, aValue, sessionID );
      }
    }
    finally {
      unlockWrite( lock );
    }
    if( needSendEvent ) {
      fireResourceChangedEvent( backend().attachedFrontends(), aTicketId );
    }
  }

  @Override
  public IStridablesList<ISkDataQualityTicket> listTickets() {
    lockRead( lock );
    try {
      return new StridablesList<>( registeredTickets );
    }
    finally {
      unlockRead( lock );
    }
  }

  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public ISkDataQualityTicket defineTicket( String aTicketId, String aName, String aDescription, IDataType aDataType ) {
    TsNullArgumentRtException.checkNulls( aTicketId, aName, aDescription, aDataType );
    lockWrite( lock );
    try {
      // Проверка попытки редактирования встроенного тикета
      ISkDataQualityTicket ticket = registeredTickets.findByKey( aTicketId );
      if( ticket != null && ticket.isBuiltin() ) {
        throw new TsIllegalArgumentRtException( ERR_EDIT_BUILTIN_TICKET, ticket.id() );
      }
      ISkDataQualityTicket newTicket = new SkDataQualityTicket( aTicketId, aName, aDescription, aDataType );
      // Сохранение конфигурации
      IOptionSetEdit config = new OptionSet( configuration() );
      S5DataQualityTicketList tickets = IS5DataQualitySupportConfig.TICKETS.getValue( config ).asValobj();
      tickets.add( newTicket );
      IS5DataQualitySupportConfig.TICKETS.setValue( config, avValobj( tickets ) );
      // Сохранение настроек в базе данных
      saveConfiguration( config );
      return newTicket;
    }
    finally {
      unlockWrite( lock );
    }
  }

  @Override
  public void removeTicket( String aTicketId ) {
    StridUtils.checkValidIdPath( aTicketId );
    lockWrite( lock );
    try {
      // Проверка попытки редактирования встроенного тикета
      ISkDataQualityTicket ticket = registeredTickets.findByKey( aTicketId );
      if( ticket != null && ticket.isBuiltin() ) {
        throw new TsIllegalArgumentRtException( ERR_EDIT_BUILTIN_TICKET, ticket.id() );
      }
      // Сохранение конфигурации
      IOptionSetEdit config = new OptionSet( configuration() );
      S5DataQualityTicketList tickets = IS5DataQualitySupportConfig.TICKETS.getValue( config ).asValobj();
      registeredTickets.removeById( aTicketId );
      IS5DataQualitySupportConfig.TICKETS.setValue( config, avValobj( tickets ) );
      // Сохранение настроек в базе данных
      saveConfiguration( config );
    }
    finally {
      unlockWrite( lock );
    }
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SessionInterceptor
  //
  @TransactionAttribute( TransactionAttributeType.MANDATORY )
  @Lock( LockType.READ )
  @Override
  public void beforeCreateSession( Skid aSessionID ) {
    // nop
  }

  @TransactionAttribute( TransactionAttributeType.MANDATORY )
  @Lock( LockType.READ )
  @Override
  public void afterCreateSession( Skid aSessionID ) {
    // nop
  }

  @TransactionAttribute( TransactionAttributeType.MANDATORY )
  @Lock( LockType.READ )
  @Override
  public void beforeCloseSession( Skid aSessionID ) {
    // nop
  }

  // 2020-12-21 mvk аннотации верные ???
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void afterCloseSession( Skid aSessionID ) {
    logger().info( MSG_AFTER_CLOSE_SESSION, aSessionID );
    setConnectedResources( aSessionID, IGwidList.EMPTY );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Регистрация встроенного тикета
   *
   * @param aId String идентификатор тикета (ИД-путь)
   * @param aDescr String описание тикета
   * @param aName String имя тикета
   * @param aAtomicType {@link EAtomicType} атомарный тип тикета
   * @param aDefaultValue {@link IAtomicValue} значение по умолчанию тикета
   * @return {@link ISkDataQualityTicket} зарегстированный тикет
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException идентификатор тикета не ИД-путь
   * @throws TsIllegalArgumentRtException тип значения по умолчанию не согласуется с типом тикета
   */
  private ISkDataQualityTicket addBuiltInTicket( String aId, String aDescr, String aName, EAtomicType aAtomicType,
      IAtomicValue aDefaultValue ) {
    TsNullArgumentRtException.checkNulls( aId, aDescr, aName, aAtomicType, aDefaultValue );
    StridUtils.checkValidIdPath( aId );
    EAtomicType defaultValueType = aDefaultValue.atomicType();
    if( defaultValueType != NONE && aAtomicType != NONE && defaultValueType != aAtomicType ) {
      // Тип значения по умолчанию не согласуется с типом тикета
      throw new TsIllegalArgumentRtException();
    }
    IOptionSetEdit contstraints = new OptionSet();
    contstraints.setValue( IAvMetaConstants.TSID_DEFAULT_VALUE, aDefaultValue );
    IDataType type = new DataType( aAtomicType, contstraints );
    // true: встроенный тикет
    ISkDataQualityTicket retValue = new SkDataQualityTicket( aId, aDescr, aName, type, true );
    registeredTickets.add( retValue );
    return retValue;
  }

  /**
   * Обновляет список зарегистрированных тикетов
   *
   * @param aTickets {@link S5DataQualityTicketList} список зарегистрированных тикетов
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void updateTickets( S5DataQualityTicketList aTickets ) {
    TsNullArgumentRtException.checkNull( aTickets );
    // Признак необходимости формирования события
    boolean needSendEvent = false;
    // Блокировка доступа
    lockWrite( lock );
    try {
      // Удаление тех тикетов которых больше нет
      for( ISkDataQualityTicket ticket : new StridablesList<>( registeredTickets ) ) {
        if( ticket.isBuiltin() ) {
          // Встроенный тикет игнорируется
          continue;
        }
        if( !aTickets.hasElem( ticket ) ) {
          // Удаление меток тикета со всех ресурсов
          IListEdit<Entry<Gwid, Pair<Set<Skid>, IOptionSetEdit>>> updatedEntries = new ElemLinkedList<>();
          try( CloseableIterator<Entry<Gwid, Pair<Set<Skid>, IOptionSetEdit>>> iterator =
              marksByGwidsCache.entrySet().iterator() ) {
            while( iterator.hasNext() ) {
              Entry<Gwid, Pair<Set<Skid>, IOptionSetEdit>> entry = iterator.next();
              if( entry.getValue().right().removeByKey( ticket.id() ) != null ) {
                updatedEntries.add( entry );
              }
            }
          }
          for( Entry<Gwid, Pair<Set<Skid>, IOptionSetEdit>> entry : updatedEntries ) {
            marksByGwidsCache.put( entry.getKey(), entry.getValue() );
          }
          // Тикет больше незарегистрирован
          registeredTickets.remove( ticket );
          // Требуем отправки события об изменении
          needSendEvent = true;
          // Журналирование
          logger().debug( MSG_UNREGISTER_TICKET, ticket.id() );
        }
      }
      // Добавление новых тикетов
      for( ISkDataQualityTicket ticket : aTickets ) {
        if( ticket.isBuiltin() ) {
          // Встроенный тикет игнорируется (добавляется напрямую)
          continue;
        }
        if( !registeredTickets.hasElem( ticket ) ) {
          // Регистрация нового тикета
          registeredTickets.add( ticket );
          // Требуем отправки события об изменении
          needSendEvent = true;
          // Журналирование
          logger().debug( MSG_REGISTER_TICKET, ticket.id() );
        }
      }
    }
    finally {
      unlockWrite( lock );
    }
    if( needSendEvent ) {
      fireTicketsChangedEvent( backend().attachedFrontends() );
    }
  }

  /**
   * Добавляет ресурсы в сессию
   *
   * @param aSessionID {@link Skid} идентификатор сессии
   * @param aResources {@link IGwidList} добавляемые ресурсы сессии
   * @return boolean <b>true</b> были добавлены ресурсы; <b>false</b> не были добавлены ресурсы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private boolean addResources( Skid aSessionID, IGwidList aResources ) {
    TsNullArgumentRtException.checkNulls( aSessionID, aResources );
    if( aResources.size() == 0 ) {
      // Нет ресурсов для регистрации
      return false;
    }
    // Разгруппировка Gwid. true: проверка существования классов, объектов и данных
    IGwidList gwids = ungroupGwids( gwidService(), aResources, true );
    // Текущий список ресурсов сессии
    GwidList connectedResources = getConnectedResources( aSessionID );
    // Список фактически добавляемых данных
    GwidList addedGwids = new GwidList();
    // Обновление кэша ресурсов сессии
    for( Gwid gwid : gwids ) {
      if( !connectedResources.hasElem( gwid ) ) {
        connectedResources.add( gwid );
        addedGwids.add( gwid );
      }
    }
    if( addedGwids.size() == 0 ) {
      return false;
    }
    // Регистрация поставщика ресурсов
    logger().info( MSG_SET_CONNECTED_VENDOR, Integer.valueOf( aResources.size() ), aSessionID, toString( aResources ) );
    // Идентификатор тикета "NotConnected"
    String ticketId = notConnectedTicket.id();
    // Проход по всем ресурсам с установкой тикета NotConnected = false
    for( Gwid gwid : addedGwids ) {
      Pair<Set<Skid>, IOptionSetEdit> entry = marksByGwidsCache.get( gwid );
      if( entry == null ) {
        entry = new Pair<>( new HashSet<>(), new OptionSet() );
      }
      // Добавление идентификатора сессии поставлямая значения данных
      entry.left().add( aSessionID );
      // Установка значения метки "notConnected"
      entry.right().setValue( notConnectedTicket.id(), AV_FALSE );
      // Обновление кэша
      marksByGwidsCache.put( gwid, entry );
      // Журналирование
      logger().debug( MSG_SET_MARK_RESOURCE, gwid, ticketId, AV_FALSE, aSessionID );
    }
    return true;
  }

  /**
   * Удаляет ресурсы из сессии
   *
   * @param aSessionID {@link Skid} идентификатор сессии
   * @param aResources {@link IGwidList} удаляемые ресурсы сессии
   * @return boolean <b>true</b> были удалены ресурсы; <b>false</b> не были удалены ресурсы
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private boolean removeResources( Skid aSessionID, IGwidList aResources ) {
    TsNullArgumentRtException.checkNulls( aSessionID, aResources );
    // Текущий список ресурсов сессии
    GwidList connectedResources = getConnectedResources( aSessionID );
    if( connectedResources.size() == 0 ) {
      // Сессия не регистрировала ресурсы
      logger().debug( MSG_NO_RESOURCE_SESSION, aSessionID );
      return false;
    }
    // Разгруппировка ugwi. false: БЕЗ проверки существования классов, объектов и данных
    IGwidList gwids = ungroupGwids( gwidService(), aResources, false );

    // Список фактически удаляемых данных
    GwidList removedGwids = new GwidList();
    // Проход по всем данным с удалением указанных
    for( Gwid gwid : gwids ) {
      if( connectedResources.hasElem( gwid ) ) {
        connectedResources.remove( gwid );
        removedGwids.add( gwid );
      }
    }
    if( removedGwids.size() == 0 ) {
      return false;
    }
    // Идентификатор тикета "NotConnected"
    String ticketId = notConnectedTicket.id();
    // Журналирование
    logger().info( MSG_REMOVE_MARK, ticketId, Integer.valueOf( removedGwids.size() ), aSessionID );
    for( Gwid gwid : removedGwids ) {
      Pair<Set<Skid>, IOptionSetEdit> entry = marksByGwidsCache.get( gwid );
      if( entry == null ) {
        // Ресурс уже дерегистрирован
        logger().warning( ERR_RESOURCE_ALREADY_UNREG, gwid );
        continue;
      }
      // Добавление идентификатора сессии поставлямая значения данных
      entry.left().remove( aSessionID );
      if( entry.left().size() == 0 ) {
        // Ресурс больше не предоставляется сессиями клиентов
        marksByGwidsCache.remove( gwid );
        // Журналирование
        logger().debug( MSG_REMOVE_MARK_RESOURCE, gwid, ticketId, aSessionID );
        continue;
      }
      // Значение ресурса предоставляется другими сессиями
      entry.right().setValue( notConnectedTicket.id(), AV_FALSE );
      // Обновление кэша
      marksByGwidsCache.put( gwid, entry );
      // Журналирование
      logger().debug( MSG_SUPPLY_BY_OTHER_SESSIONS, gwid, ticketId, Integer.valueOf( entry.left().size() ) );
    }
    return true;
  }

  /**
   * Возвращает службу обработки {@link Gwid}-идентификаторов
   *
   * @return {@link ISkGwidService} служба {@link Gwid}-идентификаторов
   */
  private ISkGwidService gwidService() {
    if( gwidService == null ) {
      gwidService = backendCore.getConnection().coreApi().gwidService();
    }
    return TsInternalErrorRtException.checkNull( gwidService );
  }

  /**
   * Формирует событие "изменение состояния ресурса"
   *
   * @param aFrontends {@link IList}&lt;{@link IS5FrontendRear}&gt; список фронтендов
   * @param aResourceId String идентификатор ресурса
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void fireResourceChangedEvent( IList<IS5FrontendRear> aFrontends, String aResourceId ) {
    TsNullArgumentRtException.checkNulls( aFrontends, aResourceId );
    GtMessage msg = SkDataQualityMsgResourceChanged.INSTANCE.makeMessage( aResourceId );
    for( IS5FrontendRear frontend : aFrontends ) {
      frontend.onBackendMessage( msg );
    }
  }

  /**
   * Формирует событие "изменение состояния ярлыков"
   *
   * @param aFrontends {@link IList}&lt;{@link IS5FrontendRear}&gt; список фронтендов
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  private static void fireTicketsChangedEvent( IList<IS5FrontendRear> aFrontends ) {
    TsNullArgumentRtException.checkNulls( aFrontends );
    GtMessage msg = SkDataQualityMsgTicketsChanged.INSTANCE.makeMessage();
    for( IS5FrontendRear frontend : aFrontends ) {
      frontend.onBackendMessage( msg );
    }
  }

  /**
   * Возвращает строку представляющую значения текущих данных
   *
   * @param aResources {@link IGwidList} список идентификаторов ресурсов
   * @return String строка представления значений текущих данных
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static String toString( IGwidList aResources ) {
    TsNullArgumentRtException.checkNulls( aResources );
    StringBuilder sb = new StringBuilder();
    for( Gwid gwid : aResources ) {
      sb.append( String.format( MSG_GWID, gwid ) );
    }
    return sb.toString();
  }
}
