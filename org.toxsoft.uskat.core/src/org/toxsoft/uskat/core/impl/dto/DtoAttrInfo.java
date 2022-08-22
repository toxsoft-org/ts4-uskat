package org.toxsoft.uskat.core.impl.dto;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.impl.dto.IDtoHardConstants.*;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * {@link IDtoAttrInfo} implementation.
 * <p>
 * As {@link StridableParameterized} extension this class stores all fields in {@link #params()}:
 * <ul>
 * <li>{@link #dataType()} - as option {@link IDtoHardConstants#OPDEF_DATA_TYPE}.</li>
 * </ul>
 *
 * @author hazard157
 */
public final class DtoAttrInfo
    extends DtoAbstractClassPropInfoBase
    implements IDtoAttrInfo {

  private static final long serialVersionUID = -2951742499712676666L;

  /**
   * Keeper singleton.
   */
  @SuppressWarnings( "hiding" )
  public static final IEntityKeeper<IDtoAttrInfo> KEEPER =
      new AbstractStridableParameterizedKeeper<>( IDtoAttrInfo.class, null ) {

        @Override
        protected IDtoAttrInfo doCreate( String aId, IOptionSet aParams ) {
          return new DtoAttrInfo( aId, aParams );
        }
      };

  private transient PriorityDataType dataType = null;

  /**
   * Constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aParams {@link IOptionSet} - {@link #params()} values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   */
  private DtoAttrInfo( String aId, IOptionSet aParams ) {
    super( aId, aParams );
  }

  /**
   * Static constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aDataType {@link IDataType} - data type
   * @param aParams {@link IOptionSet} - {@link #params()} values
   * @return {@link DtoAttrInfo} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   */
  public static DtoAttrInfo create1( String aId, IDataType aDataType, IOptionSet aParams ) {
    DtoAttrInfo ainf = new DtoAttrInfo( aId, aParams );
    ainf.setProps( aDataType );
    return ainf;
  }

  /**
   * Static constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aDataType {@link IDataType} - data type
   * @param aIdsAndValues Object[] - {@link #params()} values
   * @return {@link DtoAttrInfo} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   */
  public static DtoAttrInfo create2( String aId, IDataType aDataType, Object... aIdsAndValues ) {
    return create1( aId, aDataType, OptionSetUtils.createOpSet( aIdsAndValues ) );
  }

  // ------------------------------------------------------------------------------------
  // IDtoClassPropInfoBase
  //

  @Override
  public ESkClassPropKind kind() {
    return ESkClassPropKind.ATTR;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public DtoAttrInfo makeCopy() {
    return new DtoAttrInfo( id(), params() );
  }

  // ------------------------------------------------------------------------------------
  // IDtoAttrInfo
  //

  @Override
  public IDataType dataType() {
    if( dataType == null ) {
      IDataType dtOp = OPDEF_DATA_TYPE.getValue( params() ).asValobj();
      dataType = new PriorityDataType( dtOp.atomicType(), params(), dtOp.params() );
    }
    return dataType;
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Sets properties.
   *
   * @param aDataType {@link IDataType} - data type
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public void setProps( IDataType aDataType ) {
    TsNullArgumentRtException.checkNull( aDataType );
    OPDEF_DATA_TYPE.setValue( params(), avValobj( aDataType ) );
    dataType = null;
  }

}
