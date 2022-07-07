package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.core.tslib.av.EAtomicType.*;
import static org.toxsoft.core.tslib.av.impl.AvUtils.*;
import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;

import org.toxsoft.core.tslib.av.impl.*;
import org.toxsoft.core.tslib.av.metainfo.*;
import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;

/**
 * Messages generated by {@link IBaLinks} addon to the frontend.
 *
 * @author hazard157
 */
public interface IBaLinksMessages {

  // FIXME move to BaMsgXxx

  // ------------------------------------------------------------------------------------
  // Message: any change in the objects

  /**
   * The message ID.
   */
  String MSGID_LINKS_CHANGE = "LinksChange"; //$NON-NLS-1$

  /**
   * ID of the option {@link #OPDEF_GWIDS_LIST}.
   */
  String OPID_GWIDS_LIST = "GwidsList"; //$NON-NLS-1$

  /**
   * {@link GtMessage#args()} option: {@link IGwidList} list of the concrete GWIDs of the changed links.
   */
  IDataDef OPDEF_GWIDS_LIST = DataDef.create( OPID_GWIDS_LIST, VALOBJ, //
      TSID_IS_MANDATORY, AV_TRUE, //
      TSID_KEEPER_ID, GwidList.KEEPER_ID //
  );

  /**
   * Creates the {@link GtMessage} for any change in the links.
   *
   * @param aGwidList {@link IGwidList} - list of the concrete GWIDs of the changed links
   * @return {@link GtMessage} - created instance to send to the frontend
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  static GtMessage makeMessage( IGwidList aGwidList ) {
    TsNullArgumentRtException.checkNull( aGwidList );
    GtMessage msg;
    msg = new GtMessage( ISkObjectService.SERVICE_ID, MSGID_LINKS_CHANGE, //
        OPID_GWIDS_LIST, avValobj( aGwidList ) );
    return msg;
  }

  /**
   * Extracts {@link #OPDEF_GWIDS_LIST} value from the message with ID {@link #MSGID_LINKS_CHANGE}.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link IGwidList} - retreived value
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsIllegalArgumentRtException message is not {@link #MSGID_LINKS_CHANGE}
   */
  static IGwidList extractGwidList( GenericMessage aMsg ) {
    TsNullArgumentRtException.checkNull( aMsg );
    TsIllegalArgumentRtException.checkFalse( aMsg.messageId().equals( MSGID_LINKS_CHANGE ) );
    return OPDEF_GWIDS_LIST.getValue( aMsg.args() ).asValobj();
  }

}
