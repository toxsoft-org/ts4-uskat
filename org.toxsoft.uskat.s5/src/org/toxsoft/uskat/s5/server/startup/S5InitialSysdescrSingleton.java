package org.toxsoft.uskat.s5.server.startup;

import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.startup.IS5Resources.*;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.classes.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.s5.client.local.*;
import org.toxsoft.uskat.s5.server.backend.supports.core.*;
import org.toxsoft.uskat.s5.server.singletons.*;

/**
 * Реализация синглтона {@link IS5InitialSysdescrSingleton}.
 * <p>
 * TODO: Сделать оценку состояния БД и, при необходимости, загрузку через {@link ISkConnection} с бекендом чтения файла
 *
 * @author mvk
 */
public abstract class S5InitialSysdescrSingleton
    extends S5SingletonBase
    implements IS5InitialSysdescrSingleton {

  private static final long serialVersionUID = 157157L;

  /**
   * Имя синглетона в контейнере сервера для организации зависимостей (@DependsOn)
   */
  public static final String PROJECT_INITIAL_SYSDESCR_ID = "ProjectInitialSysdescrSingleton"; //$NON-NLS-1$

  /**
   * Ядро сервера
   */
  @EJB
  private IS5BackendCoreSingleton backendCore;

  /**
   * Поставщик локальных соединений с сервером
   */
  @EJB
  private IS5LocalConnectionSingleton localConnectionSingleton;

  /**
   * Описание системы
   */
  private ISkSysdescr sysdescr;

  /**
   * Служба управления объектами
   */
  private ISkObjectService objectService;

  /**
   * Служба управления связями
   */
  private ISkLinkService linkService;

  /**
   * Служба управления пользователями
   */
  private ISkUserService userService;

  /**
   * Конструктор.
   */
  protected S5InitialSysdescrSingleton() {
    super( PROJECT_INITIAL_SYSDESCR_ID, STR_D_PROJECT_INITIAL_SYSDESCR );
  }

  // ------------------------------------------------------------------------------------
  // Переопределение S5SingletonBase
  //
  @Override
  protected void doInit() {
    // Подключение к серверу для проверки/создания системного описания
    ISkConnection connection = localConnectionSingleton.open( id() );
    ISkCoreApi coreApi = connection.coreApi();
    sysdescr = coreApi.sysdescr();
    objectService = coreApi.objService();
    linkService = coreApi.linkService();
    userService = coreApi.userService();
    // Проверка (и если необходимо создание) системного описания
    checkSysdescr( connection );
    // Завершение соединения. представляет pure (без расширения skf-функциональностью) и не может полноценно
    // использовано в дальнейшем
    connection.close();
  }

  @Override
  protected void doClose() {
    super.doClose();
  }

  // ------------------------------------------------------------------------------------
  // Методы для наследников
  //
  /**
   * Возвращает описание системы
   *
   * @return {@link ISkSysdescr} описание
   */
  protected final ISkSysdescr sysdescr() {
    return sysdescr;
  }

  /**
   * Возвращает служба управления объектами
   *
   * @return {@link ISkObjectService} служба управления объектами
   */
  protected final ISkObjectService objectService() {
    return objectService;
  }

  /**
   * Возвращает служба управления связями.
   *
   * @return {@link ISkLinkService} служба управления связями
   */
  protected final ISkLinkService linkService() {
    return linkService;
  }

  /**
   * Возвращает служба управления пользователями.
   *
   * @return {@link ISkUserService} служба управления пользователями
   */
  protected final ISkUserService userService() {
    return userService;
  }

  /**
   * Создает нового пользователя с указанными параметрами.
   * <p>
   * Атрибуты пользователя могут определены константами:
   * <ul>
   * <li>{@link IAvMetaConstants#DDEF_NAME};</li>
   * <li>{@link IAvMetaConstants#DDEF_DESCRIPTION};</li>
   * <li>{@link ISkUserServiceHardConstants#ATRID_USER_IS_ENABLED};</li>;
   * <li>{@link ISkUserServiceHardConstants#ATRID_USER_IS_HIDDEN}.</li>;
   * <p>
   * Если пользователь уже существует в системе, то ничего не делает.
   *
   * @param aLogin String логин пользователя
   * @param aPassword String пароль пользователя
   * @param aAttrs {@link IOptionSet} атрибуты пользователя
   * @return {@link ISkUser} созданный или найденный пользователь
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected final ISkUser createUser( String aLogin, String aPassword, IOptionSet aAttrs ) {
    TsNullArgumentRtException.checkNulls( aLogin, aPassword, aAttrs );
    Skid id = new Skid( ISkUser.CLASS_ID, aLogin );
    ISkUser user = userService().listUsers().findByKey( aLogin );
    if( user != null ) {
      // Пользователь уже существует
      return user;
    }
    IDtoObject dtoUser = new DtoObject( id, aAttrs, IStringMap.EMPTY );
    return userService().createUser( new DtoFullObject( dtoUser ), aPassword );
  }

  // ------------------------------------------------------------------------------------
  // Методы для переопределения наследниками
  //
  /**
   * Проверка и если необходимо создание системного описания сервера (классы и объекты)
   */
  protected void doCreateSysdescr() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
  /**
   * Проверка и если необходимо создание системного описания сервера
   */
  private void checkSysdescr( ISkConnection aConnection ) {
    // Проверка и если необходимо создание классов s5-бекенда
    S5ClassUtils.updateSkClasses( aConnection.coreApi() );
    // Регистрация создателей объектов s5
    S5ClassUtils.registerObjectCreators( aConnection.coreApi() );
    // Информация о бекенде
    ISkBackendInfo info = backendCore.getInfo();
    // Идентификатор сервера
    Skid serverId = OP_SERVER_ID.getValue( info.params() ).asValobj();
    // Идентификатор узла сервера
    Skid nodeId = OP_SERVER_NODE_ID.getValue( info.params() ).asValobj();
    // Проверка существования сервера
    ISkServer server = objectService.find( serverId );
    if( server == null ) {
      // Сервер не найден. Создание сервера
      server = objectService.defineObject( new DtoFullObject( serverId ) );
    }
    // Проверка существования узла сервера
    ISkServerNode node = objectService.find( nodeId );
    if( node == null ) {
      // Узел не найден. Создание узла
      node = objectService.defineObject( new DtoFullObject( nodeId ) );
      linkService.defineLink( nodeId, ISkServerNode.LNKID_SERVER, ISkidList.EMPTY, new SkidList( serverId ) );
    }
    // Создание проектного sysdescr
    doCreateSysdescr();
  }
}
