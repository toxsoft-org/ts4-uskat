package org.toxsoft.uskat.s5.server.sequences.maintenance;

import static org.toxsoft.uskat.s5.server.sequences.maintenance.IS5Resources.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.s5.utils.*;

/**
 * Supported database engines.
 *
 * @author mvk
 */
public enum ES5DatabaseEngine
    implements IStridable {

  /**
   * Mariadb.
   */
  MARIADB( "MariaDB", STR_D_MARIADB, STR_N_MARIADB ), //$NON-NLS-1$

  /**
   * MysSQL
   */
  MYSQL( "MySQL", STR_D_MARIADB, STR_N_MARIADB ), //$NON-NLS-1$

  /**
   * PostgreSQL
   */
  POSTGRESQL( "PostgreSQL", STR_D_POSTGRESQL, STR_N_POSTGRESQL ); //$NON-NLS-1$

  /**
   * Идентификатор хранителя для {@link S5ValobjUtils}.
   */
  public static final String KEEPER_ID = "DatabaseEngine"; //$NON-NLS-1$

  /**
   * Синглтон хранителя.
   */
  public static final IEntityKeeper<ES5DatabaseEngine> KEEPER = new StridableEnumKeeper<>( ES5DatabaseEngine.class );

  private static IStridablesList<ES5DatabaseEngine> list = null;

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
  ES5DatabaseEngine( String aId, String aName, String aDescription ) {
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
   * @return {@link IStridablesList}&lt;{@link ES5DatabaseEngine}&gt; - list of all constants
   */
  public static IStridablesList<ES5DatabaseEngine> asList() {
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
   * @return {@link ES5DatabaseEngine} - found constant or <code>null</code> there is no constant with specified
   *         identifier
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static ES5DatabaseEngine findById( String aId ) {
    return asList().findByKey( aId );
  }

  /**
   * Returns the constant by the identifier.
   *
   * @param aId String - identifier of the constant
   * @return {@link ES5DatabaseEngine} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException there is no constant with specified identifier
   */
  public static ES5DatabaseEngine getById( String aId ) {
    return asList().getByKey( aId );
  }

}
