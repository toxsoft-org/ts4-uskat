package org.toxsoft.uskat.sysext.refbooks;

import ru.uskat.core.common.skobject.ISkObject;

/**
 * An refbook item.
 *
 * @author goga
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
