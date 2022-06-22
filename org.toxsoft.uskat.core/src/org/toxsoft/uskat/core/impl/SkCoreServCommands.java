package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
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
import org.toxsoft.uskat.core.devapi.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * {@link ISkCommandService} implementation.
 *
 * @author hazard157
 */
public class SkCoreServCommands
    extends AbstractSkCoreService
    implements ISkCommandService {

  static class Eventer
      extends AbstractTsEventer<ISkCommandServiceListener> {

    /**
     * Non <code>null</code> value means that there are penfding event.
     */
    private IGwidList excutableGwidsList = null;

    @Override
    protected boolean doIsPendingEvents() {
      return excutableGwidsList != null;
    }

    @Override
    protected void doFirePendingEvents() {
      reallyFire( excutableGwidsList );
    }

    @Override
    protected void doClearPendingEvents() {
      excutableGwidsList = null;
    }

    private void reallyFire( IGwidList aList ) {
      for( ISkCommandServiceListener l : listeners() ) {
        l.onExecutableCommandGwidsChanged( aList );
      }
    }

    void fireEvent( IGwidList aList ) {
      if( isFiringPaused() ) {
        excutableGwidsList = new GwidList( aList );
        return;
      }
      reallyFire( aList );
    }

  }

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServCommands::new;

  /**
   * {@link #history()} impementation
   */
  private final ITemporalsHistory<IDtoCompletedCommand> history = ( aInterval, aGwids ) -> {
    TsNullArgumentRtException.checkNulls( aInterval, aGwids );
    return ba().baCommands().queryCommands( aInterval, aGwids );
  };

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

  /**
   * Cache of GWIDs which have executor registered in this service.
   * <p>
   * Cache is updated every time when {@link #registerExecutor(ISkCommandExecutor, IGwidList)} or
   * {@link #unregisterExecutor(ISkCommandExecutor)} are called.
   */
  private final GwidList cacheOfExecutableCommandGwids = new GwidList();

  private final Eventer eventer = new Eventer();

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkCoreServCommands( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
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
    // nop
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  /**
   * Updates {@link #cacheOfExecutableCommandGwids} from {@link #registeredExecutors} GWIDs.
   */
  private void updateCacheOfExecutableCommandGwids() {
    IListEdit<Gwid> ll = new ElemLinkedBundleList<>();
    // iterate over all GWIDs in #registeredExecutorsMap
    for( IGwidList gl : registeredExecutors.values() ) {
      for( Gwid g : gl ) {
        gwidService().updateGwidsOfIntereset( ll, g, ESkClassPropKind.CMD );
      }
    }
    cacheOfExecutableCommandGwids.setAll( ll );
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
       * are considered as hints, not mandatory restictions. USkat does NOT checks argument values against constraints.
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
    updateCacheOfExecutableCommandGwids();
    ba().baCommands().setExcutableCommandGwids( cacheOfExecutableCommandGwids );
    eventer.fireEvent( cacheOfExecutableCommandGwids );
  }

  @Override
  public IGwidList listExecutableCommandGwids() {
    return cacheOfExecutableCommandGwids;
  }

  @Override
  public void unregisterExecutor( ISkCommandExecutor aExecutor ) {
    if( registeredExecutors.removeByKey( aExecutor ) != null ) {
      updateCacheOfExecutableCommandGwids();
      ba().baCommands().setExcutableCommandGwids( cacheOfExecutableCommandGwids );
      eventer.fireEvent( cacheOfExecutableCommandGwids );
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
  public ITemporalsHistory<IDtoCompletedCommand> history() {
    return history;
  }

  @Override
  public ITsEventer<ISkCommandServiceListener> eventer() {
    return eventer;
  }

}
