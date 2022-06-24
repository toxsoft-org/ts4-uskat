package org.toxsoft.uskat.s5.server.backend.messages;

import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.coll.helpers.ECrudOp;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

/**
 * Сообщение об предстоящем подключении к бекенду
 *
 * @author mvk
 */
public interface IS5BaBeforeConnectMessages
    extends IS5BackendMessages {

  /**
   * The message ID.
   */
  String MSGID = "BeforeConnect"; //$NON-NLS-1$

  /**
   * Creates the {@link GtMessage} for any change in the classes.
   *
   * @param aOp {@link ECrudOp} - the change kind
   * @param aClassId String - affected class ID, assumed <code>null</code> for {@link ECrudOp#LIST}
   * @return {@link GtMessage} - created instance to send to the frontend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  static GtMessage makeMessage() {
    GtMessage msg = new GtMessage( TOPICID, MSGID );
    return msg;
  }

}
