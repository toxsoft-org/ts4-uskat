package org.toxsoft.uskat.sysext.refbooks.impl;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.EEncloseMode;
import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.core.tslib.bricks.strio.IStrioReader;
import org.toxsoft.core.tslib.bricks.strio.IStrioWriter;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.core.tslib.utils.errors.TsIllegalArgumentRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.sysext.refbooks.ISkRefbookDpuItemInfo;

/**
 * {@link ISkRefbookDpuItemInfo} implementation.
 *
 * @author goga
 */
public final class SkRefbookDpuItemInfo
    implements ISkRefbookDpuItemInfo {

  /**
   * Singleton keeper.
   */
  public static final IEntityKeeper<ISkRefbookDpuItemInfo> KEEPER =
      new AbstractEntityKeeper<>( ISkRefbookDpuItemInfo.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, ISkRefbookDpuItemInfo aEntity ) {
          aSw.writeAsIs( aEntity.strid() );
          aSw.writeSeparatorChar();
          OptionSetKeeper.KEEPER.write( aSw, aEntity.attrs() );
        }

        @Override
        protected ISkRefbookDpuItemInfo doRead( IStrioReader aSr ) {
          String strid = aSr.readIdPath();
          aSr.ensureSeparatorChar();
          IOptionSet attrs = OptionSetKeeper.KEEPER.read( aSr );
          return new SkRefbookDpuItemInfo( strid, attrs );
        }
      };

  private final String         strid;
  private final IOptionSetEdit attrs = new OptionSet();

  /**
   * Constructor.
   *
   * @param aStrid String - item object strid
   * @param aAttrs {@link IOptionSet} - attributes values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   */
  public SkRefbookDpuItemInfo( String aStrid, IOptionSet aAttrs ) {
    strid = StridUtils.checkValidIdPath( aStrid );
    attrs.addAll( aAttrs );
  }

  /**
   * Static constructor.
   *
   * @param aId String - refbook item ID
   * @param aName String - name
   * @param aDescription String - description
   * @param aAttrs Object[] - attribute values as of {@link OptionSetUtils#createOpSet(Object...)}
   * @return {@link SkRefbookDpuItemInfo} - created instance
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   */
  public static SkRefbookDpuItemInfo create1( String aId, String aName, String aDescription, Object... aAttrs ) {
    IOptionSetEdit attrs = OptionSetUtils.createOpSet( aAttrs );
    attrs.setStr( TSID_NAME, aName );
    attrs.setStr( TSID_DESCRIPTION, aDescription );
    return new SkRefbookDpuItemInfo( aId, attrs );
  }

  /**
   * Static constructor.
   *
   * @param aId String - refbook item ID
   * @param aName String - name
   * @param aDescription String - description
   * @param aAttrs {@link IStringMap}&lt;{@link IAtomicValue}&gt;- attribute values map
   * @return {@link SkRefbookDpuItemInfo} - created instance
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   */
  public static SkRefbookDpuItemInfo create2( String aId, String aName, String aDescription,
      IStringMap<IAtomicValue> aAttrs ) {
    IOptionSetEdit attrs = new OptionSet();
    for( String aid : aAttrs.keys() ) {
      attrs.setValue( aid, aAttrs.getByKey( aid ) );
    }
    attrs.setStr( TSID_NAME, aName );
    attrs.setStr( TSID_DESCRIPTION, aDescription );
    return new SkRefbookDpuItemInfo( aId, attrs );
  }

  // ------------------------------------------------------------------------------------
  // ISkRefbookDpuItemInfo
  //

  @Override
  public String strid() {
    return strid;
  }

  @Override
  public IOptionSetEdit attrs() {
    return attrs;
  }

  // ------------------------------------------------------------------------------------
  // Object
  //

  @SuppressWarnings( "nls" )
  @Override
  public String toString() {
    return getClass().getSimpleName() + ": " + strid;
  }

  @Override
  public boolean equals( Object aThat ) {
    if( aThat == this ) {
      return true;
    }
    if( aThat instanceof SkRefbookDpuItemInfo that ) {
      return strid.equals( that.strid ) && attrs.equals( that.attrs );
    }
    return false;
  }

  @Override
  public int hashCode() {
    int result = TsLibUtils.INITIAL_HASH_CODE;
    result = TsLibUtils.PRIME * result + strid.hashCode();
    result = TsLibUtils.PRIME * result + attrs.hashCode();
    return result;
  }

}
