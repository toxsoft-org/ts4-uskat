package org.toxsoft.uskat.s5.common.sysdescr;

import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.uskat.core.api.sysdescr.dto.IDtoClassInfo;

/**
 * Читатель DPU-описаний типов и классов бекенда
 *
 * @author mvk
 */
public interface ISkSysdescrDtoReader {

  /**
   * Возвращает список DTO-данных зарегистрированных классов
   *
   * @return {@link IStridablesList}&lt;{@link IDtoClassInfo}&lt; список описаний классов
   */
  IStridablesList<IDtoClassInfo> readClassInfos();

}
