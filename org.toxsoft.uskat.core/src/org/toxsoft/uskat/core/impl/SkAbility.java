package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;

import org.toxsoft.core.tslib.coll.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.users.ability.*;

/**
 * {@link ISkAbility} implementation.
 *
 * @author hazard157
 */
public class SkAbility
    extends SkObject
    implements ISkAbility {

  static final ISkObjectCreator<SkAbility> CREATOR = SkAbility::new;

  SkAbility( Skid aSkid ) {
    super( aSkid );
  }

  // ------------------------------------------------------------------------------------
  // ISkAbilityKind
  //

  @Override
  public ISkAbilityKind kind() {
    IList<ISkAbilityKind> absList = getLinkRevObjs( CLSID_ABILITY_KIND, LNKID_ABILITIES_OF_KIND );
    ISkAbilityKind kind = absList.first();
    if( kind == null ) {
      kind = coreApi().objService().get( SKID_ABILITY_KIND_UNDEFINED );
    }
    return kind;
  }

}
