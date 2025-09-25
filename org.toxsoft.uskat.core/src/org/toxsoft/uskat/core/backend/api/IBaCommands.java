package org.toxsoft.uskat.core.backend.api;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Backend addon to wirk with commands.
 * <p>
 * This is the mandatory addon.
 *
 * @author hazard157
 */
public interface IBaCommands
    extends IBackendAddon {

  /**
   * ID of this backend addon.
   */
  String ADDON_ID = ISkBackendHardConstant.BAID_COMMANDS;

  /**
   * Backend starts command execution if possible.
   * <p>
   * Returned command may in any state, even in completed state.
   *
   * @param aCmdGwid {@link Gwid} - concrete GWID of the command
   * @param aAuthorSkid {@link Skid} - command author's SKID
   * @param aArgs {@link IOptionSet} - command arguments
   * @return {@link SkCommand} - created command
   */
  SkCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs );

  /**
   * Checks if command can be sent.
   * <p>
   * The successful check does not guarantees that {@link #sendCommand(Gwid, Skid, IOptionSet)} will succeed, however if
   * check fails, sending command will definitely throw an exception.
   *
   * @param aCmdGwid {@link Gwid} - concrete GWID of the command
   * @param aAuthorSkid {@link Skid} - command author's SKID
   * @param aArgs {@link IOptionSet} - command arguments
   * @return {@link ValidationResult} - the check result
   */
  default ValidationResult canSendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    /**
     * FIXME GOGA 2025-09-25<br>
     * This method is temporarily declared as 'default' one to avoid compile-time errors. As soon as development
     * finishes the method must be declared abstract.
     */
    return ValidationResult.SUCCESS;
  }

  /**
   * Tells backend which GWIDs of command have executors registered in caller frontend.
   * <p>
   * Argument replaces previous lsit of GWIDs. An empty list means the frontent will not execute any command.
   * <p>
   * List change does not affects commands being executed now.
   *
   * @param aGwids {@link IGwidList} - GWIDs of commands that are executed by this object
   */
  void setHandledCommandGwids( IGwidList aGwids );

  /**
   * Request to change state of the command being executed now.
   * <p>
   * Caller must check that command {@link DtoCommandStateChangeInfo#instanceId()} is being executed by this frontend.
   *
   * @param aStateChangeInfo {@link DtoCommandStateChangeInfo} - new state of the command
   */
  void changeCommandState( DtoCommandStateChangeInfo aStateChangeInfo );

  /**
   * Returns list of concrete command GWIDs that have executers assigned.
   * <p>
   * Note: returned list may contain multi-GWIDs.
   *
   * @return {@link IGwidList} - global (system-wide) list GWIDs of commands with assigned executors
   * @see ISkCommandService#listGloballyHandledCommandGwids()
   */
  IGwidList listGloballyHandledCommandGwids();

  /**
   * Tells backend to save completed command to the history.
   *
   * @param aCompletedCommand {@link IDtoCompletedCommand}
   */
  void saveToHistory( IDtoCompletedCommand aCompletedCommand );

  /**
   * Returns the object command history for specified time interval.
   *
   * @param aInterval {@link ITimeInterval} - query time interval
   * @param aGwid {@link Gwid} - valid concrete command(s) GWID of one object
   * @return {@link ITimedList}&lt;{@link IDtoCompletedCommand}&gt; - list of the queried entities
   */
  ITimedList<IDtoCompletedCommand> queryObjCommands( ITimeInterval aInterval, Gwid aGwid );

}
