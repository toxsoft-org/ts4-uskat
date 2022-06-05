package org.toxsoft.uskat.core.api.sysdescr;

import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * Information about class.
 *
 * @author hazard157
 */
public interface ISkClassInfo
    extends IStridableParameterized {

  /**
   * Returns parent (superclass) information.
   *
   * @return {@link ISkClassInfo} - parent class info or <code>null</code> for root class
   */
  ISkClassInfo parent();

  // ------------------------------------------------------------------------------------
  // Properties

  /**
   * Returns complete info about class attributes.
   *
   * @return {@link ISkClassProps}&lt;{@link IDtoAttrInfo}&gt; - information about attributes
   */
  ISkClassProps<IDtoAttrInfo> attrs();

  /**
   * Returns complete info about class rivets.
   *
   * @return {@link ISkClassProps}&lt;{@link IDtoRivetInfo}&gt; - information about rivets
   */
  ISkClassProps<IDtoRivetInfo> rivets();

  /**
   * Returns complete info about class CLOBs.
   *
   * @return {@link ISkClassProps}&lt;{@link IDtoClobInfo}&gt; - information about CLOBs
   */
  ISkClassProps<IDtoClobInfo> clobs();

  /**
   * Returns complete info about class links.
   *
   * @return {@link ISkClassProps}&lt;{@link IDtoLinkInfo}&gt; - information about links
   */
  ISkClassProps<IDtoLinkInfo> links();

  /**
   * Returns complete info about class RTdata.
   *
   * @return {@link ISkClassProps}&lt;{@link IDtoRtdataInfo}&gt; - information about RTdata
   */
  ISkClassProps<IDtoRtdataInfo> rtdata();

  /**
   * Returns complete info about class events.
   *
   * @return {@link ISkClassProps}&lt;{@link IDtoEventInfo}&gt; - information about events
   */
  ISkClassProps<IDtoEventInfo> events();

  /**
   * Returns complete info about class commands.
   *
   * @return {@link ISkClassProps}&lt;{@link IDtoCmdInfo}&gt; - information about commands
   */
  ISkClassProps<IDtoCmdInfo> cmds();

  // ------------------------------------------------------------------------------------
  // hierarchy info
  //

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
   * Determines if this class is one of the specified classes or its subclass.
   *
   * @param aClassIdsList {@link IStringList} - list of class IDs
   * @return boolean - <code>true</code> if this class is assignable to one the class in list
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  boolean isOfClass( IStringList aClassIdsList );

  // ------------------------------------------------------------------------------------
  // Convinience inline methods

  /**
   * Returns the superclass ID.
   * <p>
   * For root class (class with ID {@link IGwHardConstants#GW_ROOT_CLASS_ID} returns an empty string.
   *
   * @return String - superclass ID (an IDpath) or an empty string for root class
   */
  default String parentId() {
    return parent() != null ? parent().id() : TsLibUtils.EMPTY_STRING;
  }

  /**
   * Returns information about specified kinf of class properties.
   *
   * @param <T> - concrete type of property info
   * @param aKind {@link ESkClassPropKind} - the requested kind of properties
   * @return {@link ISkClassProps}&lt;T&gt; - requested properties
   */
  <T extends IDtoClassPropInfoBase> ISkClassProps<T> props( ESkClassPropKind aKind );

}
