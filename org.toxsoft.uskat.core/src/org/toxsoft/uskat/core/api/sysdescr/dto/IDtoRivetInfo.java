package org.toxsoft.uskat.core.api.sysdescr.dto;

/**
 * Information about rivet property of class.
 *
 * @author hazard157
 */
public interface IDtoRivetInfo
    extends IDtoClassPropInfoBase {

  /**
   * Returns the class allowed for the riveted objects.
   * <p>
   * As usual right objects may be of specified class or any of it's subclass.
   *
   * @return String - riveted objects class ID
   */
  String rightClassId();

  /**
   * Returns number of riveted objects.
   *
   * @return int - quantity of objects, always >= 1
   */
  int count();

}
