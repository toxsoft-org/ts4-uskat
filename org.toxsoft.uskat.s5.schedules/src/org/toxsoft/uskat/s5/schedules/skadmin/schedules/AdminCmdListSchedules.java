package org.toxsoft.uskat.s5.schedules.skadmin.schedules;

import static org.toxsoft.uskat.s5.schedules.skadmin.schedules.IAdminHardConstants.*;
import static org.toxsoft.uskat.s5.schedules.skadmin.schedules.IAdminHardResources.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;

import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.core.tslib.gw.ugwi.UgwiList;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.s5.schedules.lib.ISkSchedule;
import org.toxsoft.uskat.s5.schedules.lib.ISkScheduleService;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

/**
 * Команда s5admin: вывод зарегистрированных расписаний.
 *
 * @author mvk
 */
public class AdminCmdListSchedules
    extends AbstractAdminCmd {

  /**
   * Конструктор
   */
  public AdminCmdListSchedules() {
    // Контекст: API ISkConnection
    addArg( CTX_SK_CORE_API );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_LIST_SCHEDULES_ID;
  }

  @Override
  public String alias() {
    return CMD_LIST_SCHEDULES_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_LIST_SCHEDULES_NAME;
  }

  @Override
  public String description() {
    return CMD_LIST_SCHEDULES_DESCR;
  }

  @Override
  public IPlexyType resultType() {
    return IPlexyType.NONE;
  }

  @Override
  public IStringList roles() {
    return IStringList.EMPTY;
  }

  @Override
  public void doExec( IStringMap<IPlexyValue> aArgValues, IAdminCmdCallback aCallback ) {
    // API сервера
    ISkCoreApi coreApi = argSingleRef( CTX_SK_CORE_API );
    ISkScheduleService service = (ISkScheduleService)coreApi.services().getByKey( ISkScheduleService.SERVICE_ID );
    try {
      long startTime = System.currentTimeMillis();
      // Список расписаний. aIncludeSubclasses = true
      IList<ISkSchedule> schedules = service.listSchedules();
      // Вывод расписаний
      for( ISkSchedule schedule : schedules ) {
        printSchedule( schedule );
      }
      long delta = (System.currentTimeMillis() - startTime) / 1000;
      addResultInfo( MSG_CMD_TIME, Long.valueOf( delta ) );
      resultOk();
    }
    finally {
      // nop
    }
  }

  @Override
  protected IList<IPlexyValue> doPossibleValues( String aArgId, IStringMap<IPlexyValue> aArgValues ) {
    return IList.EMPTY;
  }

  // ------------------------------------------------------------------------------------
  // Внутренняя реализация
  //
  /**
   * Вывод расписания на консоль
   *
   * @param aSchedule {@link ISkSchedule} расписание
   * @throws TsNullArgumentRtException аргумент = null
   */
  private void printSchedule( ISkSchedule aSchedule ) {
    addResultInfo( MSG_SCHEDULE_LINE );
    addResultInfo( MSG_SCHEDULE, aSchedule.id(), //
        aSchedule.nmName(), //
        aSchedule.description(), //
        aSchedule.seconds(), //
        aSchedule.minutes(), //
        aSchedule.hours(), //
        aSchedule.daysOfMonth(), //
        aSchedule.months(), //
        aSchedule.daysOfWeek(), //
        aSchedule.years(), //
        aSchedule.timezone(), //
        TimeUtils.timestampToString( aSchedule.start() ), //
        TimeUtils.timestampToString( aSchedule.end() ), //
        UgwiList.KEEPER.ent2str( aSchedule.ugwis() ) //
    );
  }
}
