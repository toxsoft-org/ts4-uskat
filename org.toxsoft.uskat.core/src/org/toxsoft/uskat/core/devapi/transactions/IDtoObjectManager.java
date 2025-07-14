package org.toxsoft.uskat.core.devapi.transactions;

import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * {@link IDtoObject} Persistence Manager (similar to EntityManager).
 *
 * @author mvk
 */
public interface IDtoObjectManager {

  /**
   * Find object.
   *
   * @param aObjId {@link Skid} object id.
   * @return {@link IDtoObject} obj or null
   * @throws TsNullArgumentRtException arg = null
   */
  IDtoObject find( Skid aObjId );

  /**
   * Persist object.
   *
   * @param aDtoObj {@link IDtoObject} object.
   * @throws TsNullArgumentRtException arg = null
   * @throws TsIllegalArgumentRtException obj is already been removed
   * @throws TsIllegalArgumentRtException obj is already exist
   */
  void persist( IDtoObject aDtoObj );

  /**
   * Merge object.
   *
   * @param aDtoObj {@link IDtoObject} object.
   * @return {@link IDtoObject} merged object.
   * @throws TsNullArgumentRtException arg = null
   * @throws TsIllegalArgumentRtException obj is already been removed
   * @throws TsIllegalArgumentRtException obj is not found
   */
  IDtoObject merge( IDtoObject aDtoObj );

  /**
   * Remove object.
   *
   * @param aDtoObj {@link IDtoObject} object.
   * @return boolean <b>true</b> object was removed; <b>false</b> object is not found.
   * @throws TsNullArgumentRtException arg = null
   */
  boolean remove( IDtoObject aDtoObj );

  /**
   * Returns an indication that the object existed but was removed.
   *
   * @param aSkid {@link Skid} object id
   * @return boolean <b>true</b> object removed; <b>false</b> object was not removed
   * @throws TsNullArgumentRtException arg = null
   */
  boolean wasRemoved( Skid aSkid );
}
