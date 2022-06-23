package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

/**
 * {@link IBaCommands} message builder: command state change notification.
 *
 * @author hazard157
 */
public class BaCommandsMsgChangeState
    extends AbstractBackendMessageBuilder {

  /**
   * ID of the message.
   */
  public static final String MSG_ID = "ChangeState"; //$NON-NLS-1$

  /**
   * Singletone intance.
   */
  public static final BaCommandsMsgChangeState INSTANCE = new BaCommandsMsgChangeState();

  private static final String ARGID_CMD_STATE_INFO = "CmdStateInfo"; //$NON-NLS-1$

  BaCommandsMsgChangeState() {
    super( ISkCommandService.SERVICE_ID, MSG_ID );
    defineArgValobj( ARGID_CMD_STATE_INFO, DtoCommandStateChangeInfo.KEEPER_ID, true );
  }

  /**
   * Creates the message instance.
   *
   * @param aStateChangeInfo {@link IDtoCommand} - state change info
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessage( DtoCommandStateChangeInfo aStateChangeInfo ) {
    return makeMessageVarargs( ARGID_CMD_STATE_INFO, avValobj( aStateChangeInfo ) );
  }

  /**
   * Extracts {@link DtoCommandStateChangeInfo} argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link DtoCommandStateChangeInfo} - argument extracted from the message
   */
  public DtoCommandStateChangeInfo getStateChangeInfo( GenericMessage aMsg ) {
    return getArg( aMsg, ARGID_CMD_STATE_INFO ).asValobj();
  }

}
