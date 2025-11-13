package org.toxsoft.uskat.core.gui.km5.sded.objed;

import org.toxsoft.core.tsgui.bricks.tsnodes.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * Groups objects as a leafs of the classes tree.
 * <p>
 * Displays only classes which has an objects.
 *
 * @author hazard157
 */
class TreeMakerByClass
    extends AbstractTreeModeByClass {

  public TreeMakerByClass( ISkCoreApi aCoreApi ) {
    super( aCoreApi );
  }

  @Override
  protected void doFillClassesTree( IStringMapEdit<DefaultTsNode<String>> aAllClassNodes,
      IStridablesList<ISkClassInfo> aAllClasses, IList<ISkObject> aItems ) {
    // list class IDs used by items
    IStringListBasicEdit usedClassIds = new SortedStringLinkedBundleList();
    for( ISkObject o : aItems ) {
      if( !usedClassIds.hasElem( o.classId() ) ) {
        usedClassIds.add( o.classId() );
      }
    }
    // include classes listed above
    IStridablesList<ISkClassInfo> allClasses = allClasses();
    for( String classId : usedClassIds ) {
      ISkClassInfo cinf = allClasses.getByKey( classId );
      if( !cinf.id().equals( IGwHardConstants.GW_ROOT_CLASS_ID ) ) {
        DefaultTsNode<String> parentNode = ensureParentNode( cinf, aAllClassNodes, allClasses );
        DefaultTsNode<String> node = createClassIdNode( parentNode, cinf.id() );
        node.setName( StridUtils.printf( StridUtils.FORMAT_ID_NAME, cinf ) );
        parentNode.addNode( node );
        aAllClassNodes.put( node.entity(), node );
      }
    }
  }

  // private DefaultTsNode<String> ensureClassNode( ISkClassInfo aCinf, IStringMapEdit<DefaultTsNode<String>> aRootsMap,
  // IStringMapEdit<DefaultTsNode<String>> aAllMap, IStridablesList<ISkClassInfo> aAllClasses ) {
  // // return if already exists
  // DefaultTsNode<String> found = aAllMap.findByKey( aCinf.id() );
  // if( found != null ) {
  // return found;
  // }
  // DefaultTsNode<String> parentNode = ensureClassNode( aCinf.parent(), aRootsMap, aAllMap, aAllClasses );
  // found = new DefaultTsNode<>( SdedSkObjectMpc.NK_CLASS_ID, parentNode, aCinf.id() );
  // found.setName( aCinf.nmName() );
  // aAllMap.put( found.entity(), found );
  // parentNode.addNode( found );
  // // sort parent node children
  // IListEdit<ITsNode> allChildren = new ElemArrayList<>();
  // IListBasicEdit<DefaultTsNode<String>> sortedClassChildren = new SortedElemLinkedBundleListEx<>( ( aO1, aO2 ) -> {
  // ISkClassInfo cinf1 = aAllClasses.getByKey( aO1.entity() );
  // ISkClassInfo cinf2 = aAllClasses.getByKey( aO2.entity() );
  // return cinf1.nmName().compareTo( cinf2.nmName() );
  // } );
  // for( ITsNode node : parentNode.childs() ) {
  // if( node.kind().equals( SdedSkObjectMpc.NK_CLASS_ID ) ) {
  // sortedClassChildren.add( (DefaultTsNode)node );
  // }
  // }
  // allChildren.addAll( (IList)sortedClassChildren );
  //
  // IListBasicEdit<DefaultTsNode<ISkObject>> sortedObjChildren =
  // new SortedElemLinkedBundleListEx<>( ( aO1, aO2 ) -> aO1.entity().nmName().compareTo( aO2.entity().nmName() ) );
  // for( ITsNode node : parentNode.childs() ) {
  // if( node.kind().equals( SdedSkObjectMpc.NK_OBJECT ) ) {
  // sortedObjChildren.add( (DefaultTsNode)node );
  // }
  // }
  // allChildren.addAll( (IList)sortedObjChildren );
  //
  // parentNode.setNodes( allChildren );
  // return found;
  // }
  //
  // @Override
  // public IList<ITsNode> makeRoots( ITsNode aRootNode, IList<ISkObject> aObjs ) {
  // IStridablesList<ISkClassInfo> allClasses = coreApi.sysdescr().listClasses();
  // // initialize root and helper nodes with GreenWorld's root class node
  // IStringMapEdit<DefaultTsNode<String>> rootsMap = new StringMap<>();
  // IStringMapEdit<DefaultTsNode<String>> allMap = new StringMap<>();
  // DefaultTsNode<String> skrNode =
  // new DefaultTsNode<>( SdedSkObjectMpc.NK_CLASS_ID, aRootNode, IGwHardConstants.GW_ROOT_CLASS_ID );
  // skrNode.setName( allClasses.getByKey( skrNode.entity() ).nmName() );
  // rootsMap.put( skrNode.entity(), skrNode );
  // allMap.put( skrNode.entity(), skrNode );
  // // iterate over objects and add to the corresponding class node
  // for( ISkObject o : aObjs ) {
  // DefaultTsNode<String> classNode = ensureClassNode( o.classInfo(), rootsMap, allMap, allClasses );
  // DefaultTsNode<ISkObject> objNode = new DefaultTsNode<>( SdedSkObjectMpc.NK_OBJECT, classNode, o );
  // classNode.addNode( objNode );
  // }
  // IListBasicEdit<DefaultTsNode<String>> rootsList = new SortedElemLinkedBundleListEx<>( ( aO1, aO2 ) -> {
  // ISkClassInfo cinf1 = allClasses.getByKey( aO1.entity() );
  // ISkClassInfo cinf2 = allClasses.getByKey( aO2.entity() );
  // return cinf1.nmName().compareTo( cinf2.nmName() );
  // } );
  // rootsList.setAll( rootsMap.values() );
  // return (IList)rootsList;
  // }

}
