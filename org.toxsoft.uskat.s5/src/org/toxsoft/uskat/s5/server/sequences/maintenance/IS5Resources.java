package org.toxsoft.uskat.s5.server.sequences.maintenance;

/**
 * Константы, локализуемые ресурсы
 *
 * @author mvk
 */
@SuppressWarnings( "nls" )
interface IS5Resources {

  /**
   * {@link S5SequenceConfig}
   */
  String STR_N_DATABASE_ENGINE = "engine";          //$NON-NLS-1$
  String STR_D_DATABASE_ENGINE = "database engine"; //$NON-NLS-1$

  String STR_N_DATABASE_SCHEMA = "schema";          //$NON-NLS-1$
  String STR_D_DATABASE_SCHEMA = "database schema"; //$NON-NLS-1$

  String STR_N_DATABASE_DEPTH = "depth";                                                                                                                                                                         //$NON-NLS-1$
  String STR_D_DATABASE_DEPTH =
      "Determines the storage time (in days) for historical data values, events, and command history. In fact, the system can store data for a longer period (determined by the implementation), but not less."; //$NON-NLS-1$

  String STR_N_MARIADB = "MariaDB";
  String STR_D_MARIADB = "MariaDB database";

  String STR_N_MYSQL = "MySQL";
  String STR_D_MYSQL = "MySQL database";

  String STR_N_POSTGRESQL = "PostgreSQL";
  String STR_D_POSTGRESQL = "PostgreSQL database";

  String N_STATISTICS_TIMEOUT = "statDoJobTimeout";
  String D_STATISTICS_TIMEOUT = "Интервал обработки статитистики (мсек)";

  /**
   * {@link S5SequenceUnionConfig}
   */
  String N_UNION_DOJOB_TIMEOUT = "doJobTimeout";
  String D_UNION_DOJOB_TIMEOUT = "Интервал выполнения фонововой работы дефрагментации (мсек)";

  String D_UNION_INTERVAL = Messages.getString( "IS5Resources.D_UNION_INTERVAL" ); //$NON-NLS-1$
  String N_UNION_INTERVAL = Messages.getString( "IS5Resources.N_UNION_INTERVAL" ); //$NON-NLS-1$

  String D_UNION_GWIDS = Messages.getString( "IS5Resources.D_UNION_GWIDS" ); //$NON-NLS-1$
  String N_UNION_GWIDS = Messages.getString( "IS5Resources.N_UNION_GWIDS" ); //$NON-NLS-1$

  String D_UNION_PERIOD = Messages.getString( "IS5Resources.D_UNION_PERIOD" ); //$NON-NLS-1$
  String N_UNION_PERIOD = Messages.getString( "IS5Resources.N_UNION_PERIOD" ); //$NON-NLS-1$

  String D_UNION_OFFSET = Messages.getString( "IS5Resources.D_UNION_OFFSET" ); //$NON-NLS-1$
  String N_UNION_OFFSET = Messages.getString( "IS5Resources.N_UNION_OFFSET" ); //$NON-NLS-1$

  String D_FRAGMENT_TIMEOUT = Messages.getString( "IS5Resources.D_FRAGMENT_TIMEOUT" ); //$NON-NLS-1$
  String N_FRAGMENT_TIMEOUT = Messages.getString( "IS5Resources.N_FRAGMENT_TIMEOUT" ); //$NON-NLS-1$

  String D_FRAGMENT_COUNT_MIN = Messages.getString( "IS5Resources.D_FRAGMENT_COUNT_MIN" ); //$NON-NLS-1$
  String N_FRAGMENT_COUNT_MIN = Messages.getString( "IS5Resources.N_FRAGMENT_COUNT_MIN" ); //$NON-NLS-1$

  String D_FRAGMENT_COUNT_MAX = Messages.getString( "IS5Resources.D_FRAGMENT_COUNT_MAX" ); //$NON-NLS-1$
  String N_FRAGMENT_COUNT_MAX = Messages.getString( "IS5Resources.N_FRAGMENT_COUNT_MAX" ); //$NON-NLS-1$

