package org.toxsoft.uskat.alarms.s5.supports;

import static org.toxsoft.uskat.alarms.s5.supports.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.*;
import javax.persistence.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.filter.*;
import org.toxsoft.core.tslib.bricks.filter.impl.TsCombiFilter;
import org.toxsoft.core.tslib.bricks.filter.impl.TsFilterFactoriesRegistry;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.alarms.lib.*;
import org.toxsoft.uskat.alarms.lib.flacon.ISkAlarmFlacon;
import org.toxsoft.uskat.alarms.lib.impl.*;
import org.toxsoft.uskat.alarms.s5.S5AlarmValobjUtils;
import org.toxsoft.uskat.alarms.s5.addons.S5BaAlarmData;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;

/**
 * Реализация {@link IS5BackendAlarmSingleton}.
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
public class S5BackendAlarmSingleton
    extends S5BackendSupportSingleton
    implements IS5BackendAlarmSingleton {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_ALARMS_ID = "S5BackendAlarmSingleton"; //$NON-NLS-1$

  /**
   * Менеджер постоянства
   */
  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Реестр фильтров алармов
   */
  private static final ITsFilterFactoriesRegistry<ISkAlarm> FILTER_REGISTRY =
      new TsFilterFactoriesRegistry<>( ISkAlarm.class );

  static {
    // Регистрация хранителей данных
    S5AlarmValobjUtils.registerS5Keepers();
    // Регистрация фильтров
    SkAlarmUtils.registerAlarmFilters( FILTER_REGISTRY );
  }

  /**
   * Конструктор.
   */
  public S5BackendAlarmSingleton() {
    super( BACKEND_ALARMS_ID, STR_D_BACKEND_ALARMS );
  }

  // ------------------------------------------------------------------------------------
  // Определение шаблонных методов S5BackendSupportSingleton
  //
  @Override
  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  public void saveConfiguration( IOptionSet aConfiguration ) {
    TsNullArgumentRtException.checkNull( aConfiguration );
    super.saveConfiguration( aConfiguration );
  }

  @Override
  protected void doInitSupport() {
    // nop
  }

  @Override
  protected void doCloseSupport() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BackendAlarmSingleton
  //
  @Override
  public IStridablesList<ISkAlarmDef> listAlarmDefs() {
    // Выполнение запроса
    TypedQuery<S5AlarmDefEntity> query =
        entityManager.createNamedQuery( S5AlarmEntitiesUtils.QUERY_GET, S5AlarmDefEntity.class );
    List<S5AlarmDefEntity> entities = query.getResultList();
    IStridablesListEdit<ISkAlarmDef> retValue = new StridablesList<>();
    for( S5AlarmDefEntity entity : entities ) {
      retValue.add( entity );
    }
    return retValue;
  }

  @Override
  public ISkAlarmDef findAlarmDef( String aAlarmDefId ) {
    // Проверка контракта
    TsNullArgumentRtException.checkNull( aAlarmDefId );
    // Выполнение запроса
    return entityManager.find( S5AlarmDefEntity.class, aAlarmDefId );
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void registerAlarmDef( ISkAlarmDef aSkAlarmDef ) {
    // Проверка контракта
    TsNullArgumentRtException.checkNull( aSkAlarmDef );
    // Создаем сущность для хранения в БД
    S5AlarmDefEntity alarmDefEntity = new S5AlarmDefEntity( aSkAlarmDef );
    entityManager.persist( alarmDefEntity );
    entityManager.flush();
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public ISkAlarm generateAlarm( String aAlarmDefId, Skid aAuthorId, Skid aUserId, byte aSublevel,
      ISkAlarmFlacon aSkAlarmFlacon ) {
    ISkAlarmDef skAlarmDef = findAlarmDef( aAlarmDefId );
    // Проверяем выполнение контракта
    TsNullArgumentRtException.checkNulls( aAlarmDefId, skAlarmDef, aSkAlarmFlacon );
    long timestamp = System.currentTimeMillis();
    long alarmId = generateAlarmId();
    // Создаем аларм
    S5AlarmEntity alarmEntity = new S5AlarmEntity( //
        timestamp, //
        alarmId, //
        skAlarmDef.priority(), //
        aSublevel, //
        aAuthorId, //
        aUserId, //
        aAlarmDefId, //
        skAlarmDef.message(), //
        new S5AlarmFlaconEntity( aSkAlarmFlacon )//
    );

    // Сохраняем аларм в БД
    entityManager.persist( alarmEntity );
    entityManager.flush();

    // Количество клиентов получивших сообщение
    int listenersCount = 0;

    // Сообщение об аларме
    GtMessage alarmMessage = SkAlarmMsgIsOn.INSTANCE.makeMessage( alarmEntity );
    // Проход по всем фронтендам и передача сообщения об аларме
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      // Данные расширения для сессии
      S5BaAlarmData baData = frontend.frontendData().findBackendAddonData( IBaAlarms.ADDON_ID, S5BaAlarmData.class );
      if( baData == null ) {
        // фронтенд не поддерживает обработку алармов
        continue;
      }
      synchronized (baData) {
        for( ITsCombiFilterParams filterParams : baData.alarmFilters ) {
          ITsFilter<ISkAlarm> filter = TsCombiFilter.create( filterParams, FILTER_REGISTRY );
          if( filter.accept( alarmEntity ) ) {
            frontend.onBackendMessage( alarmMessage );
            break;
          }
        }
      }
    }
    // История обработки
    if( listenersCount == 0 ) {
      // Нет заинтересованных, отмечаем это в истории обработки и гасим аларм и сохраняем его для истории
      ISkAlarmThreadHistoryItem histItem =
          new SkAlarmThreadHistoryItem( timestamp, ISkAlarmThreadHistoryItem.ALARM_THREAD_NULL, IOptionSet.NULL );
      alarmEntity.addHistoryItem( histItem );
      entityManager.persist( alarmEntity );
      entityManager.flush();
    }
    return alarmEntity;
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void addAnnounceThreadHistoryItem( long aAlarmId, ISkAlarmThreadHistoryItem aItem ) {
    // находим нужный аларм
    S5AlarmEntity alarm = entityManager.find( S5AlarmEntity.class, Long.valueOf( aAlarmId ) );
    // Создаем сущность для хранения
    S5AlarmThreadHistoryItemEntity item = new S5AlarmThreadHistoryItemEntity( Long.valueOf( aItem.timestamp() ),
        aItem.announceThreadId(), aItem.params() );
    alarm.addHistoryItem( item );
    // Изменение состояния тревоги в базе данных
    entityManager.merge( alarm );
    entityManager.flush();

    // Сообщение об аларме
    GtMessage alarmMessage = SkAlarmMsgStateChanged.INSTANCE.makeMessage( alarm, item );
    // Проход по всем фронтендам и передача сообщения об аларме
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      // Данные расширения для сессии
      S5BaAlarmData baData = frontend.frontendData().findBackendAddonData( IBaAlarms.ADDON_ID, S5BaAlarmData.class );
      if( baData == null ) {
        // фронтенд не поддерживает обработку алармов
        continue;
      }
      synchronized (baData) {
        for( ITsCombiFilterParams filterParams : baData.alarmFilters ) {
          ITsFilter<ISkAlarm> filter = TsCombiFilter.create( filterParams, FILTER_REGISTRY );
          if( filter.accept( alarm ) ) {
            frontend.onBackendMessage( alarmMessage );
            break;
          }
        }
      }
    }
  }

  @Override
  public ITimedList<ISkAlarm> queryAlarms( ITimeInterval aTimeInterval, ITsCombiFilterParams aFilter ) {
    TimedList<ISkAlarm> retVal = new TimedList<>();
    // Запрос на данные
    Query query = entityManager.createNamedQuery( "S5AlarmEntity.queryAlarms" ); //$NON-NLS-1$
    query.setParameter( "startTime", Long.valueOf( aTimeInterval.startTime() ) ); //$NON-NLS-1$
    query.setParameter( "endTime", Long.valueOf( aTimeInterval.endTime() ) ); //$NON-NLS-1$
    // Фильтр на результаты запроса
    ITsFilter<ISkAlarm> filter = TsCombiFilter.create( aFilter, FILTER_REGISTRY );
    // Составляем список алармов
    for( Iterator<?> iterator = query.getResultList().iterator(); iterator.hasNext(); ) {
      S5AlarmEntity alarmEntity = (S5AlarmEntity)iterator.next();
      if( filter.accept( alarmEntity ) ) {
        retVal.add( alarmEntity );
        // retVal.add( new SkAlarm( //
        // alarmEntity.timestamp(), //
        // alarmEntity.alarmId(), //
        // alarmEntity.priority(), //
        // alarmEntity.sublevel(), //
        // alarmEntity.authorId(), //
        // alarmEntity.userId(), alarmEntity.alarmDefId(), //
        // alarmEntity.message(), //
        // alarmEntity.flacon(), //
        // alarmEntity.history() //
        // ) //
        // )
      }
    }
    return retVal;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Создает уникальный id аларма
   *
   * @return id аларма
   */
  private static long generateAlarmId() {
    // TODO сделать более осмысленный алгоритм
    return System.currentTimeMillis();
  }
}
