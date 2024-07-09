package org.toxsoft.uskat.core.gui.km5.sded.sded;

import static org.toxsoft.core.tsgui.bricks.actions.ITsStdActionDefs.*;
import static org.toxsoft.core.tsgui.graphics.icons.ITsStdIconIds.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.gui.ISkCoreGuiConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded.IKM5SdedConstants.*;
import static org.toxsoft.uskat.core.gui.km5.sded.ISkSdedKm5SharedResources.*;

import org.eclipse.swt.widgets.*;
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
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.sded.sded.opc_ua_server.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * {@link IMultiPaneComponent} implementation for {@link ISkClassInfo} collection editor.
 *
 * @author hazard157
 */
class SdedSkClassInfoMpc
    extends MultiPaneComponentModown<ISkClassInfo>
    implements ISkConnected {

  // ------------------------------------------------------------------------------------
  // dima 08.07.24
  // action for debug

  /**
   * ID of action {@link #ACDEF_PUBLIC_2_OPC_UA}.
   */
  final String ACTID_PUBLIC_2_OPC_UA = SDED_ID + ".Public2OPC_UA"; //$NON-NLS-1$

  /**
   * Public selected class throught the OPC UA protocol.
   */
  TsActionDef ACDEF_PUBLIC_2_OPC_UA = TsActionDef.ofPush1( ACTID_PUBLIC_2_OPC_UA, //
      TSID_NAME, "Public throught the OPC UA", //
      TSID_DESCRIPTION, "Public throught the OPC UA", //
      TSID_ICON_ID, ICONID_COLORS //
  );

  public static final String TMIID_BY_HIERARCHY = "ByHierarchy"; //$NON-NLS-1$

  public static final ITsNodeKind<ISkClassInfo> NK_CLASS = new TsNodeKind<>( "SkClass", //$NON-NLS-1$
      ISkClassInfo.class, true, ICONID_SDED_CLASS );

  static class TreeMakerByHierarchy
      implements ITsTreeMaker<ISkClassInfo> {

    private DefaultTsNode<ISkClassInfo> getParentNode( ISkClassInfo aCinf,
        IStringMapEdit<DefaultTsNode<ISkClassInfo>> aAllMap, IStridablesList<ISkClassInfo> aAllItems ) {
      DefaultTsNode<ISkClassInfo> parentNode = aAllMap.findByKey( aCinf.parentId() );
      if( parentNode != null ) {
        return parentNode;
      }
      ISkClassInfo parentClass = aAllItems.getByKey( aCinf.id() );
      DefaultTsNode<ISkClassInfo> grandpaNode = getParentNode( parentClass, aAllMap, aAllItems );
      parentNode = new DefaultTsNode<>( NK_CLASS, grandpaNode, parentClass );
      aAllMap.put( parentClass.id(), parentNode );
      grandpaNode.addNode( parentNode );
      return parentNode;
    }

    @Override
    public IList<ITsNode> makeRoots( ITsNode aRootNode, IList<ISkClassInfo> aItems ) {
      IStridablesList<ISkClassInfo> allItems = new StridablesList<>( aItems );
      DefaultTsNode<ISkClassInfo> skRoot =
          new DefaultTsNode<>( NK_CLASS, aRootNode, allItems.getByKey( IGwHardConstants.GW_ROOT_CLASS_ID ) );
      IStringMapEdit<DefaultTsNode<ISkClassInfo>> allMap = new StringMap<>();
      allMap.put( skRoot.entity().id(), skRoot );
      for( ISkClassInfo cinf : allItems ) {
        if( cinf.id().equals( IGwHardConstants.GW_ROOT_CLASS_ID ) ) {
          continue;
        }
        DefaultTsNode<ISkClassInfo> parentNode = getParentNode( cinf, allMap, allItems );
        DefaultTsNode<ISkClassInfo> classNode = new DefaultTsNode<>( NK_CLASS, parentNode, cinf );
        allMap.put( cinf.id(), classNode );
        parentNode.addNode( classNode );
      }
      return new SingleItemList<>( skRoot );
    }

    @Override
    public boolean isItemNode( ITsNode aNode ) {
      return aNode.kind() == NK_CLASS;
    }

  }

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
    aActs.add( ACDEF_SEPARATOR );
    aActs.add( ACDEF_PUBLIC_2_OPC_UA );
    return super.doCreateToolbar( aContext, aName, aIconSize, aActs );
  }

  @Override
  protected void doProcessAction( String aActionId ) {
    switch( aActionId ) {
      case ACTID_HIDE_CLAIMED_CLASSES: {
        refresh();
        break;
      }
      case ACTID_PUBLIC_2_OPC_UA: {
        publicSelectedClass();
        break;
      }
      default:
        throw new TsNotAllEnumsUsedRtException( aActionId );
    }
  }

  private void publicSelectedClass() {
    ISkClassInfo sel = tree().selectedItem();
    Display display = tsContext().get( Display.class );
    display.asyncExec( () -> {
      try {
        ExampleServer server = new ExampleServer( skConn(), sel, display );
        server.startup().get();

        // final CompletableFuture<Void> future = new CompletableFuture<>();
        //
        // Runtime.getRuntime().addShutdownHook( new Thread( () -> future.complete( null ) ) );
        //
        // future.get();
      }
      catch( Exception ex ) {
        // TODO Auto-generated catch block
        LoggerUtils.errorLogger().error( ex );
      }
    } );
    LoggerUtils.errorLogger().debug( "OPC UA server started!" ); //$NON-NLS-1$

  }

  @Override
  protected void doAfterCreateControls() {
    toolbar().setActionChecked( ACTID_HIDE_CLAIMED_CLASSES, false );
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
