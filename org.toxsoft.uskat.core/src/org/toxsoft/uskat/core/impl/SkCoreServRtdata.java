package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * {@link ISkRtdataService} implementation.
 *
 * @author hazard157
 */
public class SkCoreServRtdata
    extends AbstractSkCoreService
    implements ISkRtdataService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServRtdata::new;

  /**
   * {@link ISkLinkService#eventer()} implementation.
   *
   * @author goga
   */
  class Eventer
      extends AbstractTsEventer<ISkCurrDataChangeListener> {

    private IMapEdit<Gwid, ISkReadCurrDataChannel> map = null;

    @Override
    protected boolean doIsPendingEvents() {
      return map != null;
    }

    @Override
    protected void doFirePendingEvents() {
      IMap<Gwid, ISkReadCurrDataChannel> arg = map;
      map = null;
      for( ISkCurrDataChangeListener l : listeners() ) {
        try {
          l.onCurrData( arg );
        }
        catch( Exception ex ) {
          LoggerUtils.errorLogger().error( ex );
        }
      }
    }

    @Override
    protected void doClearPendingEvents() {
      map = null;
    }

    void addChannelWithFreshData( ISkReadCurrDataChannel aChannel ) {
      if( map == null ) {
        map = new ElemMap<>();
      }
      map.put( aChannel.gwid(), aChannel );
    }

    void fireAddedChannels() {
      doFirePendingEvents();
    }

  }

  /**
   * {@link ISkReadCurrDataChannel} implementation.
   *
   * @author hazard157
   */
  class SkReadCurrDataChannel
      implements ISkReadCurrDataChannel {

    private final Gwid        gwid;
    private final EAtomicType atomicType;
    private IAtomicValue      value   = null;
    private int               counter = 0;

    SkReadCurrDataChannel( Gwid aGwid ) {
      gwid = aGwid;
      ISkClassInfo cinf = sysdescr().getClassInfo( aGwid.classId() );
      IDtoRtdataInfo dinf = cinf.rtdata().list().getByKey( aGwid.propId() );
      atomicType = dinf.dataType().atomicType();
      ++counter;
    }

    @Override
    public Gwid gwid() {
      return gwid;
    }

    @Override
    public boolean isOk() {
      if( !isInited() || counter == 0 ) {
        return false;
      }
      return true;
    }

    @Override
    public void close() {
      if( !isClosed() ) {
        --counter;
      }
      if( isClosed() ) {
        value = null;
      }
    }

    @Override
    public IAtomicValue getValue() {
      TsIllegalStateRtException.checkFalse( isOk() );
      return (value != null ? value : IAtomicValue.NULL);
    }

    boolean setValue( IAtomicValue aValue ) {
      if( value != null && value.equals( aValue ) ) {
        return false;
      }
      if( atomicType != aValue.atomicType() && atomicType != EAtomicType.NONE && aValue != IAtomicValue.NULL ) {
        throw new AvTypeCastRtException( FMT_ERR_RTD_CHNL_INV_ATOMIC_TYPE, gwid.toString(), aValue.atomicType().id(),
            atomicType.id() );
      }
      value = aValue;
      return true;
    }

    boolean isClosed() {
      return counter == 0;
    }

    void incCounter() {
      ++counter;
      TsInternalErrorRtException.checkTrue( counter == Integer.MAX_VALUE );
    }

    void resetCounter() {
      counter = 0;
    }

  }

  /**
   * {@link ISkWriteCurrDataChannel} implementation.
   *
   * @author hazard157
   */
  class SkWriteCurrDataChannel
      implements ISkWriteCurrDataChannel {

    private final Gwid        gwid;
    private final EAtomicType atomicType;

    private IAtomicValue value       = IAtomicValue.NULL;
    private boolean      open        = true;
    private boolean      wasNewValue = true;

    SkWriteCurrDataChannel( Gwid aGwid ) {
      gwid = aGwid;
      ISkClassInfo cinf = sysdescr().getClassInfo( aGwid.classId() );
      IDtoRtdataInfo dinf = cinf.rtdata().list().getByKey( aGwid.propId() );
      atomicType = dinf.dataType().atomicType();
    }

    @Override
    public Gwid gwid() {
      return gwid;
    }

    @Override
    public boolean isOk() {
      return open;
    }

    @Override
    public void close() {
      open = false;
    }

    @Override
    public void setValue( IAtomicValue aValue ) {
      TsIllegalStateRtException.checkFalse( open );
      TsNullArgumentRtException.checkNull( aValue );
      AvTypeCastRtException.checkCanAssign( atomicType, aValue.atomicType() );
      if( !value.equals( aValue ) ) {
        value = aValue;
        wasNewValue = true;
      }
    }

    /**
     * Повторно открывает канал, когда он уже закрыт, но серверу еще не сообщено об этом.
     */
    void reopen() {
      open = true;
      value = IAtomicValue.NULL;
    }

    boolean isClosed() {
      return !open;
    }

    IAtomicValue getValue() {
      return value;
    }

    boolean isNewValue() {
      return wasNewValue;
    }

    boolean chackWasNewValueAndReset() {
      boolean b = wasNewValue;
      wasNewValue = false;
      return b;
    }

  }

  // ------------------------------------------------------------------------------------
  // SkCoreServRtdata
  //

  /**
   * Both read and write current data channels are hold in <code>cdXxxChannelsMap</code>. On each call to
   * <code>createXxxCurrDataChannels()</code> this maps are updated. So these <b>GWID</b>-keyed maps are used by
   * <code>RtdataService</code> to communicate through {@link ISkRtdataService} API while <code>int</code>-keyed maps
   * <code>cdXxxChannelsIndexMap</code> are used to communicate through {@link IBaRtdata} API to the backend. Index maps
   * are also updated in <code>createXxxCurrDataChannels()</code> after retreiving from backend <code>int</code> keys of
   * data.
   */
  final IMapEdit<Gwid, SkReadCurrDataChannel> cdReadChannelsMap = new ElemMap<>();

  /**
   * @see #cdReadChannelsMap
   */
  final IIntMapEdit<SkReadCurrDataChannel> cdReadChannelsIndexMap = new IntMap<>();

  /**
   * @see #cdReadChannelsMap
   */
  final IMapEdit<Gwid, SkWriteCurrDataChannel> cdWriteChannelsMap = new ElemMap<>();

  /**
   * @see #cdReadChannelsMap
   */
  final IIntMapEdit<SkWriteCurrDataChannel> cdWriteChannelsIndexMap = new IntMap<>();

  private final Eventer eventer = new Eventer();

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkCoreServRtdata( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Makes the list of valid GWIDs from the argument list of arbitrary GWIDs.
   * <p>
   * Dependiong on argument <code>aHistoric</code> includes only RTdata with either {@link IDtoRtdataInfo#isCurr()} or
   * {@link IDtoRtdataInfo#isHist()} flag set.
   * <p>
   * GWIDs is considered valid if it is concrete GWID of kind {@link EGwidKind#GW_RTDATA} and is not duplicated. Method
   * also checks that specified {@link ISkObject} exists.
   *
   * @param aList {@link IGwidList} - the source list
   * @param aHistoric boolean - <code>true</code> to selected historic rather than current RTdata
   * @return {@link IGwidList} - the selected GWIDs
   */
  private IGwidList toValidRtdataGwids( IGwidList aList, boolean aHistoric ) {
    GwidList ll = new GwidList();
    // iterate over all GWIDs in argument list
    for( Gwid g : aList ) {
      // check GWID kind
      if( !g.isAbstract() && !g.isMulti() && g.kind() == EGwidKind.GW_RTDATA ) {
        // no duplicates
        if( !ll.hasElem( g ) ) {
          ISkClassInfo cinf = sysdescr().findClassInfo( g.classId() );
          // class must exist
          if( cinf != null ) {
            IDtoRtdataInfo rinf = cinf.rtdata().list().findByKey( g.propId() );
            // RTdata must be defined
            if( rinf != null ) {
              boolean propVal = aHistoric ? rinf.isHist() : rinf.isCurr();
              // RTdata must be current or historic one (depending on aHistoric argument)
              if( propVal ) {
                // object must exist
                if( objServ().find( g.skid() ) != null ) {
                  ll.add( g );
                }
              }
            }
          }
        }
      }
    }
    return ll;
  }

  /**
   * Removes from the {@link #cdReadChannelsMap} closed channels and returns their {@link Gwid}s.
   *
   * @return {@link IGwidList} - list of closed and just removed channels GWIDs
   */
  private IGwidList listClosedReadChannelsAndRemoveFromMap() {
    GwidList list = new GwidList();
    for( SkReadCurrDataChannel channel : cdReadChannelsMap ) {
      if( channel.isClosed() ) {
        list.add( channel.gwid() );
      }
    }
    for( Gwid g : list ) {
      cdReadChannelsMap.removeByKey( g );
    }
    return list;
  }

  /**
   * Removes from the {@link #cdWriteChannelsMap} closed channels and returns their {@link Gwid}s.
   *
   * @return {@link IGwidList} - list of closed and just removed channels GWIDs
   */
  private IGwidList listClosedWriteChannelsAndRemoveFromMap() {
    GwidList list = new GwidList();
    for( SkWriteCurrDataChannel channel : cdWriteChannelsMap ) {
      if( channel.isClosed() ) {
        list.add( channel.gwid() );
      }
    }
    for( Gwid g : list ) {
      cdWriteChannelsMap.removeByKey( g );
    }
    return list;
  }

  /**
   * Sends new current data values to the backend.
   * <p>
   * "new" values means values changed since last call to {@link #sendCurrDataValues()}.
   */
  private void sendCurrDataValues() {
    int newValuesCount = 0;
    for( int i = 0, sz = cdWriteChannelsIndexMap.size(); i < sz; i++ ) {
      SkWriteCurrDataChannel wc = cdWriteChannelsIndexMap.values().get( i );
      if( wc.isNewValue() ) {
        ++newValuesCount;
      }
    }
    if( newValuesCount == 0 ) {
      return;
    }
    FixedCapacityIntMap<IAtomicValue> newValues = new FixedCapacityIntMap<>( newValuesCount );
    for( int i = 0, sz = cdWriteChannelsIndexMap.size(); i < sz; i++ ) {
      SkWriteCurrDataChannel wc = cdWriteChannelsIndexMap.values().get( i );
      if( wc.chackWasNewValueAndReset() ) {
        int cdIndex = cdWriteChannelsIndexMap.keys().getValue( i );
        newValues.add( i, wc.getValue() );
      }
    }
    // TODO SkCoreServRtdata.sendCurrDataValues()
    // FIXME ba().baRtdata().writeCurrData( newValues );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkCoreService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // nop
  }

  @Override
  protected void doClose() {
    // cancel all subscriptions
    ba().baRtdata().configureCurrDataWriter( null, IGwidList.EMPTY );
    ba().baRtdata().configureCurrDataReader( null, IGwidList.EMPTY );
    eventer.clearListenersList();
    eventer.resetPendingEvents();
    // close all open write channels
    while( !cdWriteChannelsMap.isEmpty() ) {
      SkWriteCurrDataChannel c = cdWriteChannelsMap.removeByKey( cdWriteChannelsMap.keys().first() );
      c.close();
    }
    // close all read channels
    while( !cdReadChannelsMap.isEmpty() ) {
      SkReadCurrDataChannel c = cdReadChannelsMap.removeByKey( cdReadChannelsMap.keys().first() );
      c.resetCounter();
      c.close();
    }
  }

  @Override
  public IMap<Gwid, ISkReadCurrDataChannel> createReadCurrDataChannels( IGwidList aGwids ) {
    TsNullArgumentRtException.checkNull( aGwids );
    TsIllegalStateRtException.checkFalse( isInited() );
    IMapEdit<Gwid, ISkReadCurrDataChannel> result = new ElemMap<>();
    // select valid GWIDs
    IGwidList gwids = toValidRtdataGwids( aGwids, false );
    // channels to be listened by backend
    GwidList toAddQuery = new GwidList();
    for( Gwid g : gwids ) {
      // already open channels will be returned
      SkReadCurrDataChannel channel = cdReadChannelsMap.findByKey( g );
      if( channel != null ) {
        result.put( g, channel );
        channel.incCounter();
        continue;
      }
      // create channel and add to channels to be listened by backend
      channel = new SkReadCurrDataChannel( g );
      cdReadChannelsMap.put( g, channel );
      result.put( g, channel );
      toAddQuery.add( g );
    }
    // prepare query to backend
    IGwidList toRemoveQuery = listClosedReadChannelsAndRemoveFromMap();
    IIntMap<Gwid> keyedMap = ba().baRtdata().configureCurrDataReader( toRemoveQuery, toAddQuery );
    // reresh #readChannelsIndexMap
    cdReadChannelsIndexMap.clear();
    for( int i = 0; i < keyedMap.size(); i++ ) {
      int key = keyedMap.keys().getValue( i );
      Gwid gwid = keyedMap.values().get( i );
      SkReadCurrDataChannel channel = cdReadChannelsMap.getByKey( gwid );
      cdReadChannelsIndexMap.put( key, channel );
    }
    return result;
  }

  @Override
  public IMap<Gwid, ISkWriteCurrDataChannel> createWriteCurrDataChannels( IGwidList aGwids1 ) {
    TsNullArgumentRtException.checkNull( aGwids1 );
    TsIllegalStateRtException.checkFalse( isInited() );
    IMapEdit<Gwid, ISkWriteCurrDataChannel> result = new ElemMap<>();
    // send unsent data to server
    // TODO writeCurrValues();
    // select valid GWIDs
    IGwidList gwids = toValidRtdataGwids( aGwids1, true );
    // list of new channels to be created
    GwidList toAddQuery = new GwidList();
    // process all requested GWIDs: return exsiting channels and create unexisting
    for( Gwid g : gwids ) {
      SkWriteCurrDataChannel channel = cdWriteChannelsMap.findByKey( g );
      if( channel != null ) { // use existing channel
        // if channel was closed but backend don't know yet - we can simply reopen channel
        if( channel.isClosed() ) {
          channel.reopen();
        }
      }
      else { // create unexisting channel and add request to backend
        channel = new SkWriteCurrDataChannel( g );
        cdWriteChannelsMap.put( g, channel );
        toAddQuery.add( g );
      }
      result.put( g, channel );
    }
    // prepare query to backend
    IGwidList toRemoveQuery = listClosedWriteChannelsAndRemoveFromMap();
    IIntMap<Gwid> keyedMap = ba().baRtdata().configureCurrDataWriter( toRemoveQuery, toAddQuery );
    // refresh #cdWriteChannelsIndexMap
    cdWriteChannelsIndexMap.clear();
    for( int i = 0; i < keyedMap.size(); i++ ) {
      int key = keyedMap.keys().getValue( i );
      Gwid gwid = keyedMap.values().get( i );
      SkWriteCurrDataChannel channel = cdWriteChannelsMap.getByKey( gwid );
      cdWriteChannelsIndexMap.put( key, channel );
    }
    return result;
  }

  @Override
  public ITsEventer<ISkCurrDataChangeListener> eventer() {
    return eventer;
  }

  @Override
  public IMap<Gwid, ISkWriteHistDataChannel> createWriteHistDataChannels( IGwidList aGwids ) {
    // TODO реализовать SkCoreServRtdata.createWriteHistDataChannels()
    throw new TsUnderDevelopmentRtException( "SkCoreServRtdata.createWriteHistDataChannels()" );
  }

  @Override
  public ITimedList<ITemporalAtomicValue> queryObjRtdata( IQueryInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    TsIllegalArgumentRtException.checkTrue( aGwid.kind() != EGwidKind.GW_RTDATA );
    TsIllegalArgumentRtException.checkTrue( aGwid.isAbstract() );
    TsIllegalArgumentRtException.checkTrue( aGwid.isMulti() );
    TsItemNotFoundRtException.checkFalse( gwidService().exists( aGwid ) );
    return ba().baRtdata().queryObjRtdata( aInterval, aGwid );
  }

}
