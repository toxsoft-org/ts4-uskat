package org.toxsoft.uskat.s5.server.startup;

import static org.toxsoft.uskat.concurrent.S5SynchronizedConnection.*;
import static org.toxsoft.uskat.s5.server.IS5ServerHardConstants.*;
import static org.toxsoft.uskat.s5.server.startup.IS5Resources.*;

import javax.ejb.EJB;

import org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetUtils;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.TsIllegalStateRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.classes.IS5ClassNode;
import org.toxsoft.uskat.classes.IS5ClassServer;
import org.toxsoft.uskat.classes.impl.S5ClassUtils;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.api.linkserv.ISkLinkService;
import org.toxsoft.uskat.core.api.objserv.ISkObjectService;
import org.toxsoft.uskat.core.api.sysdescr.ISkSysdescr;
import org.toxsoft.uskat.core.api.users.ISkUser;
import org.toxsoft.uskat.core.api.users.ISkUserService;
import org.toxsoft.uskat.core.backend.api.ISkBackendInfo;
import org.toxsoft.uskat.core.connection.ISkConnection;
import org.toxsoft.uskat.core.impl.dto.DtoFullObject;
import org.toxsoft.uskat.s5.client.local.IS5LocalConnectionSingleton;
import org.toxsoft.uskat.s5.server.backend.IS5BackendCoreSingleton;
import org.toxsoft.uskat.s5.server.singletons.S5SingletonBase;

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
   * Соединение с сервером. null: соединение не открывалось
   */
  private ISkConnection connection;

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
    TsIllegalStateRtException.checkNoNull( connection );
    // Подключение к серверу
    connection = createSynchronizedConnection( localConnectionSingleton.open( id() ) );
    ISkCoreApi coreApi = connection.coreApi();
    sysdescr = coreApi.sysdescr();
    objectService = coreApi.objService();
    linkService = coreApi.linkService();
    userService = coreApi.userService();
    // Проверка (и если необходимо создание) системного описания
    checkSysdescr();
    // Установка соединения для ядра сервера
    backendCore.setConnection( connection );
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
   * Возвращает служба управления связями
   *
   * @return {@link ISkLinkService} служба управления связями
   */
  protected final ISkLinkService linkService() {
    return linkService;
  }

  /**
   * Возвращает служба управления пользователями
   *
   * @return {@link ISkUserService} служба управления пользователями
   */
  protected final ISkUserService userService() {
    return userService;
  }

  /**
   * Создает нового пользователя с указанными параметрами
   * <p>
   * Параметры пользователя могут определены константами:
   * <ul>
   * <li>{@link IAvMetaConstants#DDEF_NAME};</li>
   * <li>{@link IAvMetaConstants#DDEF_DESCRIPTION};</li>
   * <li>{@link ISkUser#ATRID_PASSWORD};</li>;
   * <li>{@link ISkUser#ATRID_IS_ENABLED};</li>;
   * <li>{@link ISkUser#ATRID_IS_HIDDEN}.</li>;
   * <p>
   * Если пользователь уже существует в системе, то ничего не делает
   *
   * @param aLogin String логин пользовател
   * @param aIdsAndValues {@link IOptionSet} список параметров пользователя (имя параметра, значение параметра, ...)
   * @return {@link ISkUser} созданный или найденный пользователь
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected final ISkUser createUser( String aLogin, Object... aIdsAndValues ) {
    return createUser( aLogin, OptionSetUtils.createOpSet( aIdsAndValues ) );
  }

  /**
   * Создает нового пользователя с указанными параметрами
   * <p>
   * Параметры пользователя могут определены константами:
   * <ul>
   * <li>{@link IAvMetaConstants#DDEF_NAME};</li>
   * <li>{@link IAvMetaConstants#DDEF_DESCRIPTION};</li>
   * <li>{@link ISkUser#ATRID_PASSWORD};</li>;
   * <li>{@link ISkUser#ATRID_IS_ENABLED};</li>;
   * <li>{@link ISkUser#ATRID_IS_HIDDEN}.</li>;
   * <p>
   * Если пользователь уже существует в системе, то ничего не делает
   *
   * @param aLogin String логин пользовател
   * @param aParams {@link IOptionSet} параметры пользователя
   * @return {@link ISkUser} созданный или найденный пользователь
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  protected final ISkUser createUser( String aLogin, IOptionSet aParams ) {
    TsNullArgumentRtException.checkNulls( aLogin, aParams );
    Skid id = new Skid( ISkUser.CLASS_ID, aLogin );
    ISkUser user = userService().listUsers().findByKey( aLogin );
    if( user != null ) {
      // Пользователь уже существует
      return user;
    }
    return userService().defineUser( new DtoFullObject( id ) );
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
  private void checkSysdescr() {
    // Проверка и если необходимо создание классов s5-бекенда
    S5ClassUtils.createS5Classes( connection.coreApi() );
    // Регистрация создателей объектов s5
    S5ClassUtils.registerObjectCreators( connection.coreApi() );
    // Информация о бекенде
    ISkBackendInfo info = backendCore.getInfo();
    // Идентификатор сервера
    Skid serverId = OP_BACKEND_SERVER_ID.getValue( info.params() ).asValobj();
    // Идентификатор узла сервера
    Skid nodeId = OP_BACKEND_NODE_ID.getValue( info.params() ).asValobj();
    // Проверка существования сервера
    IS5ClassServer server = objectService.find( serverId );
    if( server == null ) {
      // Сервер не найден. Создание сервера
      server = objectService.defineObject( new DtoFullObject( serverId ) );
    }
    // Проверка существования узла сервера
    IS5ClassNode node = objectService.find( nodeId );
    if( node == null ) {
      // Узел не найден. Создание узла
      node = objectService.defineObject( new DtoFullObject( nodeId ) );
      linkService.defineLink( nodeId, IS5ClassNode.LNKID_SERVER, ISkidList.EMPTY, new SkidList( serverId ) );
    }
    // Создание проектного sysdescr
    doCreateSysdescr();
  }
}
