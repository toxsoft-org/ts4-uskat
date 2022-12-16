package org.toxsoft.uskat.base.gui.glib;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.panels.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.base.gui.conn.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * {@link TsPanel} extension to work with USkat API.
 * <p>
 * Features:
 * <ul>
 * <li>implements {@link ISkConnected};</li>
 * <li>supports used connection choosing by specifying {@link IdChain} key for {@link ISkConnectionSupplier};</li>
 * <li>overriden {@link #m5()} returns {@link IM5Domain} from {@link ISkConnection#scope() skConn().scope()} rather than
 * root domain from the application context.</li>
 * </ul>
 * By default connection with ID {@link IdChain#NULL} is used that is default connection
 * {@link ISkConnectionSupplier#defConn()}.
 *
 * @author hazard157
 */
public class SkPanel
    extends TsPanel
    implements ISkConnected {

  /**
   * FIXME class must be finished:<br>
   * - listent to the connection and/or ref change and call onSkConnectionXxxChanged()
   */

  private final ISkConnectionSupplier connSupplier;

  /**
   * ID of the {@link ISkConnection} to be supplied by {@link ISkConnectionSupplier}.
   * <p>
   * The value of <code>null</code> means to use {@link ISkConnectionSupplier#defConn()}, any other value calls
   * {@link ISkConnectionSupplier#getConn(IdChain)}.
   */
  private IdChain usedConnId = null;

  /**
   * Constructor.
   *
   * @param aParent {@link Composite} - parent component
   * @param aContext {@link ITsGuiContext} - the context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkPanel( Composite aParent, ITsGuiContext aContext ) {
    this( aParent, aContext, null );
  }

  /**
   * Constructor.
   *
   * @param aParent {@link Composite} - parent component
   * @param aContext {@link ITsGuiContext} - the context
   * @param aUsedConnId {@link IdChain} - ID of connection to be used, may be <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkPanel( Composite aParent, ITsGuiContext aContext, IdChain aUsedConnId ) {
    super( aParent, aContext );
    connSupplier = tsContext().get( ISkConnectionSupplier.class );
    usedConnId = aUsedConnId;
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    if( usedConnId == null ) {
      return connSupplier.defConn();
    }
    return connSupplier.getConn( usedConnId );
  }

  // ------------------------------------------------------------------------------------
  // ITsGuiContextable
  //

  @Override
  public IM5Domain m5() {
    return skConn().scope().get( IM5Domain.class );
  }

  // ------------------------------------------------------------------------------------
  // class API
  //

  /**
   * Returns the ID of the used connection.
   * <p>
   * Every time when {@link #skConn()} is called, the connection with this ID is retreived by
   * {@link ISkConnectionSupplier#getConn(IdChain)}. If connection ID is <code>null</code> then
   * {@link ISkConnectionSupplier#defConn()} is returned.
   *
   * @return {@link IdChain} - used onnection ID or <code>null</code> for {@link ISkConnectionSupplier#defConn()}
   */
  public IdChain getUsedConnectionId() {
    return usedConnId;
  }

  /**
   * Sets used connection ID {@link #getUsedConnectionId()}.
   *
   * @param aConnId {@link IdChain} - onnection ID or <code>null</code> for {@link ISkConnectionSupplier#defConn()}
   */
  public void setUsedConnectionId( IdChain aConnId ) {
    usedConnId = aConnId;
    // FIXME inform about connection change!
  }

  // ------------------------------------------------------------------------------------
  // to override
  //

  protected void onSkConnectionStateChanged() {
    // TODO SkPanel.onConnectionStateChanged()
  }

  protected void onSkConnectionReferenceChanged() {
    // TODO SkPanel.onConnectionStateChanged()
  }

}
