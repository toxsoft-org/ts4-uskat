package org.toxsoft.uskat.s5.server.sessions.init;

import static org.toxsoft.uskat.s5.server.sessions.init.IS5Resources.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendAddonSessionControl;

/**
 * Реализация {@link IS5SessionInitResult}
 *
 * @author mvk
 */
public final class S5SessionInitResult
    implements IS5SessionInitResult, Serializable {

  private static final long serialVersionUID = 157157L;

  private final IStringMapEdit<IS5BackendAddonSession>    addons     = new StringMap<>();
  private final IStringMapEdit<IS5SessionAddonInitResult> addonsData = new StringMap<>();

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
    addons.setAll( aSource.baSessions() );
    addonsData.clear();
    for( String addonId : addons.keys() ) {
      IS5SessionAddonInitResult addonData = aSource.getBaData( addonId, IS5SessionAddonInitResult.class );
      if( addonData != null ) {
        addonsData.put( addonId, addonData );
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
    addons.setAll( aAddons );
  }

  /**
   * Устанавливает данные расширения бекенда
   *
   * @param aAddonId String идентификатор (ИД-путь) расширения
   * @param aData {@link IS5SessionAddonInitResult} данные фронтенда расширения бекенд
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException данные должны поддерживать сериализацию
   * @throws TsItemAlreadyExistsRtException данные расширения уже установлены
   */
  public void setAddonData( String aAddonId, IS5SessionAddonInitResult aData ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aData );
    if( !(aData instanceof Serializable) ) {
      // Данные должны поддерживать сериализацию
      throw new TsIllegalArgumentRtException( ERR_MSG_ADDON_DATA_NO_SERIALIZABLE, aAddonId );
    }
    if( addonsData.hasKey( aAddonId ) ) {
      // Данные расширения уже зарегистрированы
      throw new TsItemAlreadyExistsRtException( ERR_MSG_ADDON_DATA_ALREADY_EXIST, aAddonId );
    }
    addonsData.put( aAddonId, aData );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5SessionInitResult
  //
  @Override
  public IStringMap<IS5BackendAddonSession> baSessions() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T extends IS5SessionAddonInitResult> T getBaData( String aAddonId, Class<T> aAddonDataType ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aAddonDataType );
    try {
      return aAddonDataType.cast( addonsData.findByKey( aAddonId ) );
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ex.getMessage() );
    }
  }
}
