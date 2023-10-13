package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind.*;
import static org.toxsoft.uskat.core.gui.ISkCoreGuiConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded.IKM5SdedConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded.ISkSdedKm5SharedResources.*;

import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.models.misc.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
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
          // тут выделяем только те которые принадлежат непосредственно этому классу
          ISkClassInfo clsInfo = skSysdescr().findClassInfo( aEntity.id() );
          ISkClassProps<IDtoAttrInfo> attrs = clsInfo.attrs();
          IStridablesList<IDtoAttrInfo> listSelf = attrs.listSelf();
          IStridablesList<IDtoAttrInfo> listAll = attrs.list();
          // теперь оставляем только не свои
          StridablesList<IDtoAttrInfo> haired = new StridablesList<>();
          for( IDtoAttrInfo attrInfo : listAll ) {
            if( !listSelf.hasKey( attrInfo.id() ) ) {
              haired.add( attrInfo );
            }
          }
          // теперь оставляем только те которые свои и вновь добавленные
          IListEdit<IDtoAttrInfo> retVal = new ElemArrayList<>();
          for( IDtoAttrInfo entityAttrInfo : aEntity.attrInfos() ) {
            if( !haired.hasKey( entityAttrInfo.id() ) ) {
              retVal.add( entityAttrInfo );
            }
          }
          return retVal;
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
          // тут выделяем только те которые принадлежат непосредственно этому классу
          ISkClassInfo clsInfo = skSysdescr().findClassInfo( aEntity.id() );
          ISkClassProps<IDtoRtdataInfo> rtdata = clsInfo.rtdata();
          IStridablesList<IDtoRtdataInfo> listSelf = rtdata.listSelf();
          IStridablesList<IDtoRtdataInfo> listAll = rtdata.list();
          // теперь оставляем только не свои
          StridablesList<IDtoRtdataInfo> haired = new StridablesList<>();
          for( IDtoRtdataInfo rtDataInfo : listAll ) {
            if( !listSelf.hasKey( rtDataInfo.id() ) ) {
              haired.add( rtDataInfo );
            }
          }
          // теперь оставляем только те которые свои и вновь добавленные
          IListEdit<IDtoRtdataInfo> retVal = new ElemArrayList<>();
          for( IDtoRtdataInfo entityRtDataInfo : aEntity.rtdataInfos() ) {
            if( !haired.hasKey( entityRtDataInfo.id() ) ) {
              retVal.add( entityRtDataInfo );
            }
          }
          return retVal;
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
          // тут выделяем только те которые принадлежат непосредственно этому классу
          ISkClassInfo clsInfo = skSysdescr().findClassInfo( aEntity.id() );
          ISkClassProps<IDtoLinkInfo> links = clsInfo.links();
          IStridablesList<IDtoLinkInfo> listSelf = links.listSelf();
          IStridablesList<IDtoLinkInfo> listAll = links.list();
          // теперь оставляем только не свои
          StridablesList<IDtoLinkInfo> haired = new StridablesList<>();
          for( IDtoLinkInfo linkInfo : listAll ) {
            if( !listSelf.hasKey( linkInfo.id() ) ) {
              haired.add( linkInfo );
            }
          }
          // теперь оставляем только те которые свои и вновь добавленные
          IListEdit<IDtoLinkInfo> retVal = new ElemArrayList<>();
          for( IDtoLinkInfo entityLinkInfo : aEntity.linkInfos() ) {
            if( !haired.hasKey( entityLinkInfo.id() ) ) {
              retVal.add( entityLinkInfo );
            }
          }
          return retVal;
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
          // тут выделяем только те которые принадлежат непосредственно этому классу
          ISkClassInfo clsInfo = skSysdescr().findClassInfo( aEntity.id() );
          ISkClassProps<IDtoCmdInfo> cmds = clsInfo.cmds();
          IStridablesList<IDtoCmdInfo> listSelf = cmds.listSelf();
          IStridablesList<IDtoCmdInfo> listAll = cmds.list();
          // теперь оставляем только не свои
          StridablesList<IDtoCmdInfo> haired = new StridablesList<>();
          for( IDtoCmdInfo cmdInfo : listAll ) {
            if( !listSelf.hasKey( cmdInfo.id() ) ) {
              haired.add( cmdInfo );
            }
          }
          // теперь оставляем только те которые свои и вновь добавленные
          IListEdit<IDtoCmdInfo> retVal = new ElemArrayList<>();
          for( IDtoCmdInfo entityCmdInfo : aEntity.cmdInfos() ) {
            if( !haired.hasKey( entityCmdInfo.id() ) ) {
              retVal.add( entityCmdInfo );
            }
          }
          return retVal;
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
          // тут выделяем только те которые принадлежат непосредственно этому классу
          ISkClassInfo clsInfo = skSysdescr().findClassInfo( aEntity.id() );
          ISkClassProps<IDtoEventInfo> events = clsInfo.events();
          IStridablesList<IDtoEventInfo> listSelf = events.listSelf();
          IStridablesList<IDtoEventInfo> listAll = events.list();
          // теперь оставляем только не свои
          StridablesList<IDtoEventInfo> haired = new StridablesList<>();
          for( IDtoEventInfo evInfo : listAll ) {
            if( !listSelf.hasKey( evInfo.id() ) ) {
              haired.add( evInfo );
            }
          }
          // теперь оставляем только те которые свои и вновь добавленные
          IListEdit<IDtoEventInfo> retVal = new ElemArrayList<>();
          for( IDtoEventInfo entityEvInfo : aEntity.eventInfos() ) {
            if( !haired.hasKey( entityEvInfo.id() ) ) {
              retVal.add( entityEvInfo );
            }
          }
          return retVal;
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
          // тут выделяем только те которые принадлежат непосредственно этому классу
          ISkClassInfo clsInfo = skSysdescr().findClassInfo( aEntity.id() );
          ISkClassProps<IDtoRivetInfo> rivets = clsInfo.rivets();
          IStridablesList<IDtoRivetInfo> listSelf = rivets.listSelf();
          IStridablesList<IDtoRivetInfo> listAll = rivets.list();
          // теперь оставляем только не свои
          StridablesList<IDtoRivetInfo> haired = new StridablesList<>();
          for( IDtoRivetInfo rivetInfo : listAll ) {
            if( !listSelf.hasKey( rivetInfo.id() ) ) {
              haired.add( rivetInfo );
            }
          }
          // теперь оставляем только те которые свои и вновь добавленные
          IListEdit<IDtoRivetInfo> retVal = new ElemArrayList<>();
          for( IDtoRivetInfo entityRivetInfo : aEntity.rivetInfos() ) {
            if( !haired.hasKey( entityRivetInfo.id() ) ) {
              retVal.add( entityRivetInfo );
            }
          }
          return retVal;
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
          // тут выделяем только те которые принадлежат непосредственно этому классу
          ISkClassInfo clsInfo = skSysdescr().findClassInfo( aEntity.id() );
          ISkClassProps<IDtoClobInfo> clobs = clsInfo.clobs();
          IStridablesList<IDtoClobInfo> listSelf = clobs.listSelf();
          IStridablesList<IDtoClobInfo> listAll = clobs.list();
          // теперь оставляем только не свои
          StridablesList<IDtoClobInfo> haired = new StridablesList<>();
          for( IDtoClobInfo clobInfo : listAll ) {
            if( !listSelf.hasKey( clobInfo.id() ) ) {
              haired.add( clobInfo );
            }
          }
          // теперь оставляем только те которые свои и вновь добавленные
          IListEdit<IDtoClobInfo> retVal = new ElemArrayList<>();
          for( IDtoClobInfo entityClobInfo : aEntity.clobInfos() ) {
            if( !haired.hasKey( entityClobInfo.id() ) ) {
              retVal.add( entityClobInfo );
            }
          }
          return retVal;
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
