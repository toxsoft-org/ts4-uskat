package org.toxsoft.uskat.core.backend.api;

import static org.toxsoft.core.tslib.av.impl.AvUtils.*;

import org.toxsoft.core.tslib.bricks.events.msg.*;
import org.toxsoft.core.tslib.bricks.strid.more.*;
import org.toxsoft.core.tslib.coll.helpers.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.devapi.gwiddb.*;

/**
 * {@link IBaGwidDb} message builder: database content changed.
 *
 * @author hazard157
 */
public class BaMsgGwidDbChanged
    extends AbstractBackendMessageBuilder {

  /**
   * ID of the message.
   */
  public static final String MSG_ID = "DbChanged"; //$NON-NLS-1$

  /**
   * Singletone instance.
   */
  public static final BaMsgGwidDbChanged BUILDER = new BaMsgGwidDbChanged();

  private static final String ARGID_SECTION_ID = "SectionId"; //$NON-NLS-1$
  private static final String ARGID_CRUD_OP    = "CrudOp";    //$NON-NLS-1$
  private static final String ARGID_KEY        = "Key";       //$NON-NLS-1$

  BaMsgGwidDbChanged() {
    super( ISkGwidDbService.SERVICE_ID, MSG_ID );
    defineArgValobj( ARGID_SECTION_ID, IdChain.KEEPER_ID, true );
    defineArgValobj( ARGID_CRUD_OP, ECrudOp.KEEPER_ID, true );
    defineArgValobj( ARGID_KEY, Gwid.KEEPER_ID, true );
  }

  /**
   * Creates the message instance.
   *
   * @param aSectionId {@link IdChain} - the ID of the section where the change happened
   * @param aOp {@link ECrudOp} - the change kind
   * @param aKey {@link Gwid} - the changed key or <code>null</code>
   * @return {@link GtMessage} - created instance of the message
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public GtMessage makeMessage( IdChain aSectionId, ECrudOp aOp, Gwid aKey ) {
    return makeMessageVarargs( //
        ARGID_SECTION_ID, avValobj( aSectionId ), //
        ARGID_CRUD_OP, avValobj( aOp ), //
        ARGID_KEY, avValobj( aKey ) //
    );
  }

  /**
   * Extracts "the section ID" argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link IdChain} - argument extracted from the message
   */
  public IdChain getSectionId( GenericMessage aMsg ) {
    return getArg( aMsg, ARGID_SECTION_ID ).asValobj();
  }

  /**
   * Extracts "CRUD operation" argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link ECrudOp} - argument extracted from the message
   */
  public ECrudOp getCrudOp( GenericMessage aMsg ) {
    return getArg( aMsg, ARGID_CRUD_OP ).asValobj();
  }

  /**
   * Extracts "the key" argument from the message.
   *
   * @param aMsg {@link GenericMessage} - the message
   * @return {@link Gwid} - argument extracted from the message
   */
  public Gwid getKey( GenericMessage aMsg ) {
    return getArg( aMsg, ARGID_KEY ).asValobj();
  }

}
