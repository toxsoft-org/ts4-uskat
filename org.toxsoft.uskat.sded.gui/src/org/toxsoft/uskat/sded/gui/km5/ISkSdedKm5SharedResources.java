package org.toxsoft.uskat.sded.gui.km5;

import org.toxsoft.uskat.sded.gui.km5.sded.*;

/**
 * Localizable resources.
 *
 * @author hazard157
 * @author dima
 */
@SuppressWarnings( "javadoc" )
public interface ISkSdedKm5SharedResources {

  /**
   * Common
   */
  String STR_N_DATA_TYPE       = Messages.getString( "STR_N_DATA_TYPE" );       //$NON-NLS-1$
  String STR_D_DATA_TYPE       = Messages.getString( "STR_D_DATA_TYPE" );       //$NON-NLS-1$
  String FMT_ERR_ID_NOT_IDPATH = Messages.getString( "FMT_ERR_ID_NOT_IDPATH" ); //$NON-NLS-1$

  /**
   * {@link SdedDtoPropInfoM5ModelBase}
   */
  String STR_N_PROP_ID          = Messages.getString( "STR_N_PROP_ID" );          //$NON-NLS-1$
  String STR_D_PROP_ID          = Messages.getString( "STR_D_PROP_ID" );          //$NON-NLS-1$
  String STR_N_PROP_NAME        = Messages.getString( "STR_N_PROP_NAME" );        //$NON-NLS-1$
  String STR_D_PROP_NAME        = Messages.getString( "STR_D_PROP_NAME" );        //$NON-NLS-1$
  String STR_N_PROP_DESCRIPTION = Messages.getString( "STR_N_PROP_DESCRIPTION" ); //$NON-NLS-1$
  String STR_D_PROP_DESCRIPTION = Messages.getString( "STR_D_PROP_DESCRIPTION" ); //$NON-NLS-1$

  /**
   * {@link SdedDtoClassInfoM5Model}
   */
  String STR_N_CLASS_ID          = Messages.getString( "STR_N_CLASS_ID" );          //$NON-NLS-1$
  String STR_D_CLASS_ID          = Messages.getString( "STR_D_CLASS_ID" );          //$NON-NLS-1$
  String STR_N_PARENT_ID         = Messages.getString( "STR_N_PARENT_ID" );         //$NON-NLS-1$
  String STR_D_PARENT_ID         = Messages.getString( "STR_D_PARENT_ID" );         //$NON-NLS-1$
  String STR_N_CLASS_NAME        = Messages.getString( "STR_N_CLASS_NAME" );        //$NON-NLS-1$
  String STR_D_CLASS_NAME        = Messages.getString( "STR_D_CLASS_NAME" );        //$NON-NLS-1$
  String STR_N_CLASS_DESCRIPTION = Messages.getString( "STR_N_CLASS_DESCRIPTION" ); //$NON-NLS-1$
  String STR_D_CLASS_DESCRIPTION = Messages.getString( "STR_D_CLASS_DESCRIPTION" ); //$NON-NLS-1$

  /**
   * {@link SdedDtoCmdInfoM5Model}
   */
  String STR_N_ARG_DEFS = Messages.getString( "STR_N_ARG_DEFS" ); //$NON-NLS-1$
  String STR_D_ARG_DEFS = Messages.getString( "STR_D_ARG_DEFS" ); //$NON-NLS-1$

  /**
   * {@link SdedDtoEvInfoM5Model}
   */
  String STR_N_PARAM_DEFS = Messages.getString( "STR_N_PARAM_DEFS" ); //$NON-NLS-1$
  String STR_D_PARAM_DEFS = Messages.getString( "STR_D_PARAM_DEFS" ); //$NON-NLS-1$

  /**
   * {@link SdedDtoRivetInfoM5Model}
   */
  String STR_N_RIVETED_COUNT = Messages.getString( "STR_N_RIVETED_COUNT" ); //$NON-NLS-1$
  String STR_D_RIVETED_COUNT = Messages.getString( "STR_D_RIVETED_COUNT" ); //$NON-NLS-1$

