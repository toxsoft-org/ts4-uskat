package org.toxsoft.uskat.s5.cron.lib;

import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkHardConstants;
import org.toxsoft.uskat.core.api.ISkService;
import org.toxsoft.uskat.core.api.objserv.IDtoFullObject;

/**
 * Служба планирования.
 * <p>
 *
 * @author mvk
 */
public interface ISkCronService
    extends ISkService {

  /**
   * The service ID.
   */
  String SERVICE_ID = ISkHardConstants.SK_SYSEXT_SERVICE_ID_PREFIX + ".Cron"; //$NON-NLS-1$

  /**
   * Returns all schedules.
   *
   * @return {@link IStridablesList}&lt; {@link ISkSchedule} - list of all schedules
   */
  IStridablesList<ISkSchedule> listSchedules();

  /**
   * Finds the schedule object by ID.
   *
   * @param aScheduleId String the schedule ID
   * @return {@link ISkSchedule} - the found schedule or null
   */
  ISkSchedule findSchedule( String aScheduleId );

  /**
   * Creates new or updates the existing schedule object.
   *
   * @param aDtoSchedule {@link IDtoFullObject} - the schedule definition
   * @return {@link ISkSchedule} - new or edited schedule
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ISkSchedule defineSchedule( IDtoFullObject aDtoSchedule );

  /**
   * Removes the schedule if exists.
   *
   * @param aScheduleId String the schedule ID
   */
  void removeSchedule( String aScheduleId );

  /**
   * Returns the service eventer.
   *
   * @return {@link ITsEventer}&lt;{@link ISkCronServiceListener}&gt; - the service eventer
   */
  ITsEventer<ISkCronServiceListener> eventer();

}
