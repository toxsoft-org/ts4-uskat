package org.toxsoft.uskat.backend.memtext;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.cmdserv.*;
import org.toxsoft.uskat.core.backend.*;
import org.toxsoft.uskat.core.backend.api.*;
import org.toxsoft.uskat.core.impl.*;

public class MtbBaCommands
    extends MtbAbstractAddon
    implements IBaCommands {

  /**
   * Constructor.
   *
   * @param aOwner {@link MtbAbstractBackend} - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public MtbBaCommands( MtbAbstractBackend aOwner ) {
    super( aOwner, ISkBackendHardConstant.BAINF_COMMANDS );
  }

  // ------------------------------------------------------------------------------------
  // MtbAbstractAddon
  //

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

  @Override
  public void clear() {
    // TODO Auto-generated method stub

  }

  @Override
  protected void doWrite( IStrioWriter aSw ) {
    // nop
  }

  @Override
  protected void doRead( IStrioReader aSr ) {
    // nop
  }

  // ------------------------------------------------------------------------------------
  // Package API
  //

  @Override
  void papiRemoveEntitiesOfClassIdsBeforeSave( IStringList aClassIds ) {

    // TODO реализовать papiRemoveEntitiesOfClassIdsBeforeSave()
    throw new TsUnderDevelopmentRtException( "papiRemoveEntitiesOfClassIdsBeforeSave()" );

  }

  // ------------------------------------------------------------------------------------
  //
  //

  @Override
  public SkCommand sendCommand( Gwid aCmdGwid, Skid aAuthorSkid, IOptionSet aArgs ) {
    // TODO Auto-generated method stub
    // TODO реализовать MtbBaCommands.sendCommand()
    throw new TsUnderDevelopmentRtException( "MtbBaCommands.sendCommand()" );
  }

  @Override
  public void setExcutableCommandGwids( IGwidList aGwids ) {
    // TODO Auto-generated method stub

  }

  @Override
  public void changeCommandState( DtoCommandStateChangeInfo aStateChangeInfo ) {
    // TODO Auto-generated method stub

  }

  @Override
  public ITimedList<IDtoCompletedCommand> queryCommands( IQueryInterval aInterval, IGwidList aNeededGwids ) {
    // TODO Auto-generated method stub
    return null;
  }

}
