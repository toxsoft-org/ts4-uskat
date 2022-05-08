package org.toxsoft.uskat.core.api.rtdserv;

import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;

/**
 * Core service: real-time data current and historic values.
 *
 * @author hazard157
 */
public interface ISkRtdataService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".RtData"; //$NON-NLS-1$

}
