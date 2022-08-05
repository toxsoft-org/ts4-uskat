package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.users.*;

/**
 * {@link ISkUser} implementation.
 *
 * @author hazard157
 */
class SkUser
    extends SkObject
    implements ISkUser {

  static final ISkObjectCreator<SkUser> CREATOR = SkUser::new;

  SkUser( Skid aSkid ) {
    super( aSkid );
  }

  // ------------------------------------------------------------------------------------
  // ISkUser
  //

  @Override
  public IStridablesList<ISkRole> listRoles() {
    return new StridablesList<>( getLinkObjs( LNKID_USER_ROLES ) );
  }

}