  /**
   * {@link SdedDtoRtdataInfoM5Model}
   */
  String STR_N_IS_CURR      = Messages.getString( "STR_N_IS_CURR" );      //$NON-NLS-1$
  String STR_D_IS_CURR      = Messages.getString( "STR_D_IS_CURR" );      //$NON-NLS-1$
  String STR_N_IS_HIST      = Messages.getString( "STR_N_IS_HIST" );      //$NON-NLS-1$
  String STR_D_IS_HIST      = Messages.getString( "STR_D_IS_HIST" );      //$NON-NLS-1$
  String STR_N_IS_SYNC      = Messages.getString( "STR_N_IS_SYNC" );      //$NON-NLS-1$
  String STR_D_IS_SYNC      = Messages.getString( "STR_D_IS_SYNC" );      //$NON-NLS-1$
  String STR_N_SYNC_DELTA_T = Messages.getString( "STR_N_SYNC_DELTA_T" ); //$NON-NLS-1$
  String STR_D_SYNC_DELTA_T = Messages.getString( "STR_D_SYNC_DELTA_T" ); //$NON-NLS-1$

  // String STR_N_M5M_CLASS = Messages.getString( "STR_N_M5M_CLASS" );
  // String STR_D_M5M_CLASS = Messages.getString( "STR_D_M5M_CLASS" );
  // String STR_N_ARG_DEFS = "аргументы";
  // String STR_D_ARG_DEFS = "Аргументы команды";
  // String STR_N_TMI_BY_HIERARCHY = Messages.getString( "STR_N_TMI_BY_HIERARCHY" );
  // String STR_D_TMI_BY_HIERARCHY = Messages.getString( "STR_D_TMI_BY_HIERARCHY" );

  // String STR_N_PARAMS = "params";
  // String STR_D_PARAMS = "Параметры";

  // String STR_N_ATOMIC_TYPE = "AtomicType";
  // String STR_D_ATOMIC_TYPE = "Атомарный тип";
  // String STR_N_PARAMS = "params";
  // String STR_D_PARAMS = "Параметры";
  // String STR_N_M5M_DATA_DEF = "M5 model IDataDef";
  // String STR_D_M5M_DATA_DEF = "M5 model IDataDef";
  // String STR_N_PARAM_SKID = "Skid";
  // String STR_D_PARAM_SKID = "Skid of object";

  // String STR_N_UPDATE_CINFO_FROM_ODS = "Обновить описание классов"; //$NON-NLS-1$
  // String STR_D_UPDATE_CINFO_FROM_ODS = "Обновить описание классов из ods файла"; //$NON-NLS-1$
  // String STR_N_UPDATE_FDO_FROM_ODS = "Обновить описание объектов"; //$NON-NLS-1$
  // String STR_D_UPDATE_FDO_FROM_ODS = "Обновить описание объектов из ods файла"; //$NON-NLS-1$
  // String STR_N_UPDATE_L2_CFGS_FROM_ODS = "Перегенерить конфигурацию моста OPC2Sk"; //$NON-NLS-1$
  // String STR_D_UPDATE_L2_CFGS_FROM_ODS =
  // "Перегенерить конфигурацию моста OPC2Sk по олписанию из ods файла"; //$NON-NLS-1$
  // String SELECT_FILE_4_IMPORT_STR = "Выберите файл для импорта классов"; //$NON-NLS-1$
  // String DEFAULT_PATH_STR = "C:\\"; //$NON-NLS-1$
  // String ODS_EXT = "*.ods"; //$NON-NLS-1$
  // String STR_UPDATE_CLASSES = "Обновление описания классов"; //$NON-NLS-1$
  // String STR_UPDATE_L2_CONFIGS = "Обновление конфигурации моста OPC2Sk"; //$NON-NLS-1$
  // String MSG_IMPORT_PROCESS_COMPLETED_ERROR_FREE = "Импорт завершился успешно.\n Обработан файл: %s"; //$NON-NLS-1$
  // String MSG_PROCESS_GENERATION_COMPLETED_ERROR_FREE =
  // "Процесс создания конф.файлов завершился успешно.\n Из файла: %s\nсозданы файлы:\n 1. %s\n 2. %s"; //$NON-NLS-1$
  // String MSG_GENERATION_PROCESS_FAILED = "При генерации возникли ошибки.\n Исходный файл генерации: %s";
  // //$NON-NLS-1$
  // String STR_DLG_GENERATE_L2_CONFIGS = "Генерация файлов конфигурации моста OPC2Sk"; //$NON-NLS-1$
  // String MSG_IMPORT_PROCESS_FAILED = "При импорте возникли ошибки.\n Исходный файл: %s"; //$NON-NLS-1$

}
