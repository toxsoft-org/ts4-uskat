package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tsgui.bricks.actions.ITsStdActionDefs.*;
import static org.toxsoft.core.tslib.gw.IGwHardConstants.*;
import static org.toxsoft.uskat.core.gui.ISkCoreGuiConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded.IKM5SdedConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded.ISkSdedKm5SharedResources.*;

import org.toxsoft.core.tsgui.bricks.actions.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.tsnodes.*;
import org.toxsoft.core.tsgui.bricks.tstree.tmm.*;
import org.toxsoft.core.tsgui.graphics.icons.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.*;
import org.toxsoft.core.tsgui.m5.gui.mpc.impl.*;
import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tsgui.panels.toolbar.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * {@link IMultiPaneComponent} implementation for {@link ISkClassInfo} collection editor.
 *
 * @author hazard157
 */
class SdedSkClassInfoMpc
    extends MultiPaneComponentModown<ISkClassInfo>
    implements ISkConnected {

  public static final String TMIID_BY_HIERARCHY = "ByHierarchy"; //$NON-NLS-1$

  public static final ITsNodeKind<ISkClassInfo> NK_CLASS = new TsNodeKind<>( "SkClass", //$NON-NLS-1$
      ISkClassInfo.class, true, ICONID_SDED_CLASS );

  /**
   * Groups classes by the inheritance hierarchy.
   *
   * @author hazard157
   */
  class TreeMakerByHierarchy
      implements ITsTreeMaker<ISkClassInfo> {

    private DefaultTsNode<ISkClassInfo> getParentNode( ISkClassInfo aCinf,
        IStringMapEdit<DefaultTsNode<ISkClassInfo>> aNodesMap, IStridablesList<ISkClassInfo> aAllClasses ) {
      DefaultTsNode<ISkClassInfo> parentNode = aNodesMap.findByKey( aCinf.parentId() );
      if( parentNode != null ) {
        return parentNode;
      }
      ISkClassInfo parentClass = aAllClasses.getByKey( aCinf.id() );
      DefaultTsNode<ISkClassInfo> grandpaNode = getParentNode( parentClass, aNodesMap, aAllClasses );
      parentNode = new DefaultTsNode<>( NK_CLASS, grandpaNode, parentClass );
      aNodesMap.put( parentClass.id(), parentNode );
      grandpaNode.addNode( parentNode );
      return parentNode;
    }

    @Override
    public IList<ITsNode> makeRoots( ITsNode aRoot, IList<ISkClassInfo> aItems ) {
      /**
       * Note on implementation: argument aItems may contain filtered items where parents of the classes to be displayed
       * are NOT in aItems. So implementation will create grouping nodes based on class infos found in ALL classes list.
       */
      IStridablesList<ISkClassInfo> allClasses = new StridablesList<>( tree().items() );
      if( allClasses.isEmpty() ) {
        return IList.EMPTY;
      }
      IStringMapEdit<DefaultTsNode<ISkClassInfo>> nodesMap = new StringMap<>();
      nodesMap.put( GW_ROOT_CLASS_ID, new DefaultTsNode<>( NK_CLASS, aRoot, allClasses.getByKey( GW_ROOT_CLASS_ID ) ) );
      for( ISkClassInfo cinf : aItems ) {
        if( cinf.id().equals( IGwHardConstants.GW_ROOT_CLASS_ID ) ) {
          continue;
        }
        DefaultTsNode<ISkClassInfo> parentNode = getParentNode( cinf, nodesMap, allClasses );
        DefaultTsNode<ISkClassInfo> classNode = new DefaultTsNode<>( NK_CLASS, parentNode, cinf );
        nodesMap.put( cinf.id(), classNode );
        parentNode.addNode( classNode );
      }
      return new SingleItemList<>( nodesMap.getByKey( GW_ROOT_CLASS_ID ) );
    }

    @Override
    public boolean isItemNode( ITsNode aNode ) {
      return aNode.kind() == NK_CLASS;
    }

  }

  /**
   * Constructor - creates instance to edit entities.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aModel {@link IM5Model} - the model
   * @param aItemsProvider {@link IM5ItemsProvider} - the items provider or <code>null</code>
   * @param aLifecycleManager {@link IM5LifecycleManager} - the lifecycle manager or <code>null</code>
   */
  SdedSkClassInfoMpc( ITsGuiContext aContext, IM5Model<ISkClassInfo> aModel,
      IM5ItemsProvider<ISkClassInfo> aItemsProvider, IM5LifecycleManager<ISkClassInfo> aLifecycleManager ) {
    super( aContext, aModel, aItemsProvider, aLifecycleManager );
    TreeModeInfo<ISkClassInfo> tmiByHierarchy = new TreeModeInfo<>( TMIID_BY_HIERARCHY, //
        STR_N_TMI_BY_HIERARCHY, STR_D_TMI_BY_HIERARCHY, null, new TreeMakerByHierarchy() );
    treeModeManager().addTreeMode( tmiByHierarchy );
  }

  // ------------------------------------------------------------------------------------
  // MultiPaneComponentModown
  //

  @Override
  protected ITsToolbar doCreateToolbar( ITsGuiContext aContext, String aName, EIconSize aIconSize,
      IListEdit<ITsActionDef> aActs ) {
    aActs.add( ACDEF_SEPARATOR );
    aActs.add( ACDEF_HIDE_CLAIMED_CLASSES );
    return super.doCreateToolbar( aContext, aName, aIconSize, aActs );
  }

  @Override
  protected void doProcessAction( String aActionId ) {
    switch( aActionId ) {
      case ACTID_HIDE_CLAIMED_CLASSES: {
        refresh();
        break;
      }
      default:
        throw new TsNotAllEnumsUsedRtException( aActionId );
    }
  }

  @Override
  protected void doAfterCreateControls() {
    toolbar().setActionChecked( ACTID_HIDE_CLAIMED_CLASSES, true );
    treeModeManager().setCurrentMode( TMIID_BY_HIERARCHY );
  }

  @Override
  protected void doUpdateActionsState( boolean aIsAlive, boolean aIsSel, ISkClassInfo aSel ) {
    // FIXME dima 10.10.23 здесь где-то ошибка которая плавает, не могу воспроизвести
    // can NOT edit: 1) root class, 2) claimed by service
    boolean canEdit = false;
    if( aSel != null ) {
      if( !aSel.id().equals( IGwHardConstants.GW_ROOT_CLASS_ID ) ) {
        String claiminServiceId = skSysdescr().determineClassClaimingServiceId( aSel.id() );
        if( claiminServiceId.equals( ISkSysdescr.SERVICE_ID ) ) {
          canEdit = true;
        }
      }
    }
    toolbar().setActionEnabled( ACTID_EDIT, canEdit );
  }

  @Override
  protected void doAdjustEntityCreationInitialValues( IM5BunchEdit<ISkClassInfo> aValues ) {
    ISkClassInfo sel = tree().selectedItem();
    String parentId = IGwHardConstants.GW_ROOT_CLASS_ID;
    if( sel != null ) {
      parentId = sel.id();
    }
    aValues.set( FID_PARENT_ID, parentId );
  }

  @Override
  protected void doFillTree() {
    IStridablesList<ISkClassInfo> allItems = new StridablesList<>( itemsProvider().listItems() );
    if( toolbar().isActionChecked( ACTID_HIDE_CLAIMED_CLASSES ) ) {
      IStridablesListEdit<ISkClassInfo> visibleItems = new StridablesList<>();
      // add SYSDESCR owned classes with all parents
      for( ISkClassInfo cinf : allItems ) {
        String claimingServiceId = skSysdescr().determineClassClaimingServiceId( cinf.id() );
        if( claimingServiceId.equals( ISkSysdescr.SERVICE_ID ) ) {
          if( !visibleItems.hasKey( cinf.id() ) ) {
            visibleItems.add( cinf );
            String parentId = cinf.parentId();
            while( !parentId.isEmpty() ) {
              ISkClassInfo pinf = allItems.getByKey( parentId );
              if( !visibleItems.hasKey( pinf.id() ) ) {
                visibleItems.add( pinf );
              }
              parentId = pinf.parentId();
            }
          }
        }
      }
      tree().items().setAll( visibleItems );
    }
    else {
      tree().items().setAll( allItems );
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    return model().domain().tsContext().get( ISkConnection.class );
  }

}
