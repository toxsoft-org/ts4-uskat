package org.toxsoft.uskat.core.gui.utils.ugwi;

import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.utils.ugwi.*;
import org.toxsoft.uskat.core.utils.ugwi.kind.*;

/**
 * GUI helper for UGWI kind {@link UgwiKindSkid#KIND_ID}.
 *
 * @author hazard157
 */
public class UgwiKindGuiHelperSkid
    extends UgwiKindGuiHelper {

  /**
   * The singleton instance.
   */
  public static final IUgwiKindGuiHelper INSTANCE = new UgwiKindGuiHelperSkid();

  /**
   * Constructor.
   */
  public UgwiKindGuiHelperSkid() {
    super( UgwiKindSkid.KIND_ID );
  }

  // ------------------------------------------------------------------------------------
  // AbstractUgwiKindGuiHelper
  //

  // TODO create appropriate panels

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the new instance of {@link Skid} denoted by the UGWI.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return {@link Skid} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed {@link IUgwiKind#validateUgwi(Ugwi)}
   */
  public Skid getSkid( Ugwi aUgwi ) {
    TsValidationFailedRtException.checkError( kind().validateUgwi( aUgwi ) );
    return Skid.of( aUgwi.essence() );
  }

}
