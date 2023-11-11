package org.toxsoft.uskat.backend.sqlite.addons;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.backend.sqlite.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * {@link IBaCommands} implementation for {@link SkBackendSqlite}.
 *
 * @author hazard157
 */
public class BaCommands
    extends AbstractAddon
    implements IBaCommands {

  /**
   * Constructor.
   *
   * @param aOwner {@link SkBackendSqlite} - the owner backend
   */
  public BaCommands( SkBackendSqlite aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_COMMANDS );
    // TODO Auto-generated constructor stub
  }

  // ------------------------------------------------------------------------------------
  // AbstractAddon
  //

  @Override
  protected void doInit() {
    // TODO doInit()
  }

  @Override
  public void doClose() {
    // nop
  }

  @Override
  public void clear() {
    // TODO clear()
  }

  // ------------------------------------------------------------------------------------
  // IBaCommands
  //

  @Override
  public SkCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setHandledCommandGwids( IGwidList aGwids ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void changeCommandState( DtoCommandStateChangeInfo aStateChangeInfo ) {
    // TODO Auto-generated method stub

  }

  @Override
  public IGwidList listGloballyHandledCommandGwids() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void saveToHistory( IDtoCompletedCommand aCompletedCommand ) {
    // TODO Auto-generated method stub

  }

  @Override
  public ITimedList<IDtoCompletedCommand> queryObjCommands( ITimeInterval aInterval, Gwid aGwid ) {
    // TODO Auto-generated method stub
    return null;
  }

}
