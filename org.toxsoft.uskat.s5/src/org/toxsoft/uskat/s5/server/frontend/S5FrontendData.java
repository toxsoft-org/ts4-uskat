package org.toxsoft.uskat.s5.server.frontend;

import static org.toxsoft.uskat.s5.server.frontend.IS5Resources.*;

import java.io.Serializable;

import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.impl.S5EventSupport;

/**
 * Реализация {@link IS5FrontendData}.
 *
 * @author mvk
 */
public final class S5FrontendData
    implements IS5FrontendData, Serializable {

  private static final long serialVersionUID = 157157L;

  private final S5EventSupport                       events     = new S5EventSupport();
  private final IStringMapEdit<IS5FrontendAddonData> addonsData = new StringMap<>();

  // ------------------------------------------------------------------------------------
  // Реализация IS5FrontendData
  //
  @Override
  public S5EventSupport events() {
    return events;
  }

  @Override
  public IStringList addonIds() {
    return addonsData.keys();
  }

  @Override
  public <T extends IS5FrontendAddonData> T getAddonData( String aAddonId, Class<T> aAddonDataType ) {
    TsNullArgumentRtException.checkNulls( aAddonId, aAddonDataType );
    try {
      return aAddonDataType.cast( addonsData.findByKey( aAddonId ) );
    }
    catch( Exception ex ) {
      throw new TsIllegalArgumentRtException( ex, ex.getMessage() );
    }
  }

  @Override
  public void setAddonData( String aAddonId, IS5FrontendAddonData aData ) {
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
  // Реализация Object
  //
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for( int index = 0, n = addonsData.size(); index < n; index++ ) {
      String addonId = addonsData.keys().get( index );
      IS5FrontendAddonData addonData = addonsData.values().get( index );
      sb.append( addonId );
      sb.append( '=' );
      sb.append( addonData.toString() );
      if( index + 1 < n ) {
        sb.append( ',' );
      }
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + addonsData.hashCode();
    return result;
  }

  @Override
  public boolean equals( Object aObject ) {
    if( this == aObject ) {
      return true;
    }
    if( aObject == null ) {
      return false;
    }
    if( getClass() != aObject.getClass() ) {
      return false;
    }
    S5FrontendData other = (S5FrontendData)aObject;
    return addonsData.equals( other.addonsData );
  }
}
