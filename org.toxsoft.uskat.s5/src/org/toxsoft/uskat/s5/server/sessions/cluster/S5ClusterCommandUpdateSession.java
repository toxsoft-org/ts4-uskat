package org.toxsoft.uskat.s5.server.sessions.cluster;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.core.pas.tj.impl.TjUtils.*;
import static org.toxsoft.uskat.s5.server.cluster.IS5ClusterHardConstants.*;
import static org.toxsoft.uskat.s5.server.sessions.cluster.IS5Resources.*;

import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.pas.tj.impl.TjUtils;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.classes.*;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommand;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommandHandler;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionManager;
import org.toxsoft.uskat.s5.server.sessions.S5SessionData;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionMessenger;

/**
 * Обработчик команды кластера: всем узлам обновить состояние сессии
 * {@link IS5SessionManager#writeSessionData(S5SessionData)}
 *
 * @author mvk
 */
public final class S5ClusterCommandUpdateSession
    implements IS5ClusterCommandHandler {

  /**
   * Вызов метода: {@link IS5SessionManager#tryCreateMessenger(S5SessionData)}
   */
  public static final String UDATE_SESSION_METHOD = CLUSTER_METHOD_PREFIX + "updateSession"; //$NON-NLS-1$

  /**
   * {@link Skid} идентификатор сессии {@link ISkSession} для которой создается писатель обратного вызова
   */
  private static final String SESSION_ID = "sessionID"; //$NON-NLS-1$

  /**
   * Сессия
   */
  private final IS5SessionManager sessionManager;

  /**
   * Журнал работы
   */
  private final ILogger logger = getLogger( getClass() );

  /**
   * Конструктор
   *
   * @param aSessionManager {@link IS5SessionManager} менеджер сессий
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5ClusterCommandUpdateSession( IS5SessionManager aSessionManager ) {
    sessionManager = TsNullArgumentRtException.checkNull( aSessionManager );
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Создание команды
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @return {@link IS5ClusterCommand} созданная команда
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IS5ClusterCommand updateSessionCommand( Skid aSessionID ) {
    TsNullArgumentRtException.checkNulls( aSessionID );
    return new IS5ClusterCommand() {

      @Override
      public String method() {
        return UDATE_SESSION_METHOD;
      }

      @Override
      public IStringMap<ITjValue> params() {
        IStringMapEdit<ITjValue> params = new StringMap<>();
        params.put( SESSION_ID, createString( Skid.KEEPER.ent2str( aSessionID ) ) );
        return params;
      }
    };
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ClusterCommandHandler
  //
  @Override
  public ITjValue handleClusterCommand( IS5ClusterCommand aCommand ) {
    TsNullArgumentRtException.checkNull( aCommand );
    if( !aCommand.method().equals( UDATE_SESSION_METHOD ) ) {
      return TjUtils.NULL;
    }
    Skid sessionID = Skid.KEEPER.str2ent( aCommand.params().getByKey( SESSION_ID ).asString() );
    S5SessionData sessionData = sessionManager.findSessionData( sessionID );
    if( sessionData == null ) {
      // Сессия не найдена в кэше открытых сессий
      logger.error( "handleClusterCommand(...): " + MSG_ERR_SESSION_NOT_FOUND, sessionID ); //$NON-NLS-1$
      return TjUtils.TRUE;
    }
    // Приемопередатчик сообщений
    S5SessionMessenger messenger = sessionManager.findMessenger( sessionID );
    if( messenger == null ) {
      logger.warning( "handleClusterCommand(...): %s. messenger = null", UDATE_SESSION_METHOD ); //$NON-NLS-1$
      return TjUtils.TRUE;
    }
    messenger.updateSessionData( sessionData );
    logger.info( "handleClusterCommand(...): %s. update session for messenger", UDATE_SESSION_METHOD ); //$NON-NLS-1$
    return TjUtils.TRUE;
  }
}
