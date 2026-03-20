package org.toxsoft.uskat.s5.server.sessions.pas;

import static org.toxsoft.uskat.s5.server.sessions.pas.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackHardConstants.*;

import org.toxsoft.core.pas.common.*;
import org.toxsoft.core.pas.json.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.backend.*;
import org.toxsoft.uskat.s5.server.sessions.*;

import jakarta.ejb.*;

/**
 * Вызов клиента: проверка состояния сессии
 *
 * @author mvk
 */
public final class S5SessionCallbackVerify
    implements IJSONNotificationHandler<S5SessionCallbackChannel> {

  /**
   * Вызов метода: {@link IS5BackendSessionControl#verify()}
   */
  public static final String SESSION_VERIFY_METHOD = FRONTEND_METHOD_PREFIX + "verify"; //$NON-NLS-1$

  /**
   * Менеджер сессий
   */
  private final IS5SessionManager sessionManager;

  /**
   * Конструктор
   *
   * @param aSessionManager {@link IS5SessionManager} менеджер сессий
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  S5SessionCallbackVerify( IS5SessionManager aSessionManager ) {
    TsNullArgumentRtException.checkNull( aSessionManager );
    sessionManager = aSessionManager;
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Передача по каналу вызова {@link IS5BackendSessionControl#verify()}
   *
   * @param aChannel {@link IPasTxChannel} канал передачи
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void send( IPasTxChannel aChannel ) {
    TsNullArgumentRtException.checkNulls( aChannel );
    aChannel.sendNotification( SESSION_VERIFY_METHOD, IStringMap.EMPTY );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IJSONNotificationHandler
  //
  @Override
  public void notify( S5SessionCallbackChannel aChannel, IJSONNotification aNotification ) {
    TsNullArgumentRtException.checkNull( aNotification );
    if( !aNotification.method().equals( SESSION_VERIFY_METHOD ) ) {
      // Уведомление игнорировано
      return;
    }
    Skid sessionID = aChannel.getSessionID();
    if( sessionID == Skid.NONE ) {
      return;
    }
    S5SessionData session = sessionManager.findSessionData( sessionID );
    if( session == null ) {
      // Сессия канала не найдена. Завершение работы канала
      aChannel.close();
      aChannel.logger().error( ERR_CHANNEL_SESSION_NOT_FOUND, sessionID, aChannel );
      return;
    }
    try {
      session.backend().verify();
    }
    catch( @SuppressWarnings( "unused" ) NoSuchEJBException e ) {
      // Сессия стала невалидной, но осталась в cache и существует ее callback
      aChannel.logger().error( ERR_CHANNEL_SESSION_NOT_VALID, sessionID, aChannel );
      sessionManager.closeRemoteSession( sessionID );
    }
  }
}
