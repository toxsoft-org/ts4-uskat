package org.toxsoft.uskat.refbooks.lib;

import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.bricks.time.ITimedList;
import org.toxsoft.core.tslib.bricks.validator.ITsValidationSupport;
import org.toxsoft.core.tslib.bricks.validator.impl.TsValidationFailedRtException;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkHardConstants;
import org.toxsoft.uskat.core.api.ISkService;
import org.toxsoft.uskat.core.api.evserv.SkEvent;
import org.toxsoft.uskat.core.api.objserv.ISkObjectServiceListener;
import org.toxsoft.uskat.core.api.objserv.ISkObjectServiceValidator;

/**
 * Refbooks support.
 *
 * @author hazard157
 */
public interface ISkRefbookService
    extends ISkService {

  /**
   * The service ID.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".Refbooks"; //$NON-NLS-1$

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
   * @param aDpuRefbookInfo {@link IDtoRefbookInfo} - information about refbook
   * @return {@link ISkRefbook} - created or edited refbook
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException validation failed
   */
  ISkRefbook defineRefbook( IDtoRefbookInfo aDpuRefbookInfo );

  /**
   * Removes refbook.
   *
   * @param aRefbookId String - identifier of the refbook to be removed
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException validation failed
   */
  void removeRefbook( String aRefbookId );

  /**
   * Returns the refbook editing history for specified time interval.
   * <p>
   * Note: the event has structure as specified in {@link ISkRefbookServiceHardConstants#EVDTO_REFBOOK_EDIT},
   *
   * @param aInterval {@link IQueryInterval} - query time interval
   * @param aRefbookId String - ID of the refbook
   * @return {@link ITimedList}&lt;{@link SkEvent}&gt; - list of the reuried events
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such refbook exists
   */
  ITimedList<SkEvent> queryRefbookEditHistory( IQueryInterval aInterval, String aRefbookId );

  // ------------------------------------------------------------------------------------
  // Service support

  /**
   * Returns the service validator.
   *
   * @return {@link ITsValidationSupport}&lt;{@link ISkObjectServiceValidator}&gt; - the service validator
   */
  ITsValidationSupport<ISkRefbookServiceValidator> svs();

  /**
   * Returns the service eventer.
   *
   * @return {@link ITsEventer}&lt;{@link ISkObjectServiceListener}&gt; - the service eventer
   */
  ITsEventer<ISkRefbookServiceListener> eventer();

}
