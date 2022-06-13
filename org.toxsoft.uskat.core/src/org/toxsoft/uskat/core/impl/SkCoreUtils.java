package org.toxsoft.uskat.core.impl;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.valobj.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Point of entry to the USkat and some methods used by CoreAPI implementation also useful for users.
 *
 * @author hazard157
 */
public class SkCoreUtils {

  /**
   * Initializes static stuff, must be called once before any USkat usage.
   */
  public static void initialize() {
    TsValobjUtils.registerKeeper( SkEvent.KEEPER_ID, SkEvent.KEEPER );
    TsValobjUtils.registerKeeper( EQueryState.KEEPER_ID, EQueryState.KEEPER );
    TsValobjUtils.registerKeeper( SkCommandState.KEEPER_ID, SkCommandState.KEEPER );
  }

  /**
   * Creates the instance of the single threaded {@link ISkConnection}.
   *
   * @return {@link ISkConnection} - instance of the connection in {@link ESkConnState#CLOSED CLOSED} state
   */
  public static ISkConnection createConnection() {
    return new SkConnection();
  }

  // ------------------------------------------------------------------------------------
  // FIXME where to place this methods?
  //

  /**
   * Creates root class (with ID {@link IGwHardConstants#GW_ROOT_CLASS_ID}) description.
   * <p>
   * This is the main and only way to create correct root class description.
   *
   * @return {@link DtoClassInfo} - root class
   */
  public static DtoClassInfo createRootClassDto() {
    DtoClassInfo dpuRoot = new DtoClassInfo( OptionSetUtils.createOpSet( //
        OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS, AV_TRUE //
    ) );
    // root class name
    dpuRoot.params().setStr( TSID_NAME, STR_N_ROOT_CLASS );
    dpuRoot.params().setStr( TSID_DESCRIPTION, STR_D_ROOT_CLASS );
    // --- creating attributes
    // AID_SKID
    dpuRoot.attrInfos().add( DtoAttrInfo.create1( AID_SKID, DataType.create( VALOBJ, //
        TSID_NAME, STR_N_ATTR_SKID, //
        TSID_DESCRIPTION, STR_D_ATTR_SKID, //
        TSID_KEEPER_ID, Skid.KEEPER_ID, //
        TSID_IS_NULL_ALLOWED, AV_FALSE, //
        TSID_DEFAULT_VALUE, avValobj( Skid.NONE ) //
    ), OptionSetUtils.createOpSet( //
        OPDEF_SK_IS_SYS_ATTR, AV_TRUE //
    ) ) );
    // AID_CLASS_ID
    dpuRoot.attrInfos().add( DtoAttrInfo.create1( AID_CLASS_ID, DataType.create( STRING, //
        TSID_NAME, STR_N_ATTR_CLASS_ID, //
        TSID_DESCRIPTION, STR_D_ATTR_CLASS_ID, //
        TSID_IS_NULL_ALLOWED, AV_FALSE, //
        TSID_DEFAULT_VALUE, avStr( Skid.NONE.classId() ) //
    ), OptionSetUtils.createOpSet( //
        OPDEF_SK_IS_SYS_ATTR, AV_TRUE //
    ) ) );
    // AID_STRID
    dpuRoot.attrInfos().add( DtoAttrInfo.create1( AID_STRID, DataType.create( STRING, //
        TSID_NAME, STR_N_ATTR_STRID, //
        TSID_DESCRIPTION, STR_D_ATTR_STRID, //
        TSID_IS_NULL_ALLOWED, AV_FALSE, //
        TSID_DEFAULT_VALUE, avStr( Skid.NONE.strid() ) //
    ), OptionSetUtils.createOpSet( //
        OPDEF_SK_IS_SYS_ATTR, AV_TRUE //
    ) ) );
    // AID_NAME
    dpuRoot.attrInfos().add( DtoAttrInfo.create1( AID_NAME, DDEF_NAME, IOptionSet.NULL ) );
    // AID_DESCRIPTION
    dpuRoot.attrInfos().add( DtoAttrInfo.create1( AID_DESCRIPTION, DDEF_DESCRIPTION, IOptionSet.NULL ) );
    return dpuRoot;
  }

  /**
   * Prohibit inheritance.
   */
  private SkCoreUtils() {
    // nop
  }
}
