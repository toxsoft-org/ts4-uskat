package org.toxsoft.uskat.backend.memtext;

import static org.toxsoft.uskat.backend.memtext.IBackendMemtextConstants.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strid.idgen.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.derivative.*;
import org.toxsoft.core.tslib.coll.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * {@link IBaCommands} implementation.
 *
 * @author hazard157
 */
public class MtbBaCommands
    extends MtbAbstractAddon
    implements IBaCommands {

  private static final String KW_HISTORY = "CommandsHistory"; //$NON-NLS-1$

  /**
   * Generates instance IDs when creating {@link SkCommand} in {@link #sendCommand(Gwid, Skid, IOptionSet)}.
   */
  private final IStridGenerator cmdInstanceIdGenerator = new UuidStridGenerator();

  /**
   * List of GWIDs which have executor registered from the fromtend.
   * <p>
   * List is simle updated in {@link #setHandledCommandGwids(IGwidList)}.
   */
  private final GwidList listOfHandledCommandGwids = new GwidList();

  /**
   * Ring buffer with completed commands.
   */
  private final IRingBuffer<IDtoCompletedCommand> cmdsHistory;

  /**
   * Constructor.
   *
   * @param aOwner {@link MtbAbstractBackend} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public MtbBaCommands( MtbAbstractBackend aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_COMMANDS );
    int count = OPDEF_MAX_CMDS_COUNT.getValue( aOwner.argContext().params() ).asInt();
    count = TsMiscUtils.inRange( count, MIN_MAX_CMDS_COUNT, MIN_MAX_CMDS_COUNT );
    cmdsHistory = new RingBuffer<>( count );
  }

  // ------------------------------------------------------------------------------------
  // MtbAbstractAddon
  //

  @Override
  public void close() {
    // nop
  }

  @Override
  public void clear() {
    // nop
  }

  @Override
  protected void doWrite( IStrioWriter aSw ) {
    IList<IDtoCompletedCommand> ll = cmdsHistory.getItems();
    StrioUtils.writeCollection( aSw, KW_HISTORY, ll, DtoCompletedCommand.KEEPER, true );
  }

  @Override
  protected void doRead( IStrioReader aSr ) {
    IList<IDtoCompletedCommand> ll = StrioUtils.readCollection( aSr, KW_HISTORY, DtoCompletedCommand.KEEPER );
    cmdsHistory.clear();
    for( IDtoCompletedCommand c : ll ) {
      cmdsHistory.put( c );
    }
  }

  // ------------------------------------------------------------------------------------
  // Package API
  //

  @Override
  void papiRemoveEntitiesOfClassIdsBeforeSave( IStringList aClassIds ) {
    // retrieve from buffer cmd that shall remain in storage
    IListEdit<IDtoCompletedCommand> cmdsToRemain = new ElemLinkedBundleList<>();
    while( !cmdsHistory.isEmpty() ) {
      IDtoCompletedCommand e = cmdsHistory.get();
      if( aClassIds.hasElem( e.cmd().cmdGwid().classId() ) ) {
        cmdsToRemain.add( e );
      }
    }
    // put back remained cmds to buffer
    for( IDtoCompletedCommand e : cmdsToRemain ) {
      cmdsHistory.put( e );
    }
  }

  // ------------------------------------------------------------------------------------
  //
  //

  @Override
  public SkCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    String instanceId = cmdInstanceIdGenerator.nextId();
    long time = System.currentTimeMillis();
    DtoCommand dtoCmd = new DtoCommand( time, instanceId, aCmdGwid, aAuthorSkid, aArgs );

    /**
     * FIXME remember command, start to count the elapsed time and timeout comand if needed
     */

    GtMessage msg = BaCommandsMsgExecCmd.INSTANCE.makeMessage( dtoCmd );
    owner().frontend().onBackendMessage( msg );
    return new SkCommand( dtoCmd );
  }

  @Override
  public void setHandledCommandGwids( IGwidList aGwids ) {
    listOfHandledCommandGwids.setAll( aGwids );
  }

  @Override
  public void changeCommandState( DtoCommandStateChangeInfo aStateChangeInfo ) {
    GtMessage msg = BaCommandsMsgChangeState.INSTANCE.makeMessage( aStateChangeInfo );
    owner().frontend().onBackendMessage( msg );
  }

  @Override
  public IGwidList listGloballyHandledCommandGwids() {
    return listOfHandledCommandGwids;
  }

  @Override
  public void saveToHistory( IDtoCompletedCommand aCompletedCommand ) {
    cmdsHistory.put( aCompletedCommand );
  }

  @Override
  public ITimedList<IDtoCompletedCommand> queryObjCommands( IQueryInterval aInterval, Gwid aGwid ) {
    TimedList<IDtoCompletedCommand> result = new TimedList<>();
    for( IDtoCompletedCommand e : cmdsHistory.getItems() ) {
      if( TimeUtils.contains( aInterval, e.timestamp() ) ) {
        if( e.cmd().cmdGwid().skid().equals( aGwid.skid() ) ) {
          if( aGwid.isPropMulti() || aGwid.propId().equals( e.cmd().cmdGwid().propId() ) ) {
            result.add( e );
          }
        }
      }
    }
    return result;
  }

}
