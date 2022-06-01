package org.toxsoft.uskat.skadmin.dev.batch;

import static org.toxsoft.uskat.skadmin.core.plugins.AdminPluginUtils.*;
import static org.toxsoft.uskat.skadmin.dev.batch.IAdminHardResources.*;

import org.toxsoft.core.tslib.av.IAtomicValue;
import org.toxsoft.core.tslib.av.impl.AvUtils;
import org.toxsoft.core.tslib.bricks.filter.ITsFilter;
import org.toxsoft.core.tslib.utils.TsLibUtils;
import org.toxsoft.uskat.legacy.plexy.IPlexyType;
import org.toxsoft.uskat.legacy.plexy.impl.PlexyValueUtils;
import org.toxsoft.uskat.skadmin.core.IAdminCmdArgDef;
import org.toxsoft.uskat.skadmin.core.IAdminCmdContextParam;
import org.toxsoft.uskat.skadmin.core.impl.AdminCmdArgDef;
import org.toxsoft.uskat.skadmin.dev.AdminPluginDev;

import ru.uskat.backend.addons.batchops.EOrphanProcessing;
import ru.uskat.common.dpu.container.IDpuContainer;

/**
 * Константы пакета.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IAdminHardConstants {

  /**
   * Кодировка текстовых файлов по умолчанию
   */
  String CHARSET_DEFAULT = "UTF-8";

  /**
   * Префикс идентификаторов команд и их алиасов плагина
   */
  String CMD_PATH_PREFIX = AdminPluginDev.DEV_CMD_PATH + "batch.";

  /**
   * Значение ALL.
   */
  IAtomicValue ALL = AvUtils.avStr( "ALL" );

  /**
   * Значение NONE.
   */
  IAtomicValue NONE = AvUtils.avStr( "NONE" );

  /**
   * Параметр контекста: контейнер данных системы {@link IDpuContainer}.
   */
  IAdminCmdContextParam DPU_CONTAINER_PARAM = new IAdminCmdContextParam() {

    @Override
    public String id() {
      return STR_DPU_CONTAINER_ID;
    }

    @Override
    public String nmName() {
      return id();
    }

    @Override
    public String description() {
      return STR_DPU_CONTAINER_DESCR;
    }

    @Override
    public IPlexyType type() {
      return PlexyValueUtils.ptSingleRef( IDpuContainer.class );
    }
  };

  // ------------------------------------------------------------------------------------
  // AdminCmdBatchRead
  //
  /**
   * Аргумент: Признак чтения также системных сужностей (например, объектов сессии). По умолчанию: false.
   */
  IAdminCmdArgDef ARG_INCLUDE_SYSTEM_ENTITIES =
      new AdminCmdArgDef( "includeSystemEntities", DT_BOOLEAN_NULLABLE, STR_ARG_INCLUDE_SYSTEM_ENTITIES );

  /**
   * Аргумент: Признак включения в контейнер описании выбранных классов. По умолчанию: true.
   */
  IAdminCmdArgDef ARG_INCLUDE_CLASS_INFOS =
      new AdminCmdArgDef( "includeClassInfos", DT_BOOLEAN_NULLABLE, STR_ARG_INCLUDE_CLASS_INFOS );

  /**
   * Аргумент: Признак включения в контейнер описании выбранных объектов. По умолчанию: true.
   */
  IAdminCmdArgDef ARG_INCLUDE_OBJECTS =
      new AdminCmdArgDef( "includeObjects", DT_BOOLEAN_NULLABLE, STR_ARG_INCLUDE_OBJECTS );

  /**
   * Аргумент: Признак включения в контейнер описании выбранных связей. По умолчанию: true.
   */
  IAdminCmdArgDef ARG_INCLUDE_LINKS = new AdminCmdArgDef( "includeLinks", DT_BOOLEAN_NULLABLE, STR_ARG_INCLUDE_LINKS );

  /**
   * Аргумент: Режим включения сиротских классов в контейнер. По умолчанию: {@link EOrphanProcessing#ENRICH}.
   */
  IAdminCmdArgDef ARG_ORPHAN_CLASSES =
      new AdminCmdArgDef( "orphanClassesProcessing", DT_STRING_NULLABLE, STR_ARG_ORPHAN_CLASSES );

  /**
   * Аргумент: Режим включения сиротских связей на объекты в контейнер. По умолчанию: {@link EOrphanProcessing#REMOVE}.
   */
  IAdminCmdArgDef ARG_ORPHAN_LINKS =
      new AdminCmdArgDef( "orphanLinksProcessing", DT_STRING_NULLABLE, STR_ARG_ORPHAN_LINKS );

  /**
   * Аргумент: фильтр выборки классов по идентификаторам. По умолчанию: {@link ITsFilter#ALL}.
   */
  IAdminCmdArgDef ARG_CLASS_FILTER = new AdminCmdArgDef( "classIdsFilter", DT_STRING_NULLABLE, STR_ARG_CLASS_FILTER );

  /**
   * Аргумент: фильтр выборки типов по идентификаторам. По умолчанию: {@link ITsFilter#ALL}.
   */
  IAdminCmdArgDef ARG_TYPE_FILTER = new AdminCmdArgDef( "typeIdsFilter", DT_STRING_NULLABLE, STR_ARG_TYPE_FILTER );

  /**
   * Аргумент: фильтр выборки clob по идентификаторам. По умолчанию: {@link ITsFilter#NONE}.
   */
  IAdminCmdArgDef ARG_CLOB_FILTER = new AdminCmdArgDef( "clobIdsFilter", DT_STRING_NULLABLE, STR_ARG_CLOB_FILTER );

  /**
   * Аргумент: текстовый файл, в котором сохраняется контейнер.
   */
  IAdminCmdArgDef ARG_FILE = new AdminCmdArgDef( "file", DT_STRING_NULLABLE, STR_ARG_FILE );

  String CMD_BATCH_READ_ID    = CMD_PATH_PREFIX + "batchRead";
  String CMD_BATCH_READ_ALIAS = TsLibUtils.EMPTY_STRING;
  String CMD_BATCH_READ_NAME  = TsLibUtils.EMPTY_STRING;
  String CMD_BATCH_READ_DESCR = STR_CMD_BATCH_READ;

}
