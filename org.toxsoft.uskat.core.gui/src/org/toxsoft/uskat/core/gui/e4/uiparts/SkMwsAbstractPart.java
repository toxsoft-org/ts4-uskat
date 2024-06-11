package org.toxsoft.uskat.core.gui.e4.uiparts;

import static org.toxsoft.uskat.core.gui.ISkCoreGuiConstants.*;

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
import org.toxsoft.uskat.core.gui.*;
import org.toxsoft.uskat.core.gui.conn.*;
import org.toxsoft.uskat.core.gui.utils.*;

/**
 * Base class for all views with content related to the USkat.
 * <p>
 * Main idea is the GUI content exists only if specified Sk-connecion is open. If connection closes this base
 * implementation disposes GUI content. So the subclass can be written under the assumption that the connection is
 * always open. Just need to make sure that creation {@link #doCreateContent(TsComposite)} and diposal
 * {@link #doAfterDisposeContent()}/{@link #doBeforeDisposeContent()} methods can be called multiple times as connection
 * opens/closes.
 * <p>
 * This part does <b>not</b> support connection change after part creation!
 *
 * @author hazard157
 */
public abstract class SkMwsAbstractPart
    extends MwsAbstractPart
    implements ISkGuiContextable, ISkConnectionListener {

  /**
   * Sk-connection key memorized in {@link #SkMwsAbstractPart(IdChain)} until {@link #doInit(Composite)}.
   * <p>
   * Value <code>null</code> means that constructor does not specified
   */
  private final IdChain skConnKeyMemorizedInConstructor;

  /**
   * The connection used by the UIpart.
   * <p>
   * Connection is initialized by t
   */
  private ISkConnection skConn = null;

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
   * UIpart created with this constructor uses connection specified by
   * {@link ISkCoreGuiConstants#REFDEF_SUPPLIED_SK_CONN_ID}.
   */
  public SkMwsAbstractPart() {
    this( null );
  }

  /**
   * Specifies which connection to use from the {@link ISkConnectionSupplier}.
   * <p>
   * For non-<code>null</code> argument the reference {@link ISkCoreGuiConstants#REFDEF_SUPPLIED_SK_CONN_ID} is set to
   * the {@link #partContext()}, so all content of the UIpart will use the specified connection key.
   *
   * @param aSkConnKey {@link IdChain} - connection ID or <code>null</code> to use key that is already in the context
   */
  public SkMwsAbstractPart( IdChain aSkConnKey ) {
    skConnKeyMemorizedInConstructor = aSkConnKey;
  }

  // ------------------------------------------------------------------------------------
  // ISkConnectionListener
  //

  @Override
  public void onSkConnectionStateChanged( ISkConnection aSource, ESkConnState aOldState ) {
    boolean contentExists = disposableBackplane != null;
    // ensure that for ACTIVE connection content is created
    switch( aSource.state() ) {
      case ACTIVE: {
        if( !contentExists ) {
          internalCreateContent();
        }
        break;
      }
      case CLOSED: {
        if( contentExists ) {
          internalDisposeContent();
        }
        break;
      }
      case INACTIVE: {
        // do nothing: here we have just ACTIVE->INACTIVE state change
        break;
      }
      default:
        throw new TsNotAllEnumsUsedRtException( aSource.state().id() );
    }
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
    // apply connection key if specified in constructor
    if( skConnKeyMemorizedInConstructor != null ) {
      REFDEF_SUPPLIED_SK_CONN_ID.setRef( partContext(), skConnKeyMemorizedInConstructor );
    }
    // initialize #skConn field
    IdChain connKey = REFDEF_SUPPLIED_SK_CONN_ID.getRef( partContext() );
    ISkConnectionSupplier connectionSupplier = partContext().get( ISkConnectionSupplier.class );
    TsInternalErrorRtException.checkNull( connectionSupplier );
    skConn = connectionSupplier.getConn( connKey );
    skConn.addConnectionListener( this );
    // create GUI basement
    basement = new TsComposite( aParent, SWT.NONE );
    basement.setLayout( new BorderLayout() );
    // if connection is already active - create GUI, otherwise connection listener will create GUI
    if( skConn.state() == ESkConnState.ACTIVE ) {
      try {
        internalCreateContent();
      }
      catch( Exception ex ) {
        LoggerUtils.errorLogger().error( ex );
      }
    }
    // remove connection listener when UIpart is disposed
    basement.addDisposeListener( e -> skConn.removeConnectionListener( this ) );
  }

  // ------------------------------------------------------------------------------------
  // ISkGuiContextable
  //

  @Override
  final public ISkConnection skConn() {
    TsInternalErrorRtException.checkNull( skConn );
    TsInternalErrorRtException.checkFalse( skConn.state().isOpen() );
    return skConn;
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
