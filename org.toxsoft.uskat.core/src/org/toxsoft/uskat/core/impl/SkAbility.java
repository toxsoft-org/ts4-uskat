package org.toxsoft.uskat.core.impl;

import static org.toxsoft.uskat.core.api.users.ISkUserServiceHardConstants.*;

import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
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
    // TODO Auto-generated method stub
    throw new TsUnderDevelopmentRtException();
  }

  @Override
  public boolean isEnabled() {
    return attrs().getBool( ATRID_ABILITY_IS_ENABLED );
  }

}
