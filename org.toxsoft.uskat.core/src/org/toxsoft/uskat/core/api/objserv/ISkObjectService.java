package org.toxsoft.uskat.core.api.objserv;

import org.toxsoft.tslib.bricks.events.ITsEventer;
import org.toxsoft.tslib.bricks.strid.impl.StridUtils;
import org.toxsoft.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.tslib.bricks.validator.impl.TsValidationFailedRtException;
import org.toxsoft.tslib.gw.skid.ISkidList;
import org.toxsoft.tslib.gw.skid.Skid;
import org.toxsoft.tslib.utils.errors.*;
import org.toxsoft.tslib.utils.txtmatch.ETextMatchMode;
import org.toxsoft.tslib.utils.txtmatch.TextMatcher;
import org.toxsoft.uskat.core.ISkHardConstants;
import org.toxsoft.uskat.core.api.ISkService;

/**
 * Objects management service.
 *
 * @author goga
 */
public interface ISkObjectService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".ObjectService"; //$NON-NLS-1$

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
   * @param aClassId String - the objects class ID
   * @param aIncludeSubclasses boolean - <code>true</code> to include objects of all subclasses
   * @return {@link ISkObjList} - list of the objects
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such class
   */
  ISkObjList listObjs( String aClassId, boolean aIncludeSubclasses );

  /**
   * Returns objects by SKIDs.
   * <p>
   * Service does not specifies what happens if argument contains duplicate SKIDs. Implementation may ignore duplicate
   * SKIDs as well as mey return duplicate {@link ISkObject} instances. Order of objects in returned list is not
   * specified.
   *
   * @param aSkids {@link ISkidList} - list of SKIDs
   * @return {@link ISkObjList} - objects list
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException argument contains at least one non-existing obejct SKID
   */
  ISkObjList getObjs( ISkidList aSkids );

  // TODO TRANSLATE

  /**
   * Создает новый или редактирует существующий объект.
   *
   * @param <T> - конкретный тип объекта
   * @param aDtoObject {@link IDtoObject} - данные создаваемого объекта
   * @return {@link ISkObject} - созданный или отредактированный объект
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsValidationFailedRtException не прошла проверка
   *           {@link ISkObjectServiceValidator#canCreateObject(IDtoObject)} или
   *           {@link ISkObjectServiceValidator#canEditObject(IDtoObject, ISkObject)}
   */
  <T extends ISkObject> T defineObject( IDtoObject aDtoObject );

  /**
   * Удалаяет объект.
   * <p>
   * При удалении объекта удаляются все прямые связи объекта.
   *
   * @param aSkid {@link Skid} - идентификатор объекта
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsValidationFailedRtException не прошла проверка {@link ISkObjectServiceValidator#canRemoveObject(Skid)}
   */
  void removeObject( Skid aSkid );

  /**
   * Удалаяет объекты.
   * <p>
   * При удалении объектов удаляются все прямые связи удаляемых объектов.
   *
   * @param aSkids {@link ISkidList} - идентификаторы объектов
   * @throws TsNullArgumentRtException любой аргумент = null
   * @throws TsValidationFailedRtException не прошла проверка {@link ISkObjectServiceValidator#canRemoveObject(Skid)}
   */
  void removeObjects( ISkidList aSkids );

  /**
   * Регистрирует создатель объектов по правилу проверки идентификатор класса.
   * <p>
   * Создатели,зарегистрированные для конкретного идентификатора
   * {@link #registerObjectCreator(String, ISkObjectCreator)} имеют приоритет перед зарегистрированными этим метоом.
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
   * Returns the service eventer.
   *
   * @return {@link ITsEventer}&lt;{@link ISkObjectServiceListener}&gt; - the service eventer
   */
  ITsEventer<ISkObjectServiceListener> eventer();

  /**
   * Returns the service validator.
   *
   * @return {@link ITsValidationSupport}&lt;{@link ISkObjectServiceValidator}&gt; - the service validator
   */
  ITsValidationSupport<ISkObjectServiceValidator> svs();

  // ------------------------------------------------------------------------------------
  // Convinience inline methods

  @SuppressWarnings( "javadoc" )
  default void registerObjectCreator( String aClassId, ISkObjectCreator<?> aCreator ) {
    TextMatcher tm = new TextMatcher( ETextMatchMode.EXACT, StridUtils.checkValidIdPath( aClassId ) );
    registerObjectCreator( tm, aCreator );
  }

}
