package org.toxsoft.uskat.sysext.alarms.impl;

import org.toxsoft.core.tslib.av.opset.IOptionSetEdit;
import org.toxsoft.core.tslib.av.opset.impl.OptionSet;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.events.msg.IGenericMessageListener;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.sysext.alarms.api.ISkAlarm;
import org.toxsoft.uskat.sysext.alarms.api.ISkAlarmServiceListener;

import ru.uskat.backend.ISkFrontendRear;

/**
 * Сообщение {@link ISkFrontendRear}: {@link ISkAlarmServiceListener#onAlarm(ISkAlarm)}
 *
 * @author mvk
 */
public abstract class SkAlarmMessageWhenOn
    implements IGenericMessageListener {

  /**
   * Идентификатор метода
   */
  public static final String WHEN_ALARM_ON = "whenAlarmFire"; //$NON-NLS-1$

  /**
   * Аргумент: идентификатор тревоги
   * <p>
   * Тип: String
   */
  private static final String ARG_ALARM = "alarm"; //$NON-NLS-1$

  // ------------------------------------------------------------------------------------
  // Открытые методы
  //
  /**
   * Отправляет фронтенду {@link ISkFrontendRear} сообщение {@link ISkAlarmServiceListener#onAlarm(ISkAlarm)}.
   *
   * @param aFrontend {@link ISkFrontendRear} фронтенд
   * @param aAlarm {@link ISkAlarm} аларм
   * @throws TsNullArgumentRtException любой аргумент = null
   */
  public static void send( ISkFrontendRear aFrontend, ISkAlarm aAlarm ) {
    TsNullArgumentRtException.checkNulls( aFrontend, aAlarm );
    IOptionSetEdit args = new OptionSet();
    args.setStr( ARG_ALARM, SkAlarm.KEEPER.ent2str( aAlarm ) );
    aFrontend.onGenericMessage( new GenericMessage( WHEN_ALARM_ON, args ) );
  }

  // ------------------------------------------------------------------------------------
  // Реализация ISkFrontendRear
  //
  @Override
  public void onGenericMessage( GenericMessage aMessage ) {
    TsNullArgumentRtException.checkNulls( aMessage );
    if( !aMessage.messageId().equals( WHEN_ALARM_ON ) ) {
      // Сообщение игнорировано
      return;
    }
    ISkAlarm alarm = SkAlarm.KEEPER.str2ent( aMessage.args().getStr( ARG_ALARM ) );
    doWhenAlarmOn( alarm );
  }

  // ------------------------------------------------------------------------------------
  // Методы для реализации наследниками
  //
  /**
   * Обработка полученного сообщения
   *
   * @param aAlarm {@link ISkAlarm} тревога
   */
  protected abstract void doWhenAlarmOn( ISkAlarm aAlarm );

}
