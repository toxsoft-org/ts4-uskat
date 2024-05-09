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
 * The UGWI kind storing {@link URL} text representation.
 * <p>
 * Text representation {@link URL#toString()} is used to create the URL by the constructor {@link URL#URL(String)}.
 * <p>
 * Note: {@link Ugwi#namespace()} is not used for this kind.
 *
 * @author hazard157
 */
public class UgwiKindURL
    extends UgwiKind {

  /**
   * The registered kind ID.
   */
  public static final String KIND_ID = "url"; //$NON-NLS-1$

  /**
   * The singleton instance.
   */
  public static final UgwiKind INSTANCE = new UgwiKindURL();

  /**
   * Constructor.
   */
  public UgwiKindURL() {
    super( KIND_ID, OptionSetUtils.createOpSet( //
        TSID_NAME, STR_UK_URL, //
        TSID_DESCRIPTION, STR_UK_URL_D //
    ) );
  }

  // ------------------------------------------------------------------------------------
  // AbstractUgwiKind
  //

  @Override
  protected ValidationResult doValidateEssence( String aEssence ) {
    try {
      @SuppressWarnings( "unused" )
      URL foo = new URL( aEssence );
      return ValidationResult.SUCCESS;
    }
    catch( MalformedURLException ex ) {
      return ValidationResult.error( FMT_ERR_INV_URL_FORMAT, ex.getLocalizedMessage() );
    }
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the new instance of {@link URL} denoted by the UGWI.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return {@link URL} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed {@link #validateUgwi(Ugwi)}
   */
  public URL getUrl( Ugwi aUgwi ) {
    TsValidationFailedRtException.checkError( validateUgwi( aUgwi ) );
    try {
      return new URL( aUgwi.essence() );
    }
    catch( MalformedURLException ex ) {
      throw new TsIllegalArgumentRtException( ex );
    }
  }

}
