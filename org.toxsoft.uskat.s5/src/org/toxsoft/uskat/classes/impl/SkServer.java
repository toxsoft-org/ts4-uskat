package org.toxsoft.uskat.classes.impl;

import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.uskat.classes.ISkServer;
import org.toxsoft.uskat.core.api.objserv.ISkObject;
import org.toxsoft.uskat.core.api.objserv.ISkObjectCreator;
import org.toxsoft.uskat.core.impl.SkObject;

/**
 * Реализация {@link ISkServer} как {@link ISkObject}.
 *
 * @author mvk
 */
public final class SkServer
    extends SkObject
    implements ISkServer {

  static final ISkObjectCreator<SkServer> CREATOR = SkServer::new;

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<ISkServer> KEEPER =
      new AbstractEntityKeeper<>( ISkServer.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aWriter, ISkServer aEntity ) {
          Skid.KEEPER.write( aWriter, aEntity.skid() );
          aWriter.writeSeparatorChar();
          OptionSetKeeper.KEEPER.write( aWriter, aEntity.attrs() );
          aWriter.writeSeparatorChar();
        }

        @Override
        protected ISkServer doRead( IStrioReader aReader ) {
          SkServer retValue = new SkServer( Skid.KEEPER.read( aReader ) );
          aReader.ensureSeparatorChar();
          retValue.attrs().setAll( OptionSetKeeper.KEEPER.read( aReader ) );
          aReader.ensureSeparatorChar();
          return retValue;
        }
      };

  SkServer( Skid aSkid ) {
    super( aSkid );
  }

  // ------------------------------------------------------------------------------------
  // ISkServer
  //
}
