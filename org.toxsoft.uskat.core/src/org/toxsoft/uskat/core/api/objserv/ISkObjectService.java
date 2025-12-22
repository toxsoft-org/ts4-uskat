package org.toxsoft.uskat.core.api.objserv;

import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.txtmatch.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Core service: objects manipulation.
 *
 * @author hazard157
 */
public interface ISkObjectService
    extends ISkService {

  /**
   * FIXME in object service:<br>
   * add listeners by the class IDs, otherwise and LIST operation causes everything and everywhere to rebuild...
   */

  /**
   * TODO in object service:
   * <ul>
   * <li>convoy objects;</li>
   * </ul>
   */

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".Objects"; //$NON-NLS-1$

  /**
   * Finds an object by it's SKID.
   *
   * @param <T> - expected Java-type of the object
   * @param aSkid {@link Skid} - SKID of the object
   * @return &lt;T&gt; - the object or <code>null</code> if there is no such object
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  <T extends ISkObject> T find( Skid aSkid );

  /**
   * Returns an object by it's SKID.
   *
   * @param <T> - expected Java-type of the object
   * @param aSkid {@link Skid} - SKID of the object
   * @return &lt;T&gt; - the object
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such object
   */
  <T extends ISkObject> T get( Skid aSkid );

  /**
   * Returns SKIDs of all objects of the specified class and optionally of all subclasses.
   *
   * @param aClassId String - the objects class ID
   * @param aIncludeSubclasses boolean - <code>true</code> to include objects of all subclasses
   * @return {@link ISkidList} - list of found objects SKIDs
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such class
   */
  ISkidList listSkids( String aClassId, boolean aIncludeSubclasses );

  /**
   * Lists all objects of the specified class and optionally of all subclasses.
   * <p>
   * Please note that subclassing in USkat green world does not means that Sk-object Java-implementations have the same
   * hierarchy of classes. In other words if <code>aClassId</code> is implemented by Java-class <code>SomeClass</code>,
   * its green world world subclass <code>aSubClassId</code> may be implemented by Java-class <code>OtherClass</code>.
   * And if <code>SomeClass</code> is not parent of <code>OtherClass</code>, returned list will contain instances of
   * different Java-classes.
   *
   * @param <T> - expected Java-type of the objects
   * @param aClassId String - the objects class ID
   * @param aIncludeSubclasses boolean - <code>true</code> to include objects of all subclasses
   * @return {@link IList}&lt;T&gt; - list of the objects
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such class
   */
  <T extends ISkObject> IList<T> listObjs( String aClassId, boolean aIncludeSubclasses );

  /**
   * Returns objects by SKIDs.
   * <p>
   * Service does not specifies what happens if argument contains duplicate SKIDs. Implementation may ignore duplicate
   * SKIDs as well as mey return duplicate {@link ISkObject} instances. Order of objects in returned list is not
   * specified.
   *
   * @param aSkids {@link ISkidList} - list of SKIDs
   * @return {@link IList}&lt;{@link ISkObject}&gt; - objects list
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException argument contains at least one non-existing obejct SKID
   */
  IList<ISkObject> getObjs( ISkidList aSkids );

  /**
   * Creates new or updates an existing object.
   *
   * @param <T> - expected type of the object
   * @param aDtoObject {@link IDtoObject} - the object data
   * @return &lt;T&gt; - created/updated object
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed validation
   *           {@link ISkObjectServiceValidator#canCreateObject(IDtoObject)} or
   *           {@link ISkObjectServiceValidator#canEditObject(IDtoObject, ISkObject)}
   */
  <T extends ISkObject> T defineObject( IDtoObject aDtoObject );

  /**
   * Creates new objects or updates or deletes existing objects in a single transaction.
   *
   * @param aRemoveSkids {@link ISkidList} - list of SKIDs of objects to remove
   * @param aDtoObjects {@link IList}&lt;{@link IDtoObject}&gt; - list of the object data
   * @return {@link IList}&lt;{@link ISkObject}&gt; list of the created/update objects
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed validation
   *           {@link ISkObjectServiceValidator#canCreateObject(IDtoObject)} or
   *           {@link ISkObjectServiceValidator#canEditObject(IDtoObject, ISkObject)}
   */
  IList<ISkObject> defineObjects( ISkidList aRemoveSkids, IList<IDtoObject> aDtoObjects );

  /**
   * Removes the specified object.
   * <p>
   * All forward links are also deleted.
   *
   * @param aSkid {@link Skid} - SKID of the object to be deleted
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed validation {@link ISkObjectServiceValidator#canRemoveObject(Skid)}
   */
  void removeObject( Skid aSkid );

  /**
   * Removes multiple objects at once.
   * <p>
   * All forward links of all removed objects are also deleted.
   *
   * @param aSkids {@link ISkidList} - list of SKIDs of objects to remove
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed validation {@link ISkObjectServiceValidator#canRemoveObject(Skid)}
   */
  void removeObjects( ISkidList aSkids );

  /**
   * Register the creator of Java class implementation of {@link ISkObject}.
   * <p>
   * Any object with class ID accepted by {@link TextMatcher#accept(String) aRule.accept(String)} will be created by
   * {@link ISkObjectCreator#createObject(Skid) aCreator.createObject(Skid)} For unregistered class IDs actual Java
   * implementation will be the class {@link SkObject}.
   * <p>
   * The rules are checked in the order of registration and the first rule accepting class ID creates object by by
   * corresponding creator.
   *
   * @param aRule {@link TextMatcher} - the class ID checking rule
   * @param aCreator {@link ISkObjectCreator} - the creator of objects with class IDs accepted by the <code>aRule</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemAlreadyExistsRtException this rule was already registered
   */
  void registerObjectCreator( TextMatcher aRule, ISkObjectCreator<?> aCreator );

  // ------------------------------------------------------------------------------------
  // Service support

  /**
   * Returns the service validator.
   *
   * @return {@link ITsValidationSupport}&lt;{@link ISkObjectServiceValidator}&gt; - the service validator
   */
  ITsValidationSupport<ISkObjectServiceValidator> svs();

  /**
   * Returns the service eventer.
   *
   * @return {@link ITsEventer}&lt;{@link ISkObjectServiceListener}&gt; - the service eventer
   */
  ITsEventer<ISkObjectServiceListener> eventer();

  // ------------------------------------------------------------------------------------
  // Convenience inline methods

  @SuppressWarnings( "javadoc" )
  default void registerObjectCreator( String aClassId, ISkObjectCreator<?> aCreator ) {
    TextMatcher tm = new TextMatcher( ETextMatchMode.EXACT, StridUtils.checkValidIdPath( aClassId ) );
    registerObjectCreator( tm, aCreator );
  }

}