  String D_THREADS_COUNT = Messages.getString( "IS5Resources.D_THREADS_COUNT" ); //$NON-NLS-1$
  String N_THREADS_COUNT = Messages.getString( "IS5Resources.N_THREADS_COUNT" ); //$NON-NLS-1$

  String D_LOOKUP_COUNT = Messages.getString( "IS5Resources.D_LOOKUP_COUNT" ); //$NON-NLS-1$
  String N_LOOKUP_COUNT = Messages.getString( "IS5Resources.N_LOOKUP_COUNT" ); //$NON-NLS-1$

  /**
   * {@link S5SequencePartitionConfig}
   */
  String N_PARTITION_DOJOB_TIMEOUT = "doJobTimeout";
  String D_PARTITION_DOJOB_TIMEOUT = "Интервал выполнения фонововой работы обработки разделов (мсек)";

  String N_PARTITION_TIMEOUT = "timeout";
  String D_PARTITION_TIMEOUT = "Интервал выполнения операций обработки разделов (мсек)";

  String D_REMOVE_INTERVAL = Messages.getString( "IS5Resources.D_REMOVE_INTERVAL" ); //$NON-NLS-1$
  String N_REMOVE_INTERVAL = Messages.getString( "IS5Resources.N_REMOVE_INTERVAL" ); //$NON-NLS-1$

  String D_REMOVE_FROM_TABLES = Messages.getString( "IS5Resources.D_REMOVE_FROM_TABLES" ); //$NON-NLS-1$
  String N_REMOVE_FROM_TABLES = Messages.getString( "IS5Resources.N_REMOVE_FROM_TABLES" ); //$NON-NLS-1$

  String D_REMOVE_THREADS_COUNT = Messages.getString( "IS5Resources.D_REMOVE_THREADS_COUNT" ); //$NON-NLS-1$
  String N_REMOVE_THREADS_COUNT = Messages.getString( "IS5Resources.N_REMOVE_THREADS_COUNT" ); //$NON-NLS-1$

  String D_REMOVE_LOOKUP_COUNT = Messages.getString( "IS5Resources.D_REMOVE_LOOKUP_COUNT" ); //$NON-NLS-1$
  String N_REMOVE_LOOKUP_COUNT = Messages.getString( "IS5Resources.N_REMOVE_LOOKUP_COUNT" ); //$NON-NLS-1$

  /**
   * {@link S5SequenceValidationConfig}
   */
  String D_VALID_REPAIR = Messages.getString( "IS5Resources.D_VALID_REPAIR" ); //$NON-NLS-1$
  String N_VALID_REPAIR = Messages.getString( "IS5Resources.N_VALID_REPAIR" ); //$NON-NLS-1$

  String D_VALID_FORCE_REPAIR = Messages.getString( "IS5Resources.D_VALID_FORCE_REPAIR" ); //$NON-NLS-1$
  String N_VALID_FORCE_REPAIR = Messages.getString( "IS5Resources.N_VALID_FORCE_REPAIR" ); //$NON-NLS-1$

  String D_VALID_INTERVAL = Messages.getString( "IS5Resources.D_VALID_INTERVAL" ); //$NON-NLS-1$
  String N_VALID_INTERVAL = Messages.getString( "IS5Resources.N_VALID_INTERVAL" ); //$NON-NLS-1$

  String D_VALID_GWIDS = Messages.getString( "IS5Resources.D_VALID_GWIDS" ); //$NON-NLS-1$
  String N_VALID_GWIDS = Messages.getString( "IS5Resources.N_VALID_GWIDS" ); //$NON-NLS-1$

  String D_VALID_AUTO_REPAIR = Messages.getString( "IS5Resources.D_VALID_AUTO_REPAIR" ); //$NON-NLS-1$
  String N_VALID_AUTO_REPAIR = Messages.getString( "IS5Resources.N_VALID_AUTO_REPAIR" ); //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Messages
  //

  // ------------------------------------------------------------------------------------
  // Errors
  //

}
