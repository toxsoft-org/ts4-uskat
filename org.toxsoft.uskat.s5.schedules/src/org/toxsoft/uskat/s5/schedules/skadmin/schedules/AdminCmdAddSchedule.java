package org.toxsoft.uskat.s5.schedules.skadmin.schedules;

import static org.toxsoft.uskat.s5.common.IS5CommonResources.*;
import static org.toxsoft.uskat.s5.schedules.lib.ISkSchedulesHardConstants.*;
import static org.toxsoft.uskat.s5.schedules.skadmin.schedules.IAdminHardConstants.*;
import static org.toxsoft.uskat.s5.schedules.skadmin.schedules.IAdminHardResources.*;
import static org.toxsoft.uskat.skadmin.core.EAdminCmdContextNames.*;

import org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.coll.IList;
import org.toxsoft.core.tslib.coll.primtypes.*;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.gw.skid.Skid;
import org.toxsoft.core.tslib.gw.ugwi.IUgwiList;
import org.toxsoft.core.tslib.gw.ugwi.UgwiList;
import org.toxsoft.uskat.core.ISkCoreApi;
import org.toxsoft.uskat.core.impl.dto.DtoFullObject;
import org.toxsoft.uskat.core.impl.dto.DtoObject;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.IPlexyValue;
import org.toxsoft.uskat.s5.schedules.lib.ISkSchedule;
import org.toxsoft.uskat.s5.schedules.lib.ISkScheduleService;
import org.toxsoft.uskat.skadmin.core.IAdminCmdCallback;
import org.toxsoft.uskat.skadmin.core.impl.AbstractAdminCmd;

/**
 * Команда s5admin: добавить расписание
 *
 * @author mvk
 */
public class AdminCmdAddSchedule
    extends AbstractAdminCmd {

  /**
   * Конструктор
   */
  public AdminCmdAddSchedule() {
    // Контекст: API ISkConnection
    addArg( CTX_SK_CORE_API );
    // Идентификатор расписания
    addArg( ARG_ID );
    // Имя расписания
    addArg( ARG_NAME );
    // Описание расписания
    addArg( ARG_DESCR );
    // Секунды
    addArg( ARG_SECONDS );
    // Минуты
    addArg( ARG_MINUTES );
    // Часы
    addArg( ARG_HOURS );
    // Дни месяца
    addArg( ARG_DAYS_OF_MONTH );
    // Месяцы
    addArg( ARG_MONTHS );
    // Дни недели
    addArg( ARG_DAYS_OF_WEEK );
    // Годы
    addArg( ARG_YEARS );
    // Часовой пояс
    addArg( ARG_TIMEZONE );
    // Время начала расписания
    addArg( ARG_START );
    // Время заверешния расписания
    addArg( ARG_END );
    // Ресурсы системы связанные с расписанием
    addArg( ARG_UGWIES );
  }

  // ------------------------------------------------------------------------------------
  // Реализация абстрактных методов AbstractAdminCmd
  //
  @Override
  public String id() {
    return CMD_ADD_SCHEDULE_ID;
  }

  @Override
  public String alias() {
    return CMD_ADD_SCHEDULE_ALIAS;
  }

  @Override
  public String nmName() {
    return CMD_ADD_SCHEDULE_NAME;
  }

  @Override
  public String description() {
    return CMD_ADD_SCHEDULE_DESCR;
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

    String scheduleId = argSingleValue( ARG_ID ).asString();
    IOptionSetEdit attrs = new OptionSet();
    IAvMetaConstants.DDEF_IDNAME.setValue( attrs, argSingleValue( ARG_NAME ) );
    IAvMetaConstants.DDEF_DESCRIPTION.setValue( attrs, argSingleValue( ARG_DESCR ) );
    attrs.setValue( ATRID_SECONDS, argSingleValue( ARG_SECONDS ) );
    attrs.setValue( ATRID_MINUTES, argSingleValue( ARG_MINUTES ) );
    attrs.setValue( ATRID_HOURS, argSingleValue( ARG_HOURS ) );
    attrs.setValue( ATRID_DAYS_OF_MONTH, argSingleValue( ARG_DAYS_OF_MONTH ) );
    attrs.setValue( ATRID_MONTHS, argSingleValue( ARG_MONTHS ) );
    attrs.setValue( ATRID_DAYS_OF_WEEK, argSingleValue( ARG_DAYS_OF_WEEK ) );
    attrs.setValue( ATRID_YEARS, argSingleValue( ARG_YEARS ) );
    attrs.setValue( ATRID_TIMEZONE, argSingleValue( ARG_TIMEZONE ) );
    attrs.setValue( ATRID_START, argSingleValue( ARG_START ) );
    attrs.setValue( ATRID_END, argSingleValue( ARG_END ) );
    IStringMapEdit<String> clobs = new StringMap<>();
    // Проверка формата
    try {
      IUgwiList ugwies = UgwiList.KEEPER.str2ent( argSingleValue( ARG_UGWIES ).asString() );
      clobs.put( CLBID_UGWIS, UgwiList.KEEPER.ent2str( ugwies ) );
    }
    catch( Throwable e ) {
      // Ошибка формата ugwies
      addResultError( ERR_UGWIES, cause( e ) );
      resultFail();
      return;
    }
    DtoObject dtoObj = new DtoObject( new Skid( ISkSchedule.CLASS_ID, scheduleId ), attrs, IStringMap.EMPTY );
    DtoFullObject dtoFullObj = new DtoFullObject( dtoObj, clobs, IStringMap.EMPTY );
    try {
      long startTime = System.currentTimeMillis();
      // Признак того, что объект уже существовал
      boolean needUpdate = (service.findSchedule( scheduleId ) != null);
      // Передача конфигурации на сервер
      service.defineSchedule( dtoFullObj );
      // Вывод сообщения
      addResultInfo( needUpdate ? MSG_CMD_UPDATED : MSG_CMD_ADDED );

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
