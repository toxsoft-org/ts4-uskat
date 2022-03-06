package org.toxsoft.uskat.s5.server.sessions.pas;

import static org.toxsoft.core.pas.tj.impl.TjUtils.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackHardConstants.*;

import org.toxsoft.core.pas.common.IPasTxChannel;
import org.toxsoft.core.pas.json.IJSONNotification;
import org.toxsoft.core.pas.json.IJSONNotificationHandler;
import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.IS5BackendLocal;

import ru.uskat.core.api.users.ISkSession;

/**
 * Вызов клиента: инициализация сессии
 *
 * @author mvk
 */
public final class S5SessionCallbackInit
    implements IJSONNotificationHandler<S5SessionCallbackChannel> {

  /**
   * Вызов метода: {@link IS5BackendLocal#verify()}
   */
  public static final String SESSION_INIT_METHOD = SESSIONS_METHOD_PREFIX + "init"; //$NON-NLS-1$

  /**
   * Идентификатор сессии идентификатор сессии {@link ISkSession}
   */
  private static final String SESSION_ID = "sessionID"; //$NON-NLS-1$

  /**
   * Конструктор
   */
  S5SessionCallbackInit() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Передача по каналу вызова {@link IS5BackendLocal#verify()}
   *
   * @param aChannel {@link IPasTxChannel} канал передачи
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void send( IPasTxChannel aChannel, Skid aSessionID ) {
    TsNullArgumentRtException.checkNulls( aChannel, aSessionID );
    // Формирование параметров
    IStringMapEdit<ITjValue> notifyParams = new StringMap<>();
    notifyParams.put( SESSION_ID, createString( Skid.KEEPER.ent2str( aSessionID ) ) );
    aChannel.sendNotification( SESSION_INIT_METHOD, notifyParams );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IJSONNotificationHandler
  //
  @Override
  public void notify( S5SessionCallbackChannel aChannel, IJSONNotification aNotification ) {
    TsNullArgumentRtException.checkNull( aNotification );
    if( !aNotification.method().equals( SESSION_INIT_METHOD ) ) {
      // Уведомление игнорировано
      return;
    }
    Skid sessionID = Skid.KEEPER.str2ent( aNotification.params().getByKey( SESSION_ID ).asString() );
    aChannel.setSessionID( sessionID );
  }
}
