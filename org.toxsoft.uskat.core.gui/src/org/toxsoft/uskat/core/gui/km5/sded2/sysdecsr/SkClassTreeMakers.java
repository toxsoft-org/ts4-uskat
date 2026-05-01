package org.toxsoft.uskat.core.gui.km5.sded2.sysdecsr;

import static org.toxsoft.core.tslib.gw.IGwHardConstants.*;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.bricks.tsnodes.*;
import org.toxsoft.core.tsgui.bricks.tstree.tmm.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.utils.*;

/**
 * Tree makes for the {@link ISkClassInfo} collection panel.
 *
 * @author hazard157
 */
class SkClassTreeMakers {

  private static final ITsNodeKind<ISkClassInfo> NK_CLASS = new TsNodeKind<>( "Class", ISkClassInfo.class, true ); //$NON-NLS-1$

  /**
   * Groups classes by hierarchy.
   * <p>
   * To show all parent classes of the class listed in the argument of the method {@link #makeRoots(ITsNode, IList)},
   * this implementation may add respective nodes retrieving {@link ISkClassInfo} entities from CoreAPI.
   *
   * @author hazard157
   */
  static class ByHierarchy
      extends AbstractTreeMaker {

    public ByHierarchy( ITsGuiContext aContext, ISkConnection aConn ) {
      super( aContext, aConn );
    }

    private DefaultTsNode<ISkClassInfo> ensureClassNode( String aClassId,
        IStringMapEdit<DefaultTsNode<ISkClassInfo>> aNodesMap ) {
      // if parent node already exists - return it
      DefaultTsNode<ISkClassInfo> node = aNodesMap.findByKey( aClassId );
      if( node != null ) {
        return node;
      }
      // create new node
      ISkClassInfo aClassInfo = skSysdescr().getClassInfo( aClassId );
      DefaultTsNode<ISkClassInfo> parentNode = ensureClassNode( aClassInfo.parentId(), aNodesMap );
      node = new DefaultTsNode<>( NK_CLASS, parentNode, aClassInfo );
      node.setName( StridUtils.printf( StridUtils.FORMAT_ID_NAME, aClassInfo ) );
      parentNode.addNode( node );
      aNodesMap.put( aClassInfo.id(), node );
      return node;
    }

    @Override
    public IList<ITsNode> makeRoots( ITsNode aRootNode, IList<ISkClassInfo> aItems ) {
      IStringMapEdit<DefaultTsNode<ISkClassInfo>> nodesMap = new SortedStringMap<>();
      IStridablesList<ISkClassInfo> allClasses = skSysdescr().listClasses();
      ISkClassInfo rootClassInfo = allClasses.getByKey( GW_ROOT_CLASS_ID );
      DefaultTsNode<ISkClassInfo> rootClassNode = new DefaultTsNode<>( NK_CLASS, aRootNode, rootClassInfo );
      rootClassNode.setName( StridUtils.printf( StridUtils.FORMAT_ID_NAME, rootClassInfo ) );
      nodesMap.put( GW_ROOT_CLASS_ID, rootClassNode );
      for( ISkClassInfo cinf : aItems ) {
        ensureClassNode( cinf.id(), nodesMap );
        // do nothing if class is already in tree (as root or parent of previously processed class)
        // if( nodesMap.hasKey( cinf.id() ) ) {
        // continue;
        // }

        // DefaultTsNode<ISkClassInfo> parentNode = ensureClassNode( cinf.parentId(), nodesMap );
        // DefaultTsNode<ISkClassInfo> node = new DefaultTsNode<>( NK_CLASS, parentNode, cinf );
        // node.setName( StridUtils.printf( StridUtils.FORMAT_ID_NAME, cinf ) );
        // parentNode.addNode( node );
        // nodesMap.put( cinf.id(), node );
      }
      return new SingleItemList<>( rootClassNode );
    }

  }

  /**
   * Base class for all tree makers.
   *
   * @author hazard157
   */
  static abstract class AbstractTreeMaker
      implements ITsTreeMaker<ISkClassInfo>, ISkGuiContextable {

    private final ITsGuiContext tsContext;
    private final ISkConnection skConn;

    public AbstractTreeMaker( ITsGuiContext aContext, ISkConnection aConn ) {
      TsNullArgumentRtException.checkNulls( aContext, aConn );
      tsContext = aContext;
      skConn = aConn;
    }

    @Override
    abstract public IList<ITsNode> makeRoots( ITsNode aRootNode, IList<ISkClassInfo> aItems );

    @Override
    final public boolean isItemNode( ITsNode aNode ) {
      return aNode.kind() == NK_CLASS;
    }

    // ------------------------------------------------------------------------------------
    // ITsGuiContextable
    //

    @Override
    public ITsGuiContext tsContext() {
      return tsContext;
    }

    // ------------------------------------------------------------------------------------
    // ISkGuiContextable
    //

    @Override
    public ISkConnection skConn() {
      return skConn;
    }

  }

}
