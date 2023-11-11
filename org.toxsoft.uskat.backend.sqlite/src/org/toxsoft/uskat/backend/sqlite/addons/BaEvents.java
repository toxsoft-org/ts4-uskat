package org.toxsoft.uskat.backend.sqlite.addons;

import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.backend.sqlite.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link IBaEvents} implementation for {@link SkBackendSqlite}.
 *
 * @author hazard157
 */
public class BaEvents
    extends AbstractAddon
    implements IBaEvents {

  /**
   * Constructor.
   *
   * @param aOwner {@link SkBackendSqlite} - the owner backend
   */
  public BaEvents( SkBackendSqlite aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_EVENTS );
    // TODO Auto-generated constructor stub
  }

  // ------------------------------------------------------------------------------------
  // AbstractAddon
  //

  @Override
  protected void doInit() {
    // TODO doInit()
  }

  @Override
  public void doClose() {
    // nop
  }

  @Override
  public void clear() {
    // TODO clear()
  }

  // ------------------------------------------------------------------------------------
  // IBaEvents
  //

  @Override
  public void fireEvents( ISkEventList aEvents ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void subscribeToEvents( IGwidList aNeededGwids ) {
    // TODO Auto-generated method stub

  }

  @Override
  public ITimedList<SkEvent> queryObjEvents( ITimeInterval aInterval, Gwid aGwid ) {
    // TODO Auto-generated method stub
    return null;
  }

}
