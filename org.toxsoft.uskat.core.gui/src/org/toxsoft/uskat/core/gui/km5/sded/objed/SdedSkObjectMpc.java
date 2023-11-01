package org.toxsoft.uskat.core.gui.km5.sded.objed;

import static org.toxsoft.core.tsgui.bricks.actions.ITsStdActionDefs.*;
import static org.toxsoft.uskat.core.gui.ISkCoreGuiConstants.*;
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
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * {@link IMultiPaneComponent} implementation for {@link ISkObject} collection editor.
 *
 * @author dima
 */
public class SdedSkObjectMpc
    extends MultiPaneComponentModown<ISkObject>
    implements ISkConnected {

  /**
   * Tree node kind for classes.
   */
  public static final ITsNodeKind<ISkClassInfo> NK_CLASS = new TsNodeKind<>( "SkClass", //$NON-NLS-1$
      ISkClassInfo.class, true, ICONID_SDED_CLASSES_LIST );

  /**
   * Tree node kind for objects.
   */
  public static final ITsNodeKind<ISkObject> NK_OBJECT = new TsNodeKind<>( "SkObject", //$NON-NLS-1$
      ISkObject.class, false, ICONID_SDED_CLASS );

  static final String TMID_GROUP_BY_CLASS = "GroupByClass"; //$NON-NLS-1$

  private ISkClassInfo selClass = null;

  /**
   * @return selected class info {@link ISkClassInfo}
   */
  public ISkClassInfo getSelClass() {
    return selClass;
  }

  /**
   * Класс узла дерева, содержащий в качестве пользовательского объекта - описание класса.
   *
   * @author dima
   */
  static class SkClassInfoTsNode
      extends DefaultTsNode<ISkClassInfo> {

    private int hashCode = 0;

    public SkClassInfoTsNode( ITsNode aParent, ISkClassInfo aEntity ) {
      super( NK_CLASS, aParent, aEntity );
    }

    @Override
    protected String doGetName() {
      return entity().id();
    }

    @Override
    public int hashCode() {
      if( hashCode == 0 ) {
        hashCode = 31 * entity().hashCode();
        if( parent() != null ) {
          hashCode += parent().hashCode();
        }
      }
      return hashCode;
    }

    @SuppressWarnings( "rawtypes" )
    @Override
    public boolean equals( Object obj ) {
      if( !(obj instanceof DefaultTsNode node) ) {
        return false;
      }

      if( !entity().equals( node.entity() ) ) {
        return false;
      }

      if( parent() == null ) {
        return node.parent() == null;
      }

      return parent().equals( node.parent() );
    }
  }

  static class TreeMakerByClass
      implements ITsTreeMaker<ISkObject> {

    private final ISkCoreApi coreApi;

    public TreeMakerByClass( ISkCoreApi aCoreApi ) {
      coreApi = aCoreApi;
    }

    private DefaultTsNode<ISkClassInfo> getParentNode( ISkClassInfo aCinf,
        IStringMapEdit<DefaultTsNode<ISkClassInfo>> aAllMap, IStridablesList<ISkClassInfo> aAllItems ) {
      DefaultTsNode<ISkClassInfo> parentNode = aAllMap.findByKey( aCinf.parentId() );
      if( parentNode != null ) {
        return parentNode;
      }
      ISkClassInfo parentClass = aAllItems.getByKey( aCinf.id() );
      DefaultTsNode<ISkClassInfo> grandpaNode = getParentNode( parentClass, aAllMap, aAllItems );
      // parentNode = new DefaultTsNode<>( NK_CLASS, grandpaNode, parentClass );
      parentNode = new SkClassInfoTsNode( grandpaNode, parentClass );
      aAllMap.put( parentClass.id(), parentNode );
      grandpaNode.addNode( parentNode );
      return parentNode;
    }

    @Override
    public IList<ITsNode> makeRoots( ITsNode aRootNode, IList<ISkObject> aObjs ) {
      // сначала строим дерево классов
      IListEdit<ISkClassInfo> classItems = new ElemArrayList<>();
      for( ISkObject obj : aObjs ) {
        classItems.add( obj.classInfo() );
      }

      IStridablesList<ISkClassInfo> allItems = coreApi.sysdescr().listClasses();
      IStringMapEdit<DefaultTsNode<ISkClassInfo>> allMap = new StringMap<>();
      IListEdit<ITsNode> retVal = new ElemArrayList<>();
      // в корень все классы у которых родитель GW_ROOT_CLASS_ID
      for( ISkClassInfo skClassInfo : allItems ) {
        if( skClassInfo.id().equals( IGwHardConstants.GW_ROOT_CLASS_ID ) ) {
          continue;
        }
        if( skClassInfo.parent().id().equals( IGwHardConstants.GW_ROOT_CLASS_ID ) ) {
          // DefaultTsNode<ISkClassInfo> skRoot = new DefaultTsNode<>( NK_CLASS, aRootNode, skClassInfo );
          DefaultTsNode<ISkClassInfo> skRoot = new SkClassInfoTsNode( aRootNode, skClassInfo );
          allMap.put( skRoot.entity().id(), skRoot );
          retVal.add( skRoot );
        }
      }
      for( ISkClassInfo cinf : allItems ) {
        if( cinf.id().equals( IGwHardConstants.GW_ROOT_CLASS_ID ) ) {
          continue;
        }
        if( allMap.hasKey( cinf.id() ) ) {
          continue;
        }
        DefaultTsNode<ISkClassInfo> parentNode = getParentNode( cinf, allMap, allItems );
        // DefaultTsNode<ISkClassInfo> classNode = new DefaultTsNode<>( NK_CLASS, parentNode, cinf );
        DefaultTsNode<ISkClassInfo> classNode = new SkClassInfoTsNode( parentNode, cinf );
        allMap.put( cinf.id(), classNode );
        parentNode.addNode( classNode );
      }
      // тут у нас есть дерево классов, но в нем нет листьев объектов, добавляем
      for( ISkObject obj : aObjs ) {
        // находим нужный узел класса
        DefaultTsNode<ISkClassInfo> classNode = allMap.findByKey( obj.classInfo().id() );
        DefaultTsNode<ISkObject> objNode = new DefaultTsNode<>( NK_OBJECT, classNode, obj );
        classNode.addNode( objNode );
      }
      return retVal;
    }

    @Override
    public boolean isItemNode( ITsNode aNode ) {
      return aNode.kind() == NK_OBJECT;
    }

  }

  SdedSkObjectMpc( ITsGuiContext aContext, IM5Model<ISkObject> aModel, IM5ItemsProvider<ISkObject> aItemsProvider,
      IM5LifecycleManager<ISkObject> aLifecycleManager ) {
    super( aContext, aModel, aItemsProvider, aLifecycleManager );
    TreeModeInfo<ISkObject> tmiByHierarchy = new TreeModeInfo<>( TMID_GROUP_BY_CLASS, STR_N_TMI_BY_HIERARCHY,
        STR_D_TMI_BY_HIERARCHY, null, new TreeMakerByClass( coreApi() ) );
    treeModeManager().addTreeMode( tmiByHierarchy );
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
    if( selNode != null ) {
      if( selNode.kind() == NK_CLASS ) {
        selClass = (ISkClassInfo)selNode.entity();
        canAdd = true;
        canEdit = false;
      }
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

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    return model().domain().tsContext().get( ISkConnection.class );
  }

}
