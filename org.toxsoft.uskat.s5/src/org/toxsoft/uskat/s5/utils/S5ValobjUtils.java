package org.toxsoft.uskat.s5.utils;

import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;
import org.toxsoft.uskat.core.api.cmdserv.DtoCommandStateChangeInfo;
import org.toxsoft.uskat.core.api.hqserv.ESkQueryState;
import org.toxsoft.uskat.core.connection.ESkAuthentificationType;
import org.toxsoft.uskat.core.connection.ESkConnState;
import org.toxsoft.uskat.core.impl.SkCoreUtils;
import org.toxsoft.uskat.core.impl.SkLoggedUserInfo;
import org.toxsoft.uskat.core.impl.dto.DtoCommand;
import org.toxsoft.uskat.s5.client.remote.connection.S5ClusterTopology;
import org.toxsoft.uskat.s5.common.*;
import org.toxsoft.uskat.s5.server.sessions.*;
import org.toxsoft.uskat.s5.server.statistics.*;
import org.toxsoft.uskat.s5.server.transactions.S5TransactionInfo;
import org.toxsoft.uskat.s5.server.transactions.S5TransactionInfos;
import org.toxsoft.uskat.s5.utils.collections.SkidMap;

/**
 * Value objects support in s5.
 *
 * @author mvk
 */
public class S5ValobjUtils {

  /**
   * Регистрация известных хранителей
   */
  public static void registerS5Keepers() {
    SkCoreUtils.initialize();

    TsValobjUtils.registerKeeperIfNone( ESkAuthentificationType.KEEPER_ID, ESkAuthentificationType.KEEPER );
    TsValobjUtils.registerKeeperIfNone( SkLoggedUserInfo.KEEPER_ID, SkLoggedUserInfo.KEEPER );

    TsValobjUtils.registerKeeperIfNone( S5Host.KEEPER_ID, S5Host.KEEPER );
    TsValobjUtils.registerKeeperIfNone( S5HostList.KEEPER_ID, S5HostList.KEEPER );

    TsValobjUtils.registerKeeperIfNone( S5Module.KEEPER_ID, S5Module.KEEPER );
    TsValobjUtils.registerKeeperIfNone( S5ModuleList.KEEPER_ID, S5ModuleList.KEEPER );
    TsValobjUtils.registerKeeperIfNone( S5SessionIdentity.KEEPER_ID, S5SessionIdentity.KEEPER );
    // 2020-03-21 mvk
    TsValobjUtils.registerKeeperIfNone( S5SessionIDKeeper.KEEPER_ID, S5SessionIDKeeper.KEEPER );
    TsValobjUtils.registerKeeperIfNone( S5SessionsInfos.KEEPER_ID, S5SessionsInfos.KEEPER );
    TsValobjUtils.registerKeeperIfNone( S5SessionInfo.KEEPER_ID, S5SessionInfo.KEEPER );
    TsValobjUtils.registerKeeperIfNone( S5Statistic.KEEPER_ID, S5Statistic.KEEPER );
    TsValobjUtils.registerKeeperIfNone( S5StatisticParamInfo.KEEPER_ID, S5StatisticParamInfo.KEEPER );
    TsValobjUtils.registerKeeperIfNone( EStatisticFunc.KEEPER_ID, EStatisticFunc.KEEPER );
    TsValobjUtils.registerKeeperIfNone( EStatisticInterval.KEEPER_ID, EStatisticInterval.KEEPER );
    TsValobjUtils.registerKeeperIfNone( S5StatisticIntervalList.KEEPER_ID, S5StatisticIntervalList.KEEPER );
    TsValobjUtils.registerKeeperIfNone( S5TransactionInfos.KEEPER_ID, S5TransactionInfos.KEEPER );
    TsValobjUtils.registerKeeperIfNone( S5TransactionInfo.KEEPER_ID, S5TransactionInfo.KEEPER );
    TsValobjUtils.registerKeeperIfNone( S5ClusterTopology.KEEPER_ID, S5ClusterTopology.KEEPER );
    TsValobjUtils.registerKeeperIfNone( ESkConnState.KEEPER_ID, ESkConnState.KEEPER );
    TsValobjUtils.registerKeeperIfNone( ESkQueryState.KEEPER_ID, ESkQueryState.KEEPER );
    TsValobjUtils.registerKeeperIfNone( SkidMap.KEEPER_ID, SkidMap.KEEPER );
    TsValobjUtils.registerKeeperIfNone( DtoCommand.KEEPER_ID, DtoCommand.KEEPER );
    TsValobjUtils.registerKeeperIfNone( DtoCommandStateChangeInfo.KEEPER_ID, DtoCommandStateChangeInfo.KEEPER );
  }

  /**
   * Registers the keeper if keeper with such ID is not registered already.
   *
   * @param aKeeperId String - the key, IDPath identifier
   * @param aKeeper {@link IEntityKeeper} - the keeper to be registered
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDPath
   */
  public static void registerKeeperIfNone( String aKeeperId, IEntityKeeper<?> aKeeper ) {
    TsValobjUtils.registerKeeperIfNone( aKeeperId, aKeeper );
  }
}
