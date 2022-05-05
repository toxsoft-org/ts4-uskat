package org.toxsoft.uskat.sysext.refbooks;

import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.skid.ISkidList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Refbooks and items editing validation.
 *
 * @author goga
 */
public interface ISkRefbookServiceValidator {

  /**
   * Checks if refbook may be defined.
   *
   * @param aDpuRefbookInfo {@link ISkRefbookDpuInfo} - information about refbool
   * @param aExistingRefbook {@link ISkRefbook} - existing refbook or <code>null</code> when creating new
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canDefineRefbook( ISkRefbookDpuInfo aDpuRefbookInfo, ISkRefbook aExistingRefbook );

  /**
   * Chacks if refbook can be removed.
   *
   * @param aRefbookId String - identifier of the refbook to be removed
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canRemoveRefbook( String aRefbookId );

  /**
   * Checks if refbook item may be defined.
   *
   * @param aRefbook {@link ISkRefbook} - the refbook
   * @param aDpuItemInfo {@link ISkRefbookDpuItemInfo} - information about item
   * @param aLinks {@link IStringMap}&lt;{@link ISkidList}&gt; - item links map "link ID" - "linked object IDs"
   * @param aExistingItem {@link ISkRefbookItem} - existing item or <code>null</code> when creating new
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canDefineItem( ISkRefbook aRefbook, ISkRefbookDpuItemInfo aDpuItemInfo, IStringMap<ISkidList> aLinks,
      ISkRefbookItem aExistingItem );

  /**
   * Chacks if refbook item can be removed.
   *
   * @param aRefbook {@link ISkRefbook} - the refbook
   * @param aItemId String - identifier of the item to be removed
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canRemoveItem( ISkRefbook aRefbook, String aItemId );

}
