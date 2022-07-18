package org.toxsoft.uskat.s5.server.backend.addons.rtdata;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.backend.api.IBaRtdata;
import org.toxsoft.uskat.s5.server.backend.addons.IS5BackendLocal;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonLocal;
import org.toxsoft.uskat.s5.server.backend.supports.rtdata.IS5BackendRtdataSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.rtdata.impl.S5BackendRtdataSingleton;

/**
 * Local {@link IBaRtdata} implementation.
 *
 * @author mvk
 */
class S5BaRtdataLocal
    extends S5AbstractBackendAddonLocal
    implements IBaRtdata {

  /**
   * Поддержка сервера обработки запросов к данным реального времени
   */
  private final IS5BackendRtdataSingleton rtdataSupport;

  /**
   * Constructor.
   *
   * @param aOwner {@link IS5BackendLocal} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public S5BaRtdataLocal( IS5BackendLocal aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_RTDATA );
    // Синглтон поддержки чтения/записи системного описания
    rtdataSupport =
        aOwner.backendSingleton().get( S5BackendRtdataSingleton.BACKEND_RTDATA_ID, IS5BackendRtdataSingleton.class );
  }

  // ------------------------------------------------------------------------------------
  // BackendAddonBase
  //
  @Override
  public void onBackendMessage( GtMessage aMessage ) {
    // if( aMessage.messageId().equals( S5BaAfterInitMessages.MSG_ID ) ) {
    // }
  }

  @Override
  public void close() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // IBaRtdata
  //
  @Override
  public void configureCurrDataReader( IList<Gwid> aRtdGwids ) {
    TsNullArgumentRtException.checkNull( aRtdGwids );
    rtdataSupport.configureCurrDataReader( aRtdGwids );
  }

  @Override
  public void configureCurrDataWriter( IList<Gwid> aRtdGwids ) {
    TsNullArgumentRtException.checkNull( aRtdGwids );
    rtdataSupport.configureCurrDataWriter( aRtdGwids );
  }

  @Override
  public void writeCurrData( Gwid aGwid, IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNulls( aGwid, aValue );
    rtdataSupport.writeCurrData( aGwid, aValue );
  }

  @Override
  public void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aGwid, aInterval, aValues );
    rtdataSupport.writeHistData( aGwid, aInterval, aValues );
  }

  @Override
  public ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    return rtdataSupport.queryObjRtdata( aInterval, aGwid );
  }
}
