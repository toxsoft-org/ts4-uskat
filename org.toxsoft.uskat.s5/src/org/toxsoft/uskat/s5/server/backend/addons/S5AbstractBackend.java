package org.toxsoft.uskat.s5.server.backend.addons;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.uskat.s5.client.IS5ConnectionParams.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.server.backend.addons.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.lang.reflect.InvocationTargetException;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.ctx.impl.TsContext;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.idgen.IStridGenerator;
import org.toxsoft.core.tslib.bricks.strid.idgen.UuidStridGenerator;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.coll.synch.SynchronizedStringMap;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsInternalErrorRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.core.ISkServiceCreator;
import org.toxsoft.uskat.core.backend.ISkFrontendRear;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.core.impl.AbstractSkService;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;
import org.toxsoft.uskat.s5.common.S5BackendDoJobThread;
import org.toxsoft.uskat.s5.common.sessions.ISkSession;
import org.toxsoft.uskat.s5.server.backend.messages.S5BaBeforeCloseMessages;
import org.toxsoft.uskat.s5.server.frontend.*;
import org.toxsoft.uskat.s5.utils.S5ValobjUtils;
import org.toxsoft.uskat.s5.utils.progress.IS5ProgressMonitor;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

/**
 * Абстрактная реализация s5-бекенда
 *
 * @author mvk
 * @param <ADDON> тип расширения
 */
