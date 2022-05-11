package org.toxsoft.uskat.sysext.alarms.impl;

import org.toxsoft.core.tslib.av.opset.IOptionSet;
import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.events.msg.IGenericMessageListener;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.sysext.alarms.api.*;

import ru.uskat.backend.ISkFrontendRear;

/**
 * Сообщение {@link ISkFrontendRear}:
 * {@link ISkAlarmServiceListener#onAlarmStateChanged(ISkAlarm, ISkAnnounceThreadHistoryItem)}
 *
 * @author mvk
 */
public abstract class SkAlarmMessageWhenStateChanged
    implements IGenericMessageListener {

  /**
   * Идентификатор метода
   */
  public static final String WHEN_ALARM_STATE_CHANGED = "whenAlarmStateChanged"; //$NON-NLS-1$

  /**
   * Аргумент: идентификатор тревоги
   * <p>
   * Тип: String
   */
  private static final String ARG_ALARM = "alarm"; //$NON-NLS-1$

  /**
   * Аргумент: идентификатор ресурса
   * <p>
   * Тип: String
   */
  private static final String ARG_THREAD_ITEM = "threadItem"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Отправляет фронтенду {@link ISkFrontendRear} сообщение {@link ISkAlarmServiceListener#onAlarm(ISkAlarm)}.
   *
   * @param aFrontend {@link ISkFrontendRear} фронтенд
   * @param aAlarm {@link ISkAlarm} аларм
   * @param aThreadItem {@link ISkAnnounceThreadHistoryItem} Элемент истории выполнения этапов нитки оповещения по
   *          тревоге
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void send( ISkFrontendRear aFrontend, ISkAlarm aAlarm, ISkAnnounceThreadHistoryItem aThreadItem ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aAlarm, aThreadItem );
    IOptionSetEdit args = new OptionSet();
    args.setStr( ARG_ALARM, SkAlarm.KEEPER.ent2str( aAlarm ) );
    args.setStr( ARG_THREAD_ITEM, SkAlarmAnnounceThreadHistoryItem.KEEPER.ent2str( aThreadItem ) );
    aFrontend.onGenericMessage( new GenericMessage( WHEN_ALARM_STATE_CHANGED, args ) );
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkFrontendRear
  //
  @Override
  public void onGenericMessage( GenericMessage aMessage ) {
    TsNullArgumentRtException.checkNulls( aMessage );
    if( !aMessage.messageId().equals( WHEN_ALARM_STATE_CHANGED ) ) {
      // Сообщение игнорировано
      return;
    }
    IOptionSet args = aMessage.args();
    ISkAlarm alarm = SkAlarm.KEEPER.str2ent( args.getStr( ARG_ALARM ) );
    ISkAnnounceThreadHistoryItem threadItem =
        SkAlarmAnnounceThreadHistoryItem.KEEPER.str2ent( args.getStr( ARG_THREAD_ITEM ) );
    doWhenAlarmStateChanged( alarm, threadItem );
  }

  // ------------------------------------------------------------------------------------
  // Методы для реализации наследниками
  //
  /**
   * Обработка полученного сообщения
   *
   * @param aAlarm {@link ISkAlarm} тревога
   * @param aThreadItem {@link ISkAnnounceThreadHistoryItem} Элемент истории выполнения этапов нитки оповещения по
   *          тревоге
   */
  protected abstract void doWhenAlarmStateChanged( ISkAlarm aAlarm, ISkAnnounceThreadHistoryItem aThreadItem );

}
