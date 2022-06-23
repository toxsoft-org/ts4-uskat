package org.toxsoft.uskat.core.impl.dto;

import java.io.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

/**
 * {@link IDtoCompletedCommand} implementation.
 *
 * @author hazard157
 */
public class DtoCompletedCommand
    implements IDtoCompletedCommand, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * The registered keeper ID.
   */
  public static final String KEEPER_ID = "DtoCompletedCommand"; //$NON-NLS-1$

  /**
   * The keeper singleton.
   */
  public static final IEntityKeeper<IDtoCompletedCommand> KEEPER =
      new AbstractEntityKeeper<>( IDtoCompletedCommand.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, IDtoCompletedCommand aEntity ) {
          DtoCommand.KEEPER.write( aSw, aEntity.cmd() );
          aSw.writeSeparatorChar();
          SkCommandState.KEEPER.writeColl( aSw, aEntity.statesHistory(), false );
        }

        @Override
        protected IDtoCompletedCommand doRead( IStrioReader aSr ) {
          IDtoCommand cmd = DtoCommand.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          IList<SkCommandState> history = SkCommandState.KEEPER.readColl( aSr );
          return new DtoCompletedCommand( cmd, new TimedList<>( history ) );
        }
      };

  private final IDtoCommand                    command;
  private final ITimedListEdit<SkCommandState> statesHistory = new TimedList<>();

  /**
   * Constructor.
   *
   * @param aCommand {@link IDtoCommand} - theo commands
   * @param aStates {@link ITimedList}&lt;{@link SkCommandState}&gt; - and it's history
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public DtoCompletedCommand( IDtoCommand aCommand, ITimedList<SkCommandState> aStates ) {
    command = TsNullArgumentRtException.checkNull( aCommand );
    statesHistory.addAll( aStates );
  }

  // ------------------------------------------------------------------------------------
  // IDtoCompletedCommand
  //

  @Override
  public long timestamp() {
    return command.timestamp();
  }

  @Override
  public IDtoCommand cmd() {
    return command;
  }

  @Override
  public ITimedListEdit<SkCommandState> statesHistory() {
    return statesHistory;
  }

  // ------------------------------------------------------------------------------------
  // Comparable
  //

  @Override
  public int compareTo( IDtoCompletedCommand aThat ) {
    if( aThat == null ) {
      throw new NullPointerException();
    }
    return Long.compare( timestamp(), aThat.timestamp() );
  }

}
