package org.toxsoft.uskat.s5.utils;

import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.gw.skid.SkidListKeeper;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.core.tslib.utils.valobj.TsValobjUtils;
import org.toxsoft.uskat.core.connection.ESkConnState;
import org.toxsoft.uskat.s5.client.remote.connection.S5ClusterTopology;
import org.toxsoft.uskat.s5.common.S5Host;
import org.toxsoft.uskat.s5.common.S5HostList;
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
    TsValobjUtils.registerKeeperIfNone( S5Host.KEEPER_ID, S5Host.KEEPER );
    TsValobjUtils.registerKeeperIfNone( S5HostList.KEEPER_ID, S5HostList.KEEPER );

    TsValobjUtils.registerKeeperIfNone( Skid.KEEPER_ID, Skid.KEEPER );
    TsValobjUtils.registerKeeperIfNone( SkidListKeeper.KEEPER_ID, SkidListKeeper.KEEPER );
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
    TsValobjUtils.registerKeeperIfNone( SkidMap.KEEPER_ID, SkidMap.KEEPER );
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
