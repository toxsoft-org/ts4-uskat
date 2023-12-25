package org.toxsoft.uskat.backend.sqlite.addons;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.uskat.backend.sqlite.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link IBaRtdata} implementation for {@link SkBackendSqlite}.
 *
 * @author hazard157
 */
public class BaRtData
    extends AbstractAddon
    implements IBaRtdata {

  /**
   * Constructor.
   *
   * @param aOwner {@link SkBackendSqlite} - the owner backend
   */
  public BaRtData( SkBackendSqlite aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_RTDATA );
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
  // IBaRtData
  //

  @Override
  public void configureCurrDataReader( IGwidList aRtdGwids ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void configureCurrDataWriter( IGwidList aRtdGwids ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void writeCurrData( Gwid aGwid, IAtomicValue aValue ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    // TODO Auto-generated method stub

  }

  @Override
  public ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid ) {
    // TODO Auto-generated method stub
    return null;
  }

}
