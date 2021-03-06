package org.toxsoft.uskat.s5.server.backend.supports.commands.impl;

import static org.toxsoft.core.tslib.bricks.time.EQueryIntervalType.*;
import static org.toxsoft.uskat.core.api.cmdserv.ESkCommandState.*;
import static org.toxsoft.uskat.s5.server.IS5ImplementConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.commands.IS5CommandHardConstants.*;
import static org.toxsoft.uskat.s5.server.backend.supports.commands.impl.IS5Resources.*;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.ejb.*;

import org.infinispan.Cache;
import org.infinispan.commons.util.CloseableIterator;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.utils.IParameterized;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.TimedList;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.impl.ElemArrayList;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.utils.Pair;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.api.objserv.IDtoObject;
import org.toxsoft.uskat.core.api.sysdescr.ISkClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoCmdInfo;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.SkCommand;
import org.toxsoft.uskat.core.impl.dto.DtoCommand;
import org.toxsoft.uskat.core.impl.dto.DtoCompletedCommand;
import org.toxsoft.uskat.s5.common.sysdescr.ISkSysdescrReader;
import org.toxsoft.uskat.s5.legacy.QueryInterval;
import org.toxsoft.uskat.s5.server.backend.addons.commands.S5BaCommandsData;
import org.toxsoft.uskat.s5.server.backend.addons.commands.S5BaCommandsSupport;
import org.toxsoft.uskat.s5.server.backend.supports.commands.IS5BackendCommandSingleton;
import org.toxsoft.uskat.s5.server.backend.supports.commands.sequences.IS5CommandSequence;
import org.toxsoft.uskat.s5.server.backend.supports.commands.sequences.IS5CommandSequenceEdit;
import org.toxsoft.uskat.s5.server.frontend.IS5FrontendRear;
import org.toxsoft.uskat.s5.server.sequences.ISequenceBlock;
import org.toxsoft.uskat.s5.server.sequences.ISequenceFactory;
import org.toxsoft.uskat.s5.server.sequences.impl.S5BackendSequenceSupportSingleton;
import org.toxsoft.uskat.s5.server.sequences.impl.S5SequenceFactory;
import org.toxsoft.uskat.s5.server.transactions.S5TransactionUtils;
import org.toxsoft.uskat.s5.utils.collections.S5FixedCapacityTimedList;
import org.toxsoft.uskat.s5.utils.jobs.IS5ServerJob;

/**
 * ???????????????????? ???????????????????? {@link IS5BackendCommandSingleton}
 *
 * @author mvk
 */
