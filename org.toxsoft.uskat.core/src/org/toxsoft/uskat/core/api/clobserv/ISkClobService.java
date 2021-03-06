package org.toxsoft.uskat.core.api.clobserv;

import org.toxsoft.core.tslib.bricks.events.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.*;
import org.toxsoft.uskat.core.api.*;
import org.toxsoft.uskat.core.api.sysdescr.*;

/**
 * Core service: the CLOB (Character Large OBject) class properties management.
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
  String SERVICE_ID = ISkHardConstants.SK_CORE_SERVICE_ID_PREFIX + ".Clobs"; //$NON-NLS-1$

  /**
   * Reads the CLOB content.
   * <p>
   * By definition any valid GWID of kind {@link EGwidKind#GW_CLOB} has the content. If content never will be written by
   * {@link #writeClob(Gwid, String)}, this method will return an empty string as CLOB content.
   *
   * @param aGwid {@link Gwid} - concrete GWID of the clob
   * @return String - the CLOB content
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such CLOB (either in sysdescr or no such object)
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

  // ------------------------------------------------------------------------------------
  // Service support

  /**
   * Returns the service validator.
   *
   * @return {@link ITsValidationSupport}&lt;{@link ISkClobServiceValidator}&gt; - the service validator
   */
  ITsValidationSupport<ISkClobServiceValidator> svs();

  /**
   * Returns the service eventer.
   *
   * @return {@link ITsEventer}&lt;{@link ISkClobServiceListener}&gt; - the service eventer
   */
  ITsEventer<ISkClobServiceListener> eventer();

}
