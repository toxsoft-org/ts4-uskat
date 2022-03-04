package org.toxsoft.uskat.s5.client.local;

import static java.lang.String.*;
import static org.toxsoft.uskat.s5.client.local.IS5Resources.*;
import static org.toxsoft.uskat.s5.client.local.S5ClusterCommandWhenObjectsChanged.*;
import static org.toxsoft.uskat.s5.client.local.S5ClusterCommandWhenSysdescrChanged.*;
import static org.toxsoft.uskat.s5.server.backend.supports.links.S5BackendLinksSingleton.*;
import static org.toxsoft.uskat.s5.server.backend.supports.lobs.S5BackendLobsSingleton.*;
import static org.toxsoft.uskat.s5.server.backend.supports.objects.S5BackendObjectsSingleton.*;
import static org.toxsoft.uskat.s5.server.backend.supports.sysdescr.S5BackendSysDescrSingleton.*;
import static org.toxsoft.uskat.s5.server.sessions.S5SessionUtils.*;
import static org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable.*;
import static ru.uskat.backend.messages.SkMessageWhenObjectsChanged.*;
import static ru.uskat.backend.messages.SkMessageWhenSysdescrChanged.*;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.toxsoft.core.log4j.Logger;
import org.toxsoft.core.tslib.bricks.ICooperativeMultiTaskable;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.common.S5BackendDoJobThread;
import org.toxsoft.uskat.s5.common.S5FrontendRearCaller;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.links.IS5BackendLinksSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.lobs.IS5BackendLobsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.objects.IS5BackendObjectsSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.sysdescr.IS5BackendSysDescrSingleton;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterManager;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.frontend.S5FrontendData;
import org.toxsoft.uskat.s5.server.sessions.*;
import org.toxsoft.uskat.s5.utils.threads.impl.S5Lockable;

import ru.uskat.backend.*;
import ru.uskat.common.dpu.*;
import ru.uskat.core.api.ISkBackend;
import ru.uskat.core.api.ISkExtServicesProvider;
import ru.uskat.core.api.users.ISkSession;
import ru.uskat.core.api.users.ISkUser;
import ru.uskat.core.common.helpers.sysdescr.ISkSysdescrReader;
import ru.uskat.core.connection.ISkConnection;
import ru.uskat.legacy.IdPair;

/**
 * Локальный s5-backend
 *
 * @author mvk
 */
