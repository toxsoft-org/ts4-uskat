package org.toxsoft.uskat.sysext.alarms.impl;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.uskat.sysext.alarms.api.flacon.ISkAlarmFlacon;

/**
 * Хранитель объектов типа {@link ISkAlarmFlacon}.
 *
 * @author dima
 */
public class SkAlarmFlaconKeeper
    extends AbstractEntityKeeper<ISkAlarmFlacon> {

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static IEntityKeeper<ISkAlarmFlacon> KEEPER = new SkAlarmFlaconKeeper();

  private SkAlarmFlaconKeeper() {
    super( ISkAlarmFlacon.class, EEncloseMode.NOT_IN_PARENTHESES, null );
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса AbstractEntityKeeper
  //

  @Override
  protected void doWrite( IStrioWriter aSw, ISkAlarmFlacon aEntity ) {
    aSw.writeAsIs( aEntity.params().toString() );
  }

  @Override
  protected ISkAlarmFlacon doRead( IStrioReader aSr ) {
    // TODO
    return ISkAlarmFlacon.NULL;
  }

}
