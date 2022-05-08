package org.toxsoft.uskat.core.api.clobserv;

import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;

/**
 * {@link ISkClobService} validator.
 *
 * @author hazard157
 */
public interface ISkClobServiceValidator {

  /**
   * Checks if CLOB could be written.
   *
   * @param aGwid {@link Gwid} - concrete GWID of the clob
   * @param aClob String - the CLOB content
   * @return {@link ValidationResult} - the validation result
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   */
  ValidationResult canWriteClob( Gwid aGwid, String aClob );

}
