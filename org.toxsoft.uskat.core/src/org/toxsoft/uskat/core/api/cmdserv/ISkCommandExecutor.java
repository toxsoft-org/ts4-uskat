package org.toxsoft.uskat.core.api.cmdserv;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
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

  /**
   * Determines if command can be sent.
   * <p>
   * <i>Notes on implementation</i>.<br>
   * In the ideal world this method should perform all the checks that {@link #executeCommand(IDtoCommand)} performs.
   * However not all checks can be performed without really executing the command. Moreover, not all check have to be
   * performed because of performance issues - this method may be called many times.
   *
   * @param aCmdGwid {@link Gwid} - concrete GWID of the command
   * @param aAuthorSkid {@link Skid} - command author's SKID
   * @param aArgs {@link IOptionSet} - command arguments
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  default ValidationResult canExecuteCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    return ValidationResult.SUCCESS;
  }

}
