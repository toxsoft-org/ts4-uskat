package org.toxsoft.uskat.core.api.evserv;

import java.io.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.bricks.time.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * USkat system event.
 * <p>
 * This is an immutable class.
 *
 * @author hazard157
 */
public final class SkEvent
    implements ITemporal<SkEvent>, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Keeper ID.
   */
  public static final String KEEPER_ID = "SkEvent"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<SkEvent> KEEPER =
      new AbstractEntityKeeper<>( SkEvent.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, SkEvent aEntity ) {
          aSw.writeTimestamp( aEntity.timestamp() );
          aSw.writeSeparatorChar();
          Gwid.KEEPER.write( aSw, aEntity.eventGwid() );
          aSw.writeSeparatorChar();
          OptionSetKeeper.KEEPER.write( aSw, aEntity.paramValues() );
        }

        @Override
        protected SkEvent doRead( IStrioReader aSr ) {
          long timestamp = aSr.readTimestamp();
          aSr.ensureSeparatorChar();
          Gwid gwid = Gwid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          IOptionSet params = OptionSetKeeper.KEEPER.read( aSr );
          return new SkEvent( timestamp, gwid, params );
        }

      };

  private final long       timestamp;
  private final Gwid       eventGwid;
  private final IOptionSet paramValues;

  /**
   * Constructor.
   *
   * @param aTimestamp long - event timestamp as milliseconds from epoch
   * @param aEvGwid {@link Gwid} - the event concrete GWID of kind {@link EGwidKind#GW_EVENT}
   * @param aValues {@link IOptionSet} - the event parameters
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException {@link Gwid#kind()} != {@link EGwidKind#GW_EVENT}
   * @throws TsIllegalArgumentRtException {@link Gwid#isAbstract()} == <code>true</code>
   */
  public SkEvent( long aTimestamp, Gwid aEvGwid, IOptionSet aValues ) {
    TsNullArgumentRtException.checkNulls( aValues, aEvGwid );
    TsIllegalArgumentRtException.checkTrue( aEvGwid.kind() != EGwidKind.GW_EVENT );
    TsIllegalArgumentRtException.checkTrue( aEvGwid.isAbstract() );
    timestamp = aTimestamp;
    eventGwid = aEvGwid;
    if( !aValues.isEmpty() ) {
      paramValues = new OptionSet( aValues );
    }
    else {
      paramValues = IOptionSet.NULL;
    }
  }

  private SkEvent( String aClassId, String aStrid, String aEventId, Object... aParams ) {
    timestamp = System.currentTimeMillis();
    eventGwid = Gwid.createEvent( aClassId, aStrid, aEventId );
    paramValues = OptionSetUtils.createOpSet( aParams );
  }

  /**
   * Static constructor.
   *
   * @param aSource {@link Skid} - event source object SKID
   * @param aEventId String the event ID
   * @param aParams Object[] - the event parameters as for {@link OptionSetUtils#createOpSet(Object...)}
   * @return {@link SkEvent} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException aEventId is not an IDpath
   * @throws TsIllegalArgumentRtException {@link Gwid#isAbstract()} == <code>true</code>
   */
  public static SkEvent create( Skid aSource, String aEventId, Object... aParams ) {
    TsNullArgumentRtException.checkNull( aSource );
    StridUtils.checkValidIdPath( aEventId );
    return new SkEvent( aEventId, aEventId, aEventId, aParams );
  }

  // ------------------------------------------------------------------------------------
  // Comparable
  //

  @Override
  public int compareTo( SkEvent aThat ) {
    if( aThat == null ) {
      throw new NullPointerException();
    }
    int c = Long.compare( this.timestamp, aThat.timestamp );
    if( c == 0 ) {
      c = this.eventGwid.compareTo( aThat.eventGwid );
    }
    return c;
  }

  // ------------------------------------------------------------------------------------
  // ITemporal
  //

  @Override
  public long timestamp() {
    return timestamp;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the event concrete GWID.
   *
   * @return {@link Gwid} - the event GWID
   */
  public Gwid eventGwid() {
    return eventGwid;
  }

  /**
   * Returns event parameters values.
   *
   * @return {@link IOptionSet} - parameters values
   */
  public IOptionSet paramValues() {
    return paramValues;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    return TimeUtils.timestampToString( timestamp ) + ' ' + eventGwid.toString() + ' '
        + OptionSetUtils.humanReadable( paramValues );
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof SkEvent that ) {
      return timestamp != that.timestamp && eventGwid.equals( that.eventGwid )
          && paramValues.equals( that.paramValues );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + (int)(timestamp ^ (timestamp >>> 32));
    result = TsLibUtils.PRIME * result + eventGwid.hashCode();
    result = TsLibUtils.PRIME * result + paramValues.hashCode();
    return result;
  }

}
