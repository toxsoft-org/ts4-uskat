package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tsgui.m5.gui.mpc.IMultiPaneComponentConstants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.api.sysdescr.ESkClassPropKind.*;
import static org.toxsoft.uskat.core.gui.km5.sded.IKM5SdedConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded.ISkSdedKm5SharedResources.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.gui.panels.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.models.av.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.geometry.impl.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * M5-model of the {@link IDtoCmdInfo}.
 *
 * @author dima
 */
public class SdedDtoCmdInfoM5Model
    extends SdedDtoPropInfoM5ModelBase<IDtoCmdInfo> {

  /**
   * Field {@link IDtoCmdInfo#argDefs()}.
   */
  public final IM5MultiModownFieldDef<IDtoCmdInfo, IDataDef> ARG_DEFS =
      new M5MultiModownFieldDef<>( FID_ARG_DEFS, DataDefM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_N_ARG_DEFS, STR_D_ARG_DEFS );
          setFlags( M5FF_DETAIL );
          params().setInt( IValedControlConstants.OPDEF_VERTICAL_SPAN, 5 );
        }

        protected IStridablesList<IDataDef> doGetFieldValue( IDtoCmdInfo aEntity ) {
          return aEntity.argDefs();
        }

      };

  /**
   * LM for this model.
   *
   * @author dima
   */
  class LifecycleManager
      extends PropLifecycleManagerBase {

    public LifecycleManager( IM5Model<IDtoCmdInfo> aModel ) {
      super( aModel );
    }

    private IDtoCmdInfo makeCmdInfo( IM5Bunch<IDtoCmdInfo> aValues ) {
      String id = aValues.getAsAv( FID_ID ).asString();

      StridablesList<IDataDef> dataDefs = new StridablesList<>( ARG_DEFS.getFieldValue( aValues ) );

      IOptionSetEdit params = new OptionSet();
      if( aValues.originalEntity() != null ) {
        params.setAll( aValues.originalEntity().params() );
      }
      params.setStr( FID_NAME, aValues.getAsAv( FID_NAME ).asString() );
      params.setStr( FID_DESCRIPTION, aValues.getAsAv( FID_DESCRIPTION ).asString() );
      IDtoCmdInfo inf = DtoCmdInfo.create1( id, dataDefs, params );
      return inf;
    }

    @Override
    protected IDtoCmdInfo doCreate( IM5Bunch<IDtoCmdInfo> aValues ) {
      return makeCmdInfo( aValues );
    }

    @Override
    protected IDtoCmdInfo doEdit( IM5Bunch<IDtoCmdInfo> aValues ) {
      return makeCmdInfo( aValues );
    }

    @Override
    protected void doRemove( IDtoCmdInfo aEntity ) {
      // nop
    }

  }

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SdedDtoCmdInfoM5Model( ISkConnection aConn ) {
    super( MID_SDED_CMD_INFO, IDtoCmdInfo.class, aConn );
    setNameAndDescription( CMD.nmName(), CMD.description() );
    addFieldDefs( ARG_DEFS );
    setPanelCreator( new M5DefaultPanelCreator<>() {

      protected IM5CollectionPanel<IDtoCmdInfo> doCreateCollEditPanel( ITsGuiContext aContext,
          IM5ItemsProvider<IDtoCmdInfo> aItemsProvider, IM5LifecycleManager<IDtoCmdInfo> aLifecycleManager ) {
        OPDEF_IS_ACTIONS_CRUD.setValue( aContext.params(), AV_TRUE );
        MultiPaneComponentModown<IDtoCmdInfo> mpc =
            new MultiPaneComponentModown<>( aContext, model(), aItemsProvider, aLifecycleManager ) {

              protected ITsDialogInfo doCreateDialogInfoToEditItem( IDtoCmdInfo aItem ) {
                TsDialogInfo retVal =
                    new TsDialogInfo( aContext, getShell(), STR_EDIT_CMD_DLG_CAPTION, STR_EDIT_CMD_DLG_TITLE, 0 );
                retVal.setMinSize( new TsPoint( -30, -60 ) );
                return retVal;
              }

              protected ITsDialogInfo doCreateDialogInfoToAddItem() {
                TsDialogInfo retVal =
                    new TsDialogInfo( aContext, getShell(), STR_ADD_CMD_DLG_CAPTION, STR_ADD_CMD_DLG_TITLE, 0 );
                retVal.setMinSize( new TsPoint( -30, -60 ) );

                return retVal;
              }
            };
        return new M5CollectionPanelMpcModownWrapper<>( mpc, false );
      }

    } );

  }

  @Override
  protected IM5LifecycleManager<IDtoCmdInfo> doCreateDefaultLifecycleManager() {
    return new LifecycleManager( this );
  }

  @Override
  protected IM5LifecycleManager<IDtoCmdInfo> doCreateLifecycleManager( Object aMaster ) {
    return getLifecycleManager( null );
  }

}
