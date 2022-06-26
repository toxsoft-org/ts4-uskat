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
import org.toxsoft.uskat.s5.common.sessions.ISkSession;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommand;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommandHandler;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionManager;
import org.toxsoft.uskat.s5.server.sessions.S5RemoteSession;

/**
 * Обработчик команды кластера: всем узлам создать писателей обратных вызовов
 *
 * @author mvk
 */
public final class S5ClusterCommandCreateCallback
    implements IS5ClusterCommandHandler {

  /**
   * Вызов метода: {@link IS5SessionManager#tryCreateCallbackWriter(S5RemoteSession)}
   */
  public static final String CREATE_CALLBACK_METHOD = CLUSTER_METHOD_PREFIX + "createCallback"; //$NON-NLS-1$

  /**
   * {@link Skid} идентификатор сессии {@link ISkSession} для которой создается писатель обратного вызова
   */
  private static final String SESSION_ID = "sessionID"; //$NON-NLS-1$

  /**
   * Максимальное время (мсек) ожидания появления сессии в кэше открытых сессий
   */
  private static final long SESSION_CACHE_WAIT_TIMEOUT = 10000;

  /**
   * Интервал проверки (мсек) сессии в кэше открытых сессий
   */
  private static final long SESSION_CACHE_TEST_INTERVAL = 10;

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
  public S5ClusterCommandCreateCallback( IS5SessionManager aSessionManager ) {
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
  public static IS5ClusterCommand createCallbackCommand( Skid aSessionID ) {
    TsNullArgumentRtException.checkNulls( aSessionID );
    return new IS5ClusterCommand() {

      @Override
      public String method() {
        return CREATE_CALLBACK_METHOD;
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
    if( !aCommand.method().equals( CREATE_CALLBACK_METHOD ) ) {
      return TjUtils.NULL;
    }
    Skid sessionID = Skid.KEEPER.str2ent( aCommand.params().getByKey( SESSION_ID ).asString() );
    long startTime = System.currentTimeMillis();
    S5RemoteSession session = null;
    while( System.currentTimeMillis() - startTime < SESSION_CACHE_WAIT_TIMEOUT ) {
      try {
        session = sessionManager.findSession( sessionID );
        if( session != null ) {
          break;
        }
        Thread.sleep( SESSION_CACHE_TEST_INTERVAL );
      }
      catch( InterruptedException e ) {
        logger.error( e );
      }
    }
    if( session == null ) {
      // Сессия не найдена в кэше открытых сессий
      logger.error( MSG_ERR_SESSION_NOT_FOUND, sessionID );
      return TjUtils.TRUE;
    }
    // Ожидание сессии в кэше открытых сессий
    long waitTime = System.currentTimeMillis() - startTime;
    Long wt = Long.valueOf( waitTime );
    if( waitTime < 100 ) {
      logger.debug( MSG_WAIT_OPEN_SESSION_CACHE, wt, sessionID );
    }
    if( 100 <= waitTime && waitTime < 1000 ) {
      logger.info( MSG_WAIT_OPEN_SESSION_CACHE, wt, sessionID );
    }
    if( 1000 <= waitTime && waitTime < 500 ) {
      logger.warning( MSG_WAIT_OPEN_SESSION_CACHE, wt, sessionID );
    }
    if( 500 <= waitTime ) {
      logger.error( MSG_WAIT_OPEN_SESSION_CACHE, wt, sessionID );
    }
    sessionManager.tryCreateCallbackWriter( session );
    return TjUtils.TRUE;
  }
}
