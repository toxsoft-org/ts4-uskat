package org.toxsoft.uskat.s5.cron.skadmin.cron;

import static org.toxsoft.uskat.s5.cron.skadmin.cron.IAdminHardConstants.*;
import static org.toxsoft.uskat.s5.cron.skadmin.cron.IAdminHardResources.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;

import org.toxsoft.core.tslib.bricks.validator.ValidationResult;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.IStringList;
import org.toxsoft.core.tslib.coll.primtypes.IStringMap;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.s5.cron.lib.ISkSchedule;
import org.toxsoft.uskat.s5.cron.lib.ISkCronService;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

/**
 * Команда s5admin: удаление расписания
 *
 * @author mvk
 */
public class AdminCmdRemoveSchedule
    extends AbstractAdminCmd {

  /**
   * Конструктор
   */
  public AdminCmdRemoveSchedule() {
    // Контекст: API ISkConnection
    addArg( CTX_SK_CORE_API );
    // Идентификатор расписания
    addArg( ARG_ID );
    // Режим обработки запросов системы
    addArg( ARG_YES_ID );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_REMOVE_SCHEDULE_ID;
  }

  @Override
  public String alias() {
    return CMD_REMOVE_SCHEDULE_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_REMOVE_SCHEDULE_NAME;
  }

  @Override
  public String description() {
    return CMD_REMOVE_SCHEDULE_DESCR;
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
    ISkCronService service = (ISkCronService)coreApi.services().getByKey( ISkCronService.SERVICE_ID );

    String scheduleId = argSingleValue( ARG_ID ).asString();
    boolean yes = argSingleValue( ARG_YES_ID ).asBool();

    ISkSchedule schedule = service.findSchedule( scheduleId );
    if( schedule == null ) {
      // Расписание не найдено
      addResultInfo( MSG_CMD_NOT_FOUND );
      resultFail();
      return;
    }
    // Все готово для импорта объектов. Последнее "китайское" предупреждение
    if( !yes && !queryClientConfirm( ValidationResult.warn( MSG_CMD_CONFIRM_REMOVE ), false ) ) {
      // Пользователь отказался от продолжения
      addResultInfo( MSG_CMD_REJECT, id() );
      resultFail();
      return;
    }
    try {
      long startTime = System.currentTimeMillis();
      service.removeSchedule( scheduleId );
      addResultInfo( MSG_CMD_REMOVED );
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
}
