package org.toxsoft.uskat.core.devapi;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
interface ISkResources {

  /**
   * Common messages.
   */
  // String FMT_ERR_NO_SUCH_OBJ = "Object '%s' does not exists";

  /**
   * {@link ISkatlet}
   */
  String STR_SKATLET_SUPPORT             = Messages.getString( "STR_SKATLET_SUPPORT" );             //$NON-NLS-1$
  String STR_SKATLET_SUPPORT_D           = Messages.getString( "STR_SKATLET_SUPPORT_D" );           //$NON-NLS-1$
  String STR_SKATLET_SHARED_CONNECTION   = Messages.getString( "STR_SKATLET_SHARED_CONNECTION" );   //$NON-NLS-1$
  String STR_SKATLET_SHARED_CONNECTION_D = Messages.getString( "STR_SKATLET_SHARED_CONNECTION_D" ); //$NON-NLS-1$
  String STR_SKATLET_LOAD_ORDER          = Messages.getString( "STR_SKATLET_LOAD_ORDER" );          //$NON-NLS-1$
  String STR_SKATLET_LOAD_ORDER_D        = Messages.getString( "STR_SKATLET_LOAD_ORDER_D" );        //$NON-NLS-1$

  /**
   * {@link ISkWorkerHardConstants}
   */
  String STR_WORKER_CORE_API         = Messages.getString( "STR_WORKER_CORE_API" );        //$NON-NLS-1$
  String STR_WORKER_CORE_API_D       = Messages.getString( "STR_WORKER_CORE_API_D" );      //$NON-NLS-1$
  String STR_WORKER_REGISTRY         = Messages.getString( "STR_WORKER_REGISTRY" );        //$NON-NLS-1$
  String STR_WORKER_REGISTRY_D       = Messages.getString( "STR_WORKER_REGISTRY_D" );      //$NON-NLS-1$
  String STR_WORKER_SHARED_CONTEXT   = Messages.getString( "STR_WORKER_SHARED_CONTEXT" );  //$NON-NLS-1$
  String STR_WORKER_SHARED_CONTEXT_D = Messages.getString( "STR_WORKER_SHARED_CONTEXT_D" );//$NON-NLS-1$
  String STR_WORKER_LOGGER           = Messages.getString( "STR_WORKER_LOGGER" );          //$NON-NLS-1$
  String STR_WORKER_LOGGER_D         = Messages.getString( "STR_WORKER_LOGGER_D" );        //$NON-NLS-1$

  /**
   * {@link SkWorkerRegistry}
   */
  String FMT_ERR_WORKER_IS_ALREADY_REGISTERED = Messages.getString( "FMT_ERR_WORKER_IS_ALREADY_REGISTERED" ); //$NON-NLS-1$
  String FMT_ERR_WORKER_IS_NOT_FOUND          = Messages.getString( "FMT_ERR_WORKER_IS_NOT_FOUND" );          //$NON-NLS-1$
}
