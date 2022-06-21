package org.toxsoft.uskat.core.api.cmdserv;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.utils.*;

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
   * Returns the GWIDs for which the registered executors are responsible for.
   *
   * @return {@link IGwidList} - the summary of all registered executors GWIDs
   */
  IGwidList listExecutableCommandGwids();

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
   * SKID {@link SkCommandState#OP_AUTHOR} иand/or any other applicaion specific data.
   *
   * @param aStateChangeInfo {@link DtoCommandStateChangeInfo} - state change info
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException command with specified ID is not executing now
   * @throws TsIllegalArgumentRtException invalid command state
   */
  void changeCommandState( DtoCommandStateChangeInfo aStateChangeInfo );

  /**
   * Returns the stored completed commands history.
   *
   * @return {@link ITemporalsHistory} - the commands history
   */
  ITemporalsHistory<IDtoCompletedCommand> history();

  /**
   * Returns the service eventer.
   *
   * @return {@link ITsEventer}&lt;{@link ISkCommandServiceListener}&gt; - the service eventer
   */
  ITsEventer<ISkCommandServiceListener> eventer();

}
