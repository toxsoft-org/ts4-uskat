package org.toxsoft.uskat.s5.server.sessions.init;

import static org.toxsoft.uskat.s5.server.sessions.init.IS5Resources.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesListEdit;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonRemote;
import org.toxsoft.uskat.s5.server.backend.IS5BackendAddonSession;

import ru.uskat.common.dpu.IDpuSdClassInfo;
import ru.uskat.common.dpu.IDpuSdTypeInfo;

/**
 * Реализация {@link IS5SessionInitResult}
 *
 * @author mvk
 */
public final class S5SessionInitResult
    implements IS5SessionInitResult, Serializable {

  private static final long serialVersionUID = 157157L;

  private final IStridablesListEdit<IDpuSdTypeInfo>       typeInfos  = new StridablesList<>();
  private final IStridablesListEdit<IDpuSdClassInfo>      classInfos = new StridablesList<>();
  private final IStringMapEdit<IS5BackendAddonRemote>     addons     = new StringMap<>();
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
    typeInfos.setAll( aSource.typeInfos() );
    classInfos.setAll( aSource.classInfos() );
    addons.setAll( aSource.addons() );
    addonsData.clear();
    for( String addonId : addons.keys() ) {
      IS5SessionAddonInitResult addonData = aSource.getAddonData( addonId, IS5SessionAddonInitResult.class );
      if( addonData != null ) {
        addonsData.put( addonId, addonData );
      }
    }
  }

  /**
   * Устанавливает описания всех типов зарегистрированных в системе на момент подключения к серверу
   *
   * @param aTypeInfos {@link IStridablesList}&lt;{@link IDpuSdTypeInfo}&gt; описания типов
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setTypeInfos( IStridablesList<IDpuSdTypeInfo> aTypeInfos ) {
    TsNullArgumentRtException.checkNull( aTypeInfos );
    typeInfos.setAll( aTypeInfos );
  }

  /**
   * Устанавливает описания всех классов зарегистрированных в системе на момент подключения к серверу
   *
   * @param aClassInfos {@link IStridablesList}&lt;{@link IDpuSdClassInfo}&gt; описания классов
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setClassInfos( IStridablesList<IDpuSdClassInfo> aClassInfos ) {
    TsNullArgumentRtException.checkNull( aClassInfos );
    classInfos.setAll( aClassInfos );
  }

  /**
   * Устанавливает карту удаленного доступа к расширениям backend
   *
   * @param aAddons {@link IS5BackendAddonRemote} карта удаленного доступа к расширениям backend. <br>
   *          Ключ: идентификатор backend {@link IS5BackendAddonSession#id()};<br>
   *          Значение: удаленный доступ к расширенияю backend.
   * @throws TsNullArgumentRtException аргумент = null
   */
  public void setAddons( IStringMap<IS5BackendAddonRemote> aAddons ) {
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
  public IStridablesList<IDpuSdTypeInfo> typeInfos() {
    return typeInfos;
  }

  @Override
  public IStridablesList<IDpuSdClassInfo> classInfos() {
    return classInfos;
  }

  @Override
  public IStringMap<IS5BackendAddonRemote> addons() {
    return addons;
  }

  @Override
  public <T extends IS5SessionAddonInitResult> T getAddonData( String aAddonId, Class<T> aAddonDataType ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aAddonDataType );
    try {
      return aAddonDataType.cast( addonsData.findByKey( aAddonId ) );
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ex.getMessage() );
    }
  }
}
