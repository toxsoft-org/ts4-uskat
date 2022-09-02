package org.toxsoft.uskat.s5.server.sessions.pas;

import static org.toxsoft.core.pas.tj.impl.TjUtils.*;
import static org.toxsoft.uskat.s5.server.sessions.pas.S5SessionCallbackHardConstants.*;

import org.toxsoft.core.pas.common.IPasTxChannel;
import org.toxsoft.core.pas.common.PasChannel;
import org.toxsoft.core.pas.json.IJSONNotification;
import org.toxsoft.core.pas.json.IJSONNotificationHandler;
import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.backend.ISkFrontendRear;

/**
 * Передача сообщения от фронтенда к бекенду через PAS
 *
 * @author mvk
 */
public abstract class S5CallbackOnFrontendMessage
    implements IJSONNotificationHandler<S5SessionCallbackChannel> {

  /**
   * Вызов метода: {@link ISkFrontendRear#onBackendMessage(GtMessage)}
   */
  public static final String ON_MESSAGE_METHOD = FRONTEND_METHOD_PREFIX + "onFrontendMessage"; //$NON-NLS-1$

  /**
   * Тема сообщения
   */
  private static final String MSG_TOPIC = "topicId"; //$NON-NLS-1$

  /**
   * Идентификатор сообщения
   */
  private static final String MSG_ID = "msgId"; //$NON-NLS-1$

  /**
   * Аргументы сообщения
   */
  private static final String MSG_ARGS = "args"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Отправляет сообщение по каналу {@link PasChannel}
   *
   * @param aChannel {@link IPasTxChannel} канал передачи
   * @param aMessage {@link GtMessage} сообщение.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void send( IPasTxChannel aChannel, GtMessage aMessage ) {
    TsNullArgumentRtException.checkNulls( aChannel, aMessage );
    IStringMapEdit<ITjValue> notifyParams = new StringMap<>();
    notifyParams.put( MSG_TOPIC, createString( aMessage.topicId() ) );
    notifyParams.put( MSG_ID, createString( aMessage.messageId() ) );
    notifyParams.put( MSG_ARGS, createString( OptionSetKeeper.KEEPER.ent2str( aMessage.args() ) ) );
    // Передача по каналу
    aChannel.sendNotification( ON_MESSAGE_METHOD, notifyParams );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IJSONNotificationHandler
  //
  @Override
  public void notify( S5SessionCallbackChannel aChannel, IJSONNotification aNotification ) {
    TsNullArgumentRtException.checkNull( aNotification );
    if( !aNotification.method().equals( ON_MESSAGE_METHOD ) ) {
      // Уведомление игнорировано
      return;
    }
    String topicId = aNotification.params().getByKey( MSG_TOPIC ).asString();
    String msgId = aNotification.params().getByKey( MSG_ID ).asString();
    IOptionSet msgArgs = OptionSetKeeper.KEEPER.str2ent( aNotification.params().getByKey( MSG_ARGS ).asString() );
    // Передача сообщения на обработку
    onFrontendMessage( new GtMessage( topicId, msgId, msgArgs ) );
  }

  // ------------------------------------------------------------------------------------
  // Абстрактные методы для реализации наследником
  //
  protected abstract void onFrontendMessage( GtMessage aMessage );

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
