package org.toxsoft.uskat.core.gui.km5.sgw;

import static org.toxsoft.uskat.core.gui.km5.sgw.ISgwM5Constants.*;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * M5-model of the {@link IDtoEventInfo}.
 *
 * @author dima
 */
public class SgwDtoEventInfoM5Model
    extends SgwDtoPropInfoM5ModelBase<IDtoEventInfo> {

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SgwDtoEventInfoM5Model( ISkConnection aConn ) {
    super( MID_SGW_EVENT_INFO, IDtoEventInfo.class, aConn );
  }

}
