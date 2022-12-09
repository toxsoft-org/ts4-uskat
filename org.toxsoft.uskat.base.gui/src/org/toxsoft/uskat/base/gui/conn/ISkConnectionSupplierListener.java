package org.toxsoft.uskat.base.gui.conn;

import org.toxsoft.core.tsgui.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.coll.helpers.*;

/**
 * Listsner to changes in {@link ISkConnectionSupplier}.
 * <p>
 * Note: listens to the supplier events, not the connection state change events.
 *
 * @author hazard157
 */
public interface ISkConnectionSupplierListener {

  /**
   * Called when connections map {@link ISkConnectionSupplier#allConns()} changes.
   * <p>
   * Method is classed in cases:
   * <ul>
   * <li>{@link ISkConnectionSupplier#createConnection(IdChain, ITsGuiContext)
   * createConnection(<b>aConnId</b>,aContext)} - calls this method with arguments
   * {@link #onConnectionsListChanged(ISkConnectionSupplier, ECrudOp, IdChain) onConnectionsListChanged( source,
   * <b>CREATE</b>, <b>aConnId</b> )};</li>
   * <li>{@link ISkConnectionSupplier#removeConnection(IdChain) removeConnection(<b>aConnId</b>)} calls this method with
   * arguments {@link #onConnectionsListChanged(ISkConnectionSupplier, ECrudOp, IdChain) onConnectionsListChanged(
   * source, <b>REMOVE</b>, <b>aConnId</b> )};</li>
   * <li>{@link ISkConnectionSupplier#close()} - calls with arguments
   * {@link #onConnectionsListChanged(ISkConnectionSupplier, ECrudOp, IdChain) onConnectionsListChanged( source,
   * <b>LIST</b>, <b>null</b> )};</li>
   * </ul>
   *
   * @param aSource {@link ISkConnectionSupplier} - the event source
   * @param aOp {@link ECrudOp} - change kind
   * @param aConnId {@link IdChain} - related connection id or <code>null</code> for {@link ECrudOp#LIST}
   */
  void onConnectionsListChanged( ISkConnectionSupplier aSource, ECrudOp aOp, IdChain aConnId );

  /**
   * Called when default connection {@link ISkConnectionSupplier#defConn()} changes.
   *
   * @param aSource {@link ISkConnectionSupplier} - the event source
   * @param aOldId {@link IdChain} - ID of the default connection vefore change
   */
  void onDefaulConnectionChanged( ISkConnectionSupplier aSource, IdChain aOldId );

}
