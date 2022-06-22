package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.uskat.core.ISkHardConstants.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
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
  String ADDON_ID = SK_ID + "ba.Commands"; //$NON-NLS-1$

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
   * Tells backend which GWIDs of command have executors registered in caller frontend.
   * <p>
   * Argument replaces previous lsit of GWIDs. An empty list means the frontent will not execute any command.
   * <p>
   * List change does not affects commands being executed now.
   *
   * @param aGwids {@link IGwidList} - GWIDs of commands that are executed by this object
   */
  void setExcutableCommandGwids( IGwidList aGwids );

  /**
   * Request to change state of the command being executed now.
   * <p>
   * Caller must check that command {@link DtoCommandStateChangeInfo#instanceId()} is being executed by this frontend.
   *
   * @param aStateChangeInfo {@link DtoCommandStateChangeInfo} - new state of the command
   */
  void changeCommandState( DtoCommandStateChangeInfo aStateChangeInfo );

  /**
   * Fetches completed commands from history.
   *
   * @param aInterval {@link IQueryInterval} - the time interval to be queried
   * @param aNeededGwids {@link IGwidList} - list of command GWIDs to be queried
   * @return {@link ITimedList}&lt;{@link IDtoCompletedCommand}&gt; - list of requested commands
   */
  ITimedList<IDtoCompletedCommand> queryCommands( IQueryInterval aInterval, IGwidList aNeededGwids );

}
