package org.toxsoft.uskat.core.api.cmdserv;

import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Interface of command executor.
 * <p>
 * In order to receive the commands intended for him, the executor must be registered in the service by method
 * {@link ISkCommandService#registerExecutor(ISkCommandExecutor, IGwidList)}.
 * <p>
 * Each executor is responsible for own set of objects and commands. Normally all the executors registered in the system
 * shall cover all commands for all objects.
 *
 * @author hazard157
 * @author mvk
 */
public interface ISkCommandExecutor {

  /**
   * Starts the command execution and returns immediately.
   *
   * @param aCmd {@link IDtoCommand} - the command data to be executed
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void executeCommand( IDtoCommand aCmd );

}
