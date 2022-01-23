package org.toxsoft.uskat.core.api.sysdescr;

import org.toxsoft.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassPropInfoBase;

/**
 * Complete information about the class properties of one kind..
 *
 * @author hazard157
 * @param <T> - the property type
 */
public interface ISkClassProps<T extends IDtoClassPropInfoBase> {

  /**
   * Returns the kind of property.
   *
   * @return {@link ESkClassPropKind} - the kind of property
   */
  ESkClassPropKind kind();

  /**
   * Lists all properties of this class.
   *
   * @return IStridablesList&lt;T&gt; - properties of this class
   */
  IStridablesList<T> list();

  /**
   * Lists the properties declared by this class only, without parent properties.
   *
   * @return IStridablesList&lt;T&gt; - self properties of this class
   */
  IStridablesList<T> listSelf();

  /**
   * Finds the class declaring the property starting from root down to this class inclusive.
   *
   * @param aPropId String - the property ID
   * @return {@link ISkClassInfo} - the declarer or <code>null</code> if no such property is in this class
   */
  ISkClassInfo findSuperDeclarer( String aPropId );

  /**
   * Returns the classes, whch declares properties of specified ID.
   *
   * @param aPropId String - the property ID
   * @return {@link IStridablesList}&lt;{@link ISkClassInfo}&gt; - list of declarer subclasses
   */
  IStridablesList<ISkClassInfo> findSubDeclarers( String aPropId );

}
