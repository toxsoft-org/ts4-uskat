package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.impl.ISkResources.*;

import org.toxsoft.core.tslib.av.*;
import org.toxsoft.core.tslib.av.errors.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
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

import ru.uskat.common.dpu.rt.cmds.*;
import ru.uskat.core.api.sysdescr.*;
import ru.uskat.core.common.skobject.*;
import ru.uskat.core.impl.*;

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
  private final IStringMapEdit<IDtoCommand> executingCmds = new StringMap<>();

  /**
   * Constructor.
   *
   * @param aCoreApi {@link IDevCoreApi} - owner core API implementation
   */
  SkCoreServCommands( IDevCoreApi aCoreApi ) {
    super( SERVICE_ID, aCoreApi );
  }

  // ------------------------------------------------------------------------------------
  // ApiWrapAbstractSkService
  //

  @Override
  protected void doInit( ITsContextRo aArgs ) {
    // TODO Auto-generated method stub
  }

  @Override
  protected void doClose() {
    // TODO Auto-generated method stub
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
    // Передача команды
    IDtoCommand cmd = ba().baCommands().sendCommand( aCmdGwid, aAuthorSkid, aArgs );
    // Сохранение команды в карте выполняемых команд
    executingCmds.put( cmd.id(), cmd );
    // Формирование результата
    return new SkCommand( new DtoCompletedCommand( cmd ) );
  }

  @Override
  public void registerExecutor( ISkCommandExecutor aExecutor, IGwidList aCmdGwids ) {
    // TODO Auto-generated method stub

  }

  @Override
  public IGwidList getExcutableCommandGwids() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void unregisterExecutor( ISkCommandExecutor aExecutor ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void changeCommandState( DtoCommandStateChangeInfo aStateChangeInfo ) {
    // TODO Auto-generated method stub

  }

  @Override
  public ITemporalsHistory<ISkCommand> history() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ITsEventer<ISkCommandServiceListener> eventer() {
    // TODO Auto-generated method stub
    return null;
  }

  // ------------------------------------------------------------------------------------
  // implementation
  //

  // ------------------------------------------------------------------------------------
  // ISkCommandService
  //

}
