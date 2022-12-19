package org.toxsoft.uskat.core.devapi.gwiddb;

import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * This is simple database containing key-value entries.
 * <p>
 * Keys are the GWIDs or existing entities. Values are the String CLOBs. If some entitiy is removed from the system
 * corresponding value will be removed from the database.
 *
 * @author hazard157
 */
public interface ISkGwidDbSection {

  /**
   * Lists keys of existing values in database.
   *
   * @return {@link IList}&lt; {@link Gwid} &gt; - the keys list
   */
  IList<Gwid> listKeys();

  /**
   * Determines if entry with the specified {@link Gwid} exists in the database.
   *
   * @param aKey {@link Gwid} - the key
   * @return boolean - <code>true</code> if there is a value in the database
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean hasClob( Gwid aKey );

  /**
   * Writes the value.
   *
   * @param aKey {@link Gwid} - the key
   * @param aValue String - the value
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such {@link Gwid} exits in the system
   */
  void writeClob( Gwid aKey, String aValue );

  /**
   * Reads the value of the entry nder the specified key.
   * <p>
   * For unexisting entires returns <code>null</code>. &#64;param aKey {@link Gwid} - the key &#64;return String - read
   * value or <code>null</code>
   *
   * @param aKey {@link Gwid} - the key
   * @return String - the read value or <code>null</code> if no entry exists in database
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  String readClob( Gwid aKey );

  /**
   * Removes the entry from database.
   * <p>
   * Unexisting keys are ignored.
   *
   * @param aKey {@link Gwid} - the key
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void removeClob( Gwid aKey );

}