public abstract class S5AbstractBackend<ADDON extends IS5BackendAddon>
    implements IS5Backend {

  private static final long LOCK_TIMEOUT = 1000;

  /**
   * Представленный клиентом фронтенд с которым работает бекенд
   */
  private final ISkFrontendRear frontend;

  /**
   * Параметры создания бекенда
   */
  private final ITsContextRo openArgs;

  /**
   * Данные фронтенда
   */
  private final S5FrontendData frontendData = new S5FrontendData();

  /**
   * Фронтенд с перехватом и обработкой сообщений внутри бекенда и его расширений
   */
  private final IS5FrontendRear frontendRear;

  /**
   * Блокировка доступа к frontendRear
   */
  private final S5Lockable frontendLock;

  /**
   * Используемый загрузчик классов
   */
  private final ClassLoader classLoader;

  /**
   * Монитор прогресса
   */
  private final IS5ProgressMonitor progressMonitor;

  /**
   * Генератор идентификаторов сессий, объектов {@link ISkSession}
   */
  private static IStridGenerator stridGenerator = new UuidStridGenerator( UuidStridGenerator.createState( "remote" ) ); //$NON-NLS-1$

  /**
   * Идентификатор сессии {@link ISkSession}.
   * <p>
   * Создается при открытии соединения {@link ISkConnection#open(ITsContextRo)}
   */
  private Skid sessionID = Skid.NONE;

  /**
   * Журнал работы
   */
  private ILogger logger = getLogger( getClass() );

  /**
   * Задача (поток) обслуживания потребностей бекенда {@link ICooperativeMultiTaskable#doJob()}
   */
  private final S5BackendDoJobThread backendDojobThread;

  /**
   * Список расширений бекенда поддерживаемых сервером
   */
  private final IStringMapEdit<ADDON> allAddons = new SynchronizedStringMap<>( new StringMap<>() );

  /**
   * Список создателей служб поддерживаемых сервером
   */
  private final IListEdit<ISkServiceCreator<? extends AbstractSkService>> backendServicesCreators =
      new ElemArrayList<>();

  /**
   * Статическая инициализация
   */
  static {
    // Регистрация s5-хранителей
    S5ValobjUtils.registerS5Keepers();
  }

  /**
   * Конструктор backend
   *
   * @param aFrontend {@link ISkFrontendRear} фронтенд, для которого создается бекенд
   * @param aArgs {@link ITsContextRo} аргументы (ссылки и опции) создания бекенда
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5AbstractBackend( ISkFrontendRear aFrontend, ITsContextRo aArgs ) {
    TsNullArgumentRtException.checkNulls( aArgs, aFrontend );
    // Параметры аутентификации
    IAtomicValue login = IS5ConnectionParams.OP_USERNAME.getValue( aArgs.params() );
    // Формирование идентификатора сессии
    sessionID = new Skid( ISkSession.CLASS_ID, login.asString() + "." + stridGenerator.nextId() ); //$NON-NLS-1$
    // Получение блокировки соединения
    frontendLock = aArgs.getRef( IS5ConnectionParams.REF_CONNECTION_LOCK.refKey(), S5Lockable.class );
    // Представленный фронтенд
    frontend = aFrontend;
    // Фронтенд с перехватом и обработкой сообщений внутри бекенда и его расширений
    frontendRear = new IS5FrontendRear() {

      @Override
      public Skid sessionID() {
        return sessionID;
      }

      @Override
      public boolean isLocal() {
        return S5AbstractBackend.this.doIsLocal();
      }

      @Override
      public void onBackendMessage( GtMessage aMessage ) {
        fireBackendMessage( aMessage );
      }

      @Override
      public IS5FrontendData frontendData() {
        return frontendData;
      }

    };
    // Параметры создания бекенда с собственными параметрами
    TsContext args = new TsContext( aArgs );
    args.params().setValobj( IS5ConnectionParams.OP_SESSION_ID, sessionID );
    openArgs = args;

    // Загручик классов
    classLoader = (aArgs.hasKey( REF_CLASSLOADER.refKey() ) ? //
        aArgs.getRef( REF_CLASSLOADER.refKey(), ClassLoader.class ) : //
        ClassLoader.getSystemClassLoader());
    // Монитор прогресса подключения
    progressMonitor = (aArgs.hasKey( REF_MONITOR.refKey() ) ? //
        aArgs.getRef( REF_MONITOR.refKey(), IS5ProgressMonitor.class ) : //
        IS5ProgressMonitor.NULL);

    // TODO: ??? в наследниках должна быть следующая реализация
    // Читатель системного описания
    // sysdescrReader = new SkSysdescrReader( () -> remote().baClasses().readClassInfos() );
    // Результат инициализации соединения
    // IS5SessionInitResult result = connection.sessionInitResult();
    // Инициализация читателя системного описания
    // sysdescrReader.setClassInfos( result.classInfos() );

    // Имя бекенда
    String name = "sessionID = " + sessionID.strid(); //$NON-NLS-1$
    // Задача (поток) обслуживания потребностей бекенда
    backendDojobThread = new S5BackendDoJobThread( name, this );
  }

  // ------------------------------------------------------------------------------------
  // ISkBackend
  //
  @Override
  public IS5FrontendRear frontend() {
    return frontendRear;
  }

  @Override
  public ITsContextRo openArgs() {
    return openArgs;
  }

  @Override
  public IBaClasses baClasses() {
    return (IBaClasses)allAddons.getByKey( IBaClasses.ADDON_ID );
  }

  @Override
  public IBaObjects baObjects() {
    return (IBaObjects)allAddons.getByKey( IBaObjects.ADDON_ID );
  }

  @Override
  public IBaLinks baLinks() {
    return (IBaLinks)allAddons.getByKey( IBaLinks.ADDON_ID );
  }

  @Override
  public IBaEvents baEvents() {
    return (IBaEvents)allAddons.getByKey( IBaEvents.ADDON_ID );
  }

  @Override
  public IBaClobs baClobs() {
    return (IBaClobs)allAddons.getByKey( IBaClobs.ADDON_ID );
  }

  @Override
  public IBaRtdata baRtdata() {
    return (IBaRtdata)allAddons.getByKey( IBaRtdata.ADDON_ID );
  }

  @Override
  public IBaCommands baCommands() {
    return (IBaCommands)allAddons.getByKey( IBaCommands.ADDON_ID );
  }

  @Override
  public IBaQueries baQueries() {
    return (IBaQueries)allAddons.getByKey( IBaQueries.ADDON_ID );
  }

  @Override
  public IListEdit<ISkServiceCreator<? extends AbstractSkService>> listBackendServicesCreators() {
    return backendServicesCreators;
  }

  @Override
  public <T> T findBackendAddon( String aAddonId, Class<T> aExpectedType ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aExpectedType );
    Object rawAddon = allAddons.findByKey( aAddonId );
    return aExpectedType.cast( rawAddon );
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //
  @Override
  public void doJob() {
    // 2020-10-12 mvk doJob + mainLock
    if( !tryLockWrite( frontendLock, LOCK_TIMEOUT ) ) {
      // Ошибка получения блокировки
      logger.warning( ERR_TRY_LOCK, frontendLock, frontendRear );
      return;
    }
    // Получен доступ к блокировке.
    try {
      for( IS5BackendAddon addon : allAddons ) {
        addon.doJob();
      }
    }
    finally {
      // Разблокировка доступа к SkConnection
      unlockWrite( frontendLock );
    }
  }

  // ------------------------------------------------------------------------------------
  // IClosable
  //
  @Override
  public final void close() {
    logger.info( "S5AbstractBackend.close(): starting ..." ); //$NON-NLS-1$
    // Формирование сообщения о предстоящем завершении работы
    fireBackendMessage( S5BaBeforeCloseMessages.INSTANCE.makeMessage() );
    // Завершение работы наследниками
    doClose();
    // Завершение работы базового класса
    backendDojobThread.close();
    // 2021-03-23 mvk
    // Вызов frontendCaller.close() установил состояние 'interrupted' которое может не дать должны образом выполниться
    // следующему connection.closeSession() если close() вызывается из потоков frontendCaller.doJob или
    // backendDojobThread
    Thread.interrupted();
    // Завершение работы расширений бекенда
    tryLockWrite( frontendLock );
    try {
      for( IS5BackendAddon addon : allAddons ) {
        addon.close();
      }
    }
    finally {
      unlockWrite( frontendLock );
    }

    // 2020-10-12 mvk doJob + mainLock
    S5Lockable.removeLockableFromPool( nativeLock( frontendLock ) );
    // Запись в журнал
    logger.info( "S5AbstractBackend.close(): ... finished" ); //$NON-NLS-1$
  }

  // ------------------------------------------------------------------------------------
  // Шаблонные и абстрактные методы для реализации наследниками
  //
  /**
   * Возвращает признак того, что фронтенд представляет локального клиента
   *
   * @return boolean <b>true</b> фронтенд локального клиента;<b>false</b> клиент удаленного клиента
   */
  protected abstract boolean doIsLocal();

  /**
   * Обработать завершение работы с бекендом
   */
  protected void doClose() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Методы для наследников
  //
  /**
   * Возвращает идентификатор соединения
   *
   * @return {@link Skid} идентификатор соединения
   */
  protected final Skid sessionID() {
    return sessionID;
  }

  /**
   * Возвращает все расширения бекенда поддерживаемые сервером
   *
   * @return {@link IStringMapEdit}&lt;ADDON&gt; карта расширений бекенда;
   *         <p>
   *         Ключ: идентификатор расширения;<br>
   *         Значение: s5-бекенд
   */
  protected final IStringMapEdit<ADDON> allAddons() {
    return allAddons;
  }

  /**
   * Возвращает блокировку соединения
   *
   * @return {@link S5Lockable} блокировка соединения
   */
  protected final S5Lockable frontendLock() {
    return frontendLock;
  }

  /**
   * Формирование (генерация) сообщения для фронтенда и расширений бекенда
   *
   * @param aMessage {@link GtMessage} сообщение
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected final void fireBackendMessage( GtMessage aMessage ) {
    TsNullArgumentRtException.checkNull( aMessage );
    tryLockWrite( frontendLock );
    try {
      for( IS5BackendAddon addon : allAddons ) {
        addon.onBackendMessage( aMessage );
      }
    }
    finally {
      unlockWrite( frontendLock );
    }
    frontend.onBackendMessage( aMessage );
  }

  /**
   * Возвращает загрузчик классов
   *
   * @return {@link ClassLoader} загрузчик классов
   */
  protected final ClassLoader classLoader() {
    return classLoader;
  }

  /**
   * Возвращает монитор прогресса
   *
   * @return {@link IS5ProgressMonitor} монитор прогресса
   */
  protected final IS5ProgressMonitor progressMonitor() {
    return progressMonitor;
  }

  /**
   * Возвращает журнал работы
   *
   * @return {@link ILogger} журнал работы
   */
  protected final ILogger logger() {
    return logger;
  }

  /**
   * Создает список расширений бекенда
   * <p>
   * Если класс реализации расширения не найден в classpath клиента, то выводится предупреждение
   *
   * @param aClassLoader {@link ClassLoader} используемый загрузчик классов
   * @param aAddonInfos {@link IStringMap}&lt;String&gt; карта описания расширений.
   *          <p>
   *          Ключ: идентификатор расширения {@link IS5BackendAddon};<br>
   *          Значение: полное имя java-класса реализующий расширение {@link IS5BackendAddon};<br>
   * @param aLogger {@link ILogger} журнал работы
   * @param <T> тип бекенда
   * @return {@link IStridablesList}&lt;{@link IS5BackendAddon}&gt; список расширений бекенда.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( { "unchecked" } )
  protected <T extends IS5BackendAddon> IStringMap<T> createBackendAddons( ClassLoader aClassLoader,
      IStringMap<String> aAddonInfos, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aClassLoader, aAddonInfos, aLogger );
    IStringMapEdit<T> retValue = new StringMap<>();
    for( String addonId : aAddonInfos.keys() ) {
      String addonClassName = aAddonInfos.getByKey( addonId );
      Class<T> implClassName = null;
      try {
        implClassName = (Class<T>)aClassLoader.loadClass( addonClassName );
      }
      catch( @SuppressWarnings( "unused" ) ClassNotFoundException e ) {
        // Предупреждение: класс расширения не найден в classpath клиента
        aLogger.warning( ERR_ADDON_IMPL_NOT_FOUND, addonId, addonClassName );
        continue;
      }
      try {
        // getClass() is owner class
        T addon = implClassName.getConstructor( getClass() ).newInstance( this );
        retValue.put( addon.id(), addon );
      }
      catch( NoSuchMethodException e ) {
        // Не найден открытый конструктор без параметров в классе реализации бекенда (IS5InitialImplementation)
        throw new TsInternalErrorRtException( e, ERR_NOT_FOUND_INIT_IMPL_CONSTRUCTOR, implClassName, cause( e ) );
      }
      catch( InstantiationException e ) {
        // Ошибка создания описания реализации бекенда (IS5InitialImplementation)
        throw new TsInternalErrorRtException( e, ERR_NOT_FOUND_INIT_IMPL_INSTANTIATION, implClassName, cause( e ) );
      }
      catch( SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e ) {
        // Неожиданная ошибка создания описания реализации бекенда (IS5InitialImplementation)
        throw new TsInternalErrorRtException( e, ERR_NOT_FOUND_INIT_IMPL_UNEXPECTED, implClassName, cause( e ) );
      }
    }
    return retValue;
  }
}
