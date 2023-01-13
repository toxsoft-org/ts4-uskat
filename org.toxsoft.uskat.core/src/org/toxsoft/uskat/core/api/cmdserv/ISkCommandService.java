package org.toxsoft.uskat.core.api.cmdserv;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.bricks.events.change.IGenericChangeEventer;
import org.toxsoft.core.tslib.bricks.time.ITimeInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.ISkHardConstants;
import org.toxsoft.uskat.core.api.ISkService;

/**
 * Core service: command sending and processing support.
 *
 * @author hazard157
 */
public interface ISkCommandService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".Commands"; //$NON-NLS-1$

  /**
   * Creates and sends the command.
   * <p>
   * Created instance is a "live" object changing it's {@link ISkCommand#statesHistory()} until command completes.
   * Client may either poll {@link ISkCommand#statesHistory()} or subscribe to {@link ISkCommand#stateEventer()}.
   *
   * @param aCmdGwid {@link Gwid} - concrete GWID of the command
   * @param aAuthorSkid {@link Skid} - SKID of the command author
   * @param aArgs {@link IOptionSet} - command argumens values
   * @return {@link ISkCommand} - created command instance
   */
  ISkCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs );

  /**
   * Reigsters command executor.
   * <p>
   * Registering the same executor again changes GWIDs list.
   * <p>
   * There may be only the following kinds of GWIDs in the list:
   * <ul>
   * <li>abstract {@link EGwidKind#GW_CLASS} - all commans of all objects of the class and subclasses;</li>
   * <li>concrete {@link EGwidKind#GW_CLASS} - all commands of the specified object;</li>
   * <li>abstract {@link EGwidKind#GW_CMD} - specified command all objects of the class and subclasses;</li>
   * <li>concrete {@link EGwidKind#GW_CMD} - specified command of the specified object.</li>
   * </ul>
   * Multi-GWIDs are expanded as usual. All other GWIDs are silently ignored.
   * <p>
   * If this service has already registered an executor for one of the command then an exception is thrown and mothod
   * does nothing.
   * <p>
   * If an empty list of GWIDs is specified than executor will be unregistered.
   *
   * @param aExecutor {@link ISkCommandExecutor} - the executor to be registered
   * @param aCmdGwids {@link IGwidList} - list of GWIDs the executor is responsible for
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemAlreadyExistsRtException the was an executor already ergistered for at lieast on GWID from the list
   */
  void registerExecutor( ISkCommandExecutor aExecutor, IGwidList aCmdGwids );

  /**
   * Unregisters previously registered executor (if any).
   *
   * @param aExecutor {@link ISkCommandExecutor} - executor to unregister
   */
  void unregisterExecutor( ISkCommandExecutor aExecutor );

  /**
   * Chenges state of the command currently being executed.
   * <p>
   * This interface is intended for use by the command executor hence only following states are allowed:
   * <ul>
   * <li>{@link ESkCommandState#EXECUTING} - execution is continuing, additional information is contained in
   * {@link SkCommandState#params()}. This state may occur many time sequently until command completes;</li>
   * <li>{@link ESkCommandState#FAILED} - command execution failed;</li>
   * <li>{@link ESkCommandState#SUCCESS} - command completed successfully.</li>
   * </ul>
   * <p>
   * Argument may include additional optional information like reason {@link SkCommandState#OP_REASON}, state change
   * SKID {@link SkCommandState#OP_AUTHOR} and/or any other applicaion specific data.
   *
   * @param aStateChangeInfo {@link DtoCommandStateChangeInfo} - state change info
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException command with specified ID is not executing now
   * @throws TsIllegalArgumentRtException invalid command state
   */
  void changeCommandState( DtoCommandStateChangeInfo aStateChangeInfo );

  /**
   * Returns the object command history for specified time interval.
   * <p>
   * Method accepts concrete GWID of kind {@link EGwidKind#GW_CMD}. Multi objects are <b>not</b> allowed, however
   * multi-commands {@link Gwid#isPropMulti()} = <code>true</code> are allowed.
   * <p>
   * Note: do not ask for long time interval, this method is synchronous and hence may freeze for a long time.
   *
   * @param aInterval {@link ITimeInterval} - query time interval
   * @param aGwid {@link Gwid} - concrete GWID of the command (s)
   * @return {@link ITimedList}&lt;{@link IDtoCompletedCommand}&gt; - list of the queried entities
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException invalid GWID
   * @throws TsItemNotFoundRtException no such command exists in sysdescr
   */
  ITimedList<IDtoCompletedCommand> queryObjCommands( ITimeInterval aInterval, Gwid aGwid );

  // ------------------------------------------------------------------------------------
  // Global GWIDs handling
  //

  /**
   * Returns list of concrete command GWIDs that have executers assigned.
   * <p>
   * In distributed systems each {@link ISkCommandService#registerExecutor(ISkCommandExecutor, IGwidList)} adds
   * executors handle subset of all commands of all objects. The master server keeps a record of all registered
   * executors. This method returns list of command GWIDs from the master server.
   * <p>
   * In local environments returns union of GWIDs set to be handled by executors registered with method
   * {@link #registerExecutor(ISkCommandExecutor, IGwidList)} in this servce.
   * <p>
   * Note: returned list may contain multi-GWIDs.
   *
   * @return {@link IGwidList} - global (system-wide) list GWIDs of commands with assigned executors
   */
  IGwidList listGloballyHandledCommandGwids();

  /**
   * Returns the eventer notifying about changes in {@link #listGloballyHandledCommandGwids()}.
   *
   * @return {@link IGenericChangeEventer} - the eventer
   */
  IGenericChangeEventer globallyHandledGwidsEventer();

}
