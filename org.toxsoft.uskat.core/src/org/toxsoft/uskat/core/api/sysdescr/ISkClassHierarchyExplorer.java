package org.toxsoft.uskat.core.api.sysdescr;

import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Classes hierarchy information provider.
 * <p>
 * The idea of interface is to answer on logical questions if there is an asked releation between classes. Only positive
 * answer <code>true</code> is meaningful, negative answers may caused both by absenсу of the releation or the absence
 * of the class with the specified ID.
 *
 * @author hazard157
 */
public interface ISkClassHierarchyExplorer {

  /**
   * Determines if <code>aClassId</code> is superclass of specified class.
   * <p>
   * The only difference between {@link #isSuperclassOf(String, String)} and {@link #isAssignableFrom(String, String)}
   * is that if arguments refer to the same class then the first returns <code>false</code> and the second
   * <code>true</code>.
   *
   * @param aClassId String - the probable superclass ID
   * @param aSubclassId String - the probable subclass ID
   * @return boolean - <code>true</code> if specified class is subclass of this class
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean isSuperclassOf( String aClassId, String aSubclassId );

  /**
   * Determines if <code>aClassId</code> is superclass of specified class.
   * <p>
   * The only difference between {@link #isSuperclassOf(String, String)} and {@link #isAssignableFrom(String, String)}
   * is that if arguments refer to the same class then the first returns <code>false</code> and the second
   * <code>true</code>.
   *
   * @param aClassId String - the probable superclass ID
   * @param aSubclassId String - the probable subclass ID
   * @return boolean - <code>true</code> if specified class is this class or subclass of this class
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean isAssignableFrom( String aClassId, String aSubclassId );

  /**
   * Determines if <code>aClassId</code> is subclass of specified class.
   * <p>
   * The only difference between {@link #isSubclassOf(String, String)} and {@link #isAssignableFrom(String, String)} is
   * that if arguments refer to the same class then the first returns <code>false</code> and the second
   * <code>true</code>.
   *
   * @param aClassId String - the probable subclass ID
   * @param aSuperclassId String - the probable superclass ID
   * @return boolean - <code>true</code> if specified class is superclass of this class
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean isSubclassOf( String aClassId, String aSuperclassId );

  /**
   * Determines if <code>aClassId</code> is subclass of specified class.
   * <p>
   * The only difference between {@link #isSubclassOf(String, String)} and {@link #isAssignableFrom(String, String)} is
   * that if arguments refer to the same class then the first returns <code>false</code> and the second
   * <code>true</code>.
   *
   * @param aClassId String - the probable subclass ID
   * @param aSuperclassId String - the probable superclass ID
   * @return boolean - <code>true</code> if specified class is this class or superclass of this class
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean isAssignableTo( String aClassId, String aSuperclassId );

  /**
   * Determines if <code>aClassId</code> is one of the specified classes or its subclass.
   *
   * @param aClassId String - the probable subclass ID
   * @param aClassIdsList {@link IStringList} - list of class IDs
   * @return boolean - <code>true</code> if this class is assignable to one the class in list
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean isOfClass( String aClassId, IStringList aClassIdsList );

}
