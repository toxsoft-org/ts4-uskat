package org.toxsoft.uskat.core.api.clobserv;

import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * The CLOB (Character Large OBject) service.
 * <p>
 * CLOB are class properties defined in Sysdescr method {@link ISkClassInfo#clobs()}. CLOB values are handled by this
 * service.
 * <p>
 * This API limits CLOB size up to 2GB (this is Java String limitation). However, backend implementation may add it's
 * own limits on CLOB size, either on single CLOB size or total size of all CLOBs. Reading/writing CLOBs bigger than
 * implementation limits will cause an exception.
 *
 * @author hazard157
 */
public interface ISkClobService
    extends ISkService {

  /**
   * The service ID.
   */
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".CLOBs"; //$NON-NLS-1$

  /**
   * Reads the CLOB content.
   *
   * @param aGwid {@link Gwid} - concrete GWID of the clob
   * @return String - the CLOB content
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such CLOB
   * @throws TsIllegalStateRtException CLOB is too big for reading
   */
  String readClob( Gwid aGwid );

  /**
   * Writes the CLOB content.
   *
   * @param aGwid {@link Gwid} - concrete GWID of the clob
   * @param aClob String - the CLOB content
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such CLOB
   * @throws TsIllegalArgumentRtException CLOB is too big for reading
   */
  void writeClob( Gwid aGwid, String aClob );

  // TODO CLOB validator

}
