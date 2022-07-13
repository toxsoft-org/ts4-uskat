package org.toxsoft.uskat.base.gui.e4.uiparts;

import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.dialogs.*;
import org.toxsoft.core.tsgui.mws.bases.*;
import org.toxsoft.core.tsgui.utils.layout.*;
import org.toxsoft.core.tsgui.widgets.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.logs.impl.*;
import org.toxsoft.uskat.base.gui.conn.*;
import org.toxsoft.uskat.base.gui.utils.*;
import org.toxsoft.uskat.core.connection.*;

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
   * Basement - a composite is created when constructing a uipart and exists until it is disposed.
   * <p>
   * It is needed in order not to remember the parent {@link Composite} for the uipart, since the E4 can change the
   * parent when dragging the uipart.
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
   * Uipart created with this constructor uses {@link ISkConnectionSupplier#defConn()} connection unless
   * {@link #skConn()} is overriden.
   */
  public SkMwsAbstractPart() {
    this( null );
  }

  /**
   * Specifies which connection to use from the {@link ISkConnectionSupplier}.
   * <p>
   * Uipart created with this constructor uses {@link ISkConnectionSupplier#getConn(IdChain)} connection unless
   * {@link #skConn()} is overriden.
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
    disposableBackplane = new TsComposite( basement, SWT.BORDER );
    disposableBackplane.setLayout( new BorderLayout() );
    disposableBackplane.setLayoutData( BorderLayout.CENTER );
    // попытка создать пользовательское содержимое
    try {
      doCreateContent( disposableBackplane );
    }
    catch( Exception ex ) {
      // в случае ошибки уничтожим пользовательское содержимое - не надо захламлять вью
      LoggerUtils.errorLogger().error( ex );
      TsDialogUtils.error( getShell(), ex );
      internalDisposeContent();
      return;
    }
    // если пользователь не стал создавать содержимое (по своим соображениям), подчистим подложку тоже
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
    // дадим возможность пользователю "подчистить" ресурсы
    try {
      doBeforeDisposeContent();
    }
    catch( Exception ex ) {
      LoggerUtils.errorLogger().error( ex );
    }
    // уничтожим визуальные компоненты
    try {
      disposableBackplane.dispose();
    }
    catch( Exception ex ) {
      LoggerUtils.errorLogger().error( ex );
    }
    // сброс в начальное состояние
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

      // FIXME create errorneous content
      throw new TsUnderDevelopmentRtException();
    }

    skConn.addConnectionListener( connectionListener );

    basement = new TsComposite( aParent, SWT.BORDER );
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
   * Subclass may override to implement other strategy but not that connecton must NOT be changed afetr part creation.
   */
  @Override
  public ISkConnection skConn() {
    if( suppliedConnectionId == null ) {
      return connectionSupplier().defConn();
    }
    return connectionSupplier().getConn( suppliedConnectionId );
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
   * Subclass may reklease resources allocated in {@link #doCreateContent(TsComposite)}, if any.
   * <p>
   * USkat connection is close at this moment.
   * <p>
   * There is no need to dispose SWT content of the view, it will be disposed by base class. Only additional reources,
   * if any were allocated, is needed to release here.
   * <p>
   * Does nothing in the base class, there is no need to call the superclass method when overriding.
   */
  protected void doBeforeDisposeContent() {
    // nop
  }

  /**
   * Subclass has abiltity to preform addition action afetr content is disposed.
   * <p>
   * USkat connection is close at this moment.
   * <p>
   * Does nothing in the base class, there is no need to call the superclass method when overriding.
   */
  protected void doAfterDisposeContent() {
    // nop
  }

}
