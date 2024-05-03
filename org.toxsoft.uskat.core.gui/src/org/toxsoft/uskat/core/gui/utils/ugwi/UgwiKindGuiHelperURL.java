package org.toxsoft.uskat.core.gui.utils.ugwi;

import java.net.*;

import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.utils.ugwi.*;
import org.toxsoft.uskat.core.utils.ugwi.kind.*;

/**
 * GUI helper for UGWI kind {@link UgwiKindURL#KIND_ID}.
 *
 * @author hazard157
 */
public class UgwiKindGuiHelperURL
    extends UgwiKindGuiHelper {

  /**
   * The singleton instance.
   */
  public static final IUgwiKindGuiHelper INSTANCE = new UgwiKindGuiHelperURL();

  /**
   * Constructor.
   */
  public UgwiKindGuiHelperURL() {
    super( UgwiKindURL.KIND_ID );
  }

  // ------------------------------------------------------------------------------------
  // AbstractUgwiKindGuiHelper
  //

  // TODO create appropriate panels

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the new instance of {@link URL} denoted by the UGWI.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return {@link URL} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed {@link IUgwiKind#validateUgwi(Ugwi)}
   */
  public URL getUrl( Ugwi aUgwi ) {
    TsValidationFailedRtException.checkError( kind().validateUgwi( aUgwi ) );
    try {
      return new URL( aUgwi.essence() );
    }
    catch( MalformedURLException ex ) {
      throw new TsIllegalArgumentRtException( ex );
    }
  }

}
