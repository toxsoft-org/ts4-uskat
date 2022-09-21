package org.toxsoft.uskat.base.gui.km5.sgw;

import static org.toxsoft.uskat.base.gui.km5.sgw.ISgwM5Constants.*;

import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.api.sysdescr.dto.*;
import org.toxsoft.uskat.core.connection.*;

/**
 * M5-model of the {@link IDtoAttrInfo}.
 *
 * @author hazard157
 */
public class SgwDtoAttrInfoM5Model
    extends SgwDtoPropInfoM5ModelBase<IDtoAttrInfo> {

  // public final IM5AttributeFieldDef<T>

  /**
   * Constructor.
   *
   * @param aConn {@link ISkConnection} - Sk-connection to be used in constructor
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  public SgwDtoAttrInfoM5Model( ISkConnection aConn ) {
    super( MID_SGW_ATTR_INFO, IDtoAttrInfo.class, aConn );

    // TODO Auto-generated constructor stub
  }

}
