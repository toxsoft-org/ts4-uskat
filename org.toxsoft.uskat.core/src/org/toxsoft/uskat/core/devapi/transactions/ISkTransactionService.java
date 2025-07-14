package org.toxsoft.uskat.core.devapi.transactions;

import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.linkserv.*;

/**
 * Transaction service (batch operations)
 *
 * @author mvk
 */
public interface ISkTransactionService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".Transaction"; //$NON-NLS-1$

  /**
   * Returns transaction status.
   *
   * @return boolean <b>true</b> transaction is active; <b>false</b> transaction is not active</b>.
   */
  boolean isActive();

  /**
   * Start a transaction.
   *
   * @throws TsIllegalStateRtException transaction is already active
   */
  void start();

  /**
   * Returns transaction's object manager.
   *
   * @return {@link IDtoObjectManager} object manager
   * @throws TsIllegalStateRtException transaction is not active
   */
  IDtoObjectManager objectManager();

  /**
   * Returns transaction's rivet object manager.
   *
   * @return {@link IDtoObjectManager} object manager
   * @throws TsIllegalStateRtException transaction is not active
   */
  IDtoObjectRivetManager rivetManager();

  /**
   * Add updated links.
   *
   * @param aLinks IList&lt; {@link IDtoLinkFwd}&gt; updated links
   * @throws TsIllegalStateRtException transaction is not active
   */
  void defineLinks( IList<IDtoLinkFwd> aLinks );

  /**
   * Transaction commit.
   *
   * @throws TsIllegalStateRtException transaction is not active
   */
  void commit();

  /**
   * Transaction rollback.
   */
  void rollback();

  /**
   * Returns the service eventer.
   *
   * @return {@link ITsEventer}&lt;{@link ISkTransactionServiceListener}&gt; - the service eventer
   */
  ITsEventer<ISkTransactionServiceListener> eventer();

}
