package org.toxsoft.uskat.classes.impl;

import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.uskat.classes.ISkServerHistorable;
import org.toxsoft.uskat.core.api.objserv.ISkObject;
import org.toxsoft.uskat.core.api.objserv.ISkObjectCreator;
import org.toxsoft.uskat.core.impl.SkObject;

/**
 * Реализация {@link ISkServerHistorable} как {@link ISkObject}.
 *
 * @author mvk
 */
public final class SkServerHistorable
    extends SkObject
    implements ISkServerHistorable {

  static final ISkObjectCreator<SkServerHistorable> CREATOR = SkServerHistorable::new;

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<ISkServerHistorable> KEEPER =
      new AbstractEntityKeeper<>( ISkServerHistorable.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aWriter, ISkServerHistorable aEntity ) {
          Skid.KEEPER.write( aWriter, aEntity.skid() );
          aWriter.writeSeparatorChar();
          OptionSetKeeper.KEEPER.write( aWriter, aEntity.attrs() );
          aWriter.writeSeparatorChar();
        }

        @Override
        protected ISkServerHistorable doRead( IStrioReader aReader ) {
          SkServerHistorable retValue = new SkServerHistorable( Skid.KEEPER.read( aReader ) );
          aReader.ensureSeparatorChar();
          retValue.attrs().setAll( OptionSetKeeper.KEEPER.read( aReader ) );
          aReader.ensureSeparatorChar();
          return retValue;
        }
      };

  SkServerHistorable( Skid aSkid ) {
    super( aSkid );
  }

  // ------------------------------------------------------------------------------------
  // ISkServerHistorable
  //
}
