package org.toxsoft.uskat.core.api.evserv;

import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * USkat event collecion.
 *
 * @author hazard157
 * @author mvk
 */
public interface ISkEventList
    extends ITimedList<SkEvent> {

  @Override
  SkEventList selectInterval( ITimeInterval aTimeInterval );

  @Override
  SkEventList selectExtendedInterval( ITimeInterval aTimeInterval );

  @Override
  SkEventList selectAfter( long aTimestamp );

  @Override
  SkEventList selectBefore( long aTimestamp );

}
