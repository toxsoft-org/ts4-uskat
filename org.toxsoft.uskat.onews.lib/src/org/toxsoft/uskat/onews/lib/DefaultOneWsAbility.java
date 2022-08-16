package org.toxsoft.uskat.onews.lib;

import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.av.opset.*;
import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * {@link IOneWsAbility} default implementation.
 * <p>
 * This is an immutable class.
 *
 * @author hazard157
 */
public final class DefaultOneWsAbility
    extends StridableParameterized
    implements IOneWsAbility {

  /**
   * Constructor.
   *
   * @param aAbilityId String - ability ID (IDpath)
   * @param aAbilityTypeId String - ability kind ID (IDpath)
   * @param aParams {@link IOptionSet} - {@link IOneWsAbility#params()} values
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   */
  public DefaultOneWsAbility( String aAbilityId, String aAbilityTypeId, IOptionSet aParams ) {
    super( aAbilityId, aParams );
    StridUtils.checkValidIdPath( aAbilityTypeId );
    params().setStr( IAvMetaConstants.TSID_ID, aAbilityId );
    params().setStr( IOneWsConstants.OP_OWS_ABILITY_KIND_ID, aAbilityTypeId );
  }

  /**
   * Static constructor.
   *
   * @param aAbilityId String - ability ID (IDpath)
   * @param aAbilityTypeId String - ability kind ID (IDpath)
   * @param aNameAndValues Object[] - {@link #params()} values as in {@link OptionSetUtils#createOpSet(Object...)}
   * @return {@link DefaultOneWsAbility} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException any ID is not an IDpath
   * @throws TsIllegalArgumentRtException number of elements in array is uneven
   * @throws ClassCastException argument types convention is violated
   */
  public static DefaultOneWsAbility create( String aAbilityId, String aAbilityTypeId, Object... aNameAndValues ) {
    IOptionSet params = OptionSetUtils.createOpSet( aNameAndValues );
    return new DefaultOneWsAbility( aAbilityId, aAbilityTypeId, params );
  }

  // ------------------------------------------------------------------------------------
  // IOneWsAbility
  //

  @Override
  public String kindId() {
    return params().getStr( IOneWsConstants.OP_OWS_ABILITY_KIND_ID );
  }

}
