package org.toxsoft.uskat.regref.lib.impl;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.regref.lib.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.impl.DataDef;
import org.toxsoft.core.tslib.av.impl.DataType;
import org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.coll.helpers.CollConstraint;
import org.toxsoft.core.tslib.coll.primtypes.impl.SingleStringList;
import org.toxsoft.core.tslib.gw.IGwHardConstants;
import org.toxsoft.uskat.core.ISkHardConstants;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoAttrInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoEventInfo;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * Service sonatsnts with unmodifiable values.
 *
 * @author goga
 */
@SuppressWarnings( { "javadoc", "nls" } )
public interface ISkRegRefServiceHardConstants {

  String SERVICE_ID = SK_SYSEXT_SERVICE_ID_PREFIX + "RegRefService";

  // Identifier prefix of all classes owned by this service.
  String CLASSID_PREFIX_OWNED = ISkHardConstants.SK_SYSEXT_SERVICE_ID_PREFIX + ".regref.";

  // Идентификатор класса объектов, соответствующих разделам {@link ISkRriSection}.
  String CLASSID_RRI_SECTION = CLASSID_PREFIX_OWNED + "Section";

  // Base class for all comanion classes of all sections.
  String CLASSID_RRI_COMPANION_BASE = CLASSID_PREFIX_OWNED + "CompBase";

  // Префикс идентификаторов классов объектов-компаньенов.
  String CLASSID_START_COMPANION = CLASSID_PREFIX_OWNED + "comp";

  // тип данных, содержащий в себя IOptionSet
  // String TYPEID_OPTION_SET = ISkHardConstants.SK_ID_PREFIX + "OptionSet";
  // IDpuSdTypeInfo TYPEINF_OPTION_SET = DpuSdTypeInfo.create1( TYPEID_OPTION_SET, EAtomicType.VALOBJ, //
  // TSID_NAME, STR_N_TYPE_OPRION_SET, //
  // TSID_DESCRIPTION, STR_D_TYPE_OPRION_SET, //
  // OP_DEFAULT_VALUE, OptionSetKeeper.EMPTY_OPSET_AV //
  // );

  // Связь связи от объекта компаньена к местер-объекту.
  String LID_COMPANION_MASTER = "RriMasterObject";

  DtoLinkInfo LINFO_COMPANION_MASTER = DtoLinkInfo.create1( //
      LID_COMPANION_MASTER, //
      new SingleStringList( IGwHardConstants.GW_ROOT_CLASS_ID ), //
      new CollConstraint( 1, true, true, false ), //
      OptionSetUtils.createOpSet( //
          DDEF_NAME, STR_N_LINK_RRI_COMPANION_MASTER, //
          DDEF_DESCRIPTION, STR_D_LINK_RRI_COMPANION_MASTER //
      ) );

  // Атрибут объекта раздела, который хранит параметры ISkRriSection.params()
  String       AID_RRI_SECTION_PARAMS  = "Params";
  IDtoAttrInfo AINF_RRI_SECTION_PARAMS = DtoAttrInfo.create1( AID_RRI_SECTION_PARAMS, DataType.create( VALOBJ,   //
      TSID_NAME, STR_N_ATTR_RRI_SECTION_PARAMS,                                                                  //
      TSID_DESCRIPTION, STR_N_ATTR_RRI_SECTION_PARAMS,                                                           //
      TSID_KEEPER_ID, OptionSetKeeper.KEEPER_ID,                                                                 //
      TSID_IS_NULL_ALLOWED, AV_FALSE,                                                                            //
      TSID_DEFAULT_VALUE, avValobj( new OptionSet() )                                                            //
  ), IOptionSet.NULL );

  /**
   * Идентификатор события {@link #EVDPU_RRI_PARAM_CHANGE}.
   */
  String EVID_RRI_PARAM_CHANGE = "RriParamsChange";

  String EVPRMID_REASON       = "reason";
  String EVPRMID_AUTHOR_LOGIN = "authorLogin";
  String EVPRMID_SECTION_ID   = "sectionId";
  String EVPRMID_PARAM_GWID   = "paramGwid";
  String EVPRMID_IS_LINK      = "isLink";
  String EVPRMID_OLD_VAL_ATTR = "oldAttr";
  String EVPRMID_NEW_VAL_ATTR = "newAttr";
  String EVPRMID_OLD_VAL_LINK = "oldLink";
  String EVPRMID_NEW_VAL_LINK = "newLink";

