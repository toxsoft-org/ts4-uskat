package org.toxsoft.uskat.core.api.sysdescr;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

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
   * Lists all non-system properties of this class.
   * <p>
   * Currently (as of 2023-12-29) system properties are known only for attributes. The attribute with
   * {@link ISkHardConstants#isSkSysAttr(IDtoAttrInfo)} = <code>true</code> are considered system attributes.
   *
   * @return IStridablesList&lt;T&gt; - non-system properties of this class
   */
  IStridablesList<T> listNonSys();

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
   * Returns the classes, which declares properties of specified ID.
   * <p>
   * Returns an empty list if property is not declared at all in the hierarchy of this class. also an empty list is
   * returned if such property exists in this class and obviously can not be declared by subclasses.
   *
   * @param aPropId String - the property ID
   * @return {@link IStridablesList}&lt;{@link ISkClassInfo}&gt; - list of declarer subclasses
   */
  IStridablesList<ISkClassInfo> findSubDeclarers( String aPropId );

  /**
   * Creates and returns copy of the {@link #list()} or {@link #listSelf()}.
   *
   * @param aOnlySelf boolean - <code>true</code> for {@link #listSelf()} copy, <code>false</code> for {@link #list()}
   * @return IStridablesList&lt;T&gt; - copy of the specified list of properties
   */
  IStridablesList<T> makeCopy( boolean aOnlySelf );

}
