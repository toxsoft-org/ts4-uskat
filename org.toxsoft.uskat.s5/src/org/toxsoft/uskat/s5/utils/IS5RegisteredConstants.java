package org.toxsoft.uskat.s5.utils;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Регистрируемые константы системы.
 *
 * @author mvk
 */
public interface IS5RegisteredConstants {

  /**
   * Все зарегистрированные константы системы
   */
  IStridablesList<IDataDef> ALL_REGISTERED_CONSTANTS = new StridablesList<>();

  /**
   * Создать и зарегистировать константу в пуле {@link #ALL_REGISTERED_CONSTANTS}.
   *
   * @param aId String - data identifier (IDpath)
   * @param aAtomicType {@link EAtomicType} - atomic type
   * @param aIdsAndValues Object[] - parameters as id / value pairs array
   * @return {@link IDataDef} описание созданной зарегистированной константы.
   * @throws TsItemAlreadyExistsRtException другая константа уже зарегистрирована по указанным идентификатором.
   */
  static IDataDef register( String aId, EAtomicType aAtomicType, Object... aIdsAndValues ) {
    IDataDef retValue = DataDef.create( aId, aAtomicType, aIdsAndValues );
    IDataDef other = ALL_REGISTERED_CONSTANTS.findByKey( retValue.id() );
    if( other != null && !other.equals( retValue ) ) {
      throw new TsItemAlreadyExistsRtException();
    }
    return retValue;
  }
}
