package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tslib.av.EAtomicType;
import org.toxsoft.core.tslib.bricks.events.msg.GenericMessage;
import org.toxsoft.core.tslib.bricks.events.msg.GtMessage;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.ISkHardConstants;
import org.toxsoft.uskat.core.api.cmdserv.DtoCommandStateChangeInfo;
import org.toxsoft.uskat.core.backend.ISkBackend;

/**
 * {@link ISkBackend} message builder: backend activity state change notification.
 *
 * @author mvk
 */
public class BackendMsgActiveChanged
    extends AbstractBackendMessageBuilder {

  /**
   * ID of the message.
   */
  public static final String MSG_ID = "ActiveChanged"; //$NON-NLS-1$

  /**
   * Singletone intance.
   */
  public static final BackendMsgActiveChanged INSTANCE = new BackendMsgActiveChanged();

  private static final String ARGID_ACTIVE = "Active"; //$NON-NLS-1$

  BackendMsgActiveChanged() {
    super( ISkHardConstants.SK_CORE_ID, MSG_ID );
    defineArgNonValobj( ARGID_ACTIVE, EAtomicType.BOOLEAN, true );
  }

  /**
   * Creates the message instance.
   *
   * @param aActive boolean - current state
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessage( boolean aActive ) {
    return makeMessageVarargs( ARGID_ACTIVE, avBool( aActive ) );
  }

  /**
   * Extracts {@link DtoCommandStateChangeInfo} argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link DtoCommandStateChangeInfo} - argument extracted from the message
   */
  public boolean getActive( GenericMessage aMsg ) {
    return getArg( aMsg, ARGID_ACTIVE ).asBool();
  }

}