public final class S5LocalBackend
    implements ISkBackend, IS5FrontendRear, ICooperativeMultiTaskable {

  /**
   * Тайматут (мсек) ожидания блокировки {@link ISkConnection#mainLock()}
   */
  private static final long LOCK_TIMEOUT = 1000;

  /**
   * Формат текстового представления
   */
  private static final String TO_STRING_FORMAT = "%s@%s[%s]"; //$NON-NLS-1$

  /**
   * Менеджер сессий s5-сервера
   */
  private final IS5SessionManager sessionManager;

  /**
   * Менеджер кластера s5-сервера
   */
  private final IS5ClusterManager clusterManager;

  /**
   * Имя модуля открывшего сессию
   */
  private final String moduleName;

  /**
   * Имя узла на котором модуль открыл сессию
   */
  private final String moduleNode;

  /**
   * Идентификатор сессии {@link ISkSession}. null: аутентификация проводится через ISkConnection
   */
  private final Skid sessionID;

  /**
   * Фронтенд
   */
  private final ISkFrontendRear frontend;

  /**
   * Блокировка к {@link #frontend}
   */
  private final S5Lockable frontendLock;

  /**
   * Данные фронтенда
   */
  private final S5FrontendData frontendData = new S5FrontendData();

  /**
   * Бекенд сервера
   */
  private final IS5BackendCoreSingleton backend;

  /**
   * Поддержка бекенда: системное описание
   */
  private final IS5BackendSysDescrSingleton sysdescrBackendSupport;

  /**
   * Поддержка бекенда: объекты системы
   */
  private final IS5BackendObjectsSingleton objectsBackendSupport;

  /**
   * Поддержка бекенда: связи объектов системы
   */
  private final IS5BackendLinksSingleton linksBackendSupport;

  /**
   * Поддержка бекенда: большие (Large OBject) данные системы
   */
  private final IS5BackendLobsSingleton lobsBackendSupport;

  /**
   * Список расширений backend
   */
  private final IStridablesList<S5BackendAddonLocal> addons;

  /**
   * Читатель системного описания
   */
  private final ISkSysdescrReader sysdescrReader;

  /**
   * Задача (поток) обслуживания потребностей бекенда {@link ICooperativeMultiTaskable#doJob()}
   */
  private final S5BackendDoJobThread backendDojobThread;

  /**
   * Поставщик асинхронных вызовов фронтенду
   */
  private final S5FrontendRearCaller frontendCaller;

  /**
   * Локальный бекенд завершил работу
   */
  private boolean closed = false;

  /**
   * Журнал работы
   */
  private final ILogger logger = Logger.getLogger( getClass() );

  /**
   * Конструктор backend
   *
   * @param aArgs {@link ITsContextRo} - аргументы (ссылки и опции) создания бекенда
   * @param aBackend {@link IS5BackendCoreSingleton} - backend сервера
   * @param aFrontend {@link ISkFrontendRear} - фронтенд, для которого создается бекенд
   * @param aAddonLocalClients {@link IStridablesList}&lt;{@link S5BackendAddonLocal}&gt; список расширений backend
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5LocalBackend( ITsContextRo aArgs, IS5BackendCoreSingleton aBackend, ISkFrontendRear aFrontend,
      IStridablesList<S5BackendAddonLocal> aAddonLocalClients ) {
    TsNullArgumentRtException.checkNulls( aArgs, aBackend, aFrontend, aAddonLocalClients );
    backend = aBackend;
    frontend = aFrontend;
    addons = new StridablesList<>( aAddonLocalClients );
    sysdescrBackendSupport = backend.get( BACKEND_SYSDESCR_ID, IS5BackendSysDescrSingleton.class );
    objectsBackendSupport = backend.get( BACKEND_OBJECTS_ID, IS5BackendObjectsSingleton.class );
    linksBackendSupport = backend.get( BACKEND_LINKS_ID, IS5BackendLinksSingleton.class );
    lobsBackendSupport = backend.get( BACKEND_LOBS_ID, IS5BackendLobsSingleton.class );
    sysdescrReader = sysdescrBackendSupport.getReader();
    sessionManager = TsNullArgumentRtException.checkNull( backend.sessionManager() );
    clusterManager = TsNullArgumentRtException.checkNull( backend.clusterManager() );
    moduleName = IS5LocalBackendHardConstants.OP_LOCAL_MODULE.getValue( aArgs.params() ).asString();
    moduleNode = IS5LocalBackendHardConstants.OP_LOCAL_NODE.getValue( aArgs.params() ).asString();

    // Определение идентификатора сессии
    boolean hasUserClass = (sysdescrBackendSupport.getReader().findClassInfo( ISkUser.CLASS_ID ) != null);
    boolean hasSessionClass = (sysdescrBackendSupport.getReader().findClassInfo( ISkSession.CLASS_ID ) != null);
    if( hasUserClass && hasSessionClass ) {
      // Cистемное описание инициализировано - аутентификация проводится бекендом. Создание локальной сессии
      sessionID = sessionManager.generateSessionID( moduleName, moduleNode );
      // Создание блокировки соединения
      frontendLock = S5Lockable.addLockableIfNotExistToPool( frontend.mainLock(), sessionID.strid() );
      // Создание локальной сессии
      S5LocalSession session = new S5LocalSession( sessionID, moduleName, moduleNode, this );
      // Создание локальной сессии
      sessionManager.createLocalSession( session );
    }
    else {
      // Аутентификация проводится через ISkConnection
      sessionID = Skid.NONE;
      // Создание блокировки соединения
      frontendLock = S5Lockable.addLockableIfNotExistToPool( frontend.mainLock(), sessionID.strid() );
    }

    backend.attachFrontend( this );
    // Инициализация расширений
    for( S5BackendAddonLocal addon : addons ) {
      addon.init( this, aBackend );
    }
    String name = "sessionID = " + sessionID.strid(); //$NON-NLS-1$
    frontendCaller = new S5FrontendRearCaller( name, aFrontend );
    backendDojobThread = new S5BackendDoJobThread( name, this );
  }

  // ------------------------------------------------------------------------------------
  // ISkBackend
  //
  @Override
  public boolean isActive() {
    return backend.isActive();
  }

  @Override
  public ISkBackendInfo getInfo() {
    // Запрос текущей информации о сервере (backend)
    ISkBackendInfo backendInfo = backend.getInfo();
    // Формирование информации сессии
    return new SkBackendInfo( backendInfo.id(), backendInfo.startTime(), sessionID, backendInfo.params() );
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
    sysdescrBackendSupport.writeTypeInfos( aRemoveTypeIds, aNewlyDefinedTypeInfos );
  }

  @Override
  public IStridablesList<IDpuSdClassInfo> readClassInfos() {
    return sysdescrReader.readClassInfos();
  }

  @Override
  public void writeClassInfos( IStringList aRemoveClassIds, IStridablesList<IDpuSdClassInfo> aUpdateClassInfos ) {
    TsNullArgumentRtException.checkNulls( aRemoveClassIds, aUpdateClassInfos );
    sysdescrBackendSupport.writeClassInfos( aRemoveClassIds, aUpdateClassInfos );
  }

  // ------------------------------------------------------------------------------------
  // ISkBackend - objects management
  //
  @Override
  public IDpuObject findObject( Skid aSkid ) {
    TsNullArgumentRtException.checkNull( aSkid );
    return objectsBackendSupport.findObject( aSkid );
  }

  @Override
  public IList<IDpuObject> readObjects( IStringList aClassIds ) {
    TsNullArgumentRtException.checkNull( aClassIds );
    return objectsBackendSupport.readObjects( aClassIds );
  }

  @Override
  public IList<IDpuObject> readObjectsByIds( ISkidList aSkids ) {
    TsNullArgumentRtException.checkNull( aSkids );
    return objectsBackendSupport.readObjectsByIds( aSkids );
  }

  @Override
  public void writeObjects( ISkidList aRemovedSkids, IList<IDpuObject> aObjects ) {
    TsNullArgumentRtException.checkNulls( aRemovedSkids, aObjects );
    // aInterceptable = true
    objectsBackendSupport.writeObjects( this, aRemovedSkids, aObjects, true );
  }

  // ------------------------------------------------------------------------------------
  // ISkBackend - links management
  //
  @Override
  public IDpuLinkFwd findLink( String aClassId, String aLinkId, Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNulls( aClassId, aLinkId, aLeftSkid );
    return linksBackendSupport.findLink( aClassId, aLinkId, aLeftSkid );
  }

  @Override
  public IDpuLinkFwd readLink( String aClassId, String aLinkId, Skid aLeftSkid ) {
    TsNullArgumentRtException.checkNulls( aClassId, aLinkId, aLeftSkid );
    return linksBackendSupport.readLink( aClassId, aLinkId, aLeftSkid );
  }

  @Override
  public IDpuLinkRev readReverseLink( String aClassId, String aLinkId, Skid aRightSkid, IStringList aLeftClassIds ) {
    TsNullArgumentRtException.checkNulls( aClassId, aLinkId, aRightSkid, aLeftClassIds );
    return linksBackendSupport.readReverseLink( aClassId, aLinkId, aRightSkid, aLeftClassIds );
  }

  @Override
  public void writeLink( IDpuLinkFwd aLink ) {
    TsNullArgumentRtException.checkNull( aLink );
    linksBackendSupport.writeLink( aLink );
  }

  @Override
  public void writeLinks( IList<IDpuLinkFwd> aLinks ) {
    TsNullArgumentRtException.checkNull( aLinks );
    linksBackendSupport.writeLinks( aLinks, true );
  }

  @Override
  public IList<IdPair> listLobIds() {
    return lobsBackendSupport.listLobIds();
  }

  @Override
  public void writeClob( IdPair aId, String aData ) {
    TsNullArgumentRtException.checkNulls( aId, aData );
    lobsBackendSupport.writeClob( aId, aData );
  }

  @Override
  public boolean copyClob( IdPair aSourceId, IdPair aDestId ) {
    TsNullArgumentRtException.checkNulls( aSourceId, aDestId );
    return lobsBackendSupport.copyClob( aSourceId, aDestId );
  }

  @Override
  public String readClob( IdPair aId ) {
    TsNullArgumentRtException.checkNull( aId );
    return lobsBackendSupport.readClob( aId );
  }

  @Override
  public void removeLob( IdPair aId ) {
    TsNullArgumentRtException.checkNull( aId );
    lobsBackendSupport.removeClob( aId );
  }

  @Override
  public ISkExtServicesProvider getExtServicesProvider() {
    return backend.initialConfig().impl().getExtServicesProvider();
  }

  @Override
  public <T> T getBackendAddon( String aAddonId, Class<T> aAddonInterface ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aAddonInterface );
    try {
      return aAddonInterface.cast( addons.findByKey( aAddonId ) );
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ex.getMessage() );
    }
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
    // Получен доступ к блокировке.
    try {
      for( S5BackendAddonLocal addon : addons ) {
        if( closed ) {
          // Бекенд уже завершил свою работу
          return;
        }
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
    closed = true;
    frontendCaller.close();
    backendDojobThread.close();
    backend.detachFrontend( this );
    if( sessionID != Skid.NONE ) {
      sessionManager.closeLocalSession( sessionID );
    }
    // 2020-10-12 mvk doJob + mainLock
    S5Lockable.removeLockableFromPool( nativeLock( frontendLock ) );
  }

  // ------------------------------------------------------------------------------------
  // IS5FrontendRear
  //
  @Override
  public Skid sessionID() {
    return sessionID;
  }

  @Override
  public void onGenericMessage( GenericMessage aMessage ) {
    TsNullArgumentRtException.checkNull( aMessage );
    // Асинхронная передача сообщения
    frontendCaller.onGenericMessage( aMessage );
    // Перехват сообщений об изменении системного описания - оповещение по кластеру
    if( WHEN_SYSDESCR_CHANGED.equals( aMessage.messageId() ) ) {
      // Оповещение удаленных узлов кластера: boolean remoteOnly = true; boolean primaryOnly = false;
      clusterManager.sendAsyncCommand( whenSysdescrChangedCommand(), true, false );
    }
    if( WHEN_OBJECTS_CHANGED.equals( aMessage.messageId() ) ) {
      ISkidList objectIds = aMessage.args().getValobj( ARG_OBJECT_IDS );
      // Оповещение удаленных узлов кластера: boolean remoteOnly = true; boolean primaryOnly = false;
      clusterManager.sendAsyncCommand( whenObjectsChangedCommand( objectIds ), true, false );
    }
  }

  @Override
  public ReentrantReadWriteLock mainLock() {
    return frontend.mainLock();
  }

  @Override
  public S5FrontendData frontendData() {
    return frontendData;
  }

  // ------------------------------------------------------------------------------------
  // Методы пакета
  //
  /**
   * Возвращает frontend
   *
   * @return {@link ISkFrontendRear} frontend
   */
  ISkFrontendRear frontend() {
    return frontend;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //
  @Override
  public String toString() {
    return format( TO_STRING_FORMAT, moduleName, moduleNode, sessionIDToString( sessionID, true ) );
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + sessionID.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object aObject ) {
    if( this == aObject ) {
      return true;
    }
    if( aObject == null ) {
      return false;
    }
    if( getClass() != aObject.getClass() ) {
      return false;
    }
    S5LocalBackend other = (S5LocalBackend)aObject;
    if( sessionID == null ) {
      return (other.sessionID == null);
    }
    if( !sessionID.equals( other.sessionID ) ) {
      return false;
    }
    return true;
  }
}
