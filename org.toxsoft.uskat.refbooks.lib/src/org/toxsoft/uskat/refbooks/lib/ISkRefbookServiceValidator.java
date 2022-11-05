package org.toxsoft.uskat.refbooks.lib;

import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;

/**
 * Refbooks and items editing validation.
 *
 * @author hazard157
 */
public interface ISkRefbookServiceValidator {

  /**
   * Checks if refbook may be defined.
   *
   * @param aRefbookInfo {@link IDtoClassInfo} - information about refbook
   * @param aExistingRefbook {@link ISkRefbook} - existing refbook or <code>null</code> when creating new
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canDefineRefbook( IDtoClassInfo aRefbookInfo, ISkRefbook aExistingRefbook );

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
   * @param aNewItem {@link IDtoFullObject} - information about item
   * @param aOldItem {@link ISkRefbookItem} - existing item or <code>null</code> when creating new
   * @return {@link ValidationResult} - the check result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canDefineItem( ISkRefbook aRefbook, IDtoFullObject aNewItem, ISkRefbookItem aOldItem );

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
