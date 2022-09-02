package org.toxsoft.uskat.s5.server.sessions.pas;

import org.toxsoft.core.pas.json.IJSONNotification;
import org.toxsoft.uskat.core.backend.ISkFrontendRear;

/**
 * Вспомогательные константы пакета
 *
 * @author mvk
 */
class S5SessionCallbackHardConstants {

  /**
   * Префикс идентификаторов JSON-уведомления {@link IJSONNotification#method()} используемый для передачи запросов
   * {@link ISkFrontendRear}
   */
  static final String FRONTEND_METHOD_PREFIX = "org.toxsoft.uskat.s5.frontend.messages."; //$NON-NLS-1$
}
