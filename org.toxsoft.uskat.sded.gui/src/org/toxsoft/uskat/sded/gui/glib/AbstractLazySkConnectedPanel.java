package org.toxsoft.uskat.sded.gui.glib;

import org.eclipse.swt.widgets.*;
import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tsgui.panels.lazy.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.uskat.base.gui.conn.*;
import org.toxsoft.uskat.base.gui.utils.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Extends {@link AbstractLazyPanel} with Sk-connection specified by {@link IdChain} ID.
 * <p>
 * Panel will use {@link ISkConnection} with the given ID from {@link ISkConnectionSupplier#getConn(IdChain)}. If
 * constructor argument <code>aSuppliedConnectionId</code> = <code>null</code>, then
 * {@link ISkConnectionSupplier#defConn()} will be used.
 * <p>
 * Note: it is impossible to change connection, re-create this panel to use with other connection.
 *
 * @author hazard157
 * @param <C> - type of the impementin SWT control
 */
public abstract class AbstractLazySkConnectedPanel<C extends Control>
    extends AbstractLazyPanel<C>
    implements ISkGuiContextable {

  private final IdChain       suppliedConnId;
  private final ISkConnection skConn;

  /**
   * Constrcutor.
   * <p>
   * Panel will use {@link ISkConnection} with the given ID from {@link ISkConnectionSupplier#getConn(IdChain)}. If
   * <code>aSuppliedConnectionId</code> = <code>null</code>, then {@link ISkConnectionSupplier#defConn()} will be used.
   *
   * @param aContext {@link ITsGuiContext} - the context
   * @param aSuppliedConnectionId {@link IdChain} - ID of connection or <code>null</code> for default
   */
  public AbstractLazySkConnectedPanel( ITsGuiContext aContext, IdChain aSuppliedConnectionId ) {
    super( aContext );
    // init skConn
    suppliedConnId = aSuppliedConnectionId;
    if( suppliedConnId != null ) {
      skConn = connectionSupplier().getConn( suppliedConnId );
    }
    else {
      skConn = connectionSupplier().defConn();
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkGuiContextable
  //

  @Override
  final public ISkConnection skConn() {
    return skConn;
  }

}
