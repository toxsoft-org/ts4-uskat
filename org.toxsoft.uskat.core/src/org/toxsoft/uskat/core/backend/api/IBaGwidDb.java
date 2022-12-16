package org.toxsoft.uskat.core.backend.api;

import org.toxsoft.core.tslib.bricks.strid.more.IdChain;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.gw.gwid.Gwid;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.devapi.IDevCoreApi;
import org.toxsoft.uskat.core.devapi.gwiddb.ISkGwidDbService;

/**
 * Backend addon: sectioned key-value (GWID-STRING) database management.
 * <p>
 * This is the mandatory addon needed by the {@link ISkGwidDbService} of the {@link IDevCoreApi}.
 *
 * @author hazard157
 */
public interface IBaGwidDb
    extends IBackendAddon {

  /**
   * ID of this backend addon.
   */
  String ADDON_ID = ISkBackendHardConstant.BAID_GWID_DB;

  /**
   * Returns existing section IDs.
   *
   * @return {@link IList}&lt;{@link IdChain}&gt; - the list of section IDs
   */
  IList<IdChain> listSectionIds();

  /**
   * Lists keys of existing values in database.
   *
   * @param aSectionId IdChain - the section ID
   * @return {@link IList}&lt; {@link Gwid} &gt; - the keys list
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  IList<Gwid> listKeys( IdChain aSectionId );

  /**
   * Read the value.
   * <p>
   * For unexsisting section/key returns null.
   *
   * @param aSectionId IdChain - the section ID
   * @param aKey {@link Gwid} - the key
   * @return String - the value or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  String readValue( IdChain aSectionId, Gwid aKey );

  /**
   * Writes value to th database.
   * <p>
   * Unexisting section and/or key will be created. Existing value will be overwritten.
   * <p>
   * Generates the message TODO ???
   *
   * @param aSectionId IdChain - the section ID
   * @param aKey {@link Gwid} - the key
   * @param aValue String - the value
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void writeValue( IdChain aSectionId, Gwid aKey, String aValue );

  /**
   * Removes the value from database.
   * <p>
   * Unexisting section or keys are ignored.
   *
   * @param aSectionId IdChain - the section ID
   * @param aKey {@link Gwid} - the key
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void removeValue( IdChain aSectionId, Gwid aKey );

  /**
   * Removes section with all data.
   * <p>
   * Removing last value in the section does <b>not</b> removes the section.
   * <p>
   * Does nothing if no such section exists.
   * <p>
   * Generates the message TODO ???
   *
   * @param aSectionId {@link IdChain} - the section ID
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  void removeSection( IdChain aSectionId );

}
