package org.toxsoft.uskat.s5.server.sessions.pas;

import static org.toxsoft.core.pas.tj.impl.TjUtils.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.IS5Resources.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackHardConstants.*;

import org.toxsoft.core.pas.common.*;
import org.toxsoft.core.pas.json.*;
import org.toxsoft.core.pas.tj.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.client.remote.connection.*;
import org.toxsoft.uskat.s5.server.backend.*;
import org.toxsoft.uskat.s5.server.sessions.*;

import jakarta.ejb.*;

/**
 * Вызов клиента: обновление топологии кластеров доступных клиенту
 *
 * @author mvk
 */
public final class S5SessionCallbackClusterTopology
    implements IJSONNotificationHandler<S5SessionCallbackChannel> {

  /**
   * Вызов метода: {@link IS5BackendSessionControl#setClusterTopology(S5ClusterTopology)}
   */
  public static final String SESSION_TOPOLOGY_METHOD = FRONTEND_METHOD_PREFIX + "setClusterTopology"; //$NON-NLS-1$

  /**
   * Идентификатор сообщения
   */
  private static final String TOPOLOGY = "topology"; //$NON-NLS-1$

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
  S5SessionCallbackClusterTopology( IS5SessionManager aSessionManager ) {
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
   * @param aTopology {@link S5ClusterTopology} топология кластеров доступных клиенту
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void send( IPasTxChannel aChannel, S5ClusterTopology aTopology ) {
    TsNullArgumentRtException.checkNulls( aChannel, aTopology );
    IStringMapEdit<ITjValue> notifyParams = new StringMap<>();
    notifyParams.put( TOPOLOGY, createString( S5ClusterTopology.KEEPER.ent2str( aTopology ) ) );
    // Передача по каналу
    aChannel.sendNotification( SESSION_TOPOLOGY_METHOD, notifyParams );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IJSONNotificationHandler
  //
  @Override
  public void notify( S5SessionCallbackChannel aChannel, IJSONNotification aNotification ) {
    TsNullArgumentRtException.checkNull( aNotification );
    if( !aNotification.method().equals( SESSION_TOPOLOGY_METHOD ) ) {
      // Уведомление игнорировано
      return;
    }
    Skid sessionID = aChannel.getSessionID();
    if( sessionID != null ) {
      S5SessionData session = sessionManager.findSessionData( sessionID );
      if( session == null ) {
        // Сессия канала не найдена. Завершение работы канала
        aChannel.close();
        aChannel.logger().error( ERR_CHANNEL_SESSION_NOT_FOUND, sessionID, aChannel );
        return;
      }
      try {
        ITjValue value = aNotification.params().getByKey( TOPOLOGY );
        S5ClusterTopology topology = S5ClusterTopology.KEEPER.str2ent( value.asString() );
        session.backend().setClusterTopology( topology );
      }
      catch( @SuppressWarnings( "unused" ) NoSuchEJBException e ) {
        // Сессия стала невалидной, но осталась в cache и существует ее callback
        aChannel.logger().error( ERR_CHANNEL_SESSION_NOT_VALID, sessionID, aChannel );
        sessionManager.closeRemoteSession( sessionID );
      }
    }
  }
}
