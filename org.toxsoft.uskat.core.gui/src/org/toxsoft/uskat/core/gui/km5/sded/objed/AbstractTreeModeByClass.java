package org.toxsoft.uskat.core.gui.km5.sded.objed;

import static org.toxsoft.core.tslib.gw.IGwHardConstants.*;
import static org.toxsoft.uskat.core.gui.ISkCoreGuiConstants.*;

import org.toxsoft.core.tsgui.bricks.tsnodes.*;
import org.toxsoft.core.tsgui.bricks.tstree.tmm.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.basis.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * Abstract base class for tree modes where nodes (not leafs) contains class IDs.
 *
 * @author hazard157
 */
abstract class AbstractTreeModeByClass
    implements ITsTreeMaker<ISkObject> {

  private static final ITsNodeKind<String> NK_CLASS_ID = new TsNodeKind<>( "SkClassId", //$NON-NLS-1$
      String.class, true, ICONID_SDED_CLASSES_LIST );

  private static final ITsNodeKind<ISkObject> NK_OBJECT = new TsNodeKind<>( "SkObject", //$NON-NLS-1$
      ISkObject.class, false, ICONID_SDED_CLASS );

  private final ISkCoreApi coreApi;

  public AbstractTreeModeByClass( ISkCoreApi aCoreApi ) {
    coreApi = aCoreApi;
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  final private IStringMap<DefaultTsNode<String>> createClassesTree( ITsNode aRootNode, IList<ISkObject> aItems ) {
    DefaultTsNode<String> skrNode = new DefaultTsNode<>( NK_CLASS_ID, aRootNode, GW_ROOT_CLASS_ID );
    skrNode.setName( allClasses().getByKey( skrNode.entity() ).nmName() );
    IStringMapEdit<DefaultTsNode<String>> allClassNodes = new StringMap<>();
    allClassNodes.put( skrNode.entity(), skrNode );
    doFillClassesTree( allClassNodes, allClasses(), aItems );
    return allClassNodes;
  }

  // ------------------------------------------------------------------------------------
  // API for subclasses
  //

  protected DefaultTsNode<String> ensureParentNode( ISkClassInfo aCinf, IStringMapEdit<DefaultTsNode<String>> aNodesMap,
      IStridablesList<ISkClassInfo> aAllClasses ) {
    DefaultTsNode<String> parentNode = aNodesMap.findByKey( aCinf.parentId() );
    if( parentNode != null ) {
      return parentNode;
    }
    ISkClassInfo parentClass = aAllClasses.getByKey( aCinf.parentId() );
    DefaultTsNode<String> grandpaNode = ensureParentNode( parentClass, aNodesMap, aAllClasses );
    parentNode = createClassIdNode( grandpaNode, parentClass.id() );
    aNodesMap.put( parentClass.id(), parentNode );
    grandpaNode.addNode( parentNode );
    return parentNode;
  }

  protected DefaultTsNode<String> createClassIdNode( DefaultTsNode<String> aParent, String aClassId ) {
    return new DefaultTsNode<>( NK_CLASS_ID, aParent, aClassId );
  }

  protected IStridablesList<ISkClassInfo> allClasses() {
    return coreApi.sysdescr().listClasses();
  }

  // ------------------------------------------------------------------------------------
  // ITsTreeMaker
  //

  @Override
  final public IList<ITsNode> makeRoots( ITsNode aRootNode, IList<ISkObject> aItems ) {
    // create classes tree
    IStringMap<DefaultTsNode<String>> allClassNodes = createClassesTree( aRootNode, aItems );
    // iterate over objects and add to the corresponding class node
    for( ISkObject o : aItems ) {
      DefaultTsNode<String> classNode = allClassNodes.findByKey( o.classId() );
      if( classNode != null ) {
        DefaultTsNode<ISkObject> objNode = new DefaultTsNode<>( NK_OBJECT, classNode, o );
        classNode.addNode( objNode );
      }
    }
    return new SingleItemList<>( allClassNodes.getByKey( IGwHardConstants.GW_ROOT_CLASS_ID ) );
  }

  @Override
  final public boolean isItemNode( ITsNode aNode ) {
    return aNode.kind() == NK_OBJECT;
  }

  // ------------------------------------------------------------------------------------
  // To implement
  //

  /**
   * Implementation must fill <code>aAllClassNodes</code> with classes to be shown.
   * <p>
   * <code>aAllClassNodes</code> contains only element for root class ID {@link IGwHardConstants#GW_ROOT_CLASS_ID}.
   * <p>
   * Note: nodes <b>must</b> be created by method {@link #createClassIdNode(DefaultTsNode, String)} and as a parent node
   * {@link #ensureParentNode(ISkClassInfo, IStringMapEdit, IStridablesList)} may be used.
   *
   * @param aAllClassNodes {@link IStringMapEdit}&gt;{@link DefaultTsNode}&Gt;String&lt;&lt; - editable map to fill
   * @param aAllClasses {@link IStridablesList}&lt;{@link ISkClassInfo}&lt; - all classes read from USkat
   * @param aItems {@link ITsCollection}&lt;{@link ISkObject}&gt; - list of objects to be shown in viewer
   */
  protected abstract void doFillClassesTree( IStringMapEdit<DefaultTsNode<String>> aAllClassNodes,
      IStridablesList<ISkClassInfo> aAllClasses, IList<ISkObject> aItems );

  // ------------------------------------------------------------------------------------
  // static API
  //

  /**
   * Returns class ID if class group node is specified as an argum,ent.
   *
   * @param aNode {@link ITsNode} - the node to check
   * @return String - the class ID or <code>null</code>
   */
  public static String getIfClassIdNode( ITsNode aNode ) {
    if( aNode != null && aNode.kind() == NK_CLASS_ID ) {
      return (String)aNode.entity();
    }
    return null;
  }

}
