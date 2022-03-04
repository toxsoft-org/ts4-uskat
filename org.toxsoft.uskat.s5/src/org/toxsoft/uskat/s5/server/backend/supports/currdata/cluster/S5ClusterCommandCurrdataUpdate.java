package org.toxsoft.uskat.s5.server.backend.supports.currdata.cluster;

import static org.toxsoft.core.log4j.Logger.*;
import static org.toxsoft.uskat.s5.server.cluster.IS5ClusterHardConstants.*;

import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.pas.tj.impl.TjUtils;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.backend.supports.currdata.IS5BackendCurrDataSingleton;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommand;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommandHandler;
import org.toxsoft.uskat.s5.server.sessions.IS5SessionManager;

/**
 * Обработчик команды кластера: всем узлам обновить состояние корневого набора
 *
 * @author mvk
 */
public final class S5ClusterCommandCurrdataUpdate
    implements IS5ClusterCommandHandler {

  /**
   * Вызов метода: {@link IS5BackendCurrDataSingleton#updateDataset()}
   */
  public static final String UPDATE_CURRDATA_METHOD = CLUSTER_METHOD_PREFIX + "updateCurrdata"; //$NON-NLS-1$

  /**
   * backend текущих данных
   */
  private final IS5BackendCurrDataSingleton currdataBackend;

  /**
   * Журнал работы
   */
  @SuppressWarnings( "unused" )
  private final ILogger logger = getLogger( getClass() );

  /**
   * Конструктор
   *
   * @param aCurrdataBackend {@link IS5SessionManager} менеджер сессий
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5ClusterCommandCurrdataUpdate( IS5BackendCurrDataSingleton aCurrdataBackend ) {
    currdataBackend = TsNullArgumentRtException.checkNull( aCurrdataBackend );
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Создание команды
   *
   * @return {@link IS5ClusterCommand} созданная команда
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IS5ClusterCommand createUpdateCurrdataCommand() {
    return new IS5ClusterCommand() {

      @Override
      public String method() {
        return UPDATE_CURRDATA_METHOD;
      }

      @Override
      public IStringMap<ITjValue> params() {
        return IStringMap.EMPTY;
      }
    };
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5ClusterCommandHandler
  //
  @Override
  public ITjValue handleClusterCommand( IS5ClusterCommand aCommand ) {
    TsNullArgumentRtException.checkNull( aCommand );
    if( !aCommand.method().equals( UPDATE_CURRDATA_METHOD ) ) {
      return TjUtils.NULL;
    }
    // TODO: 2020-07-30
    // currdataBackend.updateDataset();
    return TjUtils.TRUE;
  }
}
