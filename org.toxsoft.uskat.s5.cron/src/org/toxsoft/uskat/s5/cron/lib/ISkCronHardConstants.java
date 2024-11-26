package org.toxsoft.uskat.s5.cron.lib;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.core.tslib.gw.IGwHardConstants.*;
import static org.toxsoft.uskat.core.ISkHardConstants.*;
import static org.toxsoft.uskat.s5.cron.lib.ISkResources.*;

import java.util.Calendar;

import org.toxsoft.core.tslib.av.impl.DataType;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetUtils;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.strid.impl.Stridable;
import org.toxsoft.core.tslib.bricks.time.impl.TimeUtils;
import org.toxsoft.core.tslib.gw.ugwi.UgwiList;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;
import org.toxsoft.uskat.core.backend.ISkBackendHardConstant;
import org.toxsoft.uskat.core.impl.dto.*;

/**
 * Alarm service constants.
 *
 * @author mvk
 */
@SuppressWarnings( { "javadoc", "nls" } )
public interface ISkCronHardConstants {

  /**
   * Дни недели в порядке определяемым спецификацией {@link Calendar}
   */
  String[] CALENDAR_WEEK_DAYS = { "SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT" };

  /**
   * Пробел
   */
  String SPACE = " ";

  /**
   * Запятая
   */
  String COMMA = ",";

  /**
   * Символ подстановки
   */
  String WILDCARD = "*";

  /**
   * Символ подстановки
   */
  String QUSTION = "?";

  // ------------------------------------------------------------------------------------
  // IBaCrone
  //
  String BAID_CRON = ISkBackendHardConstant.SKB_ID + ".Cron";

  IStridable BAINF_CRON = new Stridable( BAID_CRON, STR_N_BA_SCHEDULES, STR_D_BA_SCHEDULES );

  // ------------------------------------------------------------------------------------
  // Schedule class properties IDs

  String CLSID_SCHEDULE = SK_ID + ".Schedule";

  String ATRID_SECONDS = "second";

  String ATRID_MINUTES = "minute";

  String ATRID_HOURS = "hour";

  String ATRID_DAY = "day";

  String ATRID_DAYS_OF_MONTH = "dayOfMonth";

  String ATRID_MONTHS = "month";

  String ATRID_DAYS_OF_WEEK = "dayOfWeek";

  String ATRID_YEARS = "year";

  String ATRID_TIMEZONE = "timezone";

  String ATRID_START = "start";

  String ATRID_END = "end";

  String CLBID_UGWIS = "ugwis";

  String EVID_ON_SCHEDULED = "onScheduled";

  // ------------------------------------------------------------------------------------
  // Schedule class properties INFOs

  DtoAttrInfo ATRINF_SECONDS = DtoAttrInfo.create2( ATRID_SECONDS, //
      DataType.create( STRING, //
          TSID_DEFAULT_VALUE, avStr( "0" ) // //$NON-NLS-1$
      ), //
      TSID_NAME, STR_N_ATTR_SCHEDULE_SECOND, //
      TSID_DESCRIPTION, STR_D_ATTR_SCHEDULE_SECOND //
  );

  DtoAttrInfo ATRINF_MINUTES = DtoAttrInfo.create2( ATRID_MINUTES, //
      DataType.create( STRING, //
          TSID_DEFAULT_VALUE, avStr( "0" ) // //$NON-NLS-1$
      ), //
      TSID_NAME, STR_N_ATTR_SCHEDULE_MINUTE, //
      TSID_DESCRIPTION, STR_D_ATTR_SCHEDULE_MINUTE//
  );

  DtoAttrInfo ATRINF_HOURS = DtoAttrInfo.create2( ATRID_HOURS, //
      DataType.create( STRING, //
          TSID_DEFAULT_VALUE, avStr( "0" ) // //$NON-NLS-1$
      ), //
      TSID_NAME, STR_N_ATTR_SCHEDULE_HOUR, //
      TSID_DESCRIPTION, STR_D_ATTR_SCHEDULE_HOUR //
  );

  DtoAttrInfo ATRINF_DAYS_OF_MONTH = DtoAttrInfo.create2( ATRID_DAYS_OF_MONTH, //
      DataType.create( STRING, //
          TSID_DEFAULT_VALUE, avStr( "*" ) // //$NON-NLS-1$
      ), //
      TSID_NAME, STR_N_ATTR_SCHEDULE_DAY_OF_MONTH, //
      TSID_DESCRIPTION, STR_D_ATTR_SCHEDULE_DAY_OF_MONTH //
  );

