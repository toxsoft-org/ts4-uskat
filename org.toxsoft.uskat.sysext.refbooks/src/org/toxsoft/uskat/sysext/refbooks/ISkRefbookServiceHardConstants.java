package org.toxsoft.uskat.sysext.refbooks;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.sysext.refbooks.ISkResources.*;
import static ru.uskat.common.ISkHardConstants.*;
import static ru.uskat.common.dpu.impl.DpuStridableDataDef.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetUtils;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.coll.helpers.ECrudOp;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.common.ISkHardConstants;
import ru.uskat.common.dpu.IDpuSdEventInfo;
import ru.uskat.common.dpu.IDpuStridableDataDef;
import ru.uskat.common.dpu.impl.DpuSdEventInfo;

/**
 * Service conatsnts with unmodifiable values.
 *
 * @author goga
 */
@SuppressWarnings( "nls" )
public interface ISkRefbookServiceHardConstants {

  /**
   * Service identifier.
   */
  String SERVICE_ID = SK_SYSEXT_SERVICE_ID_PREFIX + "RefbookService";

  /**
   * Identifier prefix of all classes owned by this service.
   */
  String CLASSID_PREFIX_OWNED = ISkHardConstants.SK_ID_PREFIX + ".refbook.class";
  @SuppressWarnings( "javadoc" )
  String CLASSID_START_OWNED  = CLASSID_PREFIX_OWNED + ".";

  /**
   * {@link ISkRefbook} class identifier.
   */
  String CLASSID_REFBOOK = CLASSID_START_OWNED + "refbook";

  /**
   * Refbook item classes identifier starting IDpath.
   */
  String CLASSID_PREFIX_REFBOOK_ITEM = CLASSID_START_OWNED + "refbook_item";
  @SuppressWarnings( "javadoc" )
  String CLASSID_START_REFBOOK_ITEM  = CLASSID_PREFIX_REFBOOK_ITEM + ".";

  /**
   * Attribute ID: {@link ISkRefbook#itemClassId()}.
   */
  String AID_ITEM_CLASS_ID = "ItemClassId";

  /**
   * Attribute OpDef: {@link #AID_ITEM_CLASS_ID}.
   */
  IDataDef OP_ATTR_ITEM_CLASS_ID = create( AID_ITEM_CLASS_ID, STRING, TSID_NAME, STR_N_ITEM_CLASS_ID, //
      TSID_DESCRIPTION, STR_D_ITEM_CLASS_ID, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_IS_MANDATORY, AV_TRUE, //
      TSID_DEFAULT_VALUE, AvUtils.AV_STR_EMPTY );

  /**
   * Determines if argument may be refbook item class ID.
   * <p>
   * Performs only syntaxic check of ID.
   *
   * @param aClassId String - ID to be checked
   * @return boolean <code>true</code> - if argument may be refbook item ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  static boolean isProbableRefbookItemClassId( String aClassId ) {
    if( StridUtils.isValidIdPath( aClassId ) ) {
      return StridUtils.startsWithIdPath( aClassId, CLASSID_PREFIX_REFBOOK_ITEM );
    }
    return false;
  }

  /**
   * Converts the refbook ID {@link ISkRefbook#id()} to the item class identifier ID {@link ISkRefbook#itemClassId()}.
   *
   * @param aRefbookId String - the refbook ID {@link ISkRefbook#id()}
   * @return String - refbook items class ID
   */
  static String makeItemClassIdFromRefbookId( String aRefbookId ) {
    return StridUtils.makeIdPath( CLASSID_PREFIX_REFBOOK_ITEM, aRefbookId );
  }

  /**
   * Converts the item class identifier ID {@link ISkRefbook#itemClassId()} to the refbook ID {@link ISkRefbook#id()}.
   *
   * @param aItemClassId String - refbook items class ID
   * @return the refbook ID {@link ISkRefbook#id()}
   */
  static String makeRefbookIdFromItemClassId( String aItemClassId ) {
    return StridUtils.removeStartingIdNames( aItemClassId,
        StridUtils.getComponents( CLASSID_PREFIX_REFBOOK_ITEM ).size() );
  }

  /**
   * Constructs the refbook object {@link Skid}.
   *
   * @param aRefbookId String - refbook ID
   * @return {@link Skid} - the refbook object {@link Skid}
   */
  static Skid makeRefbookObjSkid( String aRefbookId ) {
    return new Skid( CLASSID_REFBOOK, aRefbookId );
  }

  // ------------------------------------------------------------------------------------
  // Refbook items change event
  //

  /**
   * Refbook item has changed event ID.
   */
  String EVID_REFBOOK_ITEM_CHANGE = "RefbookItemChange"; //$NON-NLS-1$

  /**
   * {@link #EVID_REFBOOK_ITEM_CHANGE} param: kind of change, ie, {@link ECrudOp}.
   * <p>
   * Note: Parameter may have only {@link ECrudOp#CREATE}, {@link ECrudOp#EDIT} and {@link ECrudOp#REMOVE} values.
   * {@link ECrudOp#LIST} never is generated.
   */
  String EVPRMID_CRUD_OP = "crudOp";

