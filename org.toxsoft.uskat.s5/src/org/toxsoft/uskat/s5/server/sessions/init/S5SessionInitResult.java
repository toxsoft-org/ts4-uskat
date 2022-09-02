package org.toxsoft.uskat.s5.server.sessions.init;

import static org.toxsoft.uskat.s5.server.sessions.init.IS5Resources.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSessionControl;
import org.toxsoft.uskat.s5.server.frontend.IS5BackendAddonData;

/**
 * Реализация {@link IS5SessionInitResult}
 *
 * @author mvk
 */
public final class S5SessionInitResult
    implements IS5SessionInitResult, Serializable {

  private static final long serialVersionUID = 157157L;

  private final IStringMapEdit<IS5BackendAddonSession> sessions          = new StringMap<>();
  private final IStringMapEdit<IS5BackendAddonData>    backendAddonDatas = new StringMap<>();

  // ------------------------------------------------------------------------------------
  // Открытое API
  //
  /**
   * Обновить результат инициализации сессии
   *
   * @param aSource {@link IS5SessionInitResult} результат инициализации
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setAll( IS5SessionInitResult aSource ) {
    TsNullArgumentRtException.checkNull( aSource );
    sessions.setAll( aSource.baSessions() );
    backendAddonDatas.clear();
    for( String addonId : sessions.keys() ) {
      IS5BackendAddonData addonData = aSource.getBackendAddonData( addonId, IS5BackendAddonData.class );
      if( addonData != null ) {
        backendAddonDatas.put( addonId, addonData );
      }
    }
  }

  /**
   * Устанавливает карту удаленного доступа к расширениям backend
   *
   * @param aAddons {@link IS5BackendAddonSession} карта удаленного доступа к расширениям backend. <br>
   *          Ключ: идентификатор backend {@link IS5BackendAddonSessionControl#id()};<br>
   *          Значение: удаленный доступ к расширенияю backend.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setAddons( IStringMap<IS5BackendAddonSession> aAddons ) {
    TsNullArgumentRtException.checkNull( aAddons );
    sessions.setAll( aAddons );
  }

  /**
   * Устанавливает данные расширения бекенда
   *
   * @param aAddonId String идентификатор (ИД-путь) расширения
   * @param aBackendAddonData {@link IS5BackendAddonData} данные фронтенда расширения бекенд
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException данные должны поддерживать сериализацию
   * @throws TsItemAlreadyExistsRtException данные расширения уже установлены
   */
  public void setBackendAddonData( String aAddonId, IS5BackendAddonData aBackendAddonData ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aBackendAddonData );
    if( !(aBackendAddonData instanceof Serializable) ) {
      // Данные должны поддерживать сериализацию
      throw new TsIllegalArgumentRtException( ERR_MSG_ADDON_DATA_NO_SERIALIZABLE, aAddonId );
    }
    if( backendAddonDatas.hasKey( aAddonId ) ) {
      // Данные расширения уже зарегистрированы
      throw new TsItemAlreadyExistsRtException( ERR_MSG_ADDON_DATA_ALREADY_EXIST, aAddonId );
    }
    backendAddonDatas.put( aAddonId, aBackendAddonData );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SessionInitResult
  //
  @Override
  public IStringMap<IS5BackendAddonSession> baSessions() {
    return sessions;
  }

  @Override
  public <T extends IS5BackendAddonData> T getBackendAddonData( String aAddonId, Class<T> aAddonDataType ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aAddonDataType );
    try {
      return aAddonDataType.cast( backendAddonDatas.findByKey( aAddonId ) );
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ex.getMessage() );
    }
  }
}
