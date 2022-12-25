package org.toxsoft.uskat.onews.gui.km5;

import static org.toxsoft.uskat.onews.gui.km5.ISkResources.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.tsnodes.*;
import org.toxsoft.core.tsgui.bricks.tstree.tmm.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;
import org.toxsoft.uskat.onews.lib.*;

/**
 * Collection multi pane component for {@link OneWsAbilityM5Model}.
 *
 * @author hazard157
 */
class OneWsAbilityMpc
    extends MultiPaneComponentModown<IOneWsAbility>
    implements ISkConnected {

  static final ITsNodeKind<IOneWsAbility> NK_ABILITY = new TsNodeKind<>( "ability", IOneWsAbility.class, false ); //$NON-NLS-1$
  static final ITsNodeKind<String>        NK_KIND    = new TsNodeKind<>( "kind", String.class, true );            //$NON-NLS-1$

  /**
   * Groups abilities by kind ID.
   *
   * @author hazard157
   */
  class AbilityTreeMakerByKinds
      implements ITsTreeMaker<IOneWsAbility> {

    private IStringMapEdit<DefaultTsNode<String>> makeAbilityKindsMap( ITsNode aRootNode,
        IList<IOneWsAbility> aItems ) {
      IStringMapEdit<DefaultTsNode<String>> map = new StringMap<>();
      // add known ability kinds
      for( IStridable k : ows().listKnownAbilityKinds() ) {
        DefaultTsNode<String> node = new DefaultTsNode<>( NK_KIND, aRootNode, k.id() );
        node.setName( k.nmName() );
        map.put( k.id(), node );
      }
      // add unknown ability kind IDs gathered from abilities list
      for( IOneWsAbility a : aItems ) {
        if( !map.hasKey( a.kindId() ) ) {
          DefaultTsNode<String> node = new DefaultTsNode<>( NK_KIND, aRootNode, a.kindId() );
          map.put( a.kindId(), node );
        }
      }
      return map;
    }

    @SuppressWarnings( { "rawtypes", "unchecked" } )
    @Override
    public IList<ITsNode> makeRoots( ITsNode aRootNode, IList<IOneWsAbility> aItems ) {
      IStringMapEdit<DefaultTsNode<String>> roots = makeAbilityKindsMap( aRootNode, aItems );
      for( IOneWsAbility a : aItems ) {
        DefaultTsNode<String> rootNode = roots.findByKey( a.kindId() );
        DefaultTsNode<IOneWsAbility> n = new DefaultTsNode<>( NK_ABILITY, rootNode, a );
        rootNode.addNode( n );
      }
      return (IList)roots.values();
    }

    @Override
    public boolean isItemNode( ITsNode aNode ) {
      return aNode.kind() == NK_ABILITY;
    }

  }

  private final ISkConnection skConn;

  public OneWsAbilityMpc( ISkConnection aConn, ITsGuiContext aContext, IM5Model<IOneWsAbility> aModel,
      IM5ItemsProvider<IOneWsAbility> aItemsProvider ) {
    this( aConn, aContext, aModel, aItemsProvider, null );
  }

  public OneWsAbilityMpc( ISkConnection aConn, ITsGuiContext aContext, IM5Model<IOneWsAbility> aModel,
      IM5ItemsProvider<IOneWsAbility> aItemsProvider, IM5LifecycleManager<IOneWsAbility> aLifecycleManager ) {
    super( aContext, aModel, aItemsProvider, aLifecycleManager );
    skConn = TsNullArgumentRtException.checkNull( aConn );
    TreeModeInfo<IOneWsAbility> byKind = new TreeModeInfo<>( "byKind", //$NON-NLS-1$
        STR_N_TMI_ABILITIES_BY_KIND, STR_D_TMI_ABILITIES_BY_KIND, null, new AbilityTreeMakerByKinds() );
    treeModeManager().addTreeMode( byKind );
    treeModeManager().setCurrentMode( byKind.id() );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  ISkOneWsService ows() {
    return skConn.coreApi().getService( ISkOneWsService.SERVICE_ID );
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    return skConn;
  }

}
