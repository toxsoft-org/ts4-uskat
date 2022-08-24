package org.toxsoft.uskat.s5.client.local;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.ctx.ITsContextRo;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.ISkFrontendRear;
import org.toxsoft.uskat.core.backend.api.ISkBackendInfo;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;
import org.toxsoft.uskat.s5.common.sessions.IS5SessionInfo;
import org.toxsoft.uskat.s5.server.IS5ServerHardConstants;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.impl.S5BackendInfo;
import org.toxsoft.uskat.s5.server.backend.messages.*;
import org.toxsoft.uskat.s5.server.sessions.*;

/**
 * Локальный s5-backend
 *
 * @author mvk
 */
public final class S5BackendLocal
    extends S5AbstractBackend<IS5BackendAddonLocal>
    implements IS5BackendLocal {

  /**
   * Идентификатор бекенда возвращаемый как {@link ISkBackendInfo#id()}.
   */
  public static final String BACKEND_ID = ISkBackendHardConstant.SKB_ID + ".s5.local"; //$NON-NLS-1$

  /**
   * Построители расширений бекенда.
   */
  private final IStridablesList<IS5BackendAddonCreator> baCreators;
  /**
   * Синглетон реализующий бекенд
   */
  private final IS5BackendCoreSingleton                 backendSingleton;

  /**
   * Менеджер сессий s5-сервера
   */
  private final IS5SessionManager sessionManager;

  /**
   * Конструктор backend
   *
   * @param aFrontend {@link ISkFrontendRear} - фронтенд, для которого создается бекенд
   * @param aArgs {@link ITsContextRo} - аргументы (ссылки и опции) создания бекенда
   * @param aBackendSingleton {@link IS5BackendCoreSingleton} - backend сервера
   * @param aBackendAddonCreators {@link IStridablesList}&lt; {@link IS5BackendAddonCreator}&gt; список построителей
   *          расширений бекенда.
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5BackendLocal( ISkFrontendRear aFrontend, ITsContextRo aArgs, IS5BackendCoreSingleton aBackendSingleton,
      IStridablesList<IS5BackendAddonCreator> aBackendAddonCreators ) {
    super( aFrontend, aArgs, BACKEND_ID, IOptionSet.NULL );
    TsNullArgumentRtException.checkNulls( aBackendSingleton, aBackendAddonCreators );
    baCreators = aBackendAddonCreators;
    backendSingleton = aBackendSingleton;
    sessionManager = TsNullArgumentRtException.checkNull( backendSingleton.sessionManager() );
  }

  // ------------------------------------------------------------------------------------
  // IS5BackendLocal
  //
  @Override
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

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных и абстрактных методов базового класса
  //
  /**
   * Провести инициализацию бекенда в наследнике. Вызывается после вызова конструктора
   */
  @Override
  protected void doInitialize() {
    // Формирование сообщения о предстоящей инициализации расширений
    fireBackendMessage( S5BaBeforeInitMessages.INSTANCE.makeMessage() );

    // Создание и установка аддонов бекенда
    for( IS5BackendAddonCreator baCreator : baCreators ) {
      IS5BackendAddonLocal ba = baCreator.createLocal( this );
      allAddons().put( ba.id(), ba );
    }

    // Создание локальной сессии
    String programName = IS5ConnectionParams.OP_LOCAL_MODULE.getValue( openArgs().params() ).asString();
    String userName = IS5ConnectionParams.OP_LOCAL_NODE.getValue( openArgs().params() ).asString();
    S5LocalSession session = new S5LocalSession( sessionID(), programName, userName, frontend() );
    // Создание локальной сессии
    sessionManager.createLocalSession( session );

    // Подключение фронтенда
    backendSingleton.attachFrontend( frontend() );
    // Формирование сообщения о проведенной инициализации расширений
    fireBackendMessage( S5BaAfterInitMessages.INSTANCE.makeMessage() );
    // Формирование сообщения о предстоящем подключении
    fireBackendMessage( S5BaBeforeConnectMessages.INSTANCE.makeMessage() );
    // Формирование сообщения о подключении
    fireBackendMessage( S5BaAfterConnectMessages.INSTANCE.makeMessage() );
  }

  @Override
  protected boolean doIsLocal() {
    return true;
  }

  @Override
  protected ISkBackendInfo doFindServerBackendInfo() {
    // Запрос текущей информации о сервере (backend)
    ISkBackendInfo backendInfo = backendSingleton.getInfo();
    // Данные сессии
    S5SessionData sessionData = sessionManager.findSessionData( sessionID() );
    if( sessionData == null ) {
      // Вызов информации бекенда ДО создания сессии. Такое возможно смотри SkCoreApi, создание бекенда
      return backendInfo;
    }
    // Описание текущей сессии пользователя
    IS5SessionInfo sessionInfo = sessionData.info();
    // Формирование информации сессии бекенда
    S5BackendInfo retValue = new S5BackendInfo( backendInfo.id(), backendInfo.params() );
    // Идентификатор текущей сессии пользователя
    IS5ServerHardConstants.OP_BACKEND_SESSION_INFO.setValue( retValue.params(), avValobj( sessionInfo ) );

    return retValue;
  }

  @Override
  public void doClose() {
    backendSingleton.detachFrontend( frontend() );
    sessionManager.closeLocalSession( sessionID() );
  }

}
