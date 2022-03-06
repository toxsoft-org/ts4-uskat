package org.toxsoft.uskat.core.backend.api;

import org.toxsoft.uskat.core.backend.*;

/**
 * Backend addon base interface.
 *
 * @author hazard157
 */
public interface IBackendAddon {

  /**
   * Returns the owner backend.
   *
   * @return &lt;B&gt; - the owner backend
   */
  ISkBackend owner();

}
