package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * {@link ISkCommandService} implementation.
 *
 * @author hazard157
 */
public class SkCoreServCommands
    extends AbstractSkCoreService
    implements ISkCommandService {

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServCommands::new;

  /**
   * Send commands executing now.
   * <p>
   * This is map "command instance ID" - "command"
   */
  private final IStringMapEdit<SkCommand> executingCmds = new StringMap<>();

  /**
   * Registered registeredExecutors with a list of processed commands.
   */
  private final IMapEdit<ISkCommandExecutor, IGwidList> registeredExecutors = new ElemMap<>();

  private final GenericChangeEventer globallyHandledGwidsEventer;

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkCoreServCommands( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
    globallyHandledGwidsEventer = new GenericChangeEventer( this );
  }

  // ------------------------------------------------------------------------------------
  // AbstractSkCoreService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // nop
  }

  @Override
  protected void doClose() {
    globallyHandledGwidsEventer.clearListenersList();
    globallyHandledGwidsEventer.resetPendingEvents();
  }

  @Override
  protected boolean onBackendMessage( GenericMessage aMessage ) {
    switch( aMessage.messageId() ) {
      case BaMsgCommandsExecCmd.MSG_ID: {
        IDtoCommand cmd = BaMsgCommandsExecCmd.INSTANCE.getCmd( aMessage );
        handleMsgExecuteCommand( cmd );
        return true;
      }
      case BaMsgCommandsChangeState.MSG_ID: {
        DtoCommandStateChangeInfo stateChangeInfo = BaMsgCommandsChangeState.INSTANCE.getStateChangeInfo( aMessage );
        handleMsgCommandStateChanged( stateChangeInfo );
        return true;
      }
      case BaMsgCommandsGloballyHandledGwidsChanged.MSG_ID: {
        globallyHandledGwidsEventer.fireChangeEvent();
        return true;
      }
      default:
        return false;
    }
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Calculates GWIDs for which all {@link #registeredExecutors} are responsible.
   *
   * @return {@link IGwidList} - list of command GWIDs executed by thi service
   */
  private IGwidList calcHandledCommandGwids() {
    GwidList ll = new GwidList();
    // iterate over all GWIDs in #registeredExecutorsMap
    for( IGwidList gl : registeredExecutors.values() ) {
      for( Gwid g : gl ) {
        gwidService().updateGwidsOfIntereset( ll, g, ESkClassPropKind.CMD );
      }
    }
    return ll;
  }

  /**
   * Finds executor, responsible to send command with specified GWID.
   *
   * @param aCmdGwid {@link Gwid} - concrete GWID of the command
   * @return {@link ISkCommandExecutor} - command executor or <code>null</code>
   */
  private ISkCommandExecutor findExecutorForGwid( Gwid aCmdGwid ) {
    for( ISkCommandExecutor executor : registeredExecutors.keys() ) {
      for( Gwid gwid : registeredExecutors.getByKey( executor ) ) {
        if( GwidUtils.covers( gwid, aCmdGwid ) ) {
          return executor;
        }
      }
    }
    return null;
  }

  private void handleMsgExecuteCommand( IDtoCommand aCommand ) {
    ISkCommandExecutor executor = findExecutorForGwid( aCommand.cmdGwid() );
    if( executor != null ) {
      // 2022-11-17 mvk +++
      executingCmds.put( aCommand.instanceId(), new SkCommand( aCommand ) );
      try {
        executor.executeCommand( aCommand );
      }
      catch( Throwable e ) {
        // 2022-11-17 mvk +++ TODO: оценить решение о такой обработке неожиданной ошибки
        IOptionSetEdit params = new OptionSet();
        String cause = e.getLocalizedMessage();
        SkCommandState.OP_REASON.setValue( params, cause != null ? AvUtils.avStr( cause ) : IAtomicValue.NULL );
        SkCommandState state = new SkCommandState( System.currentTimeMillis(), ESkCommandState.FAILED, params );
        changeCommandState( new DtoCommandStateChangeInfo( aCommand.instanceId(), state ) );
      }
    }
    else {
      logger().warning( FMT_LOG_WARN_UNHANDLED_CMD, aCommand.toString() );
    }
  }

  private void handleMsgCommandStateChanged( DtoCommandStateChangeInfo aStateChangeInfo ) {
    SkCommand skCmd = executingCmds.findByKey( aStateChangeInfo.instanceId() );
    if( skCmd != null ) {
      SkCommandState newState = aStateChangeInfo.state();
      skCmd.papiAddState( newState ); // this generates event from SkCommand
      if( newState.state().isComplete() ) {
        executingCmds.removeByKey( skCmd.instanceId() );
        IDtoCommand dtoCmd = new DtoCommand( skCmd.timestamp(), skCmd.instanceId(), skCmd.cmdGwid(), skCmd.authorSkid(),
            skCmd.argValues() );
        IDtoCompletedCommand cc = new DtoCompletedCommand( dtoCmd, skCmd.statesHistory() );
        ba().baCommands().saveToHistory( cc );
      }
    }
    else {
      logger().warning( FMT_LOG_WARN_NO_STATE_CHANGE_CMD, aStateChangeInfo.toString() );
    }
  }

  // ------------------------------------------------------------------------------------
  // ISkCommandService
  //

  @Override
  public ISkCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    // check preconditions
    TsNullArgumentRtException.checkNulls( aCmdGwid, aAuthorSkid, aArgs );
    TsIllegalArgumentRtException.checkFalse( aCmdGwid.kind() == EGwidKind.GW_CMD );
    TsIllegalArgumentRtException.checkTrue( aCmdGwid.isAbstract() );
    TsIllegalArgumentRtException.checkTrue( aCmdGwid.isMulti() );
    // check command entities exists
    ISkClassInfo classInfo = sysdescr().findClassInfo( aCmdGwid.classId() );
    if( classInfo == null ) {
      throw new TsIllegalArgumentRtException( FMT_ERR_CMD_CLASS_NOT_EXIST, aCmdGwid.classId() );
    }
    IDtoCmdInfo cmdInfo = classInfo.cmds().list().findByKey( aCmdGwid.propId() );
    if( cmdInfo == null ) {
      throw new TsIllegalArgumentRtException( FMT_ERR_CMD_NOT_EXIST, aCmdGwid.propId(), aCmdGwid.classId() );
    }
    ISkObject author = coreApi().objService().find( aAuthorSkid );
    if( author == null ) {
      throw new TsIllegalArgumentRtException( FMT_ERR_CMD_AUTHOR_NOT_EXIST, aAuthorSkid );
    }
    // check command arguments are valid
    for( IDataDef argInfo : cmdInfo.argDefs() ) {
      IAtomicValue value = aArgs.getValue( argInfo.id() );
      AvTypeCastRtException.checkCanAssign( argInfo.atomicType(), value.atomicType() );
      /**
       * Command argument DataDef constraints mainly are application domain constraints, GUI builder info, etc so they
       * are considered as hints, not mandatory restrictions. USkat does NOT checks argument values against constraints.
       */
    }
    // send command
    SkCommand cmd = ba().baCommands().sendCommand( aCmdGwid, aAuthorSkid, aArgs );
    if( !cmd.isComplete() ) {
      executingCmds.put( cmd.instanceId(), cmd );
    }
    return cmd;
  }

  @Override
  public void registerExecutor( ISkCommandExecutor aExecutor, IGwidList aCmdGwids ) {
    TsNullArgumentRtException.checkNulls( aExecutor, aCmdGwids );
    registeredExecutors.put( aExecutor, aCmdGwids );
    IGwidList allGwids = calcHandledCommandGwids();
    ba().baCommands().setHandledCommandGwids( allGwids );
  }

  @Override
  public void unregisterExecutor( ISkCommandExecutor aExecutor ) {
    if( registeredExecutors.removeByKey( aExecutor ) != null ) {
      IGwidList allGwids = calcHandledCommandGwids();
      ba().baCommands().setHandledCommandGwids( allGwids );
    }
  }

  @Override
  public void changeCommandState( DtoCommandStateChangeInfo aStateChangeInfo ) {
    TsNullArgumentRtException.checkNull( aStateChangeInfo );
    TsItemNotFoundRtException.checkFalse( executingCmds.hasKey( aStateChangeInfo.instanceId() ) );
    switch( aStateChangeInfo.state().state() ) {
      case EXECUTING:
      case FAILED:
      case SUCCESS: {
        ba().baCommands().changeCommandState( aStateChangeInfo );
        break;
      }
      case SENDING:
      case TIMEOUTED:
      case UNHANDLED:
        throw new TsIllegalArgumentRtException();
      default:
        throw new TsNotAllEnumsUsedRtException( aStateChangeInfo.state().state().id() );
    }
  }

  @Override
  public ITimedList<IDtoCompletedCommand> queryObjCommands( ITimeInterval aInterval, Gwid aGwid ) {
    TsNullArgumentRtException.checkNulls( aInterval, aGwid );
    TsIllegalArgumentRtException.checkTrue( aGwid.kind() != EGwidKind.GW_CMD );
    TsIllegalArgumentRtException.checkTrue( aGwid.isAbstract() );
    TsIllegalArgumentRtException.checkTrue( aGwid.isStridMulti() );
    TsItemNotFoundRtException.checkFalse( gwidService().exists( aGwid ) );
    return ba().baCommands().queryObjCommands( aInterval, aGwid );
  }

  @Override
  public IGwidList listGloballyHandledCommandGwids() {
    return ba().baCommands().listGloballyHandledCommandGwids();
  }

  @Override
  public IGenericChangeEventer globallyHandledGwidsEventer() {
    return globallyHandledGwidsEventer;
  }

}
