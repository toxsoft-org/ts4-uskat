package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.api.users.ability.*;

/**
 * {@link ISkRole} implementation.
 *
 * @author hazard157
 */
class SkRole
    extends SkObject
    implements ISkRole {

  static final ISkObjectCreator<SkRole> CREATOR = SkRole::new;

  SkRole( Skid aSkid ) {
    super( aSkid );
  }

  // ------------------------------------------------------------------------------------
  // ISkRole
  //

  @Override
  public IStridablesList<ISkAbility> listAllowedAbilities() {
    IList<ISkAbility> absList;
    // root is allowed everything!
    if( id().equals( ROLE_ID_ROOT ) ) {
      absList = coreApi().userService().abilityManager().listAbilities();
    }
    else {
      absList = getLinkObjs( LNKID_ROLE_ALLOWED_ABILITIES );
    }
    return new StridablesList<>( absList );
  }

}
