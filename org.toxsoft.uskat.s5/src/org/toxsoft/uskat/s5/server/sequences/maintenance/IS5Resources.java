package org.toxsoft.uskat.s5.server.sequences.maintenance;

/**
 * Константы, локализуемые ресурсы
 *
 * @author mvk
 */
interface IS5Resources {

  /**
   * {@link S5SequenceConfig}
   */
  String STR_N_BACKEND_DB_ENGINE = "engine";          //$NON-NLS-1$
  String STR_D_BACKEND_DB_ENGINE = "database engine"; //$NON-NLS-1$

  String STR_N_BACKEND_DB_SCHEMA = "schema";          //$NON-NLS-1$
  String STR_D_BACKEND_DB_SCHEMA = "database schema"; //$NON-NLS-1$

  String STR_N_BACKEND_DB_DEPTH         = "depth";                                                                                                                                                               //$NON-NLS-1$
  String STR_D_BACKEND_DB_DEPTH =
      "Determines the storage time (in days) for historical data values, events, and command history. In fact, the system can store data for a longer period (determined by the implementation), but not less."; //$NON-NLS-1$

  /**
   * {@link S5SequenceUnionConfig}
   */
  String D_UNION_CALENDARS = Messages.getString( "IS5Resources.D_UNION_CALENDARS" ); //$NON-NLS-1$
  String N_UNION_CALENDARS = Messages.getString( "IS5Resources.N_UNION_CALENDARS" ); //$NON-NLS-1$

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
  String D_PARTITION_CALENDARS = Messages.getString( "IS5Resources.D_PARTITION_CALENDARS" ); //$NON-NLS-1$
  String N_PARTITION_CALENDARS = Messages.getString( "IS5Resources.N_PARTITION_CALENDARS" ); //$NON-NLS-1$

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
