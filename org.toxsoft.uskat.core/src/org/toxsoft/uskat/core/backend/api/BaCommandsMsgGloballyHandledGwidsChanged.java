package org.toxsoft.uskat.core.backend.api;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

/**
 * {@link IBaCommands} message builder: globally handled GWIDs list changed.
 *
 * @author hazard157
 */
public class BaCommandsMsgGloballyHandledGwidsChanged
    extends AbstractBackendMessageBuilder {

  /**
   * ID of the message.
   */
  public static final String MSG_ID = "GloballyHandledGwidsChanged"; //$NON-NLS-1$

  /**
   * Singletone intance.
   */
  public static final BaCommandsMsgGloballyHandledGwidsChanged INSTANCE =
      new BaCommandsMsgGloballyHandledGwidsChanged();

  BaCommandsMsgGloballyHandledGwidsChanged() {
    super( ISkCommandService.SERVICE_ID, MSG_ID );
  }

  /**
   * Creates the message instance.
   *
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessage() {
    return makeMessageVarargs();
  }

}
