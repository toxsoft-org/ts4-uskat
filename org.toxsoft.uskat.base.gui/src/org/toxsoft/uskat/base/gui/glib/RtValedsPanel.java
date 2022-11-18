package org.toxsoft.uskat.base.gui.glib;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.panels.*;
import org.toxsoft.core.tsgui.valed.api.*;
import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.base.gui.conn.*;
import org.toxsoft.uskat.base.gui.utils.*;
import org.toxsoft.uskat.core.api.rtdserv.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Panel with ability to update VALEDs values from the specified GWIDs of the RtData.
 * <p>
 * Usage:
 * <ul>
 * <li>create a subclass of this class;</li>
 * <li>in constructor create arbitrary panel content including VALEDs to be updated in real-time;</li>
 * <li>in constructor define which VALEDs will be updated in real type by calling
 * {@link #defineRtData(Gwid, IValedControl)} for each pair GWID-VALED;</li>
 * <li>call {@link #rtStart()} to start real-time updating. {@link #rtStart()} may be called either in constructor or
 * from outside after panel is created;</li>
 * <li>optionally, VALED updates may be temporarily paused by {@link #rtPause()} and restarted again by
 * {@link #rtStart()}.</li>
 * </ul>
 * Note: real-time VALEDs <b>must</b> with {@link IAtomicValue} types of values.<br>
 * Note: GWID-VALED bindings can not be changed after first call to {@link #rtStart()}.<br>
 *
 * @author hazard157
 */
public class RtValedsPanel
    extends TsPanel
    implements ISkGuiContextable {

  /**
   * Item holds binding GWID->VALED after panel is started.
   *
   * @author hazard157
   */
  static class RtdItem
      implements ICooperativeMultiTaskable {

    private final IValedControl<IAtomicValue> valed;
    private final ISkReadCurrDataChannel      channel;

    private IAtomicValue lastValue = IAtomicValue.NULL;

    public RtdItem( IValedControl<IAtomicValue> aValed, ISkReadCurrDataChannel aChannel ) {
      valed = aValed;
      channel = aChannel;
    }

    @Override
    public void doJob() {
      IAtomicValue av = channel.getValue();
      if( !av.equals( lastValue ) ) {
        valed.setValue( av );
        lastValue = av;
      }
    }

  }

  private final IRealTimeSensitive periodicalTimerHandler = aGwTime -> {
    for( RtdItem item : this.rtdItems ) {
      item.doJob();
    }
  };

  private final ISkConnection      skConn;
  private final IListEdit<RtdItem> rtdItems = new ElemArrayList<>();

  /**
   * Holds GWID->VALED binding until first start of this panel.
   * <p>
   * After start map will be set to <code>null</code>.
   */
  private IMapEdit<Gwid, IValedControl<IAtomicValue>> bindMap = new ElemMap<>();

  /**
   * Constructor.
   * <p>
   * Constructos stores reference to the context, does not creates copy.
   * <p>
   * Argument <code>aSkConnId</code> specifies which connection to use from {@link ISkConnectionSupplier}. If argument
   * vakue id {@link IdChain#NULL}, {@link ISkConnectionSupplier#defConn()} will be used. Note that connection is
   * defined at constructor call and does not changes after.
   *
   * @param aParent {@link Composite} - parent component
   * @param aContext {@link ITsGuiContext} - the context
   * @param aSkConnId {@link IdChain} - SK-connection ID or {@link IdChain#NULL} for default
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public RtValedsPanel( Composite aParent, ITsGuiContext aContext, IdChain aSkConnId ) {
    super( aParent, aContext, SWT.DOUBLE_BUFFERED );
    if( aSkConnId != IdChain.NULL ) {
      skConn = connectionSupplier().getConn( aSkConnId );
    }
    else {
      skConn = connectionSupplier().defConn();
    }
    guiTimersService().slowTimers().addListener( periodicalTimerHandler );
  }

  /**
   * Constructor.
   * <p>
   * Simply calls {@link RtValedsPanel#RtValedsPanel(Composite, ITsGuiContext, IdChain)} with {@link IdChain#NULL}.
   *
   * @param aParent {@link Composite} - parent component
   * @param aContext {@link ITsGuiContext} - the context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public RtValedsPanel( Composite aParent, ITsGuiContext aContext ) {
    this( aParent, aContext, IdChain.NULL );
    guiTimersService().slowTimers().addListener( periodicalTimerHandler );
    guiTimersService().slowTimers().muteListener( periodicalTimerHandler );
  }

  @Override
  protected void doDispose() {
    rtPause();
    guiTimersService().slowTimers().removeListener( periodicalTimerHandler );
  }

  // ------------------------------------------------------------------------------------
  // ISkGuiContextable
  //

  @Override
  public ISkConnection skConn() {
    return skConn;
  }

  // ------------------------------------------------------------------------------------
  // API for subclasses
  //

  /**
   * Defines VALED control to display value of the given RtData.
   *
   * @param aRtdGwid {@link Gwid} - RtData GWID
   * @param aControl {@link IValedControl}&lt;{@link IAtomicValue}&gt; - the VALED
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalStateRtException panel was already started
   * @throws TsIllegalArgumentRtException GWID is not of kind {@link EGwidKind#GW_RTDATA}
   * @throws TsIllegalArgumentRtException GWID is abstract
   */
  public void defineRtData( Gwid aRtdGwid, IValedControl<IAtomicValue> aControl ) {
    TsNullArgumentRtException.checkNulls( aRtdGwid, aControl );
    TsIllegalStateRtException.checkNull( bindMap );
    TsIllegalArgumentRtException.checkTrue( aRtdGwid.kind() != EGwidKind.GW_RTDATA );
    TsIllegalArgumentRtException.checkTrue( aRtdGwid.isAbstract() );
    bindMap.put( aRtdGwid, aControl );
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Starts the real-time update of VALEDs values.
   * <p>
   * Also this method is used to resume paused updates.
   */
  public void rtStart() {
    if( isRunning() ) {
      return;
    }
    // the first run?
    if( bindMap != null ) {
      // init #rtdItems on first start
      IMap<Gwid, ISkReadCurrDataChannel> map =
          skRtdataServ().createReadCurrDataChannels( new GwidList( bindMap.keys() ) );
      for( Gwid gwid : map.keys() ) {
        ISkReadCurrDataChannel channel = map.getByKey( gwid );
        IValedControl<IAtomicValue> valed = bindMap.getByKey( gwid );
        RtdItem item = new RtdItem( valed, channel );
        rtdItems.add( item );
      }
      bindMap = null; // reset unneeded GWID->VALED binds map
    }
    guiTimersService().slowTimers().unmuteListener( periodicalTimerHandler );
  }

  /**
   * Pauses started updates.
   * <p>
   * Has no effect on not yet started or already paused updates.
   */
  public void rtPause() {
    if( !isRunning() ) {
      return;
    }
    guiTimersService().slowTimers().muteListener( periodicalTimerHandler );
  }

  /**
   * Determines if realtime updates are started and running.
   *
   * @return <code>true</code> - <code>true</code> when VALEDs are updated in real-time now
   */
  public boolean isRunning() {
    return bindMap == null && !guiTimersService().slowTimers().isListenerMuted( periodicalTimerHandler );
  }

}
