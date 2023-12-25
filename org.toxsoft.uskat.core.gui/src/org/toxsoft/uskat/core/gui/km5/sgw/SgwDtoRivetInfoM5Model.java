package org.toxsoft.uskat.core.gui.km5.sgw;

import static org.toxsoft.uskat.core.gui.km5.sgw.ISgwM5Constants.*;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * M5-model of the {@link IDtoRivetInfo}.
 *
 * @author hazard157
 */
public class SgwDtoRivetInfoM5Model
    extends SgwDtoPropInfoM5ModelBase<IDtoRivetInfo> {

  // public final IM5SingleModownFieldDef<IDtoRivetInfo> DATA_TYPE = null;

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SgwDtoRivetInfoM5Model( ISkConnection aConn ) {
    super( MID_SGW_RIVET_INFO, IDtoRivetInfo.class, aConn );

    // TODO Auto-generated constructor stub
  }

}
