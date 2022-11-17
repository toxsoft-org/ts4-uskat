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
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonCreator;

/**
 * Обратный вызов сервера: передача карты имен классов построителей {@link IS5BackendAddonCreator} расширений
 * {@link IS5BackendAddon} бекенда поддерживаемых сервером.
 *
 * @author mvk
 */
public abstract class S5CallbackOnGetBaCreatorClasses
    implements IJSONNotificationHandler<S5CallbackChannel> {

  /**
   * Вызов метода: {@link ISkFrontendRear#onBackendMessage(GtMessage)}
   */
  public static final String ON_GET_BA_CREATOR_CLASSES_METHOD = BACKEND_METHOD_PREFIX + "onGetBaCreatorClasses"; //$NON-NLS-1$

  /**
   * Список идентификаторов аддонов {@link IS5BackendAddon} бекеда поддерживаемых сервером
   */
  private static final String ADDON_IDS = "addonIds"; //$NON-NLS-1$

  /**
   * Список полных имен java-классов реализующих построители {@link IS5BackendAddonCreator} расширений
   * {@link IS5BackendAddon} бекенда поддерживаемых сервером.
   */
  private static final String BA_CREATOR_CLASSES = "baCreatorClasses"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Отправляет сообщение по каналу {@link PasChannel}
   *
   * @param aChannel {@link IPasTxChannel} канал передачи
   * @param aBaCreators {@link IStridablesList}&lt;{@link IS5BackendAddonCreator}&gt; список построителей
   *          расширений бекенда поддерживаемых сервером
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void send( IPasTxChannel aChannel, IStridablesList<IS5BackendAddonCreator> aBaCreators ) {
    TsNullArgumentRtException.checkNulls( aChannel, aBaCreators );
    IStringList baCreatorIds = aBaCreators.ids();
    IStringListEdit baCreatorClassNames = new StringLinkedBundleList();
    for( IS5BackendAddonCreator baCreator : aBaCreators ) {
      baCreatorClassNames.add( baCreator.getClass().getName() );
    }
    IStringMapEdit<ITjValue> notifyParams = new StringMap<>();
    notifyParams.put( ADDON_IDS, createString( StringListKeeper.KEEPER.ent2str( baCreatorIds ) ) );
    notifyParams.put( BA_CREATOR_CLASSES, createString( StringListKeeper.KEEPER.ent2str( baCreatorClassNames ) ) );
    // Передача по каналу
    aChannel.sendNotification( ON_GET_BA_CREATOR_CLASSES_METHOD, notifyParams );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IJSONNotificationHandler
  //
  @Override
  public final void notify( S5CallbackChannel aChannel, IJSONNotification aNotification ) {
    TsNullArgumentRtException.checkNull( aNotification );
    if( !aNotification.method().equals( ON_GET_BA_CREATOR_CLASSES_METHOD ) ) {
      // Уведомление игнорировано
      return;
    }
    String addonIdsStr = aNotification.params().getByKey( ADDON_IDS ).asString();
    String baCreatorClassesStr = aNotification.params().getByKey( BA_CREATOR_CLASSES ).asString();
    IStringList addonIds = StringListKeeper.KEEPER.str2ent( addonIdsStr );
    IStringList baCreatorClassNames = StringListKeeper.KEEPER.str2ent( baCreatorClassesStr );
    IStringMapEdit<String> baCreatorClasses = new StringMap<>();
    for( int index = 0, n = addonIds.size(); index < n; index++ ) {
      baCreatorClasses.put( addonIds.get( index ), baCreatorClassNames.get( index ) );
    }
    // Передача сообщения наследнику
    doWhenGetBaCreatorClasses( baCreatorClasses );
  }

  // ------------------------------------------------------------------------------------
  // Методы для реализации наследниками
  //
  /**
   * Обработка полученного сообщения
   *
   * @param aBaCreatorClasses {@link IStringMap}&lt;String&gt; карта классов.
   *          <p>
   *          Ключ: идентификатор расширения {@link IS5BackendAddon#id()};<br>
   *          Значение: полное имя java-класса реализующий расширение построитель расширения
   *          {@link IS5BackendAddonCreator}.
   */
  protected abstract void doWhenGetBaCreatorClasses( IStringMap<String> aBaCreatorClasses );
}
