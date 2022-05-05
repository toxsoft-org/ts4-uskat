package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * Backend addon for objects storage.
 * <p>
 * This is the madatory addon.
 *
 * @author hazard157
 */
public interface IBaObjects
    extends IBackendAddon {

  /**
   * ID of this backend addon.
   */
  String ADDON_ID = SK_ID + "ba.Objects"; //$NON-NLS-1$

  /**
   * Finds the object by SKID.
   *
   * @param aSkid {@link Skid} - the SKID of the object
   * @return {@link IDtoObject} - found object or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IDtoObject findObject( Skid aSkid );

  /**
   * Returns all objects of the specified classes.
   * <p>
   * The backend does not understand class inheritance - it returns objects exactly by class identifiers, not including
   * objects of subclasses.
   * <p>
   * Non-existent classes are silently ignored.
   *
   * @param aClassIds {@link IStringList} - classes IDs
   * @return {@link IList}&lt;{@link IDtoObject}&gt; - the list of objects of requested classes
   */
  IList<IDtoObject> readObjects( IStringList aClassIds );

  /**
   * Returns objects of specified SKIDs.
   * <p>
   * Non-existent and repeating SKIDs are are silently ignored.
   *
   * @param aSkids {@link ISkidList} - the SKIDs
   * @return {@link IList}&lt;{@link IDtoObject}&gt; - the list of objects of requested SKIDs
   */
  IList<IDtoObject> readObjectsByIds( ISkidList aSkids );

  /**
   * Edits the objects.
   * <p>
   * Backend is "dumb" - it does not checks if rivets SKIDs refer to the existing objects.
   *
   * @param aRemoveSkids {@link ISkidList} - SKIDs of objects to be removed
   * @param aUpdateObjects {@link IList}&lt;{@link IDtoObject}&gt; - list obj objcets to add/change
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void writeObjects( ISkidList aRemoveSkids, IList<IDtoObject> aUpdateObjects );

}
