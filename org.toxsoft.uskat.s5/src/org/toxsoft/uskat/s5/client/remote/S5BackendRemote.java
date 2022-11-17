package org.toxsoft.uskat.s5.client.remote;

import static org.toxsoft.uskat.s5.client.remote.IS5Resources.*;
import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;

import java.lang.reflect.InvocationTargetException;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.helpers.ECrudOp;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.LoggerUtils;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.ISkFrontendRear;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.client.remote.connection.*;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSession;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.messages.*;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitData;
import org.toxsoft.uskat.s5.server.sessions.pas.S5CallbackOnFrontendMessage;

/**
 * Удаленный s5-backend
 *
 * @author mvk
 */
public final class S5BackendRemote
    extends S5AbstractBackend<IS5BackendAddonRemote>
    implements IS5BackendRemote, IS5ConnectionListener {

  /**
   * Идентификатор бекенда возвращаемый как {@link ISkBackendInfo#id()}.
   */
  public static final String BACKEND_ID = ISkBackendHardConstant.SKB_ID + ".s5.remote"; //$NON-NLS-1$

  /**
   * Соединение с s5-сервером
   */
  private final S5Connection connection;

  /**
   * Признак того, что было установлено соединение
   */
  private volatile boolean wasConnect;

  /**
   * Сессия бекенда на сервере
   */
  private volatile IS5BackendSession session;

  /**
   * Конструктор backend
   *
   * @param aFrontend {@link ISkFrontendRear} фронтенд, для которого создается бекенд
   * @param aArgs {@link ITsContextRo} аргументы (ссылки и опции) создания бекенда
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5BackendRemote( ISkFrontendRear aFrontend, ITsContextRo aArgs ) {
    super( aFrontend, aArgs, BACKEND_ID, IOptionSet.NULL );
    // Создание соединения
    connection = new S5Connection( sessionID(), classLoader(), frontend(), frontendLock() );
    connection.addConnectionListener( this );
    // Слушаем сообщения фроненда и передаем их бекенду через pas-канал
    eventer().addListener( aMessage -> {
      // Передача сообщения серверу
      S5CallbackOnFrontendMessage.send( connection.callbackTxChannel(), aMessage );
    } );
    // Соединение открывается в doInitialize
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendRemote
  //
  @Override
  public S5SessionInitData sessionInitData() {
    return connection.sessionInitData();
  }

  @Override
  public IS5SessionInitResult sessionInitResult() {
    return connection.sessionInitResult();
  }

  @Override
  public <SESSION extends IS5BackendAddonSession> SESSION getBaSession( String aAddonId,
      Class<SESSION> aAddonSessionClass ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aAddonSessionClass );
    try {
      return aAddonSessionClass.cast( connection.sessionInitResult().baSessions().getByKey( aAddonId ) );
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ex.getMessage() );
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkBackend
  //
  @Override
  public boolean isActive() {
    return (connection.state() == EConnectionState.CONNECTED);
  }

  // ------------------------------------------------------------------------------------
  // IS5ConnectionListener
  //
  @Override
  public void onBeforeConnect( IS5Connection aSource ) {
    if( wasConnect ) {
      // Эмуляция сообщений о возможном изменении классов, объектов и связей при восстановлении связи с сервером
      // aClassId = null (all classes)
      fireBackendMessage( IBaClassesMessages.makeMessage( ECrudOp.LIST, null ) );
      fireBackendMessage( IBaObjectsMessages.makeMessage( ECrudOp.LIST, null ) );
      // TODO: ???
      // fireBackendMessage( IBaLinksMessages.makeMessage( ECrudOp.LIST, null ) );
    }
  }

  @Override
  public void onAfterDiscover( IS5Connection aSource ) {
    if( !wasConnect ) {
      // Формирование сообщения о предстоящей инициализации расширений
      fireBackendMessage( S5BaBeforeInitMessages.INSTANCE.makeMessage() );
      // Установка построителей расширений бекенда
      setBaCreators( createBaCreators( classLoader(), aSource.baCreatorClasses() ) );
      // Формирование сообщения о проведенной инициализации расширений
      fireBackendMessage( S5BaAfterInitMessages.INSTANCE.makeMessage() );
    }
    // Формирование сообщения о предстоящем соединении с бекендом
    fireBackendMessage( S5BaBeforeConnectMessages.INSTANCE.makeMessage() );
  }

  @Override
  public void onAfterConnect( IS5Connection aSource ) {
    session = aSource.session();
    // Сообщение об активации бекенда не должно быть в конструкторе, так как слушатели состояния соединения могут
    // получить сообщение об активации соединения при еще не до конца созданном соединении
    if( isInited() ) {
      // Формирование сообщения об изменении состояния бекенда: active = true
      fireBackendMessage( BackendMsgActiveChanged.INSTANCE.makeMessage( true ) );
    }
  }

  @Override
  public void onAfterDisconnect( IS5Connection aSource ) {
    session = null;
    // Формирование сообщения об изменении состояния бекенда: active = false
    fireBackendMessage( BackendMsgActiveChanged.INSTANCE.makeMessage( false ) );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных и абстрактных методов базового класса
  //
  @Override
  public void doClose() {
    session().close();
    connection.closeSession();
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
   * @return {@link IS5BackendSession} удаленная ссылка.
   * @throws TsIllegalStateRtException нет соединения с сервером
   */
  IS5BackendSession session() {
    IS5BackendSession retValue = session;
    if( retValue == null ) {
      throw new TsIllegalStateRtException( ERR_NO_CONNECTION );
    }
    return retValue;
  }

  /**
   * Возвращает удаленную ссылку на s5-backend
   *
   * @return {@link IS5BackendSession} удаленная ссылка. null: нет связи
   */
  IS5BackendSession findSession() {
    return session;
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов
  //
  /**
   * Провести инициализацию бекенда в наследнике. Вызывается после вызова конструктора
   */
  @Override
  protected void doInitialize() {
    // Подключение к серверу
    connection.openSession( openArgs().params(), progressMonitor() );
    // Признак получения соединения
    wasConnect = true;
  }

  @Override
  protected boolean doIsLocal() {
    return false;
  }

  @Override
  protected IStringMap<IS5BackendAddonRemote> doCreateAddons( IStridablesList<IS5BackendAddonCreator> aBaCreators ) {
    IStringMapEdit<IS5BackendAddonRemote> retValue = new StringMap<>();
    // Создание и установка аддонов бекенда
    for( IS5BackendAddonCreator baCreator : aBaCreators ) {
      IS5BackendAddonRemote ba = baCreator.createRemote( this );
      retValue.put( ba.id(), ba );
    }
    return retValue;
  }

  @Override
  protected ISkBackendInfo doFindServerBackendInfo() {
    return (connection.state() == EConnectionState.CONNECTED ? session().getBackendInfo() : null);
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Создает список расширений бекенда
   * <p>
   * Если класс реализации расширения не найден в classpath клиента, то выводится предупреждение
   *
   * @param aClassLoader {@link ClassLoader} используемый загрузчик классов
   * @param aBaCreatorClasses {@link IStringMap}&lt;String&gt; карта имен классов построителей расширений бекенда.
   *          <p>
   *          Ключ: идентификатор расширения {@link IS5BackendAddon#id()};<br>
   *          Значение: полное имя java-класса реализующий расширение построитель расширения
   *          {@link IS5BackendAddonCreator}.
   * @return {@link IStridablesList}&lt;{@link IS5BackendAddon}&gt; список расширений бекенда.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  @SuppressWarnings( { "unchecked" } )
  private static IStridablesList<IS5BackendAddonCreator> createBaCreators( ClassLoader aClassLoader,
      IStringMap<String> aBaCreatorClasses ) {
    TsNullArgumentRtException.checkNulls( aClassLoader, aBaCreatorClasses );
    IStridablesListEdit<IS5BackendAddonCreator> retValue = new StridablesList<>();
    for( String addonId : aBaCreatorClasses.keys() ) {
      String baCreatorClassName = aBaCreatorClasses.getByKey( addonId );
      Class<IS5BackendAddonCreator> implClassName = null;
      try {
        implClassName = (Class<IS5BackendAddonCreator>)aClassLoader.loadClass( baCreatorClassName );
      }
      catch( ClassNotFoundException e ) {
        // Не найден класс построителя расширения бекенда в classpath клиента
        LoggerUtils.errorLogger().error( e, ERR_BA_CREATOR_NOT_FOUND, addonId, baCreatorClassName );
        continue;
      }
      try {
        IS5BackendAddonCreator baCreator = implClassName.getConstructor().newInstance();
        retValue.put( addonId, baCreator );
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
