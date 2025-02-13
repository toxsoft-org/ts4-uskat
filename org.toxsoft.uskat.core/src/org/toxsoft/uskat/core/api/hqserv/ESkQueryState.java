package org.toxsoft.uskat.core.api.hqserv;

import static org.toxsoft.uskat.core.api.hqserv.ISkResources.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.bricks.time.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Possible states of the data query {@link ISkAsynchronousQuery}.
 *
 * @author hazard157
 */
public enum ESkQueryState
    implements IStridable {

  /**
   * Initial state immediately after creation.
   */
  UNPREPARED( "Unprepared", STR_UNPREPARED_D, STR_UNPREPARED ), //$NON-NLS-1$

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
  PREPARED( "Prepared", STR_PREPARED_D, STR_PREPARED ), //$NON-NLS-1$

  /**
   * State immediately after query start via {@link ISkAsynchronousQuery#exec(IQueryInterval)}.
   * <p>
   * Generally, query receives portions of data from server when in state of executing.
   */
  EXECUTING( "Executing", STR_EXECUTING_D, STR_EXECUTING ), //$NON-NLS-1$

  /**
   * State after query finished successfuly and data (query result) is ready.
   * <p>
   * Result getter methids signature depends on the query itself and will be implemented in interfaces subclassed from
   * {@link ISkAsynchronousQuery}.
   */
  READY( "Ready", STR_READY_D, STR_READY ), //$NON-NLS-1$

  /**
   * Query execution failed, no result was received.
   */
  FAILED( "Failed", STR_FAILED_D, STR_FAILED ), //$NON-NLS-1$

  /**
   * Query was clsed by {@link ISkAsynchronousQuery#close()} so instance can't be executed or prepared again.
   * <p>
   * However if there were result data it is still accessible.
   */
  CLOSED( "Closed", STR_CLOSED_D, STR_CLOSED ); //$NON-NLS-1$

  /**
   * Registered keeepr ID.
   */
  public static final String KEEPER_ID = "ESkQueryState"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<ESkQueryState> KEEPER = new StridableEnumKeeper<>( ESkQueryState.class );

  private static IStridablesList<ESkQueryState> list = null;

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
  ESkQueryState( String aId, String aName, String aDescription ) {
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
   * @return {@link IStridablesList}&lt;{@link ESkQueryState}&gt; - list of all constants
   */
  public static IStridablesList<ESkQueryState> asList() {
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
   * @return {@link ESkQueryState} - found constant or <code>null</code> there is no constant with specified identifier
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static ESkQueryState findById( String aId ) {
    return asList().findByKey( aId );
  }

  /**
   * Returns the constant by the identifier.
   *
   * @param aId String - identifier of the constant
   * @return {@link ESkQueryState} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException there is no constant with specified identifier
   */
  public static ESkQueryState getById( String aId ) {
    return asList().getByKey( aId );
  }

}
