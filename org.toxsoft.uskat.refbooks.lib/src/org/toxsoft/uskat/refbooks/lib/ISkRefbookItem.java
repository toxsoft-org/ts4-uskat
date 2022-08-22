package org.toxsoft.uskat.refbooks.lib;

import org.toxsoft.uskat.core.api.objserv.*;

/**
 * A refbook item.
 *
 * @author hazard157
 */
public interface ISkRefbookItem
    extends ISkObject {

  /**
   * Returns the owner refbook.
   *
   * @return {@link ISkRefbook} - the owner refbook
   */
  ISkRefbook refbook();

}
