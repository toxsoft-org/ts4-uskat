package org.toxsoft.uskat.core.api.users.acl;

import static org.toxsoft.uskat.core.api.users.acl.ITmpResources.*;

import org.toxsoft.core.tslib.bricks.keeper.*;
import org.toxsoft.core.tslib.bricks.keeper.std.*;
import org.toxsoft.core.tslib.bricks.strid.*;
import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * Access rights to the USkat entities.
 * <p>
 * The constants are specified in the order of rights growth. For example {@link #WRITE} rights are "more" rights then
 * {@link #DENY}, or {@link #READ} is "less" than {@link #WRITE}.
 *
 * @author hazard157
 */
public enum ESkAccess
    implements IStridable {

  /**
   * Access is denied, the entity is not visible to the user.
   */
  DENY( "deny", STR_SK_ACCESS_DENY, STR_SK_ACCESS_DENY_D, false, false ), //$NON-NLS-1$

  /**
   * Entity is visible, value may be read but not modified.
   */
  READ( "read", STR_SK_ACCESS_READ, STR_SK_ACCESS_READ_D, true, false ), //$NON-NLS-1$

  /**
   * Entity is visible, user has full access to read and modify.
   */
  WRITE( "write", STR_SK_ACCESS_WRITE, STR_SK_ACCESS_WRITE_D, true, true ); //$NON-NLS-1$

  /**
   * The registered keeper ID.
   */
  public static final String KEEPER_ID = "ESkAccess"; //$NON-NLS-1$

  /**
   * The keeper singleton.
   */
  public static final IEntityKeeper<ESkAccess> KEEPER = new StridableEnumKeeper<>( ESkAccess.class );

  private static IStridablesListEdit<ESkAccess> list = null;

  private final String  id;
  private final String  name;
  private final String  description;
  private final boolean isRead;
  private final boolean isWrite;

  ESkAccess( String aId, String aName, String aDescription, boolean aCanRead, boolean aCanWrite ) {
    id = aId;
    name = aName;
    description = aDescription;
    isRead = aCanRead;
    isWrite = aCanWrite;
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

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Determines if this constant allows access to the entity.
   *
   * @return boolean - <code>true</code> if access is granted
   */
  public boolean canRead() {
    return isRead;
  }

  /**
   * Determines if this constant allows modification of the entity.
   * <p>
   * Modification rights always includes access (read) rights.
   *
   * @return boolean - <code>true</code> if modification is enabled
   */
  public boolean canWrite() {
    return isWrite;
  }

  // ----------------------------------------------------------------------------------
  // Stridable enum common API
  //

  /**
   * Returns all constants in single list.
   *
   * @return {@link IStridablesList}&lt; {@link ESkAccess} &gt; - list of constants in order of declaraion
   */
  public static IStridablesList<ESkAccess> asList() {
    if( list == null ) {
      list = new StridablesList<>( values() );
    }
    return list;
  }

  /**
   * Returns the constant by the ID.
   *
   * @param aId String - the ID
   * @return {@link ESkAccess} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no constant found by specified ID
   */
  public static ESkAccess getById( String aId ) {
    return asList().getByKey( aId );
  }

  /**
   * Finds the constant by the name.
   *
   * @param aName String - the name
   * @return {@link ESkAccess} - found constant or <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public static ESkAccess findByName( String aName ) {
    TsNullArgumentRtException.checkNull( aName );
    for( ESkAccess item : values() ) {
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
   * @return {@link ESkAccess} - found constant
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no constant found by specified name
   */
  public static ESkAccess getByName( String aName ) {
    return TsItemNotFoundRtException.checkNull( findByName( aName ) );
  }

}
