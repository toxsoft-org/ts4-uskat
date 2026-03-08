package org.toxsoft.uskat.core.devapi;

/**
 * Localizable resources.
 *
 * @author hazard157
 */
@SuppressWarnings( "nls" )
interface ISkResources {

  /**
   * Common messages.
   */
  // String FMT_ERR_NO_SUCH_OBJ = "Object '%s' does not exists";

  /**
   * {@link ISkatlet}
   */
  String STR_SKATLET_SUPPORT             = Messages.getString( "STR_SKATLET_SUPPORT" );
  String STR_SKATLET_SUPPORT_D           = Messages.getString( "STR_SKATLET_SUPPORT_D" );
  String STR_SKATLET_SHARED_CONNECTION   = Messages.getString( "STR_SKATLET_SHARED_CONNECTION" );   //$NON-NLS-1$
  String STR_SKATLET_SHARED_CONNECTION_D = Messages.getString( "STR_SKATLET_SHARED_CONNECTION_D" ); //$NON-NLS-1$
  String STR_SKATLET_LOAD_ORDER          = Messages.getString( "STR_SKATLET_LOAD_ORDER" );
  String STR_SKATLET_LOAD_ORDER_D        = Messages.getString( "STR_SKATLET_LOAD_ORDER_D" );

  /**
   * {@link ISkWorkerHardConstants}
   */
  String STR_WORKER_CORE_API         = Messages.getString( "STR_WORKER_CORE_API" );
  String STR_WORKER_CORE_API_D       = Messages.getString( "STR_WORKER_CORE_API_D" );
  String STR_WORKER_REGISTRY         = Messages.getString( "STR_WORKER_REGISTRY" );
  String STR_WORKER_REGISTRY_D       = Messages.getString( "STR_WORKER_REGISTRY_D" );
  String STR_WORKER_SHARED_CONTEXT   = Messages.getString( "STR_WORKER_SHARED_CONTEXT" );
  String STR_WORKER_SHARED_CONTEXT_D = Messages.getString( "STR_WORKER_SHARED_CONTEXT_D" );
  String STR_WORKER_LOGGER           = Messages.getString( "STR_WORKER_LOGGER" );
  String STR_WORKER_LOGGER_D         = Messages.getString( "STR_WORKER_LOGGER_D" );

  /**
   * {@link SkWorkerRegistry}
   */
  String FMT_ERR_WORKER_IS_ALREADY_REGISTERED = "worker %s is already registered";
  String FMT_ERR_WORKER_IS_NOT_FOUND          = "worker %s is not found";

}
