package org.toxsoft.uskat.s5.server.sessions.init;

import static org.toxsoft.uskat.s5.server.sessions.init.IS5Resources.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.client.IS5ConnectionParams;
import org.toxsoft.uskat.s5.client.remote.connection.S5ClusterTopology;
import org.toxsoft.uskat.s5.common.sessions.ISkSession;
import org.toxsoft.uskat.s5.server.frontend.IS5BackendAddonData;

/**
 * Реализация {@link IS5SessionInitData}
 *
 * @author mvk
 */
public final class S5SessionInitData
    implements IS5SessionInitData, Serializable {

  private static final long serialVersionUID = 157157L;

  private final IOptionSetEdit                      clientOptions     = new OptionSet();
  private final S5ClusterTopology                   clusterTopology   = new S5ClusterTopology();
  private final IStringMapEdit<IS5BackendAddonData> backendAddonDatas = new StringMap<>();

  /**
   * Конструктор
   *
   * @param aSessionID {@link Skid} идентификатор сессии {@link ISkSession}
   * @throws TsNullArgumentRtException аргумент = null
   */
  public S5SessionInitData( Skid aSessionID ) {
    TsNullArgumentRtException.checkNull( aSessionID );
    IS5ConnectionParams.OP_SESSION_ID.setValue( clientOptions(), AvUtils.avValobj( aSessionID ) );
  }

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Установить параметры подключения клиента к серверу
   *
   * @param aClientOptions {@link IOptionSet}&gt; параметры подключения.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setClientOptions( IOptionSet aClientOptions ) {
    TsNullArgumentRtException.checkNull( aClientOptions );
    clientOptions.setAll( aClientOptions );
  }

  /**
   * Установить описание топологии кластеров доступных клиенту
   *
   * @param aClusterTopology {@link S5ClusterTopology} описание топологии
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setClusterTopology( S5ClusterTopology aClusterTopology ) {
    TsNullArgumentRtException.checkNull( aClusterTopology );
    clusterTopology.setAll( aClusterTopology );
  }

  /**
   * Устанавливает данные расширения бекенда
   *
   * @param aAddonId String идентификатор (ИД-путь) расширения
   * @param aData {@link IS5BackendAddonData} данные фронтенда расширения бекенд
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException данные должны поддерживать сериализацию
   * @throws TsItemAlreadyExistsRtException данные расширения уже установлены
   */
  public void setBackendAddonData( String aAddonId, IS5BackendAddonData aData ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aData );
    if( !(aData instanceof Serializable) ) {
      // Данные должны поддерживать сериализацию
      throw new TsIllegalArgumentRtException( ERR_MSG_ADDON_DATA_NO_SERIALIZABLE, aAddonId );
    }
    if( backendAddonDatas.hasKey( aAddonId ) ) {
      // Данные расширения уже зарегистрированы
      throw new TsItemAlreadyExistsRtException( ERR_MSG_ADDON_DATA_ALREADY_EXIST, aAddonId );
    }
    backendAddonDatas.put( aAddonId, aData );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SessionInitData
  //
  @Override
  public IOptionSetEdit clientOptions() {
    return clientOptions;
  }

  @Override
  public S5ClusterTopology clusterTopology() {
    return clusterTopology;
  }

  @Override
  public <T extends IS5BackendAddonData> T findBackendAddonData( String aAddonId, Class<T> aAddonDataType ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aAddonDataType );
    try {
      return aAddonDataType.cast( backendAddonDatas.findByKey( aAddonId ) );
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ex.getMessage() );
    }
  }
}
