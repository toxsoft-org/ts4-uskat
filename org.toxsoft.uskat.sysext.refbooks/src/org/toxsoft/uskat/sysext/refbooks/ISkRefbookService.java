package org.toxsoft.uskat.sysext.refbooks;

import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.bricks.validator.impl.TsValidationFailedRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.core.api.ISkService;

/**
 * Refbooks support.
 *
 * @author goga
 */
public interface ISkRefbookService
    extends ISkService {

  /**
   * Service identifier.
   */
  String SERVICE_ID = ISkRefbookServiceHardConstants.SERVICE_ID;

  /**
   * Finds the refbook.
   *
   * @param aRefbookId String - the refbook identifier
   * @return {@link ISkRefbookItem} - found refbook or <code>null</code> if none
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ISkRefbook findRefbook( String aRefbookId );

  /**
   * Finds the refbook by the refbook item class identifier {@link ISkRefbook#itemClassId()}.
   *
   * @param aRefbookItemClassId String iterefbook item class identifier
   * @return {@link ISkRefbookItem} - found refbook or <code>null</code> if none
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ISkRefbook findRefbookByItemClassId( String aRefbookItemClassId );

  /**
   * Return all available refbooks.
   *
   * @return {@link IStridablesList}&lt;{@link ISkRefbook} &gt; - list of all refbooks
   */
  IStridablesList<ISkRefbook> listRefbooks();

  /**
   * Defined (either creates new or changes existing) refbook.
   *
   * @param aDpuRefbookInfo {@link ISkRefbookDpuInfo} - information about refbook
   * @return {@link ISkRefbook} - created or edited refbook
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed validation check in
   *           {@link ISkRefbookServiceValidator#canDefineRefbook(ISkRefbookDpuInfo, ISkRefbook)}
   */
  ISkRefbook defineRefbook( ISkRefbookDpuInfo aDpuRefbookInfo );

  /**
   * Removes refbook.
   *
   * @param aRefbookId String - identifier of the refbook to be removed
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed validation check in
   *           {@link ISkRefbookServiceValidator#canRemoveRefbook(String)}
   */
  void removeRefbook( String aRefbookId );

  /**
   * Возвращает испторию редактирования элементов справочника.
   * <p>
   * Если история не поддерживается реализацией, возвращает <code>null</code>.
   *
   * @return {@link ISkRefbookHistory} - доступ к истории правки элементов справочника или <code>null</code>
   */
  ISkRefbookHistory history();

  /**
   * Returns the service mutator methods pre-conditions validation helper.
   *
   * @return {@link ITsValidationSupport} - service changes validation support
   */
  ITsValidationSupport<ISkRefbookServiceValidator> svs();

  /**
   * Returns the service changes event firing helper.
   *
   * @return {@link ITsEventer} - event firing and listening helper
   */
  ITsEventer<ISkRefbookServiceListener> eventer();

}
