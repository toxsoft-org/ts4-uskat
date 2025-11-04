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
  String STR_N_WORKER_CORE_API = "coreApi";
  String STR_D_WORKER_CORE_API = "API сервера";

  String STR_N_WORKER_REGISTRY = "workers";
  String STR_D_WORKER_REGISTRY = "Реестр доступных компонентов";

  String STR_N_WORKER_SHARED_CONTEXT = "Общий контекст";
  String STR_D_WORKER_SHARED_CONTEXT = "Общий контекст разделяемый между компонентами";

  String STR_N_WORKER_LOGGER = "logger";
  String STR_D_WORKER_LOGGER = "Журнал работы компонета";

  // ------------------------------------------------------------------------------------
  // messages
  //

  // ------------------------------------------------------------------------------------
  // errors & warnings
  //
  String ERR_WORKER_IS_ALREADY_REGISTERED = "worker %s is already registered";
  String ERR_WORKER_IS_NOT_FOUND          = "worker %s is not found";

}
