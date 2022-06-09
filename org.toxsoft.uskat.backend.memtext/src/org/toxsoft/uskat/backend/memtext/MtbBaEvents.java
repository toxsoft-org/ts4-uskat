package org.toxsoft.uskat.backend.memtext;

import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link IBaEvents} implementation.
 *
 * @author hazard157
 */
class MtbBaEvents
    extends MtbAbstractAddon
    implements IBaEvents {

  /**
   * Constructor.
   *
   * @param aOwner {@link MtbAbstractBackend} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public MtbBaEvents( MtbAbstractBackend aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_EVENTS );
  }

  // ------------------------------------------------------------------------------------
  // MtbAbstractAddon
  //

  @Override
  public void close() {
    // nop
  }

  @Override
  public void clear() {
    // nop
  }

  @Override
  protected void doWrite( IStrioWriter aSw ) {
    // TODO Auto-generated method stub
  }

  @Override
  protected void doRead( IStrioReader aSr ) {
    // TODO Auto-generated method stub
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  // ------------------------------------------------------------------------------------
  // IBaLinks
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
  public ITimedList<SkEvent> queryEvents( IQueryInterval aInterval, IGwidList aNeededGwids ) {
    // TODO Auto-generated method stub
    return null;
  }

}
