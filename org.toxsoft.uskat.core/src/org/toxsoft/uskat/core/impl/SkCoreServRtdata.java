package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.temporal.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.devapi.*;

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
   * Base implementation of all channels.
   *
   * @author hazard157
   */
  abstract class SkRtdataChannel
      implements ISkRtdataChannel {

    protected final Gwid gwid;

    public SkRtdataChannel( Gwid aGwid ) {
      gwid = aGwid;
    }

    @Override
    final public Gwid gwid() {
      return gwid;
    }

    @Override
    final public boolean isOk() {
      if( !isInited() ) {
        return false;
      }
      return doIsOk();
    }

    protected abstract boolean doIsOk();

  }

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

  // ------------------------------------------------------------------------------------
  // SkCoreServRtdata
  //

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
    ba().baRtdata().configureCurrDataWriter( IGwidList.EMPTY );
    ba().baRtdata().configureCurrDataReader( IGwidList.EMPTY );
    eventer.clearListenersList();
    eventer.resetPendingEvents();
    // close all open curr data write channels
    while( !cdWriteChannelsMap.isEmpty() ) {
      cdWriteChannelsMap.values().first().close();
    }
    // close all open hist data write channels
    while( !hdWriteChannelsMap.isEmpty() ) {
      hdWriteChannelsMap.values().first().close();
    }
    // close all open curr data read channels
    while( !cdReadChannelsMap.isEmpty() ) {
      SkReadCurrDataChannel c = cdReadChannelsMap.values().first();
      c.resetCounter();
      c.close();
    }
  }

  @Override
  protected boolean onBackendMessage( GenericMessage aMessage ) {
    // TODO Auto-generated method stub
  }

  // ------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------
  // Current data write
  // ------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------

  /**
   * {@link ISkReadCurrDataChannel} implementation.
   *
   * @author hazard157
   */
  class SkReadCurrDataChannel
      extends SkRtdataChannel
      implements ISkReadCurrDataChannel {

    private final EAtomicType atomicType;
    private IAtomicValue      value   = null;
    private int               counter = 0;

    SkReadCurrDataChannel( Gwid aGwid ) {
      super( aGwid );
      ISkClassInfo cinf = sysdescr().getClassInfo( aGwid.classId() );
      IDtoRtdataInfo dinf = cinf.rtdata().list().getByKey( aGwid.propId() );
      atomicType = dinf.dataType().atomicType();
      ++counter;
    }

    @Override
    protected boolean doIsOk() {
      return counter != 0;
    }

    @Override
    public void close() {
      if( counter > 0 ) {
        --counter;
      }
      if( counter == 0 ) {
        if( cdReadChannelsMap.hasElem( this ) ) {
          cdReadChannelsMap.removeByKey( gwid );
        }
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

    void incCounter() {
      ++counter;
      TsInternalErrorRtException.checkTrue( counter == Integer.MAX_VALUE );
    }

    void resetCounter() {
      counter = 0;
    }

  }

  private final IMapEdit<Gwid, SkReadCurrDataChannel> cdReadChannelsMap = new ElemMap<>();

  @Override
  public IMap<Gwid, ISkReadCurrDataChannel> createReadCurrDataChannels( IGwidList aGwids ) {
    TsNullArgumentRtException.checkNull( aGwids );
    TsIllegalStateRtException.checkFalse( isInited() );
    IMapEdit<Gwid, ISkReadCurrDataChannel> result = new ElemMap<>();
    // for all valid GWId either get exiting or create new channel
    IGwidList gwids = toValidRtdataGwids( aGwids, false );
    for( Gwid g : gwids ) {
      SkReadCurrDataChannel channel = cdReadChannelsMap.findByKey( g );
      if( channel == null ) {
        channel = new SkReadCurrDataChannel( g );
        cdReadChannelsMap.put( g, channel );
      }
      channel.incCounter();
      result.put( g, channel );
    }
    // inform backend
    ba().baRtdata().configureCurrDataReader( gwids );
    return result;
  }

  // ------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------
  // Current data write
  // ------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------

  /**
   * {@link ISkWriteCurrDataChannel} implementation.
   *
   * @author hazard157
   */
  class SkWriteCurrDataChannel
      extends SkRtdataChannel
      implements ISkWriteCurrDataChannel {

    private final EAtomicType atomicType;

    SkWriteCurrDataChannel( Gwid aGwid ) {
      super( aGwid );
      ISkClassInfo cinf = sysdescr().getClassInfo( aGwid.classId() );
      IDtoRtdataInfo dinf = cinf.rtdata().list().getByKey( aGwid.propId() );
      atomicType = dinf.dataType().atomicType();
    }

    @Override
    protected boolean doIsOk() {
      return cdWriteChannelsMap.hasKey( gwid );
    }

    @Override
    public void close() {
      if( cdWriteChannelsMap.hasElem( this ) ) {
        cdWriteChannelsMap.removeByKey( gwid );
      }
    }

    @Override
    public void setValue( IAtomicValue aValue ) {
      TsIllegalStateRtException.checkFalse( isOk() );
      TsNullArgumentRtException.checkNull( aValue );
      AvTypeCastRtException.checkCanAssign( atomicType, aValue.atomicType() );
      ba().baRtdata().writeCurrData( gwid, aValue );
    }

  }

  /**
   * @see #cdReadChannelsMap
   */
  final IMapEdit<Gwid, SkWriteCurrDataChannel> cdWriteChannelsMap = new ElemMap<>();

  @Override
  public IMap<Gwid, ISkWriteCurrDataChannel> createWriteCurrDataChannels( IGwidList aGwids1 ) {
    TsNullArgumentRtException.checkNull( aGwids1 );
    TsIllegalStateRtException.checkFalse( isInited() );
    IMapEdit<Gwid, ISkWriteCurrDataChannel> result = new ElemMap<>();
    // for all valid GWId either get exiting or create new channel
    IGwidList gwids = toValidRtdataGwids( aGwids1, true );
    for( Gwid g : gwids ) {
      SkWriteCurrDataChannel channel = cdWriteChannelsMap.findByKey( g );
      if( channel != null ) {
        channel = new SkWriteCurrDataChannel( g );
        cdWriteChannelsMap.put( g, channel );
      }
      result.put( g, channel );
    }
    // inform backend
    ba().baRtdata().configureCurrDataWriter( cdWriteChannelsMap.keys() );
    return result;
  }

  // ------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------
  // History data write & read
  // ------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------
  // ------------------------------------------------------------------------------------

  /**
   * {@link ISkWriteHistDataChannel} implementation.
   *
   * @author hazard157
   */
  class SkWriteHistDataChannel
      extends SkRtdataChannel
      implements ISkWriteHistDataChannel {

    private final EAtomicType atomicType;

    SkWriteHistDataChannel( Gwid aGwid ) {
      super( aGwid );
      ISkClassInfo cinf = sysdescr().getClassInfo( aGwid.classId() );
      IDtoRtdataInfo dinf = cinf.rtdata().list().getByKey( aGwid.propId() );
      atomicType = dinf.dataType().atomicType();
    }

    @Override
    protected boolean doIsOk() {
      return hdWriteChannelsMap.hasElem( this );
    }

    @Override
    public void close() {
      if( hdWriteChannelsMap.hasElem( this ) ) {
        hdWriteChannelsMap.removeByKey( gwid );
      }
    }

    @Override
    public void writeValues( ITimeInterval aInterval, ITimedList<ITemporalAtomicValue> aValues ) {
      TsIllegalStateRtException.checkFalse( isOk() );
      TsNullArgumentRtException.checkNulls( aInterval, aValues );
      ITimeInterval interval = aValues.getInterval();
      if( aInterval.startTime() > interval.startTime() || aInterval.endTime() < interval.endTime() ) {
        throw new TsIllegalArgumentRtException( FMT_ERR_HDW_CHANNEL_WRONG_WRITE_INTERVAL, aInterval, interval );
      }
      for( ITemporalAtomicValue temporalValue : aValues ) {
        AvTypeCastRtException.checkCanAssign( atomicType, temporalValue.value().atomicType() );
      }
      ba().baRtdata().writeHistData( gwid, aInterval, aValues );
    }
  }

  /**
   * Open history data writer channels.
   * <p>
   * While channel is in map it it encountered as open, removing channel from map means closing the channel.
   */
  private final IMapEdit<Gwid, SkWriteHistDataChannel> hdWriteChannelsMap = new ElemMap<>();

  @Override
  public IMap<Gwid, ISkWriteHistDataChannel> createWriteHistDataChannels( IGwidList aGwids ) {
    TsNullArgumentRtException.checkNull( aGwids );
    IMapEdit<Gwid, ISkWriteHistDataChannel> result = new ElemMap<>();
    // for all valid GWId either get exiting or create new channel
    IGwidList gwids = toValidRtdataGwids( aGwids, true );
    for( Gwid g : gwids ) {
      SkWriteHistDataChannel channel = hdWriteChannelsMap.findByKey( g );
      if( channel == null ) {
        channel = new SkWriteHistDataChannel( g );
        hdWriteChannelsMap.put( g, channel );
      }
      result.put( g, channel );
    }
    return result;
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

  // ------------------------------------------------------------------------------------
  // Misc
  //

  @Override
  public ITsEventer<ISkCurrDataChangeListener> eventer() {
    return eventer;
  }

}
