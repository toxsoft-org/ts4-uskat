package org.toxsoft.uskat.alarms.lib.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.alarms.lib.*;
import org.toxsoft.uskat.core.backend.api.AbstractBackendMessageBuilder;

/**
 * {@link IBaAlarms} message builder: alarm is on.
 *
 * @author mvk
 */
public class SkAlarmMsgIsOn
    extends AbstractBackendMessageBuilder {

  /**
   * ID of the message.
   */
  public static final String MSG_ID = "SkAlarmMsgIsOn"; //$NON-NLS-1$

  /**
   * Singletone intance.
   */
  public static final SkAlarmMsgIsOn INSTANCE = new SkAlarmMsgIsOn();

  private static final String ARGID_ALARM = "alarm"; //$NON-NLS-1$

  SkAlarmMsgIsOn() {
    super( ISkAlarmService.SERVICE_ID, MSG_ID );
    defineArgValobj( ARGID_ALARM, SkAlarm.KEEPER_ID, true );
  }

  /**
   * Creates the message instance.
   *
   * @param aAlarm {@link ISkAlarm} - alarm
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessage( ISkAlarm aAlarm ) {
    TsNullArgumentRtException.checkNull( aAlarm );
    return makeMessageVarargs( ARGID_ALARM, avValobj( aAlarm ) );
  }

  /**
   * Extracts alarm argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link ISkAlarm} - alarm.
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public ISkAlarm getAlarm( GenericMessage aMsg ) {
    TsNullArgumentRtException.checkNull( aMsg );
    return getArg( aMsg, ARGID_ALARM ).asValobj();
  }

}
