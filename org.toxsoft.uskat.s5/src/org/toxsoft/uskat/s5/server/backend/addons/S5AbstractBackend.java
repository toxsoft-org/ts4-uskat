package org.toxsoft.uskat.s5.server.backend.addons;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.backend.ISkBackendHardConstant.*;
import static org.toxsoft.uskat.core.impl.ISkCoreConfigConstants.*;
import static org.toxsoft.uskat.s5.client.IS5ConnectionParams.*;
import static org.toxsoft.uskat.s5.server.backend.addons.IS5Resources.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.ctx.impl.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.strid.idgen.*;
import org.toxsoft.core.tslib.bricks.threadexec.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.s5.client.*;
import org.toxsoft.uskat.s5.server.backend.messages.*;
import org.toxsoft.uskat.s5.server.frontend.*;
import org.toxsoft.uskat.s5.utils.*;
import org.toxsoft.uskat.s5.utils.threads.impl.*;

/**
 * Абстрактная реализация s5-бекенда
 *
 * @author mvk
 * @param <ADDON> тип расширения
 */
public abstract class S5AbstractBackend<ADDON extends IS5BackendAddon>
    implements IS5Backend, Runnable {

  /**
   * Идентификатор журнала используемый по умолчанию
   */
  public static final String TS_DEFAULT_LOGGER = "TsDefaultLogger"; //$NON-NLS-1$

  /**
   * Идентификатор журнала ошибок используемый по умолчанию
   */
  public static final String TS_ERROR_LOGGER = "TsErrorLogger"; //$NON-NLS-1$

  /**
   * Таймаут ожидания блокировки (мсек).
   */
  private static final long WAIT_FRONTED_LOCK_TIMEOUT = 1000;

  /**
   * API разработчика ядра
   * <p>
   * TODO: 2022-09-22 mvk: использование {@link IDevCoreApi} в бекенде (для передачи сообщения backend->frontend) -
   * является грязным хаком. Предполагается, что метод {@link IDevCoreApi#doJobInCoreMainThread()} будет вызываться
   * косвенно клиентом. "Как и когда" - тема для обсуждения с goga.
   */
  // private final IDevCoreApi devCoreApi;

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
   * Механизм широковещательной рассылки сообщений от фронтенда к бекенду и всем фронтендам
   */
  private final GtMessageEventer broadcastEventer = new GtMessageEventer();

  /**
   * Механизм формирования сообщений от фронтенда к бекенду
   */
  private final GtMessageEventer frontendEventer = new GtMessageEventer();

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
  private final ILongOpProgressCallback progressMonitor;

  /**
   * Таймаут (мсек) фоновой обработки аддонов
   */
  private final int doJobTimeout;

  /**
   * Значения параметров бекенда
   */
  private SkBackendInfo backendInfo;

  /**
   * Генератор идентификаторов локальных сессий, объектов {@link ISkSession}
   */
  private static IStridGenerator localStridGenerator =
      new UuidStridGenerator( UuidStridGenerator.createState( "local" ) ); //$NON-NLS-1$

  /**
   * Генератор идентификаторов удаленных сессий, объектов {@link ISkSession}
   */
  private static IStridGenerator remoteStridGenerator =
      new UuidStridGenerator( UuidStridGenerator.createState( "remote" ) ); //$NON-NLS-1$

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
   * Исполнитель запросов к соединению
   */
  private final ITsThreadExecutor threadExecutor;

  /**
   * Карта построителей {@link IS5BackendAddonCreator} расширений {@link IS5BackendAddon} бекенда поддерживаемых
   * сервером.
   * <p>
   * Ключ: идентификатор расширения {@link IS5BackendAddon#id()};<br>
   * Значение: построитель расширения {@link IS5BackendAddonCreator}.
   */
  private final IStridablesListEdit<IS5BackendAddonCreator> baCreators = new StridablesList<>();

  /**
   * Список расширений бекенда поддерживаемых сервером
   */
  // 2025-01-25 mvk ---+++ излишние затраты на производительность без основания
  // private final IStringMapEdit<ADDON> allAddons = new SynchronizedStringMap<>( new StringMap<>() );
  private final IStringMapEdit<ADDON> allAddons = new StringMap<>();

  /**
   * Признак завершенной инициализации
   */
  private boolean inited;

  /**
   * Признак процесса завершения работы
   */
  private boolean isClosing;

  /**
   * Признак завершенной работы
   */
  private boolean isClosed;

  /**
   * Журнал по умолчанию
   */
  private static final ILogger tsDefaultLogger = getLogger( TS_DEFAULT_LOGGER );

  /**
   * Журнал по умолчанию
   */
  private static final ILogger tsErrorLogger = getLogger( TS_ERROR_LOGGER );

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
   * @param aBackendId String идентификатор бекенда
   * @param aBackendInfoValue {@link IOptionSet} значения параметров бекенда {@link ISkBackendInfo#params()}
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5AbstractBackend( ISkFrontendRear aFrontend, ITsContextRo aArgs, String aBackendId,
      IOptionSet aBackendInfoValue ) {
    TsNullArgumentRtException.checkNulls( aArgs, aFrontend, aBackendId, aBackendInfoValue );
    // Замена журнала по умолчанию
    if( !LoggerUtils.defaultLogger().equals( tsDefaultLogger ) ) {
      LoggerUtils.setDefaultLogger( tsDefaultLogger );
      LoggerUtils.defaultLogger().info( MSG_CHANGE_DEFAULT_LOGGER, TS_DEFAULT_LOGGER );
    }
    if( !LoggerUtils.errorLogger().equals( tsErrorLogger ) ) {
      LoggerUtils.setErrorLogger( tsErrorLogger );
      LoggerUtils.errorLogger().info( MSG_CHANGE_ERROR_LOGGER, TS_ERROR_LOGGER );
    }
    // Параметры аутентификации
    IAtomicValue login = IS5ConnectionParams.OP_USERNAME.getValue( aArgs.params() );
    // Генератор идентификаторов сессий
    IStridGenerator idGenerator = (doIsLocal() ? localStridGenerator : remoteStridGenerator);
    // Формирование идентификатора сессии
    sessionID = new Skid( ISkSession.CLASS_ID, login.asString() + "." + idGenerator.nextId() ); //$NON-NLS-1$
    // Получение блокировки соединения
    // frontendLock = aArgs.getRef( IS5ConnectionParams.REF_CONNECTION_LOCK.refKey(), S5Lockable.class );
    frontendLock = new S5Lockable();
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

      @Override
      public IGtMessageEventer broadcastEventer() {
        return broadcastEventer;
      }

      @Override
      public IGtMessageEventer frontendEventer() {
        return frontendEventer;
      }

      @Override
      public String toString() {
        return S5AbstractBackend.this.toString();
      }

    };
    // Параметры создания бекенда
    openArgs = createContextForBackend( aArgs, sessionID );
    // Загручик классов
    classLoader = (aArgs.hasKey( REF_CLASSLOADER.refKey() ) ? //
        aArgs.getRef( REF_CLASSLOADER.refKey(), ClassLoader.class ) : //
        ClassLoader.getSystemClassLoader());
    // Монитор прогресса подключения
    progressMonitor = (aArgs.hasKey( ISkCoreConfigConstants.REFDEF_PROGRESS_CALLBACK.refKey() ) ? //
        aArgs.getRef( ISkCoreConfigConstants.REFDEF_PROGRESS_CALLBACK.refKey(), ILongOpProgressCallback.class ) : //
        ILongOpProgressCallback.NONE);

    // Задача (поток) обслуживания потребностей бекенда
    // backendDojobThread = new S5BackendDoJobThread( name, this );

    // Синхронизация обращения к uskat
    threadExecutor = REFDEF_THREAD_EXECUTOR.getRef( aArgs );

    // Таймаут выполнения doJob-задачи
    doJobTimeout = OP_DOJOB_TIMEOUT.getValue( openArgs.params() ).asInt();
    // Запуск фоновой задачи обработки аддонов
    threadExecutor.timerExec( doJobTimeout, this );

    IOptionSetEdit backendInfoValue = new OptionSet( aBackendInfoValue );
    OPDEF_SKBI_NEED_THREAD_SAFE_FRONTEND.setValue( backendInfoValue, AV_TRUE );
    // backendInfo =
    backendInfo = new SkBackendInfo( aBackendId, System.currentTimeMillis(), backendInfoValue );
  }

  // ------------------------------------------------------------------------------------
  // ISkBackend
  //
  @Override
  public final void initialize() {
    doInitialize();
    inited = true;
    isClosed = false;
    if( isActive() ) {
      // Формирование сообщения об изменении состояния бекенда: active = true
      fireBackendMessage( BackendMsgActiveChanged.INSTANCE.makeMessage( true ) );
    }
  }

  @Override
  public final ISkBackendInfo getBackendInfo() {
    backendInfo.params().addAll( getBackendInfoOptions() );
    // Общие (локальный, удаленны) параметры бекендов
    // Бекенд поддерживает транзакции
    ISkBackendHardConstant.OPDEF_TRANSACTION_SUPPORT.setValue( backendInfo.params(), AV_TRUE );
    return backendInfo;
  }

  @Override
  public final IS5FrontendRear frontend() {
    return frontendRear;
  }

  @Override
  public final ITsContextRo openArgs() {
    return openArgs;
  }

  @Override
  public final IBaClasses baClasses() {
    return (IBaClasses)allAddons.getByKey( IBaClasses.ADDON_ID );
  }

  @Override
  public final IBaObjects baObjects() {
    return (IBaObjects)allAddons.getByKey( IBaObjects.ADDON_ID );
  }

  @Override
  public final IBaLinks baLinks() {
    return (IBaLinks)allAddons.getByKey( IBaLinks.ADDON_ID );
  }

  @Override
  public final IBaEvents baEvents() {
    return (IBaEvents)allAddons.getByKey( IBaEvents.ADDON_ID );
  }

  @Override
  public final IBaClobs baClobs() {
    return (IBaClobs)allAddons.getByKey( IBaClobs.ADDON_ID );
  }

  @Override
  public final IBaRtdata baRtdata() {
    return (IBaRtdata)allAddons.getByKey( IBaRtdata.ADDON_ID );
  }

  @Override
  public final IBaCommands baCommands() {
    return (IBaCommands)allAddons.getByKey( IBaCommands.ADDON_ID );
  }

  @Override
  public final IBaQueries baQueries() {
    return (IBaQueries)allAddons.getByKey( IBaQueries.ADDON_ID );
  }

  @Override
  public final IBaGwidDb baGwidDb() {
    return (IBaGwidDb)allAddons.getByKey( IBaGwidDb.ADDON_ID );
  }

  @Override
  public final IListEdit<ISkServiceCreator<? extends AbstractSkService>> listBackendServicesCreators() {
    IListEdit<ISkServiceCreator<? extends AbstractSkService>> retValue = new ElemArrayList<>();
    for( IS5BackendAddonCreator baCreator : baCreators.values() ) {
      if( isCoreAddon( baCreator.id() ) ) {
        // Встроенные в ядро службы есть всегда
        continue;
      }
      retValue.add( baCreator.serviceCreator() );
    }
    return retValue;
  }

  @Override
  public final <T> T findBackendAddon( String aAddonId, Class<T> aExpectedType ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aExpectedType );
    Object rawAddon = allAddons.findByKey( aAddonId );
    return aExpectedType.cast( rawAddon );
  }

  @Override
  public final Skid sessionID() {
    return sessionID;
  }

  @Override
  public final void sendBackendMessage( GtMessage aMessage ) {
    TsNullArgumentRtException.checkNull( aMessage );
    broadcastEventer.sendMessage( aMessage );
  }

  @Override
  public final void onFrontendMessage( GtMessage aMessage ) {
    TsNullArgumentRtException.checkNull( aMessage );
    frontendEventer.sendMessage( aMessage );
  }

  // ------------------------------------------------------------------------------------
  // Runnable
  //
  @SuppressWarnings( { "boxing" } )
  @Override
  public void run() {
    logger.debug( MSG_DOJOB );
    if( isClosed || isClosing ) {
      // backend завершил или завершает свою работу
      return;
    }
    if( !LoggerUtils.defaultLogger().equals( tsDefaultLogger ) ) {
      LoggerUtils.setDefaultLogger( tsDefaultLogger );
      LoggerUtils.defaultLogger().error( MSG_RESTORE_DEFAULT_LOGGER, TS_DEFAULT_LOGGER );
    }
    if( !LoggerUtils.errorLogger().equals( tsErrorLogger ) ) {
      LoggerUtils.setErrorLogger( tsErrorLogger );
      LoggerUtils.errorLogger().error( MSG_RESTORE_ERROR_LOGGER, TS_ERROR_LOGGER );
    }
    if( tryLockWrite( frontendLock, WAIT_FRONTED_LOCK_TIMEOUT ) ) {
      try {
        for( IS5BackendAddon addon : allAddons ) {
          addon.doJob();
          if( isClosed || isClosing ) {
            return;
          }
        }
      }
      finally {
        unlockWrite( frontendLock );
      }
    }
    else {
      logger().warning( ERR_RUN_CANT_GET_FRONTEND_LOCK, WAIT_FRONTED_LOCK_TIMEOUT );
    }
    threadExecutor.timerExec( doJobTimeout, this );
  }

  // ------------------------------------------------------------------------------------
  // IClosable
  //
  @Override
  public final void close() {
    if( isClosing || isClosed ) {
      return;
    }
    logger.info( "S5AbstractBackend.close(): starting ..." ); //$NON-NLS-1$
    // Запуск процесса завершения работы
    isClosing = true;
    // Формирование сообщения о предстоящем завершении работы
    fireBackendMessage( S5BaBeforeCloseMessages.INSTANCE.makeMessage() );
    // Завершение работы наследниками
    try {
      doClose();
    }
    catch( Throwable e ) {
      logger.error( e );
    }
    // Завершение работы базового класса
    // backendDojobThread.close();

    // Выводим из блокировки текущие блокирующие операции
    threadExecutor.thread().interrupt();

    // 2021-03-23 mvk
    // Вызов frontendCaller.close() установил состояние 'interrupted' которое может не дать должны образом выполниться
    // следующему connection.closeSession() если close() вызывается из потоков frontendCaller.doJob или
    // backendDojobThread
    Thread.interrupted();
    // Завершение работы расширений бекенда
    lockWrite( frontendLock );
    try {
      for( IS5BackendAddon addon : allAddons ) {
        try {
          addon.close();
        }
        catch( Throwable e ) {
          logger.error( e );
        }
      }
    }
    finally {
      unlockWrite( frontendLock );
    }
    // 2020-10-12 mvk doJob + mainLock
    S5Lockable.removeLockableFromPool( nativeLock( frontendLock ) );
    // Установка признака завершения работы
    isClosed = true;
    isClosing = false;
    // Запись в журнал
    logger.info( "S5AbstractBackend.close(): ... finished" ); //$NON-NLS-1$
  }

  // ------------------------------------------------------------------------------------
  // Шаблонные и абстрактные методы для реализации наследниками
  //
  /**
   * Провести инициализацию бекенда в наследнике. Вызывается после вызова конструктора
   */
  protected void doInitialize() {
    // nop
  }

  /**
   * Возвращает признак того, что фронтенд представляет локального клиента
   *
   * @return boolean <b>true</b> фронтенд локального клиента;<b>false</b> клиент удаленного клиента
   */
  protected abstract boolean doIsLocal();

  /**
   * Используя предоставленную карту построителей создает расширения бекенда
   *
   * @param aBaCreators {@link IStridablesList}&lt; {@link IS5BackendAddonCreator}&gt; карта построителей.
   *          <p>
   *          Ключ: идентификатор расширения {@link IS5BackendAddon#id()};<br>
   *          Значение: построитель расширения {@link IS5BackendAddonCreator}.
   * @return {@link IStringMapEdit}&lt;ADDON&gt; карта расширений бекенда;
   *         <p>
   *         Ключ: идентификатор расширения;<br>
   *         Значение: s5-бекенд
   */
  protected abstract IStringMap<ADDON> doCreateAddons( IStridablesList<IS5BackendAddonCreator> aBaCreators );

  /**
   * Возвращает значения параметров бекенда предоставляемых сервером.
   * <p>
   * Возвращаемые значения опций будут размещены в общем контейнере {@link ISkBackend#getBackendInfo()}.
   *
   * @return {@link SkBackendInfo} значения параметров бекенда или null если сервер недоступен
   */
  protected IOptionSet getBackendInfoOptions() {
    return IOptionSet.NULL;
  }

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
   * Возвращает признак завершения инициализации бекенда
   *
   * @return boolean <b>true</b> бекенд завершил инициализацию. <b>false</b> бекенд не завершил инициализаци.
   */
  protected final boolean isInited() {
    return inited;
  }

  /**
   * Устанавливает карту построителей {@link IS5BackendAddonCreator} расширений {@link IS5BackendAddon} бекенда
   * поддерживаемых сервером.
   *
   * @param aBaCreators {@link IStridablesList}&lt; {@link IS5BackendAddonCreator}&gt; карта построителей.
   *          <p>
   *          Ключ: идентификатор расширения {@link IS5BackendAddon#id()};<br>
   *          Значение: построитель расширения {@link IS5BackendAddonCreator}.
   */
  protected final void setBaCreators( IStridablesList<IS5BackendAddonCreator> aBaCreators ) {
    TsNullArgumentRtException.checkNull( aBaCreators );
    baCreators.setAll( aBaCreators );
    allAddons.setAll( doCreateAddons( aBaCreators ) );
  }

  /**
   * Возвращает все расширения бекенда поддерживаемые сервером
   *
   * @return {@link IStringMapEdit}&lt;ADDON&gt; карта расширений бекенда;
   *         <p>
   *         Ключ: идентификатор расширения;<br>
   *         Значение: s5-бекенд
   */
  protected final IStringMap<ADDON> allAddons() {
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
   * Формирование (генерация) сообщения от бекенда к фронтенду
   *
   * @param aMessage {@link GtMessage} сообщение
   * @throws TsNullArgumentRtException аргумент = null
   */
  protected final void fireBackendMessage( GtMessage aMessage ) {
    TsNullArgumentRtException.checkNull( aMessage );
    // 2023-02-13 mvk иногда в сообщения бывают большие массивы значений (например, результаты отчета)
    // поэтому вывод в журнал только в отладочном режиме
    if( logger.isSeverityOn( ELogSeverity.DEBUG ) ) {
      logger.info( "onBackendMessage recevied: %s", aMessage ); //$NON-NLS-1$
    }

    // 2025-03-24 mvk---
    // На valcom-проекте, при устранении "missing currdata" обнаружено что код может вызывать большие задержки и даже
    // сбой
    // lockWrite( frontendLock );
    // try {
    for( IS5BackendAddon addon : allAddons ) {
      addon.onBackendMessage( aMessage );
    }
    // }
    // finally {
    // unlockWrite( frontendLock );
    // }
    frontend.onBackendMessage( aMessage );
  }

  /**
   * Возвращает механизм широковещательной рассылки сообщений от фронтенда к бекенду и всем фронтендам
   *
   * @return {@link GtMessageEventer} механизм сообщений
   */
  protected final IGtMessageEventer broadcastEventer() {
    return broadcastEventer;
  }

  /**
   * Возвращает механизм передачи сообщений от фронтенда к бекенду
   *
   * @return {@link GtMessageEventer} механизм сообщений
   */
  protected final IGtMessageEventer frontendEventer() {
    return frontendEventer;
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
   * @return {@link ILongOpProgressCallback} монитор прогресса
   */
  protected final ILongOpProgressCallback progressMonitor() {
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

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Создание контекста для бекенда
   *
   * @param aContext {@link ITsContextRo} родительский контекст
   * @param aSessionID {@link Skid} идентификатор сессии
   * @return {@link ITsContext} контекст бекенда
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static ITsContext createContextForBackend( ITsContextRo aContext, Skid aSessionID ) {
    TsNullArgumentRtException.checkNulls( aContext, aSessionID );
    TsContext ctx = new TsContext( new IAskParent() {

      @Override
      public IAtomicValue findOp( String aId ) {
        return aContext.params().findByKey( aId );
      }

      @Override
      public Object findRef( String aKey ) {
        return aContext.find( aKey );
      }

    } );
    ctx.params().setAll( aContext.params() );
    ctx.params().putAll( S5ConfigurationUtils.readSystemConfiguraion( IS5ConnectionParams.SYBSYSTEM_ID_PREFIX ) );
    ctx.params().setValobj( IS5ConnectionParams.OP_SESSION_ID, aSessionID );
    return ctx;
  }

  /**
   * Возвращает признак того, что расширение бекенда с указанным идентификатором входит в ядро
   *
   * @param aAddonId String идентификатор расширения
   * @return boolean <b>true</b> расширение ядра;<b>false</b> не расширение ядра.
   * @throws TsNullArgumentRtException аргумент = null
   */
  private static boolean isCoreAddon( String aAddonId ) {
    TsNullArgumentRtException.checkNull( aAddonId );
    return (//
    aAddonId.equals( IBaClasses.ADDON_ID ) || //
        aAddonId.equals( IBaObjects.ADDON_ID ) || //
        aAddonId.equals( IBaLinks.ADDON_ID ) || //
        aAddonId.equals( IBaClobs.ADDON_ID ) || //
        aAddonId.equals( IBaGwidDb.ADDON_ID ) || //
        aAddonId.equals( IBaRtdata.ADDON_ID ) || //
        aAddonId.equals( IBaEvents.ADDON_ID ) || //
        aAddonId.equals( IBaCommands.ADDON_ID ) || //
        aAddonId.equals( IBaQueries.ADDON_ID ) //
    ); //
  }
}
