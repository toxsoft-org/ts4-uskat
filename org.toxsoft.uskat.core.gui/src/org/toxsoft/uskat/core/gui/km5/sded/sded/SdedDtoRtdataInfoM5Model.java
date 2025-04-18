package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tsgui.m5.gui.mpc.IMultiPaneComponentConstants.*;
import static org.toxsoft.core.tsgui.m5.valeds.IM5ValedConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind.*;
import static org.toxsoft.uskat.core.gui.km5.sded.IKM5SdedConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded.ISkSdedKm5SharedResources.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.gui.panels.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.models.av.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * M5-model of the {@link IDtoRtdataInfo}.
 *
 * @author hazard157
 * @author dima
 */
public class SdedDtoRtdataInfoM5Model
    extends SdedDtoPropInfoM5ModelBase<IDtoRtdataInfo> {

  /**
   * Modown field {@link IDtoRtdataInfo#dataType()}.
   */
  public final IM5SingleModownFieldDef<IDtoRtdataInfo, IDataType> DATA_TYPE =
      new M5SingleModownFieldDef<>( FID_DATA_TYPE, DataTypeM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_N_DATA_TYPE, STR_D_DATA_TYPE );
          params().setBool( TSID_IS_NULL_ALLOWED, false );
          params().setStr( M5_VALED_OPDEF_WIDGET_TYPE_ID, M5VWTID_INPLACE );
          params().setInt( IValedControlConstants.OPDEF_VERTICAL_SPAN, 10 );
          setFlags( M5FF_DETAIL );
        }

        protected IDataType doGetFieldValue( IDtoRtdataInfo aEntity ) {
          return aEntity.dataType();
        }

      };

  /**
   * M5-attribute {@link IDtoRtdataInfo#isSync()}.
   */
  public final IM5AttributeFieldDef<IDtoRtdataInfo> IS_CURR = new M5AttributeFieldDef<>( FID_IS_CURR, DDEF_BOOLEAN ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_IS_CURR, STR_D_IS_CURR );
      setFlags( M5FF_DETAIL );
    }

    @Override
    protected IAtomicValue doGetFieldValue( IDtoRtdataInfo aEntity ) {
      return avBool( aEntity.isCurr() );
    }

  };

  /**
   * M5-attribute {@link IDtoRtdataInfo#isHist()}.
   */
  public final IM5AttributeFieldDef<IDtoRtdataInfo> IS_HIST = new M5AttributeFieldDef<>( FID_IS_HIST, DDEF_BOOLEAN ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_IS_HIST, STR_D_IS_HIST );
      setFlags( M5FF_DETAIL );
    }

    @Override
    protected IAtomicValue doGetFieldValue( IDtoRtdataInfo aEntity ) {
      return avBool( aEntity.isHist() );
    }

  };

  /**
   * M5-attribute {@link IDtoRtdataInfo#isSync()}.
   */
  public final IM5AttributeFieldDef<IDtoRtdataInfo> IS_SYNC = new M5AttributeFieldDef<>( FID_IS_SYNC, DDEF_BOOLEAN ) {

    @Override
    protected void doInit() {
      setNameAndDescription( STR_N_IS_SYNC, STR_D_IS_SYNC );
      setFlags( M5FF_DETAIL );
    }

    @Override
    protected IAtomicValue doGetFieldValue( IDtoRtdataInfo aEntity ) {
      return avBool( aEntity.isSync() );
    }

  };

  /**
   * M5-attribute {@link IDtoRtdataInfo#syncDataDeltaT()}.
   */
  public final IM5AttributeFieldDef<IDtoRtdataInfo> SYNC_DELTA_T =
      new M5AttributeFieldDef<>( FID_SYNC_DELTA_T, DDEF_INTEGER ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_N_SYNC_DELTA_T, STR_D_SYNC_DELTA_T );
          setFlags( M5FF_DETAIL );
          setDefaultValue( AvUtils.AV_1 );
        }

        @Override
        protected IAtomicValue doGetFieldValue( IDtoRtdataInfo aEntity ) {
          return avInt( aEntity.syncDataDeltaT() );
        }

      };

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SdedDtoRtdataInfoM5Model( ISkConnection aConn ) {
    super( MID_SDED_RTDATA_INFO, IDtoRtdataInfo.class, aConn );
    setNameAndDescription( RTDATA.nmName(), RTDATA.description() );
    addFieldDefs( IS_CURR, IS_HIST, IS_SYNC, SYNC_DELTA_T, DATA_TYPE );
    setPanelCreator( new M5DefaultPanelCreator<>() {

      @Override
      protected IM5CollectionPanel<IDtoRtdataInfo> doCreateCollEditPanel( ITsGuiContext aContext,
          IM5ItemsProvider<IDtoRtdataInfo> aItemsProvider, IM5LifecycleManager<IDtoRtdataInfo> aLifecycleManager ) {
        OPDEF_IS_ACTIONS_CRUD.setValue( aContext.params(), AV_TRUE );
        OPDEF_IS_FILTER_PANE.setValue( aContext.params(), AV_TRUE );
        MultiPaneComponentModown<IDtoRtdataInfo> mpc =
            new SdedDtoRtDataInfoM5Mpc( aContext, model(), aItemsProvider, aLifecycleManager );
        return new M5CollectionPanelMpcModownWrapper<>( mpc, false );
      }
    } );

  }

  class LifecycleManager
      extends PropLifecycleManagerBase {

    public LifecycleManager( IM5Model<IDtoRtdataInfo> aModel ) {
      super( aModel );
    }

    private IDtoRtdataInfo makeRtDataInfo( IM5Bunch<IDtoRtdataInfo> aValues ) {
      String id = aValues.getAsAv( FID_ID ).asString();
      IDataType dataType = DATA_TYPE.getFieldValue( aValues );
      IAtomicValue isCurr = IS_CURR.getFieldValue( aValues );
      IAtomicValue isHist = IS_HIST.getFieldValue( aValues );
      IAtomicValue isSync = IS_SYNC.getFieldValue( aValues );
      IAtomicValue syncDeltaT = SYNC_DELTA_T.getFieldValue( aValues );
      IOptionSetEdit params = new OptionSet();
      if( aValues.originalEntity() != null ) {
        params.setAll( aValues.originalEntity().params() );
      }
      params.setStr( FID_NAME, aValues.getAsAv( FID_NAME ).asString() );
      params.setStr( FID_DESCRIPTION, aValues.getAsAv( FID_DESCRIPTION ).asString() );
      IDtoRtdataInfo inf = DtoRtdataInfo.create1( id, dataType, isCurr.asBool(), isHist.asBool(), isSync.asBool(),
          syncDeltaT.asLong(), params );
      return inf;
    }

    @Override
    protected IDtoRtdataInfo doCreate( IM5Bunch<IDtoRtdataInfo> aValues ) {
      return makeRtDataInfo( aValues );
    }

    @Override
    protected IDtoRtdataInfo doEdit( IM5Bunch<IDtoRtdataInfo> aValues ) {
      return makeRtDataInfo( aValues );
    }

    @Override
    protected void doRemove( IDtoRtdataInfo aEntity ) {
      // nop
    }

  }

  @Override
  protected IM5LifecycleManager<IDtoRtdataInfo> doCreateDefaultLifecycleManager() {
    return new LifecycleManager( this );
  }

  @Override
  protected IM5LifecycleManager<IDtoRtdataInfo> doCreateLifecycleManager( Object aMaster ) {
    return getLifecycleManager( null );
  }

}
