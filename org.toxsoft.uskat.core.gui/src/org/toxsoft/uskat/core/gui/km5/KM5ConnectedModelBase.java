package org.toxsoft.uskat.core.gui.km5;

import org.toxsoft.core.tsgui.m5.model.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.utils.*;

/**
 * Simple extension over {@link M5Model} adding {@link ISkConnection} in the constructor.
 * <p>
 * This class implements {@link ISkConnected} interface
 *
 * @author hazard157
 * @param <T> - modelled entity type
 */
public class KM5ConnectedModelBase<T>
    extends M5Model<T>
    implements ISkConnected {

  private final ISkConnection conn;

  /**
   * Constructor.
   *
   * @param aId String - the model ID mostly the same as {@link ISkObject#classId()}
   * @param aModelledClass {@link Class} - concrete java type of objects
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public KM5ConnectedModelBase( String aId, Class<T> aModelledClass, ISkConnection aConn ) {
    super( aId, aModelledClass );
    conn = TsNullArgumentRtException.checkNull( aConn );
  }

  // ------------------------------------------------------------------------------------
  // ISkConnected
  //

  @Override
  public ISkConnection skConn() {
    return conn;
  }

}
