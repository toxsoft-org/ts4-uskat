package org.toxsoft.uskat.core.backend.acl;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.users.acl.*;

/**
 * The means to determine access rights to the specified entity.
 *
 * @author hazard157
 * @param <T> - data type defining an entity
 */
public interface ISkAccessQualifier<T> {

  /**
   * Singleton of the qualifier denying access to everything..
   */
  @SuppressWarnings( "rawtypes" )
  ISkAccessQualifier ALL_DENY = aEntity -> ESkAccess.DENY;

  /**
   * Singleton of the qualifier granting read access to everything.
   */
  @SuppressWarnings( "rawtypes" )
  ISkAccessQualifier ALL_READ = aEntity -> ESkAccess.READ;

  /**
   * Singleton of the qualifier granting full access to everything.
   */
  @SuppressWarnings( "rawtypes" )
  ISkAccessQualifier ALL_WRITE = aEntity -> ESkAccess.WRITE;

  /**
   * Determines the access right to the entity.
   *
   * @param aEntity &lt;T&gt; - the key to an entity or an entity itself
   * @return {@link ESkAccess} - access right of the current user/role to the entity
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ESkAccess quilify( T aEntity );

}
