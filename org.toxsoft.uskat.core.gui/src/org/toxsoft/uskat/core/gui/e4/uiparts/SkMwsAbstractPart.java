package org.toxsoft.uskat.core.gui.e4.uiparts;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.dialogs.*;
import org.toxsoft.core.tsgui.m5.*;
import org.toxsoft.core.tsgui.mws.bases.*;
import org.toxsoft.core.tsgui.utils.layout.*;
import org.toxsoft.core.tsgui.widgets.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.utils.*;

/**
 * Base class for all viewes with content related to the USkat.
 * <p>
 * This part does <b>not</b> support changin connection after part creation!
 *
 * @author hazard157
 */
public abstract class SkMwsAbstractPart
    extends MwsAbstractPart
    implements ISkGuiContextable {

  /**
   * Create/destroy visual content as the connection opens/closes.
   */
  private final ISkConnectionListener connectionListener = ( aSource, aOldState ) -> {
    switch( aSource.state() ) {
      case ACTIVE: {
        try {
          internalCreateContent();
        }
        catch( Exception ex1 ) {
          LoggerUtils.errorLogger().error( ex1 );
        }
        break;
      }
      case INACTIVE:
      case CLOSED: {
        try {
          internalDisposeContent();
        }
        catch( Exception ex2 ) {
          LoggerUtils.errorLogger().error( ex2 );
        }
        break;
      }
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
  };

  private final IdChain suppliedConnectionId;

  /**
   * Basement - a composite is created when constructing a UIpart and exists until it is disposed.
   * <p>
   * It is needed in order not to remember the parent {@link Composite} for the UIpart, since the E4 can change the
   * parent when dragging the UIpart.
   */
  TsComposite basement = null;

  /**
   * Backplane for content - is the only child of {@link #basement}.
   * <p>
   * The backplane is created and destroyed along with custom content in the {@link #internalCreateContent()} and
   * {@link #internalDisposeContent()} methods, respectively.
   */
  TsComposite disposableBackplane = null;

  /**
   * Constructor.
   * <p>
   * UIpart created with this constructor uses {@link ISkConnectionSupplier#defConn()} connection unless
   * {@link #skConn()} is overridden.
   */
  public SkMwsAbstractPart() {
    this( null );
  }

  /**
   * Specifies which connection to use from the {@link ISkConnectionSupplier}.
   * <p>
   * UIpart created with this constructor uses {@link ISkConnectionSupplier#getConn(IdChain)} connection unless
   * {@link #skConn()} is overridden.
   *
   * @param aSuppliedConnectionId {@link IdChain} - connection ID or <code>null</code> for default
   */
  public SkMwsAbstractPart( IdChain aSuppliedConnectionId ) {
    suppliedConnectionId = aSuppliedConnectionId;
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  private void internalCreateContent() {
    if( basement.isDisposed() || disposableBackplane != null ) {
      return;
    }
    disposableBackplane = new TsComposite( basement, SWT.NONE );
    disposableBackplane.setLayout( new BorderLayout() );
    disposableBackplane.setLayoutData( BorderLayout.CENTER );
    // trying to create custom content
    try {
      doCreateContent( disposableBackplane );
    }
    catch( Exception ex ) {
      // in case of an error, we will destroy the user content - no need to clutter up the UIpart
      LoggerUtils.errorLogger().error( ex );
      TsDialogUtils.error( getShell(), ex );
      internalDisposeContent();
      return;
    }
    // if the user did not create content (for his own reasons), we will clean the background too
    if( disposableBackplane.getChildren().length == 0 ) {
      internalDisposeContent();
      return;
    }
    basement.layout( true, true );
  }

  private void internalDisposeContent() {
    if( basement.isDisposed() || disposableBackplane == null ) {
      return;
    }
    // clean up resources
    try {
      doBeforeDisposeContent();
    }
    catch( Exception ex ) {
      LoggerUtils.errorLogger().error( ex );
    }
    // destroy visual components
    try {
      disposableBackplane.dispose();
    }
    catch( Exception ex ) {
      LoggerUtils.errorLogger().error( ex );
    }
    // reset to initial state
    try {
      doAfterDisposeContent();
    }
    catch( Exception ex ) {
      LoggerUtils.errorLogger().error( ex );
    }
    disposableBackplane = null;
    basement.layout( true, true );
  }

  // ------------------------------------------------------------------------------------
  // MwsAbstractPart
  //

  @Override
  final protected void doInit( Composite aParent ) {
    ISkConnection skConn;
    try {
      skConn = skConn();
    }
    catch( Exception ex1 ) {
      LoggerUtils.errorLogger().error( ex1 );

      // FIXME create content displaying the error ?
      throw new TsUnderDevelopmentRtException();
    }

    skConn.addConnectionListener( connectionListener );

    basement = new TsComposite( aParent, SWT.NONE );
    basement.setLayout( new BorderLayout() );

    if( skConn.state() == ESkConnState.ACTIVE ) {
      try {
        internalCreateContent();
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }

  }

  // ------------------------------------------------------------------------------------
  // ISkGuiContextable
  //

  /**
   * Returns connection by ID specified in constructor.
   * <p>
   * Subclass may override to implement other strategy but not that connector must NOT be changed after part creation.
   */
  @Override
  public ISkConnection skConn() {
    if( suppliedConnectionId == null ) {
      return connectionSupplier().defConn();
    }
    return connectionSupplier().getConn( suppliedConnectionId );
  }

  @Override
  public IM5Domain m5() {
    return skConn().scope().get( IM5Domain.class );
  }

  // ------------------------------------------------------------------------------------
  // To override & implement
  //

  /**
   * The subclass must create the contents of the view.
   * <p>
   * USkat connection is active at this moment.
   * <p>
   * <code>aParent</code> is a {@link TsComposite} with layout set to {@link BorderLayout}. Subclass may change layout
   * if needed.
   * <p>
   * Each time this method is called, <code>aParent</code> is a new instance.
   *
   * @param aParent {@link TsComposite} - the parent fopr the uipart content
   */
  protected abstract void doCreateContent( TsComposite aParent );

  /**
   * Subclass may release resources allocated in {@link #doCreateContent(TsComposite)}, if any.
   * <p>
   * USkat connection is close at this moment.
   * <p>
   * There is no need to dispose SWT content of the view, it will be disposed by base class. Only additional resources,
   * if any were allocated, is needed to release here.
   * <p>
   * Does nothing in the base class, there is no need to call the superclass method when overriding.
   */
  protected void doBeforeDisposeContent() {
    // nop
  }

  /**
   * Subclass has ability to perform addition action after content is disposed.
   * <p>
   * USkat connection is close at this moment.
   * <p>
   * Does nothing in the base class, there is no need to call the superclass method when overriding.
   */
  protected void doAfterDisposeContent() {
    // nop
  }

}
