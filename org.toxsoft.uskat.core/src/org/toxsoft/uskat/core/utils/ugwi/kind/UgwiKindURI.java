package org.toxsoft.uskat.core.utils.ugwi.kind;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.utils.ugwi.kind.ITsResources.*;

import java.net.*;

import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.utils.ugwi.*;

/**
 * The UGWI kind storing {@link URI} text representation.
 * <p>
 * Text representation {@link URI#toString()} is used to create the URI by the constructor {@link URI#URI(String)}.
 * <p>
 * Note: {@link Ugwi#namespace()} is not used for this kind.
 *
 * @author hazard157
 */
public class UgwiKindURI
    extends UgwiKind {

  /**
   * The registered kind ID.
   */
  public static final String KIND_ID = "uri"; //$NON-NLS-1$

  /**
   * The singleton instance.
   */
  public static final UgwiKind INSTANCE = new UgwiKindURI();

  /**
   * Constructor.
   */
  public UgwiKindURI() {
    super( KIND_ID, OptionSetUtils.createOpSet( //
        TSID_NAME, STR_UK_URI, //
        TSID_DESCRIPTION, STR_UK_URI_D //
    ) );
  }

  // ------------------------------------------------------------------------------------
  // AbstractUgwiKind
  //

  @Override
  protected ValidationResult doValidateEssence( String aEssence ) {
    try {
      @SuppressWarnings( "unused" )
      URI foo = new URI( aEssence );
      return ValidationResult.SUCCESS;
    }
    catch( URISyntaxException ex ) {
      return ValidationResult.error( FMT_ERR_INV_URI_FORMAT, ex.getLocalizedMessage() );
    }
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the new instance of {@link URI} denoted by the UGWI.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return {@link URI} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed {@link #validateUgwi(Ugwi)}
   */
  public URI getUri( Ugwi aUgwi ) {
    TsValidationFailedRtException.checkError( validateUgwi( aUgwi ) );
    try {
      return new URI( aUgwi.essence() );
    }
    catch( URISyntaxException ex ) {
      throw new TsIllegalArgumentRtException( ex );
    }
  }

}
