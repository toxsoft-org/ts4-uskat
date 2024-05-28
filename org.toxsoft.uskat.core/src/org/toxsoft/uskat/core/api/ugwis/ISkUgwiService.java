package org.toxsoft.uskat.core.api.ugwis;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.ugwi.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;

/**
 * Service to work with GWIDs.
 *
 * @author hazard157
 */
public interface ISkUgwiService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".Ugwis"; //$NON-NLS-1$

  /**
   * Returns all registered UGWI kinds.
   *
   * @return {@link IStridablesList}&lt;{@link IUgwiKind}&gt; - list of registered kinds
   */
  IStridablesList<IUgwiKind> listKinds();

  /**
   * Determines if content addressed by the UGWI exists in this UStake space.
   * <p>
   * For unknown UGWI kinds returns <code>false</code> even if content exists.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return boolean <code>true</code> if UGWI kind is known and content exists
   */
  boolean isContent( Ugwi aUgwi );

  /**
   * Finds the content addresses by the UGWI.
   * <p>
   * For unknown UGWI kinds returns <code>null</code> even if the content exists.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return {@link Object} - the content or <code>null</code> if not found
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  Object findContent( Ugwi aUgwi );

  /**
   * Finds content as the specified class.
   *
   * @param <T> - expected type of the content
   * @param aUgwi {@link Gwid} - the UGWI
   * @param aContentClass {@link Class}&lt;T&gt; - expected class of the cntent
   * @return {@link Object} - the content or <code>null</code> if not found
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws ClassCastException content was found but not of expected type
   */
  <T> T findContentAs( Ugwi aUgwi, Class<T> aContentClass );

  /**
   * Returns the specified helper for the specified kind if any was registered.
   * <p>
   * For unknown UGWI kinds returns {@link NullPointerException};
   *
   * @param <H> - expected type of the helper
   * @param aUgwiKindId String - the kind of UGWI
   * @param aHelperClass {@link Class}&lt;T&gt; - helper class used at registration time
   * @return &lt;H&gt; - found helper or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws ClassCastException found helper is not of requested type
   */
  <H> H findHelper( String aUgwiKindId, Class<H> aHelperClass );

  /**
   * Adds kind to the known (registered) kinds list {@link #listKinds()}.
   * <p>
   * Notes:
   * <ul>
   * <li>registered kinds are <b>not</b> stored permanently. UGWI kinds must be registered every time when the
   * connection opens. In USkat GUI library there is an ability to automatically register all known by
   * {@link SkUgwiUtils#listUgwiKindCreators()} kinds.;</li>
   * <li>some UGWI kinds will not be registered, for example if corresponding <code>ISkXxxService</code> is not present
   * in {@link ISkCoreApi#services()};</li>
   * <li>attempts to register kind with an existing kind ID are ignored.</li>
   * </ul>
   *
   * @param aUgwiKind {@link AbstractUgwiKind} - the kind to register
   */
  void registerKind( AbstractUgwiKind<?> aUgwiKind );

}
