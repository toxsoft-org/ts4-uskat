package org.toxsoft.uskat.core.api.hqserv;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
interface ISkResources {

  /**
   * {@link ESkQueryState}
   */
  String STR_UNPREPARED   = Messages.getString( "STR_UNPREPARED" );   //$NON-NLS-1$
  String STR_UNPREPARED_D = Messages.getString( "STR_UNPREPARED_D" ); //$NON-NLS-1$
  String STR_PREPARED     = Messages.getString( "STR_PREPARED" );     //$NON-NLS-1$
  String STR_PREPARED_D   = Messages.getString( "STR_PREPARED_D" );   //$NON-NLS-1$
  String STR_EXECUTING    = Messages.getString( "STR_EXECUTING" );    //$NON-NLS-1$
  String STR_EXECUTING_D  = Messages.getString( "STR_EXECUTING_D" );  //$NON-NLS-1$
  String STR_READY        = Messages.getString( "STR_READY" );        //$NON-NLS-1$
  String STR_READY_D      = Messages.getString( "STR_READY_D" );      //$NON-NLS-1$
  String STR_FAILED       = Messages.getString( "STR_FAILED" );       //$NON-NLS-1$
  String STR_FAILED_D     = Messages.getString( "STR_FAILED_D" );     //$NON-NLS-1$
  String STR_CLOSED       = Messages.getString( "STR_CLOSED" );       //$NON-NLS-1$
  String STR_CLOSED_D     = Messages.getString( "STR_CLOSED_D" );     //$NON-NLS-1$

  /**
   * {@link ISkHistoryQueryServiceConstants}
   */
  String STR_MAX_AGGREGATION_INTERVAL   = Messages.getString( "STR_MAX_AGGREGATION_INTERVAL" );   //$NON-NLS-1$
  String STR_MAX_AGGREGATION_INTERVAL_D = Messages.getString( "STR_MAX_AGGREGATION_INTERVAL_D" ); //$NON-NLS-1$
  String STR_MAX_AGGREGATION_START      = Messages.getString( "STR_MAX_AGGREGATION_START" );      //$NON-NLS-1$
  String STR_MAX_AGGREGATION_START_D    = Messages.getString( "STR_MAX_AGGREGATION_START_D" );    //$NON-NLS-1$
  String STR_MAX_EXECUTION_TIME         = Messages.getString( "STR_MAX_EXECUTION_TIME" );         //$NON-NLS-1$
  String STR_MAX_EXECUTION_TIME_D       = Messages.getString( "STR_MAX_EXECUTION_TIME_D" );       //$NON-NLS-1$

}
