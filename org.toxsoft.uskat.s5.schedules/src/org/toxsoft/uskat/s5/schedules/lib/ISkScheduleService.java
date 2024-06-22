package org.toxsoft.uskat.s5.schedules.lib;

import javax.ejb.ScheduleExpression;

import org.toxsoft.core.tslib.bricks.events.ITsEventer;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkHardConstants;
import org.toxsoft.uskat.core.api.ISkService;
import org.toxsoft.uskat.core.api.linkserv.ISkLinkService;
import org.toxsoft.uskat.core.api.objserv.IDtoFullObject;
import org.toxsoft.uskat.core.api.objserv.ISkObjectService;

/**
 * Служба планирования.
 * <p>
 * Для реализации планирования используются расписания - объекты класса
 * {@link ISkSchedulesHardConstants#CLSID_SCHEDULE}). Количество расписаний неограничено.
 * <p>
 * Любой объект системы может иметь связь на расписание. Создание расписания, изменение его атрибутов или установка на
 * них связей других объектов системы проводится с помощью служб {@link ISkObjectService} и {@link ISkLinkService}.
 * <p>
 * Служба {@link ISkScheduleService} отслеживает объекты расписания (создание, изменение и удаление) и связи на них. Для
 * каждого расписания, согласно его атрибутам (подробнее смотри {@link ScheduleExpression}), формируется отдельный
 * таймер. Клиенты зарегистированные как слушатели службы информируются о событиях таймеров получая расписание и список
 * объектов связанных с этим расписанием.
 *
 * @author mvk
 */
public interface ISkScheduleService
    extends ISkService {

  /**
   * The service ID.
   */
  String SERVICE_ID = ISkHardConstants.SK_SYSEXT_SERVICE_ID_PREFIX + ".Schedules"; //$NON-NLS-1$

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
   * @return {@link ITsEventer}&lt;{@link ISkScheduleServiceListener}&gt; - the service eventer
   */
  ITsEventer<ISkScheduleServiceListener> eventer();

}
