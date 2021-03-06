package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.events.change.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

/**
 * {@link ISkCommand} implementation.
 *
 * @author hazard157
 */
public final class SkCommand
    implements ISkCommand {

  private final GenericChangeEventer eventer;

  private final IDtoCommand cmd;

  private final ITimedListEdit<SkCommandState> states = new TimedList<>();

  /**
   * Constructor creates command with state {@link ESkCommandState#SENDING}.
   *
   * @param aCmd {@link IDtoCommand} - command info
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException ID is not an IDpath
   * @throws TsIllegalArgumentRtException GWID is not comannd concrete GWID
   */
  public SkCommand( IDtoCommand aCmd ) {
    cmd = TsNullArgumentRtException.checkNull( aCmd );
    eventer = new GenericChangeEventer( this );
    states.add( new SkCommandState( aCmd.timestamp(), ESkCommandState.SENDING ) );
  }

  // ------------------------------------------------------------------------------------
  // ITemporal
  //

  @Override
  public long timestamp() {
    return cmd.timestamp();
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
    return cmd.compareTo( ((SkCommand)aThat).cmd );
  }

  // ------------------------------------------------------------------------------------
  // ISkCommand
  //

  @Override
  public String instanceId() {
    return cmd.instanceId();
  }

  @Override
  public Gwid cmdGwid() {
    return cmd.cmdGwid();
  }

  @Override
  public Skid authorSkid() {
    return cmd.authorSkid();
  }

  @Override
  public IOptionSet argValues() {
    return cmd.argValues();
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
    return cmd.toString();
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof SkCommand that ) {
      return cmd.equals( that.cmd );
    }
    return false;
  }

  @Override
  public int hashCode() {
    return cmd.hashCode();
  }

}
