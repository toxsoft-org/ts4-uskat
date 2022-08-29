package org.toxsoft.uskat.core.impl.dto;

import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * {@link IDtoClobInfo} implementation.
 *
 * @author hazard157
 */
public final class DtoClobInfo
    extends DtoAbstractClassPropInfoBase
    implements IDtoClobInfo {

  private static final long serialVersionUID = -2951742499712676666L;

  /**
   * Keeper singleton.
   */
  @SuppressWarnings( "hiding" )
  public static final IEntityKeeper<IDtoClobInfo> KEEPER =
      new AbstractStridableParameterizedKeeper<>( IDtoClobInfo.class, null ) {

        @Override
        protected IDtoClobInfo doCreate( String aId, IOptionSet aParams ) {
          return new DtoClobInfo( aId, aParams );
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
  private DtoClobInfo( String aId, IOptionSet aParams ) {
    super( aId, aParams );
  }

  /**
   * Static constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aParams {@link IOptionSet} - parameters values
   * @return {@link DtoClobInfo} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   * @throws TsIllegalArgumentRtException aMaxCharsCount < 0
   */
  public static DtoClobInfo create1( String aId, IOptionSet aParams ) {
    DtoClobInfo ainf = new DtoClobInfo( aId, aParams );
    return ainf;
  }

  /**
   * Static constructor.
   *
   * @param aId String - the ID (IDpath)
   * @param aIdsAndValues Object[] - {@link #params()} values
   * @return {@link DtoClobInfo} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException identifier is not an IDpath
   * @throws TsIllegalArgumentRtException aMaxCharsCount < 0
   */
  public static DtoClobInfo create2( String aId, Object... aIdsAndValues ) {
    return create1( aId, OptionSetUtils.createOpSet( aIdsAndValues ) );
  }

  // ------------------------------------------------------------------------------------
  // IDtoClassPropInfoBase
  //

  @Override
  public ESkClassPropKind kind() {
    return ESkClassPropKind.CLOB;
  }

  @SuppressWarnings( "unchecked" )
  @Override
  public DtoClobInfo makeCopy() {
    return new DtoClobInfo( id(), params() );
  }

}
