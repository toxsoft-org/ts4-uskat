package org.toxsoft.uskat.core.api.evserv;

import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;

/**
 * Core service: object-generated events management.
 *
 * @author hazard157
 */
public interface ISkEventService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".Events"; //$NON-NLS-1$

}
