package org.toxsoft.uskat.core.api.clobserv;

import java.io.*;

import org.toxsoft.core.tslib.bricks.events.*;
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
 * CLOBs may be of any length. However, backend implementation may add it's own limits on CLOB size, either on single
 * CLOB size or total size of all CLOBs.
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
   * <p>
   * Because Java {@link String} is limited up to {@link Integer#MAX_VALUE} symbols, for longer CLOBs
   * {@link #readClob(Gwid)} will throw an {@link TsIllegalStateRtException}.
   *
   * @param aGwid {@link Gwid} - concrete GWID of the clob
   * @return String - the CLOB content
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such CLOB
   * @throws TsIllegalStateRtException CLOB is too big for reading as {@link String}
   * @throws TsIllegalStateRtException CLOB has more than {@link Integer#MAX_VALUE} chars
   */
  String readClob( Gwid aGwid );

  /**
   * Writes the CLOB content.
   *
   * @param aGwid {@link Gwid} - concrete GWID of the clob
   * @param aClob String - the CLOB content
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such CLOB
   * @throws TsIllegalStateRtException CLOB is locked (either for reading or writing)
   */
  void writeClob( Gwid aGwid, String aClob );

  // ------------------------------------------------------------------------------------
  // Stream I/O API
  //

  /**
   * Returns the CLOB state information.
   *
   * @param aGwid {@link Gwid} - concrete GWID of the clob
   * @return {@link ISkClobState} - CLOB state
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such CLOB
   */
  ISkClobState clobState( Gwid aGwid );

  /**
   * Opens CLOB for stream reading.
   * <p>
   * Notes:
   * <ul>
   * <li>opening reader stream locks CLOB and makes it unaccessible for writing for all connections;</li>
   * <li>it is caller responsibility to call {@link Reader#close()}. CLOB will remain locked until {@link Reader} is
   * open;</li>
   * <li>closing connection will close all open {@link Reader} streams and release locks on CLOBs;</li>
   * </ul>
   *
   * @param aGwid {@link Gwid} - concrete GWID of the clob
   * @return {@link Reader} - CLOB reader
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such CLOB
   * @throws TsIllegalStateRtException CLOB is locked for writing
   */
  Reader openClobReader( Gwid aGwid );

  /**
   * Opens CLOB for writing.
   * <p>
   * Notes:
   * <ul>
   * <li>opening write stream locks CLOB and makes it unaccessible both for reading and writing for all
   * connections;</li>
   * <li>opening {@link Writer} does not clears CLOB content immediately. CLOB will be cleared on first actual writing
   * operation;</li>
   * <li>it is caller responsibility to call {@link Writer#close()}. CLOB will remain locked until {@link Writer} is
   * open;</li>
   * <li>closing connection will close all open {@link Writer} streams and release locks on CLOBs;</li>
   * </ul>
   *
   * @param aGwid {@link Gwid} - concrete GWID of the clob
   * @return {@link Writer} - CLOB writer
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such CLOB
   * @throws TsIllegalStateRtException CLOB is locked (either for reading or writing)
   */
  Writer openClobWriter( Gwid aGwid );

  /**
   * Returns the eventer for service notifications
   *
   * @return {@link ITsEventer}&lt;{@link ISkClobServiceListener}&gt; - the eventer
   */
  ITsEventer<ISkClobServiceListener> eventer();

}