  IDataDef SDD_EVPRM_REASON = DataDef.create( EVPRMID_REASON, STRING, //
      TSID_NAME, STR_N_EVPRM_REASON, //
      TSID_DESCRIPTION, STR_D_EVPRM_REASON //
  );

  IDataDef SDD_EVPRM_AUTHOR_LOGIN = DataDef.create( EVPRMID_AUTHOR_LOGIN, STRING, //
      TSID_NAME, STR_N_EVPRM_AUTHOR_LOGIN, //
      TSID_DESCRIPTION, STR_D_EVPRM_AUTHOR_LOGIN//
  );

  IDataDef SDD_EVPRM_SECTION_ID = DataDef.create( EVPRMID_SECTION_ID, STRING, //
      TSID_NAME, STR_N_EVPRM_SECTION_ID, //
      TSID_DESCRIPTION, STR_D_EVPRM_SECTION_ID //
  );

  IDataDef SDD_EVPRM_PARAM_GWID = DataDef.create( EVPRMID_PARAM_GWID, VALOBJ, //
      TSID_NAME, STR_N_EVPRM_PARAM_ID, //
      TSID_DESCRIPTION, STR_D_EVPRM_PARAM_ID //
  );

  IDataDef SDD_EVPRM_IS_LINK = DataDef.create( EVPRMID_IS_LINK, BOOLEAN, //
      TSID_NAME, STR_N_EVPRM_IS_LINK, //
      TSID_DESCRIPTION, STR_D_EVPRM_IS_LINK //
  );

  IDataDef SDD_EVPRM_OLD_VAL_ATTR = DataDef.create( EVPRMID_OLD_VAL_ATTR, NONE, //
      TSID_NAME, STR_N_EVPRM_OLD_VAL_ATTR, //
      TSID_DESCRIPTION, STR_D_EVPRM_OLD_VAL_ATTR//
  );

  IDataDef SDD_EVPRM_NEW_VAL_ATTR = DataDef.create( EVPRMID_NEW_VAL_ATTR, NONE, //
      TSID_NAME, STR_N_EVPRM_NEW_VAL_ATTR, //
      TSID_DESCRIPTION, STR_D_EVPRM_NEW_VAL_ATTR//
  );

  IDataDef SDD_EVPRM_OLD_VAL_LINK = DataDef.create( EVPRMID_OLD_VAL_LINK, VALOBJ, //
      TSID_NAME, STR_N_EVPRM_OLD_VAL_LINK, //
      TSID_DESCRIPTION, STR_D_EVPRM_OLD_VAL_LINK //
  );

  IDataDef SDD_EVPRM_NEW_VAL_LINK = DataDef.create( EVPRMID_NEW_VAL_LINK, VALOBJ, //
      TSID_NAME, STR_N_EVPRM_NEW_VAL_LINK, //
      TSID_DESCRIPTION, STR_D_EVPRM_NEW_VAL_LINK //
  );

  /**
   * Структура описание события "изменение параметров НСИ".
   * <p>
   * Внимание: это событие генерируется для объекта
   */
  IDtoEventInfo EVDPU_RRI_PARAM_CHANGE = DtoEventInfo.create1( EVID_RRI_PARAM_CHANGE, true, // aIsHist = true
      new StridablesList<>( //
          SDD_EVPRM_SECTION_ID, //
          SDD_EVPRM_PARAM_GWID, //
          SDD_EVPRM_AUTHOR_LOGIN, //
          SDD_EVPRM_IS_LINK, //
          SDD_EVPRM_REASON, //
          SDD_EVPRM_OLD_VAL_ATTR, //
          SDD_EVPRM_NEW_VAL_ATTR, //
          SDD_EVPRM_OLD_VAL_LINK, //
          SDD_EVPRM_NEW_VAL_LINK //
      ), //
      OptionSetUtils.createOpSet( //
          IAvMetaConstants.TSID_NAME, STR_N_EVENT_RRI_EDIT, //
          IAvMetaConstants.TSID_DESCRIPTION, STR_D_EVENT_RRI_EDIT //
      ) );
}
