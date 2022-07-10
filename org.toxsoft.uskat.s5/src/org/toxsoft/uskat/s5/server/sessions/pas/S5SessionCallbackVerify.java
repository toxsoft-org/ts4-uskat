package org.toxsoft.uskat.s5.server.sessions.pas;

import static org.toxsoft.uskat.s5.server.sessions.pas.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackHardConstants.*;

import javax.ejb.NoSuchEJBException;

import org.toxsoft.core.pas.common.IPasTxChannel;
import org.toxsoft.core.pas.json.IJSONNotification;
import org.toxsoft.core.pas.json.IJSONNotificationHandler;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.server.backend.IS5BackendSessionControl;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionManager;
import org.toxsoft.uskat.s5.server.sessions.S5SessionData;

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
  public static final String SESSION_VERIFY_METHOD = SESSIONS_METHOD_PREFIX + "verify"; //$NON-NLS-1$

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
