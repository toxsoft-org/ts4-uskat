package org.toxsoft.uskat.s5.client.remote.connection.pas;

import static org.toxsoft.core.pas.tj.impl.TjUtils.*;
import static org.toxsoft.uskat.s5.client.remote.connection.pas.S5CallbackHardConstants.*;

import org.toxsoft.core.pas.common.IPasTxChannel;
import org.toxsoft.core.pas.common.PasChannel;
import org.toxsoft.core.pas.json.IJSONNotification;
import org.toxsoft.core.pas.json.IJSONNotificationHandler;
import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.keeper.std.StringListKeeper;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringLinkedBundleList;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkFrontendRear;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddon;

/**
 * Обратный вызов сервера: передача списка описаний аддонов {@link IS5BackendAddon} бекенда поддерживаемых сервером
 *
 * @author mvk
 */
public abstract class S5CallbackOnGetBackendAddonInfos
    implements IJSONNotificationHandler<S5CallbackChannel> {

  /**
   * Вызов метода: {@link ISkFrontendRear#onBackendMessage(GtMessage)}
   */
  public static final String ON_GET_BACKEND_ADDON_INFOS_METHOD = FRONTENDS_METHOD_PREFIX + "onGetBackendAddonInfos"; //$NON-NLS-1$

  /**
   * Список идентификаторов аддонов {@link IS5BackendAddon} бекеда поддерживаемых сервером
   */
  private static final String ADDON_IDS = "addonIds"; //$NON-NLS-1$

  /**
   * Список полных имен java-классов реализующих аддоны {@link IS5BackendAddon} бекеда поддерживаемых сервером
   */
  private static final String ADDON_CLASS_NAMES = "addonClassNames"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Отправляет сообщение по каналу {@link PasChannel}
   *
   * @param aChannel {@link IPasTxChannel} канал передачи
   * @param aBackendAddons {@link IStridablesList}&lt;{@link IS5BackendAddon}&gt; список расширений бекенда
   *          поддерживаемых сервером
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void send( IPasTxChannel aChannel, IStridablesList<IS5BackendAddon> aBackendAddons ) {
    TsNullArgumentRtException.checkNulls( aChannel, aBackendAddons );
    IStringList addonIds = aBackendAddons.ids();
    IStringListEdit addonClassNames = new StringLinkedBundleList();
    for( IS5BackendAddon addon : aBackendAddons ) {
      addonClassNames.add( addon.getClass().getName() );
    }
    IStringMapEdit<ITjValue> notifyParams = new StringMap<>();
    notifyParams.put( ADDON_IDS, createString( StringListKeeper.KEEPER.ent2str( addonIds ) ) );
    notifyParams.put( ADDON_CLASS_NAMES, createString( StringListKeeper.KEEPER.ent2str( addonClassNames ) ) );
    // Передача по каналу
    aChannel.sendNotification( ON_GET_BACKEND_ADDON_INFOS_METHOD, notifyParams );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IJSONNotificationHandler
  //
  @Override
  public final void notify( S5CallbackChannel aChannel, IJSONNotification aNotification ) {
    TsNullArgumentRtException.checkNull( aNotification );
    if( !aNotification.method().equals( ON_GET_BACKEND_ADDON_INFOS_METHOD ) ) {
      // Уведомление игнорировано
      return;
    }
    String addonIdsStr = aNotification.params().getByKey( ADDON_IDS ).asString();
    String addonClassNamesStr = aNotification.params().getByKey( ADDON_CLASS_NAMES ).asString();
    IStringList addonIds = StringListKeeper.KEEPER.str2ent( addonIdsStr );
    IStringList addonClassNames = StringListKeeper.KEEPER.str2ent( addonClassNamesStr );
    IStringMapEdit<String> addonInfos = new StringMap<>();
    for( int index = 0, n = addonIds.size(); index < n; index++ ) {
      addonInfos.put( addonIds.get( index ), addonClassNames.get( index ) );
    }
    // Передача сообщения наследнику
    doWhenGetBackendAddonIds( addonInfos );
  }

  // ------------------------------------------------------------------------------------
  // Методы для реализации наследниками
  //
  /**
   * Обработка полученного сообщения
   *
   * @param aBackendAddonInfos {@link IStringMap}&lt;String&gt; карта описания расширений.
   *          <p>
   *          Ключ: идентификатор расширения {@link IS5BackendAddon#id()};<br>
   *          Значение: полное имя java-класса реализующий расширение {@link IS5BackendAddon}.<br>
   */
  protected abstract void doWhenGetBackendAddonIds( IStringMap<String> aBackendAddonInfos );
}
