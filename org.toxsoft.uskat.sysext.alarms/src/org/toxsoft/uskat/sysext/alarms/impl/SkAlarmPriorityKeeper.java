package org.toxsoft.uskat.sysext.alarms.impl;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.uskat.sysext.alarms.api.EAlarmPriority;

/**
 * Хранитель объектов типа {@link EAlarmPriority}.
 *
 * @author goga
 */
public class SkAlarmPriorityKeeper
    extends AbstractEntityKeeper<EAlarmPriority> {

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static IEntityKeeper<EAlarmPriority> KEEPER = new SkAlarmPriorityKeeper();

  private SkAlarmPriorityKeeper() {
    super( EAlarmPriority.class, EEncloseMode.NOT_IN_PARENTHESES, null );
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса AbstractEntityKeeper
  //

  @Override
  protected void doWrite( IStrioWriter aSw, EAlarmPriority aEntity ) {
    aSw.writeAsIs( aEntity.id() );
  }

  @Override
  protected EAlarmPriority doRead( IStrioReader aSr ) {
    return EAlarmPriority.findById( aSr.readIdPath() );
  }

}
