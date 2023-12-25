package org.toxsoft.uskat.core.gui.km5.sgw;

import static org.toxsoft.uskat.core.gui.km5.sgw.ISgwM5Constants.*;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * M5-model of the {@link IDtoClobInfo}.
 *
 * @author hazard157
 */
public class SgwDtoClobInfoM5Model
    extends SgwDtoPropInfoM5ModelBase<IDtoClobInfo> {

  // public final IM5SingleModownFieldDef<IDtoClobInfo> DATA_TYPE = null;

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SgwDtoClobInfoM5Model( ISkConnection aConn ) {
    super( MID_SGW_CLOB_INFO, IDtoClobInfo.class, aConn );

    // TODO Auto-generated constructor stub
  }

}
