package org.toxsoft.uskat.core.gui.km5.sgw;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sgw.ISgwM5Constants.*;
import static org.toxsoft.uskat.core.gui.km5.sgw.ISkResources.*;

import org.toxsoft.core.tsgui.bricks.actions.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.graphics.icons.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.gui.panels.*;
import org.toxsoft.core.tsgui.m5.gui.panels.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.m5.std.models.av.*;
import org.toxsoft.core.tsgui.panels.toolbar.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.filter.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * M5-model of the {@link IDtoRtdataInfo}.
 *
 * @author hazard157
 * @author dima
 */
public class SgwDtoRtdataInfoM5Model
    extends SgwDtoPropInfoM5ModelBase<IDtoRtdataInfo> {

  static final String ACTID_HIDE_ASYNC = SK_ID + ".rtDataInfo.HideAsync"; //$NON-NLS-1$

  static TsActionDef ACDEF_HIDE_ASYNC = TsActionDef.ofCheck2( ACTID_HIDE_ASYNC, STR_N_HIDE_ASYNC,

      STR_D_HIDE_ASYNC, ITsStdIconIds.ICONID_VIEW_FILTER );

  static final ITsFilter<IDtoRtdataInfo> FILTER_SYNC_OUT = IDtoRtdataInfo::isSync;

  /**
   * Modown field {@link IDtoRtdataInfo#dataType()}.
   */
  public final IM5SingleModownFieldDef<IDtoRtdataInfo, IDataType> DATA_TYPE =
      new M5SingleModownFieldDef<>( FID_DATA_TYPE, DataTypeM5Model.MODEL_ID ) {

        @Override
        protected void doInit() {
          setNameAndDescription( STR_N_PROP_DATA_TYPE, STR_D_PROP_DATA_TYPE );
          setFlags( M5FF_COLUMN );
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
      return avBool( aEntity.isSync() );
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
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SgwDtoRtdataInfoM5Model( ISkConnection aConn ) {
    super( MID_SGW_RTDATA_INFO, IDtoRtdataInfo.class, aConn );
    addFieldDefs( DATA_TYPE, IS_CURR, IS_HIST, IS_SYNC );

    setPanelCreator( new M5DefaultPanelCreator<>() {

      @Override
      public IM5CollectionPanel<IDtoRtdataInfo> createCollViewerPanel( ITsGuiContext aInContext,
          IM5ItemsProvider<IDtoRtdataInfo> aItemsProvider ) {

        MultiPaneComponentModown<IDtoRtdataInfo> mpc =
            new MultiPaneComponentModown<>( aInContext, model(), aItemsProvider ) {

              @Override
              protected ITsToolbar doCreateToolbar( ITsGuiContext aContext, String aName, EIconSize aIconSize,
                  IListEdit<ITsActionDef> aActs ) {
                aActs.add( ITsStdActionDefs.ACDEF_SEPARATOR );
                aActs.add( ACDEF_HIDE_ASYNC );

                return super.doCreateToolbar( aContext, aName, aIconSize, aActs );
              }

              @Override
              protected void doProcessAction( String aActionId ) {

                switch( aActionId ) {
                  case ACTID_HIDE_ASYNC: {
                    if( toolbar().isActionChecked( ACTID_HIDE_ASYNC ) ) {
                      tree().filterManager().setFilter( FILTER_SYNC_OUT );
                    }
                    else {
                      tree().filterManager().setFilter( ITsFilter.ALL );
                    }
                    refresh();
                    break;
                  }
                  default:
                    throw new TsNotAllEnumsUsedRtException( aActionId );
                }
              }

            };
        return new M5CollectionPanelMpcModownWrapper<>( mpc, true );
      }
    } );
  }
}
