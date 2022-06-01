package org.toxsoft.uskat.core.api.objserv;

import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.strid.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.txtmatch.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;

/**
 * Core service: objects manipulation.
 *
 * @author hazard157
 */
public interface ISkObjectService
    extends ISkService {

  /**
   * TODO in object service:
   * <ul>
   * <li>convoy objects;</li>
   * <li>object "owner" service or it is determited by class service?;</li>
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
   * @param <T> - конкретный Java-тип запрошенного объекта
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
   * @param <T> - extpected type of the objects
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
   * Cretes new or updates an existing object. Создает новый или редактирует существующий объект.
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
   * Returns the objects which have specified object riveted with the specified rivet.
   *
   * @param aClassId String - rivet class ID
   * @param aRivetId String - rivet ID
   * @param aRightSkid {@link Skid} - the right object of rivet
   * @return {@link ISkidList} - the list of left objects with specified right object riveted
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException нет такой связи или такого объекта в системе
   */
  ISkidList getRivetRev( String aClassId, String aRivetId, Skid aRightSkid );

  /**
   * Returns the objects which have specified object riveted with any rivet.
   *
   * @param aRightSkid {@link Skid} - the object SKID
   * @return {@link IMap}&lt;{@link Gwid},{@link ISkidList}&gt; - the map "abstract rivet" - "left objects of rivet"
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IMap<Gwid, ISkidList> getAllRivetsRev( Skid aRightSkid );

  /**
   * /** Removes multiple obejcts at once.
   * <p>
   * All forward links of all removed objects are also deleted.
   *
   * @param aSkids {@link ISkidList} - list of SKIDs of objects to remove
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed validation {@link ISkObjectServiceValidator#canRemoveObject(Skid)}
   */
  void removeObjects( ISkidList aSkids );

  // TODO TRANSLATE

  /**
   * Регистрирует создатель объектов по правилу проверки идентификатор класса.
   * <p>
   * Правила проверяются по порядку из регистриации, и поиск останавливается на первом успешном
   * {@link TextMatcher#match(String)}.
   *
   * @param aRule {@link TextMatcher} - провило проверки идентификатора класса
   * @param aCreator {@link ISkObjectCreator} - добавляемый создатель
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsItemAlreadyExistsRtException с этим правилом уже зарегистрирован создатель
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
  // Convinience inline methods

  @SuppressWarnings( "javadoc" )
  default void registerObjectCreator( String aClassId, ISkObjectCreator<?> aCreator ) {
    TextMatcher tm = new TextMatcher( ETextMatchMode.EXACT, StridUtils.checkValidIdPath( aClassId ) );
    registerObjectCreator( tm, aCreator );
  }

}
