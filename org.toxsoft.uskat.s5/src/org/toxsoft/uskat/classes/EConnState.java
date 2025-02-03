package org.toxsoft.uskat.classes;

import static org.toxsoft.uskat.classes.IS5Resources.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * {@link ISkNetNode} connection state. Used to diagnose network or hardware problems.
 *
 * @author dima
 * @author mvk
 */
public enum EConnState
    implements IStridable {

  /**
   * hardware offline .
   */
  OFFLINE( "offLine", STR_OFFLINE, STR_OFFLINE_D ), //$NON-NLS-1$

  /**
   * hardware online.
   */
  ONLINE( "onLine", STR_ONLINE, STR_ONLINE_D ), //$NON-NLS-1$

  /**
   * state of connection is unknown.
   */
  UNKNOWN( "unknown", STR_UNKNOWN, STR_UNKNOWN_D ), //$NON-NLS-1$

  ;

  /**
   * The keeper ID.
   */
  public static final String KEEPER_ID = "EConnState"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<EConnState> KEEPER = new StridableEnumKeeper<>( EConnState.class );

  private static IStridablesListEdit<EConnState> list = null;

  private final String id;
  private final String name;
  private final String description;

  EConnState( String aId, String aName, String aDescription ) {
    id = aId;
    name = aName;
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
    return name;
  }

  @Override
  public String description() {
    return description;
  }

  // ----------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns all constants in single list.
   *
   * @return {@link IStridablesList}&lt; {@link EConnState} &gt; - list of constants in order of declaraion
   */
  public static IStridablesList<EConnState> asList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list;
  }

  /**
   * Returns the constant by the ID.
   *
   * @param aId String - the ID
   * @return {@link EConnState} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no constant found by specified ID
   */
  public static EConnState getById( String aId ) {
    return asList().getByKey( aId );
  }

  /**
   * Finds the constant by the name.
   *
   * @param aName String - the name
   * @return {@link EConnState} - found constant or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static EConnState findByName( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    for( EConnState item : values() ) {
      if( item.name.equals( aName ) ) {
        return item;
      }
    }
    return null;
  }

  /**
   * Returns the constant by the name.
   *
   * @param aName String - the name
   * @return {@link EConnState} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no constant found by specified name
   */
  public static EConnState getByName( String aName ) {
    return TsItemNotFoundRtException.checkNull( findByName( aName ) );
  }

}
