package org.toxsoft.uskat.s5.utils.schedules;

import static org.toxsoft.core.tslib.bricks.time.impl.TimeUtils.*;
import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import java.util.Date;

import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;

/**
 * Хранитель объектов типа {@link IScheduleExpression} в текстовое представление.
 * <p>
 * Для хранения отдельных {@link IScheduleExpression} и их списков используйте методы интерфейса {@link IEntityKeeper}.
 *
 * @author mvk
 */
public class S5ScheduleExpressionKeeper
    extends AbstractEntityKeeper<IScheduleExpression> {

  /**
   * Экземпляр-синглтон хранителя.
   */
  public static final IEntityKeeper<IScheduleExpression> KEEPER = new S5ScheduleExpressionKeeper();

  private S5ScheduleExpressionKeeper() {
    super( IScheduleExpression.class, EEncloseMode.ENCLOSES_BASE_CLASS, null );
  }

  // ------------------------------------------------------------------------------------
  // Реализация методов класса AbstractEntityKeeper
  //
  @Override
  protected void doWrite( IStrioWriter aSw, IScheduleExpression aEntity ) {
    aSw.writeQuotedString( aEntity.getSecond() );
    aSw.writeSeparatorChar();
    aSw.writeQuotedString( aEntity.getMinute() );
    aSw.writeSeparatorChar();
    aSw.writeQuotedString( aEntity.getHour() );
    aSw.writeSeparatorChar();
    aSw.writeQuotedString( aEntity.getDayOfMonth() );
    aSw.writeSeparatorChar();
    aSw.writeQuotedString( aEntity.getMonth() );
    aSw.writeSeparatorChar();
    aSw.writeQuotedString( aEntity.getDayOfWeek() );
    aSw.writeSeparatorChar();
    aSw.writeQuotedString( aEntity.getYear() );
    aSw.writeSeparatorChar();
    String timezone = aEntity.getTimezone();
    Date start = aEntity.getStart();
    Date end = aEntity.getEnd();
    aSw.writeQuotedString( timezone != null ? timezone : EMPTY_STRING );
    aSw.writeSeparatorChar();
    aSw.writeQuotedString( start != null ? timestampToString( start.getTime() ) : EMPTY_STRING );
    aSw.writeSeparatorChar();
    aSw.writeQuotedString( end != null ? timestampToString( end.getTime() ) : EMPTY_STRING );
  }

  @Override
  protected IScheduleExpression doRead( IStrioReader aSr ) {
    S5ScheduleExpression retValue = new S5ScheduleExpression();
    retValue.second( aSr.readQuotedString() );
    aSr.ensureSeparatorChar();
    retValue.minute( aSr.readQuotedString() );
    aSr.ensureSeparatorChar();
    retValue.hour( aSr.readQuotedString() );
    aSr.ensureSeparatorChar();
    retValue.dayOfMonth( aSr.readQuotedString() );
    aSr.ensureSeparatorChar();
    retValue.month( aSr.readQuotedString() );
    aSr.ensureSeparatorChar();
    retValue.dayOfWeek( aSr.readQuotedString() );
    aSr.ensureSeparatorChar();
    retValue.year( aSr.readQuotedString() );
    aSr.ensureSeparatorChar();
    String timezone = aSr.readQuotedString();
    aSr.ensureSeparatorChar();
    String start = aSr.readQuotedString();
    aSr.ensureSeparatorChar();
    String end = aSr.readQuotedString();
    if( timezone.equals( EMPTY_STRING ) ) {
      timezone = null;
    }
    if( start.equals( EMPTY_STRING ) ) {
      start = null;
    }
    if( end.equals( EMPTY_STRING ) ) {
      end = null;
    }
    retValue.timezone( null );
    retValue.start( start != null ? new Date( TimeUtils.readTimestamp( start ) ) : null );
    retValue.end( end != null ? new Date( TimeUtils.readTimestamp( end ) ) : null );

    return retValue;
  }
}
