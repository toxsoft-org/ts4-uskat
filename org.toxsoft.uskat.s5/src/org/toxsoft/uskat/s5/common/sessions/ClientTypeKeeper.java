package org.toxsoft.uskat.s5.common.sessions;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;

/**
 * Хранитель объектов типа {@link EClientType}.
 *
 * @author hazard157
 */
public class ClientTypeKeeper
    extends AbstractEntityKeeper<EClientType> {

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static IEntityKeeper<EClientType> KEEPER = new ClientTypeKeeper();

  private ClientTypeKeeper() {
    super( EClientType.class, EEncloseMode.NOT_IN_PARENTHESES, null );
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса AbstractEntityKeeper
  //

  @Override
  protected void doWrite( IStrioWriter aSw, EClientType aEntity ) {
    aSw.writeAsIs( aEntity.id() );
  }

  @Override
  protected EClientType doRead( IStrioReader aSr ) {
    return EClientType.findById( aSr.readIdPath() );
  }

}
