package org.toxsoft.uskat.s5.client.remote;

import static org.toxsoft.uskat.s5.client.remote.IS5Resources.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.coll.helpers.ECrudOp;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.ISkFrontendRear;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.client.remote.connection.*;
import org.toxsoft.uskat.s5.client.remote.connection.pas.S5CallbackOnMessage;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSession;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.messages.*;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitData;

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
      S5CallbackOnMessage.send( connection.callbackTxChannel(), aMessage );
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
      // Создание расширений используемых клиентом (определяется наличием jar-расширения в classpath клиента)
      allAddons().putAll( createBackendAddons( classLoader(), aSource.backendAddonInfos(), logger() ) );
      // Формирование сообщения о проведенной инициализации расширений
      fireBackendMessage( S5BaAfterInitMessages.INSTANCE.makeMessage() );
    }
    // Формирование сообщения о предстоящем соединении с бекендом
    fireBackendMessage( S5BaBeforeConnectMessages.INSTANCE.makeMessage() );
  }

  @Override
  public void onAfterConnect( IS5Connection aSource ) {
    session = aSource.session();
    fireBackendMessage( S5BaAfterConnectMessages.INSTANCE.makeMessage() );
  }

  @Override
  public void onAfterDisconnect( IS5Connection aSource ) {
    session = null;
    fireBackendMessage( S5BaAfterDisconnectMessages.INSTANCE.makeMessage() );
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
  protected ISkBackendInfo doFindServerBackendInfo() {
    return (connection.state() == EConnectionState.CONNECTED ? session().getBackendInfo() : null);
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
