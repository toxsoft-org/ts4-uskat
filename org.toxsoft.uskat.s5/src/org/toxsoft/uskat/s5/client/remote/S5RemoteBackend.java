package org.toxsoft.uskat.s5.client.remote;

import static org.toxsoft.core.log4j.Logger.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.s5.client.IS5ConnectionParams.*;
import static org.toxsoft.uskat.s5.client.remote.IS5Resources.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.strid.idgen.IStridGenerator;
import org.toxsoft.core.tslib.bricks.strid.idgen.UuidStridGenerator;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.IListEdit;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.client.remote.connection.*;
import org.toxsoft.uskat.s5.common.S5BackendDoJobThread;
import org.toxsoft.uskat.s5.common.S5FrontendRearCaller;
import org.toxsoft.uskat.s5.server.backend.*;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddon;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitResult;
import org.toxsoft.uskat.s5.utils.S5ValobjUtils;
import org.toxsoft.uskat.s5.utils.progress.IS5ProgressMonitor;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

import ru.uskat.backend.ISkBackendInfo;
import ru.uskat.backend.ISkFrontendRear;
import ru.uskat.backend.messages.*;
import ru.uskat.common.dpu.*;
import ru.uskat.core.api.ISkExtServicesProvider;
import ru.uskat.core.api.sysdescr.ISkSysdescrDpuReader;
import ru.uskat.core.api.users.ISkSession;
import ru.uskat.core.common.helpers.sysdescr.ISkSysdescrReader;
import ru.uskat.core.common.helpers.sysdescr.SkSysdescrReader;
import ru.uskat.core.connection.ISkConnection;
import ru.uskat.core.impl.AbstractSkService;
import ru.uskat.core.impl.SkUtils;
import ru.uskat.legacy.IdPair;

/**
 * Удаленный s5-backend
 *
 * @author mvk
 */
