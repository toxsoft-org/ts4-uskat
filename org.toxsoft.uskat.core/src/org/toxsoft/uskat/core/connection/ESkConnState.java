package org.toxsoft.uskat.core.connection;

import static org.toxsoft.uskat.core.connection.ISkResources.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.AbstractEntityKeeper.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.strio.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.core.tslib.utils.valobj.*;

/**
 * Some enumeration.
 *
 * @author hazard157
 */
public enum ESkConnState
    implements IStridable {

  /**
   * Connection is closed (was not opened or already closed).
   */
  CLOSED( "Closed", STR_N_CLOSED, STR_D_CLOSED, false, false ), //$NON-NLS-1$

  /**
   * Connection is open but temporary there is no contact with the server.
   */
  INACTIVE( "Inactive", STR_N_INACTIVE, STR_D_INACTIVE, true, false ), //$NON-NLS-1$

  /**
   * Connection is open and active.
   */
  ACTIVE( "Active", STR_N_ACTIVE, STR_D_ACTIVE, true, true ); //$NON-NLS-1$

  /**
   * Идентификатор регистрации хранителя {@link #KEEPER} в реестре {@link TsValobjUtils}.
   */
  public static final String KEEPER_ID = "ESkConnState"; //$NON-NLS-1$

  /**
   * Синглтон хранителя.
   */
  public static final IEntityKeeper<ESkConnState> KEEPER =
      new AbstractEntityKeeper<>( ESkConnState.class, EEncloseMode.ENCLOSES_BASE_CLASS, null ) {

        @Override
        protected void doWrite( IStrioWriter aSw, ESkConnState aEntity ) {
          aSw.writeQuotedString( aEntity.id() );
        }

        @Override
        protected ESkConnState doRead( IStrioReader aSr ) {
          return ESkConnState.findById( aSr.readQuotedString() );
        }
      };

  private static IStridablesList<ESkConnState> list = null;

  private final String  id;
  private final String  nmName;
  private final String  description;
  private final boolean open;
  private final boolean active;

  /**
   * Constructor.
   *
   * @param aId String - identifier (IDpath)
   * @param aName - short name
   * @param aDescription String - description
   * @param aOpen boolean - <code>true</code> if connection is open now but maybe contact is lost with the server
   * @param aActive boolean - <code>true</code> if connection is open and communicating with the server now
   */
  ESkConnState( String aId, String aName, String aDescription, boolean aOpen, boolean aActive ) {
    id = aId;
    nmName = aName;
    description = aDescription;
    open = aOpen;
    active = aActive;
  }

  // --------------------------------------------------------------------------
  // IStridable
  //

  @Override
  public String id() {
    return id;
  }

  @Override
  public String nmName() {
    return nmName;
  }

  @Override
  public String description() {
    return description;
  }

  // ----------------------------------------------------------------------------------
  // Additional API
  //

  /**
   * Determines if connection is open.
   *
   * @return boolean - <code>true</code> if connection is open now but maybe contact is lost with the server
   */
  public boolean isOpen() {
    return open;
  }

  /**
   * Determines if there is the contact with the server right now.
   *
   * @return boolean - <code>true</code> if connection is open and communicating with the server now
   */
  public boolean isActive() {
    return active;
  }

  /**
   * Returns all constants as list.
   *
   * @return {@link IStridablesList}&lt;{@link ESkConnState}&gt; - list of all constants
   */
  public static IStridablesList<ESkConnState> list() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list;
  }

  // ----------------------------------------------------------------------------------
  // Find and get
  //

  /**
   * Finds the constant by the identifier.
   *
   * @param aId String - identifier of the constant
   * @return {@link ESkConnState} - found constant or <code>null</code> there is no constant with specified identifier
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static ESkConnState findById( String aId ) {
    TsNullArgumentRtException.checkNull( aId );
    for( ESkConnState item : list() ) {
      if( item.id.equals( aId ) ) {
        return item;
      }
    }
    return null;
  }

  /**
   * Returns the constant by the identifier.
   *
   * @param aId String - identifier of the constant
   * @return {@link ESkConnState} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException there is no constant with specified identifier
   */
  public static ESkConnState getById( String aId ) {
    return TsItemNotFoundRtException.checkNull( findById( aId ) );
  }

}
