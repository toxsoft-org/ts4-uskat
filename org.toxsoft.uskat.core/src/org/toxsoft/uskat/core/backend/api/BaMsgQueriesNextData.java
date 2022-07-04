package org.toxsoft.uskat.core.backend.api;

import org.toxsoft.uskat.core.api.cmdserv.*;

/**
 * {@link IBaQueries} message builder: next portion of data delivered from backend to frontend.
 *
 * @author hazard157
 */
public class BaMsgQueriesNextData
    extends AbstractBackendMessageBuilder {

  /**
   * ID of the message.
   */
  public static final String MSG_ID = "NextData"; //$NON-NLS-1$

  /**
   * Singletone intance.
   */
  public static final BaMsgQueriesNextData INSTANCE = new BaMsgQueriesNextData();

  public static final String ARGID_IS_FINISHED = "isFinished"; //$NON-NLS-1$

  // private static final String ARGID_XXX = "Xxx"; //$NON-NLS-1$

  BaMsgQueriesNextData() {
    super( ISkCommandService.SERVICE_ID, MSG_ID );
    // FIXME defineArgValobj( ARGID_XXX, ???.KEEPER_ID, true );
  }

  // FIXME
  // /**
  // * Creates the message instance.
  // *
  // * @param aStateChangeInfo {@link IDtoCommand} - state change info
  // * @return {@link GtMessage} - created instance of the message
  // * @throws TsNullArgumentRtException any argument = <code>null</code>
  // */
  // public GtMessage makeMessage( DtoCommandStateChangeInfo aStateChangeInfo ) {
  // return makeMessageVarargs( ARGID_XXX, avValobj( aStateChangeInfo ) );
  // }
  //
  // /**
  // * Extracts {@link DtoCommandStateChangeInfo} argument from the message.
  // *
  // * @param aMsg {@link GenericMessage} - the message
  // * @return {@link DtoCommandStateChangeInfo} - argument extracted from the message
  // */
  // public DtoCommandStateChangeInfo getStateChangeInfo( GenericMessage aMsg ) {
  // return getArg( aMsg, ARGID_XXX ).asValobj();
  // }

}
