package org.toxsoft.uskat.core.backend.api;

import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.*;

/**
 * {@link IBackendAddon} base implementation.
 *
 * @author hazard157
 * @param <B> - the class of backend for which this addon is designed for
 */
public class BackendAddonBase<B extends ISkBackend>
    extends Stridable
    implements IBackendAddon {

  private final B owner;

  /**
   * Constructor for subclasses.
   *
   * @param aOwner &lt;B&gt; - the owner backend
   * @param aInfo {@link IStridable} - the addon info
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  protected BackendAddonBase( B aOwner, IStridable aInfo ) {
    super( aInfo );
    owner = TsNullArgumentRtException.checkNull( aOwner );
  }

  // ------------------------------------------------------------------------------------
  // IBackendAddon
  //

  /**
   * Returns the owner backend.
   *
   * @return &lt;B&gt; - the owner backend
   */
  final public B owner() {
    return owner;
  }

}
