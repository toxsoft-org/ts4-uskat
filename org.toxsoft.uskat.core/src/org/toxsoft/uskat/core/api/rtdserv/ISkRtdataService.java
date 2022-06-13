package org.toxsoft.uskat.core.api.rtdserv;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * Core service: real-time data current and historic values.
 *
 * @author hazard157
 */
public interface ISkRtdataService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".RtData"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // CurrData

  /**
   * Returns channels to read current values of RTdata.
   * <p>
   * Invalid GWIDs in argument list are ignored. Valid GWIDs are concrete GWID of kind {@link EGwidKind#GW_RTDATA} for
   * existing objects and with {@link IDtoRtdataInfo#isCurr()} flag set. Duplicate GWIDs are also ignored.
   * <p>
   * For each valid GWID a channel is returned either new or shared one with internal usage counter increased.
   *
   * @param aGwids {@link IGwidList} - requested RTdata GWIDs
   * @return {@link IMap}&lt;{@link Gwid},{@link ISkReadCurrDataChannel}&gt; - channels to read current values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IMap<Gwid, ISkReadCurrDataChannel> createReadCurrDataChannels( IGwidList aGwids );

  /**
   * Returns channels to write current values of RTdata.
   * <p>
   * Invalid GWIDs in argument list are ignored. Valid GWIDs are concrete GWID of kind {@link EGwidKind#GW_RTDATA} for
   * existing objects and with {@link IDtoRtdataInfo#isCurr()} flag set. Duplicate GWIDs are also ignored.
   * <p>
   * For each valid GWID a channel is returned either new or exsiting instance.
   *
   * @param aGwids {@link IGwidList} - requested RTdata GWIDs
   * @return {@link IMap}&lt;{@link Gwid},{@link ISkWriteCurrDataChannel}&gt; - channels to write scurrent values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IMap<Gwid, ISkWriteCurrDataChannel> createWriteCurrDataChannels( IGwidList aGwids );

  /**
   * Returns current values change eventer.
   *
   * @return {@link ITsEventer}&lt;{@link ISkCurrDataChangeListener}&gt; - the eventer
   */
  ITsEventer<ISkCurrDataChangeListener> eventer();

  // ------------------------------------------------------------------------------------
  // HistData

  /**
   * Creates an empty query to historic data.
   * <p>
   * Options values are implementation specific and are described somwhere else.
   *
   * @param aOptions {@link IOptionSet} - optional query execution parameters
   * @return {@link ISkHistDataQuery} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ISkHistDataQuery createQuery( IOptionSet aOptions );

  /**
   * Returns channels to write historic data to.
   * <p>
   * Argument GWIDs will be analysed and expanded as described for {@link ISkHistDataQuery#prepare(IGwidList)}.
   * <p>
   * For each valid GWID the channel will be returned. Either new instance or an existing one.
   *
   * @param aGwids {@link IGwidList} - GWIDs of requested RTdata
   * @return {@link IMap}&lt; {@link Gwid}, {@link ISkWriteHistDataChannel} &gt; - created write channels
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IMap<Gwid, ISkWriteHistDataChannel> createWriteHistDataChannels( IGwidList aGwids );

}
