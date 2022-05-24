package org.toxsoft.uskat.core.api.linkserv;

import org.toxsoft.core.tslib.bricks.validator.*;

/**
 * {@link ISkLinkService} service validator.
 *
 * @author hazard157
 */
public interface ISkLinkServiceValidator {

  /**
   * Checks if new link can be set.
   *
   * @param aOldLink {@link IDtoLinkFwd} - old link or <code>null</code> if none exists
   * @param aNewLink {@link IDtoLinkFwd} - link to be set
   * @return {@link ValidationResult} - validation result
   */
  ValidationResult canSetLink( IDtoLinkFwd aOldLink, IDtoLinkFwd aNewLink );

}
