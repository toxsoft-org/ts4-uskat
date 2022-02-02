package org.toxsoft.uskat.core.api.linkserv;

import org.toxsoft.core.tslib.bricks.validator.ValidationResult;

/**
 * {@link ISkLinkService} service validator.
 *
 * @author hazard157
 */
public interface ISkLinkServiceValidator {

  /**
   * Checks if new can be set
   *
   * @param aLink {@link IDtoLinkFwd} - link to be set
   * @return {@link ValidationResult} - validation result
   */
  ValidationResult canSetLink( IDtoLinkFwd aLink );

}