@Startup
@Singleton
@LocalBean
@DependsOn( { //
    BACKEND_LINKS_SINGLETON //
} )
@TransactionManagement( TransactionManagementType.CONTAINER )
@TransactionAttribute( TransactionAttributeType.SUPPORTS )
// @ConcurrencyManagement( ConcurrencyManagementType.CONTAINER )
@ConcurrencyManagement( ConcurrencyManagementType.BEAN )
@AccessTimeout( value = ACCESS_TIMEOUT_DEFAULT, unit = TimeUnit.MILLISECONDS )
@Lock( LockType.READ )
public class S5BackendCommandSingleton
    extends S5BackendSequenceSupportSingleton<IS5CommandSequence, IDtoCompletedCommand>
    implements IS5BackendCommandSingleton, IS5ServerJob {

  private static final long serialVersionUID = 157157L;

  /**
   * ?????? ???????????????????? ?? ???????????????????? ?????????????? ?????? ?????????????????????? ???????????????????????? (@DependsOn)
   */
  public static final String BACKEND_COMMANDS_ID = "S5BackendCommandSingleton"; //$NON-NLS-1$

  /**
   * ???????????????? ???????????????????? doJob (????????)
   */
  private static final long DOJOB_INTERVAL = 1000;

  /**
   * ?????? ???????????? ?????????????????????? ?? ???????????? ????????????
   * <p>
   * ????????: ?????????????????????????? ??????????????, {@link IDtoCommand#instanceId()} ;<br>
   * ????????????????:
   * <ul>
   * <li>{@link Pair#left()} = {@link IDtoCommand} ??????????????;</li>
   * <li>{@link Pair#right()} = {@link ITimedListEdit} ?????????????? ??????????????????.</li>
   * </ul>
   * .
   */
  @Resource( lookup = INFINISPAN_CACHE_CMD_STATES )
  private Cache<String, Pair<IDtoCommand, ITimedListEdit<SkCommandState>>> executingCommandsCache;

  /**
   * ?????????????????????????? ???????? ???????????????? ??????????????
   */
  private String nodeId;

  /**
   * ??????????????????????.
   */
  public S5BackendCommandSingleton() {
    super( BACKEND_COMMANDS_ID, STR_D_BACKEND_COMMANDS );
  }

  // ------------------------------------------------------------------------------------
  // ???????????????????? ?????????????????? ?????????????? S5BackendSupportSingleton
  //
  @SuppressWarnings( "nls" )
  @Override
  protected void doInitSupport() {
    // ?????????????????????????? ???????????????? ????????????
    super.doInitSupport();
    // ?????????????????????????? ???????? ??????????????
    nodeId = clusterManager().group().getLocalMember().getName();
    if( nodeId.contains( "-" ) ) {
      // ???????????? '-' ???? "_"
      nodeId = nodeId.replaceAll( "-", "_" );
      logger().error( "?????? ???????????????????????? nodeId ?????? ?????????????????????? hostname ?? ?????????????? ???????????????? '-' ???? '_'" );
    }
    // ???????????? doJob
    addOwnDoJob( DOJOB_INTERVAL );
  }

  @Override
  protected void doCloseSupport() {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // ???????????????????? IS5BackendQueriesSingleton
  //
  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public SkCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    TsNullArgumentRtException.checkNulls( aCmdGwid, aAuthorSkid, aArgs );
    try {
      // ?????????????? ??????????
      long currTime = System.currentTimeMillis();
      // ???????????????? ?????????????????????? ??????????????
      IS5FrontendRear frontend = findExecutorFrontend( aCmdGwid );
      // ?????????????????????????? ??????????????
      String instanceId = S5CommandIdGenerator.INSTANCE.nextId();
      // ??????????????
      DtoCommand command = new DtoCommand( currTime, instanceId, aCmdGwid, aAuthorSkid, aArgs );
      if( frontend != null ) {
        // ?????????????????? ?????????????????? ???? "??????????????????????"
        DtoCommandStateChangeInfo changeInfo =
            createChangeInfo( instanceId, currTime, EXECUTING, REASON_EXEC_BY_QUERY );
        changeCommandState( command, new TimedList<>(), changeInfo );
        // ???????????????????? ?????????????? ???? ????????????????????
        frontend.onBackendMessage( BaMsgCommandsExecCmd.INSTANCE.makeMessage( command ) );
        // ?????????????? ?????????????? ???? ????????????????????
        return new SkCommand( command );
      }
      // ???? ???????????? ?????????????????????? ??????????????
      DtoCommandStateChangeInfo changeInfo =
          createChangeInfo( instanceId, currTime, UNHANDLED, REASON_EXECUTOR_NOT_FOUND );
      changeCommandState( command, new TimedList<>(), changeInfo );
      // ?????????? ???? ???????????????????? ??????????????
      return new SkCommand( command );
    }
    catch( Throwable e ) {
      // ?????????????????????? ???????????? ??????????????????
      logger().error( e );
      throw e;
    }
  }

  @TransactionAttribute( TransactionAttributeType.NOT_SUPPORTED )
  @Override
  public void setHandledCommandGwids( IGwidList aGwids ) {
    TsNullArgumentRtException.checkNulls( aGwids );
    // ?????????????????? frontend
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      S5BaCommandsData frontendData = findCommandsFrontendData( frontend );
      if( frontendData == null ) {
        // ???????????????? ???? ???????????????????????? ???????????????? ??????????
        continue;
      }
      // ???????????????????? ?????????????????? ???? ?????????????????? ???????????? ???????????? ???????????????????????????? ??????????????????????????
      frontend.onBackendMessage( BaMsgCommandsGloballyHandledGwidsChanged.INSTANCE.makeMessage() );
    }
    logger().info( MSG_SET_EXECUTABLE_CMDS, Integer.valueOf( listGloballyHandledCommandGwids().size() ) );
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void changeCommandState( DtoCommandStateChangeInfo aStateChangeInfo ) {
    TsNullArgumentRtException.checkNull( aStateChangeInfo );
    // ?????????? ?????????????????????? ??????????????
    Pair<IDtoCommand, ITimedListEdit<SkCommandState>> cmd = executingCommandsCache.get( aStateChangeInfo.instanceId() );
    if( cmd == null ) {
      // ?????????????????? ?????????????????? ?????????????????? ???????????????????????????? ??????????????
      logger().error( ERR_COMMAND_NOT_FOUND, aStateChangeInfo.instanceId(), aStateChangeInfo.state() );
      return;
    }
    // ?????????????????? ??????????????????. false: ?????????????????? ?????????????????? ?????? ?????????????????????? ??????????????
    changeCommandState( cmd.left(), cmd.right(), aStateChangeInfo );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public IGwidList listGloballyHandledCommandGwids() {
    GwidList retValue = new GwidList();
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      S5BaCommandsData frontendData = findCommandsFrontendData( frontend );
      if( frontendData == null ) {
        // ???????????????? ???? ???????????????????????? ???????????????? ??????????
        continue;
      }
      retValue.addAll( frontendData.commands.getHandledCommandGwids() );
    }
    return retValue;
  }

  @TransactionAttribute( TransactionAttributeType.REQUIRED )
  @Override
  public void saveToHistory( IDtoCompletedCommand aCompletedCommand ) {
    TsNullArgumentRtException.checkNull( aCompletedCommand );
    // TODO ???????????? ???? ????????????????????: ???? ???????????? ???????????????????? ??????????????, skconnection ?????????????? ???????????????????????? ?????????????? ??????????
    // ?????????????????? "????????????" ?? ?????????????????????? (????????????????, ???????????? ?????????? ?? ????????????????). ?? ?????????????? ???????????????????? ???????????? ??????
    // ???????????????????????? ?????????????????? ?????????????? (???????????? changeCommandState) ?? ???????????? ?????? ?????????????????????????? ??????????:
    // writeCommand( aCompletedCommand );
  }

  @TransactionAttribute( TransactionAttributeType.SUPPORTS )
  @Override
  public ITimedList<IDtoCompletedCommand> queryObjCommands( IQueryInterval aInterval, Gwid aObjId ) {
    TsNullArgumentRtException.checkNulls( aInterval, aObjId );
    long traceStartTime = System.currentTimeMillis();
    // ???????????????????? ???????????? ?????????????????????????????? ?????????????????????????? ????????????????. false: ?????? ????????????????
    if( aObjId.kind() != EGwidKind.GW_CLASS || aObjId.isAbstract() ) {
      // ???? ?????????????????? ?????????????????????? ???????????? ???????????????????????????? ????????????????
      return new TimedList<>();
    }
    // ???????????? ?????????????? ????????????
    long traceReadStartTime = System.currentTimeMillis();
    IList<IS5CommandSequence> sequences = readSequences( new GwidList( aObjId ), aInterval, ACCESS_TIMEOUT_DEFAULT );
    long traceReadEndTime = System.currentTimeMillis();
    // ???????????????????? ???????????? ?? ???????????????????????? ????????????????(???? ????????????????) ???????????????????? ??????????????
    TimedList<IDtoCompletedCommand> commands = new TimedList<>();
    for( IS5CommandSequence sequence : sequences ) {
      for( ISequenceBlock<IDtoCompletedCommand> block : sequence.blocks() ) {
        for( int index = 0, n = block.size(); index < n; index++ ) {
          commands.add( block.getValue( index ) );
        }
      }
    }
    // ???????????????????????? ????????????????????. aAllowDuplicates = true
    ITimedListEdit<IDtoCompletedCommand> retValue = new S5FixedCapacityTimedList<>( commands.size(), true );
    retValue.addAll( commands );

    long traceResultTime = System.currentTimeMillis();
    // ???????????????????????? ??????????????
    Integer rc = Integer.valueOf( retValue.size() );
    Long pt = Long.valueOf( traceReadStartTime - traceStartTime );
    Long rt = Long.valueOf( traceReadEndTime - traceReadStartTime );
    Long ft = Long.valueOf( traceResultTime - traceReadEndTime );
    Long at = Long.valueOf( traceResultTime - traceStartTime );
    // ?????????????????? ???????????? ?????????????? ????????????
    logger().info( MSG_READ_COMMANDS, aObjId, aInterval, rc, at, pt, rt, ft );
    return retValue;
  }

  // ------------------------------------------------------------------------------------
  // ???????????????????? IS5ServerJob
  //
  @Override
  public void doJob() {
    // ?????????????? ??????????
    long currTime = System.currentTimeMillis();
    // ???????????????????? ???????????? ???? ??????????????????
    int count = 0;
    try( CloseableIterator<Pair<IDtoCommand, ITimedListEdit<SkCommandState>>> iterator =
        executingCommandsCache.values().iterator() ) {
      while( iterator.hasNext() ) {
        Pair<IDtoCommand, ITimedListEdit<SkCommandState>> cmdPair = iterator.next();
        IDtoCommand cmd = cmdPair.left();
        // 2020-03-21, mvk, ???????????? ?????????? ???? ?????????? ?????????????? ?? ???????????????????? ???????? ???????????????? - ???????? ???????????????? ?? ???????????? ???????? ?? ??
        // ?????????????? ?????? ?? ???????? ?????????????? (????????????: ??????????????, ?????????????????? ?????????????? ??????????????, ?????????????? ?????????????????? ????).
        // if( clusterManager.isPrimary() == true && //
        // currTime - cmd.timestamp() >= getCmdTimeout( sysdescrReader(), cmd ) ) {
        if( currTime - cmd.timestamp() >= getCmdTimeout( sysdescrReader(), cmd ) ) {
          // ???????????????????? ???????????????????? ?????????????? ???? ????????????????
          DtoCommandStateChangeInfo newState =
              createChangeInfo( cmd.instanceId(), currTime, TIMEOUTED, REASON_CANCEL_BY_TIMEOUT );
          changeCommandState( cmd, cmdPair.right(), newState );

          continue;
        }
        // ?????????????? ???????????????? ???? ????????????????????
        count++;
      }
    }

    // ???????????????????? ???????????? ???? ????????????????????
    logger().debug( MSG_DOJOB, Integer.valueOf( count ) );
  }

  // ------------------------------------------------------------------------------------
  // ???????????????????? ?????????????????????? ?????????????? S5BackendSequenceSupportSingleton
  //
  @Override
  protected IS5BackendCommandSingleton getBusinessObject() {
    return sessionContext().getBusinessObject( IS5BackendCommandSingleton.class );
  }

  @Override
  protected ISequenceFactory<IDtoCompletedCommand> doCreateFactory() {
    return new S5CommandSequenceFactory( backend().initialConfig().impl(), sysdescrReader() );
  }

  // ------------------------------------------------------------------------------------
  // ?????????????????????? ?????????????????? ?????????????? S5BackendSequenceSupportSingleton
  //
  @Override
  @SuppressWarnings( { "rawtypes" } )
  protected void doAfterUpdateClass( IDtoClassInfo aPrevClassInfo, IDtoClassInfo aNewClassInfo,
      IStridablesList<IDtoClassInfo> aDescendants ) {
    if( aPrevClassInfo.cmdInfos().size() > 0 && aNewClassInfo.cmdInfos().size() == 0 ) {
      // ???????????????? ?????????????????????????????? ???????????? ???????????????? ?? ?????????????? ???????????? ?????? ????????????
      IMapEdit<Gwid, IParameterized> gwidsEditor = ((S5SequenceFactory)factory()).gwidsEditor();
      // ???????????? ???????????????? ???????????????????????? ??????????????
      IList<IDtoObject> objs = S5TransactionUtils.txUpdatedClassObjs( transactionManager(), objectsBackend(),
          aNewClassInfo.id(), aDescendants );
      for( IDtoObject obj : objs ) {
        String classId = obj.classId();
        ISkClassInfo classInfo = sysdescrReader().getClassInfo( classId );
        if( classInfo.cmds().list().size() == 0 ) {
          gwidsEditor.removeByKey( Gwid.createObj( classId, obj.strid() ) );
        }
      }
    }
  }

  @Override
  @SuppressWarnings( { "rawtypes" } )
  protected void doBeforeDeleteClass( IDtoClassInfo aClassInfo ) {
    String classId = aClassInfo.id();
    // ???????????????? ?????????????????????????????? ???????????? ???????????????? ???????????????????? ????????????
    if( aClassInfo.cmdInfos().size() > 0 ) {
      // ???????????????? ?????????????????????????????? ???????????? ???????????????? ???????????????????? ????????????
      IMapEdit<Gwid, IParameterized> gwidsEditor = ((S5SequenceFactory)factory()).gwidsEditor();
      for( Gwid gwid : new ElemArrayList<>( gwidsEditor.keys() ) ) {
        if( gwid.classId().equals( classId ) ) {
          gwidsEditor.removeByKey( gwid );
        }
      }
    }
  }

  @Override
  @SuppressWarnings( { "rawtypes" } )
  protected void doAfterWriteObjects( IMap<ISkClassInfo, IList<IDtoObject>> aRemovedObjs,
      IMap<ISkClassInfo, IList<Pair<IDtoObject, IDtoObject>>> aUpdatedObjs,
      IMap<ISkClassInfo, IList<IDtoObject>> aCreatedObjs ) {
    // ???????????????? ?????????????????????????????? ???????????? ?????????????????? ????????????????
    IMapEdit<Gwid, IParameterized> gwidsEditor = ((S5SequenceFactory)factory()).gwidsEditor();
    for( ISkClassInfo classInfo : aRemovedObjs.keys() ) {
      if( classInfo.cmds().list().size() == 0 ) {
        // ?? ???????????? ?????????????? ?????? ????????????
        continue;
      }
      IList<IDtoObject> objs = aRemovedObjs.getByKey( classInfo );
      for( IDtoObject obj : objs ) {
        gwidsEditor.removeByKey( Gwid.createObj( obj.classId(), obj.strid() ) );
      }
    }
  }

  // ------------------------------------------------------------------------------------
  // ???????????????????? ????????????
  //
  /**
   * ???????????????????? ???????????????? ?????????????????? ?????????????????? ?????????????? ?? ?????????????????? ??????????????????????????????
   *
   * @param aGwid {@link Gwid} ?????????????????????????? ??????????????
   * @return {@link IS5FrontendRear} ???????????????? ?????????????????????? ??????????????. null: ???? ????????????
   * @throws TsNullArgumentRtException ???????????????? = null
   */
  private IS5FrontendRear findExecutorFrontend( Gwid aGwid ) {
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      S5BaCommandsData frontendData = findCommandsFrontendData( frontend );
      if( frontendData == null ) {
        // ???????????????? ???? ???????????????????????? ???????????????? ??????????
        continue;
      }
      if( frontendData.commands.getHandledCommandGwids().hasElem( aGwid ) ) {
        return frontend;
      }
    }
    return null;
  }

  /**
   * ?????????????????? ???????????? ?????????????????? ??????????????
   *
   * @param aCommand {@link IDtoCommand} ?????????????? {@link IDtoCommand#instanceId()}
   * @param aStates {@link ITimedListEdit}&lt;{@link SkCommandState}&gt; ?????????????? ?????????????????? ??????????????
   * @param aStateChangeInfo {@link DtoCommandStateChangeInfo} ?????????? ?????????????????? ??????????????
   * @throws TsNullArgumentRtException ???????????????? = null
   * @throws TsIllegalArgumentRtException ???????????????????????? ?????????????? ?????????????????? ??????????????
   */
  private void changeCommandState( IDtoCommand aCommand, ITimedListEdit<SkCommandState> aStates,
      DtoCommandStateChangeInfo aStateChangeInfo ) {
    TsNullArgumentRtException.checkNulls( aCommand, aStates, aStateChangeInfo );
    // ???????????????????? ?????????????????? ??????????????
    SkCommandState prevState = aStates.last();
    // ?????????? ?????????????????? ??????????????
    SkCommandState newCommandState = aStateChangeInfo.state();
    // ???????????????? ???????????????????? ?????????????????? ?????????????????? ???????????????????? ??????????????
    switch( newCommandState.state() ) {
      case SENDING:
        if( prevState != null && prevState.state() != SENDING ) {
          throw new TsIllegalArgumentRtException();
        }
        break;
      case EXECUTING:
        if( prevState == null ) {
          // ?????????????? ???????????????????????? ?????????????? ?? ???????????????? ???? ????????????????????
          prevState = createState( newCommandState.timestamp(), SENDING, REASON_SEND_AND_EXEC );
          aStates.add( prevState );
        }
        if( prevState.state() != SENDING && prevState.state() != EXECUTING ) {
          // ???????????????????????? ?????????????? ?????????????????? ??????????????
          throw new TsIllegalArgumentRtException( ERR_WRONG_NEW_STATE, aCommand, prevState, newCommandState );
        }
        break;
      case UNHANDLED:
        if( prevState == null ) {
          // ?????????????? ???????????????????????? ?????????????? ?? ???????????????? ?????? ?????? ?????? ???? ??????????????????????
          prevState = createState( newCommandState.timestamp(), SENDING, REASON_SEND_AND_CANCEL );
          aStates.add( prevState );
        }
        if( prevState.state() != SENDING ) {
          // ???????????????????????? ?????????????? ?????????????????? ??????????????
          throw new TsIllegalArgumentRtException( ERR_WRONG_NEW_STATE, aCommand, prevState, newCommandState );
        }
        break;
      case TIMEOUTED:
      case SUCCESS:
      case FAILED:
        if( prevState == null || prevState.state() != EXECUTING ) {
          // ???????????????????????? ?????????????? ?????????????????? ??????????????
          throw new TsIllegalArgumentRtException( ERR_WRONG_NEW_STATE, aCommand, prevState, newCommandState );
        }
        break;
      default:
        throw new TsNotAllEnumsUsedRtException();
    }
    // ???????????????????? ???????????? ?????????????????? ?? ??????????????
    aStates.add( newCommandState );
    // ????????????: ?????????????????? ?????????????????? ??????????????
    logger().info( MSG_NEW_STATE, aCommand, prevState, newCommandState );

    // ?????????????????????????? ??????????????
    String cmdId = aCommand.instanceId();
    if( !newCommandState.state().isComplete() ) {
      // ?????????????????????? ???????????????????? ??????????????. ???????????????????? ????????
      executingCommandsCache.put( cmdId, new Pair<>( aCommand, aStates ) );
    }
    if( newCommandState.state().isComplete() ) {
      // ???????????????????? ???????????????????? ??????????????. ???????????????? ?????????????? ???? ????????
      executingCommandsCache.remove( cmdId );
      // ???????????? ?????????????? ?? ???????? ????????????
      writeCommand( new DtoCompletedCommand( aCommand, aStates ) );
    }
    // ???????????????????????? ????????????????????
    ITimedListEdit<DtoCommandStateChangeInfo> dpuStates = new TimedList<>();
    for( SkCommandState state : aStates ) {
      dpuStates.add( new DtoCommandStateChangeInfo( cmdId, state ) );
    }
    // ?????????????????? frontend
    for( IS5FrontendRear frontend : backend().attachedFrontends() ) {
      S5BaCommandsData frontendData = findCommandsFrontendData( frontend );
      if( frontendData == null ) {
        // ???????????????? ???? ???????????????????????? ???????????????? ??????????
        continue;
      }
      // ?????????????????? ???????????????????? ????????????
      S5BaCommandsSupport commandsSupport = frontendData.commands;
      // ???????????? ?????????????????? ?????????????????? ???????????? ???????????????????? ?????????? ?????????????? frontend
      S5CommandStateChangeInfoList states = commandsSupport.updateExecutingCmds( dpuStates );
      if( states.size() == 0 ) {
        // ?? ?????????????? ?????????????????? ?????? ?????????????????? ???????????? ?????????????????? ????????????????????
        continue;
      }
      // ???????????????????? ??????????????
      frontend.onBackendMessage( BaMsgCommandsChangeState.INSTANCE.makeMessage( aStateChangeInfo ) );
    }
  }

  /**
   * ???????????????????? ???????????????????? ?????????????????????? ?????????????? ?? ?????????????? ????????????
   *
   * @param aCommand {@link IDtoCompletedCommand} ?????????????? ?????? ????????????????????
   * @throws TsNullArgumentRtException ???????????????? = null
   */
  private void writeCommand( IDtoCompletedCommand aCommand ) {
    TsNullArgumentRtException.checkNull( aCommand );
    // ???????????????????????? ?????????????????????????????????????? ?????????????? ???????????? ???? ????????????????
    Skid skid = aCommand.cmd().cmdGwid().skid();
    TimedList<IDtoCompletedCommand> commands = new TimedList<>( aCommand );
    Gwid objId = Gwid.createObj( skid.classId(), skid.strid() );
    IQueryInterval interval = new QueryInterval( CSCE, commands.first().timestamp(), commands.last().timestamp() );
    try {
      IS5CommandSequenceEdit sequence = new S5CommandSequence( factory(), objId, interval, IList.EMPTY );
      sequence.set( commands );
      // C?????????????????? ?????????????? ?? ???????? ????????????
      IS5BackendCommandSingleton sequenceWriter =
          sessionContext().getBusinessObject( IS5BackendCommandSingleton.class );
      sequenceWriter.writeSequences( new ElemArrayList<IS5CommandSequence>( sequence ) );
    }
    catch( Throwable e ) {
      logger().error( e );
    }
  }

  /**
   * ?????????????? ?????????? ?????????????????? ??????????????
   *
   * @param aInstanceId String ?????????????????????????? ??????????????
   * @param aTimestamp long ?????????? ?????????????? ???????????? ??????????????????
   * @param aState {@link ESkCommandState} ?????????????????????????? ??????????????????
   * @param aCause String ?????????????? ???????????????? ?? ?????????? ??????????????????
   * @return {@link DtoCommandStateChangeInfo} ?????????? ??????????????????
   * @throws TsNullArgumentRtException ?????????? ???????????????? = null
   */
  private DtoCommandStateChangeInfo createChangeInfo( String aInstanceId, long aTimestamp, ESkCommandState aState,
      String aCause ) {
    TsNullArgumentRtException.checkNulls( aInstanceId, aState, aCause );
    SkCommandState commandState = createState( aTimestamp, aState, aCause );
    DtoCommandStateChangeInfo retValue = new DtoCommandStateChangeInfo( aInstanceId, commandState );
    return retValue;
  }

  /**
   * ?????????????? ?????????? ?????????????????? ??????????????
   *
   * @param aTimestamp long ?????????? ?????????????? ???????????? ??????????????????
   * @param aState {@link ESkCommandState} ?????????????????????????? ??????????????????
   * @param aCause String ?????????????? ???????????????? ?? ?????????? ??????????????????
   * @return {@link SkCommandState} ?????????? ??????????????????
   * @throws TsNullArgumentRtException ?????????? ???????????????? = null
   */
  private SkCommandState createState( long aTimestamp, ESkCommandState aState, String aCause ) {
    TsNullArgumentRtException.checkNulls( aState, aCause );
    Gwid author = Gwid.create( "skat.backend.server", nodeId, null, null, null, null ); //$NON-NLS-1$
    return new SkCommandState( aTimestamp, aState, aCause, author );
  }

  /**
   * ???????????????????? ?????????????? ???????????????????? ?????????????? ?????????? ???????????????? ?????????????? ?????????????????????? ?? ??????????????????
   * {@link ESkCommandState#TIMEOUTED}
   *
   * @param aSysdescrReader {@link ISkSysdescrReader} ???????????????? ???????????????????? ????????????????
   * @param aCommand {@link IDtoCommand} ??????????????
   * @return long long ???????????????????????? ?????????? (msec) ???????????????? ???????????????????? ???????????????????? ??????????????
   * @throws TsNullArgumentRtException ?????????? ???????????????? = ??????
   */
  private static long getCmdTimeout( ISkSysdescrReader aSysdescrReader, IDtoCommand aCommand ) {
    TsNullArgumentRtException.checkNulls( aSysdescrReader, aCommand );
    Gwid cmdGwid = aCommand.cmdGwid();
    ISkClassInfo classInfo = aSysdescrReader.findClassInfo( cmdGwid.classId() );
    if( classInfo != null ) {
      String cmdId = cmdGwid.propId();
      IDtoCmdInfo cmdInfo = classInfo.cmds().list().findByKey( cmdId );
      if( cmdInfo != null ) {
        return OP_EXECUTION_TIMEOUT.getValue( cmdInfo.params() ).asLong();
      }
    }
    // ?????????? ?????? ???????????????? ?????????????? ???? ??????????????. ???????????????? ???? ??????????????????
    return OP_EXECUTION_TIMEOUT.defaultValue().asLong();
  }

  /**
   * ???????????????????? ???????????? ?????????????????? "??????????????"
   *
   * @param aFrontend {@link IS5FrontendRear} ????????????????
   * @return {@link S5BaCommandsData} ???????????? ??????????????????. null: ???????????? ???? ????????????????????
   * @throws TsNullArgumentRtException ???????????????? = null
   */
  private static S5BaCommandsData findCommandsFrontendData( IS5FrontendRear aFrontend ) {
    return aFrontend.frontendData().findBackendAddonData( IBaCommands.ADDON_ID, S5BaCommandsData.class );
  }
}