  DtoAttrInfo ATRINF_MONTHS = DtoAttrInfo.create2( ATRID_MONTHS, //
      DataType.create( STRING, //
          TSID_DEFAULT_VALUE, avStr( "*" ) // //$NON-NLS-1$
      ), //
      TSID_NAME, STR_N_ATTR_SCHEDULE_MONTH, //
      TSID_DESCRIPTION, STR_D_ATTR_SCHEDULE_MONTH //
  );

  DtoAttrInfo ATRINF_DAYS_OF_WEEK = DtoAttrInfo.create2( ATRID_DAYS_OF_WEEK, //
      DataType.create( STRING, //
          TSID_DEFAULT_VALUE, avStr( "*" ) // //$NON-NLS-1$
      ), //
      TSID_NAME, STR_N_ATTR_SCHEDULE_DAY_OF_WEEK, //
      TSID_DESCRIPTION, STR_D_ATTR_SCHEDULE_DAY_OF_WEEK//
  );

  DtoAttrInfo ATRINF_YEARS = DtoAttrInfo.create2( ATRID_YEARS, //
      DataType.create( STRING, //
          TSID_DEFAULT_VALUE, avStr( "*" ) // //$NON-NLS-1$
      ), //
      TSID_NAME, STR_N_ATTR_SCHEDULE_YEAR, //
      TSID_DESCRIPTION, STR_D_ATTR_SCHEDULE_YEAR//
  );

  DtoAttrInfo ATRINF_TIMEZONE = DtoAttrInfo.create2( ATRID_TIMEZONE, //
      DataType.create( STRING, //
          TSID_DEFAULT_VALUE, avStr( TsLibUtils.EMPTY_STRING ) //
      ), //
      TSID_NAME, STR_N_ATTR_SCHEDULE_TIMEZONE, //
      TSID_DESCRIPTION, STR_D_ATTR_SCHEDULE_TIMEZONE//
  );

  DtoAttrInfo ATRINF_START = DtoAttrInfo.create2( ATRID_START, //
      DataType.create( TIMESTAMP, //
          TSID_DEFAULT_VALUE, avTimestamp( TimeUtils.readTimestamp( "2000-01-01" ) ) //
      ), //
      TSID_NAME, STR_N_ATTR_SCHEDULE_START, //
      TSID_DESCRIPTION, STR_D_ATTR_SCHEDULE_START//
  );

  DtoAttrInfo ATRINF_END = DtoAttrInfo.create2( ATRID_END, //
      DataType.create( TIMESTAMP, //
          TSID_DEFAULT_VALUE, avTimestamp( TimeUtils.readTimestamp( "2050-01-01" ) ) //
      ), //
      TSID_NAME, STR_N_ATTR_SCHEDULE_END, //
      TSID_DESCRIPTION, STR_D_ATTR_SCHEDULE_END//
  );

  DtoClobInfo CLBINF_UGWIS = DtoClobInfo.create2( CLBID_UGWIS, //
      TSID_NAME, STR_N_CLBID_SCHEDULE_UGWIS, //
      TSID_DESCRIPTION, STR_D_CLBID_SCHEDULE_UGWIS, //
      TSID_KEEPER_ID, UgwiList.KEEPER_ID //
  );

  DtoEventInfo EVINF_ON_SCHEDULED = DtoEventInfo.create1( EVID_ON_SCHEDULED, true, //
      new StridablesList<>(), //
      OptionSetUtils.createOpSet( //
          TSID_NAME, STR_EV_ON_SCHEDULED, //
          TSID_DESCRIPTION, STR_EV_ON_SCHEDULED_D //
      ) );

  IDtoClassInfo CLSINF_SCHEDULE = DtoClassInfo.create( CLSID_SCHEDULE, GW_ROOT_CLASS_ID, //
      OptionSetUtils.createOpSet( //
          TSID_NAME, STR_N_CLASS_SCHEDULE, //
          TSID_DESCRIPTION, STR_D_CLASS_SCHEDULE, //
          OPDEF_SK_IS_SOURCE_CODE_DEFINED_CLASS, AV_TRUE //
      ), //
      new StridablesList<>( ATRINF_SECONDS, ATRINF_MINUTES, ATRINF_HOURS, ATRINF_DAYS_OF_MONTH, ATRINF_MONTHS,
          ATRINF_DAYS_OF_WEEK, ATRINF_YEARS, ATRINF_TIMEZONE, ATRINF_START, ATRINF_END ),
      new StridablesList<>( CLBINF_UGWIS ), //
      IStridablesList.EMPTY, //
      IStridablesList.EMPTY, //
      IStridablesList.EMPTY, //
      IStridablesList.EMPTY, //
      new StridablesList<>( EVINF_ON_SCHEDULED ) //
  );

}
