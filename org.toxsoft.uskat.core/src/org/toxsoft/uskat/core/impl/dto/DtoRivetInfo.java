package org.toxsoft.uskat.core.impl.dto;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.uskat.core.impl.dto.IDtoHardConstants.*;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * {@link IDtoRivetInfo} implementation.
 * <p>
 * As {@link StridableParameterized} extension this class stores all fields in {@link #params()}:
 * <ul>
 * <li>{@link #rightClassId()} - as option {@link IDtoHardConstants#OPDEF_RIGHT_CLASS_ID}.</li>
 * <li>{@link #count()} - as option {@link IDtoHardConstants#OPDEF_COUNT}.</li>
 * </ul>
 *
 * @author hazard157
 */
public final class DtoRivetInfo
    extends DtoAbstractClassPropInfoBase
    implements IDtoRivetInfo {

  private static final long serialVersionUID = -2951742499712676666L;

  /**
   * Keeper singleton.
   */
  @SuppressWarnings( "hiding" )
  public static final IEntityKeeper<IDtoRivetInfo> KEEPER =
      new AbstractStridableParameterizedKeeper<>( IDtoRivetInfo.class, null ) {

        @Override
        protected IDtoRivetInfo doCreate( String aId, IOptionSet aParams ) {
          return new DtoRivetInfo( aId, aParams );
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
  private DtoRivetInfo( String aId, IOptionSet aParams ) {
    super( aId, aParams );
  }

  /**
   * Static constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aRightClassId String - riveted objects class ID
   * @param aCount int - quantity of objects, always >= 1
   * @param aParams {@link IOptionSet} - parameters values
   * @return {@link DtoRivetInfo} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   * @throws TsIllegalArgumentRtException aCount < 1
   */
  public static DtoRivetInfo create1( String aId, String aRightClassId, int aCount, IOptionSet aParams ) {
    DtoRivetInfo ainf = new DtoRivetInfo( aId, aParams );
    ainf.setProps( aRightClassId, aCount );
    return ainf;
  }

  // ------------------------------------------------------------------------------------
  // IDtoClassPropInfoBase
  //

  @Override
  public ESkClassPropKind kind() {
    return ESkClassPropKind.RIVET;
  }

  // ------------------------------------------------------------------------------------
  // IDtoRivetInfo
  //

  @Override
  public String rightClassId() {
    return OPDEF_RIGHT_CLASS_ID.getValue( params() ).asString();
  }

  @Override
  public int count() {
    return OPDEF_RIGHT_CLASS_ID.getValue( params() ).asInt();
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Sets properties.
   *
   * @param aRightClassId String - riveted objects class ID
   * @param aCount int - quantity of objects, always >= 1
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException aCount < 1
   */
  public void setProps( String aRightClassId, int aCount ) {
    StridUtils.checkValidIdPath( aRightClassId );
    TsIllegalArgumentRtException.checkTrue( aCount < 1 );
    OPDEF_RIGHT_CLASS_ID.setValue( params(), avStr( aRightClassId ) );
    OPDEF_COUNT.setValue( params(), avInt( aCount ) );
  }

}
