package org.toxsoft.uskat.sysext.alarms.supports;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.sysext.alarms.api.ISkAnnounceThreadHistoryItem.*;
import static org.toxsoft.uskat.sysext.alarms.supports.ISkResources.*;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ejb.*;
import javax.persistence.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.filter.ITsCombiFilterParams;
import org.toxsoft.core.tslib.bricks.filter.ITsFilter;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendSupportSingleton;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionInterceptor;
import org.toxsoft.uskat.sysext.alarms.addon.SkAlarmFrontendData;
import org.toxsoft.uskat.sysext.alarms.api.*;
import org.toxsoft.uskat.sysext.alarms.api.flacon.ISkAlarmFlacon;
import org.toxsoft.uskat.sysext.alarms.impl.*;

/**
 * Реализация {@link ISkBackendAlarmsSingleton}.
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    PROJECT_INITIAL_SYSDESCR_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
@ConcurrencyManagement( ConcurrencyManagementType.BEAN )
// @ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class SkBackendAlarmsSingleton
    extends S5BackendSupportSingleton
    implements ISkBackendAlarmsSingleton, IS5SessionInterceptor {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String BACKEND_ALARMS_ID = "SkBackendAlarmsSingleton"; //$NON-NLS-1$

  @PersistenceContext
  private EntityManager entityManager;

  private FilterFactoriesRegistry filterFactoryRegistry = new FilterFactoriesRegistry();

  /**
   * Конструктор.
   */
  public SkBackendAlarmsSingleton() {
    super( BACKEND_ALARMS_ID, STR_D_BACKEND_ALARMS );
    SkAlarmUtils.registerAlarmFilters( filterFactoryRegistry );
  }

  // ------------------------------------------------------------------------------------
  // Определение шаблонных методов S5BackendSupportSingleton
  //
  @Override
  protected void doInitSupport() {
    // nop
  }

  @Override
  protected void doCloseSupport() {
    // nop
  }

  @Override
  public void beforeCreateSession( Skid aSessionID ) {
    // nop
  }

  @Override
  public void afterCreateSession( Skid aSessionID ) {
    // nop
  }

  @Override
  public void beforeCloseSession( Skid aSessionID ) {
    // nop
  }

  @Override
  public void afterCloseSession( Skid aSessionID ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ISkBackendAddonAlarm
  //
  @Override
  public IStridablesList<ISkAlarmDef> listAlarmDefs() {
    // Выполнение запроса
    TypedQuery<SkAlarmDefEntity> query =
        entityManager.createNamedQuery( SkAlarmEntitiesUtils.QUERY_GET, SkAlarmDefEntity.class );
    List<SkAlarmDefEntity> entities = query.getResultList();
    IStridablesListEdit<ISkAlarmDef> retValue = new StridablesList<>();
    for( SkAlarmDefEntity entity : entities ) {
      retValue.add( entity );
    }
    return retValue;
  }

  @Override
  public ISkAlarmDef findAlarmDef( String aAlarmDefId ) {
    // Проверка контракта
    TsNullArgumentRtException.checkNull( aAlarmDefId );
    // Выполнение запроса
    return entityManager.find( SkAlarmDefEntity.class, aAlarmDefId );
  }

  @Override
  public void registerAlarmDef( ISkAlarmDef aSkAlarmDef ) {
    // Проверка контракта
    TsNullArgumentRtException.checkNull( aSkAlarmDef );
    // Создаем сущность для хранения в БД
    SkAlarmDefEntity alarmDefEntity = new SkAlarmDefEntity( aSkAlarmDef );
    entityManager.persist( alarmDefEntity );
    entityManager.flush();
  }

  @Override
  public ISkAlarm generateAlarm( String aAlarmDefId, Skid aAuthorId, Skid aUserId, byte aSublevel,
      ISkAlarmFlacon aSkAlarmFlacon ) {
    ISkAlarmDef skAlarmDef = findAlarmDef( aAlarmDefId );
    // Проверяем выполнение контракта
    TsNullArgumentRtException.checkNulls( aAlarmDefId, skAlarmDef, aSkAlarmFlacon );
    long timestamp = System.currentTimeMillis();
    long alarmId = generateAlarmId();
    // Создаем аларм
    SkAlarm alarm = new SkAlarm( timestamp, alarmId, skAlarmDef.priority(), aSublevel, aAuthorId, aUserId, aAlarmDefId,
        skAlarmDef.message() );
    // Создаем сразу же сущность для хранения в БД
    SkAlarmEntity alarmEntity = new SkAlarmEntity( alarm );
    // устанавливаем его флакон
    SkFlaconEntity flaconEntity = new SkFlaconEntity( aSkAlarmFlacon );
    alarmEntity.setFlacon( flaconEntity );

    // Сохраняем аларм в БД
    entityManager.persist( alarmEntity );
    entityManager.flush();

    // Количество клиентов получивших сообщение
    int listenersCount = 0;
    // Передача сообщений о тревоге клиентам
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      SkAlarmFrontendData frontendData = SkAlarmFrontendData.getFromFrontend( frontend );
      if( frontendData == null ) {
        // фронтенд не поддерживает тревоги
        continue;
      }
      for( IPolyFilterParams filterParams : frontendData.alarmFilterParams ) {
        IPolyFilter filter = PolyFilter.create( filterParams, filterFactoryRegistry.registeredFactories() );
        if( filter == IPolyFilter.NULL || filter.accept( alarm ) == true ) {
          SkAlarmMessageWhenOn.send( frontend, alarm );
          listenersCount++;
          break;
        }
      }
    }
    // История обработки
    if( listenersCount == 0 ) {
      // Нет заинтересованных, отмечаем это в истории обработки и гасим аларм и сохраняем его для истории
      ISkAnnounceThreadHistoryItem histItem =
          new SkAlarmAnnounceThreadHistoryItem( timestamp, ALARM_THREAD_NULL, IOptionSet.NULL );
      alarmEntity.addHistoryItem( histItem );
      entityManager.persist( alarmEntity );
      entityManager.flush();
    }
    return alarm;
  }

  @Override
  public void addAnnounceThreadHistoryItem( long aAlarmId, ISkAnnounceThreadHistoryItem aItem ) {
    // находим нужный аларм
    SkAlarmEntity alarm = entityManager.find( SkAlarmEntity.class, Long.valueOf( aAlarmId ) );
    // Создаем сущность для хранения
    SkAnnounceThreadHistoryItemEntity item = new SkAnnounceThreadHistoryItemEntity( Long.valueOf( aItem.timestamp() ),
        aItem.announceThreadId(), aItem.params() );
    alarm.addHistoryItem( item );
    // Изменение состояния тревоги в базе данных
    entityManager.merge( alarm );
    entityManager.flush();
    // Передача сообщений о тревоге клиентам
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      SkAlarmFrontendData frontendData = SkAlarmFrontendData.getFromFrontend( frontend );
      if( frontendData == null ) {
        // фронтенд не поддерживает тревоги
        continue;
      }
      for( IPolyFilterParams filterParams : frontendData.alarmFilterParams ) {
        IPolyFilter filter = PolyFilter.create( filterParams, filterFactoryRegistry.registeredFactories() );
        if( filter == IPolyFilter.NULL || filter.accept( alarm ) == true ) {
          SkAlarmMessageWhenStateChanged.send( frontend, alarm, item );
          break;
        }
      }
    }
  }

  @Override
  public ISkAlarmFlacon getAlarmFlacon( long aAlarmId ) {
    // находим нужный аларм
    SkAlarmEntity alarm = entityManager.find( SkAlarmEntity.class, Long.valueOf( aAlarmId ) );
    return alarm.skAlarmFlacon();
  }

  @Override
  public ITimedList<ISkAnnounceThreadHistoryItem> getAlarmHistory( long aAlarmId ) {
    // находим нужный аларм
    SkAlarmEntity alarm = entityManager.find( SkAlarmEntity.class, Long.valueOf( aAlarmId ) );
    // Выбираем по нему его историю
    ITimedList<ISkAnnounceThreadHistoryItem> retVal = alarm.history();
    return retVal;
  }

  @Override
  public ITimedList<ISkAlarm> queryAlarms( ITimeInterval aTimeInterval, ITsCombiFilterParams aQueryParams ) {
    TimedList<ISkAlarm> retVal = new TimedList<>();

    // Запрос на данные
    Query query = entityManager.createNamedQuery( "SkAlarmEntity.queryAlarms" ); //$NON-NLS-1$
    query.setParameter( "startTime", Long.valueOf( aTimeInterval.startTime() ) ); //$NON-NLS-1$
    query.setParameter( "endTime", Long.valueOf( aTimeInterval.endTime() ) ); //$NON-NLS-1$
    // Фильтр на результаты запроса
    IPolyFilter filter = aQueryParams == IPolyFilterParams.NULL ? IPolyFilter.NULL
        : filterFactoryRegistry.createPolyFilter( aQueryParams );
    // Составляем список алармов
    for( Iterator<?> iterator = query.getResultList().iterator(); iterator.hasNext(); ) {
      SkAlarmEntity alarmEntity = (SkAlarmEntity)iterator.next();
      if( filter == ITsFilter.ALL || filter.accept( alarmEntity ) ) {
        retVal.add( new SkAlarm( //
            alarmEntity.timestamp(), //
            alarmEntity.alarmId(), //
            alarmEntity.priority(), //
            alarmEntity.sublevel(), //
            alarmEntity.authorId(), //
            alarmEntity.userId(), alarmEntity.alarmDefId(), //
            alarmEntity.message() ) //
        );
      }
    }
    return retVal;
  }

  // ------------------------------------------------------------------------------------
  // ISkBackendAddonAlarm
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
