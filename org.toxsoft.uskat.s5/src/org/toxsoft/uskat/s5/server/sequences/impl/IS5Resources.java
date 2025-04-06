package org.toxsoft.uskat.s5.server.sequences.impl;

/**
 * Константы, локализуемые ресурсы реализации.
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  /**
   * Шаблон формирования toString для блоков {@link S5SequenceBlock}
   */
  String BLOCK_TO_STRING_FORMAT = Messages.getString( "IS5Resources.BLOCK_TO_STRING_FORMAT" );

  /**
   * Шаблон формирования toString для blob {@link S5SequenceBlob}
   */
  String BLOB_TO_STRING_FORMAT = Messages.getString( "IS5Resources.BLOB_TO_STRING_FORMAT" );

  /**
   * Шаблон формирования toString для blob {@link S5SequenceBlob}
   */
  String BLOB_ASYNC_TO_STRING_FORMAT = Messages.getString( "IS5Resources.BLOB_ASYNC_TO_STRING_FORMAT" );

  // ------------------------------------------------------------------------------------
  // Строки константы
  //
  String STR_DO_PARTITION_AFTER_START = "doPartitionAfterStart";
  String STR_DO_PARTITION             = "doPartition";
  String STR_DO_DEFRAG                = "doDefrag";

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_ERROR_QUERY = Messages.getString( "IS5Resources.MSG_ERROR_QUERY" );

  String MSG_UNION_START_THREAD          = Messages.getString( "IS5Resources.MSG_UNION_START_THREAD" );
  String MSG_SINGLETON_UNION_TASK_START  = Messages.getString( "IS5Resources.MSG_SINGLETON_UNION_TASK_START" );
  String MSG_SINGLETON_UNION_TASK_FINISH = Messages.getString( "IS5Resources.MSG_SINGLETON_UNION_TASK_FINISH" );
  String MSG_UNION_TASK_FINISH           = Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH" )
      + Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH___1" ) +                                        //
      Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH___2" ) +                                          //
      Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH___3" ) +                                          //
      Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH___4" ) +                                          //
      Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH___5" ) +                                          //
      Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH___6" ) +                                          //
      Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH___7" ) +                                          //
      Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH___8" );
  String MSG_UNION_START                 = Messages.getString( "IS5Resources.MSG_UNION_START" );
  String MSG_UNION_FINISH                = Messages.getString( "IS5Resources.MSG_UNION_FINISH" );
  String MSG_UNION_PASS_FINISH           = Messages.getString( "IS5Resources.MSG_UNION_PASS_FINISH" );

  String MSG_PARTITION_TASK_NOT_FOUND = "%s. There are no tasks for process partitions.";

  String MSG_PARTITION_START_THREAD          = Messages.getString( "IS5Resources.MSG_PARTITION_START_THREAD" );
  String MSG_SINGLETON_PARTITION_TASK_START  = Messages.getString( "IS5Resources.MSG_SINGLETON_PARTITION_TASK_START" );
  String MSG_SINGLETON_PARTITION_TASK_FINISH = Messages.getString( "IS5Resources.MSG_SINGLETON_PARTITION_TASK_FINISH" );
  String MSG_PARTITION_TASK_FINISH           = Messages.getString( "IS5Resources.MSG_PARTITION_TASK_FINISH" )
      + Messages.getString( "IS5Resources.MSG_PARTITION_TASK_FINISH___1" ) +                                            //
      Messages.getString( "IS5Resources.MSG_PARTITION_TASK_FINISH___2" ) +                                              //
      Messages.getString( "IS5Resources.MSG_PARTITION_TASK_FINISH___3" ) +                                              //
      Messages.getString( "IS5Resources.MSG_PARTITION_TASK_FINISH___40" ) +                                             //
      Messages.getString( "IS5Resources.MSG_PARTITION_TASK_FINISH___4" ) +                                              //
      Messages.getString( "IS5Resources.MSG_PARTITION_TASK_FINISH___6" ) +                                              //
      Messages.getString( "IS5Resources.MSG_PARTITION_TASK_FINISH___7" ) +                                              //
      Messages.getString( "IS5Resources.MSG_PARTITION_TASK_FINISH___8" );
  String MSG_PARTITION_START                 = "%s. start partition handle. aAuthor = %s.";
  String MSG_PARTITION_FINISH                = Messages.getString( "IS5Resources.MSG_PARTITION_FINISH" );
  String MSG_PARTITION_PASS_FINISH           = Messages.getString( "IS5Resources.MSG_PARTITION_PASS_FINISH" );

  String MSG_VALIDATION_UPDATE         = Messages.getString( "IS5Resources.MSG_VALIDATION_UPDATE" );
  String MSG_VALIDATION_REMOVE         = Messages.getString( "IS5Resources.MSG_VALIDATION_REMOVE" );
  String MSG_VALIDATION_REMOVE_UNORDER = Messages.getString( "IS5Resources.MSG_VALIDATION_REMOVE_UNORDER" );
  String MSG_VALIDATION_FINISH         = Messages.getString( "IS5Resources.MSG_VALIDATION_FINISH" );
  String MSG_CREATE_QUERY              = Messages.getString( "IS5Resources.MSG_CREATE_QUERY" );
  String MSG_EXECUTE_SUB_QUERY         = Messages.getString( "IS5Resources.MSG_EXECUTE_SUB_QUERY" );
  String MSG_FINISH_QUERY              = Messages.getString( "IS5Resources.MSG_FINISH_QUERY" );
  String MSG_START_EXECUTE_QUERY       = Messages.getString( "IS5Resources.MSG_START_EXECUTE_QUERY" );
  String MSG_FINISH_EXECUTE_QUERY      = Messages.getString( "IS5Resources.MSG_FINISH_EXECUTE_QUERY" );
  String MSG_READ_BEFORE_SEQUENCE_TIME = Messages.getString( "IS5Resources.MSG_READ_BEFORE_SEQUENCE_TIME" );
  String MSG_READ_SEQUENCE_TIME        = Messages.getString( "IS5Resources.MSG_READ_SEQUENCE_TIME" );
  String MSG_WRITE_SEQUENCE_TIME       = Messages.getString( "IS5Resources.MSG_WRITE_SEQUENCE_TIME" );
  String MSG_FIND_START_TIME           = Messages.getString( "IS5Resources.MSG_FIND_START_TIME" );
  String MSG_FIND_END_TIME             = Messages.getString( "IS5Resources.MSG_FIND_END_TIME" );
  String MSG_CALC_AVAILABLE_BLOCK      = Messages.getString( "IS5Resources.MSG_CALC_AVAILABLE_BLOCK" );
  String MSG_CREATE_UNION_TIMER        = Messages.getString( "IS5Resources.MSG_CREATE_UNION_TIMER" );
  String MSG_CANCEL_UNION_TIMER        = Messages.getString( "IS5Resources.MSG_CANCEL_UNION_TIMER" );
  String MSG_CREATE_PARTITION_TIMER    = Messages.getString( "IS5Resources.MSG_CREATE_PARTITION_TIMER" );
  String MSG_CANCEL_PARTITION_TIMER    = Messages.getString( "IS5Resources.MSG_CANCEL_PARTITION_TIMER" );
  String MSG_AUTO_VALIDATION_AUTHOR    = Messages.getString( "IS5Resources.MSG_AUTO_VALIDATION_AUTHOR" );
  String MSG_UNION_AUTHOR_INIT         = Messages.getString( "IS5Resources.MSG_UNION_AUTHOR_INIT" );
  String MSG_UNION_AUTHOR_SCHEDULE     = Messages.getString( "IS5Resources.MSG_UNION_AUTHOR_SCHEDULE" );
  String MSG_UNION_AUTO_OFF            = Messages.getString( "IS5Resources.MSG_UNION_AUTO_OFF" );
  String MSG_UNION_AUTO_INTERVAL       = Messages.getString( "IS5Resources.MSG_UNION_AUTO_INTERVAL" );
  String MSG_UNION_AUTO_ADD_INFO       = Messages.getString( "IS5Resources.MSG_UNION_AUTO_ADD_INFO" );
  String MSG_UNION_AUTO_REPEAT         = Messages.getString( "IS5Resources.MSG_UNION_AUTO_REPEAT" );

  String MSG_PARTITION_CHECK_AFTER_STARTUP =
      "[%s] partition(...): Start partition check after startup/reconfiguration. Interval = %d (msec)";
  String MSG_PARTITION_AUTHOR_INIT         = "Post-startup/reconfiguration processing";
  String MSG_PARTITION_AUTHOR_SCHEDULE     = "Scheduled processing";

  String MSG_PARTITION_PLAN_CREATE = "[%s] %s.%s. It's planned to create table partitions in auto mode: %s";
  String MSG_PARTITION_PLAN_ADD    =
      "[%s] %s.%s. depth = %d (days). It's planned to add table partitions in auto mode: %s";
  String MSG_PARTITION_PLAN_REMOVE =
      "[%s] %s.%s. depth = %d (days). It's planned to remove table partitions in auto mode: %s";

  String MSG_LAST_VALUES_ASYNC_QUERY = Messages.getString( "IS5Resources.MSG_LAST_VALUES_ASYNC_QUERY" );
  String MSG_LAST_VALUE_ASYNC_QUERY  = Messages.getString( "IS5Resources.MSG_LAST_VALUE_ASYNC_QUERY" );

  String MSG_VALIDATION_TASK_START  = Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_START" );
  String MSG_VALIDATION_TASK_FINISH = Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH" ) +   //
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___1" ) +                               //
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___2" ) +                               //
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___3" ) +                               //
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___4" ) +                               //
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___5" ) +                               //
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___6" ) +                               //
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___7" ) +                               //
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___8" ) +                               //
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___9" );
  String MSG_DBMS_LOADED            = Messages.getString( "IS5Resources.MSG_DBMS_LOADED" );
  String MSG_DBMS_ADDED             = Messages.getString( "IS5Resources.MSG_DBMS_ADDED" );
  String MSG_DBMS_MERGED            = Messages.getString( "IS5Resources.MSG_DBMS_MERGED" );
  String MSG_DBMS_REMOVED           = Messages.getString( "IS5Resources.MSG_DBMS_REMOVED" );
  String E_SUP_D_UNION_INTERVAL     = Messages.getString( "IS5Resources.E_SUP_D_UNION_INTERVAL" );
  String E_SUP_N_UNION_INTERVAL     = Messages.getString( "IS5Resources.E_SUP_N_UNION_INTERVAL" );
  String E_SUP_D_VALID_INTERVAL     = Messages.getString( "IS5Resources.E_SUP_D_VALID_INTERVAL" );
  String E_SUP_N_VALID_INTERVAL     = Messages.getString( "IS5Resources.E_SUP_N_VALID_INTERVAL" );

  String MSG_SYNC_ADD_NULLS = Messages.getString( "IS5Resources.MSG_SYNC_ADD_NULLS" );

  String MSG_LAST_BLOCK_LOAD_START      = Messages.getString( "IS5Resources.MSG_LAST_BLOCK_LOAD_START" );
  String MSG_BLOCKS_RECEVIED            = Messages.getString( "IS5Resources.MSG_BLOCKS_RECEVIED" );
  String MSG_BLOCKS_UNIONED             = Messages.getString( "IS5Resources.MSG_BLOCKS_UNIONED" );
  String MSG_LAST_BLOCK_LOADED          = Messages.getString( "IS5Resources.MSG_LAST_BLOCK_LOADED" );
  String MSG_LAST_BLOCK_NOT_FOUND       = Messages.getString( "IS5Resources.MSG_LAST_BLOCK_NOT_FOUND" );
  String MSG_LAST_BLOCK_LOAD_FINISH     = Messages.getString( "IS5Resources.MSG_LAST_BLOCK_LOAD_FINISH" );
  String MSG_LAST_BLOCK                 = Messages.getString( "IS5Resources.MSG_LAST_BLOCK" );
  String MSG_NOT_LAST_BLOCK             = Messages.getString( "IS5Resources.MSG_NOT_LAST_BLOCK" );
  String MSG_LOOKUP_LAST_BLOCK          = Messages.getString( "IS5Resources.MSG_LOOKUP_LAST_BLOCK" );
  String MSG_USE_LAST_BLOCK             = Messages.getString( "IS5Resources.MSG_USE_LAST_BLOCK" );
  String MSG_ADD_NEW_BLOCKS             = Messages.getString( "IS5Resources.MSG_ADD_NEW_BLOCKS" );
  String MSG_DETECT_CHANGES_LAST_VALUES = Messages.getString( "IS5Resources.MSG_DETECT_CHANGES_LAST_VALUES" );
  String MSG_REMOVE_REPEAT_VALUES       = Messages.getString( "IS5Resources.MSG_REMOVE_REPEAT_VALUES" );
  String MSG_CANT_USE_LAST_BLOCK        = Messages.getString( "IS5Resources.MSG_CANT_USE_LAST_BLOCK" );
  String MSG_CANT_USE_TX_BLOCK          = Messages.getString( "IS5Resources.MSG_CANT_USE_TX_BLOCK" );
  String MSG_REMOVE_LAST_BY_BEFORE      = Messages.getString( "IS5Resources.MSG_REMOVE_LAST_BY_BEFORE" );
  String MSG_REMOVE_LAST_BY_UNION       = Messages.getString( "IS5Resources.MSG_REMOVE_LAST_BY_UNION" );
  String MSG_REMOVE_LAST_BY_REMOVE      = "%s. Завершение удаления значений данного. Удаление последнего блока";
  String MSG_TX_COMMIT                  = Messages.getString( "IS5Resources.MSG_TX_COMMIT" );
  String MSG_TX_ROLLBACK                = Messages.getString( "IS5Resources.MSG_TX_ROLLBACK" );

  String MSG_EMPTY_BLOCK                 = Messages.getString( "IS5Resources.MSG_EMPTY_BLOCK" );
  String MSG_EMPTY_SEQUENCE              = Messages.getString( "IS5Resources.MSG_EMPTY_SEQUENCE" );
  String MSG_DIFFERENT_BY_DATA_ID        = Messages.getString( "IS5Resources.MSG_DIFFERENT_BY_DATA_ID" );
  String MSG_START_SEQUENCE_OUT_OF_BLOCK = Messages.getString( "IS5Resources.MSG_START_SEQUENCE_OUT_OF_BLOCK" );
  String MSG_AT_SEQUENCE_NOT_FOUND_VALUE = Messages.getString( "IS5Resources.MSG_AT_SEQUENCE_NOT_FOUND_VALUE" );
  String MSG_DIFFERENT_BY_TIME           = Messages.getString( "IS5Resources.MSG_DIFFERENT_BY_TIME" );
  String MSG_DIFFERENT_BY_VALUE          = Messages.getString( "IS5Resources.MSG_DIFFERENT_BY_VALUE" );

  String MSG_INFO                    = Messages.getString( "IS5Resources.MSG_INFO" );
  String MSG_INFO_COUNT              = Messages.getString( "IS5Resources.MSG_INFO_COUNT" );
  String MSG_READ_BLOCK_BEFORE_SQL   = Messages.getString( "IS5Resources.MSG_READ_BLOCK_BEFORE_SQL" );
  String MSG_READ_BLOCK_BEFORE_START = Messages.getString( "IS5Resources.MSG_READ_BLOCK_BEFORE_START" );
  String MSG_READ_BLOCK_BEFORE_END   = Messages.getString( "IS5Resources.MSG_READ_BLOCK_BEFORE_END" );
  String MSG_READ_BLOCK_SQL          = Messages.getString( "IS5Resources.MSG_READ_BLOCK_SQL" );

  String MSG_READ_BLOCK_SQL_SIZE    = "SQL size = %d chars";
  String MSG_READ_BLOCK_START       = Messages.getString( "IS5Resources.MSG_READ_BLOCK_START" );
  String MSG_READ_BLOCK_END         = Messages.getString( "IS5Resources.MSG_READ_BLOCK_END" );
  String MSG_READ_BLOCK_AFTER_SQL   = Messages.getString( "IS5Resources.MSG_READ_BLOCK_AFTER_SQL" );
  String MSG_READ_BLOCK_AFTER_START = Messages.getString( "IS5Resources.MSG_READ_BLOCK_AFTER_START" );
  String MSG_READ_BLOCK_AFTER_END   = Messages.getString( "IS5Resources.MSG_READ_BLOCK_AFTER_END" );

  // <<<<<<< HEAD
  String MSG_FIND_TIME_BEFORE_SQL    = Messages.getString( "IS5Resources.MSG_FIND_TIME_BEFORE_SQL" );
  String MSG_LOAD_BLOCK_CLASS_START  = Messages.getString( "IS5Resources.MSG_LOAD_BLOCK_CLASS_START" );
  String MSG_LOAD_BLOCK_CLASS_FINISH = Messages.getString( "IS5Resources.MSG_LOAD_BLOCK_CLASS_FINISH" );
  String MSG_LOAD_BLOCK_CLASS        = Messages.getString( "IS5Resources.MSG_LOAD_BLOCK_CLASS" );
  String MSG_TIMER_EVENT_START       = Messages.getString( "IS5Resources.MSG_TIMER_EVENT_START" );
  String MSG_TIMER_EVENT_FINISH      = Messages.getString( "IS5Resources.MSG_TIMER_EVENT_FINISH" );

  String MSG_GWID_FRAGMENT_COUNT = "prepareAuto(...): gwid = %s, lookupCount = %d, fragmentCount = %d";
  // =======
  // String MSG_FIND_TIME_BEFORE_SQL = "findTimeBefore(%s, time = %s), сформирован HSQL-запрос:\n%s ";
  // String MSG_LOAD_BLOCK_CLASS_START =
  // "%s. Начало загрузки классов реализаций блоков значений данных. Количество данных = %d.";
  // String MSG_LOAD_BLOCK_CLASS_FINISH =
  // "%s. Завершение загрузки классов реализаций блоков значений данных. Количество данных = %d. Время загрузки = %d
  // (мсек)";
  // String MSG_LOAD_BLOCK_CLASS = "%s. Загрузка класса реализации блока значений данных %s. Класс %s";
  // String MSG_TIMER_EVENT_START = "Запускается обработка события таймера: %s";
  // String MSG_TIMER_EVENT_FINISH = "Завершена обработка события таймера: %s";
  // >>>>>>> refs/heads/2020-11-15_no_localization

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_WRONG_SIZE                    = Messages.getString( "IS5Resources.ERR_WRONG_SIZE" );
  String ERR_QUERY_NOT_COMPLETED           = Messages.getString( "IS5Resources.ERR_QUERY_NOT_COMPLETED" );
  String ERR_CREATE_INTERVAL               = Messages.getString( "IS5Resources.ERR_CREATE_INTERVAL" );
  String ERR_NOT_ARRAY                     = Messages.getString( "IS5Resources.ERR_NOT_ARRAY" );
  String ERR_SIZE_OVER                     = Messages.getString( "IS5Resources.ERR_SIZE_OVER" );
  String ERR_SRC_NOT_ARRAY                 = Messages.getString( "IS5Resources.ERR_SRC_NOT_ARRAY" );
  String ERR_DEST_NOT_ARRAY                = Messages.getString( "IS5Resources.ERR_DEST_NOT_ARRAY" );
  String ERR_NOT_EQUALS_ARRAY_TYPES        = Messages.getString( "IS5Resources.ERR_NOT_EQUALS_ARRAY_TYPES" );
  String ERR_ARRAY_COPY                    = Messages.getString( "IS5Resources.ERR_ARRAY_COPY" );
  String ERR_NOT_START_TIME                = Messages.getString( "IS5Resources.ERR_NOT_START_TIME" );
  String ERR_NOT_END_TIME                  = Messages.getString( "IS5Resources.ERR_NOT_END_TIME" );
  String ERR_WRONG_TYPE                    = Messages.getString( "IS5Resources.ERR_WRONG_TYPE" );
  String ERR_SYNC_DDT                      = Messages.getString( "IS5Resources.ERR_SYNC_DDT" );
  String ERR_NOT_FOUND_VALUE_IMPL          = Messages.getString( "IS5Resources.ERR_NOT_FOUND_VALUE_IMPL" );
  String ERR_SYNC_WRONG_SIZE               = Messages.getString( "IS5Resources.ERR_SYNC_WRONG_SIZE" );
  String ERR_SYNC_WRONG_DDT                = Messages.getString( "IS5Resources.ERR_SYNC_WRONG_DDT" );
  String ERR_SYNC_WRONG_ALIGN              = Messages.getString( "IS5Resources.ERR_SYNC_WRONG_ALIGN" );
  String ERR_SYNC_WRONG_SEQUENCE           = Messages.getString( "IS5Resources.ERR_SYNC_WRONG_SEQUENCE" );
  String ERR_ASYNC_WRONG_TIMESTAMP_INDEX   = Messages.getString( "IS5Resources.ERR_ASYNC_WRONG_TIMESTAMP_INDEX" );
  String ERR_SYNC_WRONG_TIMESTAMP_INDEX    = Messages.getString( "IS5Resources.ERR_SYNC_WRONG_TIMESTAMP_INDEX" );
  String ERR_ASYNC_WRONG_SEQUENCE          = Messages.getString( "IS5Resources.ERR_ASYNC_WRONG_SEQUENCE" );
  String ERR_SYNC_OUT                      = Messages.getString( "IS5Resources.ERR_SYNC_OUT" );
  String ERR_ASYNC_OUT                     = Messages.getString( "IS5Resources.ERR_ASYNC_OUT" );
  String ERR_WRONG_CURSOR                  = Messages.getString( "IS5Resources.ERR_WRONG_CURSOR" );
  String ERR_NOT_CURSOR_DATA               = Messages.getString( "IS5Resources.ERR_NOT_CURSOR_DATA" );
  String ERR_ASYNC_WRONG_INDEX             = Messages.getString( "IS5Resources.ERR_ASYNC_WRONG_INDEX" );
  String ERR_ASYNC_WRONG_TIMES_ORDER       = Messages.getString( "IS5Resources.ERR_ASYNC_WRONG_TIMES_ORDER" );
  String ERR_CANT_EDIT_START_TIME          = Messages.getString( "IS5Resources.ERR_CANT_EDIT_START_TIME" );
  String ERR_SEQUENCE_OUT                  = Messages.getString( "IS5Resources.ERR_SEQUENCE_OUT" );
  String ERR_BLOCK_OUT                     = Messages.getString( "IS5Resources.ERR_BLOCK_OUT" );
  String ERR_WRONG_SYNC_INTERVAL           = Messages.getString( "IS5Resources.ERR_WRONG_SYNC_INTERVAL" );
  String ERR_WRONG_SUBSET                  = Messages.getString( "IS5Resources.ERR_WRONG_SUBSET" );
  String ERR_WRONG_SUBBLOCK                = Messages.getString( "IS5Resources.ERR_WRONG_SUBBLOCK" );
  String ERR_CANT_ADD_PREV                 = Messages.getString( "IS5Resources.ERR_CANT_ADD_PREV" );
  String ERR_SYNC_INEFFECTIVE              = Messages.getString( "IS5Resources.ERR_SYNC_INEFFECTIVE" );
  String ERR_UNION_BLOCKS_UNEXPECTED       = Messages.getString( "IS5Resources.ERR_UNION_BLOCKS_UNEXPECTED" );
  String ERR_SYNC_SEQUENCE_BREAK           = Messages.getString( "IS5Resources.ERR_SYNC_SEQUENCE_BREAK" );
  String ERR_SYNC_DELTA_DIFFERENT          = Messages.getString( "IS5Resources.ERR_SYNC_DELTA_DIFFERENT" );
  String ERR_CANT_ADD_NEXT                 = Messages.getString( "IS5Resources.ERR_CANT_ADD_NEXT" );
  String ERR_WRONG_QUERY                   = Messages.getString( "IS5Resources.ERR_WRONG_QUERY" );
  String ERR_OUT_VALUE_SEQENCE             = Messages.getString( "IS5Resources.ERR_OUT_VALUE_SEQENCE" );
  String ERR_WRONG_SOURCE                  = Messages.getString( "IS5Resources.ERR_WRONG_SOURCE" );
  String ERR_INTERNAL_EDIT                 = Messages.getString( "IS5Resources.ERR_INTERNAL_EDIT" );
  String ERR_CREATE_BLOCK_FROM_CURSOR      = Messages.getString( "IS5Resources.ERR_CREATE_BLOCK_FROM_CURSOR" );
  String ERR_CREATE_BLOB_FROM_CURSOR       = Messages.getString( "IS5Resources.ERR_CREATE_BLOB_FROM_CURSOR" );
  String ERR_CREATE_ASYNC_BLOB_FROM_CURSOR = Messages.getString( "IS5Resources.ERR_CREATE_ASYNC_BLOB_FROM_CURSOR" );
  String ERR_CREATE_BLOCK                  = Messages.getString( "IS5Resources.ERR_CREATE_BLOCK" );
  String ERR_INTERNAL_CREATE_BLOCK         = Messages.getString( "IS5Resources.ERR_INTERNAL_CREATE_BLOCK" );
  String ERR_WRONG_SPLIT                   = Messages.getString( "IS5Resources.ERR_WRONG_SPLIT" );
  String ERR_WRONG_SPLIT_BLOCK             = Messages.getString( "IS5Resources.ERR_WRONG_SPLIT_BLOCK" );
  String ERR_BLOCK_SPLIT                   = Messages.getString( "IS5Resources.ERR_BLOCK_SPLIT" );
  String ERR_ALIEN_BLOCK                   = Messages.getString( "IS5Resources.ERR_ALIEN_BLOCK" );
  String ERR_WRONG_VALUES_TYPE             = Messages.getString( "IS5Resources.ERR_WRONG_VALUES_TYPE" );
  String ERR_COPY_HEAD_ARRAY               = Messages.getString( "IS5Resources.ERR_COPY_HEAD_ARRAY" );
  String ERR_COPY_TAIL_ARRAY               = Messages.getString( "IS5Resources.ERR_COPY_TAIL_ARRAY" );
  String ERR_COPY_ARRAY                    = Messages.getString( "IS5Resources.ERR_COPY_ARRAY" );

  String ERR_WRONG_OBJ                         = Messages.getString( "IS5Resources.ERR_WRONG_OBJ" );
  String ERR_WRONG_VALUE_TYPE                  = Messages.getString( "IS5Resources.ERR_WRONG_VALUE_TYPE" );
  String ERR_WRONG_SYNC_DD                     = Messages.getString( "IS5Resources.ERR_WRONG_SYNC_DD" );
  String ERR_WRONG_ASYNC_DD                    = Messages.getString( "IS5Resources.ERR_WRONG_ASYNC_DD" );
  String ERR_WRONG_DD                          = Messages.getString( "IS5Resources.ERR_WRONG_DD" );
  String ERR_OBJ_DATAID_NOT_FOUND              = Messages.getString( "IS5Resources.ERR_OBJ_DATAID_NOT_FOUND" );
  String ERR_ASYNC_WRITE_UNXEXPECTED           = Messages.getString( "IS5Resources.ERR_ASYNC_WRITE_UNXEXPECTED" );
  String ERR_SEQUENCE_BLOCK_ENCLOSED_READ      = Messages.getString( "IS5Resources.ERR_SEQUENCE_BLOCK_ENCLOSED_READ" );
  String ERR_SEQUENCE_FIND_FRAGMENTATION       = Messages.getString( "IS5Resources.ERR_SEQUENCE_FIND_FRAGMENTATION" );
  String ERR_SEQUENCE_BLOCK_CROSSED_READ       = Messages.getString( "IS5Resources.ERR_SEQUENCE_BLOCK_CROSSED_READ" );
  String ERR_SEQUENCE_BLOCK_SIZES_READ         = Messages.getString( "IS5Resources.ERR_SEQUENCE_BLOCK_SIZES_READ" );
  String ERR_SEQUENCE_CROSSED_BLOCK_SIZES_READ =
      Messages.getString( "IS5Resources.ERR_SEQUENCE_CROSSED_BLOCK_SIZES_READ" );
  String ERR_READ_OUT_OF_MEMORY                = Messages.getString( "IS5Resources.ERR_READ_OUT_OF_MEMORY" );
  String ERR_READ_OUT_OF_MEMORY2               = "%s. Недостаточно памяти для чтения блоков. Причина: %s";
  String ERR_READ_UNEXPECTED                   = Messages.getString( "IS5Resources.ERR_READ_UNEXPECTED" );
  String ERR_IMPL_BLOCK_NOT_FOUND              = Messages.getString( "IS5Resources.ERR_IMPL_BLOCK_NOT_FOUND" );
  String ERR_READ_LAST_SEQUENCE_UNEXPECTED     = Messages.getString( "IS5Resources.ERR_READ_LAST_SEQUENCE_UNEXPECTED" );
  String ERR_AUTO_CANT_UNION_PASS              = Messages.getString( "IS5Resources.ERR_AUTO_CANT_UNION_PASS" );
  String ERR_AUTO_UNION_PASS_RETRY             = Messages.getString( "IS5Resources.ERR_AUTO_UNION_PASS_RETRY" );
  String ERR_UNION_PASS_MAX                    = Messages.getString( "IS5Resources.ERR_UNION_PASS_MAX" );
  String ERR_UNION_NOT_ENOUGH_MEMORY           = Messages.getString( "IS5Resources.ERR_UNION_NOT_ENOUGH_MEMORY" );
  String ERR_VALIDATION_NOT_ENOUGH_MEMORY      = Messages.getString( "IS5Resources.ERR_VALIDATION_NOT_ENOUGH_MEMORY" );
  String ERR_UNION_UNEXPECTED                  = Messages.getString( "IS5Resources.ERR_UNION_UNEXPECTED" );
  String ERR_CREATE_INFO_UNEXPECTED            = Messages.getString( "IS5Resources.ERR_CREATE_INFO_UNEXPECTED" );
  String ERR_REMOVE_INFO                       = Messages.getString( "IS5Resources.ERR_REMOVE_INFO" );
  String ERR_REMOVE_INFO_OBJID                 = Messages.getString( "IS5Resources.ERR_REMOVE_INFO_OBJID" );
  String ERR_REMOVE_BLOCK                      = Messages.getString( "IS5Resources.ERR_REMOVE_BLOCK" );
  String ERR_REMOVE_BLOCK_OBJID                = Messages.getString( "IS5Resources.ERR_REMOVE_BLOCK_OBJID" );
  String ERR_REMOVE_BLOCK_DATAID               = Messages.getString( "IS5Resources.ERR_REMOVE_BLOCK_DATAID" );
  String ERR_REMOVE_BLOCK_OBJID_DATAID         = Messages.getString( "IS5Resources.ERR_REMOVE_BLOCK_OBJID_DATAID" );
  String ERR_QUERY_DOJOB                       = Messages.getString( "IS5Resources.ERR_QUERY_DOJOB" );

  String MSG_ADD_PARTITION     = "[%s] %s.%s. Adding table partition %s.";
  String MSG_ADD_PARTITION_SQL =
      "addPartition(aSchema=%s, aTable=%s, aInfo=%s), creating=%s. SQL-query created:\n   %s";
  String MSG_REMOVE_PARTITION  = "[%s] %s.%s. Removing table partition %s.";

  String ERR_PARTITION_OP                   = "[%s] unexpected partition operation error: %s. Cause: %s";
  String ERR_REPLAIN_PARTITION_OPS_BY_ERROR =
      "[%s] Errors occurred while processing partitions. Scheduling reprocessing";
  String ERR_ADD_PARTITION                  = "[%s] %s.%s. Error adding table partition %s. Cause: %s";
  String ERR_ADD_PARTITION2                 = "%s.%s. Error adding table partition %s. Cause: %s";
  String ERR_DROP_PARTITION                 = "%[%s] %s.%s. Error removing table partition %s. Cause: %s";
  String ERR_DROP_PARTITION2                = "%s.%s. Error removing table partition %s. Cause: %s";

  String ERR_DATAID_NOT_FOUND = Messages.getString( "IS5Resources.ERR_DATAID_NOT_FOUND" );

  String ERR_QUERY_EDIT_FINALIZED   = Messages.getString( "IS5Resources.ERR_QUERY_EDIT_FINALIZED" );
  String ERR_QUERY_INFOES_UNDEF     = Messages.getString( "IS5Resources.ERR_QUERY_INFOES_UNDEF" );
  String ERR_QUERY_WRONG_INTERVAL   = Messages.getString( "IS5Resources.ERR_QUERY_WRONG_INTERVAL" );
  String ERR_QUERY_READER_UNDEF     = Messages.getString( "IS5Resources.ERR_QUERY_READER_UNDEF" );
  String ERR_QUERY_CALLBACK_UNDEF   = Messages.getString( "IS5Resources.ERR_QUERY_CALLBACK_UNDEF" );
  String ERR_QUERY_DELTA_TIME_UNDEF = Messages.getString( "IS5Resources.ERR_QUERY_DELTA_TIME_UNDEF" );

  String ERR_WRONG_START_TIMEOUT = Messages.getString( "IS5Resources.ERR_WRONG_START_TIMEOUT" );
  String ERR_WRONG_DOJOB_TIMEOUT = Messages.getString( "IS5Resources.ERR_WRONG_DOJOB_TIMEOUT" );

  String ERR_CANT_CHANGE_INFO_NO_EMPTY = Messages.getString( "IS5Resources.ERR_CANT_CHANGE_INFO_NO_EMPTY" );
  String ERR_WRITE_TASK                = Messages.getString( "IS5Resources.ERR_WRITE_TASK" );
  String ERR_ASYNC_UNION_TASK          = Messages.getString( "IS5Resources.ERR_ASYNC_UNION_TASK" );
  String ERR_ASYNC_UNION_THREAD        = Messages.getString( "IS5Resources.ERR_ASYNC_UNION_THREAD" );
  String ERR_ASYNC_UNION_THREAD_BUSY   = "%s. Ошибка доступа к %s проводящему дефрагментацию. Причина: %s";
  String ERR_ASYNC_REMOVE_TASK         = Messages.getString( "IS5Resources.ERR_ASYNC_REMOVE_TASK" );
  String ERR_ASYNC_REMOVE_THREAD       = Messages.getString( "IS5Resources.ERR_ASYNC_REMOVE_THREAD" );
  String ERR_ASYNC_REMOVE_THREAD_BUSY  = "%s. Ошибка доступа к %s проводящему удаление. Причина: %s";
  String ERR_ASYNC_VALIDATION_TASK     = Messages.getString( "IS5Resources.ERR_ASYNC_VALIDATION_TASK" );
  String ERR_ASYNC_LAST_VALUES_TASK    = Messages.getString( "IS5Resources.ERR_ASYNC_LAST_VALUES_TASK" );
  String ERR_VALIDATION_MERGE          = Messages.getString( "IS5Resources.ERR_VALIDATION_MERGE" );

  String ERR_VALIDATION_NULL_BLOB        = Messages.getString( "IS5Resources.ERR_VALIDATION_NULL_BLOB" );
  String ERR_VALIDATION_NULL_VALUES      = Messages.getString( "IS5Resources.ERR_VALIDATION_NULL_VALUES" );
  String ERR_VALIDATION_WRONG_SIZE       = Messages.getString( "IS5Resources.ERR_VALIDATION_WRONG_SIZE" );
  String ERR_VALIDATION_ZERO_SIZE        = Messages.getString( "IS5Resources.ERR_VALIDATION_ZERO_SIZE" );
  String ERR_VALIDATION_WRONG_INFOID     = Messages.getString( "IS5Resources.ERR_VALIDATION_WRONG_INFOID" );
  String ERR_VALIDATION_WRONG_INTERVAL   = Messages.getString( "IS5Resources.ERR_VALIDATION_WRONG_INTERVAL" );
  String ERR_WRONG_TIMESTAMPS_SIZE       = Messages.getString( "IS5Resources.ERR_WRONG_TIMESTAMPS_SIZE" );
  String ERR_OLD_TIMESTAMPS_STORAGE      = Messages.getString( "IS5Resources.ERR_OLD_TIMESTAMPS_STORAGE" );
  String ERR_VALIDATION_SYNC_WRONG_SIZE  = Messages.getString( "IS5Resources.ERR_VALIDATION_SYNC_WRONG_SIZE" );
  String ERR_VALIDATION_WRONG_TIMESTAMPS = Messages.getString( "IS5Resources.ERR_VALIDATION_WRONG_TIMESTAMPS" );
  String ERR_VALIDATION_CHECK_TIMESTAMPS = Messages.getString( "IS5Resources.ERR_VALIDATION_CHECK_TIMESTAMPS" );
  String ERR_WRONG_STARTTIME             = Messages.getString( "IS5Resources.ERR_WRONG_STARTTIME" );
  String ERR_RESTORE_TIMESTAMPS          = Messages.getString( "IS5Resources.ERR_RESTORE_TIMESTAMPS" );
  String ERR_RESTORE_TIMESTAMPS_SUCCESS  = Messages.getString( "IS5Resources.ERR_RESTORE_TIMESTAMPS_SUCCESS" );
  String ERR_NULL_TIMESTAMPS             = Messages.getString( "IS5Resources.ERR_NULL_TIMESTAMPS" );

  String ERR_CLEAR_BY_CHANGE_INFO_TYPE = Messages.getString( "IS5Resources.ERR_CLEAR_BY_CHANGE_INFO_TYPE" );

  String ERR_NOT_USER_TX = Messages.getString( "IS5Resources.ERR_NOT_USER_TX" );

  String ERR_NOT_OPEN_TX = Messages.getString( "IS5Resources.ERR_NOT_OPEN_TX" );

  String MSG_SPLIT_BLOCK = Messages.getString( "IS5Resources.MSG_SPLIT_BLOCK" );

  String ERR_WRONG_FIRST_LAST_ORDER = Messages.getString( "IS5Resources.ERR_WRONG_FIRST_LAST_ORDER" );
  String ERR_WRONG_ORDER            = Messages.getString( "IS5Resources.ERR_WRONG_ORDER" );

  String ERR_WRITE_DISABLE_BY_LOAD_AVERAGE     = Messages.getString( "IS5Resources.ERR_WRITE_DISABLE_BY_LOAD_AVERAGE" );
  String ERR_UNION_DISABLE_BY_LOAD_AVERAGE     = Messages.getString( "IS5Resources.ERR_UNION_DISABLE_BY_LOAD_AVERAGE" );
  String ERR_UNION_DISABLE_BY_PREV_UNITER      = Messages.getString( "IS5Resources.ERR_UNION_DISABLE_BY_PREV_UNITER" );
  String ERR_PARTITION_DISABLE_BY_LOAD_AVERAGE =
      Messages.getString( "IS5Resources.ERR_PARTITION_DISABLE_BY_LOAD_AVERAGE" );
  String ERR_PARTITION_DISABLE_BY_PREV         = Messages.getString( "IS5Resources.ERR_PARTITION_DISABLE_BY_PREV" );
  String ERR_REJECT_HANDLE                     = "%s. %s query rejected. %s is in progress.";
  String ERR_PARTITION_NOT_INIT                =
      "%s. Отклонен запрос на выполнение обработки разделов таблиц. Автор %s. Причина: Инициализация SequenceWriter";
  String ERR_PARTITION_PLAN_CREATED_ALREADY    = "%s. %s: раздел уже запланирован для добавления (aCreatingPartitions)";
  String ERR_PARTITION_PLAN_ADDED_ALREADY      = "%s. %s: раздел уже запланирован для добавления (aAddedPartitions)";
  String ERR_STATISTICS_DISABLED               = Messages.getString( "IS5Resources.ERR_STATISTICS_DISABLED" );
  String ERR_STATISTICS_NOT_READY              =
      // <<<<<<< HEAD
      Messages.getString( "IS5Resources.ERR_STATISTICS_NOT_READY" );
  String ERR_SEQUENCE_IMPL_NOT_FOUND           = Messages.getString( "IS5Resources.ERR_SEQUENCE_IMPL_NOT_FOUND" );
  String ERR_REMOTE_ACCESS                     = Messages.getString( "IS5Resources.ERR_REMOTE_ACCESS" );
  String ERR_QUERY_IS_ALREADY_EXECUTE          = Messages.getString( "IS5Resources.ERR_QUERY_IS_ALREADY_EXECUTE" );
  // ======="%s. Объект статистики %s [%s] не готов для использования. Формирование статистики записи хранимых данных
  // отключено";
  // String ERR_SEQUENCE_IMPL_NOT_FOUND = "Не найден класс реализации хранения последовательности значений '%s'";
  // String ERR_REMOTE_ACCESS = "Ошибка получения удаленного доступа к данным на узле %s. Данные: %s";
  // String ERR_QUERY_IS_ALREADY_EXECUTE = "Запрос %s уже выполняется";
  String ERR_GWID_DOUBLE_ON_READ =
      "readSequences(): gwid = '%s' указан в списке запроса несколько раз. Это может снижать эффективность обработки запроса";
  // >>>>>>>refs/heads/2020-11-15_no_localization

  String ERR_WRONG_IMPORT_CURSOR    = Messages.getString( "IS5Resources.ERR_WRONG_IMPORT_CURSOR" );
  String ERR_NOT_CURSOR_IMPORT_DATA = Messages.getString( "IS5Resources.ERR_NOT_CURSOR_IMPORT_DATA" );
  String ERR_NOT_IMPORT_DATA        = Messages.getString( "IS5Resources.ERR_NOT_IMPORT_DATA" );
  String ERR_CAST_VALUE             = Messages.getString( "IS5Resources.ERR_CAST_VALUE" );

  String ERR_UNEXPECT_DOJOB_ERROR = "unexpected doJob error. cause = %s";

  String ERR_TABLE_NOT_EXSIST = "Table %s is not exsist";

  // THREADS
  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_READ_SEQUENCES_FINISH = Messages.getString( "IS5Resources.MSG_READ_SEQUENCES_FINISH" );
  String MSG_WRITE_THREAD_FINISH   = Messages.getString( "IS5Resources.MSG_WRITE_THREAD_FINISH" );
  String MSG_WAIT_LOCK_GWID        = Messages.getString( "IS5Resources.MSG_WAIT_LOCK_GWID" );
  String MSG_WAIT_LOCK_INFOES      = Messages.getString( "IS5Resources.MSG_WAIT_LOCK_INFOES" );

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_SEQUENCE_BEFORE_OUT_OF_MEMORY = Messages.getString( "IS5Resources.ERR_SEQUENCE_BEFORE_OUT_OF_MEMORY" );
  String ERR_SEQUENCE_READ_OUT_OF_MEMORY   = Messages.getString( "IS5Resources.ERR_SEQUENCE_READ_OUT_OF_MEMORY" );
  String ERR_FIND_VALUE_INDEX              = Messages.getString( "IS5Resources.ERR_FIND_VALUE_INDEX" );
  String ERR_SEQUENCE_READ_UNEXPECTED      = Messages.getString( "IS5Resources.ERR_SEQUENCE_READ_UNEXPECTED" );
  String ERR_SEQUENCE_OUT_OF_MEMORY_ERROR  = Messages.getString( "IS5Resources.ERR_SEQUENCE_OUT_OF_MEMORY_ERROR" );
  String ERR_CONCURRENT_WRITE              = Messages.getString( "IS5Resources.ERR_CONCURRENT_WRITE" );
  String ERR_SEQUENCE_WRITE                = Messages.getString( "IS5Resources.ERR_SEQUENCE_WRITE" );
  String ERR_WRITE_UNEXPECTED              = Messages.getString( "IS5Resources.ERR_WRITE_UNEXPECTED" );
  String ERR_WRITE_FLASH_UNEXPECTED        = "Неожиданная ошибка записи (flash) блоков. Причина: %s";
  String ERR_REMOVE_FLASH_UNEXPECTED       = "Неожиданная ошибка записи (remove) блоков. Причина: %s";
  String ERR_PERSIST_UNEXPECTED            = Messages.getString( "IS5Resources.ERR_PERSIST_UNEXPECTED" );
  String ERR_MERGE_UNEXPECTED              = Messages.getString( "IS5Resources.ERR_MERGE_UNEXPECTED" );
  String ERR_REMOVE_UNEXPECTED             = Messages.getString( "IS5Resources.ERR_REMOVE_UNEXPECTED" );
  String ERR_DETACH_UNEXPECTED             = Messages.getString( "IS5Resources.ERR_DETACH_UNEXPECTED" );
  String ERR_SEQUENCE_WRITE_CONCURRENT     = Messages.getString( "IS5Resources.ERR_SEQUENCE_WRITE_CONCURRENT" );
  String ERR_SEQUENCE_WRITE_UNEXPECTED     = Messages.getString( "IS5Resources.ERR_SEQUENCE_WRITE_UNEXPECTED" );

  String ERR_FORCE_UNLOCK_INFO      = Messages.getString( "IS5Resources.ERR_FORCE_UNLOCK_INFO" );
  String ERR_FORCE_UNLOCK_INFOES    = Messages.getString( "IS5Resources.ERR_FORCE_UNLOCK_INFOES" );
  String ERR_ROLLBACK_GWID          = Messages.getString( "IS5Resources.ERR_ROLLBACK_GWID" );
  String ERR_ROLLBACK_INFOES        = Messages.getString( "IS5Resources.ERR_ROLLBACK_INFOES" );
  String ERR_LOCK_INFO_TIMEOUT      = Messages.getString( "IS5Resources.ERR_LOCK_INFO_TIMEOUT" );
  String ERR_LOCK_INFOES_TIMEOUT    = Messages.getString( "IS5Resources.ERR_LOCK_INFOES_TIMEOUT" );
  String ERR_WRONG_CUTTING_INTERVAL = Messages.getString( "IS5Resources.ERR_WRONG_CUTTING_INTERVAL" );
  String ERR_INIT_BLOCK_IMPL        = Messages.getString( "IS5Resources.ERR_INIT_BLOCK_IMPL" );
  String ERR_SEQUENCE_READONLY      = Messages.getString( "IS5Resources.ERR_SEQUENCE_READONLY" );

  // ------------------------------------------------------------------------------------
  // Тексты ошибок S5SequenceFactory
  //
  String ERR_CREATE_BLOCK_UNEXPECTED    = Messages.getString( "IS5Resources.ERR_CREATE_BLOCK_UNEXPECTED" );
  String ERR_CONSTRUCT_BLOCK_UNEXPECTED = Messages.getString( "IS5Resources.ERR_CONSTRUCT_BLOCK_UNEXPECTED" );
  String ERR_BLOCK_IMPL_NOT_FOUND       = Messages.getString( "IS5Resources.ERR_BLOCK_IMPL_NOT_FOUND" );
  String ERR_CREATE_METHOD_NOT_FOUND    = Messages.getString( "IS5Resources.ERR_CREATE_METHOD_NOT_FOUND" );
  String ERR_METHOD_NOT_FOUND           = Messages.getString( "IS5Resources.ERR_METHOD_NOT_FOUND" );

  String ERR_READ_RTDATAID_JDBC_UNEXPECTED = Messages.getString( "IS5Resources.ERR_READ_RTDATAID_JDBC_UNEXPECTED" );
}
