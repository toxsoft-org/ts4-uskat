package org.toxsoft.uskat.core.impl.dto;

import java.io.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.cmdserv.*;

/**
 * {@link IDtoCommand} implementation.
 *
 * @author hazard157
 */
public final class DtoCommand
    implements IDtoCommand, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Registered keeper ID.
   */
  public static final String KEEPER_ID = "DtoCommand"; //$NON-NLS-1$

  /**
   * The keeper singleton.
   */
  public static final IEntityKeeper<IDtoCommand> KEEPER =
      new AbstractEntityKeeper<>( IDtoCommand.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, IDtoCommand aEntity ) {
          aSw.writeTimestamp( aEntity.timestamp() );
          aSw.writeSeparatorChar();
          aSw.writeAsIs( aEntity.instanceId() );
          aSw.writeSeparatorChar();
          Gwid.KEEPER.write( aSw, aEntity.cmdGwid() );
          aSw.writeSeparatorChar();
          Skid.KEEPER.write( aSw, aEntity.authorSkid() );
          aSw.writeSeparatorChar();
          OptionSetKeeper.KEEPER.write( aSw, aEntity.argValues() );
        }

        @Override
        protected IDtoCommand doRead( IStrioReader aSr ) {
          long timestamp = aSr.readTimestamp();
          aSr.ensureSeparatorChar();
          String instanceId = aSr.readIdPath();
          aSr.ensureSeparatorChar();
          Gwid cmdGwid = Gwid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          Skid authorSkid = Skid.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          IOptionSet argValues = OptionSetKeeper.KEEPER.read( aSr );
          return new DtoCommand( timestamp, instanceId, cmdGwid, authorSkid, argValues );
        }
      };

  private final long           timestamp;
  private final String         id;
  private final Gwid           cmdGwid;
  private final Skid           authorSkid;
  private final IOptionSetEdit argValues = new OptionSet();

  /**
   * Constructor.
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
  public DtoCommand( long aTimestamp, String aId, Gwid aCmdGwid, Skid aAuthor, IOptionSet aArgs ) {
    id = StridUtils.checkValidIdPath( aId );
    TsNullArgumentRtException.checkNulls( aCmdGwid, aAuthor );
    TsIllegalArgumentRtException.checkTrue( aCmdGwid.isAbstract() );
    TsIllegalArgumentRtException.checkTrue( aCmdGwid.kind() != EGwidKind.GW_CMD );
    TsNullArgumentRtException.checkNulls( aId, aCmdGwid, aAuthor, aArgs );
    timestamp = aTimestamp;
    cmdGwid = aCmdGwid;
    authorSkid = aAuthor;
    argValues.addAll( aArgs );
  }

  // ------------------------------------------------------------------------------------
  // ITimestampable
  //

  @Override
  public long timestamp() {
    return timestamp;
  }

  // ------------------------------------------------------------------------------------
  // IDtoCommand
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
  public IOptionSetEdit argValues() {
    return argValues;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    Long lt = Long.valueOf( timestamp );
    return String.format( "%tF %tT %s %s %s", lt, lt, id, cmdGwid.toString(), authorSkid.toString() ); //$NON-NLS-1$
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof IDtoCommand that ) {
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

  // ------------------------------------------------------------------------------------
  // Comparable
  //

  @Override
  public int compareTo( IDtoCommand aThat ) {
    if( aThat == null ) {
      throw new NullPointerException();
    }
    int c = Long.compare( timestamp(), aThat.timestamp() );
    if( c != 0 ) {
      return c;
    }
    return cmdGwid.compareTo( aThat.cmdGwid() );
  }

}
