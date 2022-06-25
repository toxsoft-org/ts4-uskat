package org.toxsoft.uskat.s5.client.remote;

import static org.toxsoft.uskat.s5.client.remote.IS5Resources.*;

import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.coll.helpers.ECrudOp;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.ISkFrontendRear;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.s5.client.remote.connection.*;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSession;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.messages.*;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitResult;

/**
 * Удаленный s5-backend
 *
 * @author mvk
 */
public final class S5BackendRemote
    extends S5AbstractBackend<IS5BackendAddonRemote>
    implements IS5BackendRemote, IS5ConnectionListener {

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
   * @param aFrontend {@link ISkFrontendRear} фронтенд, для которого создается бекенд
   * @param aArgs {@link ITsContextRo} аргументы (ссылки и опции) создания бекенда
   *
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5BackendRemote( ISkFrontendRear aFrontend, ITsContextRo aArgs ) {
    super( aFrontend, aArgs );
    // Создание соединения
    connection = new S5Connection( sessionID(), classLoader(), frontend(), frontendLock() );
    connection.addConnectionListener( this );
    // Подключение к серверу
    connection.openSession( openArgs().params(), progressMonitor() );
    // Признак получения соединения
    wasConnect = true;
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendRemote
  //
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

  @Override
  public ISkBackendInfo getBackendInfo() {
    return session().getBackendInfo();
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
      // Создание расширений используемых клиентом (определяется наличием jar-расширения в classpath клиента)
      allAddons().putAll( createBackendAddons( classLoader(), aSource.backendAddonInfos(), logger() ) );
      // Формирование сообщения о проведенной инициализации расширений
      fireBackendMessage( IS5BaAfterInitMessages.makeMessage() );
    }
    // Формирование сообщения о предстоящем соединении с бекендом
    fireBackendMessage( IS5BaBeforeConnectMessages.makeMessage() );
  }

  @Override
  public void onAfterConnect( IS5Connection aSource ) {
    session = aSource.session();
    fireBackendMessage( IS5BaAfterConnectMessages.makeMessage() );
  }

  @Override
  public void onAfterDisconnect( IS5Connection aSource ) {
    session = null;
    fireBackendMessage( IS5BaAfterDisconnectMessages.makeMessage() );
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
  @Override
  protected boolean doIsLocal() {
    return false;
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
