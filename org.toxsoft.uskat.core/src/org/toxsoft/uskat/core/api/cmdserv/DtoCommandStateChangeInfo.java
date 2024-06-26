package org.toxsoft.uskat.core.api.cmdserv;

import java.io.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Unit of information about single change of the command state.
 *
 * @author hazard157
 */
public final class DtoCommandStateChangeInfo
    implements ITimestampable, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Registerd keeper ID.
   */
  public static final String KEEPER_ID = "DtoCommandStateChangeInfo"; //$NON-NLS-1$

  /**
   * The keeper singleton.
   */
  public static final IEntityKeeper<DtoCommandStateChangeInfo> KEEPER =
      new AbstractEntityKeeper<>( DtoCommandStateChangeInfo.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, DtoCommandStateChangeInfo aEntity ) {
          aSw.writeAsIs( aEntity.instanceId() );
          aSw.writeSeparatorChar();
          SkCommandState.KEEPER.write( aSw, aEntity.state() );
        }

        @Override
        protected DtoCommandStateChangeInfo doRead( IStrioReader aSr ) {
          String commandId = aSr.readIdPath();
          aSr.ensureSeparatorChar();
          SkCommandState state = SkCommandState.KEEPER.read( aSr );
          return new DtoCommandStateChangeInfo( commandId, state );
        }
      };

  private final String         commandId;
  private final SkCommandState state;

  /**
   * Constructor
   *
   * @param aCommandInstanceId String - the command instance ID
   * @param aState {@link SkCommandState} - new state of the command
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public DtoCommandStateChangeInfo( String aCommandInstanceId, SkCommandState aState ) {
    commandId = StridUtils.checkValidIdPath( aCommandInstanceId );
    state = TsNullArgumentRtException.checkNull( aState );
  }

  // ------------------------------------------------------------------------------------
  // ITimestampable
  //

  @Override
  public long timestamp() {
    return state.timestamp();
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the command instance ID.
   *
   * @return String - the command instance ID
   */
  public String instanceId() {
    return commandId;
  }

  /**
   * Returns state of the command.
   *
   * @return {@link SkCommandState} - new state of the command
   */
  public SkCommandState state() {
    return state;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    return commandId;
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof DtoCommandStateChangeInfo that ) {
      return commandId.equals( that.commandId ) && state.equals( that.state );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + commandId.hashCode();
    result = TsLibUtils.PRIME * result + state.hashCode();
    return result;
  }

}
