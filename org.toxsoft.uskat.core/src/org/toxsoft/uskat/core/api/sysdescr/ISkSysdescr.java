package org.toxsoft.uskat.core.api.sysdescr;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * Class management.
 *
 * @author hazard157
 */
public interface ISkSysdescr
    extends ISkService {

  /**
   * TODO in sysdescr:
   * <ul>
   * <li>class/object "owner" - API? make clear what it is and how it works;</li>
   * </ul>
   */

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".Sysdescr"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Get info

  /**
   * Finds the class information by the class ID.
   *
   * @param aClassId String - the class ID
   * @return {@link ISkClassInfo} - the class information or <code>null</code> if there is no such class
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ISkClassInfo findClassInfo( String aClassId );

  /**
   * Returns the class information by the class ID.
   *
   * @param aClassId String - the class ID
   * @return {@link ISkClassInfo} - the class information
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such class
   */
  ISkClassInfo getClassInfo( String aClassId );

  /**
   * Returns information about all classes.
   *
   * @return {@link IStridablesList}&lt;{@link ISkClassInfo}&gt; - list of all classes
   */
  IStridablesList<ISkClassInfo> listClasses();

  // ------------------------------------------------------------------------------------
  // Editing

  /**
   * Creates new or changes existing class.
   * <p>
   * Argument must contain only self properties of the class, without parent properties.
   *
   * @param aDtoClassInfo {@link IDtoClassInfo} - new information of the class
   * @return {@link ISkClassInfo} - created or edited class
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed check {@link ISkSysdescrValidator#canCreateClass(IDtoClassInfo)} or
   *           {@link ISkSysdescrValidator#canEditClass(IDtoClassInfo, ISkClassInfo)}
   */
  ISkClassInfo defineClass( IDtoClassInfo aDtoClassInfo );

  /**
   * Permanently removes (deletes) the class.
   *
   * @param aClassId String - ID of the class to be deleted
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed check {@link ISkSysdescrValidator#canRemoveClass(String)}
   */
  void removeClass( String aClassId );

  // ------------------------------------------------------------------------------------
  // Classes ownership

  // TODO Classes ownership

  // ------------------------------------------------------------------------------------
  // Service support

  /**
   * Returns the service validator.
   *
   * @return {@link ITsValidationSupport}&lt;{@link ISkSysdescrValidator}&gt; - the service validator
   */
  ITsValidationSupport<ISkSysdescrValidator> svs();

}
