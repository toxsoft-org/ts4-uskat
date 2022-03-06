package org.toxsoft.uskat.core.backend.api;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.backend.*;

/**
 * {@link IBackendAddon} base implementation.
 *
 * @author hazard157
 * @param <B> - the class of backend for which this addon is designed for
 */
public class BackendAddon<B extends ISkBackend>
    implements IBackendAddon {

  private final B owner;

  /**
   * Constructor for subclasses.
   *
   * @param aOwner &lt;B&gt; - the owner backend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  protected BackendAddon( B aOwner ) {
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
  @Override
  public B owner() {
    return owner;
  }

}
