package org.toxsoft.uskat.sded.gui.km5.sded;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind.*;
import static org.toxsoft.uskat.sded.gui.ISkSdedGuiConstants.*;
import static org.toxsoft.uskat.sded.gui.km5.IKM5SdedConstants.*;
import static org.toxsoft.uskat.sded.gui.km5.ISkSdedKm5SharedResources.*;

import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.models.misc.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;

/**
 * M5-model of the {@link IDtoClassInfo}.
 * <p>
 * Note: model's LM provides {@link IDtoClassInfo} with only self properties, without parents properties. Say, for
 * attributes {@link IDtoClassInfo#attrInfos()} contains only attributes declared in this class.
 *
 * @author hazard157
 * @author dima
 */
public class SdedDtoClassInfoM5Model
    extends KM5ConnectedModelBase<IDtoClassInfo> {

  /**
   * Attribute {@link IDtoClassInfo#id()}.
   */
  public final IM5AttributeFieldDef<IDtoClassInfo> CLASS_ID = new M5AttributeFieldDef<>( FID_CLASS_ID, DDEF_IDPATH ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_CLASS_ID, STR_D_CLASS_ID );
      setFlags( M5FF_INVARIANT | M5FF_COLUMN );
    }

    protected IAtomicValue doGetFieldValue( IDtoClassInfo aEntity ) {
      return avStr( aEntity.id() );
    }

  };

  /**
   * Attribute {@link IDtoClassInfo#parentId()}.
   */
  public final IM5SingleLookupFieldDef<IDtoClassInfo, String> PARENT_ID =
      new M5SingleLookupFieldDef<>( FID_PARENT_ID, StringM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_N_PARENT_ID, STR_D_PARENT_ID );
          setFlags( M5FF_INVARIANT | M5FF_DETAIL );
          setDefaultValue( IGwHardConstants.GW_ROOT_CLASS_ID );
          setLookupProvider( () -> {
            IStridablesList<ISkClassInfo> llAll = skSysdescr().listClasses();

            // TODO Auto-generated method stub
            return llAll.keys();
          } );
        }

        protected String doGetFieldValue( IDtoClassInfo aEntity ) {
          return aEntity.parentId();
        }

      };

  /**
   * Attribute {@link IDtoClassInfo#nmName()}.
   */
  public final IM5AttributeFieldDef<IDtoClassInfo> NAME = new M5AttributeFieldDef<>( FID_NAME, DDEF_NAME ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_CLASS_NAME, STR_D_CLASS_NAME );
      setFlags( M5FF_COLUMN );
    }

    protected IAtomicValue doGetFieldValue( IDtoClassInfo aEntity ) {
      return avStr( aEntity.nmName() );
    }

  };

  /**
   * Attribute {@link IDtoClassInfo#description()}.
   */
  public final IM5AttributeFieldDef<IDtoClassInfo> DESCRIPTION =
      new M5AttributeFieldDef<>( FID_DESCRIPTION, DDEF_DESCRIPTION ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_N_CLASS_DESCRIPTION, STR_D_CLASS_DESCRIPTION );
          setFlags( M5FF_COLUMN );
        }

        protected IAtomicValue doGetFieldValue( IDtoClassInfo aEntity ) {
          return avStr( aEntity.description() );
        }

      };

  /**
   * Field contains of list attrs {@link IDtoClassInfo#attrInfos()}.
   */
  public final IM5MultiModownFieldDef<IDtoClassInfo, IDtoAttrInfo> SELF_ATTR_INFOS =
      new M5MultiModownFieldDef<>( FID_SELF_ATTR_INFOS, MID_SDED_ATTR_INFO ) {

        @Override
        protected void doInit() {
          setNameAndDescription( ATTR.nmName(), ATTR.description() );
          params().setStr( TSID_ICON_ID, ICONID_SDED_CLASS_ATTR );
          setFlags( 0 );
        }

        protected IList<IDtoAttrInfo> doGetFieldValue( IDtoClassInfo aEntity ) {
          return aEntity.attrInfos();
        }

      };

  /**
   * Field contains of list rtDatas {@link IDtoClassInfo#rtdataInfos()}.
   */
  public final IM5MultiModownFieldDef<IDtoClassInfo, IDtoRtdataInfo> SELF_RTDATA_INFOS =
      new M5MultiModownFieldDef<>( FID_SELF_RTDATA_INFOS, MID_SDED_RTDATA_INFO ) {

        @Override
        protected void doInit() {
          setNameAndDescription( RTDATA.nmName(), RTDATA.description() );
          params().setStr( TSID_ICON_ID, ICONID_SDED_CLASS_DATA );
          setFlags( 0 );
        }

        protected IList<IDtoRtdataInfo> doGetFieldValue( IDtoClassInfo aEntity ) {
          return aEntity.rtdataInfos();
        }

      };

  /**
   * Field contains of list linkInfos {@link IDtoClassInfo#linkInfos()}.
   */
  public final IM5MultiModownFieldDef<IDtoClassInfo, IDtoLinkInfo> SELF_LINK_INFOS =
      new M5MultiModownFieldDef<>( FID_SELF_LINK_INFOS, MID_SDED_LINK_INFO ) {

        @Override
        protected void doInit() {
          setNameAndDescription( LINK.nmName(), LINK.description() );
          params().setStr( TSID_ICON_ID, ICONID_SDED_CLASS_LINK );
          setFlags( 0 );
        }

        protected IList<IDtoLinkInfo> doGetFieldValue( IDtoClassInfo aEntity ) {
          return aEntity.linkInfos();
        }

      };

  /**
   * Field contains of list cmdInfos {@link IDtoClassInfo#cmdInfos()}.
   */
  public final IM5MultiModownFieldDef<IDtoClassInfo, IDtoCmdInfo> SELF_CMD_INFOS =
      new M5MultiModownFieldDef<>( FID_SELF_CMD_INFOS, MID_SDED_CMD_INFO ) {

        @Override
        protected void doInit() {
          setNameAndDescription( CMD.nmName(), CMD.description() );
          params().setStr( TSID_ICON_ID, ICONID_SDED_CLASS_CMD );
          setFlags( 0 );
        }

        protected IList<IDtoCmdInfo> doGetFieldValue( IDtoClassInfo aEntity ) {
          return aEntity.cmdInfos();
        }

      };

  /**
   * Field contains of list eventInfos {@link IDtoClassInfo#eventInfos()}.
   */
  public final IM5MultiModownFieldDef<IDtoClassInfo, IDtoEventInfo> SELF_EVENT_INFOS =
      new M5MultiModownFieldDef<>( FID_SELF_EVENT_INFOS, MID_SDED_EVENT_INFO ) {

        @Override
        protected void doInit() {
          setNameAndDescription( EVENT.nmName(), EVENT.description() );
          params().setStr( TSID_ICON_ID, ICONID_SDED_CLASS_EVENT );
          setFlags( 0 );
        }

        protected IList<IDtoEventInfo> doGetFieldValue( IDtoClassInfo aEntity ) {
          return aEntity.eventInfos();
        }

      };

  /**
   * Field contains of list rivetInfos {@link IDtoClassInfo#rivetInfos()}.
   */
  public final IM5MultiModownFieldDef<IDtoClassInfo, IDtoRivetInfo> SELF_RIVET_INFOS =
      new M5MultiModownFieldDef<>( FID_SELF_RIVET_INFOS, MID_SDED_RIVET_INFO ) {

        @Override
        protected void doInit() {
          setNameAndDescription( RIVET.nmName(), RIVET.description() );
          params().setStr( TSID_ICON_ID, ICONID_SDED_CLASS_RIVET );
          setFlags( 0 );
        }

        protected IList<IDtoRivetInfo> doGetFieldValue( IDtoClassInfo aEntity ) {
          return aEntity.rivetInfos();
        }

      };

  /**
   * Field contains of list clobInfos {@link IDtoClassInfo#clobInfos()}.
   */
  public final IM5MultiModownFieldDef<IDtoClassInfo, IDtoClobInfo> SELF_CLOB_INFOS =
      new M5MultiModownFieldDef<>( FID_SELF_CLOB_INFOS, MID_SDED_CLOB_INFO ) {

        @Override
        protected void doInit() {
          setNameAndDescription( CLOB.nmName(), CLOB.description() );
          params().setStr( TSID_ICON_ID, ICONID_SDED_CLASS_CLOB );
          setFlags( 0 );
        }

        protected IList<IDtoClobInfo> doGetFieldValue( IDtoClassInfo aEntity ) {
          return aEntity.clobInfos();
        }

      };

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SdedDtoClassInfoM5Model( ISkConnection aConn ) {
    super( MID_SDED_DTO_CLASS_INFO, IDtoClassInfo.class, aConn );
    setNameAndDescription( STR_N_M5M_CLASS, STR_D_M5M_CLASS );
    addFieldDefs( CLASS_ID, NAME, PARENT_ID, DESCRIPTION, //
        SELF_ATTR_INFOS, //
        SELF_RIVET_INFOS, //
        SELF_LINK_INFOS, //
        SELF_RTDATA_INFOS, //
        SELF_CMD_INFOS, //
        SELF_EVENT_INFOS, //
        SELF_CLOB_INFOS //
    );
    setPanelCreator( new SdedDtoClassInfoM5PanelCreator() );
  }

  // ------------------------------------------------------------------------------------
  // M5Model
  //

  @Override
  protected IM5LifecycleManager<IDtoClassInfo> doCreateDefaultLifecycleManager() {
    return new SdedDtoClassInfoM5LifecycleManager( this, skConn() );
  }

  @Override
  protected IM5LifecycleManager<IDtoClassInfo> doCreateLifecycleManager( Object aMaster ) {
    return new SdedDtoClassInfoM5LifecycleManager( this, ISkConnection.class.cast( aMaster ) );
  }

}
