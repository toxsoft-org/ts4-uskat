package org.toxsoft.uskat.core.api.sysdescr;

import org.toxsoft.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.tslib.coll.primtypes.IStringList;
import org.toxsoft.tslib.gw.IGwHardConstants;
import org.toxsoft.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Information about one class hierechy.
 *
 * @author hazard157
 */
public interface ISkClassHierarchyInfo {

  /**
   * Returns parent (superclass) information.
   *
   * @return {@link ISkClassInfo} - parent class info or <code>null</code> for root class
   */
  ISkClassInfo parent();

  /**
   * Returns subclasses of this class.
   * <p>
   * Classes may be list in any order. But it is guaranteed that if <code>aIncludeSelf</code> argument is
   * <code>true</code> then this class will be the first one in the list. Also it is guaranteed that any subclass will
   * be listed after any of it's superclass.
   *
   * @param aOnlyChilds boolean - determines if only direct subclasses will be listed, not all their descendants
   * @param aIncludeSelf boolean - determines if this class needs to be included in the result
   * @return {@link IStridablesList}&lt;{@link ISkClassInfo}&gt; - list of subclasses classes
   */
  IStridablesList<ISkClassInfo> listSubclasses( boolean aOnlyChilds, boolean aIncludeSelf );

  /**
   * Returns information about superclass of this class up to the root class.
   * <p>
   * Returned list is ordered. First in list will be the root class (the class with ID
   * {@link IGwHardConstants#GW_ROOT_CLASS_ID}), the last will be this class or parent of this class depending on
   * <code>aIncludeSelf</code> argument value. If this class is the root one and the <code>aIncludeSelf</code> argument
   * is <code>false</code> then returned list will be empty.
   *
   * @param aIncludeSelf boolean - determines if this class needs to be included in the result
   * @return {@link IStridablesList}&lt;{@link ISkClassInfo}&gt; - ordered list of classes
   */
  IStridablesList<ISkClassInfo> listSuperclasses( boolean aIncludeSelf );

  /**
   * Determines if this class is superclass of specified class.
   * <p>
   * The only difference between {@link #isSuperclassOf(String)} and {@link #isAssignableFrom(String)} is that if
   * argument is this class ID the first returns <code>false</code> and the second <code>true</code>.
   *
   * @param aSubclassId String - the probable subclass ID
   * @return boolean - <code>true</code> if specified class is subclass of this class
   */
  boolean isSuperclassOf( String aSubclassId );

  /**
   * Determines if this class is superclass of specified class.
   * <p>
   * The only difference between {@link #isSuperclassOf(String)} and {@link #isAssignableFrom(String)} is that if
   * argument is this class ID the first returns <code>false</code> and the second <code>true</code>.
   *
   * @param aSubclassId String - the probable subclass ID
   * @return boolean - <code>true</code> if specified class is this class or subclass of this class
   */
  boolean isAssignableFrom( String aSubclassId );

  /**
   * Determines if this class is subclass of specified class.
   * <p>
   * The only difference between {@link #isSubclassOf(String)} and {@link #isAssignableTo(String)} is that if argument
   * is this class ID the first returns <code>false</code> and the second <code>true</code>.
   *
   * @param aSuperclassId String - the probable superclass ID
   * @return boolean - <code>true</code> if specified class is superclass of this class
   */
  boolean isSubclassOf( String aSuperclassId );

  /**
   * Determines if this class is subclass of specified class.
   * <p>
   * The only difference between {@link #isSubclassOf(String)} and {@link #isAssignableTo(String)} is that if argument
   * is this class ID the first returns <code>false</code> and the second <code>true</code>.
   *
   * @param aSuperclassId String - the probable superclass ID
   * @return boolean - <code>true</code> if specified class is this class or superclass of this class
   */
  boolean isAssignableTo( String aSuperclassId );

  /**
   * Determines if this class if one of the specified classes or its subclass.
   *
   * @param aClassIdsList {@link IStringList} - list of class IDs
   * @return boolean - <code>true</code> if this class is assignable to one the class in list
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean isOfClass( IStringList aClassIdsList );

}
