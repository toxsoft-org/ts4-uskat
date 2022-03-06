package org.toxsoft.uskat.s5.client.remote.connection.pas;

import static org.toxsoft.core.pas.tj.impl.TjUtils.*;
import static org.toxsoft.core.tslib.utils.valobj.TsValobjUtils.*;
import static org.toxsoft.uskat.s5.client.remote.connection.pas.S5CallbackHardConstants.*;

import org.toxsoft.core.pas.common.IPasTxChannel;
import org.toxsoft.core.pas.common.PasChannel;
import org.toxsoft.core.pas.json.IJSONNotification;
import org.toxsoft.core.pas.json.IJSONNotificationHandler;
import org.toxsoft.core.pas.tj.ITjValue;
import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.impl.OptionSetKeeper;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.events.msg.IGenericMessageListener;
import org.toxsoft.core.tslib.coll.primtypes.IStringMapEdit;
import org.toxsoft.core.tslib.coll.primtypes.impl.StringMap;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;

import ru.uskat.backend.ISkFrontendRear;
import ru.uskat.backend.messages.*;

/**
 * Обратный вызов сервера: передача сообщения бекенда
 *
 * @author mvk
 */
public final class S5CallbackOnMessage
    implements IJSONNotificationHandler<S5CallbackChannel> {

  /**
   * Вызов метода: {@link ISkFrontendRear#onGenericMessage(GenericMessage)}
   */
  public static final String ON_MESSAGE_METHOD = FRONTENDS_METHOD_PREFIX + "onMessage"; //$NON-NLS-1$

  /**
   * Идентификатор сообщения
   */
  private static final String MSG_ID = "msgId"; //$NON-NLS-1$

  /**
   * Аргументы сообщения
   */
  private static final String MSG_ARGS = "args"; //$NON-NLS-1$

  /**
   * frontend
   */
  private final IGenericMessageListener frontend;

  /**
   * TODO: ??? где лучше регистрировать киперы с учетом addons?
   */
  static {
    registerKeeperIfNone( SkMessageWhenCommandsStateChanged.KEEPER_ID, SkMessageWhenCommandsStateChanged.KEEPER );
    registerKeeperIfNone( SkMessageWhenCurrdataChanged.KEEPER_ID, SkMessageWhenCurrdataChanged.KEEPER );
    registerKeeperIfNone( SkMessageWhenEvents.KEEPER_ID, SkMessageWhenEvents.KEEPER );
    registerKeeperIfNone( SkMessageExecuteCommand.KEEPER_ID, SkMessageExecuteCommand.KEEPER );
  }

  /**
   * Конструктор
   *
   * @param aFrontend {@link IGenericMessageListener} frontend
   * @throws TsNullArgumentRtException аргумент = null
   */
  S5CallbackOnMessage( IGenericMessageListener aFrontend ) {
    frontend = TsNullArgumentRtException.checkNull( aFrontend );
  }

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Отправляет сообщение по каналу {@link PasChannel}
   *
   * @param aChannel {@link IPasTxChannel} канал передачи
   * @param aMessage {@link GenericMessage} сообщение.
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void send( IPasTxChannel aChannel, GenericMessage aMessage ) {
    TsNullArgumentRtException.checkNulls( aChannel, aMessage );
    IStringMapEdit<ITjValue> notifyParams = new StringMap<>();
    notifyParams.put( MSG_ID, createString( aMessage.messageId() ) );
    notifyParams.put( MSG_ARGS, createString( OptionSetKeeper.KEEPER.ent2str( aMessage.args() ) ) );
    // Передача по каналу
    aChannel.sendNotification( ON_MESSAGE_METHOD, notifyParams );
  }

  // ------------------------------------------------------------------------------------
  // Реализация IJSONNotificationHandler
  //
  @Override
  public void notify( S5CallbackChannel aChannel, IJSONNotification aNotification ) {
    TsNullArgumentRtException.checkNull( aNotification );
    if( !aNotification.method().equals( ON_MESSAGE_METHOD ) ) {
      // Уведомление игнорировано
      return;
    }
    String msgId = aNotification.params().getByKey( MSG_ID ).asString();
    IOptionSet msgArgs = OptionSetKeeper.KEEPER.str2ent( aNotification.params().getByKey( MSG_ARGS ).asString() );
    // Передача сообщения на обработку
    frontend.onGenericMessage( new GenericMessage( msgId, msgArgs ) );
  }

  // ------------------------------------------------------------------------------------
  // Внутренние методы
  //
}
