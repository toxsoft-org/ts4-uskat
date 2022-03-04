package org.toxsoft.uskat.classes.impl;

import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.uskat.classes.IS5ClassBackend;

import ru.uskat.core.common.skobject.ISkObject;
import ru.uskat.core.common.skobject.ISkObjectCreator;
import ru.uskat.core.impl.SkObject;

/**
 * Реализация {@link IS5ClassBackend} как {@link ISkObject}.
 *
 * @author mvk
 */
public final class S5ClassBackend
    extends SkObject
    implements IS5ClassBackend {

  static final ISkObjectCreator<S5ClassBackend> CREATOR = S5ClassBackend::new;

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<IS5ClassBackend> KEEPER =
      new AbstractEntityKeeper<>( IS5ClassBackend.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aWriter, IS5ClassBackend aEntity ) {
          Skid.KEEPER.write( aWriter, aEntity.skid() );
          aWriter.writeSeparatorChar();
          OptionSetKeeper.KEEPER.write( aWriter, aEntity.attrs() );
          aWriter.writeSeparatorChar();
        }

        @Override
        protected IS5ClassBackend doRead( IStrioReader aReader ) {
          S5ClassBackend retValue = new S5ClassBackend( Skid.KEEPER.read( aReader ) );
          aReader.ensureSeparatorChar();
          retValue.attrs().setAll( OptionSetKeeper.KEEPER.read( aReader ) );
          aReader.ensureSeparatorChar();
          return retValue;
        }
      };

  S5ClassBackend( Skid aSkid ) {
    super( aSkid );
  }

  // ------------------------------------------------------------------------------------
  // IS5ClassBackend
  //
}
