package org.toxsoft.uskat.concurrent;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.log4j.LoggerWrapper;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.coll.synch.SynchronizedStringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsUnsupportedFeatureRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;

import ru.uskat.core.ISkCoreApi;
import ru.uskat.core.api.ISkService;
import ru.uskat.core.api.ISkServiceCreator;
import ru.uskat.core.api.cmds.ISkCommandService;
import ru.uskat.core.api.events.ISkEventService;
import ru.uskat.core.api.links.ISkLinkService;
import ru.uskat.core.api.lobs.ISkLobService;
import ru.uskat.core.api.objserv.ISkObjectService;
import ru.uskat.core.api.rtdata.ISkRtDataService;
import ru.uskat.core.api.sysdescr.ISkSysdescr;
import ru.uskat.core.api.users.ISkSession;
import ru.uskat.core.api.users.ISkUserService;
import ru.uskat.core.devapi.IDevCoreApi;
import ru.uskat.core.impl.AbstractSkService;

/**
 * Синхронизация доступа к {@link ISkCoreApi} (декоратор)
 *
 * @author mvk
 */
public final class S5SynchronizedCoreApi
    extends S5SynchronizedResource<ISkCoreApi>
    implements ISkCoreApi {

  private final S5SynchronizedSysdescrService sysdescr;
  private final S5SynchronizedObjectService   objService;
  private final S5SynchronizedLinkService     linkService;
  private final S5SynchronizedRtDataService   rtDataService;
  private final S5SynchronizedCommandService  cmdService;
  private final S5SynchronizedEventService    eventService;
  private final S5SynchronizedUserService     userService;
  private final S5SynchronizedLobService      lobService;
  private final IStringMapEdit<ISkService>    services;

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
    rtDataService = new S5SynchronizedRtDataService( aTarget.rtDataService(), aLock );
    cmdService = new S5SynchronizedCommandService( aTarget.cmdService(), aLock );
    eventService = new S5SynchronizedEventService( aTarget.eventService(), aLock );
    userService = new S5SynchronizedUserService( aTarget.userService(), aLock );
    lobService = new S5SynchronizedLobService( aTarget.lobService(), aLock );
    services = new SynchronizedStringMap<>( new StringMap<>(), aLock );
    services.put( sysdescr.serviceId(), sysdescr );
    services.put( objService.serviceId(), objService );
    services.put( linkService.serviceId(), linkService );
    services.put( rtDataService.serviceId(), rtDataService );
    services.put( cmdService.serviceId(), cmdService );
    services.put( eventService.serviceId(), eventService );
    services.put( userService.serviceId(), userService );
    services.put( lobService.serviceId(), lobService );
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
  public ISkRtDataService rtDataService() {
    return rtDataService;
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
  public ISkUserService userService() {
    return userService;
  }

  @Override
  public ISkLobService lobService() {
    return lobService;
  }

  @Override
  public IStringMap<ISkService> services() {
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
  public ISkSession sessionInfo() {
    lockWrite( this );
    try {
      return target().sessionInfo();
    }
    finally {
      unlockWrite( this );
    }
  }
}
