package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * {@link ISkBackend} message builder: backend state change notification.
 *
 * @author mvk
 */
public class BackendMsgStateChanged
    extends AbstractBackendMessageBuilder {

  /**
   * ID of the message.
   */
  public static final String MSG_ID = "StateChanged"; //$NON-NLS-1$

  /**
   * Singletone intance.
   */
  public static final BackendMsgStateChanged INSTANCE = new BackendMsgStateChanged();

  private static final String ARGID_STATE = "State"; //$NON-NLS-1$

  BackendMsgStateChanged() {
    super( ISkHardConstants.SK_CORE_ID, MSG_ID );
    defineArgValobj( ARGID_STATE, EAtomicType.KEEPER_ID, true );
  }

  /**
   * Creates the message instance.
   *
   * @param aState {@link ESkConnState} - current state
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessage( ESkConnState aState ) {
    return makeMessageVarargs( ARGID_STATE, avValobj( aState ) );
  }

  /**
   * Extracts {@link DtoCommandStateChangeInfo} argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link DtoCommandStateChangeInfo} - argument extracted from the message
   */
  public ESkConnState getState( GenericMessage aMsg ) {
    return getArg( aMsg, ARGID_STATE ).asValobj();
  }

}
