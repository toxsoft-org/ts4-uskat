package org.toxsoft.uskat.core.gui.utils.ugwi;

import java.net.*;

import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.utils.ugwi.*;
import org.toxsoft.uskat.core.utils.ugwi.kind.*;

/**
 * GUI helper for UGWI kind {@link UgwiKindURI#KIND_ID}.
 *
 * @author hazard157
 */
public class UgwiKindGuiHelperURI
    extends UgwiKindGuiHelper {

  /**
   * The singleton instance.
   */
  public static final IUgwiKindGuiHelper INSTANCE = new UgwiKindGuiHelperURI();

  /**
   * Constructor.
   */
  public UgwiKindGuiHelperURI() {
    super( UgwiKindURI.KIND_ID );
  }

  // ------------------------------------------------------------------------------------
  // AbstractUgwiKindGuiHelper
  //

  // TODO create appropriate panels

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the new instance of {@link URI} denoted by the UGWI.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return {@link URI} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed {@link IUgwiKind#validateUgwi(Ugwi)}
   */
  public URI getUri( Ugwi aUgwi ) {
    TsValidationFailedRtException.checkError( kind().validateUgwi( aUgwi ) );
    try {
      return new URI( aUgwi.essence() );
    }
    catch( URISyntaxException ex ) {
      throw new TsIllegalArgumentRtException( ex );
    }
  }

}
