package org.toxsoft.uskat.core.api.clobserv;

import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * The CLOB state information.
 * <p>
 * Note: this is the snapshot of the CLOB state at the moment when method {@link ISkClobService#clobState(Gwid)} was
 * called.
 *
 * @author hazard157
 */
public interface ISkClobState {

  /**
   * Returns the number of characters in CLOB.
   * <p>
   * For CLOBs that were newer written returns 0.
   * <p>
   * Note: CLOBs may be of any length, however Java {@link String} is limited up to {@link Integer#MAX_VALUE} symbols.
   *
   * @return long - number of <code>char</code>s in CLOB
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsItemNotFoundRtException no such CLOB
   */
  long charsCount();

  /**
   * Returns the lock status.
   *
   * @return {@link ELockState} - CLOB lock status
   */
  ELockState lockState();

  /**
   * Returns the lock (if any) owner object identifier.
   *
   * @return {@link Skid} - owner object ID or {@link Skid#NONE} if CLOB is not locked
   */
  Skid lockOwner();

}
