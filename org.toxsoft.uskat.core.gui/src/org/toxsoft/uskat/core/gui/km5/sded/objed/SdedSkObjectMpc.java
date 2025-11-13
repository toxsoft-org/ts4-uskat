package org.toxsoft.uskat.core.gui.km5.sded.objed;

import static org.toxsoft.core.tsgui.bricks.actions.ITsStdActionDefs.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
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
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * {@link IMultiPaneComponent} implementation for {@link ISkObject} collection editor.
 *
 * @author dima
 */
class SdedSkObjectMpc
    extends MultiPaneComponentModown<ISkObject>
    implements ISkConnected {

  private static final String TMID_GROUP_BY_ALL_CLASSES = "GroupByAllClass"; //$NON-NLS-1$
  static final String         TMID_GROUP_BY_CLASS       = "GroupByClass";    //$NON-NLS-1$

  // private ISkClassInfo selClass = null;
  // private ISkClassInfo selClass = null;

  // /**
  // * @return selected class info {@link ISkClassInfo}
  // */
  // public ISkClassInfo getSelClass() {
  // return selClass;
  // }

  public SdedSkObjectMpc( ITsGuiContext aContext, IM5Model<ISkObject> aModel,
      IM5ItemsProvider<ISkObject> aItemsProvider, IM5LifecycleManager<ISkObject> aLifecycleManager ) {
    super( aContext, aModel, aItemsProvider, aLifecycleManager );
    TreeModeInfo<ISkObject> tmiByHierarchy = new TreeModeInfo<>( TMID_GROUP_BY_CLASS, STR_TMI_BY_HIERARCHY,
        STR_TMI_BY_HIERARCHY_D, null, new TreeMakerByClass( coreApi() ) );
    treeModeManager().addTreeMode( tmiByHierarchy );
    TreeModeInfo<ISkObject> tmiByHierarchy2 = new TreeModeInfo<>( TMID_GROUP_BY_ALL_CLASSES, STR_TMI_BY_WHOLE_HIERARCHY,
        STR_TMI_BY_WHOLE_HIERARCHY_D, null, new TreeMakerByAllClass( coreApi() ) );
    treeModeManager().addTreeMode( tmiByHierarchy2 );
    treeModeManager().setCurrentMode( TMID_GROUP_BY_CLASS );

  }

  // ------------------------------------------------------------------------------------
  // MultiPaneComponentModown
  //

  @Override
  protected ITsToolbar doCreateToolbar( ITsGuiContext aContext, String aName, EIconSize aIconSize,
      IListEdit<ITsActionDef> aActs ) {
    aActs.add( ACDEF_SEPARATOR );
    // aActs.add( ACDEF_HIDE_CLAIMED_CLASSES );
    return super.doCreateToolbar( aContext, aName, aIconSize, aActs );
  }

  @Override
  protected void doProcessAction( String aActionId ) {
    switch( aActionId ) {
      // case ACTID_HIDE_CLAIMED_CLASSES: {
      // refresh();
      // break;
      // }
      default:
        throw new TsNotAllEnumsUsedRtException( aActionId );
    }
  }

  @Override
  protected void doUpdateActionsState( boolean aIsAlive, boolean aIsSel, ISkObject aSel ) {
    // can NOT edit: 1) root class, 2) claimed by service
    boolean canEdit = true;
    boolean canAdd = false;
    // if( aSel != null ) {
    // if( !aSel.id().equals( IGwHardConstants.GW_ROOT_CLASS_ID ) ) {
    // String claiminServiceId = skSysdescr().determineClassClaimingServiceId( aSel.id() );
    // if( claiminServiceId.equals( ISkSysdescr.SERVICE_ID ) ) {
    // canEdit = true;
    // }
    // }
    // }
    ITsNode selNode = (ITsNode)tree().console().selectedNode();
    String selClassId = AbstractTreeModeByClass.getIfClassIdNode( selNode );
    if( selClassId != null ) {
      canAdd = true;
      canEdit = false;
    }
    toolbar().setActionEnabled( ACTID_EDIT, canEdit );
    toolbar().setActionEnabled( ACTID_ADD, canAdd );
  }

  // @Override
  // protected void doAdjustEntityCreationInitialValues( IM5BunchEdit<ISkObjectInfo> aValues ) {
  // ISkObjectInfo sel = tree().selectedItem();
  // String parentId = IGwHardConstants.GW_ROOT_CLASS_ID;
  // if( sel != null ) {
  // parentId = sel.id();
  // }
  // aValues.set( FID_PARENT_ID, avStr( parentId ) );
  // }

  // @Override
  // protected void doFillTree() {
  // IStridablesList<ISkObject> allItems = new StridablesList<>( itemsProvider().listItems() );
  // if( toolbar().isActionChecked( ACTID_HIDE_CLAIMED_CLASSES ) ) {
  // IStridablesListEdit<ISkObjectInfo> visibleItems = new StridablesList<>();
  // // add SYSDESCR owned classes with all parents
  // for( ISkObjectInfo cinf : allItems ) {
  // String claimingServiceId = skSysdescr().determineClassClaimingServiceId( cinf.id() );
  // if( claimingServiceId.equals( ISkSysdescr.SERVICE_ID ) ) {
  // if( !visibleItems.hasKey( cinf.id() ) ) {
  // visibleItems.add( cinf );
  // String parentId = cinf.parentId();
  // while( !parentId.isEmpty() ) {
  // ISkObjectInfo pinf = allItems.getByKey( parentId );
  // if( !visibleItems.hasKey( pinf.id() ) ) {
  // visibleItems.add( pinf );
  // }
  // parentId = pinf.parentId();
  // }
  // }
  // }
  // }
  // tree().items().setAll( visibleItems );
  // }
  // else {
  // tree().items().setAll( allItems );
  // }
  // }

  @Override
  protected void doAdjustEntityCreationInitialValues( IM5BunchEdit<ISkObject> aValues ) {
    ITsNode selNode = (ITsNode)tree().console().selectedNode();
    String selClassId = AbstractTreeModeByClass.getIfClassIdNode( selNode );
    aValues.set( ISkHardConstants.AID_CLASS_ID, avStr( selClassId ) );
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    return model().domain().tsContext().get( ISkConnection.class );
  }

}
