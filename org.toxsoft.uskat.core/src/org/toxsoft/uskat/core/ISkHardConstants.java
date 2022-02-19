package org.toxsoft.uskat.core;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * USkat global constants.
 *
 * @author goga
 */
public interface ISkHardConstants {

  /**
   * IDpath prefix of the all USkat identifiers.
   */
  String SK_ID = "sk"; //$NON-NLS-1$

  /**
   * Core service identifiers prefix.
   */
  String SK_CORE_SERVICE_ID_PREFIX = SK_ID + ".core"; //$NON-NLS-1$

  /**
   * System extension service identifiers prefix.
   */
  String SK_SYSEXT_SERVICE_ID_PREFIX = SK_ID + ".sysext"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // The root class constants
  //

  /**
   * Attribute identifier: ISkObject.skid().
   */
  String AID_SKID = SK_ID + ".skid"; //$NON-NLS-1$

  /**
   * Attribute identifier: ISkObject.classId().
   */
  String AID_CLASS_ID = SK_ID + ".classId"; //$NON-NLS-1$

  /**
   * Attribute identifier: ISkObject.strid().
   */
  String AID_STRID = SK_ID + ".strid"; //$NON-NLS-1$

  /**
   * Attribute identifier: ISkObject.nmName().
   */
  String AID_NAME = IAvMetaConstants.TSID_NAME;

  /**
   * Attribute identifier: ISkObject.description().
   */
  String AID_DESCRIPTION = IAvMetaConstants.TSID_DESCRIPTION;

  /**
   * Identifiers of the ISkObject system attributes.
   * <p>
   * This attributes represent the invariant SKID identifier and they must not be changed by the common code. The only
   * place the values of these attributes are set is SkObject constructor.
   */
  IStringList SK_SYS_ATTR_IDS = new StringArrayList( //
      AID_CLASS_ID, //
      AID_STRID, //
      AID_SKID //
  );

  /**
   * Determines if argument is on of the {@link #SK_SYS_ATTR_IDS} system attribute identifier.
   *
   * @param aAttrId String - identifier to be checked
   * @return boolean - <code>true</code> if argument is on of the {@link #SK_SYS_ATTR_IDS} identifiers
   * @throws TsNullArgumentRtException argument = <code>null</code>
   */
  static boolean isSkSysAttrId( String aAttrId ) {
    TsNullArgumentRtException.checkNull( aAttrId );
    return SK_SYS_ATTR_IDS.hasElem( aAttrId );
  }

}
