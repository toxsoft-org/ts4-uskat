package org.toxsoft.uskat.core.api;

import org.toxsoft.uskat.core.*;

/**
 * USkat service base interface.
 *
 * @author hazard157
 */
public interface ISkService {

  /**
   * Returns the service identifier.
   *
   * @return String - uniqune service identifier (IDpath)
   */
  String serviceId();

  /**
   * Returns the core API of which this interface is a part.
   *
   * @return {@link ISkCoreApi} - the service owner core API
   */
  ISkCoreApi coreApi();

}
