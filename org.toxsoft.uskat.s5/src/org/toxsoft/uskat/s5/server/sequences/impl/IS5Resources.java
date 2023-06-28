package org.toxsoft.uskat.s5.server.sequences.impl;

/**
 * Константы, локализуемые ресурсы реализации.
 *
 * @author mvk
 */
interface IS5Resources {

  /**
   * Шаблон формирования toString для блоков {@link S5SequenceBlock}
   */
  String BLOCK_TO_STRING_FORMAT = Messages.getString( "IS5Resources.BLOCK_TO_STRING_FORMAT" ); //$NON-NLS-1$

  /**
   * Шаблон формирования toString для blob {@link S5SequenceBlob}
   */
  String BLOB_TO_STRING_FORMAT = Messages.getString( "IS5Resources.BLOB_TO_STRING_FORMAT" ); //$NON-NLS-1$

  /**
   * Шаблон формирования toString для blob {@link S5SequenceBlob}
   */
  String BLOB_ASYNC_TO_STRING_FORMAT = Messages.getString( "IS5Resources.BLOB_ASYNC_TO_STRING_FORMAT" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_ERROR_QUERY                 = Messages.getString( "IS5Resources.MSG_ERROR_QUERY" );                   //$NON-NLS-1$
  String MSG_UNION_START_THREAD          = Messages.getString( "IS5Resources.MSG_UNION_START_THREAD" );            //$NON-NLS-1$
  String MSG_SINGLETON_UNION_TASK_START  = Messages.getString( "IS5Resources.MSG_SINGLETON_UNION_TASK_START" );    //$NON-NLS-1$
  String MSG_SINGLETON_UNION_TASK_FINISH = Messages.getString( "IS5Resources.MSG_SINGLETON_UNION_TASK_FINISH" );   //$NON-NLS-1$
  String MSG_UNION_TASK_FINISH           = Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH" )              //$NON-NLS-1$
      + Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH___1" ) +                                           // //$NON-NLS-1$
      Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH___2" ) +                                             // //$NON-NLS-1$
      Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH___3" ) +                                             // //$NON-NLS-1$
      Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH___4" ) +                                             // //$NON-NLS-1$
      Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH___5" ) +                                             // //$NON-NLS-1$
      Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH___6" ) +                                             // //$NON-NLS-1$
      Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH___7" ) +                                             // //$NON-NLS-1$
      Messages.getString( "IS5Resources.MSG_UNION_TASK_FINISH___8" );                                              //$NON-NLS-1$
  String MSG_UNION_START                 = Messages.getString( "IS5Resources.MSG_UNION_START" );                   //$NON-NLS-1$
  String MSG_UNION_FINISH                = Messages.getString( "IS5Resources.MSG_UNION_FINISH" );                  //$NON-NLS-1$
  String MSG_UNION_PASS_FINISH           = Messages.getString( "IS5Resources.MSG_UNION_PASS_FINISH" );             //$NON-NLS-1$
  String MSG_VALIDATION_UPDATE           = Messages.getString( "IS5Resources.MSG_VALIDATION_UPDATE" );             //$NON-NLS-1$
  String MSG_VALIDATION_REMOVE           = Messages.getString( "IS5Resources.MSG_VALIDATION_REMOVE" );             //$NON-NLS-1$
  String MSG_VALIDATION_REMOVE_UNORDER   = Messages.getString( "IS5Resources.MSG_VALIDATION_REMOVE_UNORDER" );     //$NON-NLS-1$
  String MSG_VALIDATION_FINISH           = Messages.getString( "IS5Resources.MSG_VALIDATION_FINISH" );             //$NON-NLS-1$
  String MSG_CREATE_QUERY                = Messages.getString( "IS5Resources.MSG_CREATE_QUERY" );                  //$NON-NLS-1$
  String MSG_EXECUTE_SUB_QUERY           = Messages.getString( "IS5Resources.MSG_EXECUTE_SUB_QUERY" );             //$NON-NLS-1$
  String MSG_FINISH_QUERY                = Messages.getString( "IS5Resources.MSG_FINISH_QUERY" );                  //$NON-NLS-1$
  String MSG_START_EXECUTE_QUERY         = Messages.getString( "IS5Resources.MSG_START_EXECUTE_QUERY" );           //$NON-NLS-1$
  String MSG_FINISH_EXECUTE_QUERY        = Messages.getString( "IS5Resources.MSG_FINISH_EXECUTE_QUERY" );          //$NON-NLS-1$
  String MSG_READ_BEFORE_SEQUENCE_TIME   = Messages.getString( "IS5Resources.MSG_READ_BEFORE_SEQUENCE_TIME" );     //$NON-NLS-1$
  String MSG_READ_SEQUENCE_TIME          = Messages.getString( "IS5Resources.MSG_READ_SEQUENCE_TIME" );            //$NON-NLS-1$
  String MSG_WRITE_SEQUENCE_TIME         = Messages.getString( "IS5Resources.MSG_WRITE_SEQUENCE_TIME" );           //$NON-NLS-1$
  String MSG_FIND_START_TIME             = Messages.getString( "IS5Resources.MSG_FIND_START_TIME" );               //$NON-NLS-1$
  String MSG_FIND_END_TIME               = Messages.getString( "IS5Resources.MSG_FIND_END_TIME" );                 //$NON-NLS-1$
  String MSG_CALC_AVAILABLE_BLOCK        = Messages.getString( "IS5Resources.MSG_CALC_AVAILABLE_BLOCK" );          //$NON-NLS-1$
  String MSG_CREATE_UNION_TIMER          = Messages.getString( "IS5Resources.MSG_CREATE_UNION_TIMER" );            //$NON-NLS-1$
  String MSG_CANCEL_UNION_TIMER          = Messages.getString( "IS5Resources.MSG_CANCEL_UNION_TIMER" );            //$NON-NLS-1$
  String MSG_AUTO_VALIDATION_AUTHOR      = Messages.getString( "IS5Resources.MSG_AUTO_VALIDATION_AUTHOR" );        //$NON-NLS-1$
  String MSG_UNION_AUTHOR_INIT           = Messages.getString( "IS5Resources.MSG_UNION_AUTHOR_INIT" );             //$NON-NLS-1$
  String MSG_UNION_AUTHOR_SCHEDULE       = Messages.getString( "IS5Resources.MSG_UNION_AUTHOR_SCHEDULE" );         //$NON-NLS-1$
  String MSG_UNION_AUTO_OFF              = Messages.getString( "IS5Resources.MSG_UNION_AUTO_OFF" );                //$NON-NLS-1$
  String MSG_UNION_AUTO_INTERVAL         = Messages.getString( "IS5Resources.MSG_UNION_AUTO_INTERVAL" );           //$NON-NLS-1$
  String MSG_UNION_AUTO_ADD_INFO         = Messages.getString( "IS5Resources.MSG_UNION_AUTO_ADD_INFO" );           //$NON-NLS-1$
  String MSG_UNION_AUTO_REPEAT           = Messages.getString( "IS5Resources.MSG_UNION_AUTO_REPEAT" );             //$NON-NLS-1$

  String MSG_LAST_VALUES_ASYNC_QUERY = Messages.getString( "IS5Resources.MSG_LAST_VALUES_ASYNC_QUERY" ); //$NON-NLS-1$
  String MSG_LAST_VALUE_ASYNC_QUERY  = Messages.getString( "IS5Resources.MSG_LAST_VALUE_ASYNC_QUERY" );  //$NON-NLS-1$

  String MSG_VALIDATION_TASK_START  = Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_START" );                   //$NON-NLS-1$
  String MSG_VALIDATION_TASK_FINISH = Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH" ) +                 // //$NON-NLS-1$
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___1" ) +                                             // //$NON-NLS-1$
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___2" ) +                                             // //$NON-NLS-1$
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___3" ) +                                             // //$NON-NLS-1$
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___4" ) +                                             // //$NON-NLS-1$
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___5" ) +                                             // //$NON-NLS-1$
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___6" ) +                                             // //$NON-NLS-1$
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___7" ) +                                             // //$NON-NLS-1$
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___8" ) +                                             // //$NON-NLS-1$
      Messages.getString( "IS5Resources.MSG_VALIDATION_TASK_FINISH___9" );                                              //$NON-NLS-1$
  String MSG_DBMS_LOADED            = Messages.getString( "IS5Resources.MSG_DBMS_LOADED" );                             //$NON-NLS-1$
  String MSG_DBMS_ADDED             = Messages.getString( "IS5Resources.MSG_DBMS_ADDED" );                              //$NON-NLS-1$
  String MSG_DBMS_MERGED            = Messages.getString( "IS5Resources.MSG_DBMS_MERGED" );                             //$NON-NLS-1$
  String MSG_DBMS_REMOVED           = Messages.getString( "IS5Resources.MSG_DBMS_REMOVED" );                            //$NON-NLS-1$
  String E_SUP_D_UNION_INTERVAL     = Messages.getString( "IS5Resources.E_SUP_D_UNION_INTERVAL" );                      //$NON-NLS-1$
  String E_SUP_N_UNION_INTERVAL     = Messages.getString( "IS5Resources.E_SUP_N_UNION_INTERVAL" );                      //$NON-NLS-1$
  String E_SUP_D_VALID_INTERVAL     = Messages.getString( "IS5Resources.E_SUP_D_VALID_INTERVAL" );                      //$NON-NLS-1$
  String E_SUP_N_VALID_INTERVAL     = Messages.getString( "IS5Resources.E_SUP_N_VALID_INTERVAL" );                      //$NON-NLS-1$

  String MSG_SYNC_ADD_NULLS = Messages.getString( "IS5Resources.MSG_SYNC_ADD_NULLS" ); //$NON-NLS-1$

  String MSG_LAST_BLOCK_LOAD_START      = Messages.getString( "IS5Resources.MSG_LAST_BLOCK_LOAD_START" );      //$NON-NLS-1$
  String MSG_BLOCKS_RECEVIED            = Messages.getString( "IS5Resources.MSG_BLOCKS_RECEVIED" );            //$NON-NLS-1$
  String MSG_BLOCKS_UNIONED             = Messages.getString( "IS5Resources.MSG_BLOCKS_UNIONED" );             //$NON-NLS-1$
  String MSG_LAST_BLOCK_LOADED          = Messages.getString( "IS5Resources.MSG_LAST_BLOCK_LOADED" );          //$NON-NLS-1$
  String MSG_LAST_BLOCK_NOT_FOUND       = Messages.getString( "IS5Resources.MSG_LAST_BLOCK_NOT_FOUND" );       //$NON-NLS-1$
  String MSG_LAST_BLOCK_LOAD_FINISH     = Messages.getString( "IS5Resources.MSG_LAST_BLOCK_LOAD_FINISH" );     //$NON-NLS-1$
  String MSG_LAST_BLOCK                 = Messages.getString( "IS5Resources.MSG_LAST_BLOCK" );                 //$NON-NLS-1$
  String MSG_NOT_LAST_BLOCK             = Messages.getString( "IS5Resources.MSG_NOT_LAST_BLOCK" );             //$NON-NLS-1$
  String MSG_LOOKUP_LAST_BLOCK          = Messages.getString( "IS5Resources.MSG_LOOKUP_LAST_BLOCK" );          //$NON-NLS-1$
  String MSG_USE_LAST_BLOCK             = Messages.getString( "IS5Resources.MSG_USE_LAST_BLOCK" );             //$NON-NLS-1$
  String MSG_ADD_NEW_BLOCKS             = Messages.getString( "IS5Resources.MSG_ADD_NEW_BLOCKS" );             //$NON-NLS-1$
  String MSG_DETECT_CHANGES_LAST_VALUES = Messages.getString( "IS5Resources.MSG_DETECT_CHANGES_LAST_VALUES" ); //$NON-NLS-1$
  String MSG_REMOVE_REPEAT_VALUES       = Messages.getString( "IS5Resources.MSG_REMOVE_REPEAT_VALUES" );       //$NON-NLS-1$
  String MSG_CANT_USE_LAST_BLOCK        = Messages.getString( "IS5Resources.MSG_CANT_USE_LAST_BLOCK" );        //$NON-NLS-1$
  String MSG_CANT_USE_TX_BLOCK          = Messages.getString( "IS5Resources.MSG_CANT_USE_TX_BLOCK" );          //$NON-NLS-1$
  String MSG_REMOVE_LAST_BY_BEFORE      = Messages.getString( "IS5Resources.MSG_REMOVE_LAST_BY_BEFORE" );      //$NON-NLS-1$
  String MSG_REMOVE_LAST_BY_UNION       = Messages.getString( "IS5Resources.MSG_REMOVE_LAST_BY_UNION" );       //$NON-NLS-1$
  String MSG_TX_COMMIT                  = Messages.getString( "IS5Resources.MSG_TX_COMMIT" );                  //$NON-NLS-1$
  String MSG_TX_ROLLBACK                = Messages.getString( "IS5Resources.MSG_TX_ROLLBACK" );                //$NON-NLS-1$

  String MSG_EMPTY_BLOCK                 = Messages.getString( "IS5Resources.MSG_EMPTY_BLOCK" );                 //$NON-NLS-1$
  String MSG_EMPTY_SEQUENCE              = Messages.getString( "IS5Resources.MSG_EMPTY_SEQUENCE" );              //$NON-NLS-1$
  String MSG_DIFFERENT_BY_DATA_ID        = Messages.getString( "IS5Resources.MSG_DIFFERENT_BY_DATA_ID" );        //$NON-NLS-1$
  String MSG_START_SEQUENCE_OUT_OF_BLOCK = Messages.getString( "IS5Resources.MSG_START_SEQUENCE_OUT_OF_BLOCK" ); //$NON-NLS-1$
  String MSG_AT_SEQUENCE_NOT_FOUND_VALUE = Messages.getString( "IS5Resources.MSG_AT_SEQUENCE_NOT_FOUND_VALUE" ); //$NON-NLS-1$
  String MSG_DIFFERENT_BY_TIME           = Messages.getString( "IS5Resources.MSG_DIFFERENT_BY_TIME" );           //$NON-NLS-1$
  String MSG_DIFFERENT_BY_VALUE          = Messages.getString( "IS5Resources.MSG_DIFFERENT_BY_VALUE" );          //$NON-NLS-1$

  String MSG_INFO                    = Messages.getString( "IS5Resources.MSG_INFO" );                    //$NON-NLS-1$
  String MSG_INFO_COUNT              = Messages.getString( "IS5Resources.MSG_INFO_COUNT" );              //$NON-NLS-1$
  String MSG_READ_BLOCK_BEFORE_SQL   = Messages.getString( "IS5Resources.MSG_READ_BLOCK_BEFORE_SQL" );   //$NON-NLS-1$
  String MSG_READ_BLOCK_BEFORE_START = Messages.getString( "IS5Resources.MSG_READ_BLOCK_BEFORE_START" ); //$NON-NLS-1$
  String MSG_READ_BLOCK_BEFORE_END   = Messages.getString( "IS5Resources.MSG_READ_BLOCK_BEFORE_END" );   //$NON-NLS-1$
  String MSG_READ_BLOCK_SQL          = Messages.getString( "IS5Resources.MSG_READ_BLOCK_SQL" );          //$NON-NLS-1$
  String MSG_READ_BLOCK_SQL_SIZE     = "SQL size = %d chars";                                            //$NON-NLS-1$
  String MSG_READ_BLOCK_START        = Messages.getString( "IS5Resources.MSG_READ_BLOCK_START" );        //$NON-NLS-1$
  String MSG_READ_BLOCK_END          = Messages.getString( "IS5Resources.MSG_READ_BLOCK_END" );          //$NON-NLS-1$
  String MSG_READ_BLOCK_AFTER_SQL    = Messages.getString( "IS5Resources.MSG_READ_BLOCK_AFTER_SQL" );    //$NON-NLS-1$
  String MSG_READ_BLOCK_AFTER_START  = Messages.getString( "IS5Resources.MSG_READ_BLOCK_AFTER_START" );  //$NON-NLS-1$
  String MSG_READ_BLOCK_AFTER_END    = Messages.getString( "IS5Resources.MSG_READ_BLOCK_AFTER_END" );    //$NON-NLS-1$

  // <<<<<<< HEAD
  String MSG_FIND_TIME_BEFORE_SQL    = Messages.getString( "IS5Resources.MSG_FIND_TIME_BEFORE_SQL" );    //$NON-NLS-1$
  String MSG_LOAD_BLOCK_CLASS_START  = Messages.getString( "IS5Resources.MSG_LOAD_BLOCK_CLASS_START" );  //$NON-NLS-1$
  String MSG_LOAD_BLOCK_CLASS_FINISH = Messages.getString( "IS5Resources.MSG_LOAD_BLOCK_CLASS_FINISH" ); //$NON-NLS-1$
  String MSG_LOAD_BLOCK_CLASS        = Messages.getString( "IS5Resources.MSG_LOAD_BLOCK_CLASS" );        //$NON-NLS-1$
  String MSG_TIMER_EVENT_START       = Messages.getString( "IS5Resources.MSG_TIMER_EVENT_START" );       //$NON-NLS-1$
  String MSG_TIMER_EVENT_FINISH      = Messages.getString( "IS5Resources.MSG_TIMER_EVENT_FINISH" );      //$NON-NLS-1$

  String MSG_GWID_FRAGMENT_COUNT = "prepareAuto(...): gwid = %s, lookupCount = %d, fragmentCount = %d"; //$NON-NLS-1$
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
  String ERR_WRONG_SIZE                    = Messages.getString( "IS5Resources.ERR_WRONG_SIZE" );                    //$NON-NLS-1$
  String ERR_QUERY_NOT_COMPLETED           = Messages.getString( "IS5Resources.ERR_QUERY_NOT_COMPLETED" );           //$NON-NLS-1$
  String ERR_CREATE_INTERVAL               = Messages.getString( "IS5Resources.ERR_CREATE_INTERVAL" );               //$NON-NLS-1$
  String ERR_NOT_ARRAY                     = Messages.getString( "IS5Resources.ERR_NOT_ARRAY" );                     //$NON-NLS-1$
  String ERR_SIZE_OVER                     = Messages.getString( "IS5Resources.ERR_SIZE_OVER" );                     //$NON-NLS-1$
  String ERR_SRC_NOT_ARRAY                 = Messages.getString( "IS5Resources.ERR_SRC_NOT_ARRAY" );                 //$NON-NLS-1$
  String ERR_DEST_NOT_ARRAY                = Messages.getString( "IS5Resources.ERR_DEST_NOT_ARRAY" );                //$NON-NLS-1$
  String ERR_NOT_EQUALS_ARRAY_TYPES        = Messages.getString( "IS5Resources.ERR_NOT_EQUALS_ARRAY_TYPES" );        //$NON-NLS-1$
  String ERR_ARRAY_COPY                    = Messages.getString( "IS5Resources.ERR_ARRAY_COPY" );                    //$NON-NLS-1$
  String ERR_NOT_START_TIME                = Messages.getString( "IS5Resources.ERR_NOT_START_TIME" );                //$NON-NLS-1$
  String ERR_NOT_END_TIME                  = Messages.getString( "IS5Resources.ERR_NOT_END_TIME" );                  //$NON-NLS-1$
  String ERR_WRONG_TYPE                    = Messages.getString( "IS5Resources.ERR_WRONG_TYPE" );                    //$NON-NLS-1$
  String ERR_SYNC_DDT                      = Messages.getString( "IS5Resources.ERR_SYNC_DDT" );                      //$NON-NLS-1$
  String ERR_NOT_FOUND_VALUE_IMPL          = Messages.getString( "IS5Resources.ERR_NOT_FOUND_VALUE_IMPL" );          //$NON-NLS-1$
  String ERR_SYNC_WRONG_SIZE               = Messages.getString( "IS5Resources.ERR_SYNC_WRONG_SIZE" );               //$NON-NLS-1$
  String ERR_SYNC_WRONG_DDT                = Messages.getString( "IS5Resources.ERR_SYNC_WRONG_DDT" );                //$NON-NLS-1$
  String ERR_SYNC_WRONG_ALIGN              = Messages.getString( "IS5Resources.ERR_SYNC_WRONG_ALIGN" );              //$NON-NLS-1$
  String ERR_SYNC_WRONG_SEQUENCE           = Messages.getString( "IS5Resources.ERR_SYNC_WRONG_SEQUENCE" );           //$NON-NLS-1$
  String ERR_ASYNC_WRONG_TIMESTAMP_INDEX   = Messages.getString( "IS5Resources.ERR_ASYNC_WRONG_TIMESTAMP_INDEX" );   //$NON-NLS-1$
  String ERR_SYNC_WRONG_TIMESTAMP_INDEX    = Messages.getString( "IS5Resources.ERR_SYNC_WRONG_TIMESTAMP_INDEX" );    //$NON-NLS-1$
  String ERR_ASYNC_WRONG_SEQUENCE          = Messages.getString( "IS5Resources.ERR_ASYNC_WRONG_SEQUENCE" );          //$NON-NLS-1$
  String ERR_SYNC_OUT                      = Messages.getString( "IS5Resources.ERR_SYNC_OUT" );                      //$NON-NLS-1$
  String ERR_ASYNC_OUT                     = Messages.getString( "IS5Resources.ERR_ASYNC_OUT" );                     //$NON-NLS-1$
  String ERR_WRONG_CURSOR                  = Messages.getString( "IS5Resources.ERR_WRONG_CURSOR" );                  //$NON-NLS-1$
  String ERR_NOT_CURSOR_DATA               = Messages.getString( "IS5Resources.ERR_NOT_CURSOR_DATA" );               //$NON-NLS-1$
  String ERR_ASYNC_WRONG_INDEX             = Messages.getString( "IS5Resources.ERR_ASYNC_WRONG_INDEX" );             //$NON-NLS-1$
  String ERR_ASYNC_WRONG_TIMES_ORDER       = Messages.getString( "IS5Resources.ERR_ASYNC_WRONG_TIMES_ORDER" );       //$NON-NLS-1$
  String ERR_CANT_EDIT_START_TIME          = Messages.getString( "IS5Resources.ERR_CANT_EDIT_START_TIME" );          //$NON-NLS-1$
  String ERR_SEQUENCE_OUT                  = Messages.getString( "IS5Resources.ERR_SEQUENCE_OUT" );                  //$NON-NLS-1$
  String ERR_BLOCK_OUT                     = Messages.getString( "IS5Resources.ERR_BLOCK_OUT" );                     //$NON-NLS-1$
  String ERR_WRONG_SYNC_INTERVAL           = Messages.getString( "IS5Resources.ERR_WRONG_SYNC_INTERVAL" );           //$NON-NLS-1$
  String ERR_WRONG_SUBSET                  = Messages.getString( "IS5Resources.ERR_WRONG_SUBSET" );                  //$NON-NLS-1$
  String ERR_WRONG_SUBBLOCK                = Messages.getString( "IS5Resources.ERR_WRONG_SUBBLOCK" );                //$NON-NLS-1$
  String ERR_CANT_ADD_PREV                 = Messages.getString( "IS5Resources.ERR_CANT_ADD_PREV" );                 //$NON-NLS-1$
  String ERR_SYNC_INEFFECTIVE              = Messages.getString( "IS5Resources.ERR_SYNC_INEFFECTIVE" );              //$NON-NLS-1$
  String ERR_UNION_BLOCKS_UNEXPECTED       = Messages.getString( "IS5Resources.ERR_UNION_BLOCKS_UNEXPECTED" );       //$NON-NLS-1$
  String ERR_SYNC_SEQUENCE_BREAK           = Messages.getString( "IS5Resources.ERR_SYNC_SEQUENCE_BREAK" );           //$NON-NLS-1$
  String ERR_SYNC_DELTA_DIFFERENT          = Messages.getString( "IS5Resources.ERR_SYNC_DELTA_DIFFERENT" );          //$NON-NLS-1$
  String ERR_CANT_ADD_NEXT                 = Messages.getString( "IS5Resources.ERR_CANT_ADD_NEXT" );                 //$NON-NLS-1$
  String ERR_WRONG_QUERY                   = Messages.getString( "IS5Resources.ERR_WRONG_QUERY" );                   //$NON-NLS-1$
  String ERR_OUT_VALUE_SEQENCE             = Messages.getString( "IS5Resources.ERR_OUT_VALUE_SEQENCE" );             //$NON-NLS-1$
  String ERR_WRONG_SOURCE                  = Messages.getString( "IS5Resources.ERR_WRONG_SOURCE" );                  //$NON-NLS-1$
  String ERR_INTERNAL_EDIT                 = Messages.getString( "IS5Resources.ERR_INTERNAL_EDIT" );                 //$NON-NLS-1$
  String ERR_CREATE_BLOCK_FROM_CURSOR      = Messages.getString( "IS5Resources.ERR_CREATE_BLOCK_FROM_CURSOR" );      //$NON-NLS-1$
  String ERR_CREATE_BLOB_FROM_CURSOR       = Messages.getString( "IS5Resources.ERR_CREATE_BLOB_FROM_CURSOR" );       //$NON-NLS-1$
  String ERR_CREATE_ASYNC_BLOB_FROM_CURSOR = Messages.getString( "IS5Resources.ERR_CREATE_ASYNC_BLOB_FROM_CURSOR" ); //$NON-NLS-1$
  String ERR_CREATE_BLOCK                  = Messages.getString( "IS5Resources.ERR_CREATE_BLOCK" );                  //$NON-NLS-1$
  String ERR_INTERNAL_CREATE_BLOCK         = Messages.getString( "IS5Resources.ERR_INTERNAL_CREATE_BLOCK" );         //$NON-NLS-1$
  String ERR_WRONG_SPLIT                   = Messages.getString( "IS5Resources.ERR_WRONG_SPLIT" );                   //$NON-NLS-1$
  String ERR_WRONG_SPLIT_BLOCK             = Messages.getString( "IS5Resources.ERR_WRONG_SPLIT_BLOCK" );             //$NON-NLS-1$
  String ERR_BLOCK_SPLIT                   = Messages.getString( "IS5Resources.ERR_BLOCK_SPLIT" );                   //$NON-NLS-1$
  String ERR_ALIEN_BLOCK                   = Messages.getString( "IS5Resources.ERR_ALIEN_BLOCK" );                   //$NON-NLS-1$
  String ERR_WRONG_VALUES_TYPE             = Messages.getString( "IS5Resources.ERR_WRONG_VALUES_TYPE" );             //$NON-NLS-1$
  String ERR_COPY_HEAD_ARRAY               = Messages.getString( "IS5Resources.ERR_COPY_HEAD_ARRAY" );               //$NON-NLS-1$
  String ERR_COPY_TAIL_ARRAY               = Messages.getString( "IS5Resources.ERR_COPY_TAIL_ARRAY" );               //$NON-NLS-1$
  String ERR_COPY_ARRAY                    = Messages.getString( "IS5Resources.ERR_COPY_ARRAY" );                    //$NON-NLS-1$

  String ERR_WRONG_OBJ                         = Messages.getString( "IS5Resources.ERR_WRONG_OBJ" );                     //$NON-NLS-1$
  String ERR_WRONG_VALUE_TYPE                  = Messages.getString( "IS5Resources.ERR_WRONG_VALUE_TYPE" );              //$NON-NLS-1$
  String ERR_WRONG_SYNC_DD                     = Messages.getString( "IS5Resources.ERR_WRONG_SYNC_DD" );                 //$NON-NLS-1$
  String ERR_WRONG_ASYNC_DD                    = Messages.getString( "IS5Resources.ERR_WRONG_ASYNC_DD" );                //$NON-NLS-1$
  String ERR_WRONG_DD                          = Messages.getString( "IS5Resources.ERR_WRONG_DD" );                      //$NON-NLS-1$
  String ERR_OBJ_DATAID_NOT_FOUND              = Messages.getString( "IS5Resources.ERR_OBJ_DATAID_NOT_FOUND" );          //$NON-NLS-1$
  String ERR_ASYNC_WRITE_UNXEXPECTED           = Messages.getString( "IS5Resources.ERR_ASYNC_WRITE_UNXEXPECTED" );       //$NON-NLS-1$
  String ERR_SEQUENCE_BLOCK_ENCLOSED_READ      = Messages.getString( "IS5Resources.ERR_SEQUENCE_BLOCK_ENCLOSED_READ" );  //$NON-NLS-1$
  String ERR_SEQUENCE_FIND_FRAGMENTATION       = Messages.getString( "IS5Resources.ERR_SEQUENCE_FIND_FRAGMENTATION" );   //$NON-NLS-1$
  String ERR_SEQUENCE_BLOCK_CROSSED_READ       = Messages.getString( "IS5Resources.ERR_SEQUENCE_BLOCK_CROSSED_READ" );   //$NON-NLS-1$
  String ERR_SEQUENCE_BLOCK_SIZES_READ         = Messages.getString( "IS5Resources.ERR_SEQUENCE_BLOCK_SIZES_READ" );     //$NON-NLS-1$
  String ERR_SEQUENCE_CROSSED_BLOCK_SIZES_READ =
      Messages.getString( "IS5Resources.ERR_SEQUENCE_CROSSED_BLOCK_SIZES_READ" );                                        //$NON-NLS-1$
  String ERR_READ_OUT_OF_MEMORY                = Messages.getString( "IS5Resources.ERR_READ_OUT_OF_MEMORY" );            //$NON-NLS-1$
  String ERR_READ_UNEXPECTED                   = Messages.getString( "IS5Resources.ERR_READ_UNEXPECTED" );               //$NON-NLS-1$
  String ERR_IMPL_BLOCK_NOT_FOUND              = Messages.getString( "IS5Resources.ERR_IMPL_BLOCK_NOT_FOUND" );          //$NON-NLS-1$
  String ERR_READ_LAST_SEQUENCE_UNEXPECTED     = Messages.getString( "IS5Resources.ERR_READ_LAST_SEQUENCE_UNEXPECTED" ); //$NON-NLS-1$
  String ERR_AUTO_CANT_UNION_PASS              = Messages.getString( "IS5Resources.ERR_AUTO_CANT_UNION_PASS" );          //$NON-NLS-1$
  String ERR_AUTO_UNION_PASS_RETRY             = Messages.getString( "IS5Resources.ERR_AUTO_UNION_PASS_RETRY" );         //$NON-NLS-1$
  String ERR_UNION_PASS_MAX                    = Messages.getString( "IS5Resources.ERR_UNION_PASS_MAX" );                //$NON-NLS-1$
  String ERR_UNION_NOT_ENOUGH_MEMORY           = Messages.getString( "IS5Resources.ERR_UNION_NOT_ENOUGH_MEMORY" );       //$NON-NLS-1$
  String ERR_VALIDATION_NOT_ENOUGH_MEMORY      = Messages.getString( "IS5Resources.ERR_VALIDATION_NOT_ENOUGH_MEMORY" );  //$NON-NLS-1$
  String ERR_UNION_UNEXPECTED                  = Messages.getString( "IS5Resources.ERR_UNION_UNEXPECTED" );              //$NON-NLS-1$
  String ERR_CREATE_INFO_UNEXPECTED            = Messages.getString( "IS5Resources.ERR_CREATE_INFO_UNEXPECTED" );        //$NON-NLS-1$
  String ERR_REMOVE_INFO                       = Messages.getString( "IS5Resources.ERR_REMOVE_INFO" );                   //$NON-NLS-1$
  String ERR_REMOVE_INFO_OBJID                 = Messages.getString( "IS5Resources.ERR_REMOVE_INFO_OBJID" );             //$NON-NLS-1$
  String ERR_REMOVE_BLOCK                      = Messages.getString( "IS5Resources.ERR_REMOVE_BLOCK" );                  //$NON-NLS-1$
  String ERR_REMOVE_BLOCK_OBJID                = Messages.getString( "IS5Resources.ERR_REMOVE_BLOCK_OBJID" );            //$NON-NLS-1$
  String ERR_REMOVE_BLOCK_DATAID               = Messages.getString( "IS5Resources.ERR_REMOVE_BLOCK_DATAID" );           //$NON-NLS-1$
  String ERR_REMOVE_BLOCK_OBJID_DATAID         = Messages.getString( "IS5Resources.ERR_REMOVE_BLOCK_OBJID_DATAID" );     //$NON-NLS-1$
  String ERR_CREATE_UNION_TIMER                = Messages.getString( "IS5Resources.ERR_CREATE_UNION_TIMER" );            //$NON-NLS-1$
  String ERR_CANCEL_UNION_TIMER                = Messages.getString( "IS5Resources.ERR_CANCEL_UNION_TIMER" );            //$NON-NLS-1$
  String ERR_STOP_UNKNOW_TIMER                 = Messages.getString( "IS5Resources.ERR_STOP_UNKNOW_TIMER" );             //$NON-NLS-1$
  String ERR_QUERY_DOJOB                       = Messages.getString( "IS5Resources.ERR_QUERY_DOJOB" );                   //$NON-NLS-1$

  String ERR_DATAID_NOT_FOUND = Messages.getString( "IS5Resources.ERR_DATAID_NOT_FOUND" ); //$NON-NLS-1$

  String ERR_QUERY_EDIT_FINALIZED   = Messages.getString( "IS5Resources.ERR_QUERY_EDIT_FINALIZED" );   //$NON-NLS-1$
  String ERR_QUERY_INFOES_UNDEF     = Messages.getString( "IS5Resources.ERR_QUERY_INFOES_UNDEF" );     //$NON-NLS-1$
  String ERR_QUERY_WRONG_INTERVAL   = Messages.getString( "IS5Resources.ERR_QUERY_WRONG_INTERVAL" );   //$NON-NLS-1$
  String ERR_QUERY_READER_UNDEF     = Messages.getString( "IS5Resources.ERR_QUERY_READER_UNDEF" );     //$NON-NLS-1$
  String ERR_QUERY_CALLBACK_UNDEF   = Messages.getString( "IS5Resources.ERR_QUERY_CALLBACK_UNDEF" );   //$NON-NLS-1$
  String ERR_QUERY_DELTA_TIME_UNDEF = Messages.getString( "IS5Resources.ERR_QUERY_DELTA_TIME_UNDEF" ); //$NON-NLS-1$

  String ERR_WRONG_START_TIMEOUT = Messages.getString( "IS5Resources.ERR_WRONG_START_TIMEOUT" ); //$NON-NLS-1$
  String ERR_WRONG_DOJOB_TIMEOUT = Messages.getString( "IS5Resources.ERR_WRONG_DOJOB_TIMEOUT" ); //$NON-NLS-1$

  String ERR_CANT_CHANGE_INFO_NO_EMPTY = Messages.getString( "IS5Resources.ERR_CANT_CHANGE_INFO_NO_EMPTY" ); //$NON-NLS-1$
  String ERR_WRITE_TASK                = Messages.getString( "IS5Resources.ERR_WRITE_TASK" );                //$NON-NLS-1$
  String ERR_ASYNC_UNION_TASK          = Messages.getString( "IS5Resources.ERR_ASYNC_UNION_TASK" );          //$NON-NLS-1$
  String ERR_ASYNC_UNION_THREAD        = Messages.getString( "IS5Resources.ERR_ASYNC_UNION_THREAD" );        //$NON-NLS-1$
  String ERR_ASYNC_UNION_THREAD_BUSY   = "%s. Ошибка доступа к %s проводящему дефрагментацию. Причина: %s";  //$NON-NLS-1$
  String ERR_ASYNC_VALIDATION_TASK     = Messages.getString( "IS5Resources.ERR_ASYNC_VALIDATION_TASK" );     //$NON-NLS-1$
  String ERR_ASYNC_LAST_VALUES_TASK    = Messages.getString( "IS5Resources.ERR_ASYNC_LAST_VALUES_TASK" );    //$NON-NLS-1$
  String ERR_VALIDATION_MERGE          = Messages.getString( "IS5Resources.ERR_VALIDATION_MERGE" );          //$NON-NLS-1$

  String ERR_VALIDATION_NULL_BLOB        = Messages.getString( "IS5Resources.ERR_VALIDATION_NULL_BLOB" );        //$NON-NLS-1$
  String ERR_VALIDATION_NULL_VALUES      = Messages.getString( "IS5Resources.ERR_VALIDATION_NULL_VALUES" );      //$NON-NLS-1$
  String ERR_VALIDATION_WRONG_SIZE       = Messages.getString( "IS5Resources.ERR_VALIDATION_WRONG_SIZE" );       //$NON-NLS-1$
  String ERR_VALIDATION_ZERO_SIZE        = Messages.getString( "IS5Resources.ERR_VALIDATION_ZERO_SIZE" );        //$NON-NLS-1$
  String ERR_VALIDATION_WRONG_INFOID     = Messages.getString( "IS5Resources.ERR_VALIDATION_WRONG_INFOID" );     //$NON-NLS-1$
  String ERR_VALIDATION_WRONG_INTERVAL   = Messages.getString( "IS5Resources.ERR_VALIDATION_WRONG_INTERVAL" );   //$NON-NLS-1$
  String ERR_WRONG_TIMESTAMPS_SIZE       = Messages.getString( "IS5Resources.ERR_WRONG_TIMESTAMPS_SIZE" );       //$NON-NLS-1$
  String ERR_OLD_TIMESTAMPS_STORAGE      = Messages.getString( "IS5Resources.ERR_OLD_TIMESTAMPS_STORAGE" );      //$NON-NLS-1$
  String ERR_VALIDATION_SYNC_WRONG_SIZE  = Messages.getString( "IS5Resources.ERR_VALIDATION_SYNC_WRONG_SIZE" );  //$NON-NLS-1$
  String ERR_VALIDATION_WRONG_TIMESTAMPS = Messages.getString( "IS5Resources.ERR_VALIDATION_WRONG_TIMESTAMPS" ); //$NON-NLS-1$
  String ERR_VALIDATION_CHECK_TIMESTAMPS = Messages.getString( "IS5Resources.ERR_VALIDATION_CHECK_TIMESTAMPS" ); //$NON-NLS-1$
  String ERR_WRONG_STARTTIME             = Messages.getString( "IS5Resources.ERR_WRONG_STARTTIME" );             //$NON-NLS-1$
  String ERR_RESTORE_TIMESTAMPS          = Messages.getString( "IS5Resources.ERR_RESTORE_TIMESTAMPS" );          //$NON-NLS-1$
  String ERR_RESTORE_TIMESTAMPS_SUCCESS  = Messages.getString( "IS5Resources.ERR_RESTORE_TIMESTAMPS_SUCCESS" );  //$NON-NLS-1$
  String ERR_NULL_TIMESTAMPS             = Messages.getString( "IS5Resources.ERR_NULL_TIMESTAMPS" );             //$NON-NLS-1$

  String ERR_CLEAR_BY_CHANGE_INFO_TYPE = Messages.getString( "IS5Resources.ERR_CLEAR_BY_CHANGE_INFO_TYPE" ); //$NON-NLS-1$

  String ERR_NOT_USER_TX = Messages.getString( "IS5Resources.ERR_NOT_USER_TX" ); //$NON-NLS-1$

  String ERR_NOT_OPEN_TX = Messages.getString( "IS5Resources.ERR_NOT_OPEN_TX" ); //$NON-NLS-1$

  String MSG_SPLIT_BLOCK            = Messages.getString( "IS5Resources.MSG_SPLIT_BLOCK" );            //$NON-NLS-1$
  String ERR_WRONG_FIRST_LAST_ORDER = Messages.getString( "IS5Resources.ERR_WRONG_FIRST_LAST_ORDER" ); //$NON-NLS-1$
  String ERR_WRONG_ORDER            = Messages.getString( "IS5Resources.ERR_WRONG_ORDER" );            //$NON-NLS-1$

  String ERR_UNION_DISABLE_BY_LOAD_AVERAGE = Messages.getString( "IS5Resources.ERR_UNION_DISABLE_BY_LOAD_AVERAGE" ); //$NON-NLS-1$
  String ERR_UNION_DISABLE_BY_PREV_UNITER  = Messages.getString( "IS5Resources.ERR_UNION_DISABLE_BY_PREV_UNITER" );  //$NON-NLS-1$
  String ERR_WRITE_DISABLE_BY_LOAD_AVERAGE = Messages.getString( "IS5Resources.ERR_WRITE_DISABLE_BY_LOAD_AVERAGE" ); //$NON-NLS-1$
  String ERR_STATISTICS_DISABLED           = Messages.getString( "IS5Resources.ERR_STATISTICS_DISABLED" );           //$NON-NLS-1$
  String ERR_STATISTICS_NOT_READY          =
      // <<<<<<< HEAD
      Messages.getString( "IS5Resources.ERR_STATISTICS_NOT_READY" );                                                 //$NON-NLS-1$
  String ERR_SEQUENCE_IMPL_NOT_FOUND       = Messages.getString( "IS5Resources.ERR_SEQUENCE_IMPL_NOT_FOUND" );       //$NON-NLS-1$
  String ERR_REMOTE_ACCESS                 = Messages.getString( "IS5Resources.ERR_REMOTE_ACCESS" );                 //$NON-NLS-1$
  String ERR_QUERY_IS_ALREADY_EXECUTE      = Messages.getString( "IS5Resources.ERR_QUERY_IS_ALREADY_EXECUTE" );      //$NON-NLS-1$
  // ======="%s. Объект статистики %s [%s] не готов для использования. Формирование статистики записи хранимых данных
  // отключено";
  // String ERR_SEQUENCE_IMPL_NOT_FOUND = "Не найден класс реализации хранения последовательности значений '%s'";
  // String ERR_REMOTE_ACCESS = "Ошибка получения удаленного доступа к данным на узле %s. Данные: %s";
  // String ERR_QUERY_IS_ALREADY_EXECUTE = "Запрос %s уже выполняется";
  String ERR_GWID_DOUBLE_ON_READ =
      "readSequences(): gwid = '%s' указан в списке запроса несколько раз. Это может снижать эффективность обработки запроса"; //$NON-NLS-1$
  // >>>>>>>refs/heads/2020-11-15_no_localization

  String ERR_WRONG_IMPORT_CURSOR    = Messages.getString( "IS5Resources.ERR_WRONG_IMPORT_CURSOR" );    //$NON-NLS-1$
  String ERR_NOT_CURSOR_IMPORT_DATA = Messages.getString( "IS5Resources.ERR_NOT_CURSOR_IMPORT_DATA" ); //$NON-NLS-1$
  String ERR_NOT_IMPORT_DATA        = Messages.getString( "IS5Resources.ERR_NOT_IMPORT_DATA" );        //$NON-NLS-1$
  String ERR_CAST_VALUE             = Messages.getString( "IS5Resources.ERR_CAST_VALUE" );             //$NON-NLS-1$

  // THREADS
  // ------------------------------------------------------------------------------------
  // Строки сообщений
  //
  String MSG_READ_SEQUENCES_FINISH = Messages.getString( "IS5Resources.MSG_READ_SEQUENCES_FINISH" ); //$NON-NLS-1$
  String MSG_WRITE_THREAD_FINISH   = Messages.getString( "IS5Resources.MSG_WRITE_THREAD_FINISH" );   //$NON-NLS-1$
  String MSG_WAIT_LOCK_GWID        = Messages.getString( "IS5Resources.MSG_WAIT_LOCK_GWID" );        //$NON-NLS-1$
  String MSG_WAIT_LOCK_INFOES      = Messages.getString( "IS5Resources.MSG_WAIT_LOCK_INFOES" );      //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок
  //
  String ERR_SEQUENCE_BEFORE_OUT_OF_MEMORY = Messages.getString( "IS5Resources.ERR_SEQUENCE_BEFORE_OUT_OF_MEMORY" ); //$NON-NLS-1$
  String ERR_SEQUENCE_READ_OUT_OF_MEMORY   = Messages.getString( "IS5Resources.ERR_SEQUENCE_READ_OUT_OF_MEMORY" );   //$NON-NLS-1$
  String ERR_FIND_VALUE_INDEX              = Messages.getString( "IS5Resources.ERR_FIND_VALUE_INDEX" );              //$NON-NLS-1$
  String ERR_SEQUENCE_READ_UNEXPECTED      = Messages.getString( "IS5Resources.ERR_SEQUENCE_READ_UNEXPECTED" );      //$NON-NLS-1$
  String ERR_SEQUENCE_OUT_OF_MEMORY_ERROR  = Messages.getString( "IS5Resources.ERR_SEQUENCE_OUT_OF_MEMORY_ERROR" );  //$NON-NLS-1$
  String ERR_CONCURRENT_WRITE              = Messages.getString( "IS5Resources.ERR_CONCURRENT_WRITE" );              //$NON-NLS-1$
  String ERR_SEQUENCE_WRITE                = Messages.getString( "IS5Resources.ERR_SEQUENCE_WRITE" );                //$NON-NLS-1$
  String ERR_WRITE_UNEXPECTED              = Messages.getString( "IS5Resources.ERR_WRITE_UNEXPECTED" );              //$NON-NLS-1$
  String ERR_WRITE_FLASH_UNEXPECTED        = "Неожиданная ошибка записи (flash) блоков. Причина: %s";                //$NON-NLS-1$
  String ERR_REMOVE_FLASH_UNEXPECTED       = "Неожиданная ошибка записи (remove) блоков. Причина: %s";               //$NON-NLS-1$
  String ERR_PERSIST_UNEXPECTED            = Messages.getString( "IS5Resources.ERR_PERSIST_UNEXPECTED" );            //$NON-NLS-1$
  String ERR_MERGE_UNEXPECTED              = Messages.getString( "IS5Resources.ERR_MERGE_UNEXPECTED" );              //$NON-NLS-1$
  String ERR_REMOVE_UNEXPECTED             = Messages.getString( "IS5Resources.ERR_REMOVE_UNEXPECTED" );             //$NON-NLS-1$
  String ERR_DETACH_UNEXPECTED             = Messages.getString( "IS5Resources.ERR_DETACH_UNEXPECTED" );             //$NON-NLS-1$
  String ERR_SEQUENCE_WRITE_CONCURRENT     = Messages.getString( "IS5Resources.ERR_SEQUENCE_WRITE_CONCURRENT" );     //$NON-NLS-1$
  String ERR_SEQUENCE_WRITE_UNEXPECTED     = Messages.getString( "IS5Resources.ERR_SEQUENCE_WRITE_UNEXPECTED" );     //$NON-NLS-1$

  String ERR_FORCE_UNLOCK_INFO      = Messages.getString( "IS5Resources.ERR_FORCE_UNLOCK_INFO" );      //$NON-NLS-1$
  String ERR_FORCE_UNLOCK_INFOES    = Messages.getString( "IS5Resources.ERR_FORCE_UNLOCK_INFOES" );    //$NON-NLS-1$
  String ERR_ROLLBACK_GWID          = Messages.getString( "IS5Resources.ERR_ROLLBACK_GWID" );          //$NON-NLS-1$
  String ERR_ROLLBACK_INFOES        = Messages.getString( "IS5Resources.ERR_ROLLBACK_INFOES" );        //$NON-NLS-1$
  String ERR_LOCK_INFO_TIMEOUT      = Messages.getString( "IS5Resources.ERR_LOCK_INFO_TIMEOUT" );      //$NON-NLS-1$
  String ERR_LOCK_INFOES_TIMEOUT    = Messages.getString( "IS5Resources.ERR_LOCK_INFOES_TIMEOUT" );    //$NON-NLS-1$
  String ERR_WRONG_CUTTING_INTERVAL = Messages.getString( "IS5Resources.ERR_WRONG_CUTTING_INTERVAL" ); //$NON-NLS-1$
  String ERR_INIT_BLOCK_IMPL        = Messages.getString( "IS5Resources.ERR_INIT_BLOCK_IMPL" );        //$NON-NLS-1$
  String ERR_SEQUENCE_READONLY      = Messages.getString( "IS5Resources.ERR_SEQUENCE_READONLY" );      //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Тексты ошибок S5SequenceFactory
  //
  String ERR_CREATE_BLOCK_UNEXPECTED    = Messages.getString( "IS5Resources.ERR_CREATE_BLOCK_UNEXPECTED" );    //$NON-NLS-1$
  String ERR_CONSTRUCT_BLOCK_UNEXPECTED = Messages.getString( "IS5Resources.ERR_CONSTRUCT_BLOCK_UNEXPECTED" ); //$NON-NLS-1$
  String ERR_BLOCK_IMPL_NOT_FOUND       = Messages.getString( "IS5Resources.ERR_BLOCK_IMPL_NOT_FOUND" );       //$NON-NLS-1$
  String ERR_CREATE_METHOD_NOT_FOUND    = Messages.getString( "IS5Resources.ERR_CREATE_METHOD_NOT_FOUND" );    //$NON-NLS-1$
  String ERR_METHOD_NOT_FOUND           = Messages.getString( "IS5Resources.ERR_METHOD_NOT_FOUND" );           //$NON-NLS-1$

  String ERR_READ_RTDATAID_JDBC_UNEXPECTED = Messages.getString( "IS5Resources.ERR_READ_RTDATAID_JDBC_UNEXPECTED" ); //$NON-NLS-1$
}
