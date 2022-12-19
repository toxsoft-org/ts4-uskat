package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * {@link IBaCommands} message builder: execute command.
 *
 * @author hazard157
 */
public class BaMsgCommandsExecCmd
    extends AbstractBackendMessageBuilder {

  /**
   * ID of the message.
   */
  public static final String MSG_ID = "ExecCmd"; //$NON-NLS-1$

  /**
   * Singletone instance.
   */
  public static final BaMsgCommandsExecCmd INSTANCE = new BaMsgCommandsExecCmd();

  private static final String ARGID_CMD_DTO = "CmdDto"; //$NON-NLS-1$

  BaMsgCommandsExecCmd() {
    super( ISkCommandService.SERVICE_ID, MSG_ID );
    defineArgValobj( ARGID_CMD_DTO, DtoCommand.KEEPER_ID, true );
  }

  /**
   * Creates the message instance.
   *
   * @param aCmd {@link IDtoCommand} - command to execute
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessage( IDtoCommand aCmd ) {
    return makeMessageVarargs( ARGID_CMD_DTO, avValobj( aCmd ) );
  }

  /**
   * Extracts {@link IDtoCommand} argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link IDtoCommand} - argument extracted from the message
   */
  public IDtoCommand getCmd( GenericMessage aMsg ) {
    return getArg( aMsg, ARGID_CMD_DTO ).asValobj();
  }

}
