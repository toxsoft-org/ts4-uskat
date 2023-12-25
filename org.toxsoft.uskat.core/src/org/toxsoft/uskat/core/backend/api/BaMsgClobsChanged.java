package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.clobserv.*;

/**
 * {@link IBaClobs} message builder: database content changed.
 *
 * @author hazard157
 */
public class BaMsgClobsChanged
    extends AbstractBackendMessageBuilder {

  /**
   * The message ID.
   */
  public static final String MSG_ID = "ClobChange"; //$NON-NLS-1$

  /**
   * The builder singleton.
   */
  public static final BaMsgClobsChanged BUILDER = new BaMsgClobsChanged();

  private static final String ARGID_CLOB_GWID = "CrudOp"; //$NON-NLS-1$

  private BaMsgClobsChanged() {
    super( ISkClobService.SERVICE_ID, MSG_ID );
    defineArgValobj( ARGID_CLOB_GWID, Gwid.KEEPER_ID, true );
  }

  /**
   * Creates the message instance.
   *
   * @param aClobGwid {@link Gwid} - GWID of the changed CLOB
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessage( Gwid aClobGwid ) {
    TsNullArgumentRtException.checkNull( aClobGwid );
    return makeMessageVarargs( //
        ARGID_CLOB_GWID, avValobj( aClobGwid ) //
    );
  }

  /**
   * Extracts "GWID of the changed CLOB" argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link Gwid} - argument extracted from the message
   */
  public Gwid getClobGwid( GenericMessage aMsg ) {
    return getArg( aMsg, ARGID_CLOB_GWID ).asValobj();
  }

}