public final class S5RemoteBackend
    implements IS5Backend, IS5ConnectionListener, ICooperativeMultiTaskable {

  /**
   * Тайматут (мсек) ожидания блокировки {@link ISkConnection#mainLock()}
   */
  private static final long LOCK_TIMEOUT = 1000;

  /**
   * Параметры создания бекенда
   */
  private final ITsContextRo createBackendParams;

  /**
   * Используемый загрузчик классов
   */
  private final ClassLoader classLoader;

  /**
   * Задача (поток) обслуживания потребностей бекенда {@link ICooperativeMultiTaskable#doJob()}
   */
  private final S5BackendDoJobThread backendDojobThread;

  /**
   * Поставщик асинхронных вызовов фронтенду
   */
  private final S5FrontendRearCaller frontendCaller;

  /**
   * Блокировка доступа к frontend
   */
  private final S5Lockable frontendLock;

  /**
   * Соединение с s5-сервером
   */
  private final S5Connection connection;

  /**
   * Читатель системного описания
   */
  private final SkSysdescrReader sysdescrReader;

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
   * Список расширений бекенда поддерживаемых сервером
   */
  private final IListEdit<IS5BackendAddon> backendAddons = new ElemArrayList<>();

  /**
   * Список стабов расширений backend
   */
  private final IStridablesListEdit<S5BackendAddonRemote<?>> remotes = new StridablesList<>();

  /**
   * Удаленная ссылка на s5-backend
   */
  private volatile IS5BackendRemote remote;

  /**
   * Инициализация завершена
   */
  private volatile boolean inited;

  /**
   * Журнал работы
   */
  private ILogger logger = getLogger( getClass() );

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
   * @param aArgs {@link ITsContextRo} аргументы (ссылки и опции) создания бекенда
   * @param aFrontend {@link ISkFrontendRear} фронтенд, для которого создается бекенд
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5RemoteBackend( ITsContextRo aArgs, ISkFrontendRear aFrontend ) {
    TsNullArgumentRtException.checkNulls( aArgs, aFrontend );
    createBackendParams = aArgs;
    // Загручик классов
    classLoader = (aArgs.hasKey( REF_CLASSLOADER.refKey() ) ? //
        aArgs.getRef( REF_CLASSLOADER.refKey(), ClassLoader.class ) : //
        ClassLoader.getSystemClassLoader());
    // Монитор прогресса подключения
    IS5ProgressMonitor progressMonitor = (aArgs.hasKey( REF_MONITOR.refKey() ) ? //
        aArgs.getRef( REF_MONITOR.refKey(), IS5ProgressMonitor.class ) : //
        IS5ProgressMonitor.NULL);
    // Параметры аутентификации
    IAtomicValue login = SkUtils.OP_LOGIN.getValue( aArgs.params() );

    // Формирование идентификатора сессии
    sessionID = new Skid( ISkSession.CLASS_ID, login.asString() + "." + stridGenerator.nextId() ); //$NON-NLS-1$

    // Типизация набора свойств с первоначально проверкой допустимости их значений
    IOptionSetEdit configuration = new OptionSet( aArgs.params() );
    // Установка параметров
    OP_SESSION_ID.setValue( configuration, avValobj( sessionID ) );
    // Читатель системного описания
    sysdescrReader = new SkSysdescrReader( new ISkSysdescrDpuReader() {

      @Override
      public IStridablesList<IDpuSdTypeInfo> readTypeInfos() {
        return remote().readTypeInfos();
      }

      @Override
      public IStridablesList<IDpuSdClassInfo> readClassInfos() {
        return remote().readClassInfos();
      }
    } );

    // Создание блокировки соединения
    frontendLock = S5Lockable.addLockableIfNotExistToPool( aFrontend.mainLock(), sessionID.strid() );

    // Имя бекенда
    String name = "sessionID = " + sessionID.strid(); //$NON-NLS-1$
    // Механизм асинхронного вызова фронтенда
    frontendCaller = new S5FrontendRearCaller( name, new ISkFrontendRear() {

      @Override
      public void onGenericMessage( GenericMessage aMessage ) {
        if( aMessage.messageId().equals( SkMessageWhenSysdescrChanged.WHEN_SYSDESCR_CHANGED ) ) {
          // Перехват события об изменении системного описания
          sysdescrReader.invalidateCache();
        }
        aFrontend.onGenericMessage( aMessage );
      }

      @Override
      public ReentrantReadWriteLock mainLock() {
        return aFrontend.mainLock();
      }
    } );
    try {
      // Создание соединения
      connection = new S5Connection( sessionID, classLoader, frontendCaller, frontendLock );
      connection.addConnectionListener( this );
      // Подключение к серверу
      connection.openSession( configuration, progressMonitor );
      // Результат инициализации соединения
      IS5SessionInitResult result = connection.sessionInitResult();
      // Инициализация читателя системного описания
      sysdescrReader.setTypeInfos( result.typeInfos() );
      sysdescrReader.setClassInfos( result.classInfos() );
      // Задача (поток) обслуживания потребностей бекенда
    }
    catch( Throwable ex ) {
      // 2021-06-22 mvk fix thread qtty leaks
      frontendCaller.close();
      throw ex;
    }
    backendDojobThread = new S5BackendDoJobThread( name, this );
    // Инициализация завершена
    inited = true;
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвраащет соединение с s5-backend сервера
   *
   * @return {@link IS5Connection} соединение с s5-backend
   */
  public IS5Connection connection() {
    return connection;
  }

  /**
   * Возвращает читателя системного описания
   *
   * @return {@link ISkSysdescrReader} читатель системного описания
   */
  public ISkSysdescrReader sysdescrReader() {
    return sysdescrReader;
  }

  // ------------------------------------------------------------------------------------
  // ISkBackend
  //
  @Override
  public boolean isActive() {
    return (connection.state() == EConnectionState.CONNECTED);
  }

  @Override
  public ISkBackendInfo getInfo() {
    return remote().getInfo();
  }

  // ------------------------------------------------------------------------------------
  // ISkBackend - system description
  //
  @Override
  public IStridablesList<IDpuSdTypeInfo> readTypeInfos() {
    return sysdescrReader.readTypeInfos();
  }

  @Override
  public void writeTypeInfos( IStringList aRemoveTypeIds, IList<IDpuSdTypeInfo> aNewlyDefinedTypeInfos ) {
    TsNullArgumentRtException.checkNulls( aRemoveTypeIds, aNewlyDefinedTypeInfos );
    remote().writeTypeInfos( aRemoveTypeIds, aNewlyDefinedTypeInfos );
  }

  @Override
  public IStridablesList<IDpuSdClassInfo> readClassInfos() {
    return sysdescrReader.readClassInfos();
  }

  @Override
  public void writeClassInfos( IStringList aRemoveClassIds, IStridablesList<IDpuSdClassInfo> aUpdateClassInfos ) {
    TsNullArgumentRtException.checkNulls( aRemoveClassIds, aUpdateClassInfos );
    remote().writeClassInfos( aRemoveClassIds, aUpdateClassInfos );
  }

  // ------------------------------------------------------------------------------------
  // ISkBackend - objects management
  //
  @Override
  public IDpuObject findObject( Skid aSkid ) {
    TsNullArgumentRtException.checkNull( aSkid );
    return remote().findObject( aSkid );
  }

  @Override
  public IList<IDpuObject> readObjects( IStringList aClassIds ) {
    TsNullArgumentRtException.checkNull( aClassIds );
    return remote().readObjects( aClassIds );
  }

  @Override
  public IList<IDpuObject> readObjectsByIds( ISkidList aSkids ) {
    TsNullArgumentRtException.checkNull( aSkids );
    return remote().readObjectsByIds( aSkids );
  }

  @Override
  public void writeObjects( ISkidList aRemovedSkids, IList<IDpuObject> aObjects ) {
    TsNullArgumentRtException.checkNulls( aRemovedSkids, aObjects );
    remote().writeObjects( aRemovedSkids, aObjects );
  }

  // ------------------------------------------------------------------------------------
  // ISkBackend - links management
  //
  @Override
  public IDpuLinkFwd findLink( String aClassId, String aLinkId, Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNulls( aClassId, aLinkId, aLeftSkid );
    return remote().findLink( aClassId, aLinkId, aLeftSkid );
  }

  @Override
  public IDpuLinkFwd readLink( String aClassId, String aLinkId, Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNulls( aClassId, aLinkId, aLeftSkid );
    return remote().readLink( aClassId, aLinkId, aLeftSkid );
  }

  @Override
  public IDpuLinkRev readReverseLink( String aClassId, String aLinkId, Skid aRightSkid, IStringList aLeftClassIds ) {
    TsNullArgumentRtException.checkNulls( aClassId, aLinkId, aRightSkid, aLeftClassIds );
    return remote().readReverseLink( aClassId, aLinkId, aRightSkid, aLeftClassIds );
  }

  @Override
  public void writeLink( IDpuLinkFwd aLink ) {
    TsNullArgumentRtException.checkNull( aLink );
    remote().writeLink( aLink );
  }

  @Override
  public void writeLinks( IList<IDpuLinkFwd> aLinks ) {
    TsNullArgumentRtException.checkNull( aLinks );
    remote().writeLinks( aLinks );
  }

  @Override
  public IList<IdPair> listLobIds() {
    return remote().listLobIds();
  }

  @Override
  public void writeClob( IdPair aId, String aData ) {
    TsNullArgumentRtException.checkNulls( aId, aData );
    remote().writeClob( aId, aData );
  }

  @Override
  public boolean copyClob( IdPair aSourceId, IdPair aDestId ) {
    TsNullArgumentRtException.checkNulls( aSourceId, aDestId );
    return remote().copyClob( aSourceId, aDestId );
  }

  @Override
  public String readClob( IdPair aId ) {
    TsNullArgumentRtException.checkNull( aId );
    return remote().readClob( aId );
  }

  @Override
  public void removeLob( IdPair aId ) {
    TsNullArgumentRtException.checkNull( aId );
    remote().removeLob( aId );
  }

  @Override
  public ISkExtServicesProvider getExtServicesProvider() {
    return aCoreApi -> {
      IStringMapEdit<AbstractSkService> retValue = new StringMap<>();
      for( IS5BackendAddon addon : backendAddons ) {
        retValue.putAll( addon.createServices( aCoreApi ) );
      }
      return retValue;
    };
  }

  @Override
  public <T> T getBackendAddon( String aAddonId, Class<T> aAddonInterface ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aAddonInterface );
    try {
      return aAddonInterface.cast( remotes.findByKey( aAddonId ) );
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ex.getMessage() );
    }
  }

  @Override
  public void close( Skid aSessionID ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    remote().close( aSessionID );
  }

  // ------------------------------------------------------------------------------------
  // IS5ConnectionListener
  //
  @Override
  public void onBeforeConnect( IS5Connection aSource ) {
    // TODO: ??? нужна ли более тонкая обработка frontendCaller ???
    if( inited ) {
      SkMessageWhenSysdescrChanged.send( frontendCaller );
      SkMessageWhenObjectsChanged.send( frontendCaller, ISkidList.EMPTY );
    }
  }

  @Override
  public void onAfterDiscover( IS5Connection aSource ) {
    // Признак того, что соединение с сервером восстанавливается
    boolean restoreConnection = (remotes.size() != 0);
    if( !restoreConnection ) {
      // Создание расширений используемых клиентом (определяется наличием jar-расширения в classpath клиента)
      backendAddons.setAll( createBackendAddons( classLoader, aSource.backendAddonInfos(), logger() ) );
      // Создание удаленного доступа к расширениям бекенда
      for( IS5BackendAddon addon : backendAddons ) {
        S5BackendAddonRemote<?> addonRemoteClient = addon.createRemoteClient( createBackendParams );
        if( addonRemoteClient == null ) {
          // Расширение не работает с удаленными клиентами
          continue;
        }
        remotes.add( addonRemoteClient );
      }
      // Инициализация расширений
      for( S5BackendAddonRemote<?> r : remotes ) {
        r.init( this );
      }
    }
    for( S5BackendAddonRemote<?> r : remotes ) {
      r.onAfterDiscover( aSource );
    }
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
   * @return {@link IStridablesList}&lt;{@link IS5BackendAddon}&gt; список расширений бекенда.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( { "unchecked" } )
  private static IStridablesList<IS5BackendAddon> createBackendAddons( ClassLoader aClassLoader,
      IStringMap<String> aAddonInfos, ILogger aLogger ) {
    TsNullArgumentRtException.checkNulls( aClassLoader, aAddonInfos, aLogger );
    IStridablesListEdit<IS5BackendAddon> retValue = new StridablesList<>();
    for( String addonId : aAddonInfos.keys() ) {
      String addonClassName = aAddonInfos.getByKey( addonId );
      Class<IS5BackendAddon> implClassName = null;
      try {
        implClassName = (Class<IS5BackendAddon>)aClassLoader.loadClass( addonClassName );
      }
      catch( @SuppressWarnings( "unused" ) ClassNotFoundException e ) {
        // Предупреждение: класс расширения не найден в classpath клиента
        aLogger.warning( ERR_ADDON_IMPL_NOT_FOUND, addonId, addonClassName );
        continue;
      }
      try {
        IS5BackendAddon addon = implClassName.getConstructor().newInstance();
        retValue.add( addon );
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

  @Override
  public void onAfterConnect( IS5Connection aSource ) {
    remote = aSource.backend();
    for( S5BackendAddonRemote<?> addon : remotes ) {
      addon.onAfterConnect( aSource );
    }
    SkMessageWhenBackendStateChanged.send( frontendCaller, true );
  }

  @Override
  public void onAfterDisconnect( IS5Connection aSource ) {
    remote = null;
    for( S5BackendAddonRemote<?> addon : remotes ) {
      addon.onAfterDisconnect( aSource );
    }
    SkMessageWhenBackendStateChanged.send( frontendCaller, false );
  }

  // ------------------------------------------------------------------------------------
  // ICooperativeMultiTaskable
  //
  @Override
  public void doJob() {
    // 2020-10-12 mvk doJob + mainLock
    if( !tryLockWrite( frontendLock, LOCK_TIMEOUT ) ) {
      // Ошибка получения блокировки
      logger.warning( ERR_TRY_LOCK, frontendLock, frontendCaller.name() );
      return;
    }
    // try {
    // Thread.sleep( 1000000 * 1000 );
    // }
    // catch( InterruptedException ex ) {
    // // TODO Auto-generated catch block
    // ex.printStackTrace();
    // }
    // Получен доступ к блокировке.
    try {
      for( S5BackendAddonRemote<?> addon : remotes ) {
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
  public void close() {
    logger.info( "S5RemoteBackend.close(): starting ..." ); //$NON-NLS-1$
    frontendCaller.close();
    backendDojobThread.close();

    // 2021-03-23 mvk
    // Вызов frontendCaller.close() установил состояние 'interrupted' которое может не дать должны образом выполниться
    // следующему connection.closeSession() если close() вызывается из потоков frontendCaller.doJob или
    // backendDojobThread
    Thread.interrupted();

    connection.closeSession();
    // 2020-10-12 mvk doJob + mainLock
    S5Lockable.removeLockableFromPool( nativeLock( frontendLock ) );
    // Запись в журнал
    logger.info( "S5RemoteBackend.close(): ... finished" ); //$NON-NLS-1$
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //
  /**
   * Возвращает текущее состояние соединения с сервером
   *
   * @return {@link EConnectionState} состояние соединения
   */
  EConnectionState connectionState() {
    return connection.state();
  }

  /**
   * Возвращает удаленную ссылку на s5-backend
   *
   * @return {@link IS5BackendRemote} удаленная ссылка.
   * @throws TsIllegalStateRtException нет соединения с сервером
   */
  IS5BackendRemote remote() {
    IS5BackendRemote retValue = remote;
    if( retValue == null ) {
      throw new TsIllegalStateRtException( ERR_NO_CONNECTION );
    }
    return retValue;
  }

  /**
   * Возвращает удаленную ссылку на s5-backend
   *
   * @return {@link IS5BackendRemote} удаленная ссылка. null: нет связи
   */
  IS5BackendRemote findRemote() {
    return remote;
  }

  /**
   * Возвращает карту удаленного доступа к расширениям backend
   *
   * @param aAddonId String - идентификатор (ИД-путь) расширения
   * @param aAddonInterface - Java-тип интерфейс расширения ядра
   * @return {@link IS5BackendAddonRemote} удаленный доступ к расширению
   * @param <T> тип возвращаемого доступа
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException не найден удаленный доступ
   * @throws TsIllegalArgumentRtException ошибка приведения типа доступа к указанному интерфейсу
   */
  <T> T getBackendAddonRemote( String aAddonId, Class<T> aAddonInterface ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aAddonInterface );
    try {
      return aAddonInterface.cast( connection.sessionInitResult().addons().getByKey( aAddonId ) );
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ex.getMessage() );
    }
  }

  /**
   * Возвращает журнал работы
   *
   * @return {@link ILogger} журнал работы
   */
  ILogger logger() {
    return logger;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
