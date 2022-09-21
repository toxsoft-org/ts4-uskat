package org.toxsoft.uskat.base.gui.km5.sgw;

import static org.toxsoft.uskat.base.gui.km5.sgw.ISgwM5Constants.*;

import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.base.gui.km5.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * M5-model of the {@link ISkObjectService}.
 * <p>
 * This model is designed to display any {@link ISkObject}. No editing or enumeration is provided, rather use own
 * {@link IM5ItemsProvider}.
 *
 * @author hazard157
 */
public class SgwSkObject
    extends KM5ModelBasic<ISkObject> {

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SgwSkObject( ISkConnection aConn ) {
    super( MID_SGW_SK_OBJECT, ISkObject.class, aConn );
    addFieldDefs( SKID, CLASS_ID, STRID, NAME, DESCRIPTION );
  }

}
