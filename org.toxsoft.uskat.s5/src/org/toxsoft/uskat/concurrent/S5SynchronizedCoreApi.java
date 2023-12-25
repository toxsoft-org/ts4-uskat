package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.*;

import org.toxsoft.core.log4j.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.coll.synch.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.clobserv.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.gwids.*;
import org.toxsoft.uskat.core.api.hqserv.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.devapi.gwiddb.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Синхронизация доступа к {@link ISkCoreApi} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedCoreApi
    extends S5SynchronizedResource<ISkCoreApi>
    implements ISkCoreApi {

  private final S5SynchronizedSysdescrService     sysdescr;
  private final S5SynchronizedObjectService       objService;
  private final S5SynchronizedLinkService         linkService;
  private final S5SynchronizedClobService         clobService;
  private final S5SynchronizedGwidDbService       gwidDbService;
  private final S5SynchronizedRtDataService       rtdService;
  private final S5SynchronizedCommandService      cmdService;
  private final S5SynchronizedEventService        eventService;
  private final S5SynchronizedHistoryQueryService hqService;
  private final S5SynchronizedUserService         userService;
  private final S5SynchronizedGwidService         gwidService;

  private final IStringMapEdit<ISkService> services;

  private final ILogger logger = LoggerWrapper.getLogger( getClass() );

  /**
   * Конструктор
   *
   * @param aTarget {@link ISkCoreApi} защищаемый ресурс
   * @param aLock {@link ReentrantReadWriteLock} блокировка доступа к ресурсу
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public S5SynchronizedCoreApi( ISkCoreApi aTarget, ReentrantReadWriteLock aLock ) {
    super( aTarget, aLock );
    sysdescr = new S5SynchronizedSysdescrService( aTarget.sysdescr(), aLock );
    objService = new S5SynchronizedObjectService( aTarget.objService(), aLock );
    linkService = new S5SynchronizedLinkService( aTarget.linkService(), aLock );
    clobService = new S5SynchronizedClobService( aTarget.clobService(), aLock );
    gwidDbService = new S5SynchronizedGwidDbService(
        (ISkGwidDbService)aTarget.services().findByKey( ISkGwidDbService.SERVICE_ID ), aLock );
    rtdService = new S5SynchronizedRtDataService( aTarget.rtdService(), aLock );
    cmdService = new S5SynchronizedCommandService( aTarget.cmdService(), aLock );
    eventService = new S5SynchronizedEventService( aTarget.eventService(), aLock );
    hqService = new S5SynchronizedHistoryQueryService( aTarget.hqService(), aLock );
    userService = new S5SynchronizedUserService( aTarget.userService(), aLock );
    gwidService = new S5SynchronizedGwidService( aTarget.gwidService(), aLock );
    services = new SynchronizedStringMap<>( new StringMap<>(), aLock );
    services.put( sysdescr.serviceId(), sysdescr );
    services.put( objService.serviceId(), objService );
    services.put( linkService.serviceId(), linkService );
    services.put( clobService.serviceId(), clobService );
    services.put( gwidDbService.serviceId(), gwidDbService );
    services.put( rtdService.serviceId(), rtdService );
    services.put( cmdService.serviceId(), cmdService );
    services.put( eventService.serviceId(), eventService );
    services.put( hqService.serviceId(), hqService );
    // TODO: ???
    // services.put( userService.serviceId(), userService );
    services.put( ISkUserService.SERVICE_ID, userService );

    services.put( gwidService.serviceId(), gwidService );
  }

  /**
   * Returns information about backend instance.
   *
   * @return {@link ISkBackendInfo} - the backend info
   */
  public ISkBackendInfo getBackendInfo() {
    lockWrite( this );
    try {
      SkCoreApi coreApi = (SkCoreApi)target();
      return coreApi.backend().getBackendInfo();
    }
    finally {
      unlockWrite( this );
    }
  }

  // ------------------------------------------------------------------------------------
  // S5SynchronizedResource
  //
  @Override
  protected void doChangeTarget( ISkCoreApi aPrevTarget, ISkCoreApi aNewTarget, ReentrantReadWriteLock aNewLock ) {
    for( String serviceId : services.keys() ) {
      S5SynchronizedService<?> sservice = (S5SynchronizedService<?>)services.getByKey( serviceId );
      sservice.changeTarget( aNewTarget.services().getByKey( serviceId ), aNewLock );
    }
  }

  // ------------------------------------------------------------------------------------
  // API пакета
  //
  /**
   * Добавляет синхронизированную службу в {@link ISkCoreApi}
   *
   * @param aService {@link S5SynchronizedService} синхронизированная служба
   * @throws TsNullArgumentRtException аргумент = null
   */
  void addService( S5SynchronizedService<?> aService ) {
    TsNullArgumentRtException.checkNull( aService );
    lockWrite( this );
    try {
      // Службы уже должны быть в несинхронизированном соединении
      // target().addService( new ISkServiceCreator<>() {
      //
      // @Override
      // public AbstractSkService createService( IDevCoreApi aCoreApi ) {
      // return (AbstractSkService)aService.target();
      // }
      // } );
      services.put( aService.serviceId(), aService );
    }
    finally {
      unlockWrite( this );
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkCoreApi
  //
  @Override
  public ISkSysdescr sysdescr() {
    return sysdescr;
  }

  @Override
  public ISkObjectService objService() {
    return objService;
  }

  @Override
  public ISkLinkService linkService() {
    return linkService;
  }

  @Override
  public ISkClobService clobService() {
    return clobService;
  }

  @Override
  public ISkRtdataService rtdService() {
    return rtdService;
  }

  @Override
  public ISkCommandService cmdService() {
    return cmdService;
  }

  @Override
  public ISkEventService eventService() {
    return eventService;
  }

  @Override
  public ISkHistoryQueryService hqService() {
    return hqService;
  }

  @Override
  public ISkUserService userService() {
    return userService;
  }

  @Override
  public ISkGwidService gwidService() {
    return gwidService;
  }

  @Override
  public IStringMapEdit<ISkService> services() {
    return services;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public <S extends ISkService> S getService( String aServiceId ) {
    return (S)services.getByKey( aServiceId );
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public <S extends AbstractSkService> S addService( ISkServiceCreator<S> aCreator ) {
    TsNullArgumentRtException.checkNull( aCreator );
    // TODO: 2021-11-12 mvk надо изменить регистрацию служб (слишком запутано)
    IDevCoreApi devApi = null;
    if( target() instanceof IDevCoreApi ) {
      devApi = (IDevCoreApi)target();
    }
    if( target() instanceof S5SynchronizedCoreApi ) {
      devApi = (IDevCoreApi)((S5SynchronizedCoreApi)target()).target();
    }
    if( devApi != null ) {
      // Добавление в API несинхронизированной службы
      ISkService retValue = aCreator.createService( devApi );
      String serviceId = retValue.serviceId();
      if( !(retValue instanceof S5SynchronizedService) ) {
        logger.error( "%s is unsynchronized service! Synchronized coreApi requires synchronized service!", serviceId ); //$NON-NLS-1$
        return null;
      }
      services.put( serviceId, retValue );
      return (S)retValue;
    }
    // Запрещенное использование реализации. Требуется использовать S5SynchronizedConnection.addService()
    throw new TsUnsupportedFeatureRtException();
  }

  @Override
  public ISkLoggedUserInfo getCurrentUserInfo() {
    lockWrite( this );
    try {
      return target().getCurrentUserInfo();
    }
    finally {
      unlockWrite( this );
    }
  }
}
