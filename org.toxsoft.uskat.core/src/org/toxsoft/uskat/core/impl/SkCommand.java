package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

/**
 * {@link ISkCommand} implementation.
 *
 * @author hazard157
 */
public class SkCommand
    implements ISkCommand {

  private final GenericChangeEventer eventer;

  private final long                           timestamp;
  private final String                         id;
  private final Gwid                           cmdGwid;
  private final Skid                           authorSkid;
  private final IOptionSetEdit                 argValues = new OptionSet();
  private final ITimedListEdit<SkCommandState> states    = new TimedList<>();

  /**
   * Constructor creates command with state {@link ESkCommandState#SENDING}.
   *
   * @param aTimestamp long - command sending start time
   * @param aId String - command instance ID
   * @param aCmdGwid {@link Gwid} - concrete GWID of the command
   * @param aAuthor {@link Skid} - author SKID
   * @param aArgs {@link IOptionSet} - argument values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   * @throws TsIllegalArgumentRtException GWID is not comannd concrete GWID
   */
  public SkCommand( long aTimestamp, String aId, Gwid aCmdGwid, Skid aAuthor, IOptionSet aArgs ) {
    id = StridUtils.checkValidIdPath( aId );
    TsNullArgumentRtException.checkNulls( aCmdGwid, aAuthor );
    TsIllegalArgumentRtException.checkTrue( aCmdGwid.isAbstract() );
    TsIllegalArgumentRtException.checkTrue( aCmdGwid.kind() != EGwidKind.GW_CMD );
    eventer = new GenericChangeEventer( this );
    timestamp = aTimestamp;
    cmdGwid = aCmdGwid;
    authorSkid = aAuthor;
    states.add( new SkCommandState( timestamp, ESkCommandState.SENDING ) );
  }

  // ------------------------------------------------------------------------------------
  // ITemporal
  //

  @Override
  public long timestamp() {
    return timestamp;
  }

  @Override
  public int compareTo( ISkCommand aThat ) {
    if( aThat == null ) {
      throw new NullPointerException();
    }
    int c = Long.compare( timestamp(), aThat.timestamp() );
    if( c != 0 ) {
      return c;
    }
    return cmdGwid.compareTo( aThat.cmdGwid() );
  }

  // ------------------------------------------------------------------------------------
  // ISkCommand
  //

  @Override
  public String instanceId() {
    return id;
  }

  @Override
  public Gwid cmdGwid() {
    return cmdGwid;
  }

  @Override
  public Skid authorSkid() {
    return authorSkid;
  }

  @Override
  public IOptionSet argValues() {
    return argValues;
  }

  @Override
  public ITimedList<SkCommandState> statesHistory() {
    return states;
  }

  @Override
  public IGenericChangeEventer stateEventer() {
    return eventer;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Add state to the history and fires an event.
   *
   * @param aNewState {@link SkCommandState} - new state of this command
   */
  public void papiAddState( SkCommandState aNewState ) {
    states.add( aNewState );
    eventer.fireChangeEvent();
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    return id + ": " + cmdGwid.toString(); //$NON-NLS-1$
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof ISkCommand that ) {
      return id.equals( that.instanceId() ) && timestamp == that.timestamp() && cmdGwid.equals( that.cmdGwid() )
          && authorSkid.equals( that.authorSkid() ) && argValues.equals( that.argValues() );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + id.hashCode();
    result = TsLibUtils.PRIME * result + (int)(timestamp ^ (timestamp >>> 32));
    result = TsLibUtils.PRIME * result + cmdGwid.hashCode();
    result = TsLibUtils.PRIME * result + authorSkid.hashCode();
    result = TsLibUtils.PRIME * result + argValues.hashCode();
    return result;
  }

}
