package org.toxsoft.uskat.s5.utils.schedules;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;

/**
 * Хранитель объекта типа {@link S5ScheduleExpressionList} в текстовое представление.
 *
 * @author mvk
 */
public class S5ScheduleExpressionListKeeper
    extends AbstractEntityKeeper<S5ScheduleExpressionList> {

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<S5ScheduleExpressionList> KEEPER = new S5ScheduleExpressionListKeeper();

  private S5ScheduleExpressionListKeeper() {
    super( S5ScheduleExpressionList.class, EEncloseMode.ENCLOSES_BASE_CLASS, null );
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса AbstractEntityKeeper
  //

  @Override
  protected void doWrite( IStrioWriter aSw, S5ScheduleExpressionList aEntity ) {
    S5ScheduleExpressionKeeper.KEEPER.writeColl( aSw, aEntity, false );
  }

  @Override
  protected S5ScheduleExpressionList doRead( IStrioReader aSr ) {
    S5ScheduleExpressionList retValue = new S5ScheduleExpressionList();
    S5ScheduleExpressionKeeper.KEEPER.readColl( aSr, retValue );
    return retValue;
  }
}
