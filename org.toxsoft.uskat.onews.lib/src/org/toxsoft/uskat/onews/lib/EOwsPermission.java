package org.toxsoft.uskat.onews.lib;

import static org.toxsoft.uskat.onews.lib.ITsResources.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Permission to access component (ability).
 *
 * @author hazard157
 */
@SuppressWarnings( "javadoc" )
public enum EOwsPermission
    implements IStridable {

  DENY( "deny", STR_N_OP_DENY, STR_D_OP_DENY ), //$NON-NLS-1$

  ALLOW( "allow", STR_N_OP_ALLOW, STR_D_OP_ALLOW ), //$NON-NLS-1$

  ;

  /**
   * The keeper ID.
   */
  public static final String KEEPER_ID = "EOwsPermission"; //$NON-NLS-1$

  /**
   * Keeper singleton.
   */
  public static final IEntityKeeper<EOwsPermission> KEEPER = new StridableEnumKeeper<>( EOwsPermission.class );

  private static IStridablesListEdit<EOwsPermission> list = null;

  private final String id;
  private final String name;
  private final String description;

  EOwsPermission( String aId, String aName, String aDescription ) {
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
   * @return {@link IStridablesList}&lt; {@link EOwsPermission} &gt; - list of constants in order of declaraion
   */
  public static IStridablesList<EOwsPermission> asList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list;
  }

  /**
   * Returns the constant by the ID.
   *
   * @param aId String - the ID
   * @return {@link EOwsPermission} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no constant found by specified ID
   */
  public static EOwsPermission getById( String aId ) {
    return asList().getByKey( aId );
  }

  /**
   * Finds the constant by the name.
   *
   * @param aName String - the name
   * @return {@link EOwsPermission} - found constant or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static EOwsPermission findByName( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    for( EOwsPermission item : values() ) {
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
   * @return {@link EOwsPermission} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no constant found by specified name
   */
  public static EOwsPermission getByName( String aName ) {
    return TsItemNotFoundRtException.checkNull( findByName( aName ) );
  }

}
