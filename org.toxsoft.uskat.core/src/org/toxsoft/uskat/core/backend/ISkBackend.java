package org.toxsoft.uskat.core.backend;

import org.toxsoft.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.tslib.coll.primtypes.IStringList;
import org.toxsoft.tslib.gw.IGwHardConstants;
import org.toxsoft.tslib.utils.ICloseable;
import org.toxsoft.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;

/**
 * USkat backend API.
 * <p>
 * Notes on implementation:
 * <ul>
 * <li>All modification methods may throw additional exceptions. Backed API does not require backend to enforce any
 * validation when writing data to it. However particular backend may check data to be written and not allow to violate
 * the storage integrity;</li>
 * <li>TODO frontend rear messaging - ???.</li>
 * <li>TODO working with threads- ???.</li>
 * </ul>
 *
 * @author hazard157
 */
public interface ISkBackend
    extends ICloseable {

  /**
   * Determines if backend is active.
   * <p>
   * Existence of backend instance (until it is closed) means that connection to USkat is open. However open connection
   * may be in inactive state, for example due to network failure. There is no API to change backend activity state -
   * backend tries to stay in active state or restore after temporary failures.
   *
   * @return boolean - backend activity state
   */
  boolean isActive();

  /**
   * Returns information about backend instance.
   *
   * @return {@link ISkBackendInfo} - the backend info
   */
  ISkBackendInfo getBackendInfo();

  /**
   * Read all classes from storage.
   * <p>
   * Note: root class is not stored so returned list does not includes class with ID
   * {@link IGwHardConstants#GW_ROOT_CLASS_ID}.
   *
   * @return {@link IStridablesList}&lt;{@link IDtoClassInfo}&gt; - list of infos of all classes
   */
  IStridablesList<IDtoClassInfo> readClassInfos();

  /**
   * Edits classes.
   *
   * @param aRemoveClassIds {@link IStringList} - class IDs to remove
   * @param aUpdateClassInfos {@link IStridablesList}&lt;{@link IDtoClassInfo}&gt; - classes to add or update
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException there is the root class in lists
   */
  void writeClassInfos( IStringList aRemoveClassIds, IStridablesList<IDtoClassInfo> aUpdateClassInfos );

}
