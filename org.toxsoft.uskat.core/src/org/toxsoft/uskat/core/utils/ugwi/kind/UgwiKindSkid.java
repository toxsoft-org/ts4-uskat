package org.toxsoft.uskat.core.utils.ugwi.kind;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.utils.ugwi.kind.ITsResources.*;

import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.gw.skid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.utils.ugwi.*;

/**
 * The UGWI kind storing {@link Skid} text representation.
 * <p>
 * The essence of UGWI is the GWID canonical string {@link Skid#canonicalString()}.
 * <p>
 * Note: {@link Ugwi#namespace()} is not used for this kind.
 *
 * @author hazard157
 */
public class UgwiKindSkid
    extends UgwiKind {

  /**
   * The registered kind ID.
   */
  public static final String KIND_ID = "skid"; //$NON-NLS-1$

  /**
   * The singleton instance.
   */
  public static final IUgwiKind INSTANCE = new UgwiKindSkid();

  /**
   * Constructor.
   */
  public UgwiKindSkid() {
    super( KIND_ID, OptionSetUtils.createOpSet( //
        TSID_NAME, STR_UK_SKID, //
        TSID_DESCRIPTION, STR_UK_SKID_D //
    ) );
  }

  // ------------------------------------------------------------------------------------
  // AbstractUgwiKind
  //

  @Override
  protected ValidationResult doValidateEssence( String aEssence ) {
    try {
      @SuppressWarnings( "unused" )
      Skid foo = Skid.of( aEssence );
      return ValidationResult.SUCCESS;
    }
    catch( Exception ex ) {
      return ValidationResult.error( FMT_ERR_INV_SKID_FORMAT, ex.getLocalizedMessage() );
    }
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the new instance of {@link Skid} denoted by the UGWI.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return {@link Skid} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed {@link #validateUgwi(Ugwi)}
   */
  public Skid getSkid( Ugwi aUgwi ) {
    TsValidationFailedRtException.checkError( validateUgwi( aUgwi ) );
    return Skid.of( aUgwi.essence() );
  }

}
