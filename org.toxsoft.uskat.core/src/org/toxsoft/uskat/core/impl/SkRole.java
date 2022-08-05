package org.toxsoft.uskat.core.impl;

import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.api.users.*;

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

}
