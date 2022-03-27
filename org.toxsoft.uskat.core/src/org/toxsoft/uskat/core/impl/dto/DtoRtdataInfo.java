package org.toxsoft.uskat.core.impl.dto;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.impl.dto.IDtoHardConstants.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * {@link IDtoRtdataInfo} implementation.
 * <p>
 * As {@link StridableParameterized} extension this class stores all fields in {@link #params()}:
 * <ul>
 * <li>{@link #dataType()} - as option {@link IDtoHardConstants#OPDEF_DATA_TYPE};</li>
 * <li>{@link #isCurr()} - as option {@link IDtoHardConstants#OPDEF_IS_CURR};</li>
 * <li>{@link #isHist()} - as option {@link IDtoHardConstants#OPDEF_IS_HIST};</li>
 * <li>{@link #isSync()} - as option {@link IDtoHardConstants#OPDEF_IS_SYNC};</li>
 * <li>{@link #syncDataDeltaT()} - as option {@link IDtoHardConstants#OPDEF_SYNC_DATA_DELTA_T}.</li>
 * </ul>
 *
 * @author hazard157
 */
public final class DtoRtdataInfo
    extends DtoAbstractClassPropInfoBase
    implements IDtoRtdataInfo {

  private static final long serialVersionUID = -2951742499712676666L;

  /**
   * Keeper singleton.
   */
  @SuppressWarnings( "hiding" )
  public static final IEntityKeeper<IDtoRtdataInfo> KEEPER =
      new AbstractStridableParameterizedKeeper<>( IDtoRtdataInfo.class, null ) {

        @Override
        protected IDtoRtdataInfo doCreate( String aId, IOptionSet aParams ) {
          return new DtoRtdataInfo( aId, aParams );
        }
      };

  /**
   * Constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aParams {@link IOptionSet} - parameters values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   */
  private DtoRtdataInfo( String aId, IOptionSet aParams ) {
    super( aId, aParams );
  }

  /**
   * Static constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aDataType {@link IDataType} - data type
   * @param aIsCurr boolean - the current data flag
   * @param aIsHist boolean - historical data flag
   * @param aIsSync boolean - the synchronous data flag
   * @param aDeltaT long - the time interval in milliseconds
   * @param aParams {@link IOptionSet} - parameters values
   * @return {@link DtoRtdataInfo} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   * @throws TsIllegalArgumentRtException aDeltaT < 1
   */
  public static DtoRtdataInfo create1( String aId, IDataType aDataType, boolean aIsCurr, boolean aIsHist,
      boolean aIsSync, long aDeltaT, IOptionSet aParams ) {
    DtoRtdataInfo ainf = new DtoRtdataInfo( aId, aParams );
    ainf.setProps( aDataType, aIsCurr, aIsHist, aIsSync, aDeltaT );
    return ainf;
  }

  // ------------------------------------------------------------------------------------
  // IDtoClassPropInfoBase
  //

  @Override
  public ESkClassPropKind kind() {
    return ESkClassPropKind.ATTR;
  }

  // ------------------------------------------------------------------------------------
  // IDtoRtdataInfo
  //

  @Override
  public IDataType dataType() {
    return OPDEF_DATA_TYPE.getValue( params() ).asValobj();
  }

  @Override
  public boolean isCurr() {
    return OPDEF_IS_CURR.getValue( params() ).asBool();
  }

  @Override
  public boolean isHist() {
    return OPDEF_IS_HIST.getValue( params() ).asBool();
  }

  @Override
  public boolean isSync() {
    return OPDEF_IS_SYNC.getValue( params() ).asBool();
  }

  @Override
  public long syncDataDeltaT() {
    return OPDEF_SYNC_DATA_DELTA_T.getValue( params() ).asLong();
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Sets properties.
   *
   * @param aDataType {@link IDataType} - data type
   * @param aIsCurr boolean - the current data flag
   * @param aIsHist boolean - historical data flag
   * @param aIsSync boolean - the synchronous data flag
   * @param aDeltaT long - the time interval in milliseconds
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException aDeltaT < 1
   */
  public void setProps( IDataType aDataType, boolean aIsCurr, boolean aIsHist, boolean aIsSync, long aDeltaT ) {
    TsNullArgumentRtException.checkNull( aDataType );
    TsIllegalArgumentRtException.checkTrue( aDeltaT < 1 );
    OPDEF_DATA_TYPE.setValue( params(), avValobj( aDataType ) );
    OPDEF_IS_CURR.setValue( params(), avBool( aIsCurr ) );
    OPDEF_IS_HIST.setValue( params(), avBool( aIsHist ) );
    OPDEF_IS_SYNC.setValue( params(), avBool( aIsSync ) );
    OPDEF_SYNC_DATA_DELTA_T.setValue( params(), avInt( aDeltaT ) );
  }

}
