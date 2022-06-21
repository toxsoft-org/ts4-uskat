package org.toxsoft.uskat.core.api.cmdserv;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.core.api.cmdserv.ISkResources.*;

import java.io.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.av.utils.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Command state history item.
 * <p>
 * This is the final immutable class.
 *
 * @author hazard157
 */
public final class SkCommandState
    implements ITemporal<SkCommandState>, IParameterized, Serializable {

  private static final long serialVersionUID = 157157L;

  /**
   * Параметр: SKID-идентификатор автора изменения состояния команды.
   */
  public static final IDataDef OP_AUTHOR = create( SK_ID + "Author", VALOBJ, //$NON-NLS-1$
      TSID_NAME, STR_N_OP_AUTHOR, //
      TSID_DESCRIPTION, STR_D_OP_AUTHOR, //
      TSID_DEFAULT_VALUE, Skid.KEEPER.ent2str( Skid.NONE ), //
      TSID_IS_MANDATORY, AV_FALSE //
  );

  /**
   * Параметр: Текст причины изменения состояния команды.
   */
  public static final IDataDef OP_REASON = create( SK_ID + "Reason", STRING, //$NON-NLS-1$
      TSID_NAME, STR_N_OP_REASON, //
      TSID_DESCRIPTION, STR_D_OP_REASON, //
      TSID_DEFAULT_VALUE, AV_STR_EMPTY, //
      TSID_IS_MANDATORY, AV_FALSE //
  );

  /**
   * Registered keeper ID.
   */
  public static final String KEEPER_ID = "SkCommandState"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<SkCommandState> KEEPER =
      new AbstractEntityKeeper<>( SkCommandState.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, SkCommandState aEntity ) {
          aSw.writeTimestamp( aEntity.timestamp() );
          aSw.writeSeparatorChar();
          ESkCommandState.KEEPER.write( aSw, aEntity.state() );
          aSw.writeSeparatorChar();
          OptionSetKeeper.KEEPER.write( aSw, aEntity.params() );
        }

        @Override
        protected SkCommandState doRead( IStrioReader aSr ) {
          long timestamp = aSr.readTimestamp();
          aSr.ensureSeparatorChar();
          ESkCommandState state = ESkCommandState.KEEPER.read( aSr );
          aSr.ensureSeparatorChar();
          IOptionSet params = OptionSetKeeper.KEEPER.read( aSr );
          return new SkCommandState( timestamp, state, params );
        }

      };

  private final long            timestamp;
  private final ESkCommandState state;
  private final IOptionSet      params;

  /**
   * Constructor.
   *
   * @param aTimestamp long - moment when command state changed
   * @param aState {@link ESkCommandState} - the new command state
   * @param aParams {@link IOptionSet} - additional parameters (eg. the reason string or change author)
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkCommandState( long aTimestamp, ESkCommandState aState, IOptionSet aParams ) {
    timestamp = aTimestamp;
    state = TsNullArgumentRtException.checkNull( aState );
    TsNullArgumentRtException.checkNull( aParams );
    if( !aParams.isEmpty() ) {
      params = new OptionSet( aParams );
    }
    else {
      params = IOptionSet.NULL;
    }
  }

  /**
   * Constructor with reason and author.
   *
   * @param aTimestamp long - moment when command state changed
   * @param aState {@link ESkCommandState} - the new command state
   * @param aReason String - reason
   * @param aAuthor {@link Gwid} - concrete {@link Gwid} of the state change author
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException author {@link Gwid#isAbstract()} = <code>true</code>
   * @throws TsIllegalArgumentRtException author {@link Gwid#isMulti()} = <code>true</code>
   */
  public SkCommandState( long aTimestamp, ESkCommandState aState, String aReason, Gwid aAuthor ) {
    TsNullArgumentRtException.checkNulls( aState, aReason, aAuthor );
    TsIllegalArgumentRtException.checkTrue( aAuthor.isAbstract() );
    TsIllegalArgumentRtException.checkTrue( aAuthor.isMulti() );
    timestamp = aTimestamp;
    state = aState;
    IOptionSetEdit ops = new OptionSet();
    OP_REASON.setValue( ops, avStr( aReason ) );
    OP_AUTHOR.setValue( ops, avValobj( aAuthor ) );
    params = ops;
  }

  /**
   * Creates instance with empty {@link #params()}.
   *
   * @param aTimestamp long - moment when command state changed
   * @param aState {@link ESkCommandState} - the new command state
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkCommandState( long aTimestamp, ESkCommandState aState ) {
    this( aTimestamp, aState, IOptionSet.NULL );
  }

  // ------------------------------------------------------------------------------------
  // Comparable
  //

  @Override
  public int compareTo( SkCommandState aThat ) {
    if( aThat == null ) {
      throw new NullPointerException();
    }
    int c = Long.compare( timestamp, aThat.timestamp );
    if( c != 0 ) {
      return c;
    }
    return state.compareTo( aThat.state );
  }

  // ------------------------------------------------------------------------------------
  // ITemporal
  //

  @Override
  public long timestamp() {
    return timestamp;
  }

  // ------------------------------------------------------------------------------------
  // IParameterized
  //

  @Override
  public IOptionSet params() {
    return params;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the command state.
   *
   * @return {@link ESkCommandState} - the command state
   */
  public ESkCommandState state() {
    return state;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @Override
  public String toString() {
    return String.format( "%1$tF %1$tT %2$s", Long.valueOf( timestamp ), state.id() ); //$NON-NLS-1$
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof SkCommandState that ) {
      return this.timestamp == that.timestamp && this.state == that.state && this.params.equals( that.params );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + (int)(timestamp ^ (timestamp >>> 32));
    result = TsLibUtils.PRIME * result + state.hashCode();
    result = TsLibUtils.PRIME * result + params.hashCode();
    return result;
  }

}
