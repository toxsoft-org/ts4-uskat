package org.toxsoft.uskat.backend.memtext;

import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;

/**
 * {@link IBaRtdata} implementation.
 *
 * @author hazard157
 */
public class MtbBaRtdata
    extends MtbAbstractAddon
    implements IBaRtdata {

  /**
   * Constructor.
   *
   * @param aOwner {@link MtbAbstractBackend} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public MtbBaRtdata( MtbAbstractBackend aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_RTDATA );
  }

  // ------------------------------------------------------------------------------------
  // MtbAbstractAddon
  //

  @Override
  public void close() {
    // TODO Auto-generated method stub
  }

  @Override
  public void clear() {
    // TODO Auto-generated method stub
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
  // Package API
  //

  @Override
  void papiRemoveEntitiesOfClassIdsBeforeSave( IStringList aClassIds ) {

    // TODO реализовать papiRemoveEntitiesOfClassIdsBeforeSave()
    throw new TsUnderDevelopmentRtException( "papiRemoveEntitiesOfClassIdsBeforeSave()" );

  }

  @Override
  public ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid ) {
    // TODO Auto-generated method stub
    return null;
  }

}
