package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.ctx.*;
import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
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

  /**
   * Service creator singleton.
   */
  public static final ISkServiceCreator<AbstractSkService> CREATOR = SkCoreServCommands::new;

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

  @Override
  public ISkCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    // TODO Auto-generated method stub
    return null;
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
