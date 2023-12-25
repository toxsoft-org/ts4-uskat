package org.toxsoft.uskat.s5.server.backend.addons.queries;

import static org.toxsoft.uskat.s5.server.backend.addons.queries.IS5Resources.*;

import org.toxsoft.core.tslib.bricks.keeper.IEntityKeeper;
import org.toxsoft.core.tslib.bricks.keeper.std.StridableEnumKeeper;
import org.toxsoft.core.tslib.bricks.strid.IStridable;
import org.toxsoft.core.tslib.bricks.strid.coll.IStridablesList;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.StridablesList;
import org.toxsoft.core.tslib.bricks.time.IQueryInterval;
import org.toxsoft.core.tslib.utils.errors.TsItemNotFoundRtException;
import org.toxsoft.core.tslib.utils.errors.TsNullArgumentRtException;
import org.toxsoft.uskat.core.api.hqserv.ISkAsynchronousQuery;

/**
 * Possible states of the data query {@link ISkAsynchronousQuery}.
 *
 * @author hazard157
 */
public enum ES5QueriesConvoyState
    implements IStridable {

  /**
   * Initial state immediately after creation.
   */
  UNPREPARED( "Unprepared", STR_D_UNPREPARED, STR_N_UNPREPARED ), //$NON-NLS-1$

  /**
   * State after call to query preparation method in {@link ISkAsynchronousQuery} implementation.
   * <p>
   * Before query data for time period it is neccessary to specify query arguments - the data to be queried and
   * optionally the data processing rules. Specifying arguments will prepare query to be executed. Prepared query may be
   * excuted multiple time with different time intervals.
   * <p>
   * Preparation method signature (like results getters) depends on the query itself and will be implemented in
   * interfaces subclassed from {@link ISkAsynchronousQuery}.
   */
  PREPARED( "Prepared", STR_D_PREPARED, STR_N_PREPARED ), //$NON-NLS-1$

  /**
   * State immediately after query start via {@link ISkAsynchronousQuery#exec(IQueryInterval)}.
   * <p>
   * Generally, query receives portions of data from server when in state of executing.
   */
  EXECUTING( "Executing", STR_D_EXECUTING, STR_N_EXECUTING ), //$NON-NLS-1$

  /**
   * State after query finished successfuly and data (query result) is ready.
   * <p>
   * Result getter methids signature depends on the query itself and will be implemented in interfaces subclassed from
   * {@link ISkAsynchronousQuery}.
   */
  READY( "Ready", STR_D_READY, STR_N_READY ), //$NON-NLS-1$

  /**
   * Query execution failed, no result was received.
   */
  FAILED( "Failed", STR_D_FAILED, STR_N_FAILED ), //$NON-NLS-1$

  /**
   * Query was clsed by {@link ISkAsynchronousQuery#close()} so instance can'debug be executed or prepared again.
   * <p>
   * However if there were result data it is still accessible.
   */
  CLOSED( "Closed", STR_D_CLOSED, STR_N_CLOSED ); //$NON-NLS-1$

  /**
   * Registered keeepr ID.
   */
  public static final String KEEPER_ID = "ES5QueriesConvoyState"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<ES5QueriesConvoyState> KEEPER =
      new StridableEnumKeeper<>( ES5QueriesConvoyState.class );

  private static IStridablesList<ES5QueriesConvoyState> list = null;

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
  ES5QueriesConvoyState( String aId, String aName, String aDescription ) {
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
   * @return {@link IStridablesList}&lt;{@link ES5QueriesConvoyState}&gt; - list of all constants
   */
  public static IStridablesList<ES5QueriesConvoyState> asList() {
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
   * @return {@link ES5QueriesConvoyState} - found constant or <code>null</code> there is no constant with specified
   *         identifier
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static ES5QueriesConvoyState findById( String aId ) {
    return asList().findByKey( aId );
  }

  /**
   * Returns the constant by the identifier.
   *
   * @param aId String - identifier of the constant
   * @return {@link ES5QueriesConvoyState} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException there is no constant with specified identifier
   */
  public static ES5QueriesConvoyState getById( String aId ) {
    return asList().getByKey( aId );
  }

}
