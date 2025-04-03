package org.toxsoft.uskat.s5.server.sessions.pas;

import static org.toxsoft.core.pas.tj.impl.TjUtils.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackHardConstants.*;

import org.toxsoft.core.pas.common.*;
import org.toxsoft.core.pas.json.*;
import org.toxsoft.core.pas.tj.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.s5.server.backend.*;

/**
 * Вызов клиента: инициализация сессии
 *
 * @author mvk
 */
public final class S5SessionCallbackInit
    implements IJSONNotificationHandler<S5SessionCallbackChannel> {

  /**
   * Вызов метода: {@link IS5BackendSessionControl#verify()}
   */
  public static final String SESSION_INIT_METHOD = FRONTEND_METHOD_PREFIX + "init"; //$NON-NLS-1$

  /**
   * Идентификатор сессии идентификатор сессии {@link ISkSession}
   */
  public static final String SESSION_ID = "sessionID"; //$NON-NLS-1$

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
   * Передача по каналу вызова {@link IS5BackendSessionControl#verify()}
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
