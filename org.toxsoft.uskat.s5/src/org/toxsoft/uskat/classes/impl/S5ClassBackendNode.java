package org.toxsoft.uskat.classes.impl;

import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.uskat.classes.IS5ClassNode;
import org.toxsoft.uskat.core.api.objserv.ISkObject;
import org.toxsoft.uskat.core.api.objserv.ISkObjectCreator;
import org.toxsoft.uskat.core.impl.SkObject;

/**
 * Реализация {@link IS5ClassNode} как {@link ISkObject}.
 *
 * @author mvk
 */
public final class S5ClassBackendNode
    extends SkObject
    implements IS5ClassNode {

  static final ISkObjectCreator<S5ClassBackendNode> CREATOR = S5ClassBackendNode::new;

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<IS5ClassNode> KEEPER =
      new AbstractEntityKeeper<>( IS5ClassNode.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aWriter, IS5ClassNode aEntity ) {
          Skid.KEEPER.write( aWriter, aEntity.skid() );
          aWriter.writeSeparatorChar();
          OptionSetKeeper.KEEPER.write( aWriter, aEntity.attrs() );
          aWriter.writeSeparatorChar();
        }

        @Override
        protected IS5ClassNode doRead( IStrioReader aReader ) {
          S5ClassBackendNode retValue = new S5ClassBackendNode( Skid.KEEPER.read( aReader ) );
          aReader.ensureSeparatorChar();
          retValue.attrs().setAll( OptionSetKeeper.KEEPER.read( aReader ) );
          aReader.ensureSeparatorChar();
          return retValue;
        }
      };

  S5ClassBackendNode( Skid aSkid ) {
    super( aSkid );
  }

  // ------------------------------------------------------------------------------------
  // IS5ClassNode
  //
}
