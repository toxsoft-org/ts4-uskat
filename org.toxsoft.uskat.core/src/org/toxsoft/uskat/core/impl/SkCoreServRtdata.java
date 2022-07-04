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

    private IAtomicValue value    = null;
    private boolean      open     = true;
    private int          keyCache = -1;  // кешированный ключ в карте cdWriteChannelsIndexMap

    SkWriteCurrDataChannel( Gwid aGwid, EAtomicType aAtomicType ) {
      gwid = aGwid;
      atomicType = aAtomicType;
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
      // 2020-10-19 mvk
      if( keyCache < 0 ) {
        throw new TsInternalErrorRtException( FMT_ERR_CDW_CHANNEL_HAS_NO_KEY, gwid.toString() );
      }
      if( value == null || !value.equals( aValue ) ) {
        value = aValue;
        cdChannelsWithFreshDataToWrite.put( gwid, this );
      }
    }

    /**
     * Повторно открывает канал, когда он уже закрыт, но серверу еще не сообщено об этом.
     */
    void reopen() {
      open = true;
      value = null;
    }

    boolean isClosed() {
      return !open;
    }

    int getCachedKey() {
      return keyCache;
    }

    void setKeyCache( int aKey ) {
      keyCache = aKey;
    }

    IAtomicValue getValue() {
      return (value == null ? IAtomicValue.NULL : value); // этот метод не вызывается до первого вызова setValue()
    }

  }

  /**
   * FIXME usage?
   */

  final IMapEdit<Gwid, SkWriteCurrDataChannel> cdChannelsWithFreshDataToWrite = new ElemMap<>();
  final IIntMapEdit<SkReadCurrDataChannel>     cdReadChannelsIndexMap         = new IntMap<>();
  final IIntMapEdit<SkWriteCurrDataChannel>    cdWriteChannelsIndexMap        = new IntMap<>();
  final IMapEdit<Gwid, SkReadCurrDataChannel>  cdReadChannelsMap              = new ElemMap<>();
  final IMapEdit<Gwid, SkWriteCurrDataChannel> cdWriteChannelsMap             = new ElemMap<>();

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
   * Makes from argument the list of valid GWIDs.
   * <p>
   * Dependiong on argument <code>aHistoric</code> includes only RTdata either {@link IDtoRtdataInfo#isCurr()} or
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
   * @return {@link IGwidList} - List of closed and just removed channels GWIDs
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
    // отменить все подписки в eventer
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
  public IMap<Gwid, ISkReadCurrDataChannel> createReadCurrDataChannels( IGwidList aGwids1 ) {
    TsNullArgumentRtException.checkNull( aGwids1 );
    TsIllegalStateRtException.checkFalse( isInited() );
    IMapEdit<Gwid, ISkReadCurrDataChannel> result = new ElemMap<>();
    // select valid GWIDs
    IGwidList gwids = toValidRtdataGwids( aGwids1, false );
    // channels to be listened by backend
    GwidList toAddQuery = new GwidList();
    for( Gwid g : gwids ) {
      // already open channels will be returned
      SkReadCurrDataChannel channel = cdReadChannelsMap.findByKey( g );
      if( channel != null && channel.isOk() ) {
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
      SkReadCurrDataChannel channel = cdReadChannelsMap.findByKey( gwid );
      TsInternalErrorRtException.checkNull( channel ); // must not happen
      cdReadChannelsIndexMap.put( key, channel );
    }
    return result;
  }

  @Override
  public IMap<Gwid, ISkWriteCurrDataChannel> createWriteCurrDataChannels( IGwidList aGwids ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ITsEventer<ISkCurrDataChangeListener> eventer() {
    return eventer;
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

  @Override
  public IMap<Gwid, ISkWriteHistDataChannel> createWriteHistDataChannels( IGwidList aGwids ) {
    // TODO Auto-generated method stub
    return null;
  }

}
