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
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.linkserv.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
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

    SkReadCurrDataChannel( Gwid aGwid, EAtomicType aAtomicType ) {
      gwid = aGwid;
      atomicType = aAtomicType;
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
      AvTypeCastRtException.checkCanAssign( atomicType, aValue.atomicType(), FMT_ERR_RTD_CHNL_INV_ATOMIC_TYPE,
          gwid.toString(), aValue.atomicType().id(), atomicType.id() );
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
  // AbstractSkCoreService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // TODO Auto-generated method stub
  }

  @Override
  protected void doClose() {
    // TODO Auto-generated method stub
  }

  @Override
  public IMap<Gwid, ISkReadCurrDataChannel> createReadCurrDataChannels( IGwidList aGwids ) {
    // TODO Auto-generated method stub
    return null;
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
