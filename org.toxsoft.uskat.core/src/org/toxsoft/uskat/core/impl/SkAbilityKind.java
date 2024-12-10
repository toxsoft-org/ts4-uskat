package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;

import org.toxsoft.core.tslib.bricks.strid.coll.*;
import org.toxsoft.core.tslib.bricks.strid.coll.impl.*;
import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.users.ability.*;

/**
 * {@link ISkAbilityKind} implementation.
 *
 * @author hazard157
 */
public class SkAbilityKind
    extends SkObject
    implements ISkAbilityKind {

  static final ISkObjectCreator<SkAbilityKind> CREATOR = SkAbilityKind::new;

  SkAbilityKind( Skid aSkid ) {
    super( aSkid );
  }

  // ------------------------------------------------------------------------------------
  // ISkAbilityKind
  //

  @Override
  public IStridablesList<ISkAbility> listAbilities() {
    IList<ISkAbility> objs = getLinkObjs( LNKID_ABILITIES_OF_KIND );
    return new StridablesList<>( objs );
  }

}
