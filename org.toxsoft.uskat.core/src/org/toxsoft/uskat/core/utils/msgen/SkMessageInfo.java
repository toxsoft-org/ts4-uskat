package org.toxsoft.uskat.core.utils.msgen;

import static org.toxsoft.core.tslib.utils.TsLibUtils.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.bricks.strio.impl.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * {@link ISkMessageInfo} editable implementation.
 *
 * @author hazard157
 */
public final class SkMessageInfo
    implements ISkMessageInfo {

  /**
   * The registered keeper ID.
   */
  public static final String KEEPER_ID = "SkMessageInfo"; //$NON-NLS-1$

  /**
   * The keeper singleton.
   */
  public static final IEntityKeeper<ISkMessageInfo> KEEPER =
      new AbstractEntityKeeper<>( ISkMessageInfo.class, EEncloseMode.ENCLOSES_BASE_CLASS, NONE ) {

        @Override
        protected void doWrite( IStrioWriter aSw, ISkMessageInfo aEntity ) {
          aSw.writeQuotedString( aEntity.fmtStr() );
          aSw.writeSeparatorChar();
          StrioUtils.writeStringMap( aSw, EMPTY_STRING, aEntity.usedUgwies(), Ugwi.KEEPER, false );
        }

        @Override
        protected ISkMessageInfo doRead( IStrioReader aSr ) {
          String fmtStr = aSr.readQuotedString();
          aSr.ensureSeparatorChar();
          IStringMap<Ugwi> uu = StrioUtils.readStringMap( aSr, EMPTY_STRING, Ugwi.KEEPER );
          return new SkMessageInfo( fmtStr, uu );
        }
      };

  private final String               fmtStr;
  private final IStringMapEdit<Ugwi> usedUgwies;

  /**
   * Constructor.
   *
   * @param aFmtStr String - the format string
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SkMessageInfo( String aFmtStr ) {
    this( aFmtStr, IStringMap.EMPTY );
  }

  /**
   * Constructor.
   *
   * @param aFmtStr String - the format string
   * @param aUsedUgwies {@link IStringMap}&lt;{@link Ugwi}&gt; - used UGWIs map "key IDpath" - "UGWI"
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any key is not an IDpath
   */
  public SkMessageInfo( String aFmtStr, IStringMap<Ugwi> aUsedUgwies ) {
    TsNullArgumentRtException.checkNulls( aFmtStr, aUsedUgwies );
    fmtStr = aFmtStr;
    usedUgwies = new StridMap<>();
    usedUgwies.putAll( aUsedUgwies );
  }

  /**
   * Static constructor.
   *
   * @param aFmtStr String - the format string
   * @param aKeysAndUgwies Object[] - String / UGWI pairs for {@link #usedUgwies()} values
   * @return {@link SkMessageInfo} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any key is not an IDpath
   * @throws TsIllegalArgumentRtException array elemts count is not even
   * @throws ClassCastException odd elements <code>aKeysAndUgwies</code> array is not {@link String}
   * @throws ClassCastException even elements <code>aKeysAndUgwies</code> array is not {@link Ugwi}
   */
  public static SkMessageInfo create( String aFmtStr, Object... aKeysAndUgwies ) {
    TsNullArgumentRtException.checkNull( aKeysAndUgwies );
    TsIllegalArgumentRtException.checkTrue( aKeysAndUgwies.length / 2 != 0 );
    SkMessageInfo minf = new SkMessageInfo( aFmtStr );
    for( int i = 0; i < aKeysAndUgwies.length; i += 2 ) {
      String key = String.class.cast( aKeysAndUgwies[i] );
      Ugwi ugwi = Ugwi.class.cast( aKeysAndUgwies[i + 1] );
      minf.addUgwi( key, ugwi );
    }
    return minf;
  }

  // ------------------------------------------------------------------------------------
  // ISkMessageInfo
  //

  @Override
  public String fmtStr() {
    return fmtStr;
  }

  @Override
  public IStringMap<Ugwi> usedUgwies() {
    return usedUgwies;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Add new or replaces existing used UGWI in the map {@link #usedUgwies()}.
   *
   * @param aKey String - the key (IDpath )
   * @param aUgwi Ugwi - the UGWI
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException the key is not an IDpath
   */
  public void addUgwi( String aKey, Ugwi aUgwi ) {
    StridUtils.checkValidIdPath( aKey );
    usedUgwies.put( aKey, aUgwi );
  }

}
