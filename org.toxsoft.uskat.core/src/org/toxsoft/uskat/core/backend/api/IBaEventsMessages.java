package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.evserv.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.impl.*;

/**
 * Messages generated by {@link IBaEvents} addon to the frontend.
 *
 * @author hazard157
 */
public interface IBaEventsMessages {

  // ------------------------------------------------------------------------------------
  // Message: list of events

  /**
   * The message ID.
   */
  String MSGID_SK_EVENTS = "SkEvents"; //$NON-NLS-1$

  /**
   * ID of the option {@link #OPDEF_EVENTS_LIST}.
   */
  String OPID_EVENTS_LIST = "EventsList"; //$NON-NLS-1$

  /**
   * {@link GtMessage#args()} option: {@link ISkEventList} list of the concrete GWIDs of the changed links.
   */
  IDataDef OPDEF_EVENTS_LIST = DataDef.create( OPID_EVENTS_LIST, VALOBJ, //
      TSID_IS_MANDATORY, AV_TRUE, //
      TSID_KEEPER_ID, SkEventList.KEEPER_ID //
  );

  /**
   * Creates the {@link GtMessage} with arrived events.
   *
   * @param aSkEventsList {@link ISkEventList} - list of the event
   * @return {@link GtMessage} - created instance to send to the frontend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  static GtMessage makeMessage( ISkEventList aSkEventsList ) {
    TsNullArgumentRtException.checkNull( aSkEventsList );
    GtMessage msg;
    msg = new GtMessage( ISkObjectService.SERVICE_ID, MSGID_SK_EVENTS, //
        OPID_EVENTS_LIST, avValobj( aSkEventsList ) );
    return msg;
  }

  /**
   * Extracts {@link #OPDEF_EVENTS_LIST} value from the message with ID {@link #MSGID_SK_EVENTS}.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link ISkEventList} - retreived value
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException message is not {@link #MSGID_SK_EVENTS}
   */
  static ISkEventList extractSkEventsList( GenericMessage aMsg ) {
    TsNullArgumentRtException.checkNull( aMsg );
    TsIllegalArgumentRtException.checkFalse( aMsg.messageId().equals( MSGID_SK_EVENTS ) );
    return OPDEF_EVENTS_LIST.getValue( aMsg.args() ).asValobj();
  }

}
