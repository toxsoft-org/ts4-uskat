package org.toxsoft.uskat.core.gui.utils.ugwi;

import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.utils.ugwi.*;
import org.toxsoft.uskat.core.utils.ugwi.kind.*;

/**
 * GUI helper for UGWI kind {@link UgwiKindGwid#KIND_ID}.
 *
 * @author hazard157
 */
public class UgwiKindGuiHelperGwid
    extends UgwiKindGuiHelper {

  /**
   * The singleton instance.
   */
  public static final IUgwiKindGuiHelper INSTANCE = new UgwiKindGuiHelperGwid();

  /**
   * Constructor.
   */
  public UgwiKindGuiHelperGwid() {
    super( UgwiKindGwid.KIND_ID );
  }

  // ------------------------------------------------------------------------------------
  // AbstractUgwiKindGuiHelper
  //

  // TODO create appropriate panels

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the new instance of {@link Gwid} denoted by the UGWI.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return {@link Gwid} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed {@link IUgwiKind#validateUgwi(Ugwi)}
   */
  public Gwid getGwid( Ugwi aUgwi ) {
    TsValidationFailedRtException.checkError( kind().validateUgwi( aUgwi ) );
    return Gwid.of( aUgwi.essence() );
  }

}
