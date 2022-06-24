package org.toxsoft.uskat.s5.server.backend.messages;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Сообщение об завершении инициализации
 *
 * @author mvk
 */
public interface IS5BaAfterInitMessages
    extends IS5BackendMessages {

  /**
   * The message ID.
   */
  String MSGID = "AfterInit"; //$NON-NLS-1$

  /**
   * Creates the {@link GtMessage} for any change in the classes.
   *
   * @return {@link GtMessage} - created instance to send to the frontend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  static GtMessage makeMessage() {
    GtMessage msg = new GtMessage( TOPICID, MSGID );
    return msg;
  }

}
