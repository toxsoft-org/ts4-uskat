package org.toxsoft.uskat.s5.server.backend.messages;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.api.AbstractBackendMessageBuilder;

/**
 * Сообщение об предстоящей инициализации расширения бекенда
 *
 * @author mvk
 */
public class S5BaBeforeInitMessages
    extends AbstractBackendMessageBuilder
    implements IS5BackendMessages {

  /**
   * ID of the message.
   */
  public static final String MSG_ID = "BeforeInit"; //$NON-NLS-1$

  /**
   * Singletone intance.
   */
  public static final S5BaAfterConnectMessages INSTANCE = new S5BaAfterConnectMessages();

  S5BaBeforeInitMessages() {
    super( TOPIC_ID, MSG_ID );
  }

  /**
   * Creates the {@link GtMessage} for any change in the classes.
   *
   * @return {@link GtMessage} - created instance to send to the frontend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  static GtMessage makeMessage() {
    GtMessage msg = new GtMessage( TOPIC_ID, MSG_ID );
    return msg;
  }

}
