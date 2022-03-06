package org.toxsoft.uskat.classes.impl;

import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.uskat.classes.IS5ClassServer;

import ru.uskat.core.common.skobject.ISkObject;
import ru.uskat.core.common.skobject.ISkObjectCreator;
import ru.uskat.core.impl.SkObject;

/**
 * Реализация {@link IS5ClassServer} как {@link ISkObject}.
 *
 * @author mvk
 */
public final class S5ClassServer
    extends SkObject
    implements IS5ClassServer {

  static final ISkObjectCreator<S5ClassServer> CREATOR = S5ClassServer::new;

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<IS5ClassServer> KEEPER =
      new AbstractEntityKeeper<>( IS5ClassServer.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aWriter, IS5ClassServer aEntity ) {
          Skid.KEEPER.write( aWriter, aEntity.skid() );
          aWriter.writeSeparatorChar();
          OptionSetKeeper.KEEPER.write( aWriter, aEntity.attrs() );
          aWriter.writeSeparatorChar();
        }

        @Override
        protected IS5ClassServer doRead( IStrioReader aReader ) {
          S5ClassServer retValue = new S5ClassServer( Skid.KEEPER.read( aReader ) );
          aReader.ensureSeparatorChar();
          retValue.attrs().setAll( OptionSetKeeper.KEEPER.read( aReader ) );
          aReader.ensureSeparatorChar();
          return retValue;
        }
      };

  S5ClassServer( Skid aSkid ) {
    super( aSkid );
  }

  // ------------------------------------------------------------------------------------
  // IS5ClassServer
  //
}
