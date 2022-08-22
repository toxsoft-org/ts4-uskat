package org.toxsoft.uskat.refbooks.lib;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * A refbook.
 *
 * @author hazard157
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
   * @return {@link IStridablesList}&lt;T&gt; - the refbook items list
   */
  <T extends ISkRefbookItem> IStridablesList<T> listItems();

  /**
   * Defines (either creates new or updates existing) item.
   * <p>
   * It is possible to use {@link ISkRefbookItem} descedanat implementations for refbook items. Item creator must be
   * defined via {@link ISkObjectService#registerObjectCreator(String, ISkObjectCreator)} before first use of this
   * method and before first access to the the uskat database content. When registering item objects creator use
   * {@link ISkRefbookServiceHardConstants#makeItemClassIdFromRefbookId(String)} as class ID.
   *
   * @param aItemInfo {@link IDtoFullObject} - the new properties of the item
   * @param aLinks {@link IStringMap}&lt;{@link ISkidList}&gt; - item links map "link ID" - "linked object IDs"
   * @return {@link ISkRefbookItem} - created or updated item
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException validation failed
   */
  ISkRefbookItem defineItem( IDtoFullObject aItemInfo, IStringMap<ISkidList> aLinks );

  /**
   * Removes the item.
   *
   * @param aItemId String - identifier of the item to be removed
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException validation failed
   */
  void removeItem( String aItemId );

  /**
   * Creates and returns the new instance of the refbook DPU information.
   * <p>
   * Return instance contains all attributes and links information of the refbok items, including superclass properties.
   *
   * @return {@link DtoObject} - refbook DPU information
   */
  DtoObject getRefbookInfoDpu();

}
