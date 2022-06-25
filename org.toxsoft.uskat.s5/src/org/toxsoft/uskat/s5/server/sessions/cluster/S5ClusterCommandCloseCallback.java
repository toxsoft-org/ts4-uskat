package org.toxsoft.uskat.s5.server.sessions.cluster;

import static org.toxsoft.core.pas.tj.impl.TjUtils.*;
import static org.toxsoft.uskat.s5.server.cluster.IS5ClusterHardConstants.*;

import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.pas.tj.impl.TjUtils;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.s5.common.sessions.ISkSession;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommand;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommandHandler;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionManager;

/**
 * Обработчик команды кластера: всем узлам завершить работу писателей обратных вызовов сессии
 *
 * @author mvk
 */
public final class S5ClusterCommandCloseCallback
    implements IS5ClusterCommandHandler {

  /**
   * Вызов метода: {@link IS5SessionManager#closeCallbackWriter(Skid)}
   */
  public static final String CLOSE_CALLBACK_METHOD = CLUSTER_METHOD_PREFIX + "closeCallbackWriter"; //$NON-NLS-1$

  /**
   * {@link Skid} идентификатор сессии {@link ISkSession} у которой завершается писатель обратного вызова
   */
  private static final String SESSION_ID = "sessionID"; //$NON-NLS-1$

  /**
   * Сессия
   */
  private final IS5SessionManager sessionManager;

  /**
   * Журнал работы
   */
  // private final ILogger logger = l4jLogger( getClass() );

  /**
   * Конструктор
   *
   * @param aSessionManager {@link IS5SessionManager} менеджер сессий
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5ClusterCommandCloseCallback( IS5SessionManager aSessionManager ) {
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
  public static IS5ClusterCommand closeCallbackCommand( Skid aSessionID ) {
    TsNullArgumentRtException.checkNulls( aSessionID );
    return new IS5ClusterCommand() {

      @Override
      public String method() {
        return CLOSE_CALLBACK_METHOD;
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
    if( !aCommand.method().equals( CLOSE_CALLBACK_METHOD ) ) {
      return TjUtils.NULL;
    }
    Skid sessionID = Skid.KEEPER.str2ent( aCommand.params().getByKey( SESSION_ID ).asString() );
    sessionManager.closeCallbackWriter( sessionID );
    return TjUtils.TRUE;
  }
}
