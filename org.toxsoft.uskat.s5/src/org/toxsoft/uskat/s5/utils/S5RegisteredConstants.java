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
public class S5RegisteredConstants {

  /**
   * Все зарегистрированные константы системы
   */
  private static final IStridablesList<IDataDef> registredConstants = new StridablesList<>();

  /**
   * Возвращает все зарегистированные константы в системе.
   *
   * @return {@link IStridablesList}&lt;{@link IDataDef}&gt; список констант.
   */
  public static IStridablesList<IDataDef> allRegisteredConstants() {
    synchronized (registredConstants) {
      return new StridablesList<>( registredConstants );
    }
  }

  /**
   * Создать и зарегистировать константу в системе {@link #registredConstants}.
   *
   * @param aId String - data identifier (IDpath)
   * @param aAtomicType {@link EAtomicType} - atomic type
   * @param aIdsAndValues Object[] - parameters as id / value pairs array
   * @return {@link IDataDef} описание созданной зарегистированной константы.
   * @throws TsItemAlreadyExistsRtException другая константа уже зарегистрирована по указанным идентификатором.
   */
  static public IDataDef register( String aId, EAtomicType aAtomicType, Object... aIdsAndValues ) {
    IDataDef newConstant = DataDef.create( aId, aAtomicType, aIdsAndValues );
    synchronized (registredConstants) {
      IDataDef retValue = registredConstants.findByKey( newConstant.id() );
      if( retValue != null && !retValue.equals( newConstant ) ) {
        throw new TsItemAlreadyExistsRtException();
      }
      if( retValue == null ) {
        ((IStridablesListEdit<IDataDef>)registredConstants).add( newConstant );
        retValue = newConstant;
      }
      return retValue;
    }
  }
}
