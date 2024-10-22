package org.toxsoft.uskat.s5.client.local;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.s5.client.*;
import org.toxsoft.uskat.s5.server.backend.*;
import org.toxsoft.uskat.s5.server.backend.addons.*;
import org.toxsoft.uskat.s5.server.backend.impl.*;
import org.toxsoft.uskat.s5.server.backend.messages.*;
import org.toxsoft.uskat.s5.server.sessions.*;

/**
 * Локальный s5-backend.
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
   * Синглетон реализующий бекенд.
   */
  private final IS5BackendCoreSingleton backendSingleton;

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
   * @param aBaCreators {@link IStridablesList}&lt; {@link IS5BackendAddonCreator}&gt; карта построителей.
   *          <p>
   *          Ключ: идентификатор расширения {@link IS5BackendAddon#id()};<br>
   *          Значение: построитель расширения {@link IS5BackendAddonCreator}.
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5BackendLocal( ISkFrontendRear aFrontend, ITsContextRo aArgs, IS5BackendCoreSingleton aBackendSingleton,
      IStridablesList<IS5BackendAddonCreator> aBaCreators ) {
    super( aFrontend, aArgs, BACKEND_ID, IOptionSet.NULL );
    TsNullArgumentRtException.checkNulls( aBackendSingleton, aBaCreators );
    backendSingleton = aBackendSingleton;
    sessionManager = TsNullArgumentRtException.checkNull( backendSingleton.sessionManager() );
    setBaCreators( aBaCreators );
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
    // Формирование сообщения об изменении состояния бекенда: active = true
    fireBackendMessage( BackendMsgActiveChanged.INSTANCE.makeMessage( true ) );
  }

  @Override
  protected boolean doIsLocal() {
    return true;
  }

  @Override
  protected IStringMap<IS5BackendAddonLocal> doCreateAddons( IStridablesList<IS5BackendAddonCreator> aBaCreators ) {
    IStringMapEdit<IS5BackendAddonLocal> retValue = new StringMap<>();
    // Создание и установка аддонов бекенда
    for( IS5BackendAddonCreator baCreator : aBaCreators ) {
      IS5BackendAddonLocal ba = baCreator.createLocal( this );
      retValue.put( ba.id(), ba );
    }
    return retValue;
  }

  @Override
  protected ISkBackendInfo doFindServerBackendInfo() {
    // Запрос текущей информации о сервере (backend)
    ISkBackendInfo backendInfo = backendSingleton.getInfo();
    // Формирование информации сессии бекенда
    S5BackendInfo retValue = new S5BackendInfo( backendInfo );
    // Информация о зарегистрированном пользователе
    SkLoggedUserInfo loggedUserInfo =
        new SkLoggedUserInfo( new Skid( ISkUser.CLASS_ID, ISkUserServiceHardConstants.USER_ID_ROOT ),
            ISkUserServiceHardConstants.SKID_ROLE_ROOT, ESkAuthentificationType.SIMPLE );
    ISkBackendHardConstant.OPDEF_SKBI_LOGGED_USER.setValue( retValue.params(), avValobj( loggedUserInfo ) );
    return retValue;
  }

  @Override
  public void doClose() {
    backendSingleton.detachFrontend( frontend() );
    sessionManager.closeLocalSession( sessionID() );
  }

}
