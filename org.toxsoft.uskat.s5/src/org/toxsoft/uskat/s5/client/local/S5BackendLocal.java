package org.toxsoft.uskat.s5.client.local;

import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkFrontendRear;
import org.toxsoft.uskat.core.backend.api.ISkBackendInfo;
import org.toxsoft.uskat.core.impl.SkBackendInfo;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonLocal;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackend;
import org.toxsoft.uskat.s5.server.backend.messages.*;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionManager;
import org.toxsoft.uskat.s5.server.sessions.S5LocalSession;

/**
 * Локальный s5-backend
 *
 * @author mvk
 */
public final class S5BackendLocal
    extends S5AbstractBackend<IS5BackendAddonLocal> {

  /**
   * Синглетон реализующий бекенд
   */
  private final IS5BackendCoreSingleton backendSingleton;

  /**
   * Менеджер сессий s5-сервера
   */
  private final IS5SessionManager sessionManager;

  /**
   * Конструктор backend
   *
   * @param aArgs {@link ITsContextRo} - аргументы (ссылки и опции) создания бекенда
   * @param aFrontend {@link ISkFrontendRear} - фронтенд, для которого создается бекенд
   * @param aBackendSingleton {@link IS5BackendCoreSingleton} - backend сервера
   * @param aAddonLocalClients {@link IStridablesList}&lt;{@link IS5BackendAddonLocal}&gt; список расширений backend
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5BackendLocal( ITsContextRo aArgs, ISkFrontendRear aFrontend, IS5BackendCoreSingleton aBackendSingleton,
      IStridablesList<IS5BackendAddonLocal> aAddonLocalClients ) {
    super( aArgs, aFrontend );
    TsNullArgumentRtException.checkNulls( aBackendSingleton, aAddonLocalClients );
    backendSingleton = aBackendSingleton;
    sessionManager = TsNullArgumentRtException.checkNull( backendSingleton.sessionManager() );

    // Установка аддонов бекенда
    for( IS5BackendAddonLocal addon : aAddonLocalClients ) {
      allAddons().put( addon.id(), addon );
    }

    // Создание локальной сессии
    String programName = IS5ConnectionParams.OP_LOCAL_MODULE.getValue( aArgs.params() ).asString();
    String userName = IS5ConnectionParams.OP_LOCAL_NODE.getValue( aArgs.params() ).asString();
    S5LocalSession session = new S5LocalSession( sessionID(), programName, userName, frontend() );
    // Создание локальной сессии
    sessionManager.createLocalSession( session );

    // Подключение фронтенда
    backendSingleton.attachFrontend( frontend() );
    // Формирование сообщения о проведенной инициализации расширений
    fireBackendMessage( IS5BaAfterInitMessages.makeMessage() );
    // Формирование сообщения о предстоящем подключении
    fireBackendMessage( IS5BaBeforeConnectMessages.makeMessage() );
    // Формирование сообщения о подключении
    fireBackendMessage( IS5BaAfterConnectMessages.makeMessage() );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Возвращает синглетон реализующий бекенд для доступа расширений бекенда к функциям {@link IS5BackendCoreSingleton}
   *
   * @return {@link IS5BackendCoreSingleton} синлетон бекенда
   */
  public IS5BackendCoreSingleton backendSingleton() {
    return backendSingleton;
  }

  // ------------------------------------------------------------------------------------
  // ISkBackend
  //
  @Override
  public boolean isActive() {
    return backendSingleton.isActive();
  }

  @Override
  public ISkBackendInfo getBackendInfo() {
    // Запрос текущей информации о сервере (backend)
    ISkBackendInfo backendInfo = backendSingleton.getInfo();
    // Формирование информации сессии
    return new SkBackendInfo( backendInfo.id(), backendInfo.startTime(), sessionID(), backendInfo.params() );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных и абстрактных методов базового класса
  //
  @Override
  protected boolean doIsLocal() {
    return true;
  }

  @Override
  public void doClose() {
    backendSingleton.detachFrontend( frontend() );
    sessionManager.closeLocalSession( sessionID() );
  }

}
