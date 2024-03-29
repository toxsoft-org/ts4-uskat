package org.toxsoft.uskat.core.gui.km5.sgw;

import static org.toxsoft.core.tsgui.m5.IM5Constants.*;
import static org.toxsoft.uskat.core.gui.km5.sgw.ISgwM5Constants.*;

import org.toxsoft.core.tsgui.m5.model.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.objserv.*;
import org.toxsoft.uskat.core.connection.*;
import org.toxsoft.uskat.core.gui.km5.*;

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
    addFieldDefs( CLASS_ID, STRID, NAME, SKID, DESCRIPTION );
    SKID.addFlags( M5FF_COLUMN );
    DESCRIPTION.addFlags( M5FF_DETAIL );
  }

}