  /**
   * {@link #EVID_REFBOOK_ITEM_CHANGE} param: the SKID of the affected item.
   */
  String EVPRMID_ITEM_SKID = "itemSkid";

  /**
   * {@link #EVID_REFBOOK_ITEM_CHANGE} param: old (before item change) values of the attributes.
   * <p>
   * For {@link ECrudOp#CREATE} this parameter does not no present or if present, has {@link IAtomicValue#NULL} value.
   */
  String EVPRMID_OLD_ATTRS = "oldAttrs";

  /**
   * {@link #EVID_REFBOOK_ITEM_CHANGE} param: new (after item change) values of the attributes.
   * <p>
   * For {@link ECrudOp#REMOVE} this parameter does not no present or if present, has {@link IAtomicValue#NULL} value.
   */
  String EVPRMID_NEW_ATTRS = "newAttrs";

  /**
   * {@link #EVID_REFBOOK_ITEM_CHANGE} param: old (before item change) values of the item links.
   * <p>
   * For {@link ECrudOp#LIST} this parameter does not no present or if present, has {@link IAtomicValue#NULL} value.
   */
  String EVPRMID_OLD_LINKS = "oldLinks";

  /**
   * {@link #EVID_REFBOOK_ITEM_CHANGE} param: new (after item change) values of the item links.
   * <p>
   * For {@link ECrudOp#REMOVE} this parameter does not no present or if present, has {@link IAtomicValue#NULL} value.
   */
  String EVPRMID_NEW_LINKS = "newLinks";

  /**
   * {@link #EVPRMID_CRUD_OP} parameter definition DPU.
   */
  IDpuStridableDataDef EVPRMDEF_CRUD_OP = create1( EVPRMID_CRUD_OP, DDID_VALOBJ, //
      TSID_NAME, STR_N_CRUD_OP, //
      TSID_DESCRIPTION, STR_D_CRUD_OP, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL );

  /**
   * {@link #EVPRMID_ITEM_SKID} parameter definition DPU.
   */
  IDpuStridableDataDef EVPRMDEF_ITEM_SKID = create1( EVPRMID_ITEM_SKID, DDID_VALOBJ, //
      TSID_NAME, STR_N_ITEM_SKID, //
      TSID_DESCRIPTION, STR_D_ITEM_SKID, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL );

  /**
   * {@link #EVPRMID_OLD_ATTRS} parameter definition DPU.
   */
  IDpuStridableDataDef EVPRMDEF_OLD_ATTRS = create1( EVPRMID_OLD_ATTRS, DDID_VALOBJ, //
      TSID_NAME, STR_N_OLD_ATTRS, //
      TSID_DESCRIPTION, STR_D_OLD_ATTRS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL );

  /**
   * {@link #EVPRMID_NEW_ATTRS} parameter definition DPU.
   */
  IDpuStridableDataDef EVPRMDEF_NEW_ATTRS = create1( EVPRMID_NEW_ATTRS, DDID_VALOBJ, //
      TSID_NAME, STR_N_NEW_ATTRS, //
      TSID_DESCRIPTION, STR_D_NEW_ATTRS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL );

  /**
   * {@link #EVPRMID_OLD_LINKS} parameter definition DPU.
   */
  IDpuStridableDataDef EVPRMDEF_OLD_LINKS = create1( EVPRMID_OLD_LINKS, DDID_VALOBJ, //
      TSID_NAME, STR_N_OLD_LINKS, //
      TSID_DESCRIPTION, STR_D_OLD_LINKS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL );

  /**
   * {@link #EVPRMID_NEW_LINKS} parameter definition DPU.
   */
  IDpuStridableDataDef EVPRMDEF_NEW_LINKS = create1( EVPRMID_NEW_LINKS, DDID_VALOBJ, //
      TSID_NAME, STR_N_NEW_LINKS, //
      TSID_DESCRIPTION, STR_D_NEW_LINKS, //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, IAtomicValue.NULL );

  /**
   * Refbook item has changed event descriptive DPU.
   */
  IDpuSdEventInfo EVDPU_REFBOOK_ITEM_CHANGE = DpuSdEventInfo.create2( EVID_REFBOOK_ITEM_CHANGE, //
      OptionSetUtils.createOpSet( //
          TSID_NAME, STR_N_REFBOOK_ITEM_CHANGE, //
          TSID_DESCRIPTION, STR_D_REFBOOK_ITEM_CHANGE //
      ), //
      EVPRMDEF_CRUD_OP, //
      EVPRMDEF_ITEM_SKID, //
      EVPRMDEF_OLD_ATTRS, //
      EVPRMDEF_NEW_ATTRS, //
      EVPRMDEF_OLD_LINKS, //
      EVPRMDEF_NEW_LINKS //
  );

}
