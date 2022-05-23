package org.toxsoft.uskat.core;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * USkat global constants.
 *
 * @author hazard157
 */
public interface ISkHardConstants {

  /**
   * IDpath prefix of the all USkat identifiers.
   */
  String SK_ID = "sk"; //$NON-NLS-1$

  /**
   * ID of the USkat core (not any particular service).
   */
  String SK_CORE_ID = SK_ID + ".core"; //$NON-NLS-1$

  /**
   * Core service identifiers prefix.
   */
  String SK_CORE_SERVICE_ID_PREFIX = SK_ID + ".service.core"; //$NON-NLS-1$

  /**
   * System extension service identifiers prefix.
   */
  String SK_SYSEXT_SERVICE_ID_PREFIX = SK_ID + ".service.sysext"; //$NON-NLS-1$

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

  // ------------------------------------------------------------------------------------
  // Meta-info on classes

  /**
   * Flags that attribute is the system managed. Type: {@link EAtomicType#BOOLEAN}<br>
   * Usage: system managed attributes values are stored as {@link SkObject} implementation class fields. At runtime this
   * attributes are also presented in the {@link ISkObject#attrs()} set howeverwhen storing object to database system
   * attributes are not stores as part of the {@link ISkObject#attrs()} set, rather they are stored by other means.<br>
   * Most obvious system attributes are {@link ISkHardConstants#AID_SKID},{@link ISkHardConstants#AID_STRID} and
   * {@link ISkHardConstants#AID_CLASS_ID}. All of them are derived from field {@link SkObject#skid()}.<br>
   * System attributes are used to decrease needed storege amount and more important, it avoids possible errors due to
   * stored data duplication (both in fields and attributes set).<br>
   * This option must be specified in {@link IDtoAttrInfo#params()}.<br>
   * Default value: <code>false</code>
   */
  IDataDef OPDEF_SK_IS_SYS_ATTR = create( SK_ID + ".IsSysAttr", BOOLEAN, //$NON-NLS-1$
      TSID_NAME, STR_N_IS_SYS_ATTR, //
      TSID_DESCRIPTION, STR_D_IS_SYS_ATTR, //
      TSID_IS_MANDATORY, AV_FALSE, //
      TSID_DEFAULT_VALUE, AV_FALSE //
  );

  /**
   * Determines if argument is the system attribute.
   *
   * @param aAttrInfo {@link IDtoAttrInfo} - the attribute info
   * @return boolean - <code>true</code> if argument has {@link #OPDEF_SK_IS_SYS_ATTR} flag set
   * @throws TsNullArgumentRtException argument = <code>null</code>
   */
  static boolean isSkSysAttr( IDtoAttrInfo aAttrInfo ) {
    TsNullArgumentRtException.checkNull( aAttrInfo );
    return OPDEF_SK_IS_SYS_ATTR.getValue( aAttrInfo.params() ).asBool();
  }

  /**
   * Flags the class definerd at runtime.<br>
   * Type: {@link EAtomicType#BOOLEAN}<br>
   * Usage: this option is not mandatory however it is very strongly recommended to set it to <code>true</code> for
   * classes, defined on runtime be the Javca source code. There is no need to save/restore such classes info, or there
   * is no need to display them in the SkIDE.<br>
   * This option must be specified in {@link IDtoClassInfo#params()}.<br>
   * Default value: <code>false</code>
   */
  IDataDef OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS = create( SK_ID + ".IsSourceCodeDefinedClass", STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_CLASS_IS_CODE_DEFINED, //
      TSID_DESCRIPTION, STR_D_CLASS_IS_CODE_DEFINED, //
      TSID_IS_NULL_ALLOWED, AV_TRUE, //
      TSID_DEFAULT_VALUE, AV_FALSE //
  );

}
