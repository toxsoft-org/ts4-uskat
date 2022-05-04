package org.toxsoft.uskat.sysext.refbooks;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.validator.impl.TsValidationFailedRtException;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.sysext.refbooks.impl.SkRefbookDpuInfo;

import ru.uskat.core.api.objserv.ISkObjectService;
import ru.uskat.core.common.skobject.ISkObject;
import ru.uskat.core.common.skobject.ISkObjectCreator;

/**
 * An refbook.
 *
 * @author goga
 */
public interface ISkRefbook
    extends ISkObject {

  /**
   * Returns the refbook items class identifier.
   * <p>
   * The item objects have this class identifier as {@link ISkObject#classId()}.
   *
   * @return String - the refbook items class identifier
   */
  String itemClassId();

  // boolean isUserEdiable(); // false= GUI can not CRUD refbook and any of it's item. CRUD via API is enabled

  /**
   * Returns the refbook items idnetifiers, not items themselfs.
   *
   * @return {@link IList}&lt;T&gt; - the refbook items identifiers list
   */
  ISkidList listItemIds();

  /**
   * Finds the item by the specified identifier {@link ISkRefbookItem#id()}.
   * <p>
   * Once again note that itemidentifier {@link ISkRefbookItem#id()} the same as it's {@link ISkObject#strid()}.
   *
   * @param <T> - items expected type
   * @param aItemId String - the specified identifier
   * @return &ltT&gt; - the found item or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsNullArgumentRtException identifier is not valid IDpath
   */
  <T extends ISkRefbookItem> T findItem( String aItemId );

  /**
   * Returns the refbook items.
   *
   * @param <T> - items expected type
   * @return {@link IList}&lt;T&gt; - the refbook items list
   */
  <T extends ISkRefbookItem> IList<T> listItems();

  /**
   * Defines (either creates new or updates existing) item.
   * <p>
   * It is possible to use {@link ISkRefbookItem} descedanat implementations for refbook items. Item creator must be
   * defined via {@link ISkObjectService#registerObjectCreator(String, ISkObjectCreator)} before first use of this
   * method and before first access to the the uskat database content. When registering item objects creator use
   * {@link ISkRefbookServiceHardConstants#makeItemClassIdFromRefbookId(String)} as class ID.
   *
   * @param aItemInfo {@link ISkRefbookDpuItemInfo} - the new properties of the item
   * @param aLinks {@link IStringMap}&lt;{@link ISkidList}&gt; - item links map "link ID" - "linked object IDs"
   * @return {@link ISkRefbookItem} - created or updated item
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException validation failed in
   *           {@link ISkRefbookServiceValidator#canDefineItem(ISkRefbook, ISkRefbookDpuItemInfo, IStringMap, ISkRefbookItem)}
   */
  ISkRefbookItem defineItem( ISkRefbookDpuItemInfo aItemInfo, IStringMap<ISkidList> aLinks );

  /**
   * Removes the item.
   *
   * @param aItemId String - identifier of the item to be removed
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException validation failed in
   *           {@link ISkRefbookServiceValidator#canRemoveItem(ISkRefbook, String)}
   */
  void removeItem( String aItemId );

  /**
   * Creates and returns the new instance of the refbook DPU information.
   * <p>
   * Return instance contains all attributes and links information of the refbok items, including superclass properties.
   *
   * @return {@link ISkRefbookDpuInfo} - refbook DPU information
   */
  SkRefbookDpuInfo getRefbookInfoDpu();

  /**
   * Returns the refbook items.
   * <p>
   * This is inline method for convinience.
   *
   * @param <T> - items expected type
   * @return {@link IStridablesList}&lt;T&gt; - the refbook items list
   */
  default <T extends ISkRefbookItem> IStridablesList<T> items() {
    return new StridablesList<>( listItems() );
  }

}
