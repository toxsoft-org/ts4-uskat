package org.toxsoft.uskat.core.gui.km5.sded.objed;

import org.toxsoft.core.tsgui.bricks.tsnodes.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * Groups objects as a leafs of the classes tree.
 * <p>
 * Displays whole tree of all classes regardless if class has any object.
 *
 * @author hazard157
 */
class TreeMakerByAllClass
    extends AbstractTreeModeByClass {

  public TreeMakerByAllClass( ISkCoreApi aCoreApi ) {
    super( aCoreApi );
  }

  @Override
  protected void doFillClassesTree( IStringMapEdit<DefaultTsNode<String>> aAllClassNodes,
      IStridablesList<ISkClassInfo> aAllClasses, IList<ISkObject> aItems ) {
    IStridablesList<ISkClassInfo> allClasses = allClasses();
    for( ISkClassInfo cinf : allClasses() ) {
      if( !cinf.id().equals( IGwHardConstants.GW_ROOT_CLASS_ID ) ) {
        DefaultTsNode<String> parentNode = ensureParentNode( cinf, aAllClassNodes, allClasses );
        DefaultTsNode<String> node = createClassIdNode( parentNode, cinf.id() );
        node.setName( StridUtils.printf( StridUtils.FORMAT_ID_NAME, cinf ) );
        parentNode.addNode( node );
        aAllClassNodes.put( node.entity(), node );
      }
    }
  }

}
