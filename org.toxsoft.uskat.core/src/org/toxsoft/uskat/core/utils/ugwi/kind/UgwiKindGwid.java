package org.toxsoft.uskat.core.utils.ugwi.kind;

import static org.toxsoft.core.tslib.av.metainfo.IAvMetaConstants.*;
import static org.toxsoft.uskat.core.utils.ugwi.kind.ITsResources.*;

import org.toxsoft.core.tslib.av.opset.impl.*;
import org.toxsoft.core.tslib.bricks.validator.*;
import org.toxsoft.core.tslib.bricks.validator.impl.*;
import org.toxsoft.core.tslib.gw.gwid.*;
import org.toxsoft.core.tslib.utils.errors.*;
import org.toxsoft.uskat.core.utils.ugwi.*;

/**
 * The UGWI kind storing {@link Gwid} text representation.
 * <p>
 * The essence of UGWI is the GWID canonical string {@link Gwid#canonicalString()}.
 * <p>
 * Note: {@link Ugwi#namespace()} is not used for this kind.
 *
 * @author hazard157
 */
public class UgwiKindGwid
    extends UgwiKind {

  /**
   * The registered kind ID.
   */
  public static final String KIND_ID = "gwid"; //$NON-NLS-1$

  /**
   * The singleton instance.
   */
  public static final UgwiKind INSTANCE = new UgwiKindGwid();

  /**
   * Constructor.
   */
  public UgwiKindGwid() {
    super( KIND_ID, OptionSetUtils.createOpSet( //
        TSID_NAME, STR_UK_GWID, //
        TSID_DESCRIPTION, STR_UK_GWID_D //
    ) );
  }

  // ------------------------------------------------------------------------------------
  // AbstractUgwiKind
  //

  @Override
  protected ValidationResult doValidateEssence( String aEssence ) {
    try {
      @SuppressWarnings( "unused" )
      Gwid foo = Gwid.of( aEssence );
      return ValidationResult.SUCCESS;
    }
    catch( Exception ex ) {
      return ValidationResult.error( FMT_ERR_INV_GWID_FORMAT, ex.getLocalizedMessage() );
    }
  }

  // ------------------------------------------------------------------------------------
  // API
  //

  /**
   * Returns the new instance of {@link Gwid} denoted by the UGWI.
   *
   * @param aUgwi {@link Ugwi} - the UGWI
   * @return {@link Gwid} - created instance
   * @throws TsNullArgumentRtException any argument = <code>null</code>
   * @throws TsValidationFailedRtException failed {@link #validateUgwi(Ugwi)}
   */
  public Gwid getGwid( Ugwi aUgwi ) {
    TsValidationFailedRtException.checkError( validateUgwi( aUgwi ) );
    return Gwid.of( aUgwi.essence() );
  }

}
