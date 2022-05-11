package org.toxsoft.uskat.sysext.alarms.api;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.impl.DataDef.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.av.metainfo.IDataDef;

/**
 * Описание параметров истории обработки аларма - наименование, тип и т.д.
 *
 * @author dima
 */
@SuppressWarnings( "nls" )
public interface ISkAlarmHistoryParamsDefinitions {

  /**
   * FIXME - оставлено для примера Параметр - булевый.
   */
  IDataDef BOOL_PARAM = create( "BoolHistoryParam", EAtomicType.BOOLEAN, TSID_NAME, "Bool param", //
      TSID_DESCRIPTION, "Boolean history parameter", //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.AV_TRUE );

  /**
   * FIXME - оставлено для примера Параметр - целочисленный.
   */
  IDataDef INT_PARAM = create( "IntHistoryParam", EAtomicType.INTEGER, TSID_NAME, "Int param", //
      TSID_DESCRIPTION, "Integer history parameter", //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.AV_0 );

  /**
   * Параметр - текстовое сообщение.
   */
  IDataDef ALARM_HIST_MSG = create( "AlarmHistMsg", EAtomicType.STRING, TSID_NAME, "SkAlarm history message", //
      TSID_DESCRIPTION, "SkAlarm history message", //
      TSID_IS_NULL_ALLOWED, AV_FALSE, //
      TSID_DEFAULT_VALUE, AvUtils.avStr( "SkAlarm history message" ) );
}
