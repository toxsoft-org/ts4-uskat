package org.toxsoft.uskat.s5.client.remote.connection.pas;

import org.toxsoft.core.pas.json.IJSONNotification;
import org.toxsoft.uskat.core.backend.ISkFrontendRear;

/**
 * Вспомогательные константы пакета
 *
 * @author mvk
 */
class S5CallbackHardConstants {

  /**
   * Префикс идентификаторов JSON-уведомления {@link IJSONNotification#method()} используемый для передачи запросов
   * {@link ISkFrontendRear}
   */
  static final String BACKEND_METHOD_PREFIX = "org.toxsoft.uskat.s5.backend.messages."; //$NON-NLS-1$

}
