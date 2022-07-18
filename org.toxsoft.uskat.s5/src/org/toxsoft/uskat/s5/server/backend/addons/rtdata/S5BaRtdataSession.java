package org.toxsoft.uskat.s5.server.backend.addons.rtdata;

import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;

import java.util.concurrent.TimeUnit;

import javax.ejb.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.temporal.ITemporalAtomicValue;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.s5.server.backend.addons.S5AbstractBackendAddonSession;
import org.toxsoft.uskat.s5.server.backend.supports.rtdata.IS5BackendRtdataSingleton;
import org.toxsoft.uskat.s5.server.sessions.init.IS5SessionInitData;
import org.toxsoft.uskat.s5.server.sessions.init.S5SessionInitResult;
import org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackWriter;

/**
 * Реализация сессии расширения бекенда {@link IS5BaRtdataSession}.
 *
 * @author mvk
 */
@Stateful
@StatefulTimeout( value = STATEFULL_TIMEOUT, unit = TimeUnit.MILLISECONDS )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
class S5BaRtdataSession
    extends S5AbstractBackendAddonSession
    implements IS5BaRtdataSession {

  private static final long serialVersionUID = 157157L;

  /**
   * Поддержка сервера запросов к данным реального времени
   */
  @EJB
  private IS5BackendRtdataSingleton rtdataSupport;

  /**
   * Пустой конструктор.
   */
  public S5BaRtdataSession() {
    super( ISkBackendHardConstant.BAINF_RTDATA );
  }

  // ------------------------------------------------------------------------------------
  // Реализация шаблонных методов S5BackendAddonSession
  //
  @Override
  protected Class<? extends IS5BaRtdataSession> doGetSessionView() {
    return IS5BaRtdataSession.class;
  }

  @Override
  protected void doAfterInit( S5SessionCallbackWriter aCallbackWriter, IS5SessionInitData aInitData,
      S5SessionInitResult aInitResult ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Реализация IS5BaRtdataSession
  //
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void configureCurrDataReader( IList<Gwid> aRtdGwids ) {
    TsNullArgumentRtException.checkNull( aRtdGwids );
    rtdataSupport.configureCurrDataReader( aRtdGwids );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void configureCurrDataWriter( IList<Gwid> aRtdGwids ) {
    TsNullArgumentRtException.checkNull( aRtdGwids );
    rtdataSupport.configureCurrDataWriter( aRtdGwids );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void writeCurrData( Gwid aGwid, IAtomicValue aValue ) {
    TsNullArgumentRtException.checkNulls( aGwid, aValue );
    rtdataSupport.writeCurrData( aGwid, aValue );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public void writeHistData( Gwid aGwid, ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
    TsNullArgumentRtException.checkNulls( aGwid, aInterval, aValues );
    rtdataSupport.writeHistData( aGwid, aInterval, aValues );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    return rtdataSupport.queryObjRtdata( aInterval, aGwid );
  }
}
