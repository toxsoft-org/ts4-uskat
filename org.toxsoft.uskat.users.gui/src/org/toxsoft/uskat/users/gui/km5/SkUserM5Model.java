package org.toxsoft.uskat.users.gui.km5;

import org.toxsoft.uskat.base.gui.km5.*;
import org.toxsoft.uskat.core.api.users.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * M5-model of {@link ISkUser}.
 *
 * @author hazard157
 */
class SkUserM5Model
    extends KM5ModelBasic<ISkUser> {

  public SkUserM5Model( ISkConnection aConn ) {
    super( ISkUser.CLASS_ID, ISkUser.class, aConn );
    // TODO Auto-generated constructor stub
  }

}
