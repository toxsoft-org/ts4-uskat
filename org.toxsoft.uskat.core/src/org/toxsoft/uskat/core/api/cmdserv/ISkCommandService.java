package org.toxsoft.uskat.core.api.cmdserv;

import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;

/**
 * Core service: command sending and processing support.
 *
 * @author hazard157
 */
public interface ISkCommandService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".Commands"; //$NON-NLS-1$

}
