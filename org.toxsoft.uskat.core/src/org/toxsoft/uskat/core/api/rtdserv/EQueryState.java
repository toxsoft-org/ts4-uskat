package org.toxsoft.uskat.core.api.rtdserv;

import static org.toxsoft.uskat.core.api.rtdserv.ISkResources.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Possible statesof the historic data query {@link ISkHistDataQuery}.
 *
 * @author hazard157
 */
public enum EQueryState
    implements IStridable {

  /**
   * Initial state immediately after creation.
   */
  UNPREPARED( "Unprepared", STR_D_UNPREPARED, STR_N_UNPREPARED ), //$NON-NLS-1$

  /**
   * State after call to {@link ISkHistDataQuery#prepare(IGwidList)}.
   */
  PREPARED( "Prepared", STR_D_PREPARED, STR_N_PREPARED ), //$NON-NLS-1$

  /**
   * State immediately after query start via {@link ISkHistDataQuery#exec(IQueryInterval)}.
   */
  EXECUTING( "Executing", STR_D_EXECUTING, STR_N_EXECUTING ), //$NON-NLS-1$

  /**
   * State after query finished successfuly and data (query result) is ready.
   */
  READY( "Ready", STR_D_READY, STR_N_READY ), //$NON-NLS-1$

  /**
   * Query execution failed, data no result was received.
   */
  FAILED( "Failed", STR_D_FAILED, STR_N_FAILED ), //$NON-NLS-1$

  /**
   * Query was clsed by {@link ISkHistDataQuery#close()} so instance is usless.
   */
  CLOSED( "Closed", STR_D_CLOSED, STR_N_CLOSED ); //$NON-NLS-1$

  /**
   * Registered keeepr ID.
   */
  public static final String KEEPER_ID = "ESkQueryState"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<EQueryState> KEEPER = new StridableEnumKeeper<>( EQueryState.class );

  private static IStridablesList<EQueryState> list = null;

  private final String id;
  private final String nmName;
  private final String description;

  /**
   * Constructor.
   *
   * @param aId String - identifier (IDpath)
   * @param aName - short name
   * @param aDescription String - description
   */
  EQueryState( String aId, String aName, String aDescription ) {
    id = aId;
    nmName = aName;
    description = aDescription;
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
   * Returns all constants as list.
   *
   * @return {@link IStridablesList}&lt;{@link EQueryState}&gt; - list of all constants
   */
  public static IStridablesList<EQueryState> asList() {
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
   * @return {@link EQueryState} - found constant or <code>null</code> there is no constant with specified identifier
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static EQueryState findById( String aId ) {
    return asList().findByKey( aId );
  }

  /**
   * Returns the constant by the identifier.
   *
   * @param aId String - identifier of the constant
   * @return {@link EQueryState} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException there is no constant with specified identifier
   */
  public static EQueryState getById( String aId ) {
    return asList().getByKey( aId );
  }

}