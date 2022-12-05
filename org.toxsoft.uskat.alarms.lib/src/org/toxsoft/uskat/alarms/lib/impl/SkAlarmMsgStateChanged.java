package org.toxsoft.uskat.alarms.lib.impl;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.alarms.lib.*;
import org.toxsoft.uskat.core.backend.api.AbstractBackendMessageBuilder;

/**
 * {@link IBaAlarms} message builder: alarm state changed.
 *
 * @author mvk
 */
public class SkAlarmMsgStateChanged
    extends AbstractBackendMessageBuilder {

  /**
   * ID of the message.
   */
  public static final String MSG_ID = "SkAlarmMsgStateChanged"; //$NON-NLS-1$

  /**
   * Singletone intance.
   */
  public static final SkAlarmMsgStateChanged INSTANCE = new SkAlarmMsgStateChanged();

  private static final String ARGID_ALARM       = "alarm";      //$NON-NLS-1$
  private static final String ARGID_THREAD_ITEM = "threadItem"; //$NON-NLS-1$

  SkAlarmMsgStateChanged() {
    super( ISkAlarmService.SERVICE_ID, MSG_ID );
    defineArgValobj( ARGID_ALARM, SkAlarm.KEEPER_ID, true );
    defineArgValobj( ARGID_THREAD_ITEM, SkAlarmThreadHistoryItem.KEEPER_ID, true );
  }

  /**
   * Creates the message instance.
   *
   * @param aAlarm {@link ISkAlarm} - alarm
   * @param aThreadItem {@link ISkAlarmThreadHistoryItem} - thread history item.
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessage( ISkAlarm aAlarm, ISkAlarmThreadHistoryItem aThreadItem ) {
    TsNullArgumentRtException.checkNulls( aAlarm, aThreadItem );
    return makeMessageVarargs( //
        ARGID_ALARM, avValobj( aAlarm ), //
        ARGID_THREAD_ITEM, avValobj( aThreadItem ) //
    );
  }

  /**
   * Extracts alarm argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link ISkAlarm} - alarm.
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public ISkAlarm getAlarm( GenericMessage aMsg ) {
    TsNullArgumentRtException.checkNulls( aMsg );
    return getArg( aMsg, ARGID_ALARM ).asValobj();
  }

  /**
   * Extracts alarm thread history item argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link ISkAlarm} - alarm thread history item.
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public ISkAlarmThreadHistoryItem getThreadHistoryItem( GenericMessage aMsg ) {
    TsNullArgumentRtException.checkNulls( aMsg );
    return getArg( aMsg, ARGID_THREAD_ITEM ).asValobj();
  }

}
