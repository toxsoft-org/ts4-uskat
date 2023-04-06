package org.toxsoft.uskat.core.gui.km5;

import org.toxsoft.core.tslib.gw.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * Internal M5-model for root class {@link IGwHardConstants#GW_ROOT_CLASS_ID}.
 *
 * @author hazard157
 */
final class KM5RootClassModel
    extends KM5ModelBasic<ISkObject> {

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public KM5RootClassModel( ISkConnection aConn ) {
    super( IGwHardConstants.GW_ROOT_CLASS_ID, ISkObject.class, aConn );
    addFieldDefs( SKID, CLASS_ID, STRID, NAME, DESCRIPTION );
  }

}
