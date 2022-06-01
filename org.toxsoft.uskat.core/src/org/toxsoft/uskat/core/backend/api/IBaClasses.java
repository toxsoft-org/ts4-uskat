package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.backend.*;

/**
 * Backend addon for classes storage.
 * <p>
 * This is the mandatory addon.
 *
 * @author hazard157
 */
public interface IBaClasses
    extends IBackendAddon {

  /**
   * ID of this backend addon.
   */
  String ADDON_ID = SK_ID + "ba.Classes"; //$NON-NLS-1$

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
   * <p>
   * Sends an {@link IBaClassesMessages#MSGID_SYSDESCR_CHANGE} messages to the
   * {@link ISkFrontendRear#onBackendMessage(GtMessage)}.
   *
   * @param aRemoveClassIds {@link IStringList} - class IDs to remove
   * @param aUpdateClassInfos {@link IStridablesList}&lt;{@link IDtoClassInfo}&gt; - classes to add or update
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException there is the root class in lists
   */
  void writeClassInfos( IStringList aRemoveClassIds, IStridablesList<IDtoClassInfo> aUpdateClassInfos );

}
