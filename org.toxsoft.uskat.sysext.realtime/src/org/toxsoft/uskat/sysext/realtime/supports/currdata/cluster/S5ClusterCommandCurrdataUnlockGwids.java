package org.toxsoft.uskat.sysext.realtime.supports.currdata.cluster;

import static org.toxsoft.core.log4j.LoggerWrapper.*;
import static org.toxsoft.uskat.s5.server.cluster.IS5ClusterHardConstants.*;

import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.pas.tj.impl.TjUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.logs.ILogger;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommand;
import org.toxsoft.uskat.s5.server.cluster.IS5ClusterCommandHandler;
import org.toxsoft.uskat.sysext.realtime.supports.currdata.IS5BackendCurrDataSingleton;

/**
 * Обработчик команды кластера: всем узлам разблокировать доступ к указанным текущим данным данным
 *
 * @author mvk
 */
public final class S5ClusterCommandCurrdataUnlockGwids
    implements IS5ClusterCommandHandler {

  /**
   * Идентификатор команды вызова метода: {@link IS5BackendCurrDataSingleton#remoteUnlockGwids(IList)}
   */
  public static final String REMOTE_UNLOCKS_GWIDS_METHOD = CLUSTER_METHOD_PREFIX + "currdata.remoteUnlockGwids"; //$NON-NLS-1$

  /**
   * Параметр команды: список идентификаторов данных
   * <p>
   * Тип: {@link IList}&lt;{@link Gwid}&gt;
   */
  private static final String GWIDS = "gwids"; //$NON-NLS-1$

  /**
   * Бекенд обработки текущих данных
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
   * @param aCurrdataBackend {@link IS5BackendCurrDataSingleton} Бекенд обработки текущих данных
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5ClusterCommandCurrdataUnlockGwids( IS5BackendCurrDataSingleton aCurrdataBackend ) {
    currdataBackend = TsNullArgumentRtException.checkNull( aCurrdataBackend );
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Создание команды
   *
   * @param aGwids {@link IGwidList} список идентификаторов данных
   * @return {@link IS5ClusterCommand} созданная команда
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static IS5ClusterCommand createCommand( IGwidList aGwids ) {
    TsNullArgumentRtException.checkNull( aGwids );
    return new IS5ClusterCommand() {

      @Override
      public String method() {
        return REMOTE_UNLOCKS_GWIDS_METHOD;
      }

      @Override
      public IStringMap<ITjValue> params() {
        IStringMapEdit<ITjValue> params = new StringMap<>();
        params.put( GWIDS, TjUtils.createString( GwidListKeeper.KEEPER.ent2str( aGwids ) ) );
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
    if( !aCommand.method().equals( REMOTE_UNLOCKS_GWIDS_METHOD ) ) {
      return TjUtils.NULL;
    }
    IGwidList gwids = GwidListKeeper.KEEPER.str2ent( aCommand.params().getByKey( GWIDS ).asString() );
    boolean result = currdataBackend.remoteUnlockGwids( gwids );
    return (result ? TjUtils.TRUE : TjUtils.FALSE);
  }
}
