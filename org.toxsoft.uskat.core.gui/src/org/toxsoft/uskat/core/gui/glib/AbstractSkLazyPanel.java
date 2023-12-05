package org.toxsoft.uskat.core.gui.glib;

import java.util.*;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.panels.lazy.*;
import org.toxsoft.core.tsgui.utils.checkstate.*;
import org.toxsoft.core.tsgui.utils.layout.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * {@link AbstractLazyPanel} extension to work with USkat API.
 * <p>
 * The panel content must be created in {@link #doInitGui(Composite)}. This method is called when connection is or
 * becomes open. Panel implementation handles connection reference and connsction state change. When connection closes
 * or reference to the connection changes to other connection, content is disposed by calling internal method
 * {@link #disposeBoardContent()} and after, when connection becames active {@link #doInitGui(Composite)} is called
 * again.
 * <p>
 * Features:
 * <ul>
 * <li>implements {@link ISkConnected};</li>
 * <li>supports used connection choosing by specifying {@link IdChain} key for {@link ISkConnectionSupplier};</li>
 * <li>overridden {@link #m5()} returns {@link IM5Domain} from {@link ISkConnection#scope() skConn().scope()} rather
 * than root domain from the application context.</li>
 * </ul>
 * By default connection with ID {@link IdChain#NULL} is used that is default connection
 * {@link ISkConnectionSupplier#defConn()}.
 *
 * @author hazard157
 */
public abstract class AbstractSkLazyPanel
    extends AbstractLazyPanel<Control>
    implements ISkConnected {

  /**
   * Detects if our {@link #skConn()} reference changed and call {@link #whenConnectionReferenceChanged(ISkConnection)}.
   */
  private final ISkConnectionSupplierListener connectionSupplierListener = ( aSource, aOp, aConnId ) -> {
    if( aConnId == null || aConnId.equals( this.usedConnId ) ) {
      whenConnectionReferenceChanged( this.lastConnection );
    }
  };

  /**
   * Calls {@link #whenConnectionStateChanged(ESkConnState)} on connection state change.
   */
  private final ISkConnectionListener connectionListener =
      ( aSource, aOldState ) -> whenConnectionStateChanged( aOldState );

  private final ISkConnectionSupplier connSupplier;

  /**
   * Connection that was last returned by {@link #skConn()}.
   */
  private ISkConnection lastConnection = null;

  /**
   * ID of the {@link ISkConnection} to be supplied by {@link ISkConnectionSupplier}.
   * <p>
   * The value of <code>null</code> means to use {@link ISkConnectionSupplier#defConn()}, any other value calls
   * {@link ISkConnectionSupplier#getConn(IdChain)}.
   */
  private IdChain usedConnId = null;

  /**
   * Constructor.
   * <p>
   * Used connection ID is initialized to <code>null</code> thus using {@link ISkConnectionSupplier#defConn()}.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public AbstractSkLazyPanel( ITsGuiContext aContext ) {
    this( aContext, null );
  }

  /**
   * Constructor.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aUsedConnId {@link IdChain} - ID of connection to be used, may be <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public AbstractSkLazyPanel( ITsGuiContext aContext, IdChain aUsedConnId ) {
    super( aContext );
    connSupplier = tsContext().get( ISkConnectionSupplier.class );
    usedConnId = aUsedConnId;
    skConn(); // update #lastConnection
  }

  // ------------------------------------------------------------------------------------
  // AbstractLazyPanel
  //

  @Override
  final protected Composite doCreateControl( Composite aParent ) {
    Composite board = new Composite( aParent, SWT.NONE );
    board.setLayout( new BorderLayout() );
    setControl( board );
    connSupplier.eventer().addListener( connectionSupplierListener );
    skConn().addConnectionListener( connectionListener );
    if( skConn().state().isOpen() ) {
      doInitGui( board );
    }
    return board;
  }

  @Override
  protected void doDispose() {
    skConn().removeConnectionListener( connectionListener );
    connSupplier.eventer().removeListener( connectionSupplierListener );
    doDisposeGui();
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    ISkConnection currConn;
    if( usedConnId == null ) {
      currConn = connSupplier.defConn();
    }
    else {
      currConn = connSupplier.getConn( usedConnId );
    }
    // #lastConnection is null only on first call of #skConn()
    if( lastConnection == null ) {
      lastConnection = currConn;
    }
    if( currConn != lastConnection ) {
      // if happens it means that not all cases of connection reference chnge were processed!
      throw new TsInternalErrorRtException();
    }
    lastConnection = currConn;
    return lastConnection;
  }

  // ------------------------------------------------------------------------------------
  // ITsGuiContextable
  //

  @Override
  public IM5Domain m5() {
    return skConn().scope().get( IM5Domain.class );
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Handles change of {@link #skConn()} reference.
   *
   * @param aOldConn {@link ISkConnection} - previously used reference or <code>null</code>
   */
  final void whenConnectionReferenceChanged( ISkConnection aOldConn ) {
    if( !isControlValid() ) {
      return;
    }
    if( aOldConn != null ) {
      aOldConn.removeConnectionListener( connectionListener );
      disposeBoardContent();
    }
    ISkConnection newConn = skConn();
    if( newConn != null ) {
      newConn.addConnectionListener( connectionListener );
      if( newConn.state().isOpen() ) {
        doInitGui( getBackplane() );
      }
    }
  }

  /**
   * Handles connection state change when reference to the connection does NOT changes.
   * <p>
   * If connection open stat {@link ESkConnState#isOpen()} changes, creates/disposes panel content.
   *
   * @param aOldState {@link ECheckState} - state of the connection before change
   */
  final void whenConnectionStateChanged( ESkConnState aOldState ) {
    boolean wasOpen = aOldState.isOpen();
    boolean nowOpen = skConn().state().isOpen();
    if( wasOpen != nowOpen ) {
      if( nowOpen ) {
        doInitGui( getBackplane() );
      }
      else {
        disposeBoardContent();
      }
    }
  }

  private void disposeBoardContent() {
    if( !isControlValid() ) {
      return;
    }
    // dispose all childs of this panel
    getBackplane().setLayoutDeferred( true );
    try {
      doDisposeGui();
      Control[] childs = getBackplane().getChildren();
      for( Control c : childs ) {
        c.dispose();
      }
    }
    finally {
      getBackplane().setLayoutDeferred( false );
      getBackplane().getParent().layout( true, true );
    }
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

  /**
   * Sets used connection ID {@link #getUsedConnectionId()}.
   *
   * @param aConnId {@link IdChain} - onnection ID or <code>null</code> for {@link ISkConnectionSupplier#defConn()}
   */
  public void setUsedConnectionId( IdChain aConnId ) {
    if( !Objects.equals( aConnId, usedConnId ) ) {
      skConn(); // refresh #lastConnection
      usedConnId = aConnId;
      whenConnectionReferenceChanged( lastConnection );
    }
  }

  // ------------------------------------------------------------------------------------
  // API for subclasses
  //

  /**
   * Returns the backplane - permanent {@link Composite} used as lazy panel implementation.
   * <p>
   * Returns the same reference as {@link #getControl()}, but casted to the {@link Composite}.
   *
   * @return {@link Composite} - the backplane, lazy control implementing SWT control
   */
  public Composite getBackplane() {
    return Composite.class.cast( getControl() );
  }

  /**
   * Determines if panel content exists.
   * <p>
   * Note: {@link #getControl()} after calling {@link #createControl(Composite)} always returns non-<code>null</code>
   * reference to the permanent backplane. That's why this method determines if user created content exists on
   * backplane.
   *
   * @return boolean - <code>true</code> if user-created content exists (is not disposed)
   */
  public boolean isPanelContent() {
    if( getControl() == null ) {
      return false;
    }
    return getBackplane().getChildren().length > 0;
  }

  // ------------------------------------------------------------------------------------
  // to override
  //

  /**
   * Subclass must create content of this panel.
   * <p>
   * Parent composite <code>aParent</code> has layout set to {@link BorderLayout}.
   *
   * @param aParent {@link Composite} - the parent
   */
  protected abstract void doInitGui( Composite aParent );

  /**
   * Subclass must release resource and reset internal references indicating that no content exists.
   * <p>
   * Called when GUI content of the panel is disposed. Also called when this panel is disposed.
   */
  protected void doDisposeGui() {
    // nop
  }

}
