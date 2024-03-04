package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tsgui.bricks.actions.ITsStdActionDefs.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tsgui.bricks.actions.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.dialogs.datarec.*;
import org.toxsoft.core.tsgui.graphics.icons.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tsgui.panels.toolbar.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * {@link MultiPaneComponentModown} implementation for {@link SdedDtoRtDataInfoM5Mpc}.
 *
 * @author dima
 */
class SdedDtoRtDataInfoM5Mpc
    extends MultiPaneComponentModown<IDtoRtdataInfo> {

  public SdedDtoRtDataInfoM5Mpc( ITsGuiContext aContext, IM5Model<IDtoRtdataInfo> aModel,
      IM5ItemsProvider<IDtoRtdataInfo> aItemsProvider, IM5LifecycleManager<IDtoRtdataInfo> aLifecycleManager ) {
    super( aContext, aModel, aItemsProvider, aLifecycleManager );
  }

  @Override
  protected ITsToolbar doCreateToolbar( ITsGuiContext aContext, String aName, EIconSize aIconSize,
      IListEdit<ITsActionDef> aActs ) {
    int index = 1 + aActs.indexOf( ACDEF_ADD );
    aActs.insert( index, ACDEF_ADD_COPY );
    return super.doCreateToolbar( aContext, aName, aIconSize, aActs );
  }

  @Override
  protected void doProcessAction( String aActionId ) {
    IDtoRtdataInfo sel = selectedItem();
    switch( aActionId ) {
      case ACTID_ADD_COPY: {
        if( sel == null ) {
          break;
        }
        ITsDialogInfo cdi = doCreateDialogInfoToAddItem();
        IM5BunchEdit<IDtoRtdataInfo> initVals = new M5BunchEdit<>( model() );
        initVals.fillFrom( sel, false );
        String itemId = initVals.getAsAv( IAvMetaConstants.TSID_ID ).asString();
        itemId = itemId + "_copy"; //$NON-NLS-1$
        initVals.set( IAvMetaConstants.TSID_ID, avStr( itemId ) );
        IDtoRtdataInfo item = M5GuiUtils.askCreate( tsContext(), model(), initVals, cdi, lifecycleManager() );
        if( item != null ) {
          fillViewer( item );
        }
        break;
      }
      default:
        throw new TsNotAllEnumsUsedRtException( aActionId );
    }
  }

  @Override
  protected void doUpdateActionsState( boolean aIsAlive, boolean aIsSel, IDtoRtdataInfo aSel ) {
    toolbar().setActionEnabled( ACTID_ADD_COPY, aIsSel );
  }

}
