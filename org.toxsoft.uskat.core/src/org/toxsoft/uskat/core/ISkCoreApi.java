package org.toxsoft.uskat.core;

import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * USkat core API.
 *
 * @author hazard157
 */
public interface ISkCoreApi {

  /**
   * Returns the system description service.
   *
   * @return {@link ISkSysdescr} - the system description service
   */
  ISkSysdescr sysdescr();

}
