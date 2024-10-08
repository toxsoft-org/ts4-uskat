package org.toxsoft.uskat.core.gui.glib;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.panels.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * {@link TsPanel} extension to work with USkat API.
 * <p>
 * The connection to use is specified in constructor and can not be changed.
 *
 * @author hazard157
 */
public class SkPanel
    extends TsPanel
    implements ISkConnected {

  private final ISkConnectionSupplier connectionSupplier;
  private final IdChain               usedConnId;

  /**
   * Constructor using {@link ISkConnectionSupplier#defConn()} as a connection.
   * <p>
   * Constructor stores reference to the context, does not creates copy.
   *
   * @param aParent {@link Composite} - parent component
   * @param aContext {@link ITsGuiContext} - the context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkPanel( Composite aParent, ITsGuiContext aContext ) {
    this( aParent, aContext, ISkConnectionSupplier.DEF_CONN_ID );
  }

  /**
   * Constructor using {@link ISkConnectionSupplier#defConn()} as a connection.
   * <p>
   * Constructor stores reference to the context, does not creates copy.
   *
   * @param aParent {@link Composite} - parent component
   * @param aContext {@link ITsGuiContext} - the context
   * @param aStyle int - SWT style
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkPanel( Composite aParent, ITsGuiContext aContext, int aStyle ) {
    this( aParent, aContext, ISkConnectionSupplier.DEF_CONN_ID, aStyle );
  }

  /**
   * Constructor.
   * <p>
   * Constructor stores reference to the context, does not creates copy.
   * <p>
   * The connection ID is the key to get connection from the {@link ISkConnectionSupplier#allConns()} map.
   *
   * @param aParent {@link Composite} - parent component
   * @param aContext {@link ITsGuiContext} - the context
   * @param aUsedConnId {@link IdChain} - ID of connection to be used
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkPanel( Composite aParent, ITsGuiContext aContext, IdChain aUsedConnId ) {
    super( aParent, aContext );
    usedConnId = TsNullArgumentRtException.checkNull( aUsedConnId );
    connectionSupplier = tsContext().get( ISkConnectionSupplier.class );
  }

  /**
   * Constructor.
   * <p>
   * Constructor stores reference to the context, does not creates copy.
   * <p>
   * The connection ID is the key to get connection from the {@link ISkConnectionSupplier#allConns()} map.
   *
   * @param aParent {@link Composite} - parent component
   * @param aContext {@link ITsGuiContext} - the context
   * @param aUsedConnId {@link IdChain} - ID of connection to be used
   * @param aStyle int - SWT style
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkPanel( Composite aParent, ITsGuiContext aContext, IdChain aUsedConnId, int aStyle ) {
    super( aParent, aContext, aStyle );
    usedConnId = TsNullArgumentRtException.checkNull( aUsedConnId );
    connectionSupplier = tsContext().get( ISkConnectionSupplier.class );
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    return connectionSupplier.getConn( usedConnId );
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
   * @return {@link IdChain} - used connection ID or <code>null</code> for {@link ISkConnectionSupplier#defConn()}
   */
  public IdChain getUsedConnectionId() {
    return usedConnId;
  }

}
